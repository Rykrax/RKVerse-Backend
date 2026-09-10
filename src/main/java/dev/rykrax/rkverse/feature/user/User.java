package dev.rykrax.rkverse.feature.user;

import dev.rykrax.rkverse.enums.UserStatus;
import dev.rykrax.rkverse.feature.role.Role;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.Instant;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@SQLRestriction("deleted_at IS NULL")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "email", unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    public static User createRegisteredUser(String username, String encodedPassword, Role defaultRole) {
        Objects.requireNonNull(username, "Username không được để null");
        Objects.requireNonNull(encodedPassword, "Mật khẩu mã hóa không được để null");
        Objects.requireNonNull(defaultRole, "Role mặc định không được để null");

        User user = new User();
        user.username = username;
        user.password = encodedPassword;
        user.status = UserStatus.ACTIVE;

        user.roles = new HashSet<>();
        user.roles.add(defaultRole);

        return user;
    }
}