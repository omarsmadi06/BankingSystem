package model;

/**
 * Savings account with higher interest and a minimum balance rule.
 * Withdrawals below the minimum balance threshold are blocked.
 */
public class SavingsAccount extends Account {

    private static final double MIN_BALANCE = 100.0;

    public SavingsAccount(String accountId, String ownerId, double initialBalance) {
        super(accountId, ownerId, initialBalance, 0.035); // 3.5%
        if (initialBalance < MIN_BALANCE)
            throw new IllegalArgumentException(
                    "Savings account requires a minimum opening balance of $" + MIN_BALANCE);
    }

    @Override
    public void withdraw(double amount) {
        if (balance - amount < MIN_BALANCE)
            throw new IllegalStateException(
                    String.format("Withdrawal would breach minimum balance of $%.2f. Available to withdraw: $%.2f",
                            MIN_BALANCE, balance - MIN_BALANCE));
        super.withdraw(amount);
    }

    @Override
    public void applyInterest() {
        double interest = balance * interestRate;
        balance += interest;
        System.out.printf("  [Interest] Savings account %s credited $%.2f%n",
                getAccountId(), interest);
    }

    @Override
    public String getAccountType() { return "Savings"; }
}
