package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.auth.admin.CreateProduct;
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
    public ResponseEntity<List<CreateProduct>> getProducts(){
        List<CreateProduct> productList = adminService.getProducts();
        return ResponseEntity.ok(productList);
    }
    @GetMapping("/product/{product_id}")
    public ResponseEntity<CreateProduct> getProducts(@PathVariable Integer product_id){
        CreateProduct product = adminService.getProduct(product_id);
        return ResponseEntity.ok(product);
    }
    @PostMapping("/create-product")
    public ResponseEntity<String> createProduct(@RequestBody CreateProduct createProduct){
        adminService.createProduct(createProduct);
        return new ResponseEntity<>("Product successfully created!", HttpStatus.CREATED);
    }
    @PutMapping("/edit-product/{product_id}")
    public ResponseEntity<String> editProduct(
            @PathVariable Integer product_id,
            @RequestBody CreateProduct createProduct){
        adminService.editProduct(product_id,createProduct);
        return new ResponseEntity<>("Product edit success!", HttpStatus.OK);
    }
    @DeleteMapping("/delete-product/{product_id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Integer product_id){
        adminService.deleteProduct(product_id);
        return new ResponseEntity<>("Product delete success!", HttpStatus.OK);
    }
}
