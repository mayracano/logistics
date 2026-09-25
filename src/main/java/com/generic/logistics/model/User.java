package com.generic.logistics.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;
import org.hibernate.envers.Audited;

@Entity
@Data
@Table(name="users")
@Audited
public class User {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    @Id
    private Long id;

    @Column(name="first_name", nullable = false, length = 50)
    @NotNull(message = "First Name is required")
    private String firstName;

    @Column(name="last_name", nullable = false, length = 50)
    @NotNull(message = "Last Name is required")
    private String lastName;

    @Column(name="email", nullable = false, length = 100)
    @NotNull(message = "email is required")
    @Email
    private String email;

    @Column(name="password", nullable = false)
    @NotNull(message = "password is required")
    @ToString.Exclude
    private String password;

    @Column(name="phone", nullable = false)
    @NotNull(message = "phone is required")
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @NotNull(message = "role is required")
    private Role role;
}
