package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.admin.CompanyDescription;
import altay.boots.altayboots.dto.user.*;
import altay.boots.altayboots.service.AdminService;
import altay.boots.altayboots.service.UserService;
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
public class UserPanelController {
    private final UserService userService;
    private final AdminService adminService;
    @GetMapping("/products")
    public ResponseEntity<List<GetProductUser>> getProducts(){
        List<GetProductUser> productList = userService.getProducts();
        return ResponseEntity.ok(productList);
    }
    @GetMapping("/product/{product_id}")
    public ResponseEntity<GetProductUser> getProducts(@PathVariable Integer product_id){
        GetProductUser product = userService.getProduct(product_id);
        return ResponseEntity.ok(product);
    }
    @GetMapping("/company")
    public ResponseEntity<CompanyDescription> getCompany(){
        CompanyDescription companyDescription = adminService.getCompany();
        return ResponseEntity.ok(companyDescription);
    }
    @GetMapping("/orders/{user_id}")
    public ResponseEntity<List<GetOrder>> getOrders(@PathVariable Integer user_id){
        List<GetOrder> orders = userService.getOrders(user_id);
        return ResponseEntity.ok(orders);
    }
    @GetMapping("/order/{order_id}")
    public ResponseEntity<GetOrder> getOrder(@PathVariable Integer order_id){
        GetOrder order = userService.getOrder(order_id);
        return ResponseEntity.ok(order);
    }
    @GetMapping("/cart")
    public ResponseEntity<CartDto> getCart() {
        return ResponseEntity.ok(userService.getCart());
    }
    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody CreateOrder createOrder){

        Integer orderId = userService.createOrder(createOrder);

        Map<String, Object> body = new HashMap<>();
        body.put("message", "Order создан успешно! \n" +
                "скопируйте номер заказа и перейдите к оплате через whatsapp нажав на кнопку ниже\n" +
                "вы должны отправить продавцу номер заказа\n" +
                "Вам обязательно ответят в течение 1 часа");
        body.put("Номер Заказа", orderId);

        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PostMapping("/add-product-to-cart")
    public ResponseEntity<String> addProductToCart(@RequestBody AddToCartDto addToCartDto){
        userService.addProductToCart(addToCartDto);
        return new ResponseEntity<>("Add Product to Cart success!", HttpStatus.OK);
    }
    @PutMapping("/edit-cart")
    public ResponseEntity<String> editCart(@RequestBody EditCartItemDto editCartItemDto) {
        userService.editCart(editCartItemDto);
        return ResponseEntity.ok("Cart updated");
    }
    @DeleteMapping("/delete-order/{order_id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Integer order_id){
        userService.deleteOrder(order_id);
        return new ResponseEntity<>("Order delete success!", HttpStatus.OK);
    }
    @DeleteMapping("/order/{orderId}/product/{productId}")
    public ResponseEntity<String> deleteProductFromOrder(
            @PathVariable Integer orderId,
            @PathVariable Integer productId
    ) {
        userService.deleteProductFromOrder(orderId, productId);
        return ResponseEntity.ok("Product removed from order");
    }
    @DeleteMapping("/delete-cart-item/{itemId}")
    public ResponseEntity<String> deleteCartItem(@PathVariable Integer itemId) {
        userService.deleteCartItem(itemId);
        return ResponseEntity.ok("Item removed");
    }
}
