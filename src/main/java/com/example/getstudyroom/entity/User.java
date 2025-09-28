package com.example.getstudyroom.entity;

import com.example.getstudyroom.enums.RolesType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "roles", nullable = false)
    private RolesType roles;

    public User(String name, String password, RolesType rolesType) {
        this.name = name;
        this.password = password;
        this.roles = rolesType;
    }
}
