package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.model.entity.*;
import altay.boots.altayboots.query.PromotionFirstImageProjection;
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

    // --- КОНСТАНТА ДЛЯ КОРНЕВОЙ ПАПКИ ЗАГРУЗКИ ---
    private static final String UPLOAD_ROOT_PATH = "C:/uploads";

    @Override
    public void createProduct(CreateProduct createProduct, List<MultipartFile> photos) {
        // 1. ПРОВЕРКА КАТАЛОГА ⚠️
        // Предполагается, что catalogRepo.findById() возвращает Catalog или null.
        // Если используется Optional<Catalog>, код ниже будет немного отличаться.
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

        // Установка найденного каталога
        product.setCatalog(catalog);

        // Первое сохранение для получения ID продукта,
        // необходимого для привязки фотографий (если у вас нет каскадного сохранения)
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
                    String photoURL = processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
                    photo.setProduct(product);

                    // Добавление фото к коллекции продукта (если у вас установлены отношения)
                    product.getPhotos().add(photo);
                }
            }
        }

        // Второе сохранение для обновления информации о фотографиях (если у вас нет каскадного сохранения)
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

        // Добавьте проверку, если findById может вернуть null, чтобы избежать сбоя
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
        List<String> photoList = product.getPhotos() != null ?
                product.getPhotos()
                        .stream()
                        .map(ProductPhoto::getPhotoURL)
                        .toList() :
                Collections.emptyList(); // Используем Collections.emptyList() для безопасности

        // 4. Возврат DTO
        return new GetProduct(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                photoList,
                // Используем безопасно извлеченный ID каталога
                catalogId
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
        // 📌 ЛОГИКА ИЗБИРАТЕЛЬНОГО УДАЛЕНИЯ ФОТОГРАФИЙ
        if (editProduct.photosToDeleteIds() != null && !editProduct.photosToDeleteIds().isEmpty()) {

            // Получаем список ID, которые нужно удалить
            List<Integer> idsToDelete = editProduct.photosToDeleteIds();

            // Фильтруем коллекцию старых фото
            List<ProductPhoto> photosToKeep = new ArrayList<>();
            List<ProductPhoto> photosToRemove = new ArrayList<>();

            for (ProductPhoto photo : product.getPhotos()) {
                if (idsToDelete.contains(photo.getId())) {
                    photosToRemove.add(photo);
                } else {
                    photosToKeep.add(photo);
                }
            }

            // Обновляем коллекцию продукта, оставляя только те фото, которые нужно сохранить
            product.getPhotos().clear();
            product.getPhotos().addAll(photosToKeep);
        }

        // 4. ДОБАВЛЕНИЕ НОВЫХ ФОТО
        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile file : photos) {
                if (!file.isEmpty()) {
                    String photoURL = processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
                    photo.setProduct(product);

                    product.getPhotos().add(photo); // Добавятся в конец списка
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
    public void createPromotion(CreatePromotion createPromotion, List<MultipartFile> photos) {

        // --- 1. ИНИЦИАЛИЗАЦИЯ И УСТАНОВКА ОСНОВНЫХ ПОЛЕЙ АКЦИИ ---
        Promotion promotion = new Promotion();

        // Установка полей, пришедших из DTO
        promotion.setName(createPromotion.name());
        promotion.setDescription(createPromotion.description());

        // Используем Integer (соответствует int в сущности/DTO)
        promotion.setPercentageDiscounted(createPromotion.percentageDiscounted());

        // Инициализация привязок к сущностям как null (т.к. привязка будет позже)
        promotion.setCatalog(null);
        promotion.setProduct(null);

        // Если акция по умолчанию не глобальная, установите false
        // Если поле global отсутствует в DTO, но обязательно в сущности:
         promotion.setGlobal(false);

        promotion.setStartDate(createPromotion.startDate());
        promotion.setEndDate(createPromotion.endDate());

        // --- 2. ПЕРВОЕ СОХРАНЕНИЕ ---
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
                    String photoURL = processPhotoAndReturnURL(file, uploadDir, subDirectory);

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
    public void editPromotion(int promotionId, EditPromotion editPromotion, List<MultipartFile> photos) {
        // 1. ПОЛУЧЕНИЕ И ПРОВЕРКА АКЦИИ
        // Предполагается, что findById возвращает Promotion или null
        Promotion promotion = promotionRepo.findById(promotionId);

        if (promotion == null) {
            throw new IllegalArgumentException("Акция с ID " + promotionId + " не найдена.");
        }

        // --- 2. ОБНОВЛЕНИЕ СВЯЗАННЫХ СУЩНОСТЕЙ (КАТАЛОГ и ПРОДУКТ) ---

        // 2.1. Обработка CatalogId
        if (editPromotion.catalogId() != 0) {
            // Если прислан 0 или null, сбрасываем привязку
            if (editPromotion.catalogId() <= 0) {
                promotion.setCatalog(null);
            } else {
                Catalog catalog = catalogRepo.findById(editPromotion.catalogId());
                if (catalog == null) {
                    throw new IllegalArgumentException("Каталог с ID " + editPromotion.catalogId() + " не найден.");
                }
                promotion.setCatalog(catalog);
            }
        }
        // Если catalogId не прислан в DTO, старая привязка сохраняется.

        // 2.2. Обработка ProductId
        if (editPromotion.productId() != 0) {
            // Если прислан 0 или null, сбрасываем привязку
            if (editPromotion.productId() <= 0) {
                promotion.setProduct(null);
            } else {
                Product product = productRepo.findById(editPromotion.productId());
                if (product == null) {
                    throw new IllegalArgumentException("Продукт с ID " + editPromotion.productId() + " не найден.");
                }
                promotion.setProduct(product);
            }
        }
        // Если productId не прислан в DTO, старая привязка сохраняется.


        // --- 3. ОБНОВЛЕНИЕ ТЕКСТОВЫХ ПОЛЕЙ ---

        // Используйте проверку на != null, чтобы обновить поле, только если оно передано в DTO
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


        // 4. ПОДГОТОВКА ПАПКИ ЗАГРУЗКИ
        final String subDirectory = "promotions";
        Path uploadDir = Paths.get(UPLOAD_ROOT_PATH, subDirectory);
        try {
            Files.createDirectories(uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать папку загрузки", e);
        }

        // --- 5. ОБРАБОТКА ИЗМЕНЕНИЙ ФОТОГРАФИЙ ---

        // 5.1. ИЗБИРАТЕЛЬНОЕ УДАЛЕНИЕ СТАРЫХ ФОТОГРАФИЙ
        if (editPromotion.photosToDeleteIds() != null && !editPromotion.photosToDeleteIds().isEmpty()) {

            List<Integer> idsToDelete = editPromotion.photosToDeleteIds();

            // Используем removeIf для удаления элементов из коллекции и очистки файлов
            promotion.getPhotos().removeIf(photo -> {
                if (idsToDelete.contains(photo.getId())) {
                    // Здесь должна быть логика удаления файла с диска:
                    // deleteFileFromDisk(photo.getPhotoURL());
                    // Если у вас настроен orphanRemoval=true, JPA удалит объект ProductPhoto из БД.
                    return true;
                }
                return false;
            });
        }

        // 5.2. ДОБАВЛЕНИЕ НОВЫХ ФОТОГРАФИЙ
        // Новые фото добавляются к оставшимся старым фото в коллекции promotion.getPhotos()
        if (photos != null && !photos.isEmpty()) {
            for (MultipartFile file : photos) {
                if (!file.isEmpty()) {
                    String photoURL = processPhotoAndReturnURL(file, uploadDir, subDirectory);

                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(photoURL);
                    photo.setPromotion(promotion);

                    promotion.getPhotos().add(photo);
                }
            }
        }

        // 6. СОХРАНЕНИЕ АКЦИИ (со всеми изменениями)
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

    @Override
    public List<GetPromotionFirstImage> getPromotionFirstImage() {
        // 1. Получаем список интерфейсов проекции
        List<PromotionFirstImageProjection> projections = promotionRepo.findPromotionsWithFirstPhotoNative();

        // 2. Преобразуем его в целевой DTO
        return projections.stream()
                .map(p -> new GetPromotionFirstImage(
                        p.getPromotionId(),       // <- Используем геттер проекции
                        p.getPromotionImages()    // <- Используем геттер проекции
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
                // Примечание: Убедитесь, что promotion.getPhotos() не возвращает null,
                // иначе потребуется дополнительная проверка или инициализация пустой коллекцией.
                promotion.getPhotos()
                        .stream()
                        .map(ProductPhoto::getPhotoURL)
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
        // Безопасное получение ID каталога
        Integer catalogId = null;

        // ⚠️ ПРОВЕРКА НА NULL: Если product.getCatalog() не null,
        // мы берем его ID. Иначе, присваиваем null.
        if (product.getCatalog() != null) {
            catalogId = product.getCatalog().getId();
        }

        // В результате, если каталог отсутствует, в поле catalog_id
        // вашего DTO будет передано null, а не произойдет сбой.

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
                // Используем безопасно извлеченный ID
                catalogId
        );
    }
}