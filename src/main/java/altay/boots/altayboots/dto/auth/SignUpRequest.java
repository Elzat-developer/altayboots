package altay.boots.altayboots.dto.auth;

import jakarta.validation.constraints.NotBlank;

public record SignUpRequest(
        @NotBlank(message = "Name is required")String name,
        @NotBlank(message = "Phone is required")String phone,
        @NotBlank(message = "Password is required")String password
) {
}
