package mtw.concretecubesvalidator.model.communication;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public enum ConnectionStatus {
	DISCONNECTING("disconnecting"), 
	DISCONNECTED("disconnected"), 
	DISCONNECTION_ERROR("disconnecting error"), 
	CONNECTING("connecting"), 
	CONNECTED("connected"), 
	CONNECTION_ERROR("connecting error");

	private StringProperty description;

	private ConnectionStatus(String description) {
		this.description = new SimpleStringProperty(description);
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