package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.model.entity.*;
import altay.boots.altayboots.repository.*;
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
    private final OrderRepo orderRepo;

    // --- КОНСТАНТА ДЛЯ КОРНЕВОЙ ПАПКИ ЗАГРУЗКИ ---
    private static final String UPLOAD_ROOT_PATH = "C:/uploads";

    @Override
    public void createProduct(CreateProduct createProduct,List<MultipartFile> photos) {
        Product product = new Product();
        product.setName(createProduct.name());
        product.setDescription(createProduct.description());
        product.setText(createProduct.text());
        product.setPrice(createProduct.price());
        product.setOldPrice(createProduct.oldPrice());

        Catalog catalog = catalogRepo.findById(createProduct.catalog_id());
        product.setCatalog(catalog);

        productRepo.save(product);

        final String subDirectory = "products";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📷 СОХРАНЕНИЕ НЕСКОЛЬКИХ ФОТО
        if (photos != null) {
            for (MultipartFile file : photos) {

                if (!file.isEmpty()) {
                    // 🔥 ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ URL
                    String photoURL = processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
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
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                photoList,
                product.getCatalog().getId()
        );
    }


    @Override
    public void editProduct(int product_id, EditProduct editProduct,List<MultipartFile> photos) {
        Product product = productRepo.findById(product_id);

        if (editProduct.name() != null)
            product.setName(editProduct.name());

        if (editProduct.description() != null)
            product.setDescription(editProduct.description());

        if (editProduct.text() != null)
            product.setText(editProduct.text());

        if (editProduct.price() != null)
            product.setPrice(editProduct.price());

        if (editProduct.oldPrice() != null)
            product.setOldPrice(editProduct.oldPrice());

        final String subDirectory = "products";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📌 ЕСЛИ ПРИШЛИ НОВЫЕ ФОТО — УДАЛЯЕМ СТАРЫЕ
        if (photos != null && !photos.isEmpty()) {
            product.getPhotos().clear();

            for (MultipartFile file : photos) {
                if (!file.isEmpty()) {
                    // 🔥 ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ URL
                    String photoURL = processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
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
        catalog.setName(createCatalog.name());
        catalogRepo.save(catalog);
    }

    @Override
    public List<GetCatalog> getCatalogs() {
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
    public void createCompanyDescription(CreateCompanyDescription createCompanyDescription, MultipartFile photo) {
        Company company = new Company();
        company.setName(createCompanyDescription.name());
        company.setText(createCompanyDescription.text());

        final String subDirectory = "company";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📷 Фото
        if (photo != null && !photo.isEmpty()) {
            // 🔥 ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ URL
            String photoURL = processPhotoAndReturnURL(photo, uploadDir, subDirectory);
            company.setPhotoURL(photoURL);
            log.info("✅ Фото успешно сохранено: {}", photoURL);
        }

        company.setBase(createCompanyDescription.base());
        company.setCity(createCompanyDescription.city());
        companyRepo.save(company);
    }

    @Override
    public CompanyDescription getCompany() {
        Company company = companyRepo.findById(1);
        return new CompanyDescription(
                company.getId(),
                company.getName(),
                company.getText(),
                company.getPhotoURL(),
                company.getBase(),
                company.getCity()
        );
    }

    @Override
    public void editCompany(CreateCompanyDescription companyDescription,MultipartFile photo) {
        Company company = companyRepo.findById(1);

        company.setName(companyDescription.name());
        company.setText(companyDescription.text());


        final String subDirectory = "company";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            log.error("❌ Не удалось создать папку загрузки: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📷 Фото
        if (photo != null && !photo.isEmpty()) {
            // 🔥 ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ URL
            String photoURL = processPhotoAndReturnURL(photo, uploadDir, subDirectory);
            company.setPhotoURL(photoURL);
            log.info("✅ Фото успешно сохранено: {}", photoURL);
        }

        company.setBase(companyDescription.base());
        company.setCity(companyDescription.city());

        companyRepo.save(company);
    }

    @Override
    public void createPromotion(CreatePromotion createPromotion,List<MultipartFile> photos) {
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

        final String subDirectory = "promotions";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📷 СОХРАНЕНИЕ НЕСКОЛЬКИХ ФОТО
        if (photos != null) {
            for (MultipartFile file : photos) {

                if (!file.isEmpty()) {
                    // 🔥 ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ URL
                    String photoURL = processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
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
                promotion.getId(),
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
    public void editPromotion(int promotionId, EditPromotion editPromotion,List<MultipartFile> photos) {
        Promotion promotion = promotionRepo.findById(promotionId);

        if (editPromotion.name() != null)
            promotion.setName(editPromotion.name());

        if (editPromotion.description() != null)
            promotion.setDescription(editPromotion.description());

        if (editPromotion.percentageDiscounted() != null)
            promotion.setPercentageDiscounted(editPromotion.percentageDiscounted());

        if (editPromotion.global() != null)
            promotion.setGlobal(editPromotion.global());

        if (editPromotion.startDate() != null)
            promotion.setStartDate(editPromotion.startDate());

        if (editPromotion.endDate() != null)
            promotion.setEndDate(editPromotion.endDate());

        final String subDirectory = "promotions";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 📌 ЕСЛИ ПРИШЛИ НОВЫЕ ФОТО — УДАЛЯЕМ СТАРЫЕ
        if (photos != null && !photos.isEmpty()) {
            promotion.getPhotos().clear();

            for (MultipartFile file : photos) {
                if (!file.isEmpty()) {
                    // 🔥 ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ URL
                    String photoURL = processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
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

    // --- МЕТОДЫ ОБРАБОТКИ ФОТО ---

    /**
     * Сохраняет фото на диск и возвращает URL-путь для базы данных.
     * @param photo Файл, полученный из запроса
     * @param uploadDir Локальный путь для сохранения файла (C:/uploads/...)
     * @param subDirectory Имя подпапки (например, "products", "company")
     * @return Относительный URL-путь (например, "/uploads/products/xyz.jpg")
     */
    private String processPhotoAndReturnURL(MultipartFile photo, Path uploadDir, String subDirectory) {
        validateFileSize(photo, 10);
        String fileName = UUID.randomUUID() + "_" + photo.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        try {
            compressAndSaveImage(photo, filePath);

            // 🔥 ВОЗВРАЩАЕМ URL-ПУТЬ, КОТОРЫЙ БУДЕТ ИСПОЛЬЗОВАТЬ ФРОНТЕНД
            return "/uploads/" + subDirectory + "/" + fileName;
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

    // --- МЕТОДЫ Order / toDto / Catalog ---

    @Override
    public List<GetAdminOrderSimple> getOrders() {
        List<Order> orders = orderRepo.findAll();
        return orders.stream()
                .map(this::toDtoOrders)
                .toList();
    }

    @Override
    public GetAdminOrder getOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Пользователь
        User u = order.getUser();
        UserDto userDto = new UserDto(
                u.getName(),
                u.getSurName(),
                u.getLastName(),
                u.getPhone(),
                u.getRegion(),
                u.getCityOrDistrict(),
                u.getStreet(),
                u.getHouseOrApartment(),
                u.getIndexPost()
        );

        // Список товаров с количеством
        List<OrderItemFullDto> items = order.getItems()
                .stream()
                .map(oi -> {
                    Product p = oi.getProduct();

                    List<ProductPhotoDto> photoDtos = p.getPhotos()
                            .stream()
                            .map(photo -> new ProductPhotoDto(photo.getPhotoURL()))
                            .toList();

                    ProductDto productDto = new ProductDto(
                            p.getId(),
                            p.getName(),
                            p.getDescription(),
                            p.getText(),
                            p.getPrice(),
                            p.getOldPrice(),
                            p.getCatalog() != null ? p.getCatalog().getId() : null,
                            photoDtos
                    );
                    return new OrderItemFullDto(productDto, oi.getQuantity());
                })
                .toList();

        return new GetAdminOrder(
                order.getId(),
                order.getOrderStartDate(),
                order.getPaidStatus(),
                userDto,
                items
        );
    }

    @Override
    public void editOrder(Integer orderId,EditOrder editOrder) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (editOrder.paidStatus() != null) {
            order.setPaidStatus(editOrder.paidStatus());
        }
        orderRepo.save(order);
    }

    @Override
    public void deleteOrder(Integer orderId) {
        orderRepo.deleteById(orderId);
    }


    private GetAdminOrderSimple toDtoOrders(Order order) {
        return new GetAdminOrderSimple(
                order.getId(),
                order.getUser().getName(),
                order.getOrderStartDate(),
                order.getPaidStatus()
        );
    }


    private GetPromotion toDtoPromotion(Promotion promotion) {
        return new GetPromotion(
                promotion.getId(),
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

    private GetCatalog toDtoCatalog(Catalog catalog) {
        return new GetCatalog(
                catalog.getId(),
                catalog.getName()
        );
    }

    private GetProduct toDtoProduct(Product product) {
        return new GetProduct(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                product.getPhotos()
                        .stream()
                        .map(ProductPhoto::getPhotoURL)
                        .toList(),
                product.getCatalog().getId()
        );
    }
}