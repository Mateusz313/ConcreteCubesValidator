package mtw.concretecubesvalidator.view;

import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import mtw.concretecubesvalidator.MainApp;
import mtw.concretecubesvalidator.model.communication.CommunicationProperties;

public class CommunicationPropertiesDialogController {

	@FXML
	private TextField nameFiled;
	@FXML
	private TextField serverIpAdressFiled;
	@FXML
	private TextField tcpPortFiled;
	@FXML
	private CheckBox fixedNetworkInterface;
	@FXML
	private TextField networkInterfaceAdressFiled;

	private CommunicationProperties communicationProperties;
	private MainApp mainApp;
	private Stage dialogStage;

	public void initMainApp(MainApp mainApp) {
		if (this.mainApp == null)
			this.mainApp = mainApp;
		else
			throw new IllegalStateException("Controller can only be initialized once");
	}

	public void setDialogStage(Stage dialogStage) {
		this.dialogStage = dialogStage;
	}

	public void setCommunicationProperties(CommunicationProperties communicationProperties) {
		this.communicationProperties = communicationProperties;
		nameFiled.setText(String.format(communicationProperties.getName()));
		serverIpAdressFiled.setText(String.format(communicationProperties.getServerIpAdress()));
		tcpPortFiled.setText(String.valueOf((communicationProperties.getTcpPort())));
		fixedNetworkInterface.setSelected(communicationProperties.isFixedNetworkInterface());
		networkInterfaceAdressFiled.setText(String.format(communicationProperties.getNetworkInterfaceIpAdress()));
	}

	@FXML
	private void handleOk() {
		String validationResoult = communicationProperties.validateInput(nameFiled.getText(), serverIpAdressFiled.getText(), tcpPortFiled.getText(), String.valueOf(fixedNetworkInterface.isSelected()),
				networkInterfaceAdressFiled.getText());
		if (validationResoult.equals("")) {
			communicationProperties.setName(nameFiled.getText());
			communicationProperties.setServerIpAdress(serverIpAdressFiled.getText());
			communicationProperties.setTcpPort(Integer.parseInt(tcpPortFiled.getText()));
			communicationProperties.setFixedNetworkInterface(fixedNetworkInterface.isSelected());
			communicationProperties.setNetworkInterfaceIpAdress(networkInterfaceAdressFiled.getText());
			if (communicationProperties.overwriteDefaultProperties() == false) {
				mainApp.showAlert(AlertType.ERROR, "Writing data error", "Default communication properties cannot be overwritten",
						"Some unexpected error occur during overwritting default communication properties. However entered changed properties will be setted in this application session");
			}
			dialogStage.close();
		} else {
			mainApp.showAlert(AlertType.ERROR, "Invalid input data", "Please see information below and correct invalid fields", validationResoult);
		}
	}

	@FXML
	private void changeAccesibilityNetworkInterfaceAdressFiled() {
		if (fixedNetworkInterface.isSelected()) {
			networkInterfaceAdressFiled.setDisable(false);
		} else {
			networkInterfaceAdressFiled.setDisable(true);
		}
	}

	@FXML
	private void handleCancel() {
		dialogStage.close();
	}
}
