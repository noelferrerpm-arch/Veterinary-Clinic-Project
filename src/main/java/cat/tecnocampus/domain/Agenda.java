package cat.tecnocampus.domain;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agenda")
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long agendaId;

    //The veterinarian whose schedule is this one
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    //@JoinColumn(name = "person_id")
    private Veterinarian veterinarian;

    private int agenda_year;

    //List of visits scheduled for the day
    @OneToMany(mappedBy = "agenda", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Visit> scheduledVisits = new ArrayList<>();

    public Long getAgendaId() {
        return agendaId;
    }

    public void setAgendaId(Long agendaId) {
        this.agendaId = agendaId;
    }

    public Veterinarian getVeterinarian() {
        return veterinarian;
    }

    public void setVeterinarian(Veterinarian veterinarian) {
        this.veterinarian = veterinarian;
    }

    public int getAgendaYear() {
        return agenda_year;
    }

    public void setAgendaYear(int agenda_year) {
        this.agenda_year = agenda_year;
    }

    public List<Visit> getScheduledVisits() {
        return scheduledVisits;
    }

    public void setScheduledVisits(List<Visit> scheduledVisits) {
        this.scheduledVisits = scheduledVisits;
    }

    public void addScheduledVisit(Visit visit) {
        scheduledVisits.add(visit);
    }

    public void removeScheduledVisit(Visit visit) {
        scheduledVisits.remove(visit);
    }

}
