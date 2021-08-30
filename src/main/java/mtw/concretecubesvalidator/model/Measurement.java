package mtw.concretecubesvalidator.model;

import java.time.LocalDate;
import java.time.LocalTime;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import mtw.concretecubesvalidator.util.DateTimeUtil;

public final class Measurement {

	private BarCodeReading barCode;
	private GeometricMeasurement lenght;
	private GeometricMeasurement width;
	private GeometricMeasurement height;
	private WeightMeasurement weight;
	private ObjectProperty<LocalDate> sampleDate;
	private ObjectProperty<LocalTime> sampleTime;

	public Measurement(BarCodeReading barCode, GeometricMeasurement lenght, GeometricMeasurement width,
			GeometricMeasurement height, WeightMeasurement weight, ObjectProperty<LocalDate> sampleDate,
			ObjectProperty<LocalTime> sampleTime) {

		this.barCode = barCode;
		this.lenght = lenght;
		this.width = width;
		this.height = height;
		this.weight = weight;
		this.sampleDate = sampleDate;
		this.sampleTime = sampleTime;

	}

	public Measurement() {

		this.barCode = new BarCodeReading("0");
		this.lenght = new GeometricMeasurement(0.0, 0.0, 0.0);
		this.width = new GeometricMeasurement(0.0, 0.0, 0.0);
		this.height = new GeometricMeasurement(0.0, 0.0, 0.0);
		this.weight = new WeightMeasurement(0.0);
		this.sampleDate = new SimpleObjectProperty<LocalDate>(LocalDate.now());
		this.sampleTime = new SimpleObjectProperty<LocalTime>(LocalTime.now());

	}

	public BarCodeReading getBarCode() {
		return barCode;
	}

	public GeometricMeasurement getLenght() {
		return lenght;
	}

	public GeometricMeasurement getWidth() {
		return width;
	}

	public GeometricMeasurement getHeight() {
		return height;
	}

	public WeightMeasurement getWeight() {
		return weight;
	}

	public LocalDate getSampleDate() {
		return sampleDate.get();
	}

	public ObjectProperty<LocalDate> sampleDateProperty() {
		return sampleDate;
	}

	public LocalTime getSampleTime() {
		return sampleTime.get();
	}

	public ObjectProperty<LocalTime> sampleTimeProperty() {
		return sampleTime;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(barCode).append(";");
		sb.append(DateTimeUtil.dateFormat(sampleDate.get())).append(";");
		sb.append(DateTimeUtil.timeFormat(sampleTime.get())).append(";");
		sb.append(weight).append(";");	
		sb.append(lenght).append(";");
		sb.append(width).append(";");
		sb.append(height).append(";");
	
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		Measurement other = (Measurement) obj;

		if (barCode == null) {
			if (other.barCode != null)
				return false;
		} else if (!barCode.equals(other.barCode))
			return false;

		if (lenght == null) {
			if (other.lenght != null)
				return false;
		} else if (!lenght.equals(other.lenght))
			return false;

		if (width == null) {
			if (other.width != null)
				return false;
		} else if (!width.equals(other.width))
			return false;

		if (height == null) {
			if (other.height != null)
				return false;
		} else if (!height.equals(other.height))
			return false;

		if (weight == null) {
			if (other.weight != null)
				return false;
		} else if (!weight.equals(other.weight))
			return false;

		if (sampleDate == null) {
			if (other.sampleDate != null)
				return false;
		} else if (!sampleDate.equals(other.sampleDate))
			return false;

		if (sampleTime == null) {
			if (other.sampleTime != null)
				return false;
		} else if (!sampleTime.equals(other.sampleTime))
			return false;

		return true;
	}

}
