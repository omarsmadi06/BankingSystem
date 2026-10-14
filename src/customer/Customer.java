package customer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class representing a bank customer.
 */
public abstract class Customer {

    private final String customerId;
    private final String username;
    private final String password;
    private final List<String> accountIds; // IDs of accounts owned by this customer

    public Customer(String customerId, String username, String password) {
        this.customerId = customerId;
        this.username   = username;
        this.password   = password;
        this.accountIds = new ArrayList<>();
    }

    public boolean authenticate(String username, String password) {
        return this.username.equals(username) && this.password.equals(password);
    }

    public void addAccountId(String accountId) { accountIds.add(accountId); }

    public List<String> getAccountIds() { return Collections.unmodifiableList(accountIds); }

    public abstract String getDisplayName();
    public abstract String getCustomerType();

    public String getCustomerId() { return customerId; }
    public String getUsername()   { return username;   }

    @Override
    public String toString() {
        return String.format("[%s] ID: %s | Name: %s | Username: %s | Accounts: %d",
                getCustomerType(), customerId, getDisplayName(), username, accountIds.size());
    }
}
