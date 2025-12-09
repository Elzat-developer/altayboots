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

    @Operation(
            summary = "Создать продукт",
            description = "Передай данные продукта + список фото"
    )
    @PostMapping(value = "/create-product", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createProduct(
            @ModelAttribute CreateProduct createProduct,
            @Parameter(description = "Фото продукта")
            @RequestPart("photos") List<MultipartFile> photos
    ) {
        adminService.createProduct(createProduct, photos);
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
    @PostMapping(value = "/create-promotion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createPromotion(
            @ModelAttribute CreatePromotion createPromotion,
            @Parameter(description = "Фото акции")
            @RequestPart("photos") List<MultipartFile> photos
    ) {
        adminService.createPromotion(createPromotion, photos);
        return new ResponseEntity<>("Promotion successfully created!", HttpStatus.CREATED);
    }

    // --------------------- EDIT ------------------------

    @Operation(summary = "Редактировать продукт")
    @PutMapping(value = "/edit-product/{product_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editProduct(
            @Parameter(description = "ID продукта")
            @PathVariable Integer product_id,

            @ModelAttribute EditProduct editProduct,

            @Parameter(description = "Новые фото", required = false)
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        adminService.editProduct(product_id, editProduct, photos);
        return new ResponseEntity<>("Product edit success!", HttpStatus.OK);
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

    @Operation(summary = "Редактировать информацию о компании")
    @PutMapping(value = "/edit-company", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editCompany(
            @ModelAttribute CreateCompanyDescription companyDescription,

            @Parameter(description = "Новое фото", required = false)
            @RequestPart(value = "photo", required = false) MultipartFile photo
    ) {
        adminService.editCompany(companyDescription, photo);
        return new ResponseEntity<>("Company edit success!", HttpStatus.OK);
    }

    @Operation(summary = "Редактировать акцию")
    @PutMapping(value = "/edit-promotion/{promotion_id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> editPromotion(
            @Parameter(description = "ID акции")
            @PathVariable Integer promotion_id,

            @ModelAttribute EditPromotion editPromotion,

            @Parameter(description = "Новые фото", required = false)
            @RequestPart(value = "photos", required = false) List<MultipartFile> photos
    ) {
        adminService.editPromotion(promotion_id, editPromotion, photos);
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
}

