package faas;

public class JobFailedException extends Exception {
    private final int code;
    private final String message;

    public JobFailedException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int code() {
        return code;
    }

    public String message() {
        return message;
    }
}
