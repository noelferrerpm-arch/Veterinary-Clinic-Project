package cat.tecnocampus.domain;

import cat.tecnocampus.domain.exceptions.InvalidDataException;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="treatment")
public class Treatment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long treatmentId;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "cost")
    private double cost;


    @OneToMany(mappedBy = "visitId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Visit> visits;

    public long getTreatmentId() {
        return treatmentId;
    }

    public void setTreatmentId(long treatmentId) {
        this.treatmentId = treatmentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name== null || name.isBlank()) {
            throw new InvalidDataException("Treatment name must not be blank");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new InvalidDataException("Treatment description must not be blank");
        }
        this.description = description;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        if (cost < 0) {
            throw new InvalidDataException("Cost must be >= 0");
        }
        this.cost = cost;
    }

    public List<Visit> getVisits() {
        return visits;
    }

    public void setVisits(List<Visit> visits) {
        this.visits = visits;
    }

    public void addVisit(Visit visit) {
        if (visits == null) {
            visits = new ArrayList<>();
        }
        visits.add(visit);
    }
}
