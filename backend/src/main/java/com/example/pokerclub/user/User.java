package com.example.pokerclub.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
//этот класс должен храниться в бд
@Entity
//связан с тблицей users
@Table(name = "users")
//implements UserDetails - User можно исп-ть в Spring Security как авторизованного пользователя
public class User implements UserDetails {

    @Id
    //бд сама генерит первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //nullable = false - не может быть пустым
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    //то же смое что и с именем
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    //unique = true - должен быть уникальным для каждого пользователя
    @Column(nullable = false, unique = true)
    private String email;
    //хранится хэшированный пароль(то есть если будет пароль admin, то в бд
    //он будет храниться как $2a$10$ примерно
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    //хранится в базе как строка(тк роли хранятся в strin в бд)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserRole role;
    //дата регистрации(
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    //выполнить следующий метод перед первым сохраннением объекта в базу
    @PrePersist
    void onCreate() {
        //если дата создания не задана, то ставит текущую
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        //если роль не задана, значит ставит Visitor
        if (role == null) {
            role = UserRole.VISITOR;
        }
    }

    //spring работает не напрямую с VISITOR а с authority
    //ROLE_VISITOR или ROLE_ADMINISTRATOR
    //getAuthorities() - возвращает список прав пользователя(для Spring Security)
    //SimpleGrantedAuthority() - хранит одно право пользователя в виде строки
    //возвращается List потому что мы храним всего одну роль для пользователя
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //"ROLE_" + role.name() - нужно чтобы потом писать правила доступа hasRole("ADMINISTRATOR")
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

