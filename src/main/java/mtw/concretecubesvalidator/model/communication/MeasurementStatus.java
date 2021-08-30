package mtw.concretecubesvalidator.model.communication;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public enum MeasurementStatus {

IDLE ("idle"), 
SINGLE_MEASUREMENT_IN_PROGRESS("Single mesurement in progress"), 
SINGLE_MEASUREMENT_COMPLETED("Single mesurement completed"), 
SINGLE_MEASUREMENT_ERROR("Single mesurement error"), 
MULTIPLE_MEASUREMENT_IN_PROGRESS("Multiple mesurement in progress"), 
MULTIPLE_MEASUREMENT_COMPLETED("Multiple mesurement completed"),  
MULTIPLE_MEASUREMENT_ERROR("Multiple mesurement error"),
RESTART_IN_PROGRESS("Restart in progress"),
RESTART_COMPLETED("Restart completed"),
RESTART_ERROR("Restart error"),
CALIBRATION_IN_PROGRESS("Calibration in progress"),
CALIBRATION_COMPLETED("Calibration completed"),
CALIBRATION_ERROR("Calibration error");	
	
	private StringProperty description;

	private MeasurementStatus(String description){
		this.description = new SimpleStringProperty (description);
	}	
	
	public String getDescription() {
		return description.get(); 
	}
	
	public StringProperty descriptionProperty() {
		return description; 
	}
	
	@Override
	public String toString() {
		return description.get(); 
	}
}