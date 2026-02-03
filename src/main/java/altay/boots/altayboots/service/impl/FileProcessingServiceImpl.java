package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class FileProcessingServiceImpl implements FileProcessingService {
    private static final String UPLOAD_ROOT_PATH = "C:/uploads";
    private static final int MAX_FILE_SIZE_MB = 10;
    @Override
    public void deleteFileFromDisk(String relativePhotoUrl) {
        if (relativePhotoUrl == null || relativePhotoUrl.trim().isEmpty()) {
            // Если путь пуст, нечего удалять.
            return;
        }

        // 1. Создаем абсолютный путь к файлу
        // (Например: /home/app/uploads + /promotions/a8b9c1d2.jpg)
        Path filePath = Paths.get(UPLOAD_ROOT_PATH, relativePhotoUrl);

        try {
            boolean deleted = Files.deleteIfExists(filePath); // Удаляем, только если существует

            if (deleted) {
                log.debug("Успешно удален файл: " + filePath);
            } else {
                // WARN (опционально): Файл не найден, но это не критическая ошибка, просто предупреждение.
                log.warn("Предупреждение: Файл для удаления не найден: " + filePath);
            }
        } catch (IOException e) {
            // Логируем ошибку, но не бросаем RuntimeException, чтобы не прерывать транзакцию
            // (Мы все равно удалили ссылку на файл из БД, даже если сам файл остался на диске).
            log.error("Ошибка при удалении файла с диска: " + filePath + ". Причина: " + e.getMessage());
        }
    }

    @Override
    public String processPhotoAndReturnURL(MultipartFile photo, String subDirectory) {
        validateFileSize(photo);

        // 1. Подготавливаем директорию
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать директорию " + uploadDir, e);
        }

        // 2. Генерируем имя ОДИН РАЗ
        String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);

        // 3. Сжимаем и сохраняем
        try {
            compressAndSaveImage(photo, filePath);
            return "/uploads/" + subDirectory + "/" + fileName;
        } catch (IOException e) {
            log.error("Ошибка при сохранении файла: {}", fileName, e);
            throw new RuntimeException("Ошибка при обработке фото", e);
        }
    }


    private void validateFileSize(MultipartFile file) {
        long maxSizeBytes = MAX_FILE_SIZE_MB * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            log.warn("Файл '{}' превышает допустимый размер {} МБ ({} байт)",
                    file.getOriginalFilename(), MAX_FILE_SIZE_MB, file.getSize());
            throw new IllegalArgumentException("Размер файла превышает " + MAX_FILE_SIZE_MB + " МБ");
        }
    }

    private void compressAndSaveImage(MultipartFile imageFile, Path filePath) throws IOException {
        String contentType = imageFile.getContentType();

        if (contentType.startsWith("image/")) {
            log.info("📸 Сжимаем изображение: {}", filePath.getFileName());

            Thumbnails.of(imageFile.getInputStream())
                    .size(1600, 1600)
                    .outputQuality(0.8)
                    .toFile(filePath.toFile());
        }
    }
}
