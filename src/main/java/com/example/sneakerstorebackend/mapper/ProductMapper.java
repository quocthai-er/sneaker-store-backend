package com.example.sneakerstorebackend.mapper;

import com.example.sneakerstorebackend.config.ConstantsConfig;
import com.example.sneakerstorebackend.domain.exception.NotFoundException;
import com.example.sneakerstorebackend.domain.payloads.request.ProductRequest;
import com.example.sneakerstorebackend.domain.payloads.response.ProductListResponse;
import com.example.sneakerstorebackend.domain.payloads.response.ProductResponse;
import com.example.sneakerstorebackend.entity.Brand;
import com.example.sneakerstorebackend.entity.Category;
import com.example.sneakerstorebackend.entity.product.Product;
import com.example.sneakerstorebackend.entity.product.ProductImage;
import com.example.sneakerstorebackend.repository.BrandRepository;
import com.example.sneakerstorebackend.repository.CategoryRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProductMapper {
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;

    public Product toProduct(ProductRequest req) {
        Optional<Category> category = categoryRepository.findCategoryByIdAndState(req.getCategory(), ConstantsConfig.ENABLE);
        Optional<Brand> brand = brandRepository.findBrandByIdAndState(req.getBrand(), ConstantsConfig.ENABLE);
        if (category.isEmpty() || brand.isEmpty())
            throw new NotFoundException("Can not found category or brand");
        return new Product(req.getName(), req.getDescription(), req.getPrice(),
                category.get(), brand.get(), ConstantsConfig.ENABLE, req.getDiscount());
    }

    public ProductListResponse toProductListRes(Product req) {
        List<ProductImage> images = new ArrayList<>();
        if (!req.getImages().isEmpty()) {
            images = req.getImages().stream()
                    .filter(ProductImage::isThumbnail).distinct().collect(Collectors.toList());
            if (images.isEmpty()) images = req.getImages();
        }
        BigDecimal extra = BigDecimal.ZERO;
        if (!req.getAttr().isEmpty()) extra = req.getProductOptions().get(0).getExtraFee();
        String discountString = (req.getPrice().add(extra)).multiply(BigDecimal.valueOf((double) (100- req.getDiscount())/100))
                .stripTrailingZeros().toPlainString();
        BigDecimal discountPrice = new BigDecimal(discountString);
        return new ProductListResponse(req.getId(), req.getName(), req.getDescription(),
                req.getPrice().add(extra),discountPrice, req.getDiscount(), req.getCategory().getId(),
                req.getCategory().getName(), req.getBrand().getId(),
                req.getBrand().getName(), req.getState(), req.getCreatedDate(), req.getAttr(), images);
    }

    public ProductResponse toProductRes(Product req) {
        String discountString = req.getPrice().multiply(BigDecimal.valueOf((double) (100- req.getDiscount())/100))
                .stripTrailingZeros().toPlainString();
        BigDecimal discountPrice = new BigDecimal(discountString);
        return new ProductResponse(req.getId(), req.getName(), req.getDescription(),
                req.getPrice(),discountPrice, req.getDiscount(),
                req.getCategory().getId(), req.getCategory().getName(),req.getBrand().getId(),
                req.getBrand().getName(), req.getState(), req.getAttr(), req.getProductOptions(), req.getImages());
    }
}
