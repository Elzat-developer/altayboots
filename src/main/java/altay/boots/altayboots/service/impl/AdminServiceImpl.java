package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.model.entity.*;
import altay.boots.altayboots.query.PromotionFirstImageProjection;
import altay.boots.altayboots.repository.*;
import altay.boots.altayboots.service.AdminService;
import altay.boots.altayboots.service.FileProcessingService;
import altay.boots.altayboots.service.PhotosOwner;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final ProductRepo productRepo;
    private final CatalogRepo catalogRepo;
    private final CompanyRepo companyRepo;
    private final PromotionRepo promotionRepo;
    private final OrderRepo orderRepo;
    private final FileProcessingService fileProcessingService;

    // --- КОНСТАНТА ДЛЯ КОРНЕВОЙ ПАПКИ ЗАГРУЗКИ ---
    private static final String UPLOAD_ROOT_PATH = "C:/uploads";

    @Override
    public void createProduct(CreateProduct createProduct, List<MultipartFile> photos) {
        Catalog catalog = catalogRepo.findById(createProduct.catalog_id());

        if (catalog == null) {
            // 🔥 Если каталог не найден, выбрасываем исключение
            throw new IllegalArgumentException("Каталог с ID " + createProduct.catalog_id() + " не найден. Продукт не может быть добавлен.");
        }

        // --- ИНИЦИАЛИЗАЦИЯ ПРОДУКТА ---
        Product product = new Product();
        if (createProduct.name() != null) {
            product.setName(createProduct.name());
        }
        if (createProduct.description() != null) {
            product.setDescription(createProduct.description());
        }
        if (createProduct.text() != null) {
            product.setText(createProduct.text());
        }
        if (createProduct.price() != null) {
            product.setPrice(createProduct.price());
        }
        if (createProduct.oldPrice() != null) {
            product.setOldPrice(createProduct.oldPrice());
        }
        if (createProduct.sizes() != null) {
            product.setSizes(createProduct.sizes());
        }

        product.setCatalog(catalog);

        // Первое сохранение для получения ID продукта,
        productRepo.save(product);

        // --- ЛОГИКА СОХРАНЕНИЯ ФОТОГРАФИЙ ---
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
                    String photoURL = fileProcessingService.processPhotoAndReturnURL(
                            file, uploadDir, subDirectory);

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
        // 1. Поиск продукта (предполагаем, что findById возвращает Product или бросает исключение)
        Product product = productRepo.findById(productId);

        if (product == null) {
            throw new IllegalArgumentException("Продукт с ID " + productId + " не найден.");
        }

        // 2. Безопасное извлечение ID каталога
        Integer catalogId = null;

        // ⚠️ ПРОВЕРКА НА NULL: Если product.getCatalog() не null, мы берем его ID.
        if (product.getCatalog() != null) {
            catalogId = product.getCatalog().getId();
        }

        // 3. Извлечение списка фото (здесь также лучше убедиться, что getPhotos() не null)
        List<GetPhotoDto> photoList = product.getPhotos() != null ?
                product.getPhotos()
                        .stream()
                        .map(photo -> new GetPhotoDto(
                                photo.getId(),
                                photo.getPhotoURL()
                        ))
                        .toList() :
                Collections.emptyList(); // Используем Collections.emptyList() для безопасности

        return new GetProduct(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                photoList,
                product.getSizes(),
                catalogId
        );
    }

    @Override
    @Transactional
    public void editProduct(int product_id, EditProduct editProduct, List<MultipartFile> photos) {
        Product product = productRepo.findById(product_id);
        if (product == null) {
            throw new IllegalArgumentException("Продукт с ID " + product_id + " не найден.");
        }

        // 1. ОБНОВЛЕНИЕ ОСНОВНЫХ ПОЛЕЙ
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
        if (editProduct.sizes() != null)
            product.setSizes(editProduct.sizes());
        // 2. ПОДГОТОВКА ПАПКИ ЗАГРУЗКИ
        final String subDirectory = "products";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 3. ЗАГРУЗКА НОВЫХ ФОТО И СОЗДАНИЕ КАРТЫ ЗАМЕН
        Map<String, ProductPhoto> newPhotosMap = new HashMap<>();

        if (photos != null && !photos.isEmpty()) {
            for (int i = 0; i < photos.size(); i++) {
                MultipartFile file = photos.get(i);
                if (!file.isEmpty()) {
                    String photoURL = fileProcessingService.processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
                    photo.setProduct(product); // Установка обратной связи с Product

                    String placeholder = "NEW_FILE_" + i;
                    newPhotosMap.put(placeholder, photo);
                }
            }
        }

        // 4. ПРИМЕНЕНИЕ ЛОГИКИ ОБНОВЛЕНИЯ ФОТОГРАФИЙ (ПЕРЕИСПОЛЬЗОВАНИЕ)
        updatePhotos(product, "Product", editProduct.finalPhotoOrder(), newPhotosMap);

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
                company.getFreeEnd()
        );
    }

    @Override
    public void editCompany(CreateCompanyDescription companyDescription,MultipartFile photo) {
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
            // --- КРИТИЧЕСКИЙ ШАГ: УДАЛЕНИЕ СТАРОГО ФАЙЛА
            String oldPhotoUrl = company.getPhotoURL();
            if (oldPhotoUrl != null) {
                fileProcessingService.deleteFileFromDisk(oldPhotoUrl);
            }

            String photoURL = fileProcessingService.processPhotoAndReturnURL(photo, uploadDir, subDirectory);
            company.setPhotoURL(photoURL);
            log.info("✅ Фото успешно сохранено: {}", photoURL);
        }

        companyRepo.save(company);
    }

    @Override
    public void createPromotion(CreatePromotion createPromotion, List<MultipartFile> photos) {

        Promotion promotion = new Promotion();

        promotion.setName(createPromotion.name());
        promotion.setDescription(createPromotion.description());

        promotion.setPercentageDiscounted(createPromotion.percentageDiscounted());

        // Инициализация привязок к сущностям как null (т.к. привязка будет позже)
        promotion.setCatalog(null);
        promotion.setProduct(null);

        // Если акция по умолчанию не глобальная, установите false
         promotion.setGlobal(false);

        promotion.setStartDate(createPromotion.startDate());
        promotion.setEndDate(createPromotion.endDate());

        // Сохранение акции для получения ID, необходимого для привязки фото
        promotionRepo.save(promotion);

        // --- 3. ПОДГОТОВКА ПАПКИ И СОХРАНЕНИЕ ФОТОГРАФИЙ ---
        final String subDirectory = "promotions";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile file : photos) {
                if (!file.isEmpty()) {
                    // 🔥 ИСПОЛЬЗУЕМ НОВЫЙ МЕТОД ДЛЯ ПОЛУЧЕНИЯ URL
                    String photoURL = fileProcessingService.processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);

                    // ⚠️ КОРРЕКТНАЯ ПРИВЯЗКА: Привязываем фото к самой АКЦИИ
                    // (Предполагается, что у ProductPhoto есть поле setPromotion(Promotion))
                    photo.setPromotion(promotion);

                    promotion.getPhotos().add(photo);
                }
            }
        }

        // 4. ПОВТОРНОЕ СОХРАНЕНИЕ (для сохранения привязанных фотографий)
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
    public void editPromotion(int promotionId, EditPromotion editPromotion, List<MultipartFile> photos) {
        Promotion promotion = promotionRepo.findById(promotionId);
        if (promotion == null) {
            throw new IllegalArgumentException("Promotion с ID " + promotionId + " не найден.");
        }
        // --- 2. ОБНОВЛЕНИЕ ОСНОВНЫХ ПОЛЕЙ И ПРИВЯЗОК ---

        // 2.1. ОБНОВЛЕНИЕ ПРИВЯЗКИ К КАТАЛОГУ (CatalogId)
        if (editPromotion.catalogId() != null) {
            if (editPromotion.catalogId() <= 0) {
                promotion.setCatalog(null); // Сброс привязки
            } else {
                Catalog catalog = catalogRepo.findById(editPromotion.catalogId())
                        .orElseThrow(() -> new IllegalArgumentException("Каталог с ID " + editPromotion.catalogId() + " не найден."));
                promotion.setCatalog(catalog);
            }
        }

        // 2.2. ОБНОВЛЕНИЕ ПРИВЯЗКИ К ПРОДУКТУ (ProductId)
        if (editPromotion.productId() != null) {
            if (editPromotion.productId() <= 0) {
                promotion.setProduct(null); // Сброс привязки
            } else {
                Product product = productRepo.findById(editPromotion.productId())
                        .orElseThrow(() -> new IllegalArgumentException("Продукт с ID " + editPromotion.productId() + " не найден."));
                promotion.setProduct(product);
            }
        }

        // 2.3. ОБНОВЛЕНИЕ ПРОСТЫХ ПОЛЕЙ
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

        if (editPromotion.startDate() != null) {
            promotion.setStartDate(editPromotion.startDate());
        }
        if (editPromotion.endDate() != null) {
            promotion.setEndDate(editPromotion.endDate());
        }

        // 1. ПОДГОТОВКА ПАПКИ ЗАГРУЗКИ
        final String subDirectory = "promotions";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // 2. ЗАГРУЗКА НОВЫХ ФОТО И СОЗДАНИЕ КАРТЫ ЗАМЕН
        Map<String, ProductPhoto> newPhotosMap = new HashMap<>();

        if (photos != null && !photos.isEmpty()) {
            for (int i = 0; i < photos.size(); i++) {
                MultipartFile file = photos.get(i);
                if (!file.isEmpty()) {
                    // Используем ваш сервис для загрузки и получения URL
                    String photoURL = fileProcessingService.processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
                    photo.setPromotion(promotion);

                    String placeholder = "NEW_FILE_" + i;
                    newPhotosMap.put(placeholder, photo);
                }
            }
        }

        // 3. ПРИМЕНЕНИЕ ЛОГИКИ ОБНОВЛЕНИЯ ФОТОГРАФИЙ (ПЕРЕИСПОЛЬЗОВАНИЕ)
        updatePhotos(promotion, "Promotion", editPromotion.finalPhotoOrder(), newPhotosMap);

        // Проверяем, что акция ПРИВЯЗАНА ХОТЯ БЫ К ОДНОМУ объекту после всех изменений.
        boolean isBoundToCatalog = promotion.getCatalog() != null;
        boolean isBoundToProduct = promotion.getProduct() != null;
        boolean isGlobal = promotion.isGlobal();

        if (!isBoundToCatalog && !isBoundToProduct && !isGlobal) {
            // Отменяем сохранение и сообщаем пользователю, что акция не имеет применения
            throw new IllegalArgumentException("Акция должна быть привязана хотя бы к одному объекту: Каталогу, Продукту или должна быть помечена как Глобальная.");
        }

        promotionRepo.save(promotion);
    }

    // ------------------- PRIVATE МЕТОД ОБРАБОТКИ ПОРЯДКА ФОТО --------------------
    private void updatePhotos(PhotosOwner ownerEntity, String entityType,
                              List<String> finalPhotoOrder, Map<String, ProductPhoto> newPhotosMap) {

        // 1. Создаем Map из существующих фото (те, что уже в БД)
        Map<Integer, ProductPhoto> existingPhotosMap = ownerEntity.getPhotos().stream()
                // Используем .filter(p -> p.getId() > 0) для примитивного int ID
                .filter(p -> p.getId() > 0)
                .collect(Collectors.toMap(ProductPhoto::getId, p -> p, (p1, p2) -> p1));

        List<ProductPhoto> photosToKeep = new ArrayList<>();

        // 2. Проходим по желаемому порядку (finalPhotoOrder) и строим финальный список.
        if (finalPhotoOrder != null) {
            for (String item : finalPhotoOrder) { // 🔥 Итерация по String, как определено в сигнатуре

                if (item.startsWith("NEW_FILE_")) {
                    // Это новая заглушка
                    ProductPhoto newPhoto = newPhotosMap.get(item);
                    if (newPhoto != null) {
                        photosToKeep.add(newPhoto);
                    }
                } else {
                    // 🔥 Это ID существующего фото (в виде строки)
                    try {
                        Integer photoId = Integer.parseInt(item);
                        ProductPhoto existingPhoto = existingPhotosMap.get(photoId);

                        if (existingPhoto != null) {
                            photosToKeep.add(existingPhoto);
                            // Удаляем из Map, чтобы оставшиеся элементы были помечены как сироты
                            existingPhotosMap.remove(photoId);
                        }
                    } catch (NumberFormatException e) {
                        // Игнорируем или логируем некорректный ID в списке
                        log.warn("Некорректный ID фото в finalPhotoOrder: {}", item);
                    }
                }
            }
        }

        // 3. УДАЛЕНИЕ ФАЙЛОВ С ДИСКА (Используем ваш метод deleteFileFromDisk, который вы указали)
        for (ProductPhoto photoToRemove : existingPhotosMap.values()) {
            try {
                fileProcessingService.deleteFileFromDisk(photoToRemove.getPhotoURL());
            } catch (Exception e) {
                log.warn("Не удалось удалить файл с диска для сущности {} ID {}: {}",
                        entityType, ownerEntity.getId(), photoToRemove.getPhotoURL(), e);
            }
        }

        // 4. Удаляем "сироты" из СУЩЕСТВУЮЩЕЙ коллекции (JpaSystemException fix)
        ownerEntity.getPhotos().removeAll(existingPhotosMap.values());

        // 5. Очищаем и заменяем СОДЕРЖИМОЕ, чтобы установить финальный порядок.
        ownerEntity.getPhotos().clear();
        ownerEntity.getPhotos().addAll(photosToKeep);
    }

    @Override
    public void deletePromotion(Integer promotionId) {
        promotionRepo.deleteById(promotionId);
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

    @Override
    public List<GetPromotionFirstImage> getPromotionFirstImage() {
        // 1. Получаем список интерфейсов проекции
        List<PromotionFirstImageProjection> projections = promotionRepo.findPromotionsWithFirstPhotoNative();

        return projections.stream()
                .map(p -> new GetPromotionFirstImage(
                        p.getPromotionId(),
                        p.getPromotionImages()
                ))
                .toList();
    }

    @Override
    public GetProductPhotos getProductsPhotos(Integer productId) {
        Product product = productRepo.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product с ID " + productId + " не найден."));

        List<GetPhotoDto> photoDtoList = product.getPhotos()
                .stream()
                .map(productPhoto -> new GetPhotoDto(
                        productPhoto.getId(),
                        productPhoto.getPhotoURL()
                ))
                .toList();

        return new GetProductPhotos(
                product.getId(),
                photoDtoList
        );
    }

    @Override
    public GetProductPhotos getPromotionsPhotos(Integer promotionId) {
        Promotion promotion = promotionRepo.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion с ID " + promotionId + " не найден."));

        List<GetPhotoDto> photoDtoList = promotion.getPhotos()
                .stream()
                .map(productPhoto -> new GetPhotoDto(
                        productPhoto.getId(),
                        productPhoto.getPhotoURL()
                ))
                .toList();

        return new GetProductPhotos(
                promotion.getId(),
                photoDtoList
        );
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
                promotion.getPhotos()
                        .stream()
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
                product.getPhotos()
                        .stream()
                        .map(photo -> new GetPhotoDto(
                                photo.getId(),
                                photo.getPhotoURL()
                        ))
                        .toList(),
                product.getSizes(),
                catalogId
        );
    }
}