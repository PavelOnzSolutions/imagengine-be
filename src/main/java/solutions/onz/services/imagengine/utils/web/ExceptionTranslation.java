package solutions.onz.services.imagengine.utils.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public interface ExceptionTranslation {
    Mono<ResponseEntity<Object>> handleAnyException(Throwable var1, ServerWebExchange var2);
}
