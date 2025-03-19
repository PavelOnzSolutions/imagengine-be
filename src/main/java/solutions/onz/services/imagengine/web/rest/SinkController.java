package solutions.onz.services.imagengine.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/sink")
public class SinkController {
    @Operation(summary = "Download a resulting image", description = "Download link for a resulting image with limited validity", tags = {"Download Sink API"}, operationId = "downloadImage")
    @GetMapping(value = "/{token}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Resource>> downloadImage(@PathVariable String token) {
        return Mono.empty();
    }
}
