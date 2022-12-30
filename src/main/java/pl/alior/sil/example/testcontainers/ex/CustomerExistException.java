package pl.alior.sil.example.testcontainers.ex;

public class CustomerExistException extends GlobalException {
    private static final String MSG = "Customer with the given pesel already exists";

    public CustomerExistException() {
        super(MSG);
    }
}
