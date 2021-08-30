package mtw.concretecubesvalidator.view;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioMenuItem;
import javafx.stage.FileChooser;
import mtw.concretecubesvalidator.MainApp;
import mtw.concretecubesvalidator.PlcCommunicationController;
import mtw.concretecubesvalidator.model.MeasurementData;
import mtw.concretecubesvalidator.model.communication.ConnectionStatus;
import mtw.concretecubesvalidator.model.communication.PlcCommunication;

public class RootLayoutController {

	private MainApp mainApp;
	private PlcCommunicationController plcComController;
	private MeasurementData measurementData;
	private Thread connectionStatusIndicatorThread;
	static final Logger logger = LoggerFactory.getLogger(PlcCommunication.class);

	@FXML
	private ProgressIndicator connectionProgressIndicator;
	@FXML
	private Label connectionStatusLabel;
	@FXML
	private RadioMenuItem plcEmulatorMenuItem;
	@FXML
	private Label plcEmulatorLabel;

	public RootLayoutController() {
		mainApp = null;
		plcComController = null;
		measurementData = null;
		connectionStatusIndicatorThread = null;
		connectionProgressIndicator = new ProgressIndicator();
	}

	public void initMainApp(MainApp mainApp) {
		if (this.mainApp == null)
			this.mainApp = mainApp;
		else
			throw new IllegalStateException("Controller can only be initialized once");
	}

	public void initPlcComController(PlcCommunicationController plcComController) {
		if (this.plcComController == null) {
			this.plcComController = plcComController;
			plcEmulatorMenuItem.setSelected(plcComController.isPlcEmulator());
			plcEmulatorLabel.setVisible(plcComController.isPlcEmulator());
		} else {
			throw new IllegalStateException("Controller can only be initialized once");
		}
	}

	public void initMeasurementData(MeasurementData measurementData) {
		if (this.measurementData == null) {
			this.measurementData = measurementData;
		} else {
			throw new IllegalStateException("Measurement data can only be initialized once");
		}
	}

	@FXML
	private void initialize() {
		// updating connection progress indicator
		connectionStatusIndicatorThread = new Thread(() -> {
			while (true) {
				Platform.runLater(() -> {
					showConnectionStatus();
				});
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.info("Connection status updating thread - sleep interrupted", e);
				}
			}
		});
		connectionStatusIndicatorThread.setDaemon(true);
		connectionStatusIndicatorThread.setName("Connection status GUI update");
		connectionStatusIndicatorThread.start();

		plcEmulatorLabel.setVisible(false);
	}

	@FXML
	private void handleConnect() {
		plcComController.connectToPlcController();
	}

	@FXML
	private void handleDisonnect() {
		plcComController.disconnectFromPlcController();
	}

	@FXML
	private void handleConnectionProperties() {
		mainApp.showCommunicationPropertiesDialog();
	}

	@FXML
	private void handleNew() {
		Optional<ButtonType> result = mainApp.showAlert(AlertType.CONFIRMATION, "Confirmation needed", "Do you want to create new file?",
				"Creating new file will cause loss unsaved measurements. Before creating new file make sure that all necessary measurements are saved");
		if (result.get() == ButtonType.OK) {
			measurementData.newMeasurementDataFile();
			mainApp.setMeasurementDataFilePathToPref(null);
		}
	}

	@FXML
	private void handleOpen() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
		fileChooser.getExtensionFilters().add(extFilter);

		Path filePath = fileChooser.showOpenDialog(mainApp.getPrimaryStage()).toPath();
		if (filePath != null) {
			measurementData.loadMeasurementDataFromFile(filePath);
			mainApp.setMeasurementDataFilePathToPref(filePath);
		}
	}

	@FXML
	private void handleSave() {
		if (mainApp.getMeasurementDataFilePathfromPref() != null)
			measurementData.saveMeasurementDataToFile(mainApp.getMeasurementDataFilePathfromPref());
		else
			handleSaveAs();
	}

	@FXML
	private void handleSaveAs() {
		FileChooser fileChooser = new FileChooser();
		FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("CSV files (*.csv)", "*.csv");
		fileChooser.getExtensionFilters().add(extFilter);

		Path filePath = fileChooser.showSaveDialog(mainApp.getPrimaryStage()).toPath();
		if (filePath != null) {
			if (!filePath.toString().toLowerCase().endsWith(".csv")) {
				filePath = Paths.get(filePath.toString(), ".csv");
			}
			measurementData.saveMeasurementDataToFile(filePath);
			mainApp.setMeasurementDataFilePathToPref(filePath);
		}
	}

	@FXML
	private void handleClose() {
		mainApp.stop();
		System.exit(0);
	}

	@FXML
	private void handleCalibration() {
		mainApp.showCalibrationDialog();
	}

	@FXML
	private void handlePlcEmulator() {
		if (plcEmulatorMenuItem.isSelected()) {
			plcComController.setPlcEmulator(true);
			plcEmulatorLabel.setVisible(true);
		} else {
			plcComController.setPlcEmulator(false);
			plcEmulatorLabel.setVisible(false);
		}
	}

	@FXML
	private void handleAbout() {
		mainApp.showAlert(AlertType.INFORMATION, "Concrete Cubs Validator Application", "Application version 1.0", "Author: Mateusz Włodarczyk");
	}

	public void exitProcedure() {
		if (measurementData.getIsAllChangeSaved() == false) {
			Optional<ButtonType> result = mainApp.showAlert(AlertType.CONFIRMATION, "Confirmation needed", "Do you want to save recent file before closing application?",
					"Closing application will cause loss unsaved measurements. Click \"OK\" to save recent file or \"CANCEL\" to discard unsaved measurements");
			if (result.get() == ButtonType.OK) {
				handleSave();
			}
		}
	}

	private void showConnectionStatus() {
		if (plcComController.getConnectionStatus() == ConnectionStatus.DISCONNECTED) {
			connectionStatusLabel.setText("Disconnected");
			connectionProgressIndicator.setProgress(0.0f);
			connectionProgressIndicator.setPadding(new Insets(0, 0, -16, 0));
			connectionProgressIndicator.setStyle(" -fx-progress-color: blue;");
		} else if (plcComController.getConnectionStatus() == ConnectionStatus.CONNECTING) {
			connectionStatusLabel.setText("Connecting");
			connectionProgressIndicator.setProgress(-1.0f);
			connectionProgressIndicator.setPadding(new Insets(0, 0, 0, 0));
			connectionProgressIndicator.setStyle(" -fx-progress-color: blue;");
		} else if (plcComController.getConnectionStatus() == ConnectionStatus.DISCONNECTING) {
			connectionStatusLabel.setText("Disconnecting");
			connectionProgressIndicator.setProgress(-1.0f);
			connectionProgressIndicator.setPadding(new Insets(0, 0, 0, 0));
			connectionProgressIndicator.setStyle(" -fx-progress-color: blue;");
		} else if (plcComController.getConnectionStatus() == ConnectionStatus.CONNECTED) {
			connectionStatusLabel.setText("Connected");
			connectionProgressIndicator.setProgress(1.0f);
			connectionProgressIndicator.setPadding(new Insets(0, 0, -16, 0));
			connectionProgressIndicator.setStyle(" -fx-progress-color: green;");
		} else if (plcComController.getConnectionStatus() == ConnectionStatus.CONNECTION_ERROR) {
			connectionStatusLabel.setText("Connection error");
			connectionProgressIndicator.setProgress(-1.0f);
			connectionProgressIndicator.setPadding(new Insets(0, 0, 0, 0));
			connectionProgressIndicator.setStyle(" -fx-progress-color: red;");
		} else if (plcComController.getConnectionStatus() == ConnectionStatus.DISCONNECTION_ERROR) {
			connectionStatusLabel.setText("Disconnection error");
			connectionProgressIndicator.setProgress(-1.0f);
			connectionProgressIndicator.setPadding(new Insets(0, 0, 0, 0));
			connectionProgressIndicator.setStyle(" -fx-progress-color: red;");
		} else {
			connectionStatusLabel.setText("");
		}
	}

}
