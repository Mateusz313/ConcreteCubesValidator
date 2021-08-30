package mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions;

public class StartAddressInvalidException extends ModbusException {

	private static final long serialVersionUID = 7182423624825182152L;

	public StartAddressInvalidException() {
	}

	public StartAddressInvalidException(String s) {
		super(s);
	}
}
