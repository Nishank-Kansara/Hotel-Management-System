package com.Nishank_Kansara.hotel_management_system.service;

import com.Nishank_Kansara.hotel_management_system.exception.RoleAlreadyExistException;
import com.Nishank_Kansara.hotel_management_system.model.Role;
import com.Nishank_Kansara.hotel_management_system.model.User;
import com.Nishank_Kansara.hotel_management_system.repository.RoleRepository;
import com.Nishank_Kansara.hotel_management_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoleService implements IRoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final IUserService userService;

    @Override
    public List<Role> getRoles() {
        return roleRepository.findAll();
    }

    @Override
    public Role createRole(Role theRole) {
        String roleName = "ROLE_" + theRole.getName().toUpperCase();
        Role role = new Role(roleName);
        if (roleRepository.existsByName(roleName)) {
            throw new RoleAlreadyExistException(theRole.getName() + " role Already Exists");
        }
        return roleRepository.save(role);
    }

    @Override
    public void deleteRole(Long roleId) {
        this.removeAllUsersFromRole(roleId);
        roleRepository.deleteById(roleId);
    }

    @Override
    public Role findByName(String name) {
        return roleRepository.findByName("ROLE_" + name.toUpperCase())
                .orElseThrow(() -> new RuntimeException("Role not found"));
    }

    @Override
    public User removeUserFromRole(Long userId, Long roleId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Role> role = roleRepository.findById(roleId);

        if (user.isPresent() && role.isPresent()) {
            if (role.get().getUsers().contains(user.get())) {
                role.get().removeUserFromRole(user.get());
                roleRepository.save(role.get());
                return user.get();
            } else {
                throw new RuntimeException("User is not assigned to the role");
            }
        }
        throw new UsernameNotFoundException("User or role not found");
    }

    @Override
    public User assignRoleToUser(Long userId, Long roleId) {
        Optional<User> user = userRepository.findById(userId);
        Optional<Role> role = roleRepository.findById(roleId);

        if (user.isPresent() && role.isPresent()) {
            if (user.get().getRoles().contains(role.get())) {
                throw new RoleAlreadyExistException(
                        user.get().getFirstName() + " is already assigned to the " + role.get().getName() + " role"
                );
            }
            role.get().assignRoleToUser(user.get());
            roleRepository.save(role.get());
            return user.get();
        }

        throw new UsernameNotFoundException("User or role not found");
    }

    @Override
    public Role removeAllUsersFromRole(Long roleId) {
        Optional<Role> role = roleRepository.findById(roleId);
        if (role.isPresent()) {
            role.get().removeAllUsersFromRole();
            return roleRepository.save(role.get());
        }
        throw new RuntimeException("Role not found");
    }
}
