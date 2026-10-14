package customer;

/**
 * Business customer — a registered company or organization.
 */
public class BusinessCustomer extends Customer {

    private final String companyName;
    private final String taxId;

    public BusinessCustomer(String customerId, String username, String password,
                            String companyName, String taxId) {
        super(customerId, username, password);
        this.companyName = companyName;
        this.taxId       = taxId;
    }

    @Override public String getDisplayName()  { return companyName;  }
    @Override public String getCustomerType() { return "Business";   }
    public    String getTaxId()               { return taxId;        }
}
