package altay.boots.altayboots.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Запрос для регистрации нового пользователя")
public record SignUpRequest(

        @Schema(description = "Имя пользователя", example = "Азамат")
        @NotBlank(message = "Имя не может быть пустым")
        String name,

        @Schema(description = "Номер телефона", example = "+77071112233")
        @NotBlank(message = "Телефон не может быть пустым")
        String phone,

        @Schema(description = "Пароль", example = "qwerty123")
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        String password
) {}

