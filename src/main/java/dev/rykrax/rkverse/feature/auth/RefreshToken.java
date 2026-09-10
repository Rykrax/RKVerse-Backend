package dev.rykrax.rkverse.feature.auth;

import dev.rykrax.rkverse.feature.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "refresh_token")
@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", nullable = false, unique = true, length = 255)
    private String token;

    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @Column(name = "revoked")
    private Boolean revoked = false;

    @Column(name = "created_at", updatable = false, insertable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false)
    private Instant updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public boolean isExpired() {
        return this.expiryDate != null && this.expiryDate.isBefore(Instant.now());
    }

    public boolean isRevoked() {
        return Boolean.TRUE.equals(this.revoked);
    }

    public boolean isValid() {
        return !isRevoked() && !isExpired();
    }
}
