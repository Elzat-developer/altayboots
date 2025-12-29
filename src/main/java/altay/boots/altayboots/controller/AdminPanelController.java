package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Admin Panel", description = "Управление продуктами, каталогами, акциями, заказами и компанией")
public class AdminPanelController {

    private final AdminService adminService;

    @Operation(summary = "Получить список заказов")
    @GetMapping("/orders")
    public ResponseEntity<List<GetAdminOrderSimple>> getOrders() {
        return ResponseEntity.ok(adminService.getOrders());
    }

    @Operation(summary = "Получить заказ по ID")
    @GetMapping("/order/{order_id}")
    public ResponseEntity<GetAdminOrder> getOrders(
            @Parameter(description = "ID заказа", example = "5")
            @PathVariable Integer order_id
    ) {
        return ResponseEntity.ok(adminService.getOrder(order_id));
    }

    // --------------------- CREATE ------------------------

    @Operation(summary = "Создать продукта")
    @PostMapping("/create-product")
    public ResponseEntity<String> createProduct(@RequestBody CreateProduct createProduct) {
        adminService.createProduct(createProduct);
        return new ResponseEntity<>("Product successfully created!", HttpStatus.CREATED);
    }

    @Operation(summary = "Создать каталог")
    @PostMapping("/create-catalog")
    public ResponseEntity<String> createCatalog(
            @RequestBody CreateCatalog createCatalog
    ) {
        adminService.createCatalog(createCatalog);
        return new ResponseEntity<>("Catalog successfully created!", HttpStatus.CREATED);
    }

    @Operation(summary = "Создать акцию")
    @PostMapping("/create-promotion")
    public ResponseEntity<String> createPromotion(
            @RequestBody CreatePromotion createPromotion,
            @RequestParam(required = false) List<Integer> photoIds
    ) {
        adminService.createPromotion(createPromotion, photoIds);
        return new ResponseEntity<>("Promotion created!", HttpStatus.CREATED);
    }
    @Operation(summary = "Загрузить фотографии (без привязки)")
    @PostMapping(value = "/create-photos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPhotos(@RequestPart("photos") List<MultipartFile> photos) {
        adminService.createPhotos(photos);
        return new ResponseEntity<>("Photos uploaded successfully", HttpStatus.CREATED);
    }

    // --------------------- EDIT ------------------------

    @Operation(summary = "Редактировать продукт")
    @PutMapping("/edit-product/{product_id}")
    public ResponseEntity<String> editProduct(
            @PathVariable Integer product_id,
            @RequestBody EditProduct editProduct,
            @RequestParam(required = false) List<Integer> photoIds
    ) {
        adminService.editProduct(product_id, editProduct, photoIds);
        return ResponseEntity.ok("Product updated");
    }

    @Operation(summary = "Редактировать каталог")
    @PutMapping("/edit-catalog/{catalog_id}")
    public ResponseEntity<String> editCatalog(
            @Parameter(description = "ID каталога")
            @PathVariable Integer catalog_id,
            @RequestBody CreateCatalog catalog
    ) {
        adminService.editCatalog(catalog_id, catalog);
        return new ResponseEntity<>("Catalog edit success!", HttpStatus.OK);
    }

    @Operation(summary = "Редактировать компанию")
    @PutMapping("/edit-company")
    public ResponseEntity<String> editCompany(
            @RequestBody CreateCompanyDescription description,
            @RequestParam(required = false) Integer photoId
    ) {
        adminService.editCompany(description, photoId);
        return ResponseEntity.ok("Company updated");
    }

    @Operation(summary = "Редактировать акцию")
    @PutMapping(value = "/edit-promotion/{promotion_id}")
    public ResponseEntity<String> editPromotion(
            @Parameter(description = "ID акции")
            @PathVariable Integer promotion_id,
            @RequestBody EditPromotion editPromotion,
            @Parameter(description = "Новые фото", required = false)
            @RequestParam(required = false) List<Integer> photoIds
    ) {
        adminService.editPromotion(promotion_id, editPromotion, photoIds);
        return new ResponseEntity<>("Promotion edit success!", HttpStatus.OK);
    }

    @Operation(summary = "Редактировать заказ")
    @PutMapping("/edit-order/{order_id}")
    public ResponseEntity<String> editOrder(
            @Parameter(description = "ID заказа")
            @PathVariable Integer order_id,
            @RequestBody EditOrder editOrder
    ) {
        adminService.editOrder(order_id, editOrder);
        return new ResponseEntity<>("Order edit success!", HttpStatus.OK);
    }
    @Operation(summary = "Заменить файл фотографии по ID")
    @PutMapping(value = "/edit-photo/{photo_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editPhoto(@PathVariable Integer photo_id, @RequestPart("photo") MultipartFile photo) {
        adminService.editPhoto(photo_id, photo);
        return ResponseEntity.ok("Photo updated");
    }
    // --------------------- DELETE ------------------------

    @Operation(summary = "Удалить продукт")
    @DeleteMapping("/delete-product/{product_id}")
    public ResponseEntity<String> deleteProduct(
            @Parameter(description = "ID продукта")
            @PathVariable Integer product_id
    ) {
        adminService.deleteProduct(product_id);
        return new ResponseEntity<>("Product delete success!", HttpStatus.OK);
    }

    @Operation(summary = "Удалить каталог")
    @DeleteMapping("/delete-catalog/{catalog_id}")
    public ResponseEntity<String> deleteCatalog(
            @Parameter(description = "ID каталога")
            @PathVariable Integer catalog_id
    ) {
        adminService.deleteCatalog(catalog_id);
        return new ResponseEntity<>("Catalog delete success!", HttpStatus.OK);
    }

    @Operation(summary = "Удалить акцию")
    @DeleteMapping("/delete-promotion/{promotion_id}")
    public ResponseEntity<String> deletePromotion(
            @Parameter(description = "ID акции")
            @PathVariable Integer promotion_id
    ) {
        adminService.deletePromotion(promotion_id);
        return new ResponseEntity<>("Promotion delete success!", HttpStatus.OK);
    }

    @Operation(summary = "Удалить заказ")
    @DeleteMapping("/delete-order/{order_id}")
    public ResponseEntity<String> deleteOrder(
            @Parameter(description = "ID заказа")
            @PathVariable Integer order_id
    ) {
        adminService.deleteOrder(order_id);
        return new ResponseEntity<>("Order delete success!", HttpStatus.OK);
    }
    @Operation(summary = "Удалить фотографию (совсем)")
    @DeleteMapping("/delete-photo/{photo_id}")
    public ResponseEntity<String> deletePhoto(@PathVariable Integer photo_id) {
        adminService.deletePhoto(photo_id);
        return ResponseEntity.ok("Photo deleted");
    }
}

