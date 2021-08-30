package mtw.concretecubesvalidator.model.communication;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public enum CommunicationCommands {
	WAITING_FOR_COMMAND("waiting for command"), 
	CONNECT("connect"), 
	DISCONNECT("disconnect"), 
	READ_ALL_REGISTERS("read all registers"), 
	SINGLE_MEASUREMENT("single measurement"), 
	MULTIPLE_MEASUREMENT("multiple measurement"), 
	CALIBRATION("calibration"),
	RESET_MEASUREMENT_LINE("reset measurement line");
	
	private StringProperty description;

	private CommunicationCommands(String description){
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

