package kz.timshowtime.onlineShop.security;

import kz.timshowtime.onlineShop.model.ClientUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserDetailsImpl implements UserDetails {

    @Getter
    private final Long id;
    private final String username;
    private final String password;
    private final List<GrantedAuthority> authorities;


    public UserDetailsImpl(ClientUser clientUser) {
        this.id = clientUser.getId();
        this.username = clientUser.getUsername();
        this.password = clientUser.getPassword();
        this.authorities = List.of(clientUser.getRole());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}
