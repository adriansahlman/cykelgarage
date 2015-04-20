package garage.logging;

public abstract class LogAccess {
	private final static String EVENT_LOG_FILENAME = "EventLog.txt";
	private final static String ERROR_LOG_FILENAME = "ErrorLog.txt";

	private static Logger eventLogger;
	private static Logger errorLogger;
	
	public static Logger event() {
		return (eventLogger != null) ? eventLogger : (eventLogger = new Logger(EVENT_LOG_FILENAME, false));
	}
	
	public static Logger error() {
		return (errorLogger != null) ? errorLogger : (errorLogger = new Logger(ERROR_LOG_FILENAME, false));
	}
	
	public static void initialize(boolean showLogs) {
		eventLogger = new Logger(EVENT_LOG_FILENAME, showLogs);
		errorLogger = new Logger(ERROR_LOG_FILENAME, showLogs);
	}
}