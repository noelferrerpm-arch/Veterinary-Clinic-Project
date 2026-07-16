package cat.tecnocampus.domain;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity(name="Speciality")
@Table(name="speciality")
public class Speciality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long specialtyId;
    private String name;

    //Many veterinarians can have the same specialities
    // and many specialties can be assigned to the same veterinarian
    @ManyToMany(mappedBy = "specialities")
    private Set<Veterinarian> veterinarians = new HashSet<>();

    public Speciality() {}

    public Speciality(String name) {
        this.name = name;
    }

    public Long getSpecialtyId() {
        return specialtyId;
    }

    public void setSpecialtyId(Long specialtyId) {
        this.specialtyId = specialtyId;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<Veterinarian> getVeterinarians() {
        return veterinarians;
    }

    public void setVeterinarians(Set<Veterinarian> veterinarians) {
        this.veterinarians = veterinarians;
    }
}
