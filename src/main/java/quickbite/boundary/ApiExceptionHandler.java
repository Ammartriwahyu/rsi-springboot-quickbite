package quickbite.boundary;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import quickbite.boundary.dto.ApiError;
import quickbite.controller.exception.PaymentFailedException;
import quickbite.controller.exception.PesananTidakBisaDibatalkanException;
import quickbite.controller.exception.ResourceNotFoundException;
import quickbite.controller.exception.StokTidakCukupException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(StokTidakCukupException.class)
    public ResponseEntity<ApiError> stokHabis(
            StokTidakCukupException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("STOK_HABIS",
                        e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> tidakDitemukan(
            ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiError("NOT_FOUND",
                        e.getMessage()));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ApiError> gagalBayar(
            PaymentFailedException e) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new ApiError("PAYMENT_FAILED",
                        e.getMessage()));
    }

    @ExceptionHandler(PesananTidakBisaDibatalkanException.class)
    public ResponseEntity<ApiError> tidakBisaDibatalkan(
            PesananTidakBisaDibatalkanException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ApiError("TIDAK_BISA_DIBATALKAN",
                        e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validasiGagal(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fe : e.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ApiError("VALIDASI_GAGAL",
                        "Validasi input gagal", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> jsonTidakValid(
            HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest()
                .body(new ApiError("FORMAT_TIDAK_VALID",
                        "Body request bukan JSON yang valid"));
    }
}
