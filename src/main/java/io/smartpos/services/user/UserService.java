/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.smartpos.services.user;

import io.smartpos.core.domain.security.User;
import java.util.List;

public interface UserService {

    void createUser(User user, String rawPassword);

    void assignRole(int userId, int roleId);

    void deactivateUser(int userId);

    List<User> findAllActiveUsers();
}
