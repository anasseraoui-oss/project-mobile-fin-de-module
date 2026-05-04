package com.elearning.authserver.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    // private final UserRepository userRepository;
    // public CustomUserDetailsService(UserRepository userRepository) { ... }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Logique fictive pour l'exemple. À remplacer par `userRepository.findByEmail(username)`
        if ("superadmin@elearning.com".equals(username)) {
            return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("{noop}admin123") // À hasher avec BCryptPasswordEncoder
                .authorities(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"))
                .build();
        } 
        else if ("trainer@elearning.com".equals(username)) {
            return org.springframework.security.core.userdetails.User.builder()
                .username(username)
                .password("{noop}trainer123")
                .authorities(new SimpleGrantedAuthority("ROLE_FORMATEUR"))
                .build();
        }
        
        // Rôles possibles: ROLE_SUPER_ADMIN, ROLE_ORGANISATION, ROLE_FORMATEUR, ROLE_APPRENANT
        throw new UsernameNotFoundException("Utilisateur non trouvé: " + username);
    }
}
