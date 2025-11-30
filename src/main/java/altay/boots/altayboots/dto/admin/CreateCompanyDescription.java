package altay.boots.altayboots.dto.admin;


public record CreateCompanyDescription(
        String name,
        String text,
        String photoURL,
        String base,
        String city
) {
}
