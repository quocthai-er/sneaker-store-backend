package com.example.sneakerstorebackend.domain.payloads.request;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ImageRequest {
    String color;
    String imageId;
    List<MultipartFile> files;
}
