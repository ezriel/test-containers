package pl.alior.sil.example.testcontainers.ex;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GlobalException extends RuntimeException {
    private String message;

    public GlobalException(String message) {
        super(message);
        this.message = message;
    }
}
