package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class FileProcessingServiceImpl implements FileProcessingService {
    private static final String UPLOAD_ROOT_PATH = "C:/uploads";
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
    public String processPhotoAndReturnURL(MultipartFile photo, Path uploadDir, String subDirectory) {
        validateFileSize(photo, 10);
        String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        try {
            compressAndSaveImage(photo, filePath);
            return filePath.toAbsolutePath().toString();
            // 🔥 ВОЗВРАЩАЕМ URL-ПУТЬ, КОТОРЫЙ БУДЕТ ИСПОЛЬЗОВАТЬ ФРОНТЕНД
           // return "/uploads/" + subDirectory + "/" + fileName;
        } catch (IOException e) {
            log.error("Ошибка при обработке фото '{}': {}", photo.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("Ошибка при обработке фото", e);
        }
    }


    private void validateFileSize(MultipartFile file, int maxSizeMb) {
        long maxSizeBytes = maxSizeMb * 1024L * 1024L;
        if (file.getSize() > maxSizeBytes) {
            log.warn("Файл '{}' превышает допустимый размер {} МБ ({} байт)",
                    file.getOriginalFilename(), maxSizeMb, file.getSize());
            throw new IllegalArgumentException("Размер файла превышает " + maxSizeMb + " МБ");
        }
    }

    private void compressAndSaveImage(MultipartFile imageFile, Path outputPath) throws IOException {
        BufferedImage image = ImageIO.read(imageFile.getInputStream());
        if (image == null) {
            throw new IllegalArgumentException("Неверный формат изображения");
        }

        try (OutputStream os = Files.newOutputStream(outputPath);
             ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {

            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) throw new IllegalStateException("JPEG writer не найден");

            ImageWriter writer = writers.next();
            writer.setOutput(ios);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.6f); // 60% качества
            }

            writer.write(null, new IIOImage(image, null, null), param);
            writer.dispose();
        }

        log.info("📸 Фото успешно сжато и сохранено: {}", outputPath);
    }
}
