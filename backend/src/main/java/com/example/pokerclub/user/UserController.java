package com.example.pokerclub.user;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/api/admin/visitors")
    public List<UserResponse> getVisitors() {
        return userService.getVisitors()
                .stream()
                .map(UserResponse::from)
                .toList();
    }
}