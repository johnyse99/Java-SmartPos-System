package io.smartpos.services.auth;

import io.smartpos.core.domain.user.User;
import io.smartpos.infrastructure.dao.UserDao;
import io.smartpos.core.exceptions.BusinessException;
import org.mindrot.jbcrypt.BCrypt;

public class AuthServiceImpl implements AuthService {

    private final UserDao userDao;
    private User currentUser;

    public AuthServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public User login(String username, String password) {
        User user = userDao.findByUsername(username);

        if (user == null || !user.isActive()) {
            throw new BusinessException("Invalid credentials or inactive account.");
        }

        String storedHash = user.getPasswordHash();
        boolean authenticated = false;

        // 1. Try BCrypt if it looks like a hash
        if (storedHash != null && storedHash.startsWith("$2")) {
            try {
                if (BCrypt.checkpw(password, storedHash)) {
                    authenticated = true;
                }
            } catch (IllegalArgumentException e) {
                // Not a valid BCrypt hash, ignore and try plain text fallback
            }
        }

        // 2. Fallback: Plain text comparison (Migration)
        if (!authenticated && password.equals(storedHash)) {
            // It matches as plain text! Let's migrate it now.
            String newHash = hashPassword(password);
            user.setPasswordHash(newHash);
            userDao.update(user);
            authenticated = true;
        }

        if (authenticated) {
            this.currentUser = user;
            return user;
        }

        throw new BusinessException("Invalid username or password.");
    }

    @Override
    public String hashPassword(String plainText) {
        return BCrypt.hashpw(plainText, BCrypt.gensalt());
    }

    @Override
    public User getCurrentUser() {
        return currentUser;
    }

    @Override
    public void logout() {
        this.currentUser = null;
    }

    @Override
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    @Override
    public boolean hasRole(String role) {
        return currentUser != null && currentUser.getRole().equalsIgnoreCase(role);
    }
}
