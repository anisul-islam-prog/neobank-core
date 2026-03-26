package com.neobank.auth.internal.docs;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Filter for validating documentation access tokens.
 * Checks for access_token query parameter or X-DOC-ACCESS-TOKEN header.
 * 
 * Only active in non-test profiles.
 */
@Component
@org.springframework.context.annotation.Profile("!test")
public class DocAccessTokenFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(DocAccessTokenFilter.class);

    private static final String TOKEN_HEADER = "X-DOC-ACCESS-TOKEN";
    private static final String TOKEN_PARAM = "access_token";

    private final DocTokenService tokenService;
    private final SecurityContextRepository securityContextRepository;

    public DocAccessTokenFilter(DocTokenService tokenService, SecurityContextRepository securityContextRepository) {
        this.tokenService = tokenService;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Only process documentation-related requests
        String path = request.getRequestURI();
        if (!isDocumentationPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token from header or query parameter
        String token = extractToken(request);
        
        if (token != null && !token.isBlank()) {
            DocTokenEntity entity = tokenService.validateToken(token);
            
            if (entity != null) {
                // Token is valid - set up security context
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                
                // Create authentication with DOC_ACCESS role
                DocAccessTokenAuthentication authentication = new DocAccessTokenAuthentication(
                        entity.getToken(),
                        entity.getCreatedBy(),
                        Collections.singletonList(new SimpleGrantedAuthority("ROLE_DOC_ACCESS"))
                );
                
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                securityContextRepository.saveContext(context, request, response);
                
                log.debug("Documentation access granted for token: {} (uses: {})", 
                        token.substring(0, Math.min(12, token.length())) + "...", entity.getUseCount());
                
                filterChain.doFilter(request, response);
                return;
            } else {
                log.warn("Invalid or expired documentation access token: {}", 
                        token.substring(0, Math.min(12, token.length())) + "...");
            }
        }

        // No valid token - continue without authentication
        // SecurityConfig will handle authorization
        filterChain.doFilter(request, response);
    }

    /**
     * Check if the request path is for documentation.
     */
    private boolean isDocumentationPath(String path) {
        return path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs") ||
               path.startsWith("/swagger-resources") ||
               path.startsWith("/webjars/swagger") ||
               "/swagger-ui.html".equals(path);
    }

    /**
     * Extract token from header or query parameter.
     */
    private String extractToken(HttpServletRequest request) {
        // Try header first
        String token = request.getHeader(TOKEN_HEADER);
        
        // Try query parameter
        if (token == null || token.isBlank()) {
            token = request.getParameter(TOKEN_PARAM);
        }
        
        return token;
    }
}
