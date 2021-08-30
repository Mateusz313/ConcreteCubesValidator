package mtw.concretecubesvalidator.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class GeometricMeasurement {

	private DoubleProperty sensor1Value;
	private DoubleProperty sensor2Value;
	private DoubleProperty sensor3Value;
	private DoubleProperty calculatededValue;

	public GeometricMeasurement(double sensor1Value, double sensor2Value, double sensor3Value) {
		super();
		this.sensor1Value = new SimpleDoubleProperty(sensor1Value);
		this.sensor2Value = new SimpleDoubleProperty(sensor2Value);
		this.sensor3Value = new SimpleDoubleProperty(sensor3Value);
		this.calculatededValue = new SimpleDoubleProperty(0);
		this.calulate();
	}

	public void calulate() {
		calculatededValue.set((sensor1Value.get() + sensor2Value.get() + sensor3Value.get()) / 3.0);
	}

	public double getSensor1Value() {
		return sensor1Value.get();
	}

	public DoubleProperty sensor1ValueProperty() {
		return sensor1Value;
	}

	public double getSensor2Value() {
		return sensor2Value.get();
	}

	public DoubleProperty sensor2ValueProperty() {
		return sensor2Value;
	}

	public double getSensor3Value() {
		return sensor3Value.get();
	}

	public DoubleProperty sensor3ValueProperty() {
		return sensor3Value;
	}

	public double getCalculatededValue() {
		return calculatededValue.get();
	}

	public DoubleProperty calculatededValueProperty() {
		return calculatededValue;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%.2f", calculatededValue.get())).append(";");
		sb.append(String.format("%.2f", sensor1Value.get())).append(";");
		sb.append(String.format("%.2f", sensor2Value.get())).append(";");
		sb.append(String.format("%.2f", sensor3Value.get()));

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((calculatededValue == null) ? 0 : calculatededValue.hashCode());
		result = prime * result + ((sensor1Value == null) ? 0 : sensor1Value.hashCode());
		result = prime * result + ((sensor2Value == null) ? 0 : sensor2Value.hashCode());
		result = prime * result + ((sensor3Value == null) ? 0 : sensor3Value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GeometricMeasurement other = (GeometricMeasurement) obj;

		if (calculatededValue == null) {
			if (other.calculatededValue != null)
				return false;
		} else if (!calculatededValue.equals(other.calculatededValue))
			return false;

		if (sensor1Value == null) {
			if (other.sensor1Value != null)
				return false;
		} else if (!sensor1Value.equals(other.sensor1Value))
			return false;

		if (sensor2Value == null) {
			if (other.sensor2Value != null)
				return false;
		} else if (!sensor2Value.equals(other.sensor2Value))
			return false;

		if (sensor3Value == null) {
			if (other.sensor3Value != null)
				return false;
		} else if (!sensor3Value.equals(other.sensor3Value))
			return false;

		return true;
	}

}
