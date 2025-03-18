package solutions.onz.services.imagengine.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.domain.Authority;
import solutions.onz.services.imagengine.repository.AuthorityRepository;
import solutions.onz.services.imagengine.utils.web.HeaderUtil;
import solutions.onz.services.imagengine.utils.web.exception.BadRequestAlertException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/authorities")
public class AuthorityController {
    private static final String ENTITY_NAME = "adminAuthority";

    @Value("${spring.application.name}")
    private String applicationName;

    private final AuthorityRepository authorityRepository;


    public AuthorityController(AuthorityRepository authorityRepository) {
        this.authorityRepository = authorityRepository;
    }

    /**
     * {@code POST  /api/authorities} : Create a new authority.
     *
     * @param authority the authority to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new authority, or with status {@code 400 (Bad Request)} if the authority has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @Operation(summary = "Create a new authority", description = "Create a new authority", tags = {"Authority API"}, operationId = "createAuthority")
    @PostMapping("")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<Authority>> createAuthority(@Valid @RequestBody Authority authority) throws URISyntaxException {
        log.debug("REST request to save Authority : {}", authority);
        return authorityRepository
                .existsById(authority.getName())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new BadRequestAlertException("authority already exists", ENTITY_NAME, "idexists"));
                    }
                    return authorityRepository
                            .save(authority)
                            .map(result -> {
                                try {
                                    return ResponseEntity.created(new URI("/api/authorities/" + result.getName()))
                                            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, result.getName()))
                                            .body(result);
                                } catch (URISyntaxException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                });
    }

    /**
     * {@code GET  /authorities} : get all the authorities.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of authorities in body.
     */
    @Operation(summary = "Get all authorities", description = "Get all authorities", tags = {"Authority API"}, operationId = "getAllAuthorities")
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<List<Authority>> getAllAuthorities() {
        log.debug("REST request to get all Authorities");
        return authorityRepository.findAll().collectList();
    }

    /**
     * {@code GET  /authorities} : get all the authorities as a stream.
     * @return the {@link Flux} of authorities.
     */
    @Operation(summary = "Get all authorities as a stream", description = "Get all authorities as a stream", tags = {"Authority API"}, operationId = "getAllAuthoritiesAsStream")
    @GetMapping(value = "", produces = MediaType.APPLICATION_NDJSON_VALUE)
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Flux<Authority> getAllAuthoritiesAsStream() {
        log.debug("REST request to get all Authorities as a stream");
        return authorityRepository.findAll();
    }

    /**
     * {@code GET  /authorities/:id} : get the "id" authority.
     *
     * @param id the id of the authority to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the authority, or with status {@code 404 (Not Found)}.
     */
    @Operation(summary = "Get a authority by ID", description = "Get a authority by ID", tags = {"Authority API"}, operationId = "getAuthorityById")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<Authority>> getAuthority(@PathVariable("id") String id) {
        log.debug("REST request to get Authority : {}", id);
        Mono<Authority> authority = authorityRepository.findById(id);
        return authority.switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND))).map(ResponseEntity::ok);
    }

    /**
     * {@code DELETE  /authorities/:id} : delete the "id" authority.
     *
     * @param id the id of the authority to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(summary = "Delete a authority by ID", description = "Delete a authority by ID", tags = {"Authority API"}, operationId = "deleteAuthority")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')")
    public Mono<ResponseEntity<Void>> deleteAuthority(@PathVariable("id") String id) {
        log.debug("REST request to delete Authority : {}", id);
        return authorityRepository
                .deleteById(id)
                .then(
                        Mono.just(
                                ResponseEntity.noContent()
                                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id))
                                        .build()
                        )
                );
    }

}
