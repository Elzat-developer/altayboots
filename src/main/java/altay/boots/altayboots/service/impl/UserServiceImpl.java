package altay.boots.altayboots.service.impl;

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
        // 1. Получаем текущего аутентифицированного пользователя
        User user = getContextUser();

        // 2. Обновление данных пользователя с проверкой на null (Ваш код)
        // Это гарантирует, что существующие данные не будут перезаписаны null
        if (createOrder.surName() != null) { user.setSurName(createOrder.surName()); }
        if (createOrder.lastName() != null) { user.setLastName(createOrder.lastName()); }
        if (createOrder.region() != null) { user.setRegion(createOrder.region()); }
        if (createOrder.cityOrDistrict() != null) { user.setCityOrDistrict(createOrder.cityOrDistrict()); }
        if (createOrder.street()!= null) { user.setStreet(createOrder.street()); }
        if (createOrder.houseOrApartment() != null) { user.setHouseOrApartment(createOrder.houseOrApartment()); }
        if (createOrder.index() != null) { user.setIndexPost(createOrder.index()); }
        // 2. Ищем корзину пользователя
        Cart cart = cartRepo.findByUser(user);
        if (cart == null || cart.getItems().isEmpty()) { // Проверяем, что корзина существует и не пуста
            throw new RuntimeException("Cannot create order: The cart is empty.");
        }

        // 3. Получаем все позиции из корзины
        // Лучше использовать cartItemRepo.findByCart(cart) или даже fetch-запрос,
        // чтобы сразу загрузить продукт.
        List<CartItem> cartItems = cart.getItems();

        // 4. Создаем новый заказ
        Order order = new Order();
        order.setOrderStartDate(LocalDateTime.now());
        order.setPaidStatus(PaidStatus.NOTPAY); // Или PaidStatus.PENDING, в зависимости от логики
        order.setUser(user);

        List<OrderItem> orderItems = new ArrayList<>();

        // 5. Перенос позиций из корзины в OrderItem
        for (CartItem cartItem : cartItems) {

            // Проверка наличия продукта
            Product product = cartItem.getProduct(); // Продукт уже должен быть загружен/связан

            if (product == null) {
                // Если продукт в корзине каким-то образом оказался NULL
                throw new RuntimeException("Product missing in cart item ID: " + cartItem.getId());
            }
            if (cartItem.getQuantity() <= 0) {
                continue; // Игнорируем или выбрасываем исключение
            }

            // Создаем OrderItem
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setOrder(order); // Связываем с новым заказом

            orderItems.add(orderItem);

            // ⚠️ ОЧЕНЬ ВАЖНЫЙ ШАГ: Удаляем позицию из корзины
            cartItemRepo.delete(cartItem);
        }

        // 6. Устанавливаем и сохраняем заказ
        order.setItems(orderItems);
        Order savedOrder = orderRepo.save(order);

        // 7. Очистка/удаление самой корзины (если нужно)
        cartRepo.delete(cart); // Если корзина удаляется после оформления заказа.

        return savedOrder.getId();
    }


    @Override
    @Transactional
    public List<GetOrder> getOrders(int userId) {
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
        // Каталог должен быть загружен либо Eager, либо внутри транзакции
        String catalogName = (product.getCatalog() != null) ? product.getCatalog().getName() : null;

        return new DetailedOrderProductDTO(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getOldPrice(),
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
    @Transactional(readOnly = true)
    public CartDto getCart() {
        User user = getContextUser();

        Cart cart = cartRepo.findByUser(user);
        if (cart == null) {
            return new CartDto(null, List.of(), 0);
        }

        List<CartItem> items = cartItemRepo.findByCartId(cart.getId());

        List<CartItemDto> dtos = items.stream()
                .map(i -> new CartItemDto(
                        i.getId(),
                        i.getProduct().getId(),
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getProduct().getPrice()
                ))
                .toList();

        int total = items.stream()
                .mapToInt(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();

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
        // Если Product.catalog является объектом Catalog, нужно получить его ID
        String catalogName = (product.getCatalog() != null)
                ? product.getCatalog().getName()
                : null;

        return new OrderItemProductDTO(
                product.getId(),
                product.getName(),
                product.getPrice(),
                catalogName
        );
    }
}