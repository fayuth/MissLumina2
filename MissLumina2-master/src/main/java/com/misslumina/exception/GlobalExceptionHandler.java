package com.misslumina.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(String mensaje, LocalDateTime fechaHora) {
    }

    @ExceptionHandler(ProductoNoEncontradoException.class)
    public ResponseEntity<ErrorResponse> productoNoEncontrado(ProductoNoEncontradoException e) {
        return responder(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> stockInsuficiente(StockInsuficienteException e) {
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<ErrorResponse> credenciales(CredencialesInvalidasException e) {
        return responder(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(PedidoDuplicadoException.class)
    public ResponseEntity<ErrorResponse> pedidoDuplicado(PedidoDuplicadoException e) {
        log.error("Pedido duplicado: {}", e.getMessage(), e);
        return responder(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ResponseEntity<ErrorResponse> datosInvalidos(DatosInvalidosException e) {
        return responder(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(ErrorBaseDatosException.class)
    public ResponseEntity<ErrorResponse> errorBaseDatos(ErrorBaseDatosException e) {
        log.error("Error de base de datos", e);
        return responder(HttpStatus.SERVICE_UNAVAILABLE, e.getMessage());
    }

    @ExceptionHandler(TransaccionException.class)
    public ResponseEntity<ErrorResponse> transaccion(TransaccionException e) {
        log.error("Error al confirmar una transaccion", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> dataAccess(DataAccessException e) {
        log.error("Error de acceso a datos", e);
        return responder(HttpStatus.SERVICE_UNAVAILABLE,
                "No fue posible conectar con PostgreSQL. Verifique que el servidor este activo.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> errorGeneral(Exception e) {
        log.error("Error no controlado", e);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrio un error inesperado. Consulte el log de la aplicacion.");
    }

    private ResponseEntity<ErrorResponse> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado)
                .body(new ErrorResponse(mensaje, LocalDateTime.now()));
    }
}
