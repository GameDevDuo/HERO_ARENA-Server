package com.gamedevduo.heroarena.domain.user.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Table(name = "users")
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    private long trophy;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    public void updatePassword(String password) {
        this.password = password;
    }
}