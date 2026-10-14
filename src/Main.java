import bank.Bank;
import customer.*;
import model.*;
import ui.BankCLI;

/**
 * Entry point for CS310 Bank — Sprint 3.
 * Seeds demo data and launches the interactive CLI.
 */
public class Main {

    public static void main(String[] args) {

        Bank bank = new Bank("CS310 Bank");

        // ── Seed demo customers ────────────────────────────────────────────────
        IndividualCustomer alice = bank.registerIndividual(
                "alice", "1234", "Alice Johnson", "NID-001");
        IndividualCustomer bob = bank.registerIndividual(
                "bob", "5678", "Bob Smith", "NID-002");
        BusinessCustomer acme = bank.registerBusiness(
                "acme", "corp99", "Acme Corp Ltd.", "TAX-555");
        HybridCustomer freelancer = bank.registerHybrid(
                "charlie", "pass1", "Charlie Brown", "NID-003",
                "Charlie Designs LLC", "TAX-888");

        // ── Seed demo accounts ─────────────────────────────────────────────────
        Account aliceGeneral  = bank.openAccount(alice,      "general", 1000.00);
        Account aliceSavings  = bank.openAccount(alice,      "savings",  500.00);
        Account bobDaily      = bank.openAccount(bob,        "daily",    300.00);
        Account acmeGeneral   = bank.openAccount(acme,       "general", 5000.00);
        Account charlieHybrid = bank.openAccount(freelancer, "savings",  750.00);

        // ── Run the interactive CLI ────────────────────────────────────────────
        new BankCLI(bank).run();
    }
}
