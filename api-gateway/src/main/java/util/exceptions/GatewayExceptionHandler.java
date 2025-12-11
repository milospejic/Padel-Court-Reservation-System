package util.exceptions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Configuration
public class GatewayExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();

        if (ex instanceof NoDataFoundException) {
            status = HttpStatus.NOT_FOUND;
        } else if (ex instanceof InvalidRequestException) {
            status = HttpStatus.BAD_REQUEST;
        } else if (ex instanceof EntityAlreadyExistsException) {
            status = HttpStatus.CONFLICT;
        } else if (ex instanceof ForbidenActionException) {
            status = HttpStatus.FORBIDDEN;
        } 
        else if (ex instanceof ResponseStatusException) {
            status = HttpStatus.valueOf(((ResponseStatusException) ex).getStatusCode().value());
            message = ((ResponseStatusException) ex).getReason();
        }

        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ExceptionModel errorResponse = new ExceptionModel(
            status.value(),
            status,
            message
        );

        return exchange.getResponse().writeWith(Mono.fromCallable(() -> {
            DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
            try {
                return bufferFactory.wrap(objectMapper.writeValueAsBytes(errorResponse));
            } catch (JsonProcessingException e) {
                return bufferFactory.wrap(new byte[0]);
            }
        }));
    }
}