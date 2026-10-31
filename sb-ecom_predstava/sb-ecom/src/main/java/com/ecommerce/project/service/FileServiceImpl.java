package com.ecommerce.project.service;

import com.cloudinary.Cloudinary;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {

    private final Cloudinary cloudinary;

    public FileServiceImpl(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

        Map<String, Object> options = new HashMap<>();
        options.put("folder", "theatre/performances");
        options.put("resource_type", "image");

        Map<?, ?> uploadResult = cloudinary.uploader().upload(
                file.getBytes(),
                options
        );

        return (String) uploadResult.get("secure_url");
    }
}