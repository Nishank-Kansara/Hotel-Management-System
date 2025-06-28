package com.Nishank_Kansara.hotel_management_system.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@Entity
@NoArgsConstructor
@Getter
@Setter
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToMany(mappedBy = "roles")
    @JsonIgnore
    private Collection<User> users = new HashSet<>();

    public Role(String name) {
        this.name = name;
    }

    public void assignRoleToUser(User user) {
        user.getRoles().add(this);
        this.users.add(user);
    }

    public void removeUserFromRole(User user) {
        user.getRoles().remove(this);
        this.users.remove(user);
    }

    public void removeAllUsersFromRole() {
        if (this.users != null) {
            List<User> roleUsers = List.copyOf(this.users);
            roleUsers.forEach(this::removeUserFromRole);
        }
    }

    public String getName() {
        return name != null ? name : "";
    }
}
