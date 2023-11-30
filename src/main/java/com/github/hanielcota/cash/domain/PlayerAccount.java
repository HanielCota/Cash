package com.github.hanielcota.cash.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlayerAccount {

    private String playerId;
    private double balance;
}
