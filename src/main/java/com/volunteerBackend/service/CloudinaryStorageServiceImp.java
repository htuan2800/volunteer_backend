package com.volunteerBackend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.volunteerBackend.type.FileType;

@Service
public class CloudinaryStorageServiceImp implements CloudinaryStorageService {

    private final Cloudinary cloudinary;

    public CloudinaryStorageServiceImp(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Tải file lên và trả về PUBLIC_ID (không phải URL).
     * public_id là "chìa khóa" để bạn quản lý (xóa, sửa) file sau này.
     */
    @Override
    public String storeFile(MultipartFile file, FileType fileType) {

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "upload_preset", fileType.getPath(),
                            "resource_type", "auto"));

            // THAY ĐỔI 3: Trả về public_id để có thể xóa
            return uploadResult.get("public_id").toString();

        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not upload file to Cloudinary. Error: " + e.getMessage(), e);
        }
    }

    /**
     * Tải lên nhiều file và trả về danh sách các public_id.
     */
    @Override
    public List<String> storeMultipleFiles(MultipartFile[] files, FileType fileType) {
        List<String> publicIds = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // Tự động gọi phương thức storeFile đã được sửa ở trên
                publicIds.add(storeFile(file, fileType));
            }
        }
        return publicIds; // Giờ đây trả về List<String> các public_id
    }

    /**
     * Tải file lên với tên (public_id) tùy chỉnh và trả về public_id đầy đủ.
     */
    @Override
    public String storeFileWithCustomName(MultipartFile file, FileType fileType, String customFileName) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getInputStream(),
                    ObjectUtils.asMap(
                            "folder", fileType.getPath(),
                            "public_id", customFileName,
                            "resource_type", "auto"));
            return uploadResult.get("public_id").toString();

        } catch (IOException e) {
            throw new RuntimeException("Could not upload file to Cloudinary", e);
        }
    }

    /**
     * Xóa file khỏi Cloudinary bằng public_id.
     */
    @Override
    public void deleteFile(String publicId) {
    try {
        // Thử xóa như "image" (mặc định)
        cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        
    } catch (Exception e1) {
        try {
            // Nếu thất bại, thử xóa như "video"
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "video"));
        } catch (Exception e2) {
            try {
                 // Nếu vẫn thất bại, thử xóa như "raw" (PDF, txt)
                cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));
            } catch (Exception e3) {
                 // Nếu cả 3 đều thất bại, ném ra lỗi
                 throw new RuntimeException("Could not delete file from Cloudinary: " + publicId + " " + e3.getMessage(), e3);
            }
        }
    }
}

    @Override
    public Resource loadFileAsResource(String filePath) {
        throw new UnsupportedOperationException("Use Cloudinary URL directly instead of local resource");
    }

}