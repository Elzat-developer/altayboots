package altay.boots.altayboots.service;

import altay.boots.altayboots.dto.admin.CreateCatalog;
import altay.boots.altayboots.dto.admin.CreateCompanyDescription;
import altay.boots.altayboots.dto.admin.CreateProduct;

import java.util.List;

public interface AdminService {
    void createProduct(CreateProduct createProduct);

    List<CreateProduct> getProducts();

    CreateProduct getProduct(int productId);

    void editProduct(int product_id,CreateProduct createProduct);

    void deleteProduct(Integer productId);

    void createCatalog(CreateCatalog createCatalog);

    List<CreateCatalog> getCatalogs();

    void editCatalog(int catalogId, CreateCatalog catalog);

    List<CreateProduct> getProductsCatalog(int catalogId);

    void deleteCatalog(Integer catalogId);

    void createCompanyDescription(CreateCompanyDescription createCompanyDescription);

    CreateCompanyDescription getCompany();

    void editCompany(CreateCompanyDescription companyDescription);
}
