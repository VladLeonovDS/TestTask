package org.deathstrix.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TelegramUser {
    @Id
    private Long id;

    private String firstName;
    private String lastName;
    private String username;

    private Long authDate;
}

