package mtw.concretecubesvalidator.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class BarCodeReading {

	private StringProperty barCode;

	public BarCodeReading(String barCode) {
		this.barCode = new SimpleStringProperty(barCode);
	}

	public String getBarCode() {
		return barCode.get();
	}

	public StringProperty barCodeProperty() {
		return barCode;
	}

	@Override
	public String toString() {
		return barCode.get();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BarCodeReading other = (BarCodeReading) obj;
		if (barCode == null) {
			if (other.barCode != null)
				return false;
		} else if (!barCode.equals(other.barCode))
			return false;
		return true;
	}
}
