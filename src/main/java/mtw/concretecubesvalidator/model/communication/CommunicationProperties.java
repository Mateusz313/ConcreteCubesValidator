package mtw.concretecubesvalidator.model.communication;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommunicationProperties {

	private String name;
	private String serverIpAdress;
	private int tcpPort;
	private boolean fixedNetworkInterface;
	private String networkInterfaceIpAdress;
	static final Logger logger = LoggerFactory.getLogger(PlcCommunication.class);

	public CommunicationProperties() {
		readDefaultProperties();
	}

	public boolean overwriteDefaultProperties() {

		File file;
		URI uri;
		boolean overwrittingOK = false;

		try {
			uri = CommunicationProperties.class.getResource("files/comunicationProperties.csv").toURI();
		} catch (URISyntaxException e) {
			uri = null;
			logger.error("Overwrite default properties error - invalid URI ", e);
		}

		if (uri != null) {
			file = new File(Paths.get(uri).toString());

			ArrayList<String> writing = new ArrayList<String>();
			writing.add(name);
			writing.add(serverIpAdress);
			writing.add(String.valueOf(tcpPort));
			writing.add(String.valueOf(fixedNetworkInterface));
			writing.add(networkInterfaceIpAdress);

			try {
				Files.write(file.toPath(), writing);
				overwrittingOK = true;
			} catch (IOException e) {
				overwrittingOK = false;
				logger.error("Overwrite default properties error - writting to file error", e);
			}
		} else {
			overwrittingOK = false;
		}
		return overwrittingOK;
	}

	private boolean readDefaultProperties() {
		File file;
		URI uri;
		boolean readingOK = false;

		try {
			uri = CommunicationProperties.class.getResource("files/comunicationProperties.csv").toURI();
		} catch (URISyntaxException e) {
			uri = null;
			logger.error("Read default properties error - invalid URI ", e);
		}

		if (uri != null) {
			file = new File(Paths.get(uri).toString());
			ArrayList<String> reading = new ArrayList<String>();
			try {
				reading = (ArrayList<String>) Files.readAllLines(file.toPath());
				readingOK = true;
			} catch (IOException e) {
				readingOK = false;
				logger.error("Read default connection properties error", e);
			}

			if (readingOK == true && reading.size() >= 5) {
				if (validateInput(reading.get(0), reading.get(1), reading.get(2), reading.get(3), reading.get(4)).equals("")) {
					name = reading.get(0);
					serverIpAdress = reading.get(1);
					tcpPort = Integer.parseInt(reading.get(2));
					if (reading.get(3).equals("true")) {
						fixedNetworkInterface = true;
						networkInterfaceIpAdress = reading.get(4);
					} else {
						fixedNetworkInterface = false;
						networkInterfaceIpAdress = "0.0.0.0";
					}
				}
			} else {
				name = "default properties";
				serverIpAdress = "192.168.0.1";
				tcpPort = 502;
				fixedNetworkInterface = true;
				networkInterfaceIpAdress = "192.168.0.99";
				readingOK = false;
			}
		} else {
			name = "default properties";
			serverIpAdress = "192.168.0.1";
			tcpPort = 502;
			fixedNetworkInterface = true;
			networkInterfaceIpAdress = "192.168.0.99";
			readingOK = false;
		}
		return readingOK;
	}

	public String validateInput(String nameFiled, String serverIpAdressField, String tcpPortFiled, String fixedNetworkInterface, String networkInterfaceIpAdress) {
		String validationResult = "";

		if (nameFiled == null || nameFiled.length() == 0) {
			validationResult = "No valid name!\n";
		} else if (!validateIpAdress(serverIpAdressField).equals("")) {
			validationResult = "Server IP Adress: " + validateIpAdress(serverIpAdressField);
		} else if (!fixedNetworkInterface.equals("false") && !fixedNetworkInterface.equals("true")) {
			validationResult = "No valid fixed network interface field (must be 'true' or 'false')! \n";
		} else if (fixedNetworkInterface.equals("true") && !validateIpAdress(networkInterfaceIpAdress).equals("")) {
			validationResult = "Network interface IP Adress: " + validateIpAdress(networkInterfaceIpAdress);
		} else if (tcpPortFiled == null || tcpPortFiled.length() == 0) {
			validationResult = "No valid TCP Port!";
		} else {
			try {
				Integer.parseInt(tcpPortFiled);
			} catch (NumberFormatException e) {
				validationResult = "No valid TCP Port (must be an integer)!";
				logger.error("Validation properties error - No valid TCP Port", e);
			}
		}
		return validationResult;
	}

	private String validateIpAdress(String ipAdress) {
		String validationResoult = "";
		if (ipAdress == null || ipAdress.length() < 7) {
			validationResoult = "No valid IP Adress!\n";
		} else {
			String[] ipAdressTable = new String[] { "", "", "", "", "" };
			int j = 0;
			for (int i = 0; i < ipAdress.length(); i++) {
				if (ipAdress.charAt(i) == '.') {
					j++;
					if (j > 3) {
						validationResoult = "No valid IP Adress (must be in format 'x.x.x.x')!\n";
						break;
					}
				} else if (j < 4) {
					ipAdressTable[j] += ipAdress.charAt(i);
				} else {
					validationResoult = "No valid IP Adress (must be in format 'x.x.x.x')!\n";
					break;
				}
			}
			if (validationResoult.equals("")) {
				for (int i = 0; i < 4; i++) {
					if (ipAdressTable[i].length() > 3) {
						validationResoult = "No valid IP Adress must be 3-digits integers separates by '.')!\n";
						break;
					} else {
						try {
							Integer.parseInt(ipAdressTable[i]);
						} catch (NumberFormatException e) {
							validationResoult = "No valid IP Adress (must be integers separates by '.')!\n";
							logger.error("Validation properties error - No valid IP Adress", e);
						}
					}
				}
			}
		}
		return validationResoult;
	}

	@Override
	public String toString() {
		return "CommunicationProperties [name=" + name + ", serverIpAdress=" + serverIpAdress + ", tcpPort=" + tcpPort + ", fixedNetworkInterface=" + fixedNetworkInterface
				+ ", networkInterfaceIpAdress=" + networkInterfaceIpAdress + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getServerIpAdress() {
		return serverIpAdress;
	}

	public void setServerIpAdress(String serverIpAdress) {
		this.serverIpAdress = serverIpAdress;
	}

	public int getTcpPort() {
		return tcpPort;
	}

	public void setTcpPort(int tcpPort) {
		this.tcpPort = tcpPort;
	}

	public boolean isFixedNetworkInterface() {
		return fixedNetworkInterface;
	}

	public void setFixedNetworkInterface(boolean fixedNetworkInterface) {
		this.fixedNetworkInterface = fixedNetworkInterface;
	}

	public String getNetworkInterfaceIpAdress() {
		return networkInterfaceIpAdress;
	}

	public void setNetworkInterfaceIpAdress(String networkInterfaceIpAdress) {
		this.networkInterfaceIpAdress = networkInterfaceIpAdress;
	}

}
