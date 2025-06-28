package com.Nishank_Kansara.hotel_management_system.service;

import com.Nishank_Kansara.hotel_management_system.model.Role;
import com.Nishank_Kansara.hotel_management_system.model.User;

import java.util.List;

public interface IRoleService {

    List<Role>getRoles();
    Role createRole(Role role);
    void deleteRole(Long id);
    Role findByName(String name);
    User removeUserFromRole(Long userId, Long roleId);
    User assignRoleToUser(Long userId, Long roleId);
    Role removeAllUsersFromRole(Long roleId);
}
