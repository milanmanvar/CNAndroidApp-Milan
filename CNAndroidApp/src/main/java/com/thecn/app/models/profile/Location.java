package com.thecn.app.models.profile;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.profile.Country;

import java.io.Serializable;

/**
 * Model for location construct in server json.
 * Includes time zone, city, state, country, etc.
 */
public class Location implements Serializable {

    @SerializedName("city")
    private String city;

    @SerializedName("country")
    private Country country;

    @SerializedName("county")
    private String county;

    @SerializedName("id")
    private String id;

    @SerializedName("nation")
    private String nation;

    @SerializedName("state")
    private String state;

    @SerializedName("state_abbreviation")
    private String stateAbbreviation;

    @SerializedName("time_zone")
    private String timeZone;

    @SerializedName("zip_code")
    private String zipCode;

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNation() {
        return nation;
    }

    public void setNation(String nation) {
        this.nation = nation;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStateAbbreviation() {
        return stateAbbreviation;
    }

    public void setStateAbbreviation(String stateAbbreviation) {
        this.stateAbbreviation = stateAbbreviation;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
