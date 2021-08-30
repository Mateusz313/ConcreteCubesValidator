package mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions;

public class ConnectionException extends ModbusException {

	private static final long serialVersionUID = -3505381119048895043L;

	public ConnectionException() {
	}

	public ConnectionException(String s) {
		super(s);
	}
}