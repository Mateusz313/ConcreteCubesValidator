package mtw.concretecubesvalidator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.stage.Modality;
import javafx.stage.Stage;
import mtw.concretecubesvalidator.model.Measurement;
import mtw.concretecubesvalidator.model.MeasurementData;
import mtw.concretecubesvalidator.view.CalibrationDialogController;
import mtw.concretecubesvalidator.view.CcvActualValuesController;
import mtw.concretecubesvalidator.view.CommunicationPropertiesDialogController;
import mtw.concretecubesvalidator.view.MeasurementDetailsDialogController;
import mtw.concretecubesvalidator.view.RootLayoutController;

public class MainApp extends Application {

	private Stage primaryStage;
	private BorderPane rootLayout;
	private PlcCommunicationController plcComController;
	private MeasurementData measurementData;
	private CcvActualValuesController ccvActualValuesController;
	private RootLayoutController rootLayoutController;
	private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

	public MainApp() {
		plcComController = new PlcCommunicationController();
		measurementData = new MeasurementData();
	}

	@Override
	public void start(Stage primaryStage) {
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Concrete cubes validator");
		this.primaryStage.getIcons().add(new Image(MainApp.class.getResource("images/cube_icon.png").toString()));
		initRootLayout();
		showCCVActualValues();
		plcComController.initMainApp(this);
		plcComController.initMeasurementData(measurementData);
		plcComController.savePlcAlarmAndLogData();
		measurementData.saveMeasurementListBackup();
	}

	@Override
	public void stop() {
		logger.info("Stop procedure started");
		rootLayoutController.exitProcedure();
		ccvActualValuesController.stopAllThreads();
		plcComController.stopAllThreads();
	}

	public void initRootLayout() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/RootLayout.fxml"));
			rootLayout = (BorderPane) loader.load();

			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();

			rootLayoutController = loader.getController();
			rootLayoutController.initMainApp(this);
			rootLayoutController.initPlcComController(plcComController);
			rootLayoutController.initMeasurementData(measurementData);

			// Try to load last opened measurement data.
			Path filePath = getMeasurementDataFilePathfromPref();
			if (filePath != null && new File(filePath.toString()).exists()) {
				measurementData.loadMeasurementDataFromFile(filePath);
				primaryStage.setTitle("Concrete Cubs Validator - " + filePath.getFileName());
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showCCVActualValues() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/CcvActualValues.fxml"));
			AnchorPane ccvActualValues = (AnchorPane) loader.load();

			rootLayout.setCenter(ccvActualValues);
			ccvActualValuesController = loader.getController();
			ccvActualValuesController.initMainApp(this);
			ccvActualValuesController.initPlcComController(plcComController);
			ccvActualValuesController.initMeasurementData(measurementData);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showMeasurementDetailsDialog(Measurement measurement) {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/MeasurementDetailsDialog.fxml"));
			AnchorPane dialog = (AnchorPane) loader.load();

			Stage dialogStage = new Stage();
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setTitle("Measurement details");
			dialogStage.getIcons().add(new Image(MainApp.class.getResource("images/cube_icon.png").toString()));

			Scene scene = new Scene(dialog);
			dialogStage.setScene(scene);

			MeasurementDetailsDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.setMeasurement(measurement);

			dialogStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showCalibrationDialog() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/CalibrationDialog.fxml"));
			AnchorPane dialog = (AnchorPane) loader.load();

			Stage dialogStage = new Stage();
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setTitle("Calibration");
			dialogStage.getIcons().add(new Image(MainApp.class.getResource("images/cube_icon.png").toString()));

			Scene scene = new Scene(dialog);
			dialogStage.setScene(scene);

			CalibrationDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.initMainApp(this);
			controller.initPlcComController(plcComController);

			dialogStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void showCommunicationPropertiesDialog() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("view/CommunicationPropertiesDialog.fxml"));
			AnchorPane dialog = (AnchorPane) loader.load();

			Stage dialogStage = new Stage();
			dialogStage.initModality(Modality.WINDOW_MODAL);
			dialogStage.initOwner(primaryStage);
			dialogStage.setTitle("Communication");
			dialogStage.getIcons().add(new Image(MainApp.class.getResource("images/cube_icon.png").toString()));

			Scene scene = new Scene(dialog);
			dialogStage.setScene(scene);

			CommunicationPropertiesDialogController controller = loader.getController();
			controller.setDialogStage(dialogStage);
			controller.initMainApp(this);
			controller.setCommunicationProperties(plcComController.getCommunicationProperties());
			dialogStage.showAndWait();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Optional<ButtonType> showAlert(AlertType alertType, String title, String headerText, String contentText) {
		Alert alert = new Alert(alertType);
		DialogPane dialogPane = alert.getDialogPane();
		dialogPane.getStylesheets().add(getClass().getResource("view/AppTheme.css").toExternalForm());
		dialogPane.getStyleClass().add("AppTheme");
		Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
		dialogStage.getIcons().add(new Image(MainApp.class.getResource("images/cube_icon.png").toString()));
		alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
		alert.setTitle(title);
		alert.setHeaderText(headerText);
		alert.setContentText(contentText);
		Optional<ButtonType> result = alert.showAndWait();
		return result;
	}

	public Path getMeasurementDataFilePathfromPref() {
		Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
		String filePath = prefs.get("filePath", null);
		if (filePath != null) {
			return Paths.get(filePath);
		} else {
			return null;
		}
	}

	public void setMeasurementDataFilePathToPref(Path filePath) {
		Preferences prefs = Preferences.userNodeForPackage(MainApp.class);
		if (filePath != null) {
			prefs.put("filePath", filePath.toString());
			primaryStage.setTitle("Concrete Cubs Validator - " + filePath.getFileName());
		} else {
			prefs.remove("filePath");
			primaryStage.setTitle("Concrete Cubs Validator");
		}
	}

	public Stage getPrimaryStage() {
		return primaryStage;
	}

	public static void main(String[] args) {
		launch(args);
	}

}
