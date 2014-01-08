package ErrorsAndExceptions;

public class OutOfPOSException extends Error {
	public OutOfPOSException() {
		super();
	}

	public OutOfPOSException(String msg) {
		super(msg);
	}
}
