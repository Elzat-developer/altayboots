package altay.boots.altayboots.service;

import altay.boots.altayboots.dto.admin.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminService {
    void createProduct(CreateProduct createProduct,List<MultipartFile> photos);

    List<GetProduct> getProducts();

    GetProduct getProduct(int productId);

    void editProduct(int product_id,EditProduct editProduct,List<MultipartFile> photos);

    void deleteProduct(Integer productId);

    void createCatalog(CreateCatalog createCatalog);

    List<GetCatalog> getCatalogs();

    void editCatalog(int catalogId, CreateCatalog catalog);

    List<GetProduct> getProductsCatalog(int catalogId);

    void deleteCatalog(Integer catalogId);

    void createCompanyDescription(CreateCompanyDescription createCompanyDescription, MultipartFile photo);

    CompanyDescription getCompany();

    void editCompany(CreateCompanyDescription companyDescription,MultipartFile photo);

    void createPromotion(CreatePromotion createPromotion,List<MultipartFile> photos);

    List<GetPromotion> getPromotions();

    GetPromotion getPromotion(int promotionId);

    void editPromotion(int promotionId, EditPromotion editPromotion,List<MultipartFile> photos);

    void deletePromotion(Integer promotionId);

    List<GetAdminOrderSimple> getOrders();

    GetAdminOrder getOrder(Integer orderId);

    void editOrder(Integer orderId,EditOrder editOrder);

    void deleteOrder(Integer orderId);
}
