package altay.boots.altayboots.service;

import altay.boots.altayboots.dto.admin.*;

import java.util.List;

public interface AdminService {
    void createProduct(CreateProduct createProduct);

    List<GetProduct> getProducts();

    GetProduct getProduct(int productId);

    void editProduct(int product_id,CreateProduct createProduct);

    void deleteProduct(Integer productId);

    void createCatalog(CreateCatalog createCatalog);

    List<CreateCatalog> getCatalogs();

    void editCatalog(int catalogId, CreateCatalog catalog);

    List<GetProduct> getProductsCatalog(int catalogId);

    void deleteCatalog(Integer catalogId);

    void createCompanyDescription(CreateCompanyDescription createCompanyDescription);

    CompanyDescription getCompany();

    void editCompany(CreateCompanyDescription companyDescription);

    void createPromotion(CreatePromotion createPromotion);

    List<GetPromotion> getPromotions();

    GetPromotion getPromotion(int promotionId);

    void editPromotion(int promotionId, CreatePromotion createPromotion);

    void deletePromotion(Integer promotionId);
}
