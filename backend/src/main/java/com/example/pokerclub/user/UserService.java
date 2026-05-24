package com.example.pokerclub.user;

import com.example.pokerclub.common.NotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    //возвращает только посетителей
    public List<User> getVisitors() {
        return userRepository.findByRoleOrderByLastNameAscFirstNameAsc(UserRole.VISITOR);
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User getVisitorById(Long visitorId) {
        User user = userRepository.findById(visitorId)
            .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() != UserRole.VISITOR) {
            throw new NotFoundException("Visitor not found");
        }
        return user;
    }
}

