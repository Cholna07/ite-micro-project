package kh.edu.istad.platform.payment.domain.entity;

import kh.edu.istad.platform.payment.domain.exception.PaymentDomainException;
import kh.edu.istad.platform.payment.domain.valueobject.CreditEntryId;
import kh.edu.istad.platform.payment.domain.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.valueobject.Money;

import java.math.BigDecimal;

public class CreditEntry {

    private CreditEntryId creditEntryId;
    private CustomerId customerId;
    private Money totalCreditAmount;

    public CreditEntry() {
    }

    public CreditEntry(
            CreditEntryId creditEntryId,
            CustomerId customerId,
            Money totalCreditAmount
    ) {
        this.creditEntryId = creditEntryId;
        this.customerId = customerId;
        this.totalCreditAmount = totalCreditAmount;
    }

    public void addCreditAmount(Money amount) {
        validateAmount(amount);

        BigDecimal current = totalCreditAmount == null
                ? BigDecimal.ZERO
                : totalCreditAmount.amount();

        totalCreditAmount = new Money(current.add(amount.amount()));
    }

    public void subtractCreditAmount(Money amount) {
        validateAmount(amount);

        BigDecimal current = totalCreditAmount == null
                ? BigDecimal.ZERO
                : totalCreditAmount.amount();

        if (current.compareTo(amount.amount()) < 0) {
            throw new PaymentDomainException(
                    "Insufficient credit balance"
            );
        }

        totalCreditAmount = new Money(current.subtract(amount.amount()));
    }

    private void validateAmount(Money amount) {
        if (amount == null || amount.amount() == null
                || amount.amount().signum() <= 0) {
            throw new PaymentDomainException(
                    "Credit amount must be greater than zero"
            );
        }
    }

    public CreditEntryId getCreditEntryId() {
        return creditEntryId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getTotalCreditAmount() {
        return totalCreditAmount;
    }
}
