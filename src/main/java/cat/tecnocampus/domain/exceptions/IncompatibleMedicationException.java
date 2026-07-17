package cat.tecnocampus.domain.exceptions;

public class IncompatibleMedicationException extends RuntimeException {
    public IncompatibleMedicationException(String message) {
        super(message);
    }
}
