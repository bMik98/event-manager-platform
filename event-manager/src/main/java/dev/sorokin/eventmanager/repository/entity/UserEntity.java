package dev.sorokin.eventmanager.repository.entity;

import dev.sorokin.eventmanager.common.EventManagerConstants;
import dev.sorokin.eventmanager.service.model.UserRole;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @NotNull
    @Size(max = 255)
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role = UserRole.USER;

    @NotNull
    @Min(EventManagerConstants.MIN_USER_AGE)
    @Column(name = "age", nullable = false)
    private Integer age;
}
