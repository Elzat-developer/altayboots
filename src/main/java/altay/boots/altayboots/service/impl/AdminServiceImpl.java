package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.model.entity.*;
import altay.boots.altayboots.query.PromotionFirstImageProjection;
import altay.boots.altayboots.repository.*;
import altay.boots.altayboots.service.AdminService;
import altay.boots.altayboots.service.FileProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
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
        product.setName(createProduct.name());
        product.setDescription(createProduct.description());
        product.setText(createProduct.text());
        product.setPrice(createProduct.price());
        product.setOldPrice(createProduct.oldPrice());

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
                catalogId
        );
    }

    @Override
    public void editProduct(int product_id, EditProduct editProduct, List<MultipartFile> photos) {
        Product product = productRepo.findById(product_id);

        if (product == null) {
            throw new IllegalArgumentException("Продукт с ID " + product_id + " не найден.");
        }

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

        // 3. ПОДГОТОВКА ПАПКИ ЗАГРУЗКИ
        final String subDirectory = "products";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // --- 4. НОВАЯ ЛОГИКА ОБРАБОТКИ ФОТОГРАФИЙ (ЗАМЕНА И ПОРЯДОК) ---

        // 4.1. ЗАГРУЗКА НОВЫХ ФОТО И СОЗДАНИЕ КАРТЫ ЗАМЕН
        Map<String, ProductPhoto> newPhotosMap = new HashMap<>();
        // Key: Заглушка (NEW_FILE_X), Value: ProductPhoto

        if (photos != null && !photos.isEmpty()) {
            for (int i = 0; i < photos.size(); i++) {
                MultipartFile file = photos.get(i);
                if (!file.isEmpty()) {
                    String photoURL = fileProcessingService.processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
                    photo.setProduct(product);

                    // Создаем заглушку, соответствующую порядку загруженных файлов
                    String placeholder = "NEW_FILE_" + i;
                    newPhotosMap.put(placeholder, photo);
                }
            }
        }

        // 4.2. ОПРЕДЕЛЕНИЕ УДАЛЯЕМЫХ ФОТОГРАФИЙ
        List<ProductPhoto> finalPhotosList = getFinalOrderedPhotos(
                editProduct.finalPhotoOrder(),
                product.getPhotos(),
                newPhotosMap);

        // 4.5. ПЕРЕЗАПИСЬ КОЛЛЕКЦИИ (КЛЮЧЕВОЙ МОМЕНТ ДЛЯ @OrderColumn)
        // Hibernate использует новый список для обновления поля photo_order в БД.
        product.setPhotos(finalPhotosList);

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
            // --- КРИТИЧЕСКИЙ ШАГ: УДАЛЕНИЕ СТАРОГО ФАЙЛА
            String oldPhotoUrl = company.getPhotoURL();
            if (oldPhotoUrl != null) {
                fileProcessingService.deleteFileFromDisk(oldPhotoUrl);
            }

            String photoURL = fileProcessingService.processPhotoAndReturnURL(photo, uploadDir, subDirectory);
            company.setPhotoURL(photoURL);
            log.info("✅ Фото успешно сохранено: {}", photoURL);
        }

        company.setBase(companyDescription.base());
        company.setCity(companyDescription.city());

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
    public void editPromotion(int promotionId, EditPromotion editPromotion, List<MultipartFile> photos) {

        Promotion promotion = promotionRepo.findById(promotionId);

        if (promotion == null) {
            throw new IllegalArgumentException("Акция с ID " + promotionId + " не найдена.");
        }

        // 2.1. Обработка CatalogId
        // Обновляем, только если поле явно прислано (не null)
        if (editPromotion.catalogId() != null) {
            if (editPromotion.catalogId() <= 0) {
                // Если прислан 0 или отрицательное число, сбрасываем привязку
                promotion.setCatalog(null);
            } else {
                Catalog catalog = catalogRepo.findById(editPromotion.catalogId()).orElse(null);

                if (catalog == null) {
                    throw new IllegalArgumentException("Каталог с ID " + editPromotion.catalogId() + " не найден.");
                }
                promotion.setCatalog(catalog);
            }
        }

        // 2.2. Обработка ProductId
        // Обновляем, только если поле явно прислано (не null)
        if (editPromotion.productId() != null) {
            if (editPromotion.productId() <= 0) {
                // Если прислан 0 или отрицательное число, сбрасываем привязку
                promotion.setProduct(null);
            } else {
                // *** ИСПРАВЛЕНИЕ: Используем .orElse(null) для обработки Optional ***
                Product product = productRepo.findById(editPromotion.productId()).orElse(null);

                if (product == null) {
                    throw new IllegalArgumentException("Продукт с ID " + editPromotion.productId() + " не найден.");
                }
                promotion.setProduct(product);
            }
        }
        // Если editPromotion.productId() == null, старая привязка сохраняется.

        if (editPromotion.name() != null) promotion.setName(editPromotion.name());

        if (editPromotion.description() != null) promotion.setDescription(editPromotion.description());

        if (editPromotion.percentageDiscounted() != null) promotion.setPercentageDiscounted(editPromotion.percentageDiscounted());

        if (editPromotion.global() != null) promotion.setGlobal(editPromotion.global());

        if (editPromotion.startDate() != null) promotion.setStartDate(editPromotion.startDate());

        if (editPromotion.endDate() != null) promotion.setEndDate(editPromotion.endDate());


        // --- 4. ПОДГОТОВКА ПАПКИ ЗАГРУЗКИ ---
        final String subDirectory = "promotions";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // --- 5. ОБРАБОТКА ИЗМЕНЕНИЙ ФОТОГРАФИЙ (СЛОЖНАЯ ЛОГИКА ЗАМЕНЫ) ---

        // 5.1. ЗАГРУЗКА НОВЫХ ФОТОГРАФИЙ И СОЗДАНИЕ КАРТЫ ЗАМЕН
        Map<String, ProductPhoto> newPhotosMap = new HashMap<>();
        // Key: Заглушка (NEW_FILE_X), Value: Созданный объект ProductPhoto

        if (photos != null && !photos.isEmpty()) {
            for (int i = 0; i < photos.size(); i++) {
                MultipartFile file = photos.get(i);
                if (!file.isEmpty()) {
                    String photoURL = fileProcessingService.processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
                    photo.setPromotion(promotion);
                    // Важно: Пока не сохраняем, ID не будет, но объект готов

                    // Создаем заглушку, соответствующую порядку загруженных файлов
                    String placeholder = "NEW_FILE_" + i;
                    newPhotosMap.put(placeholder, photo);
                }
            }
        }

        // 5.2. АНАЛИЗ ПОРЯДКА И ОПРЕДЕЛЕНИЕ УДАЛЯЕМЫХ ФОТОГРАФИЙ
        List<ProductPhoto> finalPhotosList = getFinalOrderedPhotos(
                editPromotion.finalPhotoOrder(),
                promotion.getPhotos(),
                newPhotosMap);

        promotion.setPhotos(finalPhotosList);

        // --- 6. КОМПЛЕКСНАЯ ПОСТ-ВАЛИДАЦИЯ ---

        // Проверяем, что акция ПРИВЯЗАНА ХОТЯ БЫ К ОДНОМУ объекту после всех изменений.
        boolean isBoundToCatalog = promotion.getCatalog() != null;
        boolean isBoundToProduct = promotion.getProduct() != null;
        // Используем isGlobal(), если ваша сущность использует этот метод, иначе getGlobal()
        boolean isGlobal = promotion.isGlobal();

        if (!isBoundToCatalog && !isBoundToProduct && !isGlobal) {
            // Отменяем сохранение и сообщаем пользователю, что акция не имеет применения
            throw new IllegalArgumentException("Акция должна быть привязана хотя бы к одному объекту: Каталогу, Продукту или должна быть помечена как Глобальная.");
        }

        // 7. СОХРАНЕНИЕ АКЦИИ (со всеми изменениями)
        promotionRepo.save(promotion);
    }
    // ------------------- PRIVATE МЕТОД ОБРАБОТКИ ПОРЯДКА ФОТО --------------------
    private List<ProductPhoto> getFinalOrderedPhotos(
            List<String> desiredOrderList,
            List<ProductPhoto> currentPhotos,
            Map<String, ProductPhoto> newPhotosMap) {

        if (desiredOrderList == null) desiredOrderList = Collections.emptyList();

        // 1. ОПРЕДЕЛЕНИЕ ID, КОТОРЫЕ ДОЛЖНЫ ОСТАТЬСЯ
        Set<Integer> desiredExistingIds = desiredOrderList.stream()
                .filter(s -> !s.startsWith("NEW_FILE_"))
                .map(Integer::valueOf)
                .collect(Collectors.toSet());

        // 2. ОПРЕДЕЛЕНИЕ ID, КОТОРЫЕ НУЖНО УДАЛИТЬ
        Set<Integer> currentPhotoIds = currentPhotos.stream()
                .map(ProductPhoto::getId)
                .collect(Collectors.toSet());

        Set<Integer> idsToDelete = currentPhotoIds.stream()
                .filter(id -> !desiredExistingIds.contains(id))
                .collect(Collectors.toSet());

        // 3. ФИЗИЧЕСКОЕ УДАЛЕНИЕ СТАРЫХ ФОТО ИЗ КОЛЛЕКЦИИ
        if (!idsToDelete.isEmpty()) {
            currentPhotos.removeIf(photo -> {
                if (idsToDelete.contains(photo.getId())) {
                    fileProcessingService.deleteFileFromDisk(photo.getPhotoURL());
                    return true; // Удалить из коллекции
                }
                return false;
            });
        }

        // 4. ФОРМИРОВАНИЕ ФИНАЛЬНОГО, УПОРЯДОЧЕННОГО СПИСКА
        // Карта оставшихся старых объектов ProductPhoto
        Map<Integer, ProductPhoto> existingPhotosMap = currentPhotos.stream()
                .collect(Collectors.toMap(ProductPhoto::getId, Function.identity()));

        List<ProductPhoto> finalPhotosList = new ArrayList<>();

        // Проходим по желаемому порядку:
        for (String item : desiredOrderList) {
            if (item.startsWith("NEW_FILE_")) {
                // Вставляем новое фото вместо заглушки
                ProductPhoto newPhoto = newPhotosMap.get(item);
                if (newPhoto != null) {
                    finalPhotosList.add(newPhoto);
                }
            } else {
                // Вставляем старое, оставшееся фото
                Integer photoId = Integer.valueOf(item);
                ProductPhoto existingPhoto = existingPhotosMap.get(photoId);
                if (existingPhoto != null) {
                    finalPhotosList.add(existingPhoto);
                }
            }
        }
        return finalPhotosList;
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
                catalogId
        );
    }
}