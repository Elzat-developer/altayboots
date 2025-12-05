package altay.boots.altayboots.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос для авторизации пользователя")
public record SignInRequest(

        @Schema(description = "Номер телефона", example = "+77071112233")
        @NotBlank(message = "Phone is required")
        String phone,

        @Schema(description = "Пароль", example = "123456")
        @NotBlank(message = "Password is required")
        String password
) {}

