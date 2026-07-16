package cat.tecnocampus.domain;

import cat.tecnocampus.application.dto.AvailabilityDTO;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "veterinarian")
@PrimaryKeyJoinColumn(name = "person_id")
public class Veterinarian extends Person {
    private int licenseNumber;
    private int yearsOfExperience;

    @OneToMany(mappedBy = "veterinarian", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Availability> availabilities = new ArrayList<Availability>();

    @ManyToMany(cascade = CascadeType.ALL)
    private List<Speciality> specialities = new ArrayList<Speciality>();

    public Veterinarian() {
        super();
    }

    public int getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(int licenseNumber) {
        if(licenseNumber<1 ){
            throw new InvalidDataException("License number must be an existing positive number");
        }
        this.licenseNumber = licenseNumber;
    }

    public int getYearsOfExperience() {
        return yearsOfExperience;
    }

    public void setYearsOfExperience(int yearsOfExperience) {
        if(yearsOfExperience<0 ){
            throw new InvalidDataException("Years of Experience must be an existing number over 0");
        }
        this.yearsOfExperience = yearsOfExperience;
    }

    // Metods of availability
    public List<Availability> getAvailability() {
        return availabilities;
    }

    public void setAvailability(List<Availability> availability) {
        this.availabilities = availability;
    }

    public void addAvailability(Availability availability) {
        this.availabilities.add(availability);
    }

    public Availability getAvailabilityById(Long availabilityId) {

        return availabilities.stream()
                .filter(a -> a.getAvailabilityId().equals(availabilityId))
                .findAny().orElseThrow(() -> new NotFoundException("Availability not found"));

    }

    public Availability updateAvailability(Long availabilityId, AvailabilityDTO availabilityDTO) {
        Availability a = this.getAvailabilityById(availabilityId);
        a.updateAvailability(availabilityDTO);
        return a;
    }

    public Availability removeAvailability(Long availabilityId) {
        Availability a = this.getAvailabilityById(availabilityId);
        if(a==null){
            throw new NotFoundException("Availability with id "+availabilityId+" not found");
        }
        this.availabilities.remove(a);
        return a;
    }

    // Metods of specialities
    public List<Speciality> getSpecialities() {
        return specialities;
    }

    public void setSpecialities(List<Speciality> specialities) {
        this.specialities = specialities;
    }

    public void addSpeciality(Speciality speciality) {
        this.specialities.add(speciality);
    }

    public void removeSpeciality(Speciality speciality) {
        this.specialities.remove(speciality);
    }

    public Speciality getSpecialityByName(String name) {
        for (Speciality speciality : specialities) {
            if (speciality.getName().equalsIgnoreCase(name)) {
                return speciality;
            }
        }
        return null;
    }

    public boolean isAvailable(LocalDateTime schedule) {
        if (schedule == null || availabilities == null || availabilities.isEmpty()) {
            return false;
        }

        LocalDateTime appointmentStart = schedule;
        LocalDateTime appointmentEnd = schedule.plusMinutes(15);

        for (Availability a : availabilities) {
            if (a != null && a.isAvailable(appointmentStart, appointmentEnd)) {
                return true;
            }
        }

        return false;
    }
}
