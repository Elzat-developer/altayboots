package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    // --------------------- PRODUCTS ------------------------

    @Operation(summary = "Получить список продуктов")
    @ApiResponse(responseCode = "200", description = "Список продуктов успешно получен")
    @GetMapping("/products")
    public ResponseEntity<List<GetProduct>> getProducts() {
        return ResponseEntity.ok(adminService.getProducts());
    }

    @Operation(summary = "Получить продукт по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Продукт найден"),
            @ApiResponse(responseCode = "404", description = "Продукт не найден")
    })
    @GetMapping("/product/{product_id}")
    public ResponseEntity<GetProduct> getProducts(
            @Parameter(description = "ID продукта", example = "1")
            @PathVariable Integer product_id
    ) {
        return ResponseEntity.ok(adminService.getProduct(product_id));
    }

    // --------------------- CATALOGS ------------------------

    @Operation(summary = "Получить список каталогов")
    @GetMapping("/catalogs")
    public ResponseEntity<List<GetCatalog>> getCatalogs() {
        return ResponseEntity.ok(adminService.getCatalogs());
    }

    @Operation(summary = "Получить продукты каталога по ID")
    @GetMapping("/catalog/{catalog_id}")
    public ResponseEntity<List<GetProduct>> getCatalog(
            @Parameter(description = "ID каталога", example = "1")
            @PathVariable Integer catalog_id
    ) {
        return ResponseEntity.ok(adminService.getProductsCatalog(catalog_id));
    }

    // --------------------- COMPANY ------------------------

    @Operation(summary = "Получить описание компании")
    @GetMapping("/company")
    public ResponseEntity<CompanyDescription> getCompany() {
        return ResponseEntity.ok(adminService.getCompany());
    }

    // --------------------- PROMOTIONS ------------------------

    @Operation(summary = "Получить список акций")
    @GetMapping("/promotions")
    public ResponseEntity<List<GetPromotion>> getPromotions() {
        return ResponseEntity.ok(adminService.getPromotions());
    }

    @Operation(summary = "Получить акцию по ID")
    @GetMapping("/promotion/{promotion_id}")
    public ResponseEntity<GetPromotion> getPromotions(
            @Parameter(description = "ID акции", example = "1")
            @PathVariable Integer promotion_id
    ) {
        return ResponseEntity.ok(adminService.getPromotion(promotion_id));
    }

    // --------------------- ORDERS ------------------------

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

    @Operation(summary = "Создать описание компании")
    @PostMapping(value = "/create-company", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createCompanyDescription(
            @ModelAttribute CreateCompanyDescription createCompanyDescription,
            @Parameter(description = "Фото компании")
            @RequestPart("photo") MultipartFile photo
    ) {
        adminService.createCompanyDescription(createCompanyDescription, photo);
        return new ResponseEntity<>("Company successfully created!", HttpStatus.CREATED);
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

