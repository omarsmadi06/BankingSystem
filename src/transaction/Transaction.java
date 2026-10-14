package transaction;

import model.Account;

/**
 * Represents a single bank transaction.
 *
 * Lifecycle:
 *   PENDING → validate() → execute() → COMMITTED
 *                       ↘ rollback() → ROLLED_BACK
 */
public class Transaction {

    private static int counter = 0;

    private final String          transactionId;
    private final TransactionType type;
    private final Account         fromAccount;
    private final Account         toAccount;   // null for deposit / withdraw
    private final double          amount;
    private       TransactionStatus status;
    private       String          failureReason;

    // Snapshots for rollback
    private double savedFromBalance;
    private double savedToBalance;

    public Transaction(TransactionType type, Account fromAccount, Account toAccount, double amount) {
        this.transactionId = "TXN-" + String.format("%04d", ++counter);
        this.type          = type;
        this.fromAccount   = fromAccount;
        this.toAccount     = toAccount;
        this.amount        = amount;
        this.status        = TransactionStatus.PENDING;
    }

    // ── Lifecycle methods ─────────────────────────────────────────────────────

    /**
     * Validates the transaction before any funds move.
     * @return true if valid, false otherwise.
     */
    public boolean validate() {
        if (amount <= 0) {
            failureReason = "Amount must be positive.";
            return false;
        }
        if (fromAccount == null) {
            failureReason = "Source account not found.";
            return false;
        }
        if (type == TransactionType.TRANSFER && toAccount == null) {
            failureReason = "Destination account not found.";
            return false;
        }
        if (type == TransactionType.TRANSFER && fromAccount.getAccountId().equals(toAccount.getAccountId())) {
            failureReason = "Cannot transfer to the same account.";
            return false;
        }
        if ((type == TransactionType.WITHDRAW || type == TransactionType.TRANSFER)
                && fromAccount.getBalance() < amount) {
            failureReason = String.format("Insufficient funds. Available: $%.2f", fromAccount.getBalance());
            return false;
        }
        return true;
    }

    /**
     * Executes the transaction.
     * Saves balance snapshots BEFORE touching any account so rollback is safe.
     */
    public void execute() {
        // Save snapshots
        savedFromBalance = fromAccount.getBalance();
        savedToBalance   = (toAccount != null) ? toAccount.getBalance() : 0.0;

        try {
            switch (type) {
                case DEPOSIT:
                    fromAccount.deposit(amount);
                    break;
                case WITHDRAW:
                    fromAccount.withdraw(amount);
                    break;
                case TRANSFER:
                    fromAccount.withdraw(amount);
                    toAccount.deposit(amount);
                    break;
            }
            status = TransactionStatus.COMMITTED;
        } catch (Exception ex) {
            failureReason = ex.getMessage();
            rollback();
        }
    }

    /**
     * Reverts balances to their pre-execution snapshots.
     */
    public void rollback() {
        if (fromAccount != null) fromAccount.forceSetBalance(savedFromBalance);
        if (toAccount   != null) toAccount.forceSetBalance(savedToBalance);
        status = TransactionStatus.ROLLED_BACK;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String            getTransactionId() { return transactionId;  }
    public TransactionType   getType()           { return type;           }
    public double            getAmount()         { return amount;         }
    public TransactionStatus getStatus()         { return status;         }
    public String            getFailureReason()  { return failureReason;  }
    public Account           getFromAccount()    { return fromAccount;    }
    public Account           getToAccount()      { return toAccount;      }

    @Override
    public String toString() {
        String base = String.format("[%s] %s | Amount: $%.2f | Status: %s",
                transactionId, type, amount, status);
        if (status == TransactionStatus.ROLLED_BACK && failureReason != null)
            base += " | Reason: " + failureReason;
        return base;
    }
}
