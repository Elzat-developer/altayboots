package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.auth.admin.CreateProduct;
import altay.boots.altayboots.model.entity.Product;
import altay.boots.altayboots.repository.ProductRepo;
import altay.boots.altayboots.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final ProductRepo productRepo;
    @Override
    public void createProduct(CreateProduct createProduct) {
        Product product = new Product();
        product.setName(createProduct.name());
        product.setDescription(createProduct.description());
        product.setText(createProduct.text());
        product.setPrice(createProduct.price());
        product.setOldPrice(createProduct.oldPrice());
        product.setPhotoURL(createProduct.photoURL());
        productRepo.save(product);
    }

    @Override
    public List<CreateProduct> getProducts() {
        List<Product> products = productRepo.findAll();
        return products.stream()
                .map(this::toDtoProduct)
                .toList();
    }

    @Override
    public CreateProduct getProduct(int productId) {
        Product product = productRepo.findById(productId);
        return new CreateProduct(
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                product.getPhotoURL()
        );
    }

    @Override
    public void editProduct(int product_id,CreateProduct createProduct) {
        Product product = productRepo.findById(product_id);
        product.setName(createProduct.name());
        product.setDescription(createProduct.description());
        product.setText(createProduct.text());
        product.setPrice(createProduct.price());
        product.setOldPrice(createProduct.oldPrice());
        product.setPhotoURL(createProduct.photoURL());
        productRepo.save(product);
    }

    @Override
    public void deleteProduct(Integer productId) {
        productRepo.deleteById(productId);
    }

    private CreateProduct toDtoProduct(Product product) {
        return new CreateProduct(
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                product.getPhotoURL()
        );
    }
}
