package altay.boots.altayboots.service;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileProcessingService {
    void deleteFileFromDisk(String relativePhotoUrl);
    String processPhotoAndReturnURL(MultipartFile photo, Path uploadDir, String subDirectory);
}
