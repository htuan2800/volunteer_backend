package com.volunteerBackend.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.volunteerBackend.config.FileStorageProperties;
import com.volunteerBackend.type.FileType;

import org.springframework.core.io.Resource;

@Service
public class FileStorageServiceImp implements FileStorageService {
    private final Path fileStorageLocation;
    private final FileStorageProperties fileStorageProperties;

    public FileStorageServiceImp(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    // Store file với custom path
    public String storeFile(MultipartFile file, FileType fileType) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Tạo thư mục theo fileType nếu chưa có
            Path typeDirectory = this.fileStorageLocation.resolve(fileType.getPath());
            Files.createDirectories(typeDirectory);

            // Generate unique filename
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString() + fileExtension;

            // Copy file to the target location
            Path targetLocation = typeDirectory.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Return relative path: campaign/uuid.jpg
            return "/uploads/" + fileType.getPath() + "/" + newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public List<String> storeMultipleFiles(MultipartFile[] files, FileType fileType) {
        List<String> filePaths = new ArrayList<>();

        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue; // Skip empty files
            }
            String filePath = storeFile(file, fileType);
            filePaths.add(filePath);
        }

        return filePaths;
    }

    @Override
    // Store file với custom filename (ví dụ: userId.jpg)
    public String storeFileWithCustomName(MultipartFile file, FileType fileType, String customFileName) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            if (originalFileName.contains("..")) {
                throw new RuntimeException("Sorry! Filename contains invalid path sequence " + originalFileName);
            }

            // Tạo thư mục theo fileType
            Path typeDirectory = this.fileStorageLocation.resolve(fileType.getPath());
            Files.createDirectories(typeDirectory);

            // Get file extension
            String fileExtension = "";
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // Use custom filename
            String newFileName = customFileName + fileExtension;

            // Copy file
            Path targetLocation = typeDirectory.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return fileType.getPath() + "/" + newFileName;
        } catch (IOException ex) {
            throw new RuntimeException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    // Load file từ relative path
    public Resource loadFileAsResource(String filePath) {
        try {
            Path file = this.fileStorageLocation.resolve(filePath).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("File not found " + filePath);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("File not found " + filePath, ex);
        }
    }

    @Override
    // Delete file từ relative path
    public void deleteFile(String filePathUrl) {
        try {
            // if (filePathUrl.startsWith("http")) {
            //     String baseUrl = fileStorageProperties.getBaseUrl();
            //     filePathUrl = filePathUrl.replace(baseUrl, "");
            // }
            if (filePathUrl.startsWith("/uploads/")) {
                filePathUrl = filePathUrl.substring("/uploads/".length());
            }
            Path file = this.fileStorageLocation.resolve(filePathUrl).normalize();
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            throw new RuntimeException("Could not delete file " + filePathUrl, ex);
        }
    }
}
