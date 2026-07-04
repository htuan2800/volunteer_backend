package com.volunteerBackend.service;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.volunteerBackend.type.FileType;

public interface CloudinaryStorageService {
    public String storeFile(MultipartFile file, FileType fileType);
    public String storeFileWithCustomName(MultipartFile file, FileType fileType, String customFileName);
    public List<String> storeMultipleFiles(MultipartFile[] files, FileType fileType);
    public Resource loadFileAsResource(String filePath);
    public void deleteFile(String publicId);
}
