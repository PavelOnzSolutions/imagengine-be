package solutions.onz.services.imagengine.web.rest;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.domain.DeepLink;
import solutions.onz.services.imagengine.services.DeepLinkService;

@Slf4j
@RestController
@RequestMapping(value = "/api/deep-link", name = "DeepLink")
public class DeepLinkController {
    private final DeepLinkService deepLinkService;

    public DeepLinkController(DeepLinkService deepLinkService) {
        this.deepLinkService = deepLinkService;
    }

    /**
     * Create a new Deep Link
     * @param path
     * @return {@link Mono<ResponseEntity<DeepLink>>}
     */
    @Operation(
            summary = "Create a new Deep Link",
            description = "Create a new Deep Link",
            method = "PUT",
            tags = { "Deep Link API" }
    )
    @PutMapping(value = "")
    public Mono<ResponseEntity<DeepLink>> createDeepLink(@RequestParam String path) {
        return deepLinkService.createLink(path)
                .map(ResponseEntity::ok);
    }

    /**
     * Get a Deep Link by hash or a 404 if none found or expired
     * @param hash
     * @return {@link Mono<ResponseEntity<DeepLink>>}
     */
    @Operation(
            summary = "Get a Deep Link by hash.",
            description = "Get a Deep Link by hash or a 404 if none found or expired.",
            method = "GET",
            tags = { "Deep Link API" }
    )
    @GetMapping("/{hash}")
    public Mono<ResponseEntity<DeepLink>> getDeepLink(@PathVariable String hash) {
        return deepLinkService.getDeepLink(hash)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
