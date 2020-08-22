package core.di.factory.abnormal;

public class CircularReferenceException extends IllegalArgumentException {

    public CircularReferenceException(String message) {
        super(message);
    }
}
