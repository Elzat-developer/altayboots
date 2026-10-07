package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.model.entity.*;
import altay.boots.altayboots.repository.*;
import altay.boots.altayboots.service.AdminService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final ProductRepo productRepo;
    private final CatalogRepo catalogRepo;
    private final CompanyRepo companyRepo;
    private final PromotionRepo promotionRepo;
    private final OrderRepo orderRepo;
    private final CartItemRepository cartItemRepository;
    private final ProductPhotoRepo productPhotoRepo;
    @Value("${storage.base-path}")
    private String basePath;
    @Value("${storage.dirs.product}")
    private String productDir;
    @Value("${storage.dirs.promotion}")
    private String promotionDir;
    @Override
    @Transactional
    public void createProduct(CreateProduct createProduct,List<MultipartFile> photos) {
        Catalog catalog = catalogRepo.findById(createProduct.catalog_id());

        if (catalog == null) {
            throw new IllegalArgumentException("Каталог с ID " + createProduct.catalog_id() + " не найден. Продукт не может быть добавлен.");
        }

        Product product = new Product();
        updateProductFields(product, createProduct);
        product.setCatalog(catalog);

        // --- ЛОГИКА СОХРАНЕНИЯ ФОТОГРАФИЙ ---
        if (photos != null && !photos.isEmpty()) {
            setPhotosProduct(photos, product);
        }
        productRepo.save(product);
    }

    private void setPhotosProduct(List<MultipartFile> photos, Product product) {
        // 1. Базовая защита от пустых списков
        if (photos == null || photos.isEmpty()) {
            return;
        }

        // 2. Защита от NullPointerException, если Hibernate подсунул null вместо пустой коллекции
        if (product.getPhotos() == null) {
            product.setPhotos(new ArrayList<>());
        }

        // 3. Перебираем файлы и сохраняем каждый
        for (MultipartFile file : photos) {
            if (file != null && !file.isEmpty()) {
                // Передаем файл и название директории (productDir) в наш безопасный единый метод
                String relativePath = processMultipartFile(file, productDir);

                ProductPhoto productImage = new ProductPhoto();
                productImage.setPhotoURL(relativePath);
                productImage.setProduct(product);

                product.getPhotos().add(productImage);
                log.info("✅ Добавлено фото к продукту: {}", relativePath);
            }
        }
    }
    private void setPhotosPromotion(List<MultipartFile> photos, Promotion promotion) {
        // 1. Базовая защита от пустых списков
        if (photos == null || photos.isEmpty()) {
            return;
        }

        // 2. Защита от NullPointerException, если Hibernate подсунул null вместо пустой коллекции
        if (promotion.getPhotos() == null) {
            promotion.setPhotos(new ArrayList<>());
        }

        // 3. Перебираем файлы и сохраняем каждый
        for (MultipartFile file : photos) {
            if (file != null && !file.isEmpty()) {
                // Передаем файл и название директории (productDir) в наш безопасный единый метод
                String relativePath = processMultipartFile(file, promotionDir);

                ProductPhoto productImage = new ProductPhoto();
                productImage.setPhotoURL(relativePath);
                productImage.setPromotion(promotion);

                promotion.getPhotos().add(productImage);
                log.info("✅ Добавлено фото к продукту: {}", relativePath);
            }
        }
    }
    private String processMultipartFile(MultipartFile multipartFile, String targetDirName) {
        // 1. Защита от null
        String originalFilename = Optional.of(multipartFile.getOriginalFilename())
                .orElse("unknown_file");

        // 2. Защита от Path Traversal: берем только конечное имя файла
        String safeOriginalName = Paths.get(originalFilename).getFileName().toString();

        String baseName = UUID.randomUUID().toString();
        String contentType = Optional.of(multipartFile.getContentType()).orElse("");

        String extension = "";
        if (safeOriginalName.contains(".")) {
            extension = safeOriginalName.substring(safeOriginalName.lastIndexOf(".") + 1).toLowerCase();
        }

        Path uploadDir = Paths.get(basePath, targetDirName).toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadDir);

            if (contentType.startsWith("image/")) {
                String fileName = extension.isEmpty() ? baseName : baseName + "." + extension;
                Path filePath = uploadDir.resolve(fileName);

                if (extension.equals("webp") || extension.equals("svg")) {
                    multipartFile.transferTo(filePath);
                } else {
                    net.coobird.thumbnailator.Thumbnails.of(multipartFile.getInputStream())
                            .size(1600, 1600)
                            .outputQuality(0.8)
                            .outputFormat(extension.isEmpty() ? "jpg" : extension)
                            .toFile(filePath.toFile());
                }
                return Paths.get(targetDirName, fileName).toString(); // Сохраняем в БД относительный путь!
            } else {
                // Для не-картинок используем очищенное имя
                String fileName = baseName + "_" + safeOriginalName;
                Path filePath = uploadDir.resolve(fileName);
                multipartFile.transferTo(filePath);
                return Paths.get(targetDirName, fileName).toString();
            }
        } catch (IOException e) {
            log.error("❌ Ошибка при сохранении '{}': {}", safeOriginalName, e.getMessage());
            throw new RuntimeException("Ошибка при обработке файла", e);
        }
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
        // 1. Поиск продукта (предполагаем, что findById возвращает Product или бросает исключение)
        Product product = productRepo.findById(productId);
        if (product == null) {
            throw new IllegalArgumentException("Продукт с ID " + productId + " не найден.");
        }
        return toDtoProduct(product);
    }

    @Override
    @Transactional
    public void editProduct(int product_id, EditProduct editProduct,List<MultipartFile> photos) {
        Product product = productRepo.findById(product_id);
        if (product == null) {
            throw new IllegalArgumentException("Продукт с ID " + product_id + " не найден.");
        }

        // 1. ОБНОВЛЕНИЕ ОСНОВНЫХ ПОЛЕЙ
        if (editProduct.name() != null) product.setName(editProduct.name());
        if (editProduct.description() != null) product.setDescription(editProduct.description());
        if (editProduct.text() != null) product.setText(editProduct.text());
        if (editProduct.price() != null) product.setPrice(editProduct.price());
        if (editProduct.oldPrice() != null) product.setOldPrice(editProduct.oldPrice());
        if (editProduct.sizes() != null) product.setSizes(editProduct.sizes());
        // 2. ОБНОВЛЕНИЕ ФОТО С СОХРАНЕНИЕМ ПОРЯДКА
        if (photos != null && !photos.isEmpty()){
            // 1. Сначала удаляем старые файлы с диска
            product.getPhotos().forEach(photo -> deleteFileFromDisk(photo.getPhotoURL()));

            // 2. Очищаем список (orphanRemoval = true в сущности удалит их из БД)
            product.getPhotos().clear();

            // 3. Сохраняем новые сжатые фото
            setPhotosProduct(photos,product);
        }
        if (editProduct.youtubeUrl() != null) product.setYoutubeUrl(editProduct.youtubeUrl());

        productRepo.save(product);
    }



    @Override
    @Transactional
    public void deleteProduct(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));
        cartItemRepository.deleteByProductId(productId);
        product.setActive(false);
        productRepo.save(product);
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
    public CompanyDescription getCompany() {
        Company company = companyRepo.findById(1);
        return new CompanyDescription(
                company.getId(),
                company.getName(),
                company.getText(),
                company.getPhotoURL(),
                company.getBase(),
                company.getCity(),
                company.getStreet(),
                company.getEmail(),
                company.getPhone(),
                company.getJobStart(),
                company.getJobEnd(),
                company.getFreeStart(),
                company.getFreeEnd(),
                company.getUserMainUrl()
        );
    }

    @Override
    public void editCompany(CreateCompanyDescription companyDescription) {
        Company company = companyRepo.findById(1);
        if (companyDescription.name() != null) {
            company.setName(companyDescription.name());
        }
        if (companyDescription.text() != null) {
            company.setText(companyDescription.text());
        }
        if (companyDescription.base() != null){
            company.setBase(companyDescription.base());
        }
        if (companyDescription.city() != null){
            company.setCity(companyDescription.city());
        }
        if (companyDescription.street() != null){
            company.setStreet(companyDescription.street());
        }
        if (companyDescription.email() != null){
            company.setEmail(companyDescription.email());
        }
        if (companyDescription.phone() != null){
            company.setPhone(companyDescription.phone());
        }
        if (companyDescription.jobStart() != null){
            company.setJobStart(companyDescription.jobStart());
        }
        if (companyDescription.jobEnd() != null){
            company.setJobEnd(companyDescription.jobEnd());
        }
        if (companyDescription.freeStart() != null){
            company.setFreeStart(companyDescription.freeStart());
        }
        if (companyDescription.freeEnd() != null){
            company.setFreeEnd(companyDescription.freeEnd());
        }

        // Обновляем фото через ID
        if (companyDescription.photoId() != null) {
            ProductPhoto photo = productPhotoRepo.findById(companyDescription.photoId())
                    .orElseThrow(() -> new EntityNotFoundException("Photo ID not found"));
            company.setPhotoURL(photo.getPhotoURL());
        }
        if (companyDescription.userMainPhotoId() != null){
            ProductPhoto photo = productPhotoRepo.findById(companyDescription.userMainPhotoId())
                    .orElseThrow(() -> new EntityNotFoundException("Photo ID not found"));
            company.setUserMainUrl(photo.getPhotoURL());
        }

        companyRepo.save(company);
    }

    @Override
    @Transactional
    public void createPromotion(CreatePromotion createPromotion,List<MultipartFile> urlPhoto) {
        Promotion promotion = new Promotion();

        if (createPromotion.name() != null) promotion.setName(createPromotion.name());
        if (createPromotion.description() != null) promotion.setDescription(createPromotion.description());
        if (createPromotion.percentageDiscounted() != null) promotion.setPercentageDiscounted(createPromotion.percentageDiscounted());
        if (createPromotion.startDate() != null) promotion.setStartDate(createPromotion.startDate());
        if (createPromotion.endDate() != null) promotion.setEndDate(createPromotion.endDate());

        // Инициализация привязок к сущностям как null (т.к. привязка будет позже)
        promotion.setCatalog(null);
        promotion.setProduct(null);

        // Если акция по умолчанию не глобальная, установите false
         promotion.setGlobal(false);
        // Привязываем фото
        if (urlPhoto != null && !urlPhoto.isEmpty()) {
            setPhotosPromotion(urlPhoto,promotion);
        }
        // Сохранение акции для получения ID, необходимого для привязки фото
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
        return toDtoPromotion(promotion);
    }

    @Override
    @Transactional
    public void editPromotion(int promotionId, EditPromotion editPromotion,List<MultipartFile> photos) {
        Promotion promotion = promotionRepo.findById(promotionId);
        if (promotion == null) {throw new IllegalArgumentException("Promotion с ID " + promotionId + " не найден.");}
        // --- 2. ОБНОВЛЕНИЕ ОСНОВНЫХ ПОЛЕЙ И ПРИВЯЗОК ---
        if (editPromotion.name() != null) promotion.setName(editPromotion.name());
        if (editPromotion.description() != null) promotion.setDescription(editPromotion.description());
        if (editPromotion.percentageDiscounted() != null) {
            int discount = editPromotion.percentageDiscounted();
            if (discount < 1 || discount > 100) {
                throw new IllegalArgumentException("Скидка должна быть в диапазоне от 1 до 100 процентов.");
            }
            promotion.setPercentageDiscounted(discount);
        }
        if (editPromotion.global() != null) promotion.setGlobal(editPromotion.global());
        if (editPromotion.startDate() != null) promotion.setStartDate(editPromotion.startDate());
        if (editPromotion.endDate() != null) promotion.setEndDate(editPromotion.endDate());
        // Обновление связей с Catalog/Product
        updatePromotionLinks(promotion, editPromotion.catalogId(), editPromotion.productId());

        // Обновление фотографий
        if (photos != null && !photos.isEmpty()){
            // 1. Сначала удаляем старые файлы с диска
            promotion.getPhotos().forEach(photo -> deleteFileFromDisk(photo.getPhotoURL()));

            // 2. Очищаем список (orphanRemoval = true в сущности удалит их из БД)
            promotion.getPhotos().clear();

            // 3. Сохраняем новые сжатые фото
            setPhotosPromotion(photos,promotion);
        }

        promotionRepo.save(promotion);
    }
    /**
     * Метод для массового удаления файлов (принимает List, Set и любую другую Collection)
     */
    private void deleteFilesFromDisk(Collection<String> relativePaths) {
        if (relativePaths == null || relativePaths.isEmpty()) {
            return;
        }

        // Проходимся по списку и вызываем метод удаления для каждого пути
        relativePaths.forEach(this::deleteFileFromDisk);
    }
    private void deleteFileFromDisk(String relativePath) {
        if (relativePath == null || relativePath.isBlank()) {
            return;
        }

        try {
            // Строим полный путь и нормализуем его
            Path fullPath = Paths.get(basePath, relativePath).toAbsolutePath().normalize();
            Path baseDirPath = Paths.get(basePath).toAbsolutePath().normalize();

            // Security Check: проверяем, что путь действительно внутри basePath
            if (!fullPath.startsWith(baseDirPath)) {
                log.warn("⚠️ Попытка Path Traversal при удалении! Путь: {}", relativePath);
                return;
            }

            if (Files.deleteIfExists(fullPath)) {
                log.info("🗑️ Файл успешно удален: {}", fullPath);
            }
        } catch (IOException e) {
            log.error("❌ Ошибка при удалении файла {}: {}", relativePath, e.getMessage());
        }
    }

    private void updateProductFields(Product product, CreateProduct dto) {
        if (dto.name() != null) product.setName(dto.name());
        if (dto.description() != null) product.setDescription(dto.description());
        if (dto.text() != null) product.setText(dto.text());
        if (dto.price() != null) product.setPrice(dto.price());
        if (dto.oldPrice() != null) product.setOldPrice(dto.oldPrice());
        if (dto.sizes() != null) product.setSizes(dto.sizes());
        if (dto.youtubeUrl() != null) product.setYoutubeUrl(dto.youtubeUrl());
        product.setActive(true);
    }
    private void updatePromotionLinks(Promotion promotion, Integer catalogId, Integer productId) {
        if (catalogId != null) {
            promotion.setCatalog(catalogRepo.findById(catalogId).orElse(null));
        }
        if (productId != null) {
            promotion.setProduct(productRepo.findById(productId).orElse(null));
        }
    }
    @Override
    @Transactional
    public void deletePromotion(Integer promotionId) {
        Promotion promotion = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion Not Found with id: " + promotionId));

        // 1. До удаления из БД собираем список относительных путей к файлам
        List<String> filePathsToDelete = null;
        if (promotion.getPhotos() != null && !promotion.getPhotos().isEmpty()) {
            filePathsToDelete = promotion.getPhotos().stream()
                    .map(ProductPhoto::getPhotoURL)
                    .filter(Objects::nonNull)
                    .filter(url -> !url.isBlank())
                    .toList();
        }

        // 2. Удаляем акцию из базы данных (каскадно удалятся и записи ProductPhoto)
        promotionRepo.delete(promotion);

        // 3. Если удаление из базы прошло успешно — удаляем файлы с диска
        if (filePathsToDelete != null && !filePathsToDelete.isEmpty()) {
            deleteFilesFromDisk(filePathsToDelete);
        }
    }

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
                u.getId(),
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

                    List<ProductPhotoDto> photoDto = p.getPhotos()
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
                            photoDto
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
        // ⚠️ БЕЗОПАСНОЕ ИЗВЛЕЧЕНИЕ ID КАТАЛОГА
        // Если getCatalog() вернет null, мы вернем null для catalogId, избегая NullPointerException.
        Integer catalogId = null;
        if (promotion.getCatalog() != null) {
            catalogId = promotion.getCatalog().getId();
        }

        // ⚠️ БЕЗОПАСНОЕ ИЗВЛЕЧЕНИЕ ID ПРОДУКТА
        // Если getProduct() вернет null, мы вернем null для productId.
        Integer productId = null;
        if (promotion.getProduct() != null) {
            productId = promotion.getProduct().getId();
        }
        return new GetPromotion(
                promotion.getId(),
                promotion.getName(),
                promotion.getDescription(),
                promotion.getPhotos() == null ? List.of() : promotion.getPhotos()
                        .stream()
                        .filter(Objects::nonNull) // 👈 ФИЛЬТР: пропускаем null элементы в коллекции
                        .map(photo -> new GetPhotoDto(
                                photo.getId(),
                                photo.getPhotoURL()
                        ))
                        .toList(),
                promotion.getPercentageDiscounted(),
                promotion.isGlobal(),

                // Используем безопасно извлеченные ID
                catalogId,
                productId,

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
        Integer catalogId = null;

        // ⚠️ ПРОВЕРКА НА NULL: Если product.getCatalog() не null,
        // мы берем его ID. Иначе, присваиваем null.
        if (product.getCatalog() != null) {
            catalogId = product.getCatalog().getId();
        }

        return new GetProduct(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                product.getPhotos() == null ? List.of() : product.getPhotos()
                        .stream()
                        .filter(Objects::nonNull) // 👈 ФИЛЬТР: пропускаем null элементы в коллекции
                        .map(photo -> new GetPhotoDto(
                                photo.getId(),
                                photo.getPhotoURL()
                        ))
                        .toList(),
                product.getSizes(),
                catalogId,
                product.getYoutubeUrl()
        );
    }
}