package solutions.onz.services.imagengine.services.dto;

import lombok.Getter;
import lombok.Setter;
import solutions.onz.services.imagengine.domain.User;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * A DTO representing a user, with only the public attributes.
 */
@Getter
@Setter
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;

    private String login;

    public UserDTO() {
        // Empty constructor needed for Jackson.
    }

    public UserDTO(User user) {
        this.id = user.getId();
        // Customize it here if you need, or not, firstName/lastName/etc
        this.login = user.getLogin();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserDTO userDTO = (UserDTO) o;
        if (userDTO.getId() == null || getId() == null) {
            return false;
        }

        return Objects.equals(getId(), userDTO.getId()) && Objects.equals(getLogin(), userDTO.getLogin());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLogin());
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "UserDTO{" +
            "id='" + id + '\'' +
            ", login='" + login + '\'' +
            "}";
    }
}
