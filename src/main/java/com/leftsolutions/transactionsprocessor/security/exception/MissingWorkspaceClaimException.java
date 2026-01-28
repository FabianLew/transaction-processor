package com.leftsolutions.transactionsprocessor.security.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MissingWorkspaceClaimException extends ResponseStatusException {
    private static final String MSG_JWT_MISSING = "JWT token is missing";
    private static final String MSG_WORKSPACE_CLAIM_MISSING = "Missing required workspace claim";

    private MissingWorkspaceClaimException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }

    public static MissingWorkspaceClaimException jwtMissing() {
        return new MissingWorkspaceClaimException(MSG_JWT_MISSING);
    }

    public static MissingWorkspaceClaimException workspaceClaimMissing() {
        return new MissingWorkspaceClaimException(MSG_WORKSPACE_CLAIM_MISSING);
    }
}
