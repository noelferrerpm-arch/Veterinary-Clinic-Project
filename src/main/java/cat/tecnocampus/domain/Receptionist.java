package cat.tecnocampus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "receptionist")
public class Receptionist extends Person {

    @Column(nullable = false)
    private String receptionist;

    public Receptionist() {
        super();
    }

    public String getReceptionist() {
        return receptionist;
    }

    public void setReceptionist(String receptionist) {
        this.receptionist = receptionist;
    }
}
