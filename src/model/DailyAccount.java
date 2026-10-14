package model;

/**
 * Daily transaction account.
 * Tracks a daily withdrawal limit and resets it on demand.
 * Low interest — designed for frequent everyday use.
 */
public class DailyAccount extends Account {

    private static final double DAILY_LIMIT = 500.0;
    private double dailyWithdrawn = 0.0;

    public DailyAccount(String accountId, String ownerId, double initialBalance) {
        super(accountId, ownerId, initialBalance, 0.005); // 0.5%
    }

    @Override
    public void withdraw(double amount) {
        if (dailyWithdrawn + amount > DAILY_LIMIT)
            throw new IllegalStateException(
                    String.format("Daily limit exceeded. Limit: $%.2f | Used today: $%.2f | Remaining: $%.2f",
                            DAILY_LIMIT, dailyWithdrawn, DAILY_LIMIT - dailyWithdrawn));
        super.withdraw(amount);
        dailyWithdrawn += amount;
    }

    /** Call at the start of each new banking day to reset the daily counter. */
    public void resetDailyLimit() {
        dailyWithdrawn = 0.0;
    }

    public double getRemainingDailyLimit() { return DAILY_LIMIT - dailyWithdrawn; }

    @Override
    public void applyInterest() {
        double interest = balance * interestRate;
        balance += interest;
        System.out.printf("  [Interest] Daily account %s credited $%.2f%n",
                getAccountId(), interest);
    }

    @Override
    public String getAccountType() { return "Daily"; }
}
