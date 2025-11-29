package altay.boots.altayboots.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record SignInRequest(
        @NotBlank(message = "Name is required")String name,
        @NotBlank(message = "Password is required")String password
) {
}
