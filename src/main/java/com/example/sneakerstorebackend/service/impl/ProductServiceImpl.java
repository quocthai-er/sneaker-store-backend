package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.CloudinaryConfig;
import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.ProductPriceAndDiscount;
import com.example.sneakerstorebackend.domain.payloads.request.ProductRequest;
import com.example.sneakerstorebackend.domain.payloads.response.ProductListResponse;
import com.example.sneakerstorebackend.domain.payloads.response.ProductResponse;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.Brand;
import com.example.sneakerstorebackend.entity.Category;
import com.example.sneakerstorebackend.entity.product.Product;
import com.example.sneakerstorebackend.entity.product.ProductAttribute;
import com.example.sneakerstorebackend.entity.product.ProductImage;
import com.example.sneakerstorebackend.mapper.ProductMapper;
import com.example.sneakerstorebackend.repository.BrandRepository;
import com.example.sneakerstorebackend.repository.CategoryRepository;
import com.example.sneakerstorebackend.repository.ProductRepository;
import com.example.sneakerstorebackend.service.ProductService;
import com.mongodb.MongoWriteException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    private final ProductMapper productMapper;

    private final CategoryRepository categoryRepository;

    private final BrandRepository brandRepository;

    private final CloudinaryConfig cloudinary;


    @Override
    public ResponseEntity<?> findAll(String state, Pageable pageable) {
        Page<Product> products;
        if (state.equalsIgnoreCase(ConstantsConfig.ENABLE) || state.equalsIgnoreCase(ConstantsConfig.DISABLE))
            products = productRepository.findAllByState(state.toLowerCase(), pageable);
        else products = productRepository.findAll(pageable);
        List<ProductListResponse> resList = products.getContent().stream().map(productMapper::toProductListRes).collect(Collectors.toList());
        ResponseEntity<?> resp = addPageableToRes(products, resList);
        if (resp != null) return resp;
        throw new NotFoundException("Can not found any product");
    }

    private ResponseEntity<?> addPageableToRes(Page<Product> products, List<ProductListResponse> resList) {
        Map<String, Object> resp = new HashMap<>();
        resp.put("list", resList);
        resp.put("totalQuantity", products.getTotalElements());
        resp.put("totalPage", products.getTotalPages());
        if (!resList.isEmpty() )
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get all product success", resp));
        return null;
    }

    @Override
    public ResponseEntity<?> findById(String id, String userId) {
        Optional<Product> product = productRepository.findProductByIdAndState(id, ConstantsConfig.ENABLE);
        if (product.isPresent()) {
            ProductResponse res = productMapper.toProductRes(product.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get product success", res));
        }
        throw new NotFoundException("Can not found any product with id: "+id);
    }

    @Override
    public ResponseEntity<?> findByCategoryIdOrBrandId(String id, Pageable pageable) {
        Page<Product> products;
        try {
            Optional<Category> category = categoryRepository.findCategoryByIdAndState(id, ConstantsConfig.ENABLE);
            if (category.isPresent()) {
                List<ObjectId> subCat = category.get().getSubCategories().stream().map(c -> new ObjectId(c.getId())).collect(Collectors.toList());
                products = productRepository.findProductsByCategory(new ObjectId(id),
                        subCat, pageable);
            } else products = productRepository.findAllByCategory_IdOrBrand_IdAndState(new ObjectId(id),
                    new ObjectId(id),ConstantsConfig.ENABLE, pageable);
        } catch (Exception e) {
            throw new AppException(HttpStatus.BAD_REQUEST.value(), "Error when finding");
        }
        List<ProductListResponse> resList = products.stream().map(productMapper::toProductListRes).collect(Collectors.toList());
        ResponseEntity<?> resp = addPageableToRes(products, resList);
        if (resp != null) return resp;
        throw new NotFoundException("Can not found any product with category or brand id: "+id);
    }

    @Override
    public ResponseEntity<?> search(String key, Pageable pageable) {
        Page<Product> products;
        try {
            products = productRepository.findAllBy(TextCriteria
                            .forDefaultLanguage().matchingAny(key), pageable);
        } catch (Exception e) {
            throw new NotFoundException("Can not found any product with: "+key);
        }
        List<ProductListResponse> resList = products.getContent().stream().map(productMapper::toProductListRes).collect(Collectors.toList());
        ResponseEntity<?> resp = addPageableToRes(products, resList);
        if (resp != null) return resp;
        throw new NotFoundException("Can not found any product with: "+key);
    }

    @Override
    public ResponseEntity<?> addProduct(ProductRequest req) {
        if (req != null) {
            Product product = productMapper.toProduct(req);
            try {
                productRepository.save(product);
            } catch (Exception e) {
                throw new AppException(HttpStatus.CONFLICT.value(), "Product name already exists");
            }
            ProductResponse res = productMapper.toProductRes(product);
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseObject(true, "Add product successfully ", res)
            );
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                new ResponseObject(false, "Request is null", "")
        );
    }

    @Override
    public ResponseEntity<?> updateProduct(String id, ProductRequest req) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent() && req != null) {
            processUpdate(req, product.get());
            try {
                productRepository.save(product.get());
            } catch (MongoWriteException e) {
                throw new AppException(HttpStatus.CONFLICT.value(), "Product name already exists");
            } catch (Exception e) {
                throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
            }
            ProductResponse res = productMapper.toProductRes(product.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Update product successfully ", res)
            );
        }
        throw new NotFoundException("Can not found product with id: "+id);
    }

    @Override
    public ResponseEntity<?> deactivatedProduct(String id) {
        Optional<Product> product = productRepository.findProductByIdAndState(id, ConstantsConfig.ENABLE);
        if (product.isPresent()) {
            product.get().setState(ConstantsConfig.DISABLE);
            productRepository.save(product.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Delete product successfully ", "")
            );
        } throw new NotFoundException("Can not found product with id: "+id);    }

    @Override
    public ResponseEntity<?> addImagesToProduct(String id, String color, List<MultipartFile> files) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            try {
                if (files == null || files.isEmpty() || color.isEmpty()) throw new AppException(HttpStatus.BAD_REQUEST.value(), "Images and color is require");
                files.forEach(f -> {
                    try {
                        String url = cloudinary.uploadImage(f, null);
                        product.get().getImages().add(new ProductImage(UUID.randomUUID().toString(), url, false, color));
                    } catch (IOException e) {
                        log.error(e.getMessage());
                        throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Error when upload images");
                    }
                    productRepository.save(product.get());
                });
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, "Add image to product successfully", product.get().getImages())
                );
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new NotFoundException("Error when save image: " + e.getMessage());
            }
        } throw new NotFoundException("Can not found product with id: " + id);
    }

    @Override
    public ResponseEntity<?> addAttribute(String id, ProductAttribute request) {
        Optional<Product> product = productRepository.findProductByIdAndState(id, ConstantsConfig.ENABLE);
        if (product.isPresent()) {
            if (product.get().getAttr().stream().anyMatch(a -> a.getName().equals(request.getName())))
                throw new AppException(HttpStatus.CONFLICT.value(), "Attribute name already exists");
            ProductAttribute attribute = new ProductAttribute(request.getName(), request.getVal());
            product.get().getAttr().add(attribute);
            productRepository.save(product.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Add attribute successfully", attribute)
            );
        } throw new NotFoundException("Can not found product with id: "+id);
    }

    @Override
    public ResponseEntity<?> updateAttribute(String id, String oldName, ProductAttribute request) {
        Optional<Product> product = productRepository.findProductByIdAndState(id, ConstantsConfig.ENABLE);
        if (product.isPresent()) {
            AtomicBoolean existAttr = new AtomicBoolean(false);
            product.get().getAttr().forEach(a -> {
                if (a.getName().equals(oldName)) {
                    existAttr.set(true);
                    a.setName(request.getName());
                    a.setVal(request.getVal());
                }
            });
            if (!existAttr.get()) throw new NotFoundException("Can not found attribute " + oldName + " with product id " + id);
            productRepository.save(product.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Update attribute successfully", request)
            );
        } throw new NotFoundException("Can not found product with id: "+id);
    }

    @Override
    public ResponseEntity<?> updateMultiplePriceAndDiscount(ProductPriceAndDiscount request) {
        List<Product> products = productRepository.findAllByIdIsIn(List.of(request.getId().split(",")));
        if (products.isEmpty()) throw new NotFoundException("Can not found any product with id: " + request.getId());
        else {
            products.stream().forEach(p -> {
                if (request.getPrice() != null && !request.getPrice().equals(p.getPrice()))
                    p.setPrice(request.getPrice());
                if (request.getDiscount() != -1 && request.getDiscount() != p.getDiscount())
                    p.setDiscount(request.getDiscount());
            });
            try {
                productRepository.saveAll(products);
            } catch (Exception e) {
                throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Update product price and discount successfully ", request)
            );
        }
    }

    @Override
    public ResponseEntity<?> updatePriceAndDiscount(ProductPriceAndDiscount request) {
        Optional<Product> product = productRepository.findById(request.getId());
        if (product.isPresent()) {
            if (request.getPrice() != null && !request.getPrice().equals(product.get().getPrice()))
                product.get().setPrice(request.getPrice());
            if (request.getDiscount() != -1 && request.getDiscount() != product.get().getDiscount())
                product.get().setDiscount(request.getDiscount());
            try {
                productRepository.save(product.get());
            } catch (Exception e) {
                throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Update product price and discount successfully ", request)
            );
        }
        throw new NotFoundException("Can not found product with id: "+ request.getId());
    }

    @Override
    public ResponseEntity<?> deleteAttribute(String id, String name) {
        Optional<Product> product = productRepository.findProductByIdAndState(id, ConstantsConfig.ENABLE);
        if (product.isPresent() && !name.isBlank()) {
            product.get().getAttr().removeIf(a -> a.getName().equals(name));
            productRepository.save(product.get());
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Delete attribute successfully", "")
            );
        } throw new NotFoundException("Can not found product with id: "+id);    }

    @Override
    public ResponseEntity<?> deleteImageFromProduct(String id, String imageId) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent() && !product.get().getImages().isEmpty()) {
            try {
                Optional<ProductImage> checkDelete = product.get().getImages().stream().filter(i -> i.getImageId().equals(imageId)).findFirst();
                if (checkDelete.isPresent()) {
                    cloudinary.deleteImage(checkDelete.get().getUrl());
                    product.get().getImages().remove(checkDelete.get());
                    productRepository.save(product.get());
                    return ResponseEntity.status(HttpStatus.OK).body(
                            new ResponseObject(true, "Delete image successfully", imageId)
                    );
                } else throw new NotFoundException("Can not found image in product with id: " + imageId);
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new NotFoundException("Can not found product with id: " + id);
            }
        } throw new NotFoundException("Can not found any image or product with id: " + id);
    }

    public void processUpdate(ProductRequest req, Product product) {
        if (!req.getName().equals(product.getName()))
            product.setName(req.getName());
        if (!req.getDescription().equals(product.getDescription()))
            product.setDescription(req.getDescription());
        if (!req.getPrice().equals(product.getPrice()))
            product.setPrice(req.getPrice());
        if (req.getDiscount() != product.getDiscount())
            product.setDiscount(req.getDiscount());
        if (!req.getCategory().equals(product.getCategory().getId())) {
            Optional<Category> category = categoryRepository.findCategoryByIdAndState(req.getCategory(), ConstantsConfig.ENABLE);
            if (category.isPresent())
                product.setCategory(category.get());
            else throw new NotFoundException("Can not found category with id: "+req.getCategory());
        }
        if (!req.getBrand().equals(product.getBrand().getId())) {
            Optional<Brand> brand = brandRepository.findBrandByIdAndState(req.getBrand(), ConstantsConfig.ENABLE);
            if (brand.isPresent())
                product.setBrand(brand.get());
            else throw new NotFoundException("Can not found brand with id: "+req.getBrand());
        }
        if (req.getState() != null && !req.getState().isEmpty() &&
                (req.getState().equalsIgnoreCase(ConstantsConfig.ENABLE) ||
                        req.getState().equalsIgnoreCase(ConstantsConfig.DISABLE)))
            product.setState(req.getState());
        else throw new AppException(HttpStatus.BAD_REQUEST.value(), "Invalid state");
    }
}
