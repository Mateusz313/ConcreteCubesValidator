package mtw.concretecubesvalidator.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import mtw.concretecubesvalidator.MainApp;
import mtw.concretecubesvalidator.util.DateTimeUtil;

public class MeasurementData {

	private ObservableList<Measurement> measurementList;
	private ObjectProperty<Measurement> slectedMeasurement;
	private ObjectProperty<Measurement> currentMeasurement;
	private boolean isAllChangeSaved;
	private static final Logger logger = LoggerFactory.getLogger(MainApp.class);

	public MeasurementData() {
		measurementList = FXCollections.observableArrayList();
		slectedMeasurement = new SimpleObjectProperty<>(null);
		currentMeasurement = new SimpleObjectProperty<>(new Measurement());
		isAllChangeSaved = false;
		listChangeListener();

		measurementList.add(new Measurement(new BarCodeReading("984151"), new GeometricMeasurement(98.0, 100.0, 101.0), new GeometricMeasurement(144.0, 150.0, 151.0),
				new GeometricMeasurement(198.0, 210.0, 201.0), new WeightMeasurement(1000.12), new SimpleObjectProperty<LocalDate>(LocalDate.now()),
				new SimpleObjectProperty<LocalTime>(LocalTime.now())));

		measurementList.add(new Measurement(new BarCodeReading("879481"), new GeometricMeasurement(99.0, 105.0, 101.0), new GeometricMeasurement(140.0, 155.0, 154.0),
				new GeometricMeasurement(193.0, 200.0, 189.0), new WeightMeasurement(999.3457), new SimpleObjectProperty<LocalDate>(LocalDate.now()),
				new SimpleObjectProperty<LocalTime>(LocalTime.now())));

		measurementList.add(new Measurement(new BarCodeReading("789789"), new GeometricMeasurement(97.0, 100.0, 111.0), new GeometricMeasurement(164.0, 154.0, 151.0),
				new GeometricMeasurement(188.0, 212.0, 201.0), new WeightMeasurement(855.80825), new SimpleObjectProperty<LocalDate>(LocalDate.now()),
				new SimpleObjectProperty<LocalTime>(LocalTime.now())));

	}

	public void newMeasurementDataFile() {
		measurementList.clear();
	}

	public void saveMeasurementDataToFile(Path filePath) {
		logger.info("Saving measurement data to file: {} ...", filePath);
		StringBuilder out = new StringBuilder();
		out.append(saveFileHeader());
		for (Measurement measurement : measurementList) {
			out.append(measurement.toString()).append("\r\n");
		}
		try (FileWriter fileWriter = new FileWriter(filePath.toString())) {
			fileWriter.write(out.toString());
		} catch (IOException e) {
			logger.error("Saving measurement data to file error", e);
		}
		isAllChangeSaved = true;
	}

	public void loadMeasurementDataFromFile(Path filePath) {
		logger.info("Loading measurement data from file: {} ...", filePath);
		ArrayList<String> in = new ArrayList<String>();
		try {
			in = (ArrayList<String>) Files.readAllLines(filePath);
		} catch (IOException e) {
			logger.error("Loading measurement data from file error", e);
		}
		measurementList.clear();
		toMeasurementList(in);
		isAllChangeSaved = true;
	}

	public void saveMeasurementListBackup() {
		measurementList.addListener(new ListChangeListener<Measurement>() {
			@Override
			public void onChanged(Change<? extends Measurement> c) {
				while (c.next()) {
					for (Measurement additem : c.getAddedSubList()) {
						save(additem);
					}
				}
			}

			private void save(Measurement measurement) {
				logger.info("Saving measurement backup to file...");
				StringBuffer registration = new StringBuffer();
				if (!new File(createPath()).exists()) {
					registration.append(saveFileHeader());
				}
				registration.append(measurement.toString()).append("\r\n");

				// try with resources block
				try (FileWriter fileWriter = new FileWriter(createPath(), true)) {
					fileWriter.write(registration.toString());
				} catch (IOException e) {
					logger.error("Save measurement data list backup to file error", e);
				}
			}

			private String createPath() {
				StringBuffer path = new StringBuffer();
				path.append("src/main/resources/mtw/concretecubesvalidator/Files/").append(LocalDate.now().getYear());
				if (LocalDate.now().getMonthValue() < 10)
					path.append(0).append(LocalDate.now().getMonthValue());
				else
					path.append(LocalDate.now().getMonthValue());
				if (LocalDate.now().getDayOfMonth() < 10)
					path.append(0).append(LocalDate.now().getDayOfMonth());
				else
					path.append(LocalDate.now().getDayOfMonth());
				path.append("_MeasurementListBackupFile.csv");

				return path.toString();
			}
		});
	}

	private String saveFileHeader() {
		StringBuffer sb = new StringBuffer();

		sb.append("Bar code").append(";");
		sb.append("Sample date").append(";");
		sb.append("Sample time").append(";");
		sb.append("Weight [g]").append(";");
		sb.append("Lenght [mm]").append(";");
		sb.append("Lenght sensor 1 [mm]").append(";");
		sb.append("Lenght sensor 2 [mm]").append(";");
		sb.append("Lenght sensor 3 [mm]").append(";");
		sb.append("Width [mm]").append(";");
		sb.append("Width sensor 1 [mm]").append(";");
		sb.append("Width sensor 2 [mm]").append(";");
		sb.append("Width sensor 3 [mm]").append(";");
		sb.append("Height [mm]").append(";");
		sb.append("Height sensor 1 [mm]").append(";");
		sb.append("Height sensor 2 [mm]").append(";");
		sb.append("Height sensor 3 [mm]").append(";");
		sb.append("\r\n");

		return sb.toString();
	}

	private void toMeasurementList(ArrayList<String> measurementStringArray) {
		measurementStringArray.remove(0);
		for (String line : measurementStringArray) {
			String[] l = line.split(";");

			BarCodeReading barCode = new BarCodeReading(l[0]);
			ObjectProperty<LocalDate> sampleDate = new SimpleObjectProperty<LocalDate>(DateTimeUtil.dateParse(l[1]));
			ObjectProperty<LocalTime> sampleTime = new SimpleObjectProperty<LocalTime>(DateTimeUtil.timeParse(l[2]));
			WeightMeasurement weight = new WeightMeasurement(Double.parseDouble(l[3].replaceAll(",", ".")));
			GeometricMeasurement lenght = new GeometricMeasurement(Double.parseDouble(l[5].replaceAll(",", ".")), Double.parseDouble(l[6].replaceAll(",", ".")),
					Double.parseDouble(l[7].replaceAll(",", ".")));
			GeometricMeasurement width = new GeometricMeasurement(Double.parseDouble(l[9].replaceAll(",", ".")), Double.parseDouble(l[10].replaceAll(",", ".")),
					Double.parseDouble(l[11].replaceAll(",", ".")));
			GeometricMeasurement height = new GeometricMeasurement(Double.parseDouble(l[13].replaceAll(",", ".")), Double.parseDouble(l[14].replaceAll(",", ".")),
					Double.parseDouble(l[15].replaceAll(",", ".")));

			Measurement measurement = new Measurement(barCode, lenght, width, height, weight, sampleDate, sampleTime);
			measurementList.add(measurement);

		}
	}

	private void listChangeListener() {
		measurementList.addListener(new ListChangeListener<Measurement>() {
			@Override
			public void onChanged(Change<? extends Measurement> c) {
				isAllChangeSaved = false;
			}
		});
	}

	public void addMeasurement(Measurement measurement) {
		measurementList.add(measurement);
	}

	public void removeSlectedMeasurement() {
		measurementList.remove(slectedMeasurement.get());
	}

	public ObservableList<Measurement> getMeasurementList() {
		return measurementList;
	}

	public ObjectProperty<Measurement> slectedMeasurementProperty() {
		return slectedMeasurement;
	}

	public Measurement getSlectedMeasurement() {
		return slectedMeasurement.get();
	}

	public void setSlectedMeasurement(Measurement measurement) {
		slectedMeasurement.set(measurement);
	}

	public ObjectProperty<Measurement> currentMeasurementProperty() {
		return currentMeasurement;
	}

	public Measurement getCurrentMeasurement() {
		return currentMeasurement.get();
	}

	public void setCurrentMeasurement(Measurement measurement) {
		currentMeasurement.set(measurement);
	}

	public boolean getIsAllChangeSaved() {
		return isAllChangeSaved;
	}
}
