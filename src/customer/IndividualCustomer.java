package customer;

/**
 * Individual customer — a private person.
 */
public class IndividualCustomer extends Customer {

    private final String fullName;
    private final String nationalId;

    public IndividualCustomer(String customerId, String username, String password,
                              String fullName, String nationalId) {
        super(customerId, username, password);
        this.fullName   = fullName;
        this.nationalId = nationalId;
    }

    @Override public String getDisplayName()   { return fullName;      }
    @Override public String getCustomerType()  { return "Individual";  }
    public    String getNationalId()           { return nationalId;    }
}
