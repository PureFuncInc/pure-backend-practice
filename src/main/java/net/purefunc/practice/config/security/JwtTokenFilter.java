package net.purefunc.practice.config.security;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private final JwtTokenService jwtTokenService;

    private final UserDetailsService springUserDetailsService;

    public JwtTokenFilter(JwtTokenService jwtTokenService, UserDetailsService springUserDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.springUserDetailsService = springUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Optional
                .ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION))
                .filter(v -> v.startsWith("Bearer "))
                .ifPresent(v -> {
                    final String username = jwtTokenService.retrieveSubject(v.substring(7));
                    final UserDetails userDetails = springUserDetailsService.loadUserByUsername(username);
                    final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                });

        filterChain.doFilter(request, response);
    }
}
