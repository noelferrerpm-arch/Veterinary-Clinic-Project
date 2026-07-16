package cat.tecnocampus.security.authentication;

public record AuthenticationRequest (Long userId, String password) {
}
