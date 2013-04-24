package ErrorsAndExceptions;

public class OutOfPOSException extends Error {
	OutOfPOSException() {
		super();
	}

	public OutOfPOSException(String msg) {
		super(msg);
	}
}
