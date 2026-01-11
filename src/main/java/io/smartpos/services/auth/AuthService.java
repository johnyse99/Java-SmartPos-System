package io.smartpos.services.auth;

import io.smartpos.core.domain.user.User;

public interface AuthService {
    User login(String username, String password);

    String hashPassword(String plainText);

    User getCurrentUser();

    void logout();

    boolean isAuthenticated();

    boolean hasRole(String role);
}
