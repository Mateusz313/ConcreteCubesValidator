package mtw.concretecubesvalidator.view;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import mtw.concretecubesvalidator.MainApp;
import mtw.concretecubesvalidator.PlcCommunicationController;

public class CalibrationDialogController {

	@FXML
	private TextField lengthValue1Field;
	@FXML
	private TextField lengthValue2Field;
	@FXML
	private TextField widthValue1Field;
	@FXML
	private TextField widthValue2Field;
	@FXML
	private TextField heightValue1Field;
	@FXML
	private TextField heightValue2Field;

	private Stage dialogStage;
	private MainApp mainApp;
	private PlcCommunicationController plcComController;
	private double[] calibrationValues;

	public CalibrationDialogController() {
		dialogStage = null;
		mainApp = null;
		plcComController = null;
		calibrationValues = new double[] { 99.00, 101.00, 149.00, 151.00, 199.00, 201.00 };
	}

	@FXML
	private void initialize() {
		lengthValue1Field.setText("99.00");
		lengthValue2Field.setText("101.00");
		widthValue1Field.setText("149.00");
		widthValue2Field.setText("151.00");
		heightValue1Field.setText("199.00");
		heightValue2Field.setText("201.00");
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
		} else
			throw new IllegalStateException("Controller can only be initialized once");
	}

	@FXML
	private void handleCancel() {
		dialogStage.close();
	}

	@FXML
	private void handleStartCalibration() {
		if (isInputValid() == true) {
			plcComController.calibration(calibrationValues);
			dialogStage.close();
		}
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	private boolean isInputValid() {
		String errorMessage = "";

		if (lengthValue1Field.getText() == null || lengthValue1Field.getText().length() == 0) {
			errorMessage += "No valid length value 1 field!\n";
		} else {
			try {
				calibrationValues[0] = Double.parseDouble(lengthValue1Field.getText().replaceAll(",", "."));
			} catch (NumberFormatException e) {
				errorMessage += "No valid length value 1 field(must be a number)!\n";
			}
		}
		if (lengthValue2Field.getText() == null || lengthValue2Field.getText().length() == 0) {
			errorMessage += "No valid length value 2 field!\n";
		} else {
			try {
				calibrationValues[1] = Double.parseDouble(lengthValue1Field.getText().replaceAll(",", "."));
			} catch (NumberFormatException e) {
				errorMessage += "No valid length value1 field(must be a number)!\n";
			}
		}
		if (widthValue1Field.getText() == null || widthValue1Field.getText().length() == 0) {
			errorMessage += "No valid width value 1 field!\n";
		} else {
			try {
				calibrationValues[2] = Double.parseDouble(widthValue1Field.getText().replaceAll(",", "."));
			} catch (NumberFormatException e) {
				errorMessage += "No valid width value 1 field(must be a number)!\n";
			}
		}
		if (widthValue2Field.getText() == null || widthValue2Field.getText().length() == 0) {
			errorMessage += "No valid width value 2 field!\n";
		} else {
			try {
				calibrationValues[3] = Double.parseDouble(lengthValue2Field.getText().replaceAll(",", "."));
			} catch (NumberFormatException e) {
				errorMessage += "No valid width value 2 field(must be a number)!\n";
			}
		}
		if (heightValue1Field.getText() == null || heightValue1Field.getText().length() == 0) {
			errorMessage += "No valid height value 1 field!\n";
		} else {
			try {
				calibrationValues[4] = Double.parseDouble(heightValue1Field.getText().replaceAll(",", "."));
			} catch (NumberFormatException e) {
				errorMessage += "No valid height value 1 field(must be a number)!\n";
			}
		}
		if (heightValue2Field.getText() == null || heightValue2Field.getText().length() == 0) {
			errorMessage += "No valid height value 2 field!\n";
		} else {
			try {
				calibrationValues[5] = Double.parseDouble(heightValue2Field.getText().replaceAll(",", "."));
			} catch (NumberFormatException e) {
				errorMessage += "No valid height value 2 field(must be a number)!\n";
			}
		}

		if (errorMessage.length() == 0) {
			return true;
		} else {
			mainApp.showAlert(AlertType.ERROR, "Invalid Fields", "Please correct invalid fields", errorMessage);
			return false;
		}
	}
}
