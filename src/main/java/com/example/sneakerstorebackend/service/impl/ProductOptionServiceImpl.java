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
