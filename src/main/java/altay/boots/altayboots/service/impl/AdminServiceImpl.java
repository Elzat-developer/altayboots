package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.model.entity.*;
import altay.boots.altayboots.repository.CatalogRepo;
import altay.boots.altayboots.repository.CompanyRepo;
import altay.boots.altayboots.repository.ProductRepo;
import altay.boots.altayboots.repository.PromotionRepo;
import altay.boots.altayboots.service.AdminService;
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
import java.util.List;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final ProductRepo productRepo;
    private final CatalogRepo catalogRepo;
    private final CompanyRepo companyRepo;
    private final PromotionRepo promotionRepo;
    @Override
    public void createProduct(CreateProduct createProduct) {
        Product product = new Product();
        product.setName(createProduct.name());
        product.setDescription(createProduct.description());
        product.setText(createProduct.text());
        product.setPrice(createProduct.price());
        product.setOldPrice(createProduct.oldPrice());

        Catalog catalog = catalogRepo.findById(createProduct.catalog_id());
        product.setCatalog(catalog);

        productRepo.save(product);

        Path uploadDir = Paths.get("C:/uploads/products");
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📷 СОХРАНЕНИЕ НЕСКОЛЬКИХ ФОТО
        if (createProduct.photos() != null) {
            for (MultipartFile file : createProduct.photos()) {

                if (!file.isEmpty()) {
                    String photoPath = processPhoto(file, uploadDir);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoPath);
                    photo.setProduct(product);

                    product.getPhotos().add(photo);
                }
            }
        }

        productRepo.save(product);
    }


    @Override
    public List<GetProduct> getProducts() {
        List<Product> products = productRepo.findAll();
        return products.stream()
                .map(this::toDtoProduct)
                .toList();
    }

    @Override
    public GetProduct getProduct(int productId) {
        Product product = productRepo.findById(productId);

        List<String> photoList = product.getPhotos()
                .stream()
                .map(ProductPhoto::getPhotoURL)
                .toList();

        return new GetProduct(
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                photoList,
                product.getCatalog().getId(),
                product.getPaidStatus()
        );
    }


    @Override
    public void editProduct(int product_id, CreateProduct createProduct) {
        Product product = productRepo.findById(product_id);

        product.setName(createProduct.name());
        product.setDescription(createProduct.description());
        product.setText(createProduct.text());
        product.setPrice(createProduct.price());
        product.setOldPrice(createProduct.oldPrice());
        product.setPaidStatus(createProduct.paidStatus());

        Path uploadDir = Paths.get("C:/uploads/products");
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📌 ЕСЛИ ПРИШЛИ НОВЫЕ ФОТО — УДАЛЯЕМ СТАРЫЕ
        if (createProduct.photos() != null && !createProduct.photos().isEmpty()) {
            product.getPhotos().clear();

            for (MultipartFile file : createProduct.photos()) {
                if (!file.isEmpty()) {
                    String photoPath = processPhoto(file, uploadDir);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoPath);
                    photo.setProduct(product);

                    product.getPhotos().add(photo);
                }
            }
        }

        productRepo.save(product);
    }


    @Override
    public void deleteProduct(Integer productId) {
        productRepo.deleteById(productId);
    }

    @Override
    public void createCatalog(CreateCatalog createCatalog) {
        Catalog catalog = new Catalog();
        catalog.setName(catalog.getName());
        catalogRepo.save(catalog);
    }

    @Override
    public List<CreateCatalog> getCatalogs() {
        List<Catalog> catalogs = catalogRepo.findAll();
        return catalogs.stream()
                .map(this::toDtoCatalog)
                .toList();
    }

    @Override
    public void editCatalog(int catalogId, CreateCatalog catalog) {
        Catalog catalogRepoById = catalogRepo.findById(catalogId);
        catalogRepoById.setName(catalog.name());
        catalogRepo.save(catalogRepoById);
    }

    @Override
    public List<GetProduct> getProductsCatalog(int catalogId) {
        Catalog catalog = catalogRepo.findById(catalogId);

        if (catalog == null) {
            throw new RuntimeException("Catalog not found");
        }

        // Получаем список продуктов
        List<Product> products = catalog.getProducts();

        return products.stream()
                .map(this::toDtoProduct)
                .toList();
    }

    @Override
    public void deleteCatalog(Integer catalogId) {
        catalogRepo.deleteById(catalogId);
    }

    @Override
    public void createCompanyDescription(CreateCompanyDescription createCompanyDescription) {
        Company company = new Company();
        company.setName(createCompanyDescription.name());
        company.setText(createCompanyDescription.text());

        Path uploadDir = Paths.get("C:/uploads/company");
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📷 Фото
        if (createCompanyDescription.photoURL() != null && !createCompanyDescription.photoURL().isEmpty()) {
            String photoPath = processPhoto(createCompanyDescription.photoURL(), uploadDir);
            company.setPhotoURL(photoPath);
            log.info("✅ Фото успешно сохранено: {}", photoPath);
        }

        company.setBase(createCompanyDescription.base());
        company.setCity(createCompanyDescription.city());
        companyRepo.save(company);
    }

    @Override
    public CompanyDescription getCompany() {
        Company company = companyRepo.findById(1);
        return new CompanyDescription(
                company.getName(),
                company.getText(),
                company.getPhotoURL(),
                company.getBase(),
                company.getCity()
        );
    }

    @Override
    public void editCompany(CreateCompanyDescription companyDescription) {
        Company company = companyRepo.findById(1);

        company.setName(companyDescription.name());
        company.setText(companyDescription.text());


        Path uploadDir = Paths.get("C:/uploads/company");
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📷 Фото
        if (companyDescription.photoURL() != null && !companyDescription.photoURL().isEmpty()) {
            String photoPath = processPhoto(companyDescription.photoURL(), uploadDir);
            company.setPhotoURL(photoPath);
            log.info("✅ Фото успешно сохранено: {}", photoPath);
        }

        company.setBase(companyDescription.base());
        company.setCity(companyDescription.city());

        companyRepo.save(company);
    }

    @Override
    public void createPromotion(CreatePromotion createPromotion) {
        Promotion promotion = new Promotion();
        promotion.setName(createPromotion.name());
        promotion.setDescription(createPromotion.description());
        promotion.setPercentageDiscounted(createPromotion.percentageDiscounted());
        Catalog catalog = catalogRepo.findById(createPromotion.catalogId());
        promotion.setCatalog(catalog);
        Product product = productRepo.findById(createPromotion.productId());
        promotion.setProduct(product);
        promotion.setStartDate(createPromotion.startDate());
        promotion.setEndDate(createPromotion.endDate());

        Path uploadDir = Paths.get("C:/uploads/promotions");
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📷 СОХРАНЕНИЕ НЕСКОЛЬКИХ ФОТО
        if (createPromotion.photos() != null) {
            for (MultipartFile file : createPromotion.photos()) {

                if (!file.isEmpty()) {
                    String photoPath = processPhoto(file, uploadDir);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoPath);
                    photo.setProduct(product);

                    product.getPhotos().add(photo);
                }
            }
        }
        promotionRepo.save(promotion);
    }

    @Override
    public List<GetPromotion> getPromotions() {
        List<Promotion> promotions = promotionRepo.findAll();
        return promotions.stream()
                .map(this::toDtoPromotion)
                .toList();
    }

    @Override
    public GetPromotion getPromotion(int promotionId) {
        Promotion promotion = promotionRepo.findById(promotionId);
        return new GetPromotion(
                promotion.getName(),
                promotion.getDescription(),
                promotion.getPhotos()
                        .stream()
                        .map(ProductPhoto::getPhotoURL)
                        .toList(),
                promotion.getPercentageDiscounted(),
                promotion.isGlobal(),
                promotion.getCatalog().getId(),
                promotion.getProduct().getId(),
                promotion.getStartDate(),
                promotion.getEndDate()
        );
    }

    @Override
    public void editPromotion(int promotionId, CreatePromotion createPromotion) {
        Promotion promotion = promotionRepo.findById(promotionId);
        promotion.setName(createPromotion.name());
        promotion.setDescription(createPromotion.description());
        promotion.setPercentageDiscounted(createPromotion.percentageDiscounted());
        promotion.setGlobal(createPromotion.global());
        promotion.setStartDate(createPromotion.startDate());
        promotion.setEndDate(createPromotion.endDate());

        Path uploadDir = Paths.get("C:/uploads/products");
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📌 ЕСЛИ ПРИШЛИ НОВЫЕ ФОТО — УДАЛЯЕМ СТАРЫЕ
        if (createPromotion.photos() != null && !createPromotion.photos().isEmpty()) {
            promotion.getPhotos().clear();

            for (MultipartFile file : createPromotion.photos()) {
                if (!file.isEmpty()) {
                    String photoPath = processPhoto(file, uploadDir);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoPath);
                    photo.setPromotion(promotion);

                    promotion.getPhotos().add(photo);
                }
            }
        }
        promotionRepo.save(promotion);
    }

    @Override
    public void deletePromotion(Integer promotionId) {
        promotionRepo.deleteById(promotionId);
    }

    private GetPromotion toDtoPromotion(Promotion promotion) {
        return new GetPromotion(
                promotion.getName(),
                promotion.getDescription(),
                promotion.getPhotos()
                        .stream()
                        .map(ProductPhoto::getPhotoURL)
                        .toList(),
                promotion.getPercentageDiscounted(),
                promotion.isGlobal(),
                promotion.getCatalog().getId(),
                promotion.getProduct().getId(),
                promotion.getStartDate(),
                promotion.getEndDate()
        );
    }

    private String processPhoto(MultipartFile photo, Path uploadDir) {
        validateFileSize(photo, 10);
        String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        try {
            compressAndSaveImage(photo, filePath);
            return filePath.toString();
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

    private CreateCatalog toDtoCatalog(Catalog catalog) {
        return new CreateCatalog(
                catalog.getName()
        );
    }

    private GetProduct toDtoProduct(Product product) {
        return new GetProduct(
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                product.getPhotos()
                        .stream()
                        .map(ProductPhoto::getPhotoURL)
                        .toList(),
                product.getCatalog().getId(),
                product.getPaidStatus()
        );
    }
}
