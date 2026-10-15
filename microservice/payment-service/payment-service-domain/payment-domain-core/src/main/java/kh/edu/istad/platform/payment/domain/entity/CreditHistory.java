package kh.edu.istad.platform.payment.domain.entity;

import kh.edu.istad.platform.payment.domain.valueobject.CreditHistoryId;
import kh.edu.istad.platform.payment.domain.valueobject.CustomerId;
import kh.edu.istad.platform.payment.domain.valueobject.Money;
import kh.edu.istad.platform.payment.domain.valueobject.TransactionType;

public class CreditHistory {

    private CreditHistoryId creditHistoryId;
    private CustomerId customerId;
    private Money amount;
    private TransactionType transactionType;

    public CreditHistory() {
    }

    public CreditHistory(
            CreditHistoryId creditHistoryId,
            CustomerId customerId,
            Money amount,
            TransactionType transactionType
    ) {
        this.creditHistoryId = creditHistoryId;
        this.customerId = customerId;
        this.amount = amount;
        this.transactionType = transactionType;
    }

    public CreditHistoryId getCreditHistoryId() {
        return creditHistoryId;
    }

    public CustomerId getCustomerId() {
        return customerId;
    }

    public Money getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }
}
