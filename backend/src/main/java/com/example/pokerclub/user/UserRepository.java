package com.example.pokerclub.user;
//отвечает за работе с таблицей users
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
//Spring Data JPA во время запуска приложения автоматически создает объект, который реализует этот интерфейс.
//extends JpaRepository<User, Long> - нам автоматически доступны методы: save(user), findById(id), findAll(), delete(user), count(), existsById(id)

public interface UserRepository extends JpaRepository<User, Long> {
    //ищем пользователей по роли;
    //сортируем по фамилии и имени;
    //нам нужны только VISITOR.
    List<User> findByRoleOrderByLastNameAscFirstNameAsc(UserRole role);

    //Optional<User> - потому что пользователь может быть найдет а может быть не найден(чтобы не возвращать NULL короче)
    Optional<User> findByEmailIgnoreCase(String email);
    //проверка есть ли пользователь с таким же email
    boolean existsByEmailIgnoreCase(String email);
}

