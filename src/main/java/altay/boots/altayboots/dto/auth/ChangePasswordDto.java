package altay.boots.altayboots.dto.auth;

public record ChangePasswordDto(
        String name,
        String oldPassword,
        String newPassword
) {
}
