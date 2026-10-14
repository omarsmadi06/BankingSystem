package model;

/**
 * Abstract base class representing a bank account.
 * Applies Encapsulation and Abstraction principles.
 */
public abstract class Account {

    private final String accountId;
    private final String ownerId;
    protected double balance;
    protected final double interestRate;

    public Account(String accountId, String ownerId, double initialBalance, double interestRate) {
        if (initialBalance < 0)
            throw new IllegalArgumentException("Initial balance cannot be negative.");
        this.accountId    = accountId;
        this.ownerId      = ownerId;
        this.balance      = initialBalance;
        this.interestRate = interestRate;
    }

    // ── Core operations ──────────────────────────────────────────────────────

    public void deposit(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Deposit amount must be positive.");
        balance += amount;
    }

    public void withdraw(double amount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal amount must be positive.");
        if (amount > balance) throw new IllegalStateException(
                "Insufficient funds. Available: $" + String.format("%.2f", balance));
        balance -= amount;
    }

    /** Apply periodic interest — implemented differently per account type. */
    public abstract void applyInterest();

    /** Human-readable account type label. */
    public abstract String getAccountType();

    // ── Getters ───────────────────────────────────────────────────────────────

    /** Used exclusively by Transaction.rollback() — do not call elsewhere. */
    public void forceSetBalance(double balance) { this.balance = balance; }

    public String getAccountId()  { return accountId;    }
    public String getOwnerId()    { return ownerId;       }
    public double getBalance()    { return balance;       }
    public double getInterestRate() { return interestRate; }

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | Owner: %s | Balance: $%.2f | Rate: %.2f%%",
                getAccountType(), accountId, ownerId, balance, interestRate * 100);
    }
}
