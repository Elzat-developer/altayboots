package altay.boots.altayboots.service;

import altay.boots.altayboots.dto.admin.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AdminService {
    void createProduct(CreateProduct createProduct, List<Integer> photoIds);
    List<GetProduct> getProducts();
    GetProduct getProduct(int productId);
    void editProduct(int productId, EditProduct editProduct, List<Integer> photoIds);
    void deleteProduct(Integer productId);

    void createCatalog(CreateCatalog createCatalog);
    List<GetCatalog> getCatalogs();
    void editCatalog(int catalogId, CreateCatalog catalog);
    List<GetProduct> getProductsCatalog(int catalogId);
    void deleteCatalog(Integer catalogId);

    CompanyDescription getCompany();
    void editCompany(CreateCompanyDescription companyDescription, Integer photoId);

    void createPromotion(CreatePromotion createPromotion, List<Integer> photoIds);
    List<GetPromotion> getPromotions();
    GetPromotion getPromotion(int promotionId);
    void editPromotion(int promotionId, EditPromotion editPromotion, List<Integer> photoIds);
    void deletePromotion(Integer promotionId);

    List<GetAdminOrderSimple> getOrders();
    GetAdminOrder getOrder(Integer orderId);
    void editOrder(Integer orderId,EditOrder editOrder);
    void deleteOrder(Integer orderId);

    List<GetPromotionFirstImage> getPromotionFirstImage();

    void createPhotos(List<MultipartFile> photos);
    List<GetPhotoDto> getAllPhotos(); // Чтобы видеть ID
    void editPhoto(Integer photoId, MultipartFile photo);
    void deletePhoto(Integer photoId);
}
