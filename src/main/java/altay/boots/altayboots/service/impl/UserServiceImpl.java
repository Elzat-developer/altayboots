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
    public List<GetProductUser> getProducts() {
        List<Product> products = productRepo.findAll();
        return products.stream()
                .map(this::toDtoProduct)
                .toList();
    }

    private GetProductUser toDtoProduct(Product product) {
        return new GetProductUser(
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

    @Override
    public GetProductUser getProduct(int productId) {
        Product product = productRepo.findById(productId);

        List<String> photoList = product.getPhotos()
                .stream()
                .map(ProductPhoto::getPhotoURL)
                .toList();

        return new GetProductUser(
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
    @Transactional
    public Integer createOrder(CreateOrder createOrder) {

        User user = getContextUser();

        Order order = new Order();
        order.setOrderStartDate(LocalDateTime.now());
        order.setPaidStatus(PaidStatus.NOTPAY);

        user.setSurName(createOrder.surName());
        user.setLastName(createOrder.lastName());
        user.setRegion(createOrder.region());
        user.setCityOrDistrict(createOrder.cityOrDistrict());
        user.setStreet(createOrder.street());
        user.setHouseOrApartment(createOrder.houseOrApartment());
        user.setIndexPost(createOrder.index());
        userRepo.save(user);

        order.setUser(user);

        List<OrderItem> items = new ArrayList<>();

        for (OrderItemDto itemDto : createOrder.items()) {

            Product product = productRepo.findById(itemDto.productId());
            if (product == null)
                throw new RuntimeException("Product not found: " + itemDto.productId());

            OrderItem item = new OrderItem();
            item.setProduct(product);
            item.setQuantity(itemDto.quantity());
            item.setOrder(order);

            items.add(item);
        }

        order.setItems(items);

        Order savedOrder = orderRepo.save(order);

        return savedOrder.getId(); // <-- ВОТ ЭТО ВАЖНО
    }


    @Override
    public List<GetOrder> getOrders(int userId) {
        List<Order> orders = orderRepo.findAllByUserId(userId);
        return orders.stream()
                .map(this::toDtoOrder)
                .toList();
    }

    @Override
    public GetOrder getOrder(int orderId) {
        Order order = orderRepo.findById(orderId);
        return new GetOrder(
                order.getName(),
                order.getOrderStartDate(),
                order.getPaidStatus(),
                order.getItems().stream()
                        .map(this::toDtoItem)
                        .toList()
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
        User user = userRepo.findByName(name);
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
                order.getName(),
                order.getOrderStartDate(),
                order.getPaidStatus(),
                order.getItems().stream()
                        .map(this::toDtoItem)
                        .toList()
        );
    }

    private OrderItemDto toDtoItem(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getId(),
                orderItem.getQuantity()
        );
    }
}