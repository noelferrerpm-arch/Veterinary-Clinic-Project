package cat.tecnocampus.domain.exceptions;

import cat.tecnocampus.domain.Medication;

public class belowThresholdMedicationException extends RuntimeException {
  public belowThresholdMedicationException(Medication medication) {

      super(medication.getName() + " is under the threshold of quantity");
  }
}
