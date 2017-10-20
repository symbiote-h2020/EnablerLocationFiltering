package eu.h2020.symbiote.enablerlogic;

public class EnablerLogicException extends RuntimeException {
    private static final long serialVersionUID = -4267824772789523250L;

    public EnablerLogicException(String message, Throwable cause) {
        super(message, cause);
    }

    public EnablerLogicException(String message) {
        super(message);
    }
}
