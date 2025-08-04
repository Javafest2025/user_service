package org.solace.scholar_ai.user_service.service.auth;

import java.util.List;
import org.solace.scholar_ai.user_service.model.User;
import org.solace.scholar_ai.user_service.model.UserIdentityProvider;
import org.solace.scholar_ai.user_service.repository.UserIdentityProviderRepository;
import org.solace.scholar_ai.user_service.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class UserLoadingService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserIdentityProviderRepository userIdentityProviderRepository;

    public UserLoadingService(
            UserRepository userRepository, UserIdentityProviderRepository userIdentityProviderRepository) {
        this.userRepository = userRepository;
        this.userIdentityProviderRepository = userIdentityProviderRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        User user = userRepository
                .findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with email: " + username));

        // Check if user has identity providers (social login)
        UserIdentityProvider identityProvider = userIdentityProviderRepository.findByUserId(user.getId());
        boolean isSocialUser = identityProvider != null;

        // Create authority from user role
        GrantedAuthority grantedAuthority =
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
        List<GrantedAuthority> authorityList = List.of(grantedAuthority);

        // For social users, use empty password since they authenticate through OAuth
        String password = isSocialUser ? "" : user.getEncryptedPassword();

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                password,
                user.isEmailConfirmed(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorityList);
    }
}
