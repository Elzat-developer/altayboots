package altay.boots.altayboots.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Запрос для регистрации нового пользователя")
public record SignUpRequest(

        @Schema(description = "Имя пользователя", example = "Азамат")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Номер телефона", example = "+77071112233")
        @NotBlank(message = "Phone is required")
        String phone,

        @Schema(description = "Пароль", example = "qwerty123")
        @NotBlank(message = "Password is required")
        String password
) {}

