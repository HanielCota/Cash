package com.github.hanielcota.cash.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class CashTransaction {

    private final double amount;
    private final LocalDateTime transactionTime;
}
