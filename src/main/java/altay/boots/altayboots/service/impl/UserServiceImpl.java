package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.admin.GetPhotoDto;
import altay.boots.altayboots.dto.status.PaidStatus;
import altay.boots.altayboots.dto.user.*;
import altay.boots.altayboots.model.entity.*;
import altay.boots.altayboots.repository.*;
import altay.boots.altayboots.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepo userRepo;
    private final ProductRepo productRepo;
    private final OrderRepo orderRepo;
    private final CartRepo cartRepo;
    private final CartItemRepository cartItemRepo;
    @Bean
    public UserDetailsService userDetailsService(){
        return userRepo::findByPhone;
    }

    @Override
    @Transactional
    public Integer createOrder(CreateOrder createOrder) {
        User user = getContextUser();

        // 1. Обновление профиля пользователя
        updateUserProfile(user, createOrder);

        // 2. Поиск корзины
        Cart cart = cartRepo.findByUser(user);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new RuntimeException("Корзина пуста. Нечего оформлять.");
        }

        // 3. Разделяем товары на "живые" и "удаленные" (из-за Soft Delete)
        List<CartItem> allItems = cart.getItems();
        List<CartItem> validItems = allItems.stream()
                .filter(i -> i.getProduct() != null)
                .toList();

        List<CartItem> deletedItems = allItems.stream()
                .filter(i -> i.getProduct() == null)
                .toList();

        // 4. Если в процессе оформления нашлись удаленные товары:
        if (!deletedItems.isEmpty()) {
            cartItemRepo.deleteAll(deletedItems); // Чистим корзину от "мусора"

            // Если ПОСЛЕ чистки живых товаров не осталось - выходим
            if (validItems.isEmpty()) {
                throw new RuntimeException("К сожалению, все товары в вашей корзине более недоступны.");
            }

            // Опционально: можно прервать процесс и попросить пользователя проверить корзину еще раз
            // throw new RuntimeException("Некоторые товары стали недоступны. Мы обновили вашу корзину, проверьте её ещё раз.");
        }

        // 5. Создаем заказ только из валидных товаров
        Order order = new Order();
        order.setOrderStartDate(LocalDateTime.now());
        order.setPaidStatus(PaidStatus.NOTPAY);
        order.setUser(user);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : validItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order);
            orderItems.add(orderItem);
        }

        order.setItems(orderItems);
        Order savedOrder = orderRepo.save(order);

        // 6. Полная очистка корзины после успешного заказа
        cartItemRepo.deleteAll(allItems);
        // cartRepo.delete(cart); // Обычно саму корзину (объект-контейнер) не удаляют, чтобы не плодить ID

        return savedOrder.getId();
    }

    // Вынес обновление профиля в отдельный метод для чистоты кода
    private void updateUserProfile(User user, CreateOrder dto) {
        if (dto.surName() != null) user.setSurName(dto.surName());
        if (dto.lastName() != null) user.setLastName(dto.lastName());
        if (dto.region() != null) user.setRegion(dto.region());
        if (dto.cityOrDistrict() != null) user.setCityOrDistrict(dto.cityOrDistrict());
        if (dto.street() != null) user.setStreet(dto.street());
        if (dto.houseOrApartment() != null) user.setHouseOrApartment(dto.houseOrApartment());
        if (dto.index() != null) user.setIndexPost(dto.index());
        // userRepo.save(user); // Если нет @Transactional, нужно сохранить явно
    }


    @Override
    @Transactional
    public List<GetOrder> getOrders() {
        User user = getContextUser();
        int userId = user.getId();
        List<Order> orders = orderRepo.findAllByUserId(userId);
        return orders.stream()
                .map(this::toDtoOrder)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DetailedOrderDTO getOrder(int orderId) {
        Order order = orderRepo.findByIdWithDetails(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));
        return toDetailedOrderDTO(order);
    }

    private DetailedOrderDTO toDetailedOrderDTO(Order order) {
        // Преобразование PaidStatus в строку
        String paidStatus = (order.getPaidStatus() != null) ? order.getPaidStatus().name() : null;

        return new DetailedOrderDTO(
                order.getId(),
                order.getName(),
                order.getOrderStartDate(),
                paidStatus,
                toDtoOrderUser(order.getUser()),
                order.getItems().stream().map(this::toDtoDetailedItem).toList()
        );
    }

    private OrderUserDTO toDtoOrderUser(User user) {
        return new OrderUserDTO(
                user.getId(),
                user.getName(),
                user.getSurName(),
                user.getLastName(),
                user.getRegion(),
                user.getCityOrDistrict(),
                user.getStreet(),
                user.getHouseOrApartment(),
                user.getIndexPost()
        );
    }
    private ProductPhotoDTO toDtoPhoto(ProductPhoto photo) {
        return new ProductPhotoDTO(photo.getPhotoURL());
    }

    // Преобразование Продукта
    private DetailedOrderProductDTO toDtoDetailedProduct(Product product) {
        // *** КРИТИЧЕСКОЕ ИСПРАВЛЕНИЕ: БЕЗОПАСНАЯ ОБРАБОТКА NULL ***
        if (product == null) {
            // Если продукт удален, возвращаем DTO с минимальной информацией/заглушкой
            return new DetailedOrderProductDTO(
                    null,
                    "[Продукт удален]", // Название-заглушка
                    "Данные об этом продукте больше не доступны.",
                    0,
                    null,
                    false,
                    null,
                    List.of() // Пустой список фото
            );
        }
        // Каталог должен быть загружен либо Eager, либо внутри транзакции
        String catalogName = (product.getCatalog() != null) ? product.getCatalog().getName() : null;

        return new DetailedOrderProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getOldPrice(),
                product.isActive(),
                catalogName,
                product.getPhotos().stream().map(this::toDtoPhoto).toList()
        );
    }

    // Преобразование Позиции Заказа (Item)
    private DetailedOrderItemDTO toDtoDetailedItem(OrderItem item) {
        return new DetailedOrderItemDTO(
                item.getId(),
                item.getQuantity(),
                toDtoDetailedProduct(item.getProduct())
        );
    }

    @Override
    public void deleteOrder(Integer orderId) {
        orderRepo.deleteById(orderId);
    }

    @Override
    @Transactional
    public void deleteProductFromOrder(int orderId, int productId) {
        Order order = orderRepo.findById(orderId);
        if (order == null) throw new RuntimeException("Order not found");

        // ищем OrderItem по товару
        OrderItem itemToRemove = order.getItems()
                .stream()
                .filter(i -> i.getProduct().getId() == productId)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Product not found in this order"));

        // удаляем OrderItem
        order.getItems().remove(itemToRemove);

        // благодаря orphanRemoval = true — удалится из таблицы order_items
        orderRepo.save(order);
    }

    @Override
    @Transactional
    public void addProductToCart(AddToCartDto addToCartDto) {
        User user = getContextUser();

        // Получаем корзину пользователя
        Cart cart = cartRepo.findByUser(user);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cartRepo.save(cart);
        }

        // Проверяем товар
        Product product = productRepo.findById(addToCartDto.productId());
        if (product == null) throw new RuntimeException("Product not found");
        if (addToCartDto.quantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        // 🟢 Находим товар в корзине, не грузя ВСЕ CartItem!
        CartItem existingItem = cartItemRepo.findByCartAndProduct(cart, product);

        if (existingItem != null) {
            // 🟢 Если товар уже есть — просто увеличиваем количество
            existingItem.setQuantity(existingItem.getQuantity() + addToCartDto.quantity());
            cartItemRepo.save(existingItem);
        } else {
            // 🟢 Иначе — создаём новую запись
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setQuantity(addToCartDto.quantity());
            cartItemRepo.save(newItem);
        }
    }

    @Override
    @Transactional
    public CartDto getCart() {
        User user = getContextUser();

        Cart cart = cartRepo.findByUser(user);
        if (cart == null) {
            return new CartDto(null, List.of(), 0);
        }

        List<CartItem> items = cartItemRepo.findByCartIdWithProducts(cart.getId());

        // 1. Формируем список DTO, игнорируя удаленные товары
        List<CartItemDto> dtos = items.stream()
                .filter(item -> item.getProduct() != null) // Игнорируем товары со статусом is_active=false
                .map(i -> new CartItemDto(
                        i.getId(),
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getProduct().getPrice()
                ))
                .toList();

        // 2. Считаем общую сумму только для существующих товаров
        int total = items.stream()
                .filter(item -> item.getProduct() != null)
                .mapToInt(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();

        // 3. (Опционально, но полезно) Авто-очистка
        // Если в корзине нашлись "удаленные" товары, их лучше удалить из БД,
        // чтобы они не висели мертвым грузом.
        List<CartItem> orphanItems = items.stream()
                .filter(i -> i.getProduct() == null)
                .toList();
        if (!orphanItems.isEmpty()) {
            cartItemRepo.deleteAll(orphanItems);
        }

        return new CartDto(cart.getId(), dtos, total);
    }

    private User getContextUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String name = auth.getName();
        User user = userRepo.findByPhone(name);
        if (user == null) throw new RuntimeException("User not found");
        return user;
    }

    @Override
    public void editCart(EditCartItemDto editCartItemDto) {
        CartItem item = cartItemRepo.findById(editCartItemDto.cartItemId())
                .orElseThrow(() -> new RuntimeException("Cart item not found"));

        if (editCartItemDto.quantity() <= 0) {
            cartItemRepo.delete(item);
            return;
        }
        item.setQuantity(editCartItemDto.quantity());
        cartItemRepo.save(item);
    }

    @Override
    public void deleteCartItem(Integer itemId) {
        cartItemRepo.deleteById(itemId);
    }

    private GetOrder toDtoOrder(Order order) {
        return new GetOrder(
                order.getId(),
                order.getOrderStartDate(),
                order.getPaidStatus(),
                order.getItems().stream()
                        .map(this::toDtoItem)
                        .toList()
        );
    }

    private OrderItemDto toDtoItem(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getQuantity(), // <--- Убедитесь, что это поле есть и оно используется
                toDtoItemProduct(orderItem.getProduct())
        );
    }

    private OrderItemProductDTO toDtoItemProduct(Product product) {
        // 1. Проверяем, существует ли сам продукт
        if (product == null) {
            // Возвращаем пустой DTO или DTO с пометкой "Товар удален"
            return new OrderItemProductDTO(
                    0, // или null для ID
                    "Товар более недоступен",
                    0,
                    Collections.emptyList(),
                    false,
                    "Каталог отсутствует"
            );
        }

        // 2. Если продукт есть, проверяем каталог внутри него
        String catalogName = (product.getCatalog() != null)
                ? product.getCatalog().getName()
                : "Без каталога";

        return new OrderItemProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getPhotos()
                        .stream()
                        .map(photo -> new GetPhotoDto(
                                photo.getId(),
                                photo.getPhotoURL()
                        ))
                        .toList(),
                product.isActive(),
                catalogName
        );
    }
}