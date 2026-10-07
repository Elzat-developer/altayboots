package altay.boots.altayboots.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
        @NotBlank(message = "Пароль не может быть пустым")
        @Size(min = 8, message = "Пароль должен содержать не менее 8 символов")
        @Pattern(
                regexp = "^(?=.*[0-9])(?=.*[a-zA-Zа-яА-ЯёЁ]).{8,}$",
                message = "Пароль должен содержать как минимум одну букву и одну цифру"
        )
        String password
) {}

