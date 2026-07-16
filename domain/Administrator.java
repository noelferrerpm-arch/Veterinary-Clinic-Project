package cat.tecnocampus.domain;
import jakarta.persistence.*;

@Entity
@Table(name = "administrator")
@PrimaryKeyJoinColumn(name = "person_id")
public class Administrator extends Person {

    private String admin;

    protected Administrator() {}

    public String getAdmin() { return admin; }
    public void setAdmin(String admin) { this.admin = admin; }
}
