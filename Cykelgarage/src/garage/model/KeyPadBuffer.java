package garage.model;

import garage.hardware.interfaces.*;

public class KeyPadBuffer implements PinCodeTerminalListener {
	
	private static int WAITING_TIME = 4;
	
	private KeyPadBufferListener manager;
	
	private TimerThread timer = null;
	
	private int bufferSize;
	private char[] buffer;
	private int expectedInput;
	
	public KeyPadBuffer(int bufferSize, PinCodeTerminal pinCodeTerminal, KeyPadBufferListener manager) {
		this.manager = manager;
		pinCodeTerminal.register(this);
		this.bufferSize = bufferSize;
		buffer = new char[bufferSize];
		expectedInput = bufferSize;
	}
	
	/**
	 * Set the expected number of chars for the buffer.
	 * @param n Number of chars to expect. 0 <= m < BUFFER_SIZE
	 */
	public void setExpectedInput(int n) {
		if (n < 0) {
			throw new IllegalArgumentException(
					"Cannot set expected input to a negative value");
		}
		if (n > bufferSize) {
			throw new IllegalArgumentException(
					"Cannot set expect input to a number larger than BUFFER_SIZE");
		}
		expectedInput = n;
	}
	
	/**
	 * Method called by the KeyPad interface when a button
	 * is pressed.
	 * @param c Value of button that was pressed.
	 */
	public void entryCharacter(char c) {
		buffer[buffer.length] = c;
		if (buffer.length >= expectedInput) {
			manager.recieveBuffer(buffer.toString());
			newBuffer();
		}
		newTimer();
	}
	
	private void newTimer() {
		timer = new TimerThread(WAITING_TIME, this);
	}
	
	private void newBuffer() {
		buffer = new char[bufferSize];
		expectedInput = bufferSize;
	}
	
	
	private void timerFinnished(TimerThread caller) {
		if (timer == null || ! timer.equals(caller)) {
			return;
		}
		newBuffer();
		//TODO Red led lights up
	}
	
	private class TimerThread extends Thread implements Runnable {
		private KeyPadBuffer manager;
		private long time;
		private TimerThread(int seconds, KeyPadBuffer manager) {
			this.manager = manager;
			time = seconds * 1000l;
			this.start();
		}
		
		public void run() {
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				// TODO Logg stacktrace?
			}
			manager.timerFinnished(this);
		}
	}
}
