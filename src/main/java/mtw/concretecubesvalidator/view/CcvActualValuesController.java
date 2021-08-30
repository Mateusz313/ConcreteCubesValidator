package mtw.concretecubesvalidator.view;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ToggleButton;
import mtw.concretecubesvalidator.MainApp;
import mtw.concretecubesvalidator.PlcCommunicationController;
import mtw.concretecubesvalidator.model.Measurement;
import mtw.concretecubesvalidator.model.MeasurementData;
import mtw.concretecubesvalidator.model.communication.ConnectionStatus;
import mtw.concretecubesvalidator.model.communication.MeasurementStatus;
import mtw.concretecubesvalidator.model.communication.PlcCommunication;
import mtw.concretecubesvalidator.model.communication.PlcStatus;
import mtw.concretecubesvalidator.util.DateTimeUtil;
import mtw.concretecubesvalidator.util.NumberUtil;

public class CcvActualValuesController {

//Measurement tab view
	@FXML
	private TableView<Measurement> measurementList;
	@FXML
	private TableColumn<Measurement, LocalDate> sampleDateColumn;
	@FXML
	private TableColumn<Measurement, LocalTime> sampleTimeColumn;
	@FXML
	private TableColumn<Measurement, String> barCodeColumn;
	@FXML
	private TableColumn<Measurement, Double> weightColumn;
	@FXML
	private TableColumn<Measurement, Double> lengthColumn;
	@FXML
	private TableColumn<Measurement, Double> widthColumn;
	@FXML
	private TableColumn<Measurement, Double> heightColumn;

//PLC Alarms tab view	
	@FXML
	private TableView<PlcStatus> plcStatusList;
	@FXML
	private TableColumn<PlcStatus, LocalDate> occuranceDateColumn;
	@FXML
	private TableColumn<PlcStatus, LocalTime> occuranceTimeColumn;
	@FXML
	private TableColumn<PlcStatus, Integer> priorityColumn;
	@FXML
	private TableColumn<PlcStatus, String> descriptionColumn;

	// Labels in actual values window
	@FXML
	private Label lengthLabel;
	@FXML
	private Label widthLabel;
	@FXML
	private Label heightLabel;
	@FXML
	private Label weightLabel;
	@FXML
	private Label barCodeLabel;
	@FXML
	private Label sampleDateLabel;
	@FXML
	private Label sampleTimeLabel;

	@FXML
	private Label lengthsensor1Label;
	@FXML
	private Label lengthsensor2Label;
	@FXML
	private Label lengthsensor3Label;

	@FXML
	private Label widthsensor1Label;
	@FXML
	private Label widthsensor2Label;
	@FXML
	private Label widthsensor3Label;

	@FXML
	private Label heightsensor1Label;
	@FXML
	private Label heightsensor2Label;
	@FXML
	private Label heightsensor3Label;

	// Labels in measurement window
	@FXML
	private Label lengthLabel_mw;
	@FXML
	private Label widthLabel_mw;
	@FXML
	private Label heightLabel_mw;
	@FXML
	private Label weightLabel_mw;
	@FXML
	private Label barCodeLabel_mw;
	@FXML
	private Label sampleDateLabel_mw;
	@FXML
	private Label sampleTimeLabel_mw;

	// Buttons & general label
	@FXML
	private ToggleButton startStopReadActualValuesToggleButton;
	@FXML
	private Label measurementStatusLabel_mw;

	private MainApp mainApp;
	private PlcCommunicationController plcComController;
	private MeasurementData measurementData;
	private Thread measurementStatusThread;
	private Thread ReadActualValuesThread;
	static final Logger logger = LoggerFactory.getLogger(PlcCommunication.class);

	public CcvActualValuesController() {

	}

	@FXML
	private void initialize() {

//Measurement tab
		sampleDateColumn.setCellFactory((TableColumn<Measurement, LocalDate> tc) -> new TableCell<Measurement, LocalDate>() {
			@Override
			protected void updateItem(LocalDate value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(DateTimeUtil.dateFormat(value));
				}
				tc.setStyle("-fx-alignment: top-center;");
			}
		});

		sampleTimeColumn.setCellFactory((TableColumn<Measurement, LocalTime> tc) -> new TableCell<Measurement, LocalTime>() {
			@Override
			protected void updateItem(LocalTime value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(DateTimeUtil.timeFormat(value));
				}
				tc.setStyle("-fx-alignment: top-center;");
			}
		});

		barCodeColumn.setCellFactory((TableColumn<Measurement, String> tc) -> new TableCell<Measurement, String>() {
			@Override
			protected void updateItem(String value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(value);
				}
				tc.setStyle("-fx-alignment: top-center;");
			}
		});

		weightColumn.setCellFactory((TableColumn<Measurement, Double> tc) -> new TableCell<Measurement, Double>() {
			@Override
			protected void updateItem(Double value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(NumberUtil.doubleFormat(value, 1));
				}
				tc.setStyle("-fx-alignment: top-left;");
			}
		});

		lengthColumn.setCellFactory((TableColumn<Measurement, Double> tc) -> new TableCell<Measurement, Double>() {
			@Override
			protected void updateItem(Double value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(NumberUtil.doubleFormat(value, 2));
				}
				tc.setStyle("-fx-alignment: top-left;");
			}
		});

		widthColumn.setCellFactory((TableColumn<Measurement, Double> tc) -> new TableCell<Measurement, Double>() {
			@Override
			protected void updateItem(Double value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(NumberUtil.doubleFormat(value, 2));
				}
				tc.setStyle("-fx-alignment: top-left;");
			}
		});

		heightColumn.setCellFactory((TableColumn<Measurement, Double> tc) -> new TableCell<Measurement, Double>() {
			@Override
			protected void updateItem(Double value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(NumberUtil.doubleFormat(value, 2));
				}
				tc.setStyle("-fx-alignment: top-left;");
			}
		});

//PLC Alarms tab
		occuranceDateColumn.setCellFactory((TableColumn<PlcStatus, LocalDate> tc) -> new TableCell<PlcStatus, LocalDate>() {
			@Override
			protected void updateItem(LocalDate value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(DateTimeUtil.dateFormat(value));
				}
				tc.setStyle("-fx-alignment: top-center;");
			}
		});

		occuranceTimeColumn.setCellFactory((TableColumn<PlcStatus, LocalTime> tc) -> new TableCell<PlcStatus, LocalTime>() {
			@Override
			protected void updateItem(LocalTime value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(DateTimeUtil.timeFormat(value));
				}
				tc.setStyle("-fx-alignment: top-center;");
			}
		});

		priorityColumn.setCellFactory((TableColumn<PlcStatus, Integer> tc) -> new TableCell<PlcStatus, Integer>() {
			@Override
			protected void updateItem(Integer value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(NumberUtil.intFormat(value));
				}
				tc.setStyle("-fx-alignment: top-center;");
			}
		});

		descriptionColumn.setCellFactory((TableColumn<PlcStatus, String> tc) -> new TableCell<PlcStatus, String>() {
			@Override
			protected void updateItem(String value, boolean empty) {
				super.updateItem(value, empty);
				if (value == null || empty) {
					setText(null);
					setGraphic(null);
				} else {
					setText(value);
				}
				tc.setStyle("-fx-alignment: top-center;");
			}
		});

		// Measurement tab
		sampleDateColumn.setCellValueFactory(cellData -> (cellData.getValue().sampleDateProperty()));
		sampleTimeColumn.setCellValueFactory(cellData -> (cellData.getValue().sampleTimeProperty()));
		barCodeColumn.setCellValueFactory(cellData -> (cellData.getValue().getBarCode().barCodeProperty()));
		weightColumn.setCellValueFactory(cellData -> (cellData.getValue().getWeight().weightValueProperty().asObject()));
		lengthColumn.setCellValueFactory(cellData -> (cellData.getValue().getLenght().calculatededValueProperty().asObject()));
		widthColumn.setCellValueFactory(cellData -> (cellData.getValue().getWidth().calculatededValueProperty().asObject()));
		heightColumn.setCellValueFactory(cellData -> (cellData.getValue().getHeight().calculatededValueProperty().asObject()));
		// PLC Error tab
		occuranceDateColumn.setCellValueFactory(cellData -> (cellData.getValue().occuranceDateProperty()));
		occuranceTimeColumn.setCellValueFactory(cellData -> (cellData.getValue().occuranceTimeProperty()));
		priorityColumn.setCellValueFactory(cellData -> (cellData.getValue().priorityProperty().asObject()));
		descriptionColumn.setCellValueFactory(cellData -> (cellData.getValue().descriptionProperty()));

		// Clear details.
		showMeasurementValues(null);
		showActualValues(null);

		// updating measurement status
		measurementStatusThread = new Thread(() -> {
			while (true) {
				Platform.runLater(() -> {
					measurementStatusLabel_mw.setText(plcComController.getMeasurementStatus().getDescription());
				});
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					logger.info("Measurement status updating thread - sleep interrupted", e);
				}
			}
		});
		measurementStatusThread.setDaemon(true);
		measurementStatusThread.setName("Measurement status GUI update thread");
		measurementStatusThread.start();
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
			plcStatusList.setItems(plcComController.getPlcStatusList());
		} else
			throw new IllegalStateException("Controller can only be initialized once");
	}

	public void initMeasurementData(MeasurementData measurementData) {
		if (this.measurementData == null) {
			this.measurementData = measurementData;
			measurementList.setItems(measurementData.getMeasurementList());
			measurementList.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> measurementData.setSlectedMeasurement(newSelection));

			measurementData.slectedMeasurementProperty().addListener((obs, oldMeasurement, newMeasurement) -> {
				if (newMeasurement == null)
					measurementList.getSelectionModel().clearSelection();
				else
					measurementList.getSelectionModel().select(newMeasurement);

				showMeasurementValues(newMeasurement);
			});
		} else
			throw new IllegalStateException("Measurement data can only be initialized once");
	}

	@FXML
	private void handleNewSingleMeasurement() {
		if (startStopReadActualValuesToggleButton.isSelected() == true)
			mainApp.showAlert(AlertType.INFORMATION, "PLC Communication", "PLC Communication is busy", "Please firstly stop continuous actual values reading");
		else
			plcComController.newSingleMeasurement();
	}

	@FXML
	private void handleMeasurementSerie() {
		if (startStopReadActualValuesToggleButton.isSelected() == true)
			mainApp.showAlert(AlertType.INFORMATION, "PLC Communication", "PLC Communication is busy", "Please firstly stop continuous actual values reading");
		else
			plcComController.measurementSerie();
	}

	@FXML
	private void handleRestartMeasurementLine() {
		if (startStopReadActualValuesToggleButton.isSelected() == true)
			mainApp.showAlert(AlertType.INFORMATION, "PLC Communication", "PLC Communication is busy", "Please firstly stop continuous actual values reading");
		else
			plcComController.restartMeasurementLine();
	}

	@FXML
	private void handleStopMeasurementLine() {
		plcComController.stopMeasurementLine();
	}

	@FXML
	private void handleDeleteMeasurement() {
		if (measurementData.getSlectedMeasurement() != null) {
			Optional<ButtonType> result = mainApp.showAlert(AlertType.CONFIRMATION, "Confirmation needed", "Do you want to delate selected measurement?",
					"Click \"OK\" to delate selected measurement or \"CANCEL\" to cancel deleting operation");
			if (result.get() == ButtonType.OK)
				measurementData.removeSlectedMeasurement();
		} else {
			mainApp.showAlert(AlertType.WARNING, "No Selection", "No measurement selected", "Please select a measurement in the table");
		}
	}

	@FXML
	private void handleMeasurementDetails() {
		if (measurementData.getSlectedMeasurement() != null)
			mainApp.showMeasurementDetailsDialog(measurementData.getSlectedMeasurement());
		else
			mainApp.showAlert(AlertType.WARNING, "No Selection", "No measurement selected", "Please select a measurement in the table");
	}

	@FXML
	private void handleAcknowledgementSelected() {
		int selectedIndex = plcStatusList.getSelectionModel().getSelectedIndex();
		plcComController.acknowledgementAlarm(selectedIndex);
	}

	@FXML
	private void handleAcknowledgementAll() {
		plcComController.acknowledgementAllAlarms();
	}

	// Possibility in future to add listener to model CurrentMeasurement property
	@FXML
	private void handleReadActualValuesThread() {
		if (plcComController.getConnectionStatus() != ConnectionStatus.CONNECTED) {
			showActualValues(null);
			startStopReadActualValuesToggleButton.setSelected(false);
			startStopReadActualValuesToggleButton.setText("Start Read Data");
			mainApp.showAlert(AlertType.ERROR, "Connection Error", "Connection not established", "Please firstly connect to PLC");
		} else if (plcComController.getMeasurementStatus() != MeasurementStatus.IDLE && plcComController.getMeasurementStatus() != MeasurementStatus.SINGLE_MEASUREMENT_ERROR
				&& plcComController.getMeasurementStatus() != MeasurementStatus.MULTIPLE_MEASUREMENT_ERROR && plcComController.getMeasurementStatus() != MeasurementStatus.RESTART_ERROR) {
			showActualValues(null);
			startStopReadActualValuesToggleButton.setSelected(false);
			startStopReadActualValuesToggleButton.setText("Start Read Data");
			mainApp.showAlert(AlertType.INFORMATION, "Measurement Status", "Measurement line is in incorrect state",
					"Measurement Status needs to be \"Idle\" or \"in Error State\" to start continuous register reading");
		} else {
			if (startStopReadActualValuesToggleButton.isSelected() == true) {
				startStopReadActualValuesToggleButton.setText("Stop Read Data");
				ReadActualValuesThread = new Thread(() -> {
					while (startStopReadActualValuesToggleButton.isSelected() == true) {
						plcComController.readAllregisters();
						Platform.runLater(() -> showActualValues(measurementData.getCurrentMeasurement()));
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							logger.info("Read actual value thread - sleep interrupted", e);
							break;
						}
					}
					showActualValues(null);
				});
				ReadActualValuesThread.setName("Read actual values thread");
				ReadActualValuesThread.start();
			} else {
				showActualValues(null);
				startStopReadActualValuesToggleButton.setText("Start Read Data");
			}
		}
	}

	public void stopAllThreads() {
		if (ReadActualValuesThread != null)
			ReadActualValuesThread.interrupt();
	}

	public void showActualValues(Measurement measurementActualValues) {
		if (measurementActualValues != null) {
			lengthLabel.setText(String.format("%.2f", measurementActualValues.getLenght().getCalculatededValue()) + " mm");
			widthLabel.setText(String.format("%.2f", measurementActualValues.getWidth().getCalculatededValue()) + " mm");
			heightLabel.setText(String.format("%.2f", measurementActualValues.getHeight().getCalculatededValue()) + " mm");
			weightLabel.setText(String.format("%.1f", measurementActualValues.getWeight().getWeightValue()) + " g");
			barCodeLabel.setText(measurementActualValues.getBarCode().getBarCode());
			sampleDateLabel.setText(DateTimeUtil.dateFormat(measurementActualValues.getSampleDate()));
			sampleTimeLabel.setText(DateTimeUtil.timeFormat(measurementActualValues.getSampleTime()));
			lengthsensor1Label.setText(String.format("%.2f", measurementActualValues.getLenght().getSensor1Value()) + " mm");
			lengthsensor2Label.setText(String.format("%.2f", measurementActualValues.getLenght().getSensor2Value()) + " mm");
			lengthsensor3Label.setText(String.format("%.2f", measurementActualValues.getLenght().getSensor3Value()) + " mm");
			widthsensor1Label.setText(String.format("%.2f", measurementActualValues.getWidth().getSensor1Value()) + " mm");
			widthsensor2Label.setText(String.format("%.2f", measurementActualValues.getWidth().getSensor2Value()) + " mm");
			widthsensor3Label.setText(String.format("%.2f", measurementActualValues.getWidth().getSensor3Value()) + " mm");
			heightsensor1Label.setText(String.format("%.2f", measurementActualValues.getHeight().getSensor1Value()) + " mm");
			heightsensor2Label.setText(String.format("%.2f", measurementActualValues.getHeight().getSensor2Value()) + " mm");
			heightsensor3Label.setText(String.format("%.2f", measurementActualValues.getHeight().getSensor3Value()) + " mm");
		} else {
			lengthLabel.setText(String.format("%.2f", 0.0) + " mm");
			widthLabel.setText(String.format("%.2f", 0.0) + " mm");
			heightLabel.setText(String.format("%.2f", 0.0) + " mm");
			weightLabel.setText(String.format("%.1f", 0.0) + " g");
			barCodeLabel.setText("*********");
			sampleDateLabel.setText("****-**-**");
			sampleTimeLabel.setText("** : ** : **");
			lengthsensor1Label.setText(String.format("%.2f", 0.0) + " mm");
			lengthsensor2Label.setText(String.format("%.2f", 0.0) + " mm");
			lengthsensor3Label.setText(String.format("%.2f", 0.0) + " mm");
			widthsensor1Label.setText(String.format("%.2f", 0.0) + " mm");
			widthsensor2Label.setText(String.format("%.2f", 0.0) + " mm");
			widthsensor3Label.setText(String.format("%.2f", 0.0) + " mm");
			heightsensor1Label.setText(String.format("%.2f", 0.0) + " mm");
			heightsensor2Label.setText(String.format("%.2f", 0.0) + " mm");
			heightsensor3Label.setText(String.format("%.2f", 0.0) + " mm");
		}
	}

	public void showMeasurementValues(Measurement measurement_mw) {
		if (measurement_mw != null) {
			lengthLabel_mw.setText(String.format("%.2f", measurement_mw.getLenght().getCalculatededValue()) + " mm");
			widthLabel_mw.setText(String.format("%.2f", measurement_mw.getWidth().getCalculatededValue()) + " mm");
			heightLabel_mw.setText(String.format("%.2f", measurement_mw.getHeight().getCalculatededValue()) + " mm");
			weightLabel_mw.setText(String.format("%.1f", measurement_mw.getWeight().getWeightValue()) + " g");
			barCodeLabel_mw.setText(measurement_mw.getBarCode().getBarCode());
			sampleDateLabel_mw.setText(DateTimeUtil.dateFormat(measurement_mw.getSampleDate()));
			sampleTimeLabel_mw.setText(DateTimeUtil.timeFormat(measurement_mw.getSampleTime()));
		} else {
			lengthLabel_mw.setText(String.format("%.2f", 0.0) + " mm");
			widthLabel_mw.setText(String.format("%.2f", 0.0) + " mm");
			heightLabel_mw.setText(String.format("%.2f", 0.0) + " mm");
			weightLabel_mw.setText(String.format("%.1f", 0.0) + " g");
			barCodeLabel_mw.setText("*********");
			sampleDateLabel_mw.setText("****-**-**");
			sampleTimeLabel_mw.setText("** : ** : **");
		}
	}

}
