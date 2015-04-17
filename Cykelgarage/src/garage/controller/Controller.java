package garage.controller;

import java.util.List;

import garage.database.Database;
import garage.model.*;
import garage.hardware.interfaces.*;

/**
 * This class is the controller of a bicycle garage. It handles
 * hardware input and output through hardware interfaces and
 * hardware extensions.
 */
public class Controller implements KeyPadBufferListener, BarcodeReaderListener {
	private static int USERNAME_LENGTH = 10;
	private static int PASSWORD_LENGTH = 4;
	private static int UNLOCK_TIME = 10;
	private static int GREEN_LED_TIME = 1;
	private static int RED_LED_TIME = 1;
	
	private ElectronicLock frontDoorLock;
	
	private ElectronicLock bikeExitDoorLock;
	
	private PinCodeTerminal pinCodeTerminal;
	
	private BarcodePrinter barcodePrinter;
	
	Database database;
	
	KeyPadBuffer buffer;
	
	private String storedUsername = "";

	
	public Controller(Database database, ElectronicLock frontDoorLock,
			ElectronicLock bikeExitDoorLock, PinCodeTerminal pinCodeTerminal,
			BarcodeReader frontDoorBarcodeReader, BarcodeReader bikeExitDoorBarcodeReader,
			BarcodePrinter barcodePrinter) {
		this.database = database;
		
		this.frontDoorLock = frontDoorLock;
		this.bikeExitDoorLock = bikeExitDoorLock;
		this.pinCodeTerminal = pinCodeTerminal;
		frontDoorBarcodeReader.register(this);
		bikeExitDoorBarcodeReader.register(this);
		this.barcodePrinter = barcodePrinter;
		
		buffer = new KeyPadBuffer(USERNAME_LENGTH, pinCodeTerminal, this);
	}
		
	
	/**
	 * Recieve new input from the buffer.
	 * @param input Input to be recieved from the buffer.
	 */
	public void recieveBuffer(String input) {
		if (input.length() == USERNAME_LENGTH) {
			newUsernameInput(input);
		} else {
			newPasswordInput(input);
		}
	}
	
	/**
	 * Method handles new username input. If username exists, light
	 * up green LED and store the username. If username does not exist,
	 * light up red LED.
	 * @param username Username input.
	 */
	private void newUsernameInput(String username) {
		if (database.userExists(username)) {
			storedUsername = username;
			buffer.setExpectedInput(PASSWORD_LENGTH);
			pinCodeTerminal.lightLED(PinCodeTerminal.GREEN_LED, GREEN_LED_TIME);
		} else {
			storedUsername = "";
			pinCodeTerminal.lightLED(PinCodeTerminal.RED_LED, RED_LED_TIME);
		}
	}
	
	/**
	 * Method handles new password input. If password is correct, unlock
	 * front door. If password is incorrect, light up red LED.
	 * @param password String to be compared to actual password for user.
	 */
	private void newPasswordInput(String password) {
		String correctPassword = database.getPassword(storedUsername);
		if (correctPassword != null && correctPassword.equals(password)) {
			storedUsername = "";
			frontDoorLock.open(UNLOCK_TIME);
			pinCodeTerminal.lightLED(PinCodeTerminal.GREEN_LED, GREEN_LED_TIME);
			//TODO Register checkIn to database
		} else {
			storedUsername = "";
			pinCodeTerminal.lightLED(PinCodeTerminal.RED_LED, RED_LED_TIME);
		}
	}

	@Override
	public void entryBarcode(String code) {
		if (database.bikeConnected(code)) {
			frontDoorLock.open(UNLOCK_TIME);
			//TODO Register checkIn to database
		}
	}

	@Override
	public void exitBarcode(String code) {
		String user = database.getBikeOwner(code);
		if (user != null && database.userCheckedIn(user)) {
			bikeExitDoorLock.open(UNLOCK_TIME);
			//TODO Remove checkIn-status for user. Should no longer be checked in.
		}
	}
	
	/**
     * Create a new user account.
     * @param username Username for the account.
     * @param password Password for the account.
     * @return True if account creation was successful, otherwise false.
     */
	public boolean newUser(String username, String password) {
		return database.createUser(username, password);
	}
	
	/**
     * Connect a new bike to this garage and print its new
     * barcode if successful.
     * @param username The username associated with the bike.
     * @return True if successful, otherwise false.
     */
	public boolean connectNewBike(String username) {
		String bikeID = database.connectNewBike(username);
		if (bikeID == null) {
			return false;
		}
		barcodePrinter.printBarcode(bikeID);
		return true;
	}
	
	/**
	 * Get all Bike ID's of bikes belonging to a specific user.
	 * @param username Username of user whose bikes are requested.
	 * If null method will return all bikes in the bike-garage.
	 * @return List of bikes ID's.
	 */
	public List<String> getBikes(String username) {
		return database.getBikes(username);
	}
	
	/**
	 * Get all usernames of users registered to this garage.
	 * @return List of usernames.
	 */
	public List<String> getUsers() {
		return database.getUsers();
	}
	
	/**
     * Search for userinfo by username.
     * @param username Username to look for.
     * @return List of Strings with search result.
     */
    public List<String> searchUsername(String username) {
		return database.searchUsername(username);
	}
	
    /**
     * Search for bikeinfo by bike ID.
     * @param bikeID Bike ID to look for.
     * @return List of String with search result.
     */
	public List<String> searchBikeID(String bikeID) {
		return database.searchBikeID(bikeID);
	}
	
}
