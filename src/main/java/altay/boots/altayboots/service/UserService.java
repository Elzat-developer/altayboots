package altay.boots.altayboots.service;

import altay.boots.altayboots.dto.user.*;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

public interface UserService {
    UserDetailsService userDetailsService();

    Integer createOrder(CreateOrder createOrder);

    List<GetOrder> getOrders();

    DetailedOrderDTO getOrder(int orderId);

    void deleteOrder(Integer orderId);

    void deleteProductFromOrder(int orderId, int productId);

    void addProductToCart(AddToCartDto addToCartDto);

    CartDto getCart();

    void editCart(EditCartItemDto editCartItemDto);

    void deleteCartItem(Integer itemId);

}
