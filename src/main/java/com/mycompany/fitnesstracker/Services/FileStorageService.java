package com.mycompany.fitnesstracker.Services;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final String uploadDir = "uploads/exercises/";

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            log.error("Could not create upload folder!");
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    public String saveFile(MultipartFile file) {
        try {
            String originalFilename = file.getOriginalFilename();
            // Дістаємо розширення файлу (наприклад, .png або .jpg)
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Генеруємо унікальне ім'я (наприклад: 550e8400-e29b-41d4-a716-446655440000.jpg)
            String newFilename = UUID.randomUUID().toString() + extension;
            Path filePath = Paths.get(uploadDir).resolve(newFilename);

            // Зберігаємо файл
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // Повертаємо URL-шлях, за яким фронтенд зможе дістати цю картинку
            return "/uploads/exercises/" + newFilename;

        } catch (Exception e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Failed to store file", e);
        }
    }
}