package customer;

/**
 * Hybrid customer — an individual who also operates as a business (e.g. sole trader).
 * Combines personal and business identity.
 */
public class HybridCustomer extends Customer {

    private final String fullName;
    private final String nationalId;
    private final String companyName;
    private final String taxId;

    public HybridCustomer(String customerId, String username, String password,
                          String fullName, String nationalId,
                          String companyName, String taxId) {
        super(customerId, username, password);
        this.fullName    = fullName;
        this.nationalId  = nationalId;
        this.companyName = companyName;
        this.taxId       = taxId;
    }

    @Override
    public String getDisplayName() {
        return fullName + " / " + companyName;
    }

    @Override public String getCustomerType() { return "Hybrid";    }
    public    String getFullName()            { return fullName;    }
    public    String getNationalId()          { return nationalId;  }
    public    String getCompanyName()         { return companyName; }
    public    String getTaxId()               { return taxId;       }
}
