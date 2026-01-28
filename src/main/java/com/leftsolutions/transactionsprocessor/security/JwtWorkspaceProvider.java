package com.leftsolutions.transactionsprocessor.security;

import com.leftsolutions.transactionsprocessor.security.exception.MissingWorkspaceClaimException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
class JwtWorkspaceProvider implements WorkspaceProvider {

    private static final String WORKSPACE_ID_CLAIM = "workspace_id";

    @Override
    public String currentWorkspaceId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw MissingWorkspaceClaimException.jwtMissing();
        }

        var workspaceId = jwt.getClaimAsString(WORKSPACE_ID_CLAIM);
        if (workspaceId == null || workspaceId.isBlank()) {
            throw MissingWorkspaceClaimException.workspaceClaimMissing();
        }

        return workspaceId;
    }
}
