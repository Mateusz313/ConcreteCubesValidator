package mtw.concretecubesvalidator.model.communication;

import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import mtw.concretecubesvalidator.model.BarCodeReading;
import mtw.concretecubesvalidator.model.GeometricMeasurement;
import mtw.concretecubesvalidator.model.Measurement;
import mtw.concretecubesvalidator.model.WeightMeasurement;
import mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions.ConnectionException;
import mtw.concretecubesvalidator.model.communication.modbus.modbusExceptions.ModbusException;
import mtw.concretecubesvalidator.model.communication.modbus.modbusTcp.ModbusClient;

public class PlcCommunication extends Thread {

	private ModbusClient modbusClient;
	private short[] registers;
	private ConnectionStatus connectionStatus;
	private CommunicationCommands command;
	private MeasurementStatus measurementStatus;
	private Measurement currentMeasurementReading;
	private Measurement newSingleMeasurementReading;
	private ArrayList<Measurement> newMultipleMeasurementsReading;
	private CommunicationProperties communicationProperties;
	private ObservableList<PlcStatus> plcStatusList;
	private boolean closeCommunication;
	private short multipleMeasurementReadingConfirmation;
	private double[] calibrationValues;
	private boolean plcEmulator;
	private MeasurementStatus previousMeasurementStatusSimulation;
	private int[] simulationStatesChangeDelay;
	static final Logger logger = LoggerFactory.getLogger(PlcCommunication.class);

	public PlcCommunication() {
		modbusClient = null;
		registers = new short[40];
		connectionStatus = ConnectionStatus.DISCONNECTED;
		command = CommunicationCommands.WAITING_FOR_COMMAND;
		measurementStatus = MeasurementStatus.IDLE;
		communicationProperties = new CommunicationProperties();
		newSingleMeasurementReading = new Measurement();
		currentMeasurementReading = new Measurement();
		newMultipleMeasurementsReading = new ArrayList<Measurement>();
		plcStatusList = FXCollections.observableArrayList();
		plcStatusList.add(PlcStatus.EMPTY);
		closeCommunication = false;
		multipleMeasurementReadingConfirmation = 0;
		calibrationValues = new double[6];
		plcEmulator = true;
		previousMeasurementStatusSimulation = MeasurementStatus.IDLE;
		simulationStatesChangeDelay = new int[7];
	}

	@Override
	public void run() {
		while (closeCommunication == false) {

			if (plcEmulator == false) {
				if (modbusClient != null) {
					if (modbusClient.isConnected()) {
						connectionStatus = ConnectionStatus.CONNECTED;
					} else {
						connectionStatus = ConnectionStatus.DISCONNECTED;
					}
				} else {
					connectionStatus = ConnectionStatus.DISCONNECTED;
				}
			} else {
				simulateConnectionStatus();
			}

			switch (command) {

			case WAITING_FOR_COMMAND:
				synchronized (this) {
					if (connectionStatus == ConnectionStatus.CONNECTED) {
						if (plcEmulator == false) {
							readStatusRegistersFromPlc();
							decodeStatusRegisters();
						} else {
							simulateRegistersReading();
							decodeStatusRegisters();
						}
					}
					try {
						sleep(500);
					} catch (InterruptedException e) {
						logger.info("Case: WAITING_FOR_COMMAND - sleep interrupted", e);
					}
					this.notifyAll();
				}
				break;

			case CONNECT:
				synchronized (this) {
					logger.info("Connection attempt");
					if (plcEmulator == false) {
						connectionStatus = ConnectionStatus.CONNECTING;
						modbusClient = new ModbusClient(1, communicationProperties.getServerIpAdress(), communicationProperties.getTcpPort());
						if (communicationProperties.isFixedNetworkInterface()) {
							try {
								modbusClient.connect(communicationProperties.getNetworkInterfaceIpAdress());
							} catch (UnknownHostException e) {
								logger.error("Connecting error", e);
								connectionStatus = ConnectionStatus.CONNECTION_ERROR;
							} catch (IOException e) {
								logger.error("Connecting error", e);
								connectionStatus = ConnectionStatus.CONNECTION_ERROR;
							}
						} else {
							try {
								modbusClient.connect();
							} catch (UnknownHostException e) {
								logger.error("Connecting error", e);
								connectionStatus = ConnectionStatus.CONNECTION_ERROR;
							} catch (IOException e) {
								logger.error("Connecting error", e);
								connectionStatus = ConnectionStatus.CONNECTION_ERROR;
							}
						}
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						if (modbusClient.isConnected() == true) {
							connectionStatus = ConnectionStatus.CONNECTED;
							writeCommandToPlc();
						} else {
							connectionStatus = ConnectionStatus.CONNECTION_ERROR;
						}
					} else {
						while (connectionStatus != ConnectionStatus.CONNECTED && connectionStatus != ConnectionStatus.CONNECTION_ERROR) {
							simulateConnectionStatus();
							try {
								sleep(500);
							} catch (InterruptedException e) {
								logger.info("Case: CONNECT - sleep interrupted", e);
								break;
							}
						}
						command = CommunicationCommands.WAITING_FOR_COMMAND;
					}
					this.notifyAll();
				}
				break;

			case DISCONNECT:
				synchronized (this) {
					logger.info("Disconnection attempt");
					if (plcEmulator == false) {
						connectionStatus = ConnectionStatus.DISCONNECTING;
						try {
							modbusClient.disconnect();
							connectionStatus = ConnectionStatus.DISCONNECTED;
						} catch (IOException e) {
							logger.error("Disconnecting error", e);
							connectionStatus = ConnectionStatus.DISCONNECTION_ERROR;
						}
					} else {
						while (connectionStatus != ConnectionStatus.DISCONNECTED && connectionStatus != ConnectionStatus.DISCONNECTION_ERROR) {
							simulateConnectionStatus();
							try {
								sleep(500);
							} catch (InterruptedException e) {
								logger.info("Case: DISCONNECT - sleep interrupted", e);
								break;
							}
						}
					}
					command = CommunicationCommands.WAITING_FOR_COMMAND;
					this.notifyAll();
				}
				break;

			case READ_ALL_REGISTERS:
				synchronized (this) {
					logger.info("Read all regisers request");
					if (plcEmulator == false) {
						writeCommandToPlc();
						readAllRegistersFromPlc();
						decodeAllRegisters();
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						writeCommandToPlc();
					} else {
						simulateRegistersReading();
						decodeAllRegisters();
						command = CommunicationCommands.WAITING_FOR_COMMAND;
					}

					this.notifyAll();
				}
				break;

			case SINGLE_MEASUREMENT:
				synchronized (this) {
					logger.info("Single measurement request");
					if (plcEmulator == false) {
						writeCommandToPlc();
						while (measurementStatus != MeasurementStatus.SINGLE_MEASUREMENT_COMPLETED && measurementStatus != MeasurementStatus.SINGLE_MEASUREMENT_ERROR) {
							readAllRegistersFromPlc();
							decodeAllRegisters();
							try {
								sleep(500);
							} catch (InterruptedException e) {
								logger.info("Case: SINGLE_MEASUREMENT - sleep interrupted", e);
								command = CommunicationCommands.WAITING_FOR_COMMAND;
								writeCommandToPlc();
								break;
							}
						}
						if (measurementStatus == MeasurementStatus.SINGLE_MEASUREMENT_COMPLETED) {
							newSingleMeasurementReading = currentMeasurementReading;
						}
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						writeCommandToPlc();
					} else {
						while (measurementStatus != MeasurementStatus.SINGLE_MEASUREMENT_COMPLETED && measurementStatus != MeasurementStatus.SINGLE_MEASUREMENT_ERROR) {
							simulateRegistersReading();
							decodeAllRegisters();
							try {
								sleep(500);
							} catch (InterruptedException e) {
								logger.info("Case: SINGLE_MEASUREMENT - sleep interrupted", e);
								command = CommunicationCommands.WAITING_FOR_COMMAND;
								break;
							}
						}
						if (measurementStatus == MeasurementStatus.SINGLE_MEASUREMENT_COMPLETED) {
							newSingleMeasurementReading = currentMeasurementReading;
						}
						command = CommunicationCommands.WAITING_FOR_COMMAND;
					}
					this.notifyAll();
				}
				break;

			case MULTIPLE_MEASUREMENT:
				logger.info("Multiple measurement request");
				if (plcEmulator == false) {
					synchronized (this) {
						writeCommandToPlc();
						this.notifyAll();
					}

					while (measurementStatus != MeasurementStatus.MULTIPLE_MEASUREMENT_COMPLETED && measurementStatus != MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR) {
						try {
							sleep(100);
						} catch (InterruptedException e) {
							logger.info("Case: MULTIPLE_MEASUREMENT - sleep interrupted", e);
							command = CommunicationCommands.WAITING_FOR_COMMAND;
							synchronized (this) {
								writeCommandToPlc();
								this.notifyAll();
							}
							break;
						}
						synchronized (this) {
							readAllRegistersFromPlc();
							decodeAllRegisters();
							if (multipleMeasurementReadingConfirmation == 1) {
								newMultipleMeasurementsReading.add(currentMeasurementReading);
								multipleMeasurementReadingConfirmation = 0;
								writeMultipleMeasurementReadingConfirmationToPlc();
							}
							this.notifyAll();
						}
					}
					synchronized (this) {
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						writeCommandToPlc();
						this.notifyAll();
					}
				} else {
					while (measurementStatus != MeasurementStatus.MULTIPLE_MEASUREMENT_COMPLETED && measurementStatus != MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR) {
						try {
							sleep(100);
						} catch (InterruptedException e) {
							logger.info("Case: MULTIPLE_MEASUREMENT - sleep interrupted", e);
							command = CommunicationCommands.WAITING_FOR_COMMAND;
							break;
						}
						synchronized (this) {
							simulateRegistersReading();
							decodeAllRegisters();
							try {
								sleep(500);
							} catch (InterruptedException e) {
								logger.info("Case: MULTIPLE_MEASUREMENT - sleep interrupted", e);
								command = CommunicationCommands.WAITING_FOR_COMMAND;
								break;
							}
							if (multipleMeasurementReadingConfirmation == 1) {
								newMultipleMeasurementsReading.add(currentMeasurementReading);
								multipleMeasurementReadingConfirmation = 0;
							}
							this.notifyAll();
						}
					}
					synchronized (this) {
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						this.notifyAll();
					}
				}
				break;

			case CALIBRATION:
				logger.info("Calibration request");
				if (plcEmulator == false) {
					synchronized (this) {
						writeCalibrationValuesToPlc();
						writeCommandToPlc();
						while (measurementStatus != MeasurementStatus.CALIBRATION_COMPLETED && measurementStatus != MeasurementStatus.CALIBRATION_ERROR) {
							readStatusRegistersFromPlc();
							decodeAllRegisters();
							try {
								sleep(500);
							} catch (InterruptedException e) {
								logger.info("Case: CALIBRATION - sleep interrupted", e);
								command = CommunicationCommands.WAITING_FOR_COMMAND;
								break;
							}
						}
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						writeCommandToPlc();
						this.notifyAll();
					}
				} else {
					synchronized (this) {
						while (measurementStatus != MeasurementStatus.CALIBRATION_COMPLETED && measurementStatus != MeasurementStatus.CALIBRATION_ERROR) {
							simulateRegistersReading();
							decodeAllRegisters();
							try {
								sleep(500);
							} catch (InterruptedException e) {
								logger.info("Case: CALIBRATION - sleep interrupted", e);
								command = CommunicationCommands.WAITING_FOR_COMMAND;
								break;
							}
						}
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						this.notifyAll();
					}
				}
				break;

			case RESET_MEASUREMENT_LINE:
				logger.info("Reset measurement line request");
				if (plcEmulator == false) {
					synchronized (this) {
						writeCommandToPlc();
						readStatusRegistersFromPlc();
						decodeAllRegisters();
						while (!(measurementStatus == MeasurementStatus.RESTART_COMPLETED || measurementStatus == MeasurementStatus.RESTART_ERROR)) {
							readStatusRegistersFromPlc();
							decodeAllRegisters();
							try {
								sleep(500);
							} catch (InterruptedException e) {
								logger.info("Case: RESET_MEASUREMENT_LINE - sleep interrupted", e);
								command = CommunicationCommands.WAITING_FOR_COMMAND;
								writeCommandToPlc();
								break;
							}
						}
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						writeCommandToPlc();
						this.notifyAll();
					}
				} else {
					synchronized (this) {
						simulateRegistersReading();
						decodeAllRegisters();
						while (!(measurementStatus == MeasurementStatus.RESTART_COMPLETED || measurementStatus == MeasurementStatus.RESTART_ERROR)) {
							simulateRegistersReading();
							decodeAllRegisters();
							try {
								sleep(500);
							} catch (InterruptedException e) {
								logger.info("Case: RESET_MEASUREMENT_LINE - sleep interrupted", e);
								command = CommunicationCommands.WAITING_FOR_COMMAND;
								break;
							}
						}
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						this.notifyAll();
					}
				}
				break;

			}

			try {
				sleep(500);
			} catch (InterruptedException e) {
				logger.info("Main loop - sleep interrupted", e);
				if (plcEmulator == false) {
					if (Thread.interrupted() == true) {
						command = CommunicationCommands.WAITING_FOR_COMMAND;
						writeCommandToPlc();
					}
				} else {
					if (Thread.interrupted() == true) {
						command = CommunicationCommands.WAITING_FOR_COMMAND;
					}
				}
			}
		}
	}

	public void stopAllThreads() {
		this.interrupt();
		closeCommunication = true;
	}

	private void decodeAllRegisters() {
		GeometricMeasurement length = new GeometricMeasurement(((double) registers[0]) / 100.0, ((double) registers[1]) / 100.0, ((double) registers[2]) / 100.0);
		GeometricMeasurement width = new GeometricMeasurement(((double) registers[3]) / 100.0, ((double) registers[4]) / 100.0, ((double) registers[5]) / 100.0);
		GeometricMeasurement height = new GeometricMeasurement(((double) registers[6]) / 100.0, ((double) registers[7]) / 100.0, ((double) registers[8]) / 100.0);
		WeightMeasurement weight = new WeightMeasurement(((double) registers[9]) + (double) registers[10] / 100.0);
		BarCodeReading barcode = new BarCodeReading(String.format("%03d", registers[11]) + String.format("%03d", registers[12]) + String.format("%03d", registers[13]));
		LocalDate sampleDate = LocalDate.of(registers[14], registers[15], registers[16]);
		LocalTime sampleTime = LocalTime.of(registers[17], registers[18], registers[19]);

		previousMeasurementStatusSimulation = measurementStatus;
		measurementStatus = measurementStatusFromRegister(registers[20]);
		plcStatusListFromRegister(registers[21]);
		multipleMeasurementReadingConfirmation = registers[25];

		currentMeasurementReading = new Measurement(barcode, length, width, height, weight, new SimpleObjectProperty<LocalDate>(sampleDate), new SimpleObjectProperty<LocalTime>(sampleTime));
	}

	private void readAllRegistersFromPlc() {
		try {
			modbusClient.readHoldingRegisters(0, registers.length);
		} catch (UnknownHostException e) {
			logger.error("Registers reading error", e);
		} catch (SocketException e) {
			logger.error("Registers reading error", e);
		} catch (ModbusException e) {
			logger.error("Registers reading error", e);
		} catch (IOException e) {
			logger.error("Registers reading error", e);
		}
	}

	private void readStatusRegistersFromPlc() {
		try {
			modbusClient.readHoldingRegisters(20, 2);
		} catch (UnknownHostException e) {
			logger.error("Registers reading error", e);
		} catch (SocketException e) {
			logger.error("Registers reading error", e);
		} catch (ModbusException e) {
			logger.error("Registers reading error", e);
		} catch (IOException e) {
			logger.error("Registers reading error", e);
		}
	}

	private void decodeStatusRegisters() {
		previousMeasurementStatusSimulation = measurementStatus;
		measurementStatus = measurementStatusFromRegister(registers[20]);
		plcStatusListFromRegister(registers[21]);
	}

	private void writeCommandToPlc() {
		registers[30] = (short) command.ordinal();
		try {
			modbusClient.writeHoldingRegisters(30, new short[] { registers[30] });
		} catch (ConnectionException e) {
			logger.error("Command register writing error", e);
		} catch (IOException e) {
			logger.error("Command register writing error", e);
		} catch (ModbusException e) {
			logger.error("Command register writing error", e);
		}
	}

	private void writeMultipleMeasurementReadingConfirmationToPlc() {
		registers[25] = multipleMeasurementReadingConfirmation;
		try {
			modbusClient.writeHoldingRegisters(30, new short[] { registers[25] });
		} catch (ConnectionException e) {
			logger.error("Command register writing error", e);
		} catch (IOException e) {
			logger.error("Command register writing error", e);
		} catch (ModbusException e) {
			logger.error("Command register writing error", e);
		}
	}

	private void writeCalibrationValuesToPlc() {
		registers[31] = (short) (calibrationValues[0] * 100.0);
		registers[32] = (short) (calibrationValues[1] * 100.0);
		registers[33] = (short) (calibrationValues[2] * 100.0);
		registers[34] = (short) (calibrationValues[3] * 100.0);
		registers[35] = (short) (calibrationValues[4] * 100.0);
		registers[36] = (short) (calibrationValues[5] * 100.0);
		try {
			modbusClient.writeHoldingRegisters(31, new short[] { registers[31], registers[32], registers[33], registers[34], registers[35], registers[36] });
		} catch (ConnectionException e) {
			logger.error("Command register writing error", e);
		} catch (IOException e) {
			logger.error("Command register writing error", e);
		} catch (ModbusException e) {
			logger.error("Command register writing error", e);
		}
	}

	private void simulateRegistersReading() {
		short[] barCodeValueRegs = barCodeValueSimulation();
		short[] sampleDateTimeRegs = sampleDateTimeSimulation();

		registers[0] = sensorValueSimulation(10000, 1.0);
		registers[1] = sensorValueSimulation(10000, 1.0);
		registers[2] = sensorValueSimulation(10000, 1.0);
		registers[3] = sensorValueSimulation(15000, 1.0);
		registers[4] = sensorValueSimulation(15000, 1.0);
		registers[5] = sensorValueSimulation(15000, 1.0);
		registers[6] = sensorValueSimulation(20000, 1.0);
		registers[7] = sensorValueSimulation(20000, 1.0);
		registers[8] = sensorValueSimulation(20000, 1.0);
		registers[9] = sensorValueSimulation(8500, 1.0);
		registers[10] = sensorValueSimulation(50, 99.0);
		registers[11] = barCodeValueRegs[0];
		registers[12] = barCodeValueRegs[1];
		registers[13] = barCodeValueRegs[2];
		registers[14] = sampleDateTimeRegs[0];
		registers[15] = sampleDateTimeRegs[1];
		registers[16] = sampleDateTimeRegs[2];
		registers[17] = sampleDateTimeRegs[3];
		registers[18] = sampleDateTimeRegs[4];
		registers[19] = sampleDateTimeRegs[5];
		registers[20] = measurementStatusSimulation();
		registers[21] = plcStatusSimulation();
		registers[25] = multipleMeasurementReadingConfirmationSimulation();
	}

	private short sensorValueSimulation(int value, double var) {
		Random rnd = new Random();
		short sensorValue = (short) Math.round(value + (value * var / 100.0 * rnd.nextDouble()) * Math.pow(-1, rnd.nextInt()));

		return sensorValue;
	}

	private short[] barCodeValueSimulation() {
		Random rnd = new Random();
		short[] barCodeValue = new short[3];
		barCodeValue[0] = (short) Math.round(rnd.nextInt(999));
		barCodeValue[1] = (short) Math.round(rnd.nextInt(999));
		barCodeValue[2] = (short) Math.round(rnd.nextInt(999));

		return barCodeValue;
	}

	private short[] sampleDateTimeSimulation() {
		Random rnd = new Random();
		short[] sampleDateTime = new short[6];
		sampleDateTime[0] = (short) (2020 + rnd.nextInt(10));
		sampleDateTime[1] = (short) (1 + rnd.nextInt(11));
		sampleDateTime[2] = (short) (1 + rnd.nextInt(27));
		sampleDateTime[3] = (short) (rnd.nextInt(24));
		sampleDateTime[4] = (short) (rnd.nextInt(60));
		sampleDateTime[5] = (short) (rnd.nextInt(60));

		return sampleDateTime;
	}

	private short measurementStatusSimulation() {
		boolean los = losBoolean();
		short result = (short) measurementStatus.ordinal();

		if (measurementStatus == MeasurementStatus.IDLE && command == CommunicationCommands.SINGLE_MEASUREMENT)
			result = 1;
		else if (measurementStatus == MeasurementStatus.SINGLE_MEASUREMENT_IN_PROGRESS && command == CommunicationCommands.SINGLE_MEASUREMENT && simulationStatesChangeDelay(0, 5)) {
			if (los == true)
				result = 2;
			else
				result = 3;
		} else if (measurementStatus == MeasurementStatus.SINGLE_MEASUREMENT_COMPLETED && command == CommunicationCommands.WAITING_FOR_COMMAND)
			result = 310;
		else if (measurementStatus == MeasurementStatus.IDLE && command == CommunicationCommands.MULTIPLE_MEASUREMENT)
			result = 4;
		else if (measurementStatus == MeasurementStatus.MULTIPLE_MEASUREMENT_IN_PROGRESS && command == CommunicationCommands.MULTIPLE_MEASUREMENT && simulationStatesChangeDelay(1, 30)
				&& multipleMeasurementReadingConfirmation == 0) {
			if (los == true)
				result = 5;
			else
				result = 6;
		} else if (measurementStatus == MeasurementStatus.MULTIPLE_MEASUREMENT_COMPLETED && command == CommunicationCommands.WAITING_FOR_COMMAND)
			result = 410;
		if (measurementStatus == MeasurementStatus.IDLE && command == CommunicationCommands.CALIBRATION)
			result = 10;
		else if (measurementStatus == MeasurementStatus.CALIBRATION_IN_PROGRESS && command == CommunicationCommands.CALIBRATION && simulationStatesChangeDelay(6, 5)) {
			if (los == true)
				result = 11;
			else
				result = 12;
		} else if (measurementStatus == MeasurementStatus.CALIBRATION_COMPLETED && command == CommunicationCommands.WAITING_FOR_COMMAND)
			result = 510;
		else if ((measurementStatus == MeasurementStatus.SINGLE_MEASUREMENT_ERROR || measurementStatus == MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR
				|| measurementStatus == MeasurementStatus.RESTART_ERROR || measurementStatus == MeasurementStatus.CALIBRATION_ERROR) && command == CommunicationCommands.RESET_MEASUREMENT_LINE)
			result = 7;
		else if (measurementStatus == MeasurementStatus.RESTART_IN_PROGRESS && command == CommunicationCommands.RESET_MEASUREMENT_LINE && simulationStatesChangeDelay(2, 5)) {
			if (los == true)
				result = 8;
			else
				result = 9;
		} else if (measurementStatus == MeasurementStatus.RESTART_COMPLETED && command == CommunicationCommands.WAITING_FOR_COMMAND)
			result = 610;
		else if (measurementStatus == MeasurementStatus.RESTART_ERROR && command == CommunicationCommands.RESET_MEASUREMENT_LINE)
			result = 7;
		else if (measurementStatus == MeasurementStatus.SINGLE_MEASUREMENT_IN_PROGRESS && command == CommunicationCommands.WAITING_FOR_COMMAND)
			result = 3;
		else if (measurementStatus == MeasurementStatus.MULTIPLE_MEASUREMENT_IN_PROGRESS && command == CommunicationCommands.WAITING_FOR_COMMAND)
			result = 6;
		else if (measurementStatus == MeasurementStatus.CALIBRATION_IN_PROGRESS && command == CommunicationCommands.WAITING_FOR_COMMAND)
			result = 12;
		else if (measurementStatus == MeasurementStatus.RESTART_IN_PROGRESS && command == CommunicationCommands.WAITING_FOR_COMMAND)
			result = 9;

		return result;
	}

	private short plcStatusSimulation() {
		short result;
		if ((measurementStatus == MeasurementStatus.SINGLE_MEASUREMENT_ERROR && previousMeasurementStatusSimulation == MeasurementStatus.SINGLE_MEASUREMENT_IN_PROGRESS)
				|| (measurementStatus == MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR && previousMeasurementStatusSimulation == MeasurementStatus.MULTIPLE_MEASUREMENT_IN_PROGRESS)
				|| (measurementStatus == MeasurementStatus.CALIBRATION_ERROR && previousMeasurementStatusSimulation == MeasurementStatus.CALIBRATION_IN_PROGRESS)
				|| (measurementStatus == MeasurementStatus.RESTART_ERROR && previousMeasurementStatusSimulation == MeasurementStatus.RESTART_IN_PROGRESS)) {
			plcStatusList.remove(PlcStatus.NO_PENDING_ALARMS);
			result = 8;
		} else if (plcStatusList.size() == 2 && plcStatusList.contains(PlcStatus.PNEUMATIC_SUPPLY_LOSS)) {
			result = registers[15];
		} else {
			result = 1;
		}
		return result;
	}

	private short multipleMeasurementReadingConfirmationSimulation() {
		short result = 0;
		if (multipleMeasurementReadingConfirmation == 0 && simulationStatesChangeDelay(5, 10)) {
			result = 1;
		} else
			result = multipleMeasurementReadingConfirmation;
		return result;
	}

	private void simulateConnectionStatus() {
		ConnectionStatus result;
		boolean los = losBoolean();

		if ((connectionStatus == ConnectionStatus.DISCONNECTED || connectionStatus == ConnectionStatus.CONNECTION_ERROR) && command == CommunicationCommands.CONNECT)
			result = ConnectionStatus.CONNECTING;
		else if (connectionStatus == ConnectionStatus.CONNECTING && command == CommunicationCommands.CONNECT && simulationStatesChangeDelay(3, 5)) {
			if (los == true)
				result = ConnectionStatus.CONNECTED;
			else
				result = ConnectionStatus.CONNECTION_ERROR;
		} else if ((connectionStatus == ConnectionStatus.CONNECTION_ERROR || connectionStatus == ConnectionStatus.CONNECTED || connectionStatus == ConnectionStatus.DISCONNECTION_ERROR)
				&& command == CommunicationCommands.DISCONNECT)
			result = ConnectionStatus.DISCONNECTING;
		else if (connectionStatus == ConnectionStatus.DISCONNECTING && command == CommunicationCommands.DISCONNECT && simulationStatesChangeDelay(4, 5)) {
			if (los == true)
				result = ConnectionStatus.DISCONNECTED;
			else
				result = ConnectionStatus.DISCONNECTION_ERROR;
		} else
			result = connectionStatus;

		connectionStatus = result;
	}

	private boolean simulationStatesChangeDelay(int countHolder, int delay) {
		boolean result = false;
		if (simulationStatesChangeDelay[countHolder] == delay) {
			simulationStatesChangeDelay[countHolder] = 0;
			result = true;
		} else
			simulationStatesChangeDelay[countHolder]++;

		return result;
	}

	private MeasurementStatus measurementStatusFromRegister(short registerValue) {
		if (registerValue == 1)
			return MeasurementStatus.SINGLE_MEASUREMENT_IN_PROGRESS;
		else if (registerValue == 2)
			return MeasurementStatus.SINGLE_MEASUREMENT_COMPLETED;
		else if (registerValue == 3)
			return MeasurementStatus.SINGLE_MEASUREMENT_ERROR;
		else if (registerValue == 4)
			return MeasurementStatus.MULTIPLE_MEASUREMENT_IN_PROGRESS;
		else if (registerValue == 5)
			return MeasurementStatus.MULTIPLE_MEASUREMENT_COMPLETED;
		else if (registerValue == 6)
			return MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR;
		else if (registerValue == 7)
			return MeasurementStatus.RESTART_IN_PROGRESS;
		else if (registerValue == 8)
			return MeasurementStatus.RESTART_COMPLETED;
		else if (registerValue == 9)
			return MeasurementStatus.RESTART_ERROR;
		if (registerValue == 10)
			return MeasurementStatus.CALIBRATION_IN_PROGRESS;
		else if (registerValue == 11)
			return MeasurementStatus.CALIBRATION_COMPLETED;
		else if (registerValue == 12)
			return MeasurementStatus.CALIBRATION_ERROR;
		else
			return MeasurementStatus.IDLE;
	}

	private void plcStatusListFromRegister(short registerValue) {
		int[] intArray = new int[PlcStatus.getStatusQuantity()];
		PlcStatus[] errors = PlcStatus.values();
		for (int i = 0; i < PlcStatus.getStatusQuantity(); i++) {
			intArray[i] = (registerValue & (short) Math.pow(2, i)) / (short) Math.pow(2, i);
			if (intArray[i] == 1 && !plcStatusList.contains(errors[i + 1])) {
				errors[i + 1].setOccuranceDate(LocalDate.now());
				errors[i + 1].setOccuranceTime(LocalTime.now());
				plcStatusList.add(errors[i + 1]);
			}
		}
	}

	private boolean losBoolean() {
		Random rnd = new Random();
		return rnd.nextBoolean();
	}

	public CommunicationCommands getCommand() {
		return command;
	}

	public void setCommand(CommunicationCommands command) {
		this.command = command;
	}

	public Measurement getCurrentMeasurementReading() {
		return currentMeasurementReading;
	}

	public Measurement getNewSingleMeasurementReading() {
		return newSingleMeasurementReading;
	}

	public ConnectionStatus getConnectionStatus() {
		return connectionStatus;
	}

	public CommunicationProperties getCommunicationProperties() {
		return communicationProperties;
	}

	public MeasurementStatus getMeasurementStatus() {
		return measurementStatus;
	}

	public ObservableList<PlcStatus> getPlcStatusList() {
		return plcStatusList;
	}

	public ArrayList<Measurement> getNewMultipleMeasurementsReading() {
		return newMultipleMeasurementsReading;
	}

	public void setCalibrationValues(double[] calibrationValues) {
		this.calibrationValues = calibrationValues;
	}

	public boolean isPlcEmulator() {
		return plcEmulator;
	}

	public void setPlcEmulator(boolean plcEmulator) {
		this.plcEmulator = plcEmulator;
		logger.info("PLC Emulator was switched to: {}", plcEmulator);
	}

}
