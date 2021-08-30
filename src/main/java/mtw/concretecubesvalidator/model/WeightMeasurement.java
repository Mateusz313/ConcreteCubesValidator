package mtw.concretecubesvalidator.model;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class WeightMeasurement {

	private DoubleProperty weightValue;

	public WeightMeasurement(double weightValue) {
		this.weightValue = new SimpleDoubleProperty(weightValue);
	}
	
	public double getWeightValue() {
		return weightValue.get();
	}
	
	public DoubleProperty weightValueProperty() {
		return weightValue;
	}

	@Override
	public String toString() {
		return String.format("%.1f", getWeightValue());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WeightMeasurement other = (WeightMeasurement) obj;
		if (weightValue == null) {
			if (other.weightValue != null)
				return false;
		} else if (!weightValue.equals(other.weightValue))
			return false;
		return true;
	}
}
