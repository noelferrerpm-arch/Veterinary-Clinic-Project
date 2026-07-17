package cat.tecnocampus.domain;

import cat.tecnocampus.application.dto.AvailabilityExceptionDTO;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name="availabilityException")
public class AvailabilityException {
    @Id
    @GeneratedValue
    private Long availabilityExceptionId;

    String reason;

    @Column(name="dayOfWeek")
    private int dayOfWeek;

    private LocalTime startTime;

    private LocalTime endTime;

    private LocalDate periodStart;

    private LocalDate periodEnd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "availability_id", nullable = false)
    private Availability availability;

    public AvailabilityException() {}
    //Fer DTO eliminar aquest

    public Long getAvailabilityExceptionId() {
        return availabilityExceptionId;
    }

    public void setAvailabilityExceptionId(Long availabilityExceptionId) {
        this.availabilityExceptionId = availabilityExceptionId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public void updateAvailabilityException(AvailabilityExceptionDTO availabilityException) {
        this.setReason(availabilityException.reason());
        this.setDayOfWeek(availabilityException.dayOfWeek());
        this.setStartTime(availabilityException.startTime());
        this.setEndTime(availabilityException.endTime());
        this.setPeriodStart(availabilityException.periodStart());
        this.setPeriodEnd(availabilityException.periodEnd());
    }

    public boolean overlaps(LocalDateTime appointmentStart, LocalDateTime appointmentEnd) {
        if (appointmentStart == null || appointmentEnd == null) return false;

        if (!appointmentEnd.isAfter(appointmentStart)) return false;

        if (!appointmentStart.toLocalDate().equals(appointmentEnd.toLocalDate())) return false;

        int appointmentDow = appointmentStart.getDayOfWeek().getValue();
        if (this.dayOfWeek != appointmentDow) return false;

        LocalDate d = appointmentStart.toLocalDate();
        if (this.periodStart != null && d.isBefore(this.periodStart)) return false;
        if (this.periodEnd != null && d.isAfter(this.periodEnd)) return false;

        if (this.startTime == null || this.endTime == null) return false;

        LocalDateTime exStart = LocalDateTime.of(d, this.startTime);
        LocalDateTime exEnd = LocalDateTime.of(d, this.endTime);

        return appointmentStart.isBefore(exEnd) && appointmentEnd.isAfter(exStart);
    }


    public boolean equals(AvailabilityException availabilityException) {
        return this.availabilityExceptionId.equals(availabilityException.availabilityExceptionId);
    }
}
