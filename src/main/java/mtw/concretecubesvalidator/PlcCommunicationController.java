package mtw.concretecubesvalidator;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import mtw.concretecubesvalidator.model.MeasurementData;
import mtw.concretecubesvalidator.model.communication.CommunicationCommands;
import mtw.concretecubesvalidator.model.communication.CommunicationProperties;
import mtw.concretecubesvalidator.model.communication.ConnectionStatus;
import mtw.concretecubesvalidator.model.communication.MeasurementStatus;
import mtw.concretecubesvalidator.model.communication.PlcCommunication;
import mtw.concretecubesvalidator.model.communication.PlcStatus;

public class PlcCommunicationController {

	private PlcCommunication plcCom;
	private MainApp mainApp;
	private MeasurementData measurementData;
	private Thread connectToPlcControllerThread;
	private Thread disconnectFromPlcControllerThread;
	private Thread newSingleMeasurementThread;
	private Thread measurementSerieThread;
	private Thread calibrationThread;
	private Thread restartMeasurementLineThread;
	static final Logger logger = LoggerFactory.getLogger(PlcCommunication.class);

	PlcCommunicationController() {
		plcCom = new PlcCommunication();
		mainApp = null;
		measurementData = null;
		connectToPlcControllerThread = null;
		disconnectFromPlcControllerThread = null;
		newSingleMeasurementThread = null;
		measurementSerieThread = null;
		calibrationThread = null;
		restartMeasurementLineThread = null;

		plcCom.setName("PLC Communication Thread");
		plcCom.start();
	}

	public void initMeasurementData(MeasurementData measurementData) {
		if (this.measurementData == null)
			this.measurementData = measurementData;
		else
			throw new IllegalStateException("Measurement data can only be initialized once");
	}

	public void initMainApp(MainApp mainApp) {
		if (this.mainApp == null)
			this.mainApp = mainApp;
		else
			throw new IllegalStateException("Controller can only be initialized once");
	}

	public void stopAllThreads() {
		logger.info("Stoppin all Threads");
		if (connectToPlcControllerThread != null)
			connectToPlcControllerThread.interrupt();
		if (disconnectFromPlcControllerThread != null)
			disconnectFromPlcControllerThread.interrupt();
		if (newSingleMeasurementThread != null)
			newSingleMeasurementThread.interrupt();
		if (measurementSerieThread != null)
			measurementSerieThread.interrupt();
		if (calibrationThread != null)
			calibrationThread.interrupt();
		if (restartMeasurementLineThread != null)
			restartMeasurementLineThread.interrupt();

		plcCom.stopAllThreads();
	}

	public void connectToPlcController() {
		if (plcCom.getConnectionStatus() == ConnectionStatus.CONNECTED)
			mainApp.showAlert(AlertType.INFORMATION, "Connection Status", "Communication with PLC is already established", "");
		else if (plcCom.getConnectionStatus() == ConnectionStatus.CONNECTING)
			mainApp.showAlert(AlertType.INFORMATION, "Connection Status", "Application is trying to connect to PLC right now", "");
		else if (plcCom.getConnectionStatus() == ConnectionStatus.DISCONNECTING)
			mainApp.showAlert(AlertType.INFORMATION, "Connection Status", "Application is trying to disconnect from PLC right now", "Please wait untill disconnection procedure will end");
		else if (plcCom.getConnectionStatus() == ConnectionStatus.DISCONNECTION_ERROR)
			mainApp.showAlert(AlertType.INFORMATION, "Connection Status", "Previous disconnection attempt ended with an error", "Please firstly try to disconnect from PLC once again");
		else {
			connectToPlcControllerThread = new Thread(() -> {
				synchronized (plcCom) {
					plcCom.setCommand(CommunicationCommands.CONNECT);
					try {
						plcCom.wait();
					} catch (InterruptedException e) {
						logger.info("Connect to PLC Controller - wait interrupted", e);
					}
					if (plcCom.getConnectionStatus() == ConnectionStatus.CONNECTION_ERROR) {
						Platform.runLater(() -> mainApp.showAlert(AlertType.WARNING, "Connection status", "Connection attept ended with an error",
								"Please try connect again or check communication properties."));
					} else if (plcCom.getConnectionStatus() == ConnectionStatus.CONNECTED) {
						Platform.runLater(() -> mainApp.showAlert(AlertType.INFORMATION, "Connection status", "Connection attept successfully accomplished", ""));
					}
				}
			});
			connectToPlcControllerThread.setName("Connecting to PLC controller thread");
			connectToPlcControllerThread.start();
		}
	}

	public void disconnectFromPlcController() {
		if (plcCom.getConnectionStatus() == ConnectionStatus.DISCONNECTED)
			mainApp.showAlert(AlertType.INFORMATION, "Connection Status", "Communication with PLC is already disconnected", "");
		else if (plcCom.getConnectionStatus() == ConnectionStatus.DISCONNECTING)
			mainApp.showAlert(AlertType.INFORMATION, "Connection Status", "Application is trying to diconnect from PLC right now", "");
		else if (plcCom.getConnectionStatus() == ConnectionStatus.CONNECTING)
			mainApp.showAlert(AlertType.INFORMATION, "Connection Status", "Application is trying to connect to PLC right now", "Please wait untill connection procedure will end");
		else {
			Optional<ButtonType> result = mainApp.showAlert(AlertType.CONFIRMATION, "Confirmation needed", "Do you want to disconnect from PLC?",
					"Click \"OK\"' to perform disconnection procedure or \"CANCEL\" to cancel breaking connection with PLC");
			if (result.get() == ButtonType.OK) {
				disconnectFromPlcControllerThread = new Thread(() -> {
					synchronized (plcCom) {
						plcCom.setCommand(CommunicationCommands.DISCONNECT);
						try {
							plcCom.wait();
						} catch (InterruptedException e) {
							logger.info("Disconnect from PLC Controller - wait interrupted", e);
						}
						if (plcCom.getConnectionStatus() == ConnectionStatus.DISCONNECTION_ERROR) {
							Platform.runLater(() -> mainApp.showAlert(AlertType.WARNING, "Connection status", "Disconnection attept ended with an error", ""));
						} else if (plcCom.getConnectionStatus() == ConnectionStatus.DISCONNECTED) {
							Platform.runLater(() -> mainApp.showAlert(AlertType.INFORMATION, "Connection status", "Disconnection attept successfully accomplished", ""));
						}
					}
				});
				disconnectFromPlcControllerThread.setName("Disconnecting from PLC controller thread");
				disconnectFromPlcControllerThread.start();
			}
		}
	}

	public void newSingleMeasurement() {
		if (plcCom.getConnectionStatus() != ConnectionStatus.CONNECTED)
			mainApp.showAlert(AlertType.ERROR, "Connection Error", "Connection not established", "Please firstly connect to PLC");
		else if (plcStatusListContainPriority(3))
			mainApp.showAlert(AlertType.ERROR, "PLC Errors", "PLC alarms are not resolved", "Please go to PLC Alarms cart for detailed inforamtion");
		else if (plcStatusListContainPriority(2))
			mainApp.showAlert(AlertType.WARNING, "PLC Warnings", "PLC alarms are not resolved", "Please go to PLC Alarms cart for detailed inforamtion");
		else if (!plcStatusListContainPriority(1))
			mainApp.showAlert(AlertType.ERROR, "PLC Status", "PLC is not ready", "PLC is not ready for starting measurement");
		else if (plcCom.getMeasurementStatus() == MeasurementStatus.SINGLE_MEASUREMENT_ERROR || plcCom.getMeasurementStatus() == MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR
				|| plcCom.getMeasurementStatus() == MeasurementStatus.RESTART_ERROR)
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line is in error state", "Please firstly restart measurement line before you start single measurement");
		else if (plcCom.getMeasurementStatus() != MeasurementStatus.IDLE)
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line is busy", "Measurement Status needs to be \"Idle\" to start single measurement");
		else {
			newSingleMeasurementThread = new Thread(() -> {
				synchronized (plcCom) {
					plcCom.setCommand(CommunicationCommands.SINGLE_MEASUREMENT);
					try {
						plcCom.wait();
					} catch (InterruptedException e) {
						logger.info("New single measurement - wait interrupted", e);
					}
					if (plcCom.getMeasurementStatus() == MeasurementStatus.SINGLE_MEASUREMENT_COMPLETED) {
						measurementData.addMeasurement(plcCom.getNewSingleMeasurementReading());
						Platform.runLater(() -> mainApp.showAlert(AlertType.INFORMATION, "Measurement status", "Single measurement successfully accomplished", ""));
					} else {
						Platform.runLater(() -> mainApp.showAlert(AlertType.WARNING, "Measurement status", "Single measurement ended with an error", ""));
					}
				}
			});
			newSingleMeasurementThread.setName("New single measurement thread");
			newSingleMeasurementThread.start();
		}
	}

	public void measurementSerie() {
		if (plcCom.getConnectionStatus() != ConnectionStatus.CONNECTED)
			mainApp.showAlert(AlertType.ERROR, "Connection Error", "Connection not established", "Please firstly connect to PLC");
		else if (plcStatusListContainPriority(3))
			mainApp.showAlert(AlertType.ERROR, "PLC Errors", "PLC alarms are not resolved", "Please go to PLC Alarms cart for detailed inforamtion");
		else if (plcStatusListContainPriority(2))
			mainApp.showAlert(AlertType.WARNING, "PLC Warnings", "PLC alarms are not resolved", "Please go to PLC Alarms cart for detailed inforamtion");
		else if (!plcStatusListContainPriority(1))
			mainApp.showAlert(AlertType.ERROR, "PLC Status", "PLC is not ready", "PLC is not ready for starting measurement");
		else if (plcCom.getMeasurementStatus() == MeasurementStatus.SINGLE_MEASUREMENT_ERROR || plcCom.getMeasurementStatus() == MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR
				|| plcCom.getMeasurementStatus() == MeasurementStatus.RESTART_ERROR)
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line is in error state", "Please firstly restart measurement line before you start single measurement");
		else if (plcCom.getMeasurementStatus() != MeasurementStatus.IDLE)
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line is busy", "Measurement Status needs to be \"Idle\" to start single measurement");
		else {
			measurementSerieThread = new Thread(() -> {
				synchronized (plcCom) {
					plcCom.setCommand(CommunicationCommands.MULTIPLE_MEASUREMENT);
					while (plcCom.getMeasurementStatus() != MeasurementStatus.MULTIPLE_MEASUREMENT_COMPLETED && plcCom.getMeasurementStatus() != MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR) {
						try {
							plcCom.wait();
						} catch (InterruptedException e) {
							logger.info("Measurement serie - wait interrupted", e);
						}
						if (!plcCom.getNewMultipleMeasurementsReading().isEmpty()) {
							measurementData.addMeasurement(plcCom.getNewMultipleMeasurementsReading().get(0));
							plcCom.getNewMultipleMeasurementsReading().remove(0);
						}
					}
					if (plcCom.getMeasurementStatus() == MeasurementStatus.MULTIPLE_MEASUREMENT_COMPLETED) {
						while (!plcCom.getNewMultipleMeasurementsReading().isEmpty()) {
							measurementData.addMeasurement(plcCom.getNewMultipleMeasurementsReading().get(0));
							plcCom.getNewMultipleMeasurementsReading().remove(0);
						}
						Platform.runLater(() -> mainApp.showAlert(AlertType.INFORMATION, "Measurement status", "Measurement serie successfully accomplished", ""));
					} else {
						while (!plcCom.getNewMultipleMeasurementsReading().isEmpty()) {
							measurementData.addMeasurement(plcCom.getNewMultipleMeasurementsReading().get(0));
							plcCom.getNewMultipleMeasurementsReading().remove(0);
						}
						Platform.runLater(() -> mainApp.showAlert(AlertType.WARNING, "Measurement status", "Measurement serie ended with an error", ""));
					}
				}
			});
			measurementSerieThread.setName("Measurement serie thread");
			measurementSerieThread.start();
		}
	}

	public void calibration(double[] calibrationVaules) {
		if (plcCom.getConnectionStatus() != ConnectionStatus.CONNECTED)
			mainApp.showAlert(AlertType.ERROR, "Connection Error", "Connection not established", "Please firstly connect to PLC");
		else if (plcStatusListContainPriority(3))
			mainApp.showAlert(AlertType.ERROR, "PLC Errors", "PLC alarms are not resolved", "Please go to PLC Alarms cart for detailed inforamtion");
		else if (plcCom.getPlcStatusList().size() == 1)
			mainApp.showAlert(AlertType.ERROR, "PLC Status", "PLC is not ready", "PLC is not ready for starting calibration procedure");
		else if (plcCom.getMeasurementStatus() == MeasurementStatus.SINGLE_MEASUREMENT_ERROR || plcCom.getMeasurementStatus() == MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR
				|| plcCom.getMeasurementStatus() == MeasurementStatus.CALIBRATION_ERROR || plcCom.getMeasurementStatus() == MeasurementStatus.RESTART_ERROR)
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line is in error state", "Please firstly restart measurement line before you start calibration procedure");
		else if (plcCom.getMeasurementStatus() != MeasurementStatus.IDLE)
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line is busy", "Measurement Status needs to be \"Idle\" to start calibration procedure");
		else {
			calibrationThread = new Thread(() -> {
				synchronized (plcCom) {
					plcCom.setCalibrationValues(calibrationVaules);
					plcCom.setCommand(CommunicationCommands.CALIBRATION);
					try {
						plcCom.wait();
					} catch (InterruptedException e) {
						logger.info("Calibration - wait interrupted", e);
					}
					if (plcCom.getMeasurementStatus() == MeasurementStatus.CALIBRATION_COMPLETED) {
						Platform.runLater(() -> mainApp.showAlert(AlertType.INFORMATION, "Calibration status", "Measurement line calibration successfully accomplished", ""));
					} else {
						Platform.runLater(() -> mainApp.showAlert(AlertType.WARNING, "Calibration status", "Measurement line calibration ended with an error", ""));
					}
				}
			});
			calibrationThread.setName("Calibration thread");
			calibrationThread.start();
		}
	}

	public void readAllregisters() {
		if (plcCom.getConnectionStatus() == ConnectionStatus.CONNECTED) {
			new Thread(() -> {
				synchronized (plcCom) {
					plcCom.setCommand(CommunicationCommands.READ_ALL_REGISTERS);
					try {
						plcCom.wait();
					} catch (InterruptedException e) {
						logger.info("Read all registers - wait interrupted", e);
					}
					measurementData.setCurrentMeasurement(plcCom.getCurrentMeasurementReading());
				}
			}).start();
		}
	}

	public void restartMeasurementLine() {
		if (plcCom.getConnectionStatus() != ConnectionStatus.CONNECTED)
			mainApp.showAlert(AlertType.ERROR, "Connection Error", "Connection not established", "Please firstly connect to PLC");
		else if (plcStatusListContainPriority(3))
			mainApp.showAlert(AlertType.ERROR, "PLC Errors", "PLC alarms are not resolved", "Please go to PLC Alarms cart for detailed inforamtion");
		else if ((plcCom.getMeasurementStatus() == MeasurementStatus.IDLE || plcCom.getMeasurementStatus() == MeasurementStatus.SINGLE_MEASUREMENT_COMPLETED
				|| plcCom.getMeasurementStatus() == MeasurementStatus.SINGLE_MEASUREMENT_COMPLETED || plcCom.getMeasurementStatus() == MeasurementStatus.RESTART_COMPLETED))
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line cannot be restarted", "Measurement line is already in intitial state");
		else if (!(plcCom.getMeasurementStatus() == MeasurementStatus.SINGLE_MEASUREMENT_ERROR || plcCom.getMeasurementStatus() == MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR
				|| plcCom.getMeasurementStatus() == MeasurementStatus.CALIBRATION_ERROR || plcCom.getMeasurementStatus() == MeasurementStatus.RESTART_ERROR))
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line cannot be restarted", "Measurement line is busy");
		else {
			restartMeasurementLineThread = new Thread(() -> {
				synchronized (plcCom) {
					plcCom.setCommand(CommunicationCommands.RESET_MEASUREMENT_LINE);
					try {
						plcCom.wait();
					} catch (InterruptedException e) {
						logger.info("Restart measurement line - wait interrupted", e);
					}
					if (plcCom.getMeasurementStatus() == MeasurementStatus.RESTART_COMPLETED) {
						Platform.runLater(() -> mainApp.showAlert(AlertType.INFORMATION, "Measurement status", "Restarting procedure successfully accomplished", ""));
					} else {
						Platform.runLater(() -> mainApp.showAlert(AlertType.WARNING, "Measurement status", "Restarting procedure ended with an error", ""));
					}
				}
			});
			restartMeasurementLineThread.setName("Restart measurement line thread");
			restartMeasurementLineThread.start();
		}
	}

	public void stopMeasurementLine() {
		if (plcCom.getConnectionStatus() != ConnectionStatus.CONNECTED) {
			mainApp.showAlert(AlertType.ERROR, "Connection Error", "Connection not established", "Please firstly connect to PLC");
		} else if (!(plcCom.getMeasurementStatus() == MeasurementStatus.SINGLE_MEASUREMENT_IN_PROGRESS || plcCom.getMeasurementStatus() == MeasurementStatus.MULTIPLE_MEASUREMENT_IN_PROGRESS
				|| plcCom.getMeasurementStatus() == MeasurementStatus.RESTART_IN_PROGRESS)) {
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line cannot be stopped", "Measurement line is already stopped");
		} else {
			plcCom.interrupt();
		}
	}

	public void acknowledgementAlarm(int selectedIndex) {
		if (selectedIndex >= 1) {
			synchronized (plcCom) {
				plcCom.getPlcStatusList().remove(selectedIndex);
			}
		} else if (selectedIndex == 0)
			mainApp.showAlert(AlertType.WARNING, "No proper selction", "No alarm selected", "This is information only. It not need confirmation");
		else
			mainApp.showAlert(AlertType.WARNING, "No Selection", "No alarm selected", "Please select an alarm in the table");
	}

	public void acknowledgementAllAlarms() {
		int plcErrorsListSize = plcCom.getPlcStatusList().size();
		if (plcErrorsListSize > 1) {
			synchronized (plcCom) {
				for (int i = 0; i < plcErrorsListSize - 1; i++) {
					plcCom.getPlcStatusList().remove(plcErrorsListSize - 1 - i);
				}
			}
		} else {
			mainApp.showAlert(AlertType.WARNING, "No alarms", "No alarms to acknowledgement", "");
		}
	}

	public void savePlcAlarmAndLogData() {
		plcCom.getPlcStatusList().addListener(new ListChangeListener<PlcStatus>() {
			@Override
			public void onChanged(Change<? extends PlcStatus> c) {
				while (c.next()) {
					if (c.wasPermutated()) {
						for (int i = c.getFrom(); i < c.getTo(); i++) {
							// System.out.println("PlcStatusList was permuted");
						}
					} else if (c.wasUpdated()) {
						// System.out.println("PlcStatusList was updated");
					} else {
						for (PlcStatus remitem : c.getRemoved()) {
							// System.out.println("Status: " + remitem + " was removed from PlcStatusList");
							logger.info("PLC status removed: " + remitem.toString());
						}
						for (PlcStatus additem : c.getAddedSubList()) {
							// System.out.println("Status: " + additem + " was added to PlcStatusList");
							logger.warn("PLC status added: " + additem.toString());
						}
					}
				}
			}
		});
	}

	public void setPlcEmulator(boolean value) {
		synchronized (plcCom) {
			plcCom.setPlcEmulator(value);
		}
	}

	public boolean isPlcEmulator() {
		return plcCom.isPlcEmulator();
	}

	private boolean plcStatusListContainPriority(int priority) {
		boolean result = false;
		for (PlcStatus status : plcCom.getPlcStatusList()) {
			if (status.getPriority() == priority) {
				result = true;
				break;
			}
		}
		return result;
	}

	public MeasurementStatus getMeasurementStatus() {
		return plcCom.getMeasurementStatus();
	}

	public ObservableList<PlcStatus> getPlcStatusList() {
		return plcCom.getPlcStatusList();
	}

	public ConnectionStatus getConnectionStatus() {
		return plcCom.getConnectionStatus();
	}

	public CommunicationProperties getCommunicationProperties() {
		return plcCom.getCommunicationProperties();
	}

}
