package altay.boots.altayboots.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
        @NotBlank(message = "Phone is required")String phone,
        @NotBlank(message = "Password is required")String password
) {
}
