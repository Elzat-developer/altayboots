package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminPanelController {
    private final AdminService adminService;
    @GetMapping("/products")
    public ResponseEntity<List<GetProduct>> getProducts(){
        List<GetProduct> productList = adminService.getProducts();
        return ResponseEntity.ok(productList);
    }
    @GetMapping("/product/{product_id}")
    public ResponseEntity<GetProduct> getProducts(@PathVariable Integer product_id){
        GetProduct product = adminService.getProduct(product_id);
        return ResponseEntity.ok(product);
    }
    @GetMapping("/catalogs")
    public ResponseEntity<List<CreateCatalog>> getCatalogs(){
        List<CreateCatalog> catalogList = adminService.getCatalogs();
        return ResponseEntity.ok(catalogList);
    }
    @GetMapping("/catalog/{catalog_id}")
    public ResponseEntity<List<GetProduct>> getCatalog(@PathVariable Integer catalog_id){
        List<GetProduct> products = adminService.getProductsCatalog(catalog_id);
        return ResponseEntity.ok(products);
    }
    @GetMapping("/company")
    public ResponseEntity<CompanyDescription> getCompany(){
        CompanyDescription companyDescription = adminService.getCompany();
        return ResponseEntity.ok(companyDescription);
    }
    @GetMapping("/promotions")
    public ResponseEntity<List<GetPromotion>> getPromotions(){
        List<GetPromotion> promotions = adminService.getPromotions();
        return ResponseEntity.ok(promotions);
    }
    @GetMapping("/promotion/{promotion_id}")
    public ResponseEntity<GetPromotion> getPromotions(@PathVariable Integer promotion_id){
        GetPromotion promotion = adminService.getPromotion(promotion_id);
        return ResponseEntity.ok(promotion);
    }
    @GetMapping("/orders")
    public ResponseEntity<List<GetAdminOrderSimple>> getOrders(){
        List<GetAdminOrderSimple> orders = adminService.getOrders();
        return ResponseEntity.ok(orders);
    }
    @GetMapping("/order/{order_id}")
    public ResponseEntity<GetAdminOrder> getOrders(@PathVariable Integer order_id){
        GetAdminOrder order = adminService.getOrder(order_id);
        return ResponseEntity.ok(order);
    }
    @PostMapping(value = "/create-product",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createProduct(
            @ModelAttribute CreateProduct createProduct,
            @RequestPart("photos") List<MultipartFile> photos){
        adminService.createProduct(createProduct,photos);
        return new ResponseEntity<>("Product successfully created!", HttpStatus.CREATED);
    }
    @PostMapping("/create-catalog")
    public ResponseEntity<String> createCatalog(@RequestBody CreateCatalog createCatalog){
        adminService.createCatalog(createCatalog);
        return new ResponseEntity<>("Catalog successfully created!", HttpStatus.CREATED);
    }
    @PostMapping(value = "/create-company", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createCompanyDescription(
            @ModelAttribute CreateCompanyDescription createCompanyDescription,
            @RequestPart("photo") MultipartFile photo
    ){
        adminService.createCompanyDescription(createCompanyDescription,photo);
        return new ResponseEntity<>("Company successfully created!", HttpStatus.CREATED);
    }
    @PostMapping(value = "/create-promotion",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPromotion(
            @ModelAttribute CreatePromotion createPromotion,
            @RequestPart("photos") List<MultipartFile> photos
    ){
        adminService.createPromotion(createPromotion,photos);
        return new ResponseEntity<>("Promotion successfully created!", HttpStatus.CREATED);
    }
    @PutMapping(value = "/edit-product/{product_id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editProduct(
            @PathVariable Integer product_id,
            @ModelAttribute EditProduct editProduct,
            @RequestPart(value = "photos",required = false) List<MultipartFile> photos){
        adminService.editProduct(product_id,editProduct,photos);
        return new ResponseEntity<>("Product edit success!", HttpStatus.OK);
    }
    @PutMapping("/edit-catalog/{catalog_id}")
    public ResponseEntity<String> editCatalog(
            @PathVariable Integer catalog_id,
            @RequestBody CreateCatalog catalog
    ){
        adminService.editCatalog(catalog_id,catalog);
        return new ResponseEntity<>("Catalog edit success!", HttpStatus.OK);
    }
    @PutMapping(value = "/edit-company",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editCompany(
            @ModelAttribute CreateCompanyDescription companyDescription,
            @RequestPart(value = "photo",required = false) MultipartFile photo){
        adminService.editCompany(companyDescription,photo);
        return new ResponseEntity<>("Company edit success!", HttpStatus.OK);
    }
    @PutMapping(value = "/edit-promotion/{promotion_id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editPromotion(
            @PathVariable Integer promotion_id,
            @ModelAttribute EditPromotion editPromotion,
            @RequestPart(value = "photos",required = false) List<MultipartFile> photos){
        adminService.editPromotion(promotion_id,editPromotion,photos);
        return new ResponseEntity<>("Promotion edit success!", HttpStatus.OK);
    }
    @PutMapping("/edit-order/{order_id}")
    public ResponseEntity<String> editOrder(@PathVariable Integer order_id,@RequestBody EditOrder editOrder){
        adminService.editOrder(order_id,editOrder);
        return new ResponseEntity<>("Order edit success!", HttpStatus.OK);
    }
    @DeleteMapping("/delete-product/{product_id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer product_id){
        adminService.deleteProduct(product_id);
        return new ResponseEntity<>("Product delete success!", HttpStatus.OK);
    }
    @DeleteMapping("/delete-catalog/{catalog_id}")
    public ResponseEntity<String> deleteCatalog(@PathVariable Integer catalog_id){
        adminService.deleteCatalog(catalog_id);
        return new ResponseEntity<>("Catalog delete success!", HttpStatus.OK);
    }
    @DeleteMapping("/delete-promotion/{promotion_id}")
    public ResponseEntity<String> deletePromotion(@PathVariable Integer promotion_id){
        adminService.deletePromotion(promotion_id);
        return new ResponseEntity<>("Promotion delete success!", HttpStatus.OK);
    }
    @DeleteMapping("/delete-order/{order_id}")
    public ResponseEntity<String> deleteOrder(@PathVariable Integer order_id){
        adminService.deleteOrder(order_id);
        return new ResponseEntity<>("Order delete success!", HttpStatus.OK);
    }
}
