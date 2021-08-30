package mtw.concretecubesvalidator.model.communication.modbus.modbusTcp;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

import mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions.ConnectionException;
import mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions.FunctionCodeNotSupportedException;
import mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions.ModbusException;
import mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions.QuantityInvalidException;
import mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions.StartAddressInvalidException;

public class ModbusClient {
	private String ipAddress;
	private int port;
	private Socket clientSocket;
	private int connectTimeout;
	private DataOutputStream outputStream;
	private InputStream inputStream;
	private byte[] transactionIdentifier;
	private byte[] protocolIdentifier;
	private byte[] length;
	private byte unitIdentifier;
	private byte functionCode;
	private byte[] startAddress;
	private byte[] quantity;
	private byte byteCount;
	private byte[] sendFrame;
	private byte[] receivedFrame;
	private short[] sendRegistersValues;
	private short[] receivedRegistersValues;

	public ModbusClient(int unitIdentifier, String ipAddress, int port) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.clientSocket = null;
		this.connectTimeout = 500;
		this.outputStream = null;
		this.inputStream = null;
		this.transactionIdentifier = new byte[2];
		this.protocolIdentifier = new byte[2];
		this.length = new byte[2];
		this.unitIdentifier = (byte) unitIdentifier;
		this.functionCode = 0;
		this.startAddress = new byte[2];
		this.quantity = new byte[2];
		this.byteCount = 0;
		this.sendFrame = null;
		this.receivedFrame = null;
		this.sendRegistersValues = null;
		this.receivedRegistersValues = null;
	}

	public void connect() throws UnknownHostException, IOException {
		clientSocket = new Socket(ipAddress, port);
		clientSocket.setSoTimeout(connectTimeout);
		outputStream = new DataOutputStream(clientSocket.getOutputStream());
		inputStream = clientSocket.getInputStream();
	}

	public void connect(String networkInterfaceIpAddress) throws UnknownHostException, IOException {
		clientSocket = new Socket();
		clientSocket.bind(new InetSocketAddress(networkInterfaceIpAddress, 0));
		clientSocket.connect(new InetSocketAddress(ipAddress, port));
		clientSocket.setSoTimeout(connectTimeout);
		outputStream = new DataOutputStream(clientSocket.getOutputStream());
		inputStream = clientSocket.getInputStream();
	}

	public void disconnect() throws IOException {
		if (inputStream != null) {
			inputStream.close();
		}
		if (outputStream != null) {
			outputStream.close();
		}
		if (clientSocket != null) {
			clientSocket.close();
		}
		clientSocket = null;
	}

	public short[] readHoldingRegisters(int firstRegisterAddress, int registersQuantity) throws ConnectionException, IOException, ModbusException {
		if (this.clientSocket == null) {
			throw new ConnectionException("connection Error");
		} else if (firstRegisterAddress > 65535 || registersQuantity > 125) {
			throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 125");
		} else {
			receivedRegistersValues = new short[registersQuantity];
			transactionIdentifier = toByteArray(1);
			protocolIdentifier = toByteArray(0);
			length = toByteArray(6);
			functionCode = 3;
			startAddress = toByteArray(firstRegisterAddress);
			quantity = toByteArray(registersQuantity);

			sendFrame = new byte[12];

			sendFrame[0] = transactionIdentifier[1];
			sendFrame[1] = transactionIdentifier[0];
			sendFrame[2] = protocolIdentifier[1];
			sendFrame[3] = protocolIdentifier[0];
			sendFrame[4] = length[1];
			sendFrame[5] = length[0];
			sendFrame[6] = unitIdentifier;
			sendFrame[7] = functionCode;
			sendFrame[8] = startAddress[1];
			sendFrame[9] = startAddress[0];
			sendFrame[10] = quantity[1];
			sendFrame[11] = quantity[0];

			if (clientSocket.isConnected()) {
				outputStream.write(sendFrame);

				receivedFrame = new byte[1000];
				inputStream.read(receivedFrame, 0, receivedFrame.length);
			}

			if (receivedFrame[7] == 131 & receivedFrame[8] == 1) {
				throw new FunctionCodeNotSupportedException("Function code not supported by master");
			} else if (receivedFrame[7] == 131 & receivedFrame[8] == 2) {
				throw new StartAddressInvalidException("Starting adress invalid or starting adress and quantity invalid");
			} else if (receivedFrame[7] == 131 & receivedFrame[8] == 3) {
				throw new QuantityInvalidException("Quantity invalid");
			} else if (receivedFrame[7] == 131 & receivedFrame[8] == 4) {
				throw new ModbusException("Slave error");
			} else {
				for (int j = 0; j < registersQuantity; j++) {
					byte[] bytes = new byte[] { receivedFrame[9 + j * 2], receivedFrame[9 + j * 2 + 1] };
					ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
					receivedRegistersValues[j] = byteBuffer.getShort();
				}
				return receivedRegistersValues;
			}
		}
	}

	public void writeHoldingRegisters(int firstRegisterAddress, short[] registersValues) throws ConnectionException, IOException, ModbusException{
		if (this.clientSocket == null) {
			throw new ConnectionException("connection Error");
		} else if (firstRegisterAddress > 65535 || registersValues.length > 125) {
			throw new IllegalArgumentException("Starting adress must be 0 - 65535; quantity must be 0 - 125");
		} else {
			sendRegistersValues = registersValues;
			byteCount = (byte) (sendRegistersValues.length * 2);
			quantity = toByteArray(sendRegistersValues.length);
			transactionIdentifier = toByteArray(1);
			protocolIdentifier = toByteArray(0);
			length = toByteArray(7 + sendRegistersValues.length * 2);
			functionCode = 16;
			startAddress = toByteArray(firstRegisterAddress);

			sendFrame = new byte[13 + sendRegistersValues.length * 2];

			sendFrame[0] = transactionIdentifier[1];
			sendFrame[1] = transactionIdentifier[0];
			sendFrame[2] = protocolIdentifier[1];
			sendFrame[3] = protocolIdentifier[0];
			sendFrame[4] = length[1];
			sendFrame[5] = length[0];
			sendFrame[6] = unitIdentifier;
			sendFrame[7] = functionCode;
			sendFrame[8] = startAddress[1];
			sendFrame[9] = startAddress[0];
			sendFrame[10] = quantity[1];
			sendFrame[11] = quantity[0];
			sendFrame[12] = byteCount;

			for (int i = 0; i < sendRegistersValues.length; i++) {
				byte[] singleRegisterValue = toByteArray(sendRegistersValues[i]);
				sendFrame[13 + i * 2] = singleRegisterValue[1];
				sendFrame[14 + i * 2] = singleRegisterValue[0];
			}

			if (clientSocket.isConnected()) {
				outputStream.write(sendFrame, 0, sendFrame.length);

				receivedFrame = new byte[1000];
				inputStream.read(receivedFrame, 0, receivedFrame.length);
			}

			if ((receivedFrame[7] & 255) == 144 & receivedFrame[8] == 1) {
				throw new FunctionCodeNotSupportedException("Function code not supported by master");
			} else if ((receivedFrame[7] & 255) == 144 & receivedFrame[8] == 2) {
				throw new StartAddressInvalidException("Starting address invalid or starting address and quantity invalid");
			} else if ((receivedFrame[7] & 255) == 144 & receivedFrame[8] == 3) {
				throw new QuantityInvalidException("quantity invalid");
			} else if ((receivedFrame[7] & 255) == 144 & receivedFrame[8] == 4) {
				throw new ModbusException("error reading");
			}
		}
	}

	public boolean isConnected() {
		boolean returnValue = false;
		if (clientSocket == null) {
			returnValue = false;
		} else if (clientSocket.isConnected()) {
			returnValue = true;
		} else {
			returnValue = false;
		}
		return returnValue;
	}

	private static byte[] toByteArray(int value) {
		byte[] result = new byte[] { (byte) value, (byte) (value >> 8) };
		return result;
	}
}
