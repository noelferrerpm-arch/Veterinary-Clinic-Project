package cat.tecnocampus.domain;

import cat.tecnocampus.application.dto.AvailabilityDTO;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name="availability")
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long availabilityId;

    private int dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    //Missing relationship: : Veterinarian
    @ManyToOne()
    //@JoinColumn(name = "person_id", referencedColumnName = "personId")
    private Veterinarian veterinarian;

    //Availability can have multiple exceptions
    @OneToMany(
            mappedBy = "availability",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<AvailabilityException> availabilityExceptions = new ArrayList<>();

    public Availability() {
    }

    public Long getAvailabilityId() {
        return availabilityId;
    }

    public void setAvailabilityId(Long availabilityId) {
        this.availabilityId = availabilityId;
    }

    public int getDayOfWeek() { return dayOfWeek; }

    public void setDayOfWeek(int dayOfWeek) { this.dayOfWeek = dayOfWeek; }

    public LocalTime getStartTime() { return startTime; }

    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }

    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public LocalDate getPeriodStart() { return periodStart; }

    public void setPeriodStart(LocalDate periodStart) { this.periodStart = periodStart; }

    public LocalDate getPeriodEnd() { return periodEnd; }

    public void setPeriodEnd(LocalDate periodEnd) { this.periodEnd = periodEnd; }

    public Veterinarian getVeterinarian() {
        return veterinarian;
    }

    public void setVeterinarian(Veterinarian veterinarian) {
        this.veterinarian = veterinarian;
    }

    public List<AvailabilityException> getAvailabilityExceptions() { return availabilityExceptions; }

    public void setAvailabilityExceptions(List<AvailabilityException> availabilityExceptions) {
        this.availabilityExceptions = availabilityExceptions;
    }

    public void addAvailabilityException(AvailabilityException exception) {
        availabilityExceptions.add(exception);
    }

    public AvailabilityException getAvailabilityExceptionById(Long availabilityExcpetionId) {
        return availabilityExceptions.stream()
                .filter(a -> a.getAvailabilityExceptionId().equals(availabilityExcpetionId))
                .findAny().orElseThrow(() -> new NotFoundException("Availability Exception not found"));
    }

    public AvailabilityException removeAvailabilityException(Long availabilityExceptionId) {
        AvailabilityException a = this.getAvailabilityExceptionById(availabilityExceptionId);
        if(a==null){
            throw new NotFoundException("Availability with id "+availabilityExceptionId+" not found");
        }
        this.availabilityExceptions.remove(a);
        return a;
    }

    public void updateAvailability(AvailabilityDTO availability) {
        this.setDayOfWeek(availability.dayOfWeek());
        this.setStartTime(availability.startTime());
        this.setEndTime(availability.endTime());
        this.setPeriodStart(availability.periodStart());
        this.setPeriodEnd(availability.periodEnd());
    }

    public boolean isAvailable(LocalDateTime appointmentStart, LocalDateTime appointmentEnd) {
        if (appointmentStart == null || appointmentEnd == null) return false;

        if (!appointmentEnd.isAfter(appointmentStart)) return false;

        if (!appointmentStart.toLocalDate().equals(appointmentEnd.toLocalDate())) return false;

        int appointmentDow = appointmentStart.getDayOfWeek().getValue();
        if (this.dayOfWeek != appointmentDow) return false;

        LocalDate d = appointmentStart.toLocalDate();
        if (this.periodStart != null && d.isBefore(this.periodStart)) return false;
        if (this.periodEnd != null && d.isAfter(this.periodEnd)) return false;

        if (this.startTime == null || this.endTime == null) return false;


        if (appointmentStart.toLocalTime().isBefore(this.startTime)) return false;
        if (appointmentEnd.toLocalTime().isAfter(this.endTime)) return false;

        if (this.availabilityExceptions != null) {
            for (AvailabilityException ex : this.availabilityExceptions) {
                if (ex.overlaps(appointmentStart, appointmentEnd)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean equals(Availability availability) {
        return this.availabilityId.equals(availability.availabilityId);
    }

}
