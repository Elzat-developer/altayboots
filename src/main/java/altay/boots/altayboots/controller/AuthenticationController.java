package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.admin.*;
import altay.boots.altayboots.dto.auth.JwtAuthenticationResponce;
import altay.boots.altayboots.dto.auth.SignInRequest;
import altay.boots.altayboots.dto.auth.SignUpRequest;
import altay.boots.altayboots.service.AdminService;
import altay.boots.altayboots.service.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Аутентификация", description = "Эндпоинты для регистрации и входа пользователя")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final AdminService adminService;

    @PostMapping("/sign-in")
    @Operation(
            summary = "Авторизация пользователя",
            description = "Принимает телефон и пароль, возвращает JWT токен"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Успешная авторизация",
            content = @Content(schema = @Schema(implementation = JwtAuthenticationResponce.class))
    )
    @ApiResponse(
            responseCode = "401",
            description = "Неверный логин или пароль"
    )
    public ResponseEntity<JwtAuthenticationResponce> signIn(
            @RequestBody SignInRequest signInRequest
    ) {
        return new ResponseEntity<>(authenticationService.signIn(signInRequest), HttpStatus.OK);
    }


    @PostMapping("/sign-up")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Создаёт учетную запись на основе имени, телефона и пароля"
    )
    @ApiResponse(
            responseCode = "201",
            description = "Аккаунт успешно создан"
    )
    @ApiResponse(
            responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(schema = @Schema())
    )
    public ResponseEntity<String> signUp(
            @Valid @RequestBody SignUpRequest signUpRequest
    ) {
        authenticationService.signUp(signUpRequest);
        return new ResponseEntity<>("Аккаунт успешно сохранен!", HttpStatus.CREATED);
    }

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
    @GetMapping("/company")
    @Operation(
            summary = "Получить данные о компании",
            description = "Возвращает описание компании"
    )
    public ResponseEntity<CompanyDescription> getCompany() {
        return ResponseEntity.ok(adminService.getCompany());
    }

    @Operation(summary = "Получить список каталогов")
    @GetMapping("/catalogs")
    public ResponseEntity<List<GetCatalog>> getCatalogs() {
        return ResponseEntity.ok(adminService.getCatalogs());
    }

    @Operation(summary = "Получить продукты каталога по ID")
    @GetMapping("/catalog-products/{catalog_id}")
    public ResponseEntity<List<GetProduct>> getCatalog(
            @Parameter(description = "ID каталога", example = "1")
            @PathVariable Integer catalog_id
    ) {
        return ResponseEntity.ok(adminService.getProductsCatalog(catalog_id));
    }
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
    @Operation(summary = "Получить список акций, но его только первое фото и id")
    @GetMapping("/promotions-first-image")
    public ResponseEntity<List<GetPromotionFirstImage>> getPromotionFirstImage(){
        return ResponseEntity.ok(adminService.getPromotionFirstImage());
    }
    @Operation(summary = "Получить все загруженные фотографии")
    @GetMapping("/photos")
    public ResponseEntity<List<GetPhotoDto>> getAllPhotos() {
        return ResponseEntity.ok(adminService.getAllPhotos());
    }
}