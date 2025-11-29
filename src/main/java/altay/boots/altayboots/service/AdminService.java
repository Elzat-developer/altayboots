package altay.boots.altayboots.service;

import altay.boots.altayboots.dto.auth.admin.CreateProduct;

import java.util.List;

public interface AdminService {
    void createProduct(CreateProduct createProduct);

    List<CreateProduct> getProducts();

    CreateProduct getProduct(int productId);

    void editProduct(int product_id,CreateProduct createProduct);

    void deleteProduct(Integer productId);
}
