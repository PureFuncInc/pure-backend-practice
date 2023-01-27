package net.purefunc.practice.config.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional
                .ofNullable(SecurityContextHolder.getContext())
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(v -> (AbstractAuthenticationToken) v)
                .map(AbstractAuthenticationToken::getName)
                .or(() -> Optional.of(""));
    }
}
