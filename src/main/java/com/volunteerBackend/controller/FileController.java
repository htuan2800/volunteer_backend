package com.volunteerBackend.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.volunteerBackend.service.CloudinaryStorageService;
import com.volunteerBackend.service.FileStorageService;
import com.volunteerBackend.type.FileType;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    
    private final FileStorageService fileStorageService;


    
    private final CloudinaryStorageService cloudinaryStorageService;

    // Upload với FileType
    // @PostMapping("/upload")
    // public ResponseEntity<Map<String, String>> uploadFile(
    //         @RequestParam("file") MultipartFile file,
    //         @RequestParam("type") String type) {
        
    //     FileType fileType = FileType.valueOf(type.toUpperCase());
    //     String filePath = fileStorageService.storeFile(file, fileType);
        
    //     String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
    //             .path("/uploads/")
    //             .path(filePath)
    //             .toUriString();

    //     Map<String, String> response = new HashMap<>();
    //     response.put("filePath", filePath); // "campaign/uuid.jpg"
    //     response.put("fileUrl", fileDownloadUri);
    //     response.put("fileType", file.getContentType());

    //     return ResponseEntity.ok(response);
    // }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("type") String type) {
        
        FileType fileType = FileType.valueOf(type.toUpperCase());
        String filePath = cloudinaryStorageService.storeFile(file, fileType);

        Map<String, String> response = new HashMap<>();
        response.put("filePath", filePath); // "campaign/uuid.jpg"

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-multiple")
    public ResponseEntity<Map<String, Object>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("type") String type) {
        
        FileType fileType = FileType.valueOf(type.toUpperCase());
        List<String> filePaths = cloudinaryStorageService.storeMultipleFiles(files, fileType);
        
        List<Map<String, String>> fileInfos = new ArrayList<>();
        for (String filePath : filePaths) {
            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put("url", filePath);
            fileInfos.add(fileInfo);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Files uploaded successfully");
        response.put("totalFiles", filePaths.size());
        response.put("files", fileInfos);

        return ResponseEntity.ok(response);
    }

    // Upload user avatar với userId
    @PostMapping("/upload/user-avatar/{userId}")
    public ResponseEntity<Map<String, String>> uploadUserAvatar(
            @RequestParam("file") MultipartFile file,
            @PathVariable Long userId) {
        
        String filePath = fileStorageService.storeFileWithCustomName(
            file, 
            FileType.USER_AVATAR, 
            userId.toString()
        );
        
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .path(filePath)
                .toUriString();

        Map<String, String> response = new HashMap<>();
        response.put("filePath", filePath); // "users/avatar/1.jpg"
        response.put("fileUrl", fileDownloadUri);

        return ResponseEntity.ok(response);
    }

    // Upload organizer image
    @PostMapping("/upload/organizer")
    public ResponseEntity<Map<String, String>> uploadOrganizerImage(
            @RequestParam("file") MultipartFile file) {
        
        String filePath = fileStorageService.storeFile(
            file, 
            FileType.ORGANIZER_IMAGE
        );
        
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .path(filePath)
                .toUriString();

        Map<String, String> response = new HashMap<>();
        response.put("filePath", filePath); // "organizers/2.jpg"
        response.put("fileUrl", fileDownloadUri);

        return ResponseEntity.ok(response);
    }

    // Upload campaign image (giữ tên random)
    @PostMapping("/upload/campaign")
    public ResponseEntity<Map<String, String>> uploadCampaignImage(
            @RequestParam("file") MultipartFile file) {
        
        String filePath = fileStorageService.storeFile(file, FileType.CAMPAIGN_IMAGE);
        
        String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/files/")
                .path(filePath)
                .toUriString();

        Map<String, String> response = new HashMap<>();
        response.put("filePath", filePath); // "campaign/uuid.jpg"
        response.put("fileUrl", fileDownloadUri);

        return ResponseEntity.ok(response);
    }

    // Get file - support nested path
    @GetMapping("/**")
    public ResponseEntity<Resource> downloadFile(HttpServletRequest request) {
        // Extract full path after /api/files/
        String fullPath = request.getRequestURI().split("/api/files/")[1];
        
        Resource resource = fileStorageService.loadFileAsResource(fullPath);

        String contentType = null;
        try {
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            // Could not determine file type
        }

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // Delete file - support nested path
    @DeleteMapping("/**")
    public ResponseEntity<Map<String, String>> deleteFile(HttpServletRequest request) {
        String fullPath = request.getRequestURI().split("/api/files/")[1];
        fileStorageService.deleteFile(fullPath);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "File deleted successfully");
        response.put("filePath", fullPath);
        
        return ResponseEntity.ok(response);
    }
}