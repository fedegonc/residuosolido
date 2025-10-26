package com.residuosolido.app.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    public CloudinaryService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret
    ) {
        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        ));
    }

    public String uploadFile(MultipartFile file) throws IOException {
        Map<?, ?> rawResult = cloudinary.uploader().upload(file.getBytes(), Map.of());
        Map<String, Object> uploadResult = new HashMap<>();
        rawResult.forEach((key, value) -> {
            if (key != null) {
                uploadResult.put(key.toString(), value);
            }
        });
        Object secureUrl = uploadResult.get("secure_url");
        return secureUrl != null ? secureUrl.toString() : null;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        return uploadFile(file);
    }
}
