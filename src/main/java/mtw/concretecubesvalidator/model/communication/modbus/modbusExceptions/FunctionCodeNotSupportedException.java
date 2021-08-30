package mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions;

public class FunctionCodeNotSupportedException extends ModbusException {

	private static final long serialVersionUID = -4170009460375134470L;

	public FunctionCodeNotSupportedException() {
	}

	public FunctionCodeNotSupportedException(String s) {
		super(s);
	}
}
