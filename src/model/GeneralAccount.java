package model;

/**
 * General-purpose bank account.
 * No special interest rules — interest is flat per cycle.
 */
public class GeneralAccount extends Account {

    public GeneralAccount(String accountId, String ownerId, double initialBalance) {
        super(accountId, ownerId, initialBalance, 0.01); // 1% flat
    }

    @Override
    public void applyInterest() {
        double interest = balance * interestRate;
        balance += interest;
        System.out.printf("  [Interest] General account %s credited $%.2f%n",
                getAccountId(), interest);
    }

    @Override
    public String getAccountType() { return "General"; }
}
