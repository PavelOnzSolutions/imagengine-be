package solutions.onz.services.imagengine.dbmigrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;
import solutions.onz.services.imagengine.domain.Authority;
import solutions.onz.services.imagengine.domain.BillingRecord;
import solutions.onz.services.imagengine.domain.User;
import solutions.onz.services.imagengine.identity.AuthoritiesConstants;

import java.time.Instant;
import java.util.UUID;

import static solutions.onz.services.imagengine.config.Constants.*;

/**
 * Creates the initial database setup.
 */
@ChangeUnit(id = "users-initialization", order = "001")
public class InitialMigration {

    private final MongoTemplate template;

    public InitialMigration(MongoTemplate template) {
        this.template = template;
    }

    @Execution
    public void changeSet() {
        Authority userAuthority = createUserAuthority();
        userAuthority = template.save(userAuthority);
        Authority adminAuthority = createAdminAuthority();
        adminAuthority = template.save(adminAuthority);
        addUsers(userAuthority, adminAuthority);
    }

    @RollbackExecution
    public void rollback() {}

    private Authority createAuthority(String authority, String description) {
        Authority adminAuthority = new Authority();
        adminAuthority.setName(authority);
        adminAuthority.setDescription(description);
        return adminAuthority;
    }

    private Authority createAdminAuthority() {
        return createAuthority(AuthoritiesConstants.ADMIN, "Admin authority with all the permissions");
    }

    private Authority createUserAuthority() {
        return createAuthority(AuthoritiesConstants.USER, "User authority with basic permissions");
    }

    private void addUsers(Authority userAuthority, Authority adminAuthority) {
        User user = createUser(userAuthority);
        template.save(user);
        User admin = createAdmin(adminAuthority, userAuthority);
        template.save(admin);
    }

    private User createUser(Authority userAuthority) {
        User userUser = new User();
        userUser.setId(UUID.randomUUID());
        userUser.setLogin("user");
        userUser.setPassword("$2a$10$VEjxo0jq2YG9Rbk2HmX9S.k1uZBGYUHdUcid3g/vfiEl7lwWgOH/K");
        userUser.setFirstName("User");
        userUser.setLastName("User");
        userUser.setEmail("user@localhost");
        userUser.setActivated(true);
        userUser.setLangKey("en");
        userUser.setCreatedBy(SYSTEM);
        userUser.setCreatedDate(Instant.now());
        userUser.getAuthorities().add(userAuthority);
        return createBilling(userUser);
    }

    private User createAdmin(Authority adminAuthority, Authority userAuthority) {
        User adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setLogin("admin");
        adminUser.setPassword("$2a$10$gSAhZrxMllrbgj/kkK9UceBPpChGWJA7SYIb1Mqo.n5aNLq1/oRrC");
        adminUser.setFirstName("admin");
        adminUser.setLastName("Administrator");
        adminUser.setEmail("admin@localhost");
        adminUser.setActivated(true);
        adminUser.setLangKey("en");
        adminUser.setCreatedBy(SYSTEM);
        adminUser.setCreatedDate(Instant.now());
        adminUser.getAuthorities().add(adminAuthority);
        adminUser.getAuthorities().add(userAuthority);
        return createBilling(adminUser);
    }

    private User createBilling(User user) {
        BillingRecord br = new BillingRecord();
        user.setBilling(br);
        return user;
    }
}
