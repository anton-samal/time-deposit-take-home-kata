package org.ikigaidigital.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A withdrawal from a time deposit. Kept separate from {@link org.ikigaidigital.TimeDeposit}, which must not change,
 * and linked to it by id (as the {@code timeDepositId} foreign key in the database).
 * Withdrawals don't affect interest: the existing calculation ignores them.
 */
public record Withdrawal(int id, int timeDepositId, BigDecimal amount, LocalDate date) {
}
