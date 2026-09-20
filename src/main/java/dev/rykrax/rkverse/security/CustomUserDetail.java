package dev.rykrax.rkverse.security;

import dev.rykrax.rkverse.enums.UserStatus;
import dev.rykrax.rkverse.feature.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@Getter
@AllArgsConstructor
public class CustomUserDetail implements UserDetails {

    private final User user;

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        if (user.getRoles() == null || user.getRoles().trim().isEmpty()) {
//            return Collections.emptyList();
//        }
//        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
//    }

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Collections.emptyList();
        }

        Set<GrantedAuthority> authorities = new HashSet<>();

        user.getRoles().forEach(role -> {
            if (role.getName() != null && !role.getName().isBlank()) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            }

            //thêm permissions thuộc Role
            if (role.getPermissions() != null) {
                role.getPermissions().forEach(permission -> {
                    if (permission.getCode() != null && !permission.getCode().isBlank()) {
                        authorities.add(new SimpleGrantedAuthority(permission.getCode()));
                    }
                });
            }
        });

        return authorities;
    }

    @Override
    public @Nullable String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        // Ném LockedException nếu tài khoản bị khóa/ban
        return user.getStatus() != UserStatus.LOCKED && user.getStatus() != UserStatus.BANNED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return user.getDeletedAt() == null && user.getStatus() == UserStatus.ACTIVE;
    }

    public Long getId() {
        return user.getId();
    }
}
