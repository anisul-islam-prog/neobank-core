package com.neobank.auth.internal.docs;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

/**
 * Authentication token for documentation access.
 * Represents an authenticated session via documentation access token.
 */
public class DocAccessTokenAuthentication extends AbstractAuthenticationToken {

    private final String token;
    private final UUID createdBy;
    private final Collection<GrantedAuthority> authorities;

    public DocAccessTokenAuthentication(String token, UUID createdBy, 
                                        Collection<GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.createdBy = createdBy;
        this.authorities = authorities;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return "DOC_ACCESS_" + createdBy;
    }

    public String getToken() {
        return token;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        return authorities;
    }
}
