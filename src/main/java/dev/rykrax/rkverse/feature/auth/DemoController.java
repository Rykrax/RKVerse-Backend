package dev.rykrax.rkverse.feature.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
@RequiredArgsConstructor
public class DemoController {

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('user.select')")
    public String adminOnly() {
        return "Chỉ admin được xem nha";
    }

    @PutMapping("/update")
    @PreAuthorize("hasAuthority('user.update')")
    public String userOrAdmin() {
        return "User hoặc Admin đều được xem";
    }


}
