package kz.timshowtime.onlineShop.model;

import kz.timshowtime.onlineShop.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Table("users")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ClientUser {

    @Id
    private Long id;
    private String username;
    private String password;
    private Role role;
    private Instant createdAt;
}
