package ui;

import bank.Bank;
import customer.Customer;
import model.Account;

import java.util.Scanner;

/**
 * Command-line interface for CS310 Bank.
 * Delegates all business logic to Bank; this class handles only I/O.
 */
public class BankCLI {

    private final Bank    bank;
    private final Scanner scanner;
    private       Customer loggedIn = null;

    public BankCLI(Bank bank) {
        this.bank    = bank;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        printBanner();
        boolean running = true;
        while (running) {
            printMainMenu();
            switch (readLine()) {
                case "1": handleLogin();         break;
                case "2": handleRegister();      break;
                case "3": bank.printAllAccounts(); break;
                case "0":
                    System.out.println("\n  Thank you for using " + bank.getBankName() + ". Goodbye!\n");
                    running = false;
                    break;
                default: System.out.println("  Invalid option.");
            }
        }
        scanner.close();
    }

    // ── Main menu actions ─────────────────────────────────────────────────────

    private void handleLogin() {
        System.out.print("\n  Username: ");
        String user = readLine();
        System.out.print("  Password: ");
        String pass = readLine();
        Customer c = bank.authenticate(user, pass);
        if (c == null) {
            System.out.println("  [FAILED] Invalid credentials.");
            return;
        }
        loggedIn = c;
        System.out.println("  Welcome back, " + loggedIn.getDisplayName() + "!");
        sessionLoop();
        loggedIn = null;
    }

    private void handleRegister() {
        System.out.println("\n  ── Register New Customer ────────────────────────");
        System.out.println("  1. Individual");
        System.out.println("  2. Business");
        System.out.println("  3. Hybrid (Individual + Business)");
        System.out.print("  Choose type: ");
        String type = readLine();
        System.out.print("  Username  : "); String username = readLine();
        System.out.print("  Password  : "); String password = readLine();

        Customer c = null;
        try {
            switch (type) {
                case "1":
                    System.out.print("  Full Name : "); String name = readLine();
                    System.out.print("  National ID: "); String nid = readLine();
                    c = bank.registerIndividual(username, password, name, nid);
                    break;
                case "2":
                    System.out.print("  Company Name: "); String company = readLine();
                    System.out.print("  Tax ID      : "); String tax = readLine();
                    c = bank.registerBusiness(username, password, company, tax);
                    break;
                case "3":
                    System.out.print("  Full Name   : "); String hname   = readLine();
                    System.out.print("  National ID : "); String hnid    = readLine();
                    System.out.print("  Company Name: "); String hcompany = readLine();
                    System.out.print("  Tax ID      : "); String htax    = readLine();
                    c = bank.registerHybrid(username, password, hname, hnid, hcompany, htax);
                    break;
                default:
                    System.out.println("  Invalid type."); return;
            }
        } catch (IllegalArgumentException ex) {
            System.out.println("  [ERROR] " + ex.getMessage()); return;
        }

        // Offer to open an account immediately
        System.out.print("\n  Open an account now? (y/n): ");
        if (readLine().equalsIgnoreCase("y")) openAccountMenu(c);
    }

    // ── Session (logged-in) loop ──────────────────────────────────────────────

    private void sessionLoop() {
        boolean active = true;
        while (active) {
            printSessionMenu();
            switch (readLine()) {
                case "1": doDeposit();             break;
                case "2": doWithdraw();            break;
                case "3": doTransfer();            break;
                case "4": doViewBalance();         break;
                case "5": openAccountMenu(loggedIn); break;
                case "6": bank.printCustomerStatement(loggedIn); break;
                case "0":
                    System.out.println("  Goodbye, " + loggedIn.getDisplayName() + ".");
                    active = false;
                    break;
                default: System.out.println("  Invalid option.");
            }
        }
    }

    // ── Banking operations ────────────────────────────────────────────────────

    private void doDeposit() {
        Account acc = selectAccount("deposit into");
        if (acc == null) return;
        System.out.print("  Amount: $");
        double amount = readDouble();
        bank.enqueueDeposit(acc, amount);
        bank.processAll();
    }

    private void doWithdraw() {
        Account acc = selectAccount("withdraw from");
        if (acc == null) return;
        System.out.print("  Amount: $");
        double amount = readDouble();
        bank.enqueueWithdraw(acc, amount);
        bank.processAll();
    }

    private void doTransfer() {
        Account from = selectAccount("transfer FROM");
        if (from == null) return;
        System.out.print("  Destination Account ID: ");
        String toId = readLine();
        Account to = bank.getAccount(toId);
        if (to == null) { System.out.println("  Account not found: " + toId); return; }
        System.out.print("  Amount: $");
        double amount = readDouble();
        bank.enqueueTransfer(from, to, amount);
        bank.processAll();
    }

    private void doViewBalance() {
        for (String id : loggedIn.getAccountIds()) {
            Account a = bank.getAccount(id);
            if (a != null)
                System.out.printf("  %s [%s] Balance: $%.2f%n",
                        a.getAccountType(), a.getAccountId(), a.getBalance());
        }
    }

    private void openAccountMenu(Customer owner) {
        System.out.println("\n  ── Open New Account ─────────────────────────────");
        System.out.println("  1. General");
        System.out.println("  2. Savings  (min balance $100 | 3.5% interest)");
        System.out.println("  3. Daily    (daily withdrawal limit $500 | 0.5% interest)");
        System.out.print("  Choose type: ");
        String typeChoice = readLine();
        String type;
        switch (typeChoice) {
            case "2": type = "savings"; break;
            case "3": type = "daily";   break;
            default:  type = "general"; break;
        }
        System.out.print("  Opening Balance: $");
        double balance = readDouble();
        try {
            bank.openAccount(owner, type, balance);
        } catch (Exception ex) {
            System.out.println("  [ERROR] " + ex.getMessage());
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Account selectAccount(String purpose) {
        if (loggedIn.getAccountIds().isEmpty()) {
            System.out.println("  You have no accounts. Open one first.");
            return null;
        }
        System.out.println("  ── Your Accounts ────");
        for (String id : loggedIn.getAccountIds()) {
            Account a = bank.getAccount(id);
            if (a != null)
                System.out.printf("  %s [%s] $%.2f%n", a.getAccountType(), id, a.getBalance());
        }
        System.out.print("  Enter account ID to " + purpose + ": ");
        String id = readLine();
        Account a = bank.getAccount(id);
        if (a == null) System.out.println("  Account not found.");
        return a;
    }

    private String readLine() {
        return scanner.nextLine().trim();
    }

    private double readDouble() {
        while (true) {
            try { return Double.parseDouble(readLine()); }
            catch (NumberFormatException e) { System.out.print("  Invalid. Enter a number: $"); }
        }
    }

    // ── UI rendering ──────────────────────────────────────────────────────────

    private void printBanner() {
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║          CS310 BANK  —  Sprint 3             ║");
        System.out.println("║       Object-Oriented Banking System         ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }

    private void printMainMenu() {
        System.out.println("\n┌─── MAIN MENU ─────────────────────────────────┐");
        System.out.println("│  1. Login                                      │");
        System.out.println("│  2. Register New Customer                      │");
        System.out.println("│  3. View All Accounts (Admin)                  │");
        System.out.println("│  0. Exit                                       │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.print("  Choose: ");
    }

    private void printSessionMenu() {
        System.out.println("\n┌─── BANKING MENU ──────────────────────────────┐");
        System.out.printf("│  Logged in as: %-32s│%n", loggedIn.getDisplayName());
        System.out.println("│  1. Deposit                                    │");
        System.out.println("│  2. Withdraw                                   │");
        System.out.println("│  3. Transfer                                   │");
        System.out.println("│  4. View Balance                               │");
        System.out.println("│  5. Open New Account                           │");
        System.out.println("│  6. My Statement                               │");
        System.out.println("│  0. Logout                                     │");
        System.out.println("└────────────────────────────────────────────────┘");
        System.out.print("  Choose: ");
    }
}
