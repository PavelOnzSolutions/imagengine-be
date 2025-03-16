package solutions.onz.services.imagengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.*;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter;
import org.springframework.security.web.server.header.XFrameOptionsServerHttpHeadersWriter;
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher;
import solutions.onz.services.imagengine.identity.AuthoritiesConstants;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers;

@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfiguration {
    private final ApplicationProperties properties;

    public SecurityConfiguration(ApplicationProperties properties) {
        this.properties = properties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveAuthenticationManager reactiveAuthenticationManager(ReactiveUserDetailsService userDetailsService) {
        UserDetailsRepositoryReactiveAuthenticationManager authenticationManager = new UserDetailsRepositoryReactiveAuthenticationManager(
                userDetailsService
        );
        authenticationManager.setPasswordEncoder(passwordEncoder());
        return authenticationManager;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .securityMatcher(
                        new NegatedServerWebExchangeMatcher(
                                new OrServerWebExchangeMatcher(pathMatchers("/app/**", "/i18n/**", "/content/**", "/swagger-ui/**"))
                        )
                )
                .cors(withDefaults())
                .csrf(csrf -> csrf.disable())
                .headers(headers ->
                        headers
                                .contentSecurityPolicy(csp -> csp.policyDirectives(properties.getSecurity().getCsp()))
                                .frameOptions(frameOptions -> frameOptions.mode(XFrameOptionsServerHttpHeadersWriter.Mode.DENY))
                                .referrerPolicy(referrer ->
                                        referrer.policy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
                                )
                                .permissionsPolicy(permissions ->
                                        permissions.policy(
                                                "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                                        )
                                )
                )
                .authorizeExchange(authz ->
                        // prettier-ignore
                        // TODO: Disabled AuthN/AuthZ as this would be handled by Cloud provider
                        authz
                                .pathMatchers("/graphql", "/graphiql").permitAll()
                                .pathMatchers("/api/result-files/**").permitAll()
                                .pathMatchers("/api/authenticate").permitAll()
                                .pathMatchers("/api/register").permitAll()
                                .pathMatchers("/api/activate").permitAll()
                                .pathMatchers("/api/account/reset-password/init").permitAll()
                                .pathMatchers("/api/account/reset-password/finish").permitAll()
                                .pathMatchers("/api/admin/**").hasAuthority(AuthoritiesConstants.ADMIN)
                                .pathMatchers("/api/**").authenticated()
                                .pathMatchers("/services/**").authenticated()
                                .pathMatchers("/v3/api-docs/**").permitAll()
                                .pathMatchers("/management/health").permitAll()
                                .pathMatchers("/management/health/**").permitAll()
                                .pathMatchers("/management/jhimetrics").permitAll()
                                .pathMatchers("/management/info").permitAll()
                                .pathMatchers("/management/prometheus").permitAll()
                                .pathMatchers("/management/**").hasAuthority(AuthoritiesConstants.ADMIN)
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }
}
