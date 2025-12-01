package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @PostMapping("/create-product")
    public ResponseEntity<String> createProduct(@RequestBody CreateProduct createProduct){
        adminService.createProduct(createProduct);
        return new ResponseEntity<>("Product successfully created!", HttpStatus.CREATED);
    }
    @PostMapping("/create-catalog")
    public ResponseEntity<String> createCatalog(@RequestBody CreateCatalog createCatalog){
        adminService.createCatalog(createCatalog);
        return new ResponseEntity<>("Catalog successfully created!", HttpStatus.CREATED);
    }
    @PostMapping("/create-company")
    public ResponseEntity<String> createCompanyDescription(
            @RequestBody CreateCompanyDescription createCompanyDescription
    ){
        adminService.createCompanyDescription(createCompanyDescription);
        return new ResponseEntity<>("Company successfully created!", HttpStatus.CREATED);
    }
    @PostMapping("/create-promotion")
    public ResponseEntity<String> createPromotion(@RequestBody CreatePromotion createPromotion){
        adminService.createPromotion(createPromotion);
        return new ResponseEntity<>("Promotion successfully created!", HttpStatus.CREATED);
    }
    @PutMapping("/edit-product/{product_id}")
    public ResponseEntity<String> editProduct(
            @PathVariable Integer product_id,
            @RequestBody CreateProduct createProduct){
        adminService.editProduct(product_id,createProduct);
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
    @PutMapping("/edit-company")
    public ResponseEntity<String> editCompany(
            @RequestBody CreateCompanyDescription companyDescription){
        adminService.editCompany(companyDescription);
        return new ResponseEntity<>("Company edit success!", HttpStatus.OK);
    }
    @PutMapping("/edit-promotion/{promotion_id}")
    public ResponseEntity<String> editPromotion(
            @PathVariable Integer promotion_id,
            @RequestBody CreatePromotion createPromotion){
        adminService.editPromotion(promotion_id,createPromotion);
        return new ResponseEntity<>("Promotion edit success!", HttpStatus.OK);
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
}
