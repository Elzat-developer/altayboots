package altay.boots.altayboots.controller;

import altay.boots.altayboots.dto.admin.CompanyDescription;
import altay.boots.altayboots.dto.auth.JwtAuthenticationResponce;
import altay.boots.altayboots.dto.auth.SignInRequest;
import altay.boots.altayboots.dto.auth.SignUpRequest;
import altay.boots.altayboots.dto.user.GetProductUser;
import altay.boots.altayboots.service.AdminService;
import altay.boots.altayboots.service.AuthenticationService;
import altay.boots.altayboots.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    private final UserService userService;

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
            @org.springframework.web.bind.annotation.RequestBody SignUpRequest signUpRequest
    ) {
        authenticationService.signUp(signUpRequest);
        return new ResponseEntity<>("Аккаунт успешно сохранен!", HttpStatus.CREATED);
    }
    @GetMapping("/products")
    @Operation(
            summary = "Получить список продуктов",
            description = "Возвращает список товаров для пользовательской витрины"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список товаров успешно получен",
            content = @Content(schema = @Schema(implementation = GetProductUser.class))
    )
    public ResponseEntity<List<GetProductUser>> getProducts() {
        return ResponseEntity.ok(userService.getProducts());
    }

    @GetMapping("/product/{product_id}")
    @Operation(
            summary = "Получить один продукт",
            description = "Возвращает данные одного товара по его ID"
    )
    public ResponseEntity<GetProductUser> getProducts(@PathVariable Integer product_id) {
        return ResponseEntity.ok(userService.getProduct(product_id));
    }
    @GetMapping("/company")
    @Operation(
            summary = "Получить данные о компании",
            description = "Возвращает описание компании"
    )
    public ResponseEntity<CompanyDescription> getCompany() {
        return ResponseEntity.ok(adminService.getCompany());
    }
}