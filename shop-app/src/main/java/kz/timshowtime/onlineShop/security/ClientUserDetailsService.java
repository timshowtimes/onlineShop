package kz.timshowtime.onlineShop.security;

import kz.timshowtime.onlineShop.model.ClientUser;
import kz.timshowtime.onlineShop.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ClientUserDetailsService implements ReactiveUserDetailsService {

    private final UserRepository userRepository;

    public Mono<ClientUser> loadUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::toUserDetails);
    }

    private UserDetails toUserDetails(ClientUser clientUser) {
        return new UserDetailsImpl(clientUser);
    }
}
