package mtw.concretecubesvalidator.view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import mtw.concretecubesvalidator.model.Measurement;

public class MeasurementDetailsDialogController {

	@FXML
	private Label lengthLabel;
	@FXML
	private Label widthLabel;
	@FXML
	private Label heightLabel;
	
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
	
	private Stage dialogStage;

	@FXML
	private void initialize() {
		
	}
	
	@FXML
	private void handleOk() {
		dialogStage.close();
	}
	
	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}
	
	public void setMeasurement(Measurement measurement) {
		lengthLabel.setText(String.format("%.2f", measurement.getLenght().getCalculatededValue()));
		widthLabel.setText(String.format("%.2f", measurement.getWidth().getCalculatededValue()));
		heightLabel.setText(String.format("%.2f", measurement.getHeight().getCalculatededValue()));
		lengthsensor1Label.setText(String.format("%.2f", measurement.getLenght().getSensor1Value()));
		lengthsensor2Label.setText(String.format("%.2f", measurement.getLenght().getSensor2Value()));
		lengthsensor3Label.setText(String.format("%.2f", measurement.getLenght().getSensor3Value()));
		widthsensor1Label.setText(String.format("%.2f", measurement.getWidth().getSensor1Value()));
		widthsensor2Label.setText(String.format("%.2f", measurement.getWidth().getSensor2Value()));
		widthsensor3Label.setText(String.format("%.2f", measurement.getWidth().getSensor3Value()));
		heightsensor1Label.setText(String.format("%.2f", measurement.getHeight().getSensor1Value()));
		heightsensor2Label.setText(String.format("%.2f", measurement.getHeight().getSensor2Value()));
		heightsensor3Label.setText(String.format("%.2f", measurement.getHeight().getSensor3Value()));
	}	
}
