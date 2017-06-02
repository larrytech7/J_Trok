package comn.example.user.j_trok.models;

import java.io.Serializable;

/**
 * Created by Larry Akah on 6/2/17.
 */

public class Price implements Serializable {

    long amount;
    String currency;

    public Price(long amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "Price: "+amount+" "+currency;
    }
}
