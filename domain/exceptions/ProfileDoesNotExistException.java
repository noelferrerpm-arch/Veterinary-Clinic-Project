package cat.tecnocampus.domain.exceptions;

public class ProfileDoesNotExistException extends RuntimeException {
    public ProfileDoesNotExistException(String message) {
        super(message);
    }
}
