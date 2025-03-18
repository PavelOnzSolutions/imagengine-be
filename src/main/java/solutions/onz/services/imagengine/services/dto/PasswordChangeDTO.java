package solutions.onz.services.imagengine.services.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * A DTO representing a password change required data - current and new password.
 */
@Setter
@Getter
public class PasswordChangeDTO implements Serializable {

    @Serial
    private static final long serialVeSrsionUID = 1L;

    private String currentPassword;
    private String newPassword;

    public PasswordChangeDTO() {
        // Empty constructor needed for Jackson.
    }

    public PasswordChangeDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

}
