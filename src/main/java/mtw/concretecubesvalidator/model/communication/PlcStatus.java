package mtw.concretecubesvalidator.model.communication;

import java.time.LocalDate;
import java.time.LocalTime;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import mtw.concretecubesvalidator.util.DateTimeUtil;

public enum PlcStatus {

	EMPTY("system initialization", 0), 
	NO_PENDING_ALARMS("no pending alarms", 1), 
	CALIBRATION_NEEDED("system calibration is needed", 2),
	POWER_SUPPLY_LOSS("power supply loss", 3), 
	PNEUMATIC_SUPPLY_LOSS("pneumatic supply loss", 3),
	CUBE_PRESENCE_SENSOR1("no cube on 1st conveyor", 3);

	private StringProperty description;
	private IntegerProperty priority;
	private ObjectProperty<LocalDate> occuranceDate;
	private ObjectProperty<LocalTime> occuranceTime;
	private static int statusQuantity = 6;

	private PlcStatus(String description, int priority) {
		this.description = new SimpleStringProperty(description);
		this.priority = new SimpleIntegerProperty(priority);
		this.occuranceDate = new SimpleObjectProperty<LocalDate>(LocalDate.now());
		this.occuranceTime = new SimpleObjectProperty<LocalTime>(LocalTime.now());
	}

	public String getDesription() {
		return description.get();
	}

	public StringProperty descriptionProperty() {
		return description;
	}

	public static int getStatusQuantity() {
		return statusQuantity;
	}

	public LocalDate getOccuranceDate() {
		return occuranceDate.get();
	}
	
	public void setOccuranceDate(LocalDate occuranceDate) {
		this.occuranceDate.set(occuranceDate);
	}

	public ObjectProperty<LocalDate> occuranceDateProperty() {
		return occuranceDate;
	}

	public LocalTime getOccuranceTime() {
		return occuranceTime.get();
	}
	
	public void setOccuranceTime(LocalTime occuranceTime) {
		this.occuranceTime.set(occuranceTime);
	}

	public ObjectProperty<LocalTime> occuranceTimeProperty() {
		return occuranceTime;
	}

	public int getPriority() {
		return priority.get();
	}

	public IntegerProperty priorityProperty() {
		return priority;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(priority.get()).append("; ");
		sb.append(DateTimeUtil.dateFormat(occuranceDate.get())).append("; ");
		sb.append(DateTimeUtil.timeFormat(occuranceTime.get())).append("; ");
		sb.append(description.get()).append("; ");

		return sb.toString();
	}
}
