package solutions.onz.services.imagengine.web.rest;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.ForwardedHeaderUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import solutions.onz.services.imagengine.config.Constants;
import solutions.onz.services.imagengine.domain.User;
import solutions.onz.services.imagengine.identity.AuthoritiesConstants;
import solutions.onz.services.imagengine.repository.UserRepository;
import solutions.onz.services.imagengine.services.MailService;
import solutions.onz.services.imagengine.services.UserService;
import solutions.onz.services.imagengine.services.dto.AdminUserDTO;
import solutions.onz.services.imagengine.utils.web.HeaderUtil;
import solutions.onz.services.imagengine.utils.web.PaginationUtil;
import solutions.onz.services.imagengine.utils.web.exception.BadRequestAlertException;
import solutions.onz.services.imagengine.utils.web.exception.EmailAlreadyUsedException;
import solutions.onz.services.imagengine.utils.web.exception.LoginAlreadyUsedException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {
    private static final List<String> ALLOWED_ORDERED_PROPERTIES = Collections.unmodifiableList(
            Arrays.asList(
                    "id",
                    "login",
                    "firstName",
                    "lastName",
                    "email",
                    "imageUrl",
                    "activated",
                    "langKey",
                    "createdBy",
                    "createdDate",
                    "lastModifiedBy",
                    "lastModifiedDate"
            )
    );

    @Value("${spring.application.name}")
    private String applicationName;

    private final UserService userService;
    private final UserRepository userRepository;
    private final MailService mailService;


    public UserController(
            UserService userService,
            UserRepository userRepository,
            MailService mailService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.mailService = mailService;
    }

    /**
     * {@code POST  /api/users}  : Creates a new user.
     * <p>
     * Creates a new user if the login and email are not already used, and sends an
     * mail with an activation link.
     * The user needs to be activated on creation.
     *
     * @param userDTO the user to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new user, or with status {@code 400 (Bad Request)} if the login or email is already in use.
     * @throws BadRequestAlertException {@code 400 (Bad Request)} if the login or email is already in use.
     */
    @Operation(summary = "Create a new user", description = "Create a new user", tags = {"User Management API"}, operationId = "createUser")
    @PostMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<User>> createUser(@Valid @RequestBody AdminUserDTO userDTO) {
        log.debug("REST request to save User : {}", userDTO);

        if (userDTO.getId() != null) {
            throw new BadRequestAlertException("A new user cannot already have an ID", "userManagement", "idexists");
            // Lowercase the user login before comparing with database
        }
        return userRepository
                .findOneByLogin(userDTO.getLogin().toLowerCase())
                .hasElement()
                .flatMap(loginExists -> {
                    if (Boolean.TRUE.equals(loginExists)) {
                        return Mono.error(new LoginAlreadyUsedException());
                    }
                    return userRepository.findOneByEmailIgnoreCase(userDTO.getEmail());
                })
                .hasElement()
                .flatMap(emailExists -> {
                    if (Boolean.TRUE.equals(emailExists)) {
                        return Mono.error(new EmailAlreadyUsedException());
                    }
                    return userService.createUser(userDTO);
                })
                .doOnSuccess(mailService::sendCreationEmail)
                .map(user -> {
                    try {
                        return ResponseEntity.created(new URI("/api/api/users/" + user.getLogin()))
                                .headers(
                                        HeaderUtil.createAlert(applicationName, "A user is created with identifier " + user.getLogin(), user.getLogin())
                                )
                                .body(user);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * {@code PUT /api/users} : Updates an existing User.
     *
     * @param userDTO the user to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated user.
     * @throws EmailAlreadyUsedException {@code 400 (Bad Request)} if the email is already in use.
     * @throws LoginAlreadyUsedException {@code 400 (Bad Request)} if the login is already in use.
     */
    @Operation(summary = "Update an existing user", description = "Update an existing user", tags = {"User Management API"}, operationId = "updateUser")
    @PutMapping({"", "/users/{login}"})
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<AdminUserDTO>> updateUser(
            @PathVariable(name = "login", required = false) @Pattern(regexp = Constants.LOGIN_REGEX) String login,
            @Valid @RequestBody AdminUserDTO userDTO
    ) {
        log.debug("REST request to update User : {}", userDTO);
        return userRepository
                .findOneByEmailIgnoreCase(userDTO.getEmail())
                .filter(user -> !user.getId().equals(userDTO.getId()))
                .hasElement()
                .flatMap(emailExists -> {
                    if (Boolean.TRUE.equals(emailExists)) {
                        return Mono.error(new EmailAlreadyUsedException());
                    }
                    return userRepository.findOneByLogin(userDTO.getLogin().toLowerCase());
                })
                .filter(user -> !user.getId().equals(userDTO.getId()))
                .hasElement()
                .flatMap(loginExists -> {
                    if (Boolean.TRUE.equals(loginExists)) {
                        return Mono.error(new LoginAlreadyUsedException());
                    }
                    return userService.updateUser(userDTO);
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                .map(user ->
                        ResponseEntity.ok()
                                .headers(
                                        HeaderUtil.createAlert(
                                                applicationName,
                                                "A user is updated with identifier " + userDTO.getLogin(),
                                                userDTO.getLogin()
                                        )
                                )
                                .body(user)
                );
    }

    /**
     * {@code GET /api/users} : get all users with all the details - calling this are only allowed for the administrators.
     *
     * @param request  a {@link ServerHttpRequest} request.
     * @param pageable the pagination information.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body all users.
     */
    @Operation(summary = "Get all users", description = "Get all users", tags = {"User Management API"}, operationId = "getAllUsers")
    @GetMapping("")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<Flux<AdminUserDTO>>> getAllUsers(
            @org.springdoc.core.annotations.ParameterObject ServerHttpRequest request,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        log.debug("REST request to get all User for an admin");
        if (!onlyContainsAllowedProperties(pageable)) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        return userService
                .countManagedUsers()
                .map(total -> new PageImpl<>(new ArrayList<>(), pageable, total))
                .map(page ->
                        PaginationUtil.generatePaginationHttpHeaders(
                                ForwardedHeaderUtils.adaptFromForwardedHeaders(request.getURI(), request.getHeaders()),
                                page
                        )
                )
                .map(headers -> ResponseEntity.ok().headers(headers).body(userService.getAllManagedUsers(pageable)));
    }

    private boolean onlyContainsAllowedProperties(Pageable pageable) {
        return pageable.getSort().stream().map(Sort.Order::getProperty).allMatch(ALLOWED_ORDERED_PROPERTIES::contains);
    }

    /**
     * {@code GET /api/users/stream} : get all users as a stream - calling this are only allowed for the administrators.
     *
     * @return the {@link Flux} of all users.
     */
    @Operation(summary = "Get all users as a stream", description = "Get all users as a stream", tags = {"User Management API"}, operationId = "getAllUsersAsStream")
    @GetMapping("/stream")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Flux<AdminUserDTO> getAllUsersAsStream() {
        log.debug("REST request to get all User for an admin as a stream");
        return userService.getAllManagedUsers();
    }

    /**
     * {@code GET /api/users/:login} : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the "login" user, or with status {@code 404 (Not Found)}.
     */
    @Operation(summary = "Get a user by login", description = "Get a user by login", tags = {"User Management API"}, operationId = "getUser")
    @GetMapping("/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<AdminUserDTO> getUser(@PathVariable("login") String login) {
        log.debug("REST request to get User : {}", login);
        return userService
                .getUserWithAuthoritiesByLogin(login)
                .map(AdminUserDTO::new)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)));
    }

    /**
     * {@code DELETE /api/users/:login} : delete the "login" User.
     *
     * @param login the login of the user to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @Operation(summary = "Delete a user by login", description = "Delete a user by login", tags = {"User Management API"}, operationId = "deleteUser")
    @DeleteMapping("/{login}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable("login") @Pattern(regexp = Constants.LOGIN_REGEX) String login) {
        log.debug("REST request to delete User: {}", login);
        return userService
                .deleteUser(login)
                .then(
                        Mono.just(
                                ResponseEntity.noContent()
                                        .headers(HeaderUtil.createAlert(applicationName, "A user is deleted with identifier " + login, login))
                                        .build()
                        )
                );
    }
}
