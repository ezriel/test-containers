package pl.alior.sil.example.testcontainers.ex;

public class CustomerNotFoundException extends GlobalException {

    private static final String MSG = "Customer %d not found";

    public CustomerNotFoundException(Long customerId) {
        super(String.format(MSG, customerId));
    }
}
