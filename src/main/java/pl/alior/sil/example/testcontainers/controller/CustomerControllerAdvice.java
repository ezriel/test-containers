package pl.alior.sil.example.testcontainers.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.alior.sil.example.testcontainers.data.model.ErrorMessage;
import pl.alior.sil.example.testcontainers.ex.SearchException;

@RestControllerAdvice
public class CustomerControllerAdvice {
    @ExceptionHandler(value = {SearchException.class})
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorMessage resourceNotFoundException(SearchException ex) {
        return new ErrorMessage("100100", ex.getMessage());
    }
}
