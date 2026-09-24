package dev.rykrax.rkverse.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class ClientIdentifier {

    public String resolveClientKey(HttpServletRequest request) {
        // 1. Nếu người dùng đã đăng nhập (qua JWT / Security)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }

        // 2. Nếu là user chưa đăng nhập thì lấy IP thật từ header X-Forwarded-For qua Proxy Nginx
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isBlank()) {
            return "ip:" + xfHeader.split(",")[0].trim();
        }

        return "ip:" + request.getRemoteAddr();
    }
}
