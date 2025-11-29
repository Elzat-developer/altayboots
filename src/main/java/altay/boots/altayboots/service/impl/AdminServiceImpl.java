package altay.boots.altayboots.service.impl;

import altay.boots.altayboots.dto.admin.CreateCatalog;
import altay.boots.altayboots.dto.admin.CreateProduct;
import altay.boots.altayboots.model.entity.Catalog;
import altay.boots.altayboots.model.entity.Product;
import altay.boots.altayboots.repository.CatalogRepo;
import altay.boots.altayboots.repository.ProductRepo;
import altay.boots.altayboots.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final ProductRepo productRepo;
    private final CatalogRepo catalogRepo;
    @Override
    public void createProduct(CreateProduct createProduct) {
        Product product = new Product();
        product.setName(createProduct.name());
        product.setDescription(createProduct.description());
        product.setText(createProduct.text());
        product.setPrice(createProduct.price());
        product.setOldPrice(createProduct.oldPrice());
        product.setPhotoURL(createProduct.photoURL());

        Catalog catalog = catalogRepo.findById(createProduct.catalog_id());
        product.setCatalog(catalog);

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
                product.getPhotoURL(),
                product.getCatalog().getId()
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

    @Override
    public void createCatalog(CreateCatalog createCatalog) {
        Catalog catalog = new Catalog();
        catalog.setName(catalog.getName());
        catalogRepo.save(catalog);
    }

    @Override
    public List<CreateCatalog> getCatalogs() {
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
    public List<CreateProduct> getProductsCatalog(int catalogId) {
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


    private CreateCatalog toDtoCatalog(Catalog catalog) {
        return new CreateCatalog(
                catalog.getName()
        );
    }

    private CreateProduct toDtoProduct(Product product) {
        return new CreateProduct(
                product.getName(),
                product.getDescription(),
                product.getText(),
                product.getPrice(),
                product.getOldPrice(),
                product.getPhotoURL(),
                product.getCatalog().getId()
        );
    }
}
