package com.example.sneakerstorebackend.service.impl;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.AppException;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.ProductOptionRequest;
import com.example.sneakerstorebackend.domain.payloads.response.ResponseObject;
import com.example.sneakerstorebackend.entity.product.Product;
import com.example.sneakerstorebackend.entity.product.ProductOption;
import com.example.sneakerstorebackend.entity.product.ProductVariant;
import com.example.sneakerstorebackend.repository.ProductOptionRepository;
import com.example.sneakerstorebackend.repository.ProductRepository;
import com.example.sneakerstorebackend.service.ProductOptionService;
import com.mongodb.MongoWriteException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
@Slf4j
public class ProductOptionServiceImpl implements ProductOptionService {
    private final ProductRepository productRepository;
    private final ProductOptionRepository productOptionRepository;
    @Override
    public ResponseEntity<?> addOption(String productId, ProductOptionRequest req) {
        Optional<ProductOption> checkOption = productOptionRepository.findByNameAndVariantsColorAndProductId(
                req.getName(), req.getColor(), new ObjectId(productId));
        if (checkOption.isPresent()) {
            throw new AppException(HttpStatus.CONFLICT.value(),
                    String.format("Option with name: %s, color code: %s, product id: %s already exists",
                            req.getName(), req.getColor(), productId));
        }
        Optional<ProductOption> option = productOptionRepository.findByNameAndProduct_Id(req.getName(), new ObjectId(productId));
        Optional<Product> product = productRepository.findProductByIdAndState(productId, ConstantsConfig.ENABLE);
        if (product.isEmpty()) throw new NotFoundException("Can not found product with id: "+productId);
        // case does not exist size
        if (option.isEmpty()) {
            ProductOption productOption = new ProductOption(req.getName(), req.getExtraFee());
            productOption.setProduct(product.get());
            processVariant(productOption, req.getColor(), req.getStock());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseObject(true, "Add product option success", productOption));
        } else {
            processVariant(option.get(), req.getColor(), req.getStock());
            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ResponseObject(true, "Add product option success", option.get()));
        }
    }

    @Override
    public ResponseEntity<?> updateOptionVariant(String id, String variantColor, ProductOptionRequest req) {
        Optional<ProductOption> productOption = productOptionRepository.findByIdAndVariantColor(id, variantColor);
        if (productOption.isPresent()) {
            productOption.get().setName(req.getName());
            productOption.get().setExtraFee(req.getExtraFee());
            productOption.get().getVariants().forEach(variant -> {
                if (variant.getColor().equals(variantColor)) {
                    variant.setStock(req.getStock());
                    if (!variant.getColor().equals(req.getColor())) {
                        variant.setColor(req.getColor());
                    }
                }
            });
            try {
                productOptionRepository.save(productOption.get());
                return ResponseEntity.status(HttpStatus.OK).body(
                        new ResponseObject(true, "Update product option success", productOption.get()));
            } catch (Exception e) {
                log.error(e.getMessage());
                throw new AppException(HttpStatus.EXPECTATION_FAILED.value(), "Error when update option");
            }

        } throw new NotFoundException("Can not found product option with id: "+id);
    }

    @Override
    public ResponseEntity<?> findOptionById(String id) {
        Optional<ProductOption> productOption = productOptionRepository.findById(id);
        if (productOption.isPresent())
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get product option success", productOption.get()));
        throw new NotFoundException("Can not found product option with id: "+id);
    }

    @Override
    public ResponseEntity<?> findOptionByProductId(String id) {
        List<ProductOption> productOptions = productOptionRepository.findAllByProduct_Id(new ObjectId(id));
        if (productOptions.size() > 0) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ResponseObject(true, "Get product option success", productOptions));
        } throw new NotFoundException("Can not found any product option with id: "+id);
    }

    public void processVariant (ProductOption productOption ,String color,
                                Long stock) {
        ProductVariant variants = new ProductVariant(UUID.randomUUID(), color, stock);
        productOption.getVariants().add(variants);
        try {
            productOptionRepository.save(productOption);
        } catch (MongoWriteException e) {
            log.error(e.getMessage());
            throw new AppException(HttpStatus.CONFLICT.value(), "Color already exists");
        }
    }

}
