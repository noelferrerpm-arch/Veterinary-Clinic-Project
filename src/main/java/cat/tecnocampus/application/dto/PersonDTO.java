package cat.tecnocampus.application.dto;

public record PersonDTO (
    long personId,
    String firstName,
    String lastName,
    String phoneNumber,
    String email,
    String address
    ){
}
