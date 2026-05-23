package com.example.pokerclub.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
//срабатывает перед контроллерами и проверяет токен на каждом запросе
@Component
//OncePerRequestFilter - фильтр выполнится один раз на каждый HTTP-запрос
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService; //нужен чтобы достать email и проверить валидность
    private final UserDetailsService userDetailsService; //нужен чтобы загрузит пользователя из бд оп email

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, //входящий запрос(из него можно получить URL, headers,body,query params
        HttpServletResponse response, //ответ который бэк отправит клиенту
        FilterChain filterChain // цепочка фильтров и контроллеров
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization"); //сюда кладет токен
        //проверяет есть и токен и начинается ли он с Bearer(стандартный формат передачи токена)
        //если токена нет, фильтр не авторизуе пользователя а пропускает запрос дальше
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //берем сам токен(Bearer ewrsfesfdsf....) поэтому убираем первые 7 символов(Bearer )
        String token = authHeader.substring(7);
        String email = jwtService.extractUsername(token); //достаем email

        //проверка что пользовател еще не авторизован
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails user = userDetailsService.loadUserByUsername(email);
            //проверяет принадлежит ли токен польователю, не истек ли срок действия, корректная подпись ли токена
            if (jwtService.isValid(token, (com.example.pokerclub.user.User) user)) {
                //этот объект говррит что пользователь авторизован и передает самого пользователя и роль, null-пароль, он не нужен)
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    user.getAuthorities()
                );
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        //пропускаем запрос дальше
        filterChain.doFilter(request, response);
    }
}

