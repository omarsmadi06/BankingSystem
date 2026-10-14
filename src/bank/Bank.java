package bank;

import customer.*;
import model.*;
import transaction.*;

import java.util.*;

/**
 * Central bank controller.
 * Manages customers, accounts, and processes a FIFO transaction queue.
 */
public class Bank {

    private final String bankName;

    // In-memory stores
    private final Map<String, Customer> customers  = new LinkedHashMap<>();
    private final Map<String, Account>  accounts   = new LinkedHashMap<>();
    private final Queue<Transaction>    txQueue    = new LinkedList<>();
    private final List<Transaction>     txHistory  = new ArrayList<>();

    private int customerCounter = 0;
    private int accountCounter  = 0;

    public Bank(String bankName) { this.bankName = bankName; }

    // ── Customer management ───────────────────────────────────────────────────

    public IndividualCustomer registerIndividual(String username, String password,
                                                 String fullName, String nationalId) {
        validateNewUsername(username);
        String id = "CUS-" + String.format("%03d", ++customerCounter);
        IndividualCustomer c = new IndividualCustomer(id, username, password, fullName, nationalId);
        customers.put(id, c);
        System.out.println("  ✓ Individual customer registered: " + c.getDisplayName() + " [" + id + "]");
        return c;
    }

    public BusinessCustomer registerBusiness(String username, String password,
                                             String companyName, String taxId) {
        validateNewUsername(username);
        String id = "CUS-" + String.format("%03d", ++customerCounter);
        BusinessCustomer c = new BusinessCustomer(id, username, password, companyName, taxId);
        customers.put(id, c);
        System.out.println("  ✓ Business customer registered: " + c.getDisplayName() + " [" + id + "]");
        return c;
    }

    public HybridCustomer registerHybrid(String username, String password,
                                         String fullName, String nationalId,
                                         String companyName, String taxId) {
        validateNewUsername(username);
        String id = "CUS-" + String.format("%03d", ++customerCounter);
        HybridCustomer c = new HybridCustomer(id, username, password, fullName, nationalId, companyName, taxId);
        customers.put(id, c);
        System.out.println("  ✓ Hybrid customer registered: " + c.getDisplayName() + " [" + id + "]");
        return c;
    }

    private void validateNewUsername(String username) {
        for (Customer c : customers.values())
            if (c.getUsername().equalsIgnoreCase(username))
                throw new IllegalArgumentException("Username already taken: " + username);
    }

    public Customer authenticate(String username, String password) {
        for (Customer c : customers.values())
            if (c.authenticate(username, password)) return c;
        return null;
    }

    // ── Account management ────────────────────────────────────────────────────

    public Account openAccount(Customer owner, String type, double initialBalance) {
        String id = "ACC-" + String.format("%04d", ++accountCounter);
        Account account;
        switch (type.toLowerCase()) {
            case "savings": account = new SavingsAccount(id, owner.getCustomerId(), initialBalance); break;
            case "daily":   account = new DailyAccount(id, owner.getCustomerId(), initialBalance);   break;
            default:        account = new GeneralAccount(id, owner.getCustomerId(), initialBalance); break;
        }
        accounts.put(id, account);
        owner.addAccountId(id);
        System.out.printf("  ✓ %s account opened [%s] for %s | Balance: $%.2f%n",
                account.getAccountType(), id, owner.getDisplayName(), initialBalance);
        return account;
    }

    public Account getAccount(String accountId) { return accounts.get(accountId); }

    // ── Transaction queue ─────────────────────────────────────────────────────

    public void enqueueDeposit(Account account, double amount) {
        txQueue.add(new Transaction(TransactionType.DEPOSIT, account, null, amount));
    }

    public void enqueueWithdraw(Account account, double amount) {
        txQueue.add(new Transaction(TransactionType.WITHDRAW, account, null, amount));
    }

    public void enqueueTransfer(Account from, Account to, double amount) {
        txQueue.add(new Transaction(TransactionType.TRANSFER, from, to, amount));
    }

    /**
     * Process all pending transactions sequentially (FIFO).
     */
    public void processAll() {
        while (!txQueue.isEmpty()) {
            Transaction t = txQueue.poll();
            processTransaction(t);
            txHistory.add(t);
        }
    }

    private void processTransaction(Transaction t) {
        System.out.println("\n  ┌─ Processing " + t.getTransactionId() + " ─────────────────────────");
        System.out.printf("  │  Type   : %s%n", t.getType());
        System.out.printf("  │  Amount : $%.2f%n", t.getAmount());
        System.out.printf("  │  From   : %s%n", t.getFromAccount().getAccountId());
        if (t.getToAccount() != null)
            System.out.printf("  │  To     : %s%n", t.getToAccount().getAccountId());

        if (!t.validate()) {
            t.rollback(); // status set to ROLLED_BACK inside rollback()
            System.out.println("  │  Status : ✗ ROLLED BACK — " + t.getFailureReason());
        } else {
            t.execute();
            if (t.getStatus() == TransactionStatus.COMMITTED) {
                System.out.printf("  │  Status : ✓ COMMITTED%n");
                System.out.printf("  │  New Balance (%s): $%.2f%n",
                        t.getFromAccount().getAccountId(), t.getFromAccount().getBalance());
                if (t.getToAccount() != null)
                    System.out.printf("  │  New Balance (%s): $%.2f%n",
                            t.getToAccount().getAccountId(), t.getToAccount().getBalance());
            } else {
                System.out.println("  │  Status : ✗ ROLLED BACK — " + t.getFailureReason());
            }
        }
        System.out.println("  └─────────────────────────────────────────────");
    }

    // ── Reporting ─────────────────────────────────────────────────────────────

    public void applyInterestToAll() {
        System.out.println("\n  [Applying interest to all accounts...]");
        for (Account a : accounts.values()) a.applyInterest();
    }

    public void printAllAccounts() {
        System.out.println("\n  ── All Accounts ──────────────────────────────────");
        if (accounts.isEmpty()) { System.out.println("  No accounts."); return; }
        for (Account a : accounts.values())
            System.out.println("  " + a);
        System.out.println("  ──────────────────────────────────────────────────");
    }

    public void printTransactionHistory() {
        System.out.println("\n  ── Transaction History ───────────────────────────");
        if (txHistory.isEmpty()) { System.out.println("  No transactions yet."); return; }
        for (Transaction t : txHistory) System.out.println("  " + t);
        System.out.println("  ──────────────────────────────────────────────────");
    }

    public void printCustomerStatement(Customer customer) {
        System.out.println("\n  ── Statement for " + customer.getDisplayName() + " ───────────────");
        for (String accId : customer.getAccountIds()) {
            Account a = accounts.get(accId);
            if (a != null) System.out.println("  " + a);
        }
        System.out.println("  ──────────────────────────────────────────────────");
    }

    public String getBankName()          { return bankName;   }
    public List<Transaction> getHistory(){ return Collections.unmodifiableList(txHistory); }
}
