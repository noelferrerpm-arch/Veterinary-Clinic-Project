package cat.tecnocampus.persistence;


import cat.tecnocampus.application.outputDTO.VeterinarianDemandDTO;
import cat.tecnocampus.domain.Status_VPR;

import cat.tecnocampus.domain.MedicationBatch;

import cat.tecnocampus.domain.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {
    @Query("""
      SELECT new cat.tecnocampus.application.outputDTO.VeterinarianDemandDTO(
          v.personId, CONCAT(v.firstName, ' ', v.lastName), COUNT(vis))
      FROM Visit vis
      JOIN vis.veterinarian v
      WHERE vis.startDate BETWEEN :start AND :end
        AND vis.status = :status
      GROUP BY v.personId, v.firstName, v.lastName
      ORDER BY COUNT(vis) DESC
      """)
    List<VeterinarianDemandDTO> findVeterinarianDemandBetween(LocalDateTime start, LocalDateTime end, Status_VPR status);


    @Query("""
            SELECT a
            FROM Visit a
            WHERE a.pet.petId = :id
            """)
    List<Visit> findallVisitsByPet (Long id);
}
