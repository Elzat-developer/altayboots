package altay.boots.altayboots.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileProcessingService {
    void deleteFileFromDisk(String relativePhotoUrl);
    String processPhotoAndReturnURL(MultipartFile photo, String subDirectory);
}
