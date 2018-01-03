package nanosoft.com.phonenumberauthentication.model;

import java.util.List;

/**
 * Created by Jemsheer K D on 24 April, 2017.
 * Package in.techware.lataxidriver.model
 * Project LaTaxiDriver
 */

public class CountryListBean extends BaseBean {
    private List<CountryBean> countries;

    public List<CountryBean> getCountries() {
        return countries;
    }

    public void setCountries(List<CountryBean> countries) {
        this.countries = countries;
    }
}
