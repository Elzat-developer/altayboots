package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.user.*;
import altay.boots.altayboots.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
@Tag(name = "Пользовательская панель", description = "Эндпоинты для работы с товарами, корзиной и заказами")
public class UserPanelController {

    private final UserService userService;

    @GetMapping("/orders/{user_id}")
    @Operation(
            summary = "Получить заказы пользователя",
            description = "Возвращает список заказов по user_id"
    )
    public ResponseEntity<List<GetOrder>> getOrders(@PathVariable Integer user_id) {
        return ResponseEntity.ok(userService.getOrders(user_id));
    }

    @GetMapping("/order/{order_id}")
    @Operation(
            summary = "Получить один заказ",
            description = "Возвращает детали заказа по ID"
    )
    public ResponseEntity<GetOrder> getOrder(@PathVariable Integer order_id) {
        return ResponseEntity.ok(userService.getOrder(order_id));
    }

    @GetMapping("/cart")
    @Operation(
            summary = "Получить корзину",
            description = "Возвращает текущую корзину пользователя"
    )
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(userService.getCart());
    }

    @PostMapping("/create-order")
    @Operation(
            summary = "Создать заказ",
            description = "Создаёт заказ на основе данных корзины и адреса"
    )
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrder createOrder) {

        Integer orderId = userService.createOrder(createOrder);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Order создан успешно! ...");
        body.put("Номер Заказа", orderId);

        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PostMapping("/add-product-to-cart")
    @Operation(
            summary = "Добавить продукт в корзину",
            description = "Добавляет указанный товар в корзину пользователя"
    )
    public ResponseEntity<String> addProductToCart(@RequestBody AddToCartDto addToCartDto) {
        userService.addProductToCart(addToCartDto);
        return ResponseEntity.ok("Add Product to Cart success!");
    }

    @PutMapping("/edit-cart")
    @Operation(
            summary = "Изменить количество товара в корзине",
            description = "Позволяет обновить количество единиц товара в корзине"
    )
    public ResponseEntity<String> editCart(@Valid @RequestBody EditCartItemDto editCartItemDto) {
        userService.editCart(editCartItemDto);
        return ResponseEntity.ok("Cart updated");
    }

    @DeleteMapping("/delete-order/{order_id}")
    @Operation(
            summary = "Удалить заказ",
            description = "Удаляет заказ по ID"
    )
    public ResponseEntity<String> deleteOrder(@PathVariable Integer order_id) {
        userService.deleteOrder(order_id);
        return ResponseEntity.ok("Order delete success!");
    }

    @DeleteMapping("/order/{orderId}/product/{productId}")
    @Operation(
            summary = "Удалить товар из заказа",
            description = "Удаляет продукт из заказа"
    )
    public ResponseEntity<String> deleteProductFromOrder(
            @PathVariable Integer orderId,
            @PathVariable Integer productId
    ) {
        userService.deleteProductFromOrder(orderId, productId);
        return ResponseEntity.ok("Product removed from order");
    }

    @DeleteMapping("/delete-cart-item/{itemId}")
    @Operation(
            summary = "Удалить товар из корзины",
            description = "Удаляет позицию корзины по ID"
    )
    public ResponseEntity<String> deleteCartItem(@PathVariable Integer itemId) {
        userService.deleteCartItem(itemId);
        return ResponseEntity.ok("Item removed");
    }
}
