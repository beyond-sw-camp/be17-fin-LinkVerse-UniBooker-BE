package org.example.unibooker.domain.notification.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestPrincipalController {

    private final SimpUserRegistry simpUserRegistry;

    @GetMapping("/users")
    public List<String> getConnectedUsers() {
        return simpUserRegistry.getUsers().stream()
                .map(u -> u.getName())
                .toList();
    }
}
