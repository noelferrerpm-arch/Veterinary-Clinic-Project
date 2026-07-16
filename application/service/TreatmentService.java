package cat.tecnocampus.application.service;

import cat.tecnocampus.application.dto.TreatmentPrescriptionDTO;
import cat.tecnocampus.application.mapper.MapperHelper;
import cat.tecnocampus.domain.Status_VPR;
import cat.tecnocampus.domain.Treatment;
import cat.tecnocampus.domain.Visit;
import cat.tecnocampus.domain.exceptions.InvalidDataException;
import cat.tecnocampus.persistence.VisitRepository;
import jakarta.transaction.Transactional;
import cat.tecnocampus.domain.exceptions.NotFoundException;
import org.springframework.stereotype.Service;
import cat.tecnocampus.persistence.TreatmentRepository;

@Service
@Transactional
public class TreatmentService {
    private final TreatmentRepository treatmentRepository;
    private final VisitRepository visitRepository;

    public TreatmentService(TreatmentRepository treatmentRepository,
                            VisitRepository visitRepository) {
        this.treatmentRepository = treatmentRepository;
        this.visitRepository = visitRepository;
    }

    public TreatmentPrescriptionDTO createTreatment(Long visitId, TreatmentPrescriptionDTO dto) {
        Visit v=visitRepository.findById(visitId).orElseThrow(NotFoundException::new);
        if(v.getStatus()!= Status_VPR.In_Progress&&v.getStatus()!=Status_VPR.Completed){
            throw new InvalidDataException("No Visit in progress or completed");
        }
        Treatment treatment = new Treatment();
        treatment.setName(dto.name());
        treatment.setDescription(dto.description());
        treatment.setCost(dto.cost());
        treatment.addVisit(v);


        treatmentRepository.save(treatment);
        return MapperHelper.mapTreatmentPrescriptionDTO(treatment);
    }
}

