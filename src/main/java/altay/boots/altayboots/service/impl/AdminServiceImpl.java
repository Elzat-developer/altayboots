package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.model.entity.*;
import altay.boots.altayboots.query.PromotionFirstImageProjection;
import altay.boots.altayboots.repository.*;
import altay.boots.altayboots.service.AdminService;
import altay.boots.altayboots.service.FileProcessingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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
    private final CartItemRepository cartItemRepository;
    private final ProductPhotoRepo productPhotoRepo;
    private static final String subDirectory = "all_media";

    @Override
    @Transactional
    public void createProduct(CreateProduct createProduct) {
        Catalog catalog = catalogRepo.findById(createProduct.catalog_id());

        if (catalog == null) {
            throw new IllegalArgumentException("Каталог с ID " + createProduct.catalog_id() + " не найден. Продукт не может быть добавлен.");
        }

        Product product = new Product();
        updateProductFields(product, createProduct);
        product.setCatalog(catalog);

        // Инициализируем пустой список в продукте, если он еще не инициализирован
        if (product.getPhotos() == null) {
            product.setPhotos(new ArrayList<>());
        }

        productRepo.save(product);

        // --- ЛОГИКА СОХРАНЕНИЯ ФОТОГРАФИЙ ---
        savePhotosProduct(createProduct.photoIds(), product);
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
    public void editProduct(int product_id, EditProduct editProduct) {
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
        if (editProduct.photoIds() != null) {
            updateProductPhotos(product, editProduct.photoIds());
        }

        productRepo.save(product);
    }

    private void updateProductPhotos(Product product, List<Integer> photoIds) {
        // ШАГ 1: Полностью отвязываем старые фото (чтобы индексы в БД обнулились)
        List<ProductPhoto> currentPhotos = productPhotoRepo.findAllByProduct(product);
        for (ProductPhoto p : currentPhotos) {
            p.setProduct(null);
        }
        productPhotoRepo.saveAll(currentPhotos);

        // ШАГ 2: Очищаем список внутри Java-объекта
        if (product.getPhotos() == null) {
            product.setPhotos(new ArrayList<>());
        } else {
            product.getPhotos().clear();
        }

        // ШАГ 3: Привязываем новые фото строго по списку photoIds
        for (Integer photoId : photoIds) {
            productPhotoRepo.findById(photoId).ifPresent(photo -> {
                photo.setProduct(product); // Устанавливаем связь
                product.getPhotos().add(photo); // Добавляем в список (Hibernate сам проставит индекс в @OrderColumn)
            });
        }
        // Сохранять photoRepo.saveAll(photos) не обязательно, так как стоит CascadeType.ALL
    }
    private void savePhotosProduct(List<Integer> photoIds, Product product) {
        if (photoIds != null && !photoIds.isEmpty()) {
            List<ProductPhoto> photos = productPhotoRepo.findAllById(photoIds);

            for (ProductPhoto photo : photos) {
                photo.setProduct(product); // Устанавливаем владельца (сторона Photo)
                product.getPhotos().add(photo); // Добавляем в коллекцию (сторона Product)
            }

            productPhotoRepo.saveAll(photos);
        }
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
                company.getFreeEnd()
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
    public void createPromotion(CreatePromotion createPromotion) {
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



        // Сохранение акции для получения ID, необходимого для привязки фото
        promotionRepo.save(promotion);

        // Привязываем фото
        savePhotosPromotion(createPromotion.photoIds(), promotion);
    }

    private void savePhotosPromotion(List<Integer> photoIds, Promotion promotion) {
        if (photoIds != null && !photoIds.isEmpty()) {
            List<ProductPhoto> photos = productPhotoRepo.findAllById(photoIds);

            for (ProductPhoto photo : photos) {
                photo.setPromotion(promotion); // Устанавливаем владельца (сторона Photo)
                promotion.getPhotos().add(photo); // Добавляем в коллекцию (сторона Product)
            }

            productPhotoRepo.saveAll(photos);
        }
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
    public void editPromotion(int promotionId, EditPromotion editPromotion) {
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
        savePhotosPromotion(editPromotion.photoIds(), promotion);

        promotionRepo.save(promotion);
    }
    private void updateProductFields(Product product, CreateProduct dto) {
        if (dto.name() != null) product.setName(dto.name());
        if (dto.description() != null) product.setDescription(dto.description());
        if (dto.text() != null) product.setText(dto.text());
        if (dto.price() != null) product.setPrice(dto.price());
        if (dto.oldPrice() != null) product.setOldPrice(dto.oldPrice());
        if (dto.sizes() != null) product.setSizes(dto.sizes());
        product.setActive(true);
    }
    private void updatePromotionLinks(Promotion promotion, Integer catalogId, Integer productId) {
        if (catalogId != null) {
            if (catalogId <= 0) promotion.setCatalog(null);
            else promotion.setCatalog(catalogRepo.findById(catalogId).orElse(null));
        }
        if (productId != null) {
            if (productId <= 0) promotion.setProduct(null);
            else promotion.setProduct(productRepo.findById(productId).orElse(null));
        }
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
    @Transactional
    public void createPhotos(List<MultipartFile> photos) {
        if (photos == null || photos.isEmpty()) return;

        List<ProductPhoto> photoEntities = photos.stream()
                .filter(file -> !file.isEmpty())
                .map(file -> {
                    String url = fileProcessingService.processPhotoAndReturnURL(file, subDirectory);
                    ProductPhoto photo = new ProductPhoto();
                    photo.setPhotoURL(url);
                    return photo;
                })
                .toList();

        productPhotoRepo.saveAll(photoEntities);
    }

    @Override
    public List<GetPhotoDto> getAllPhotos() {
        return productPhotoRepo.findAll().stream()
                .map(p -> new GetPhotoDto(p.getId(), p.getPhotoURL()))
                .toList();
    }

    @Override
    @Transactional
    public void editPhoto(Integer photoId, MultipartFile file) {
        ProductPhoto photo = productPhotoRepo.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found"));

        // Удаляем старый файл
        fileProcessingService.deleteFileFromDisk(photo.getPhotoURL());

        // Загружаем новый
        // Загружаем новый и обновляем URL
        String newUrl = fileProcessingService.processPhotoAndReturnURL(file, subDirectory);
        photo.setPhotoURL(newUrl);

        productPhotoRepo.save(photo);
    }

    @Override
    @Transactional
    public void deletePhoto(Integer photoId) {
        ProductPhoto photo = productPhotoRepo.findById(photoId)
                .orElseThrow(() -> new EntityNotFoundException("Photo not found"));
        fileProcessingService.deleteFileFromDisk(photo.getPhotoURL());
        productPhotoRepo.delete(photo);
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
                catalogId
        );
    }
}