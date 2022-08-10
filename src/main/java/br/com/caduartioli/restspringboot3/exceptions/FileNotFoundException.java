package br.com.caduartioli.restspringboot3.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serial;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FileNotFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public FileNotFoundException(String ex) {
        super(ex);
    }

    public FileNotFoundException(String ex, Throwable cause) {
        super(ex, cause);
    }
}
