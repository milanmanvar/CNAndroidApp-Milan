package com.thecn.app.models.user;

import com.google.gson.annotations.SerializedName;
import com.thecn.app.models.profile.Country;
import com.thecn.app.models.profile.Institution;
import com.thecn.app.models.profile.Location;
import com.thecn.app.models.profile.SocialNetwork;
import com.thecn.app.models.content.Website;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for user profile construct in server json.
 * Contains additional information about user not included in {@link com.thecn.app.models.user.User}
 */
public class UserProfile implements Serializable {

    @SerializedName("about")
    private String about;

    @SerializedName("tagline")
    private String tagLine;

    @SerializedName("birthday")
    private String birthday;

    @SerializedName("country_id")
    private String countryID;

    //true male false female
    private boolean genderFlag;

    @SerializedName("gender")
    private String gender;

    @SerializedName("current_city_id")
    private String currentCityID;

    @SerializedName("display_about")
    private String displayAbout;

    @SerializedName("primary_email")
    private String primaryEmail;

    @SerializedName("primary_language")
    private String primaryLanguageID;

    @SerializedName("primary_language_name")
    private String primaryLanguage;

    @SerializedName("secondary_email")
    private String secondaryEmail;

    @SerializedName("position")
    private UserPosition userPosition;

    @SerializedName("current_work")
    private UserCurrentWork userCurrentWork;

    @SerializedName("time_zone_id")
    private String timeZoneID;

    @SerializedName("time_zone_name")
    private String timeZone;

    @SerializedName("zip_code")
    private String zipCode;

    @SerializedName("zip_code_detail")
    private Location zipCodeDetail;

    @SerializedName("networks")
    private ArrayList<SocialNetwork> socialNetworks;

    @SerializedName("websites")
    private ArrayList<Website> websites;

    @SerializedName("theme_home_banner_url")
    private String bannerURL;

    @SerializedName("other_fullname")
    private String otherFullName;

    @SerializedName("headline")
    private String headline;

    @SerializedName("institutions")
    private ArrayList<Institution> institutions;

    private transient Country country;

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public boolean getGenderFlag() {
        return genderFlag;
    }

    public void setGenderFlag(boolean genderFlag) {
        this.genderFlag = genderFlag;
    }

    public ArrayList<SocialNetwork> getSocialNetworks() {
        return socialNetworks;
    }

    public void setSocialNetworks(ArrayList<SocialNetwork> socialNetworks) {
        this.socialNetworks = socialNetworks;
    }

    public ArrayList<Website> getWebsites() {
        return websites;
    }

    public void setWebsites(ArrayList<Website> websites) {
        this.websites = websites;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    public String getCountryID() {
        return countryID;
    }

    public void setCountryID(String countryID) {
        this.countryID = countryID;
    }

    public String getCurrentCityID() {
        return currentCityID;
    }

    public void setCurrentCityID(String currentCityID) {
        this.currentCityID = currentCityID;
    }

    public String getDisplayAbout() {
        return displayAbout;
    }

    public void setDisplayAbout(String displayAbout) {
        this.displayAbout = displayAbout;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public String getSecondaryEmail() {
        return secondaryEmail;
    }

    public void setSecondaryEmail(String secondaryEmail) {
        this.secondaryEmail = secondaryEmail;
    }


    public UserPosition getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(UserPosition userPosition) {
        this.userPosition = userPosition;
    }


    public String getPrimaryLanguage() {
        return primaryLanguage;
    }

    public void setPrimaryLanguage(String primaryLanguage) {
        this.primaryLanguage = primaryLanguage;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public UserCurrentWork getUserCurrentWork() {
        return userCurrentWork;
    }

    public void setUserCurrentWork(UserCurrentWork userCurrentWork) {
        this.userCurrentWork = userCurrentWork;
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

    public Location getZipCodeDetail() {
        return zipCodeDetail;
    }

    public void setZipCodeDetail(Location zipCodeDetail) {
        this.zipCodeDetail = zipCodeDetail;
    }

    public String getTimeZoneID() {
        return timeZoneID;
    }

    public void setTimeZoneID(String timeZoneID) {
        this.timeZoneID = timeZoneID;
    }

    public String getBannerURL() {
        return bannerURL;
    }

    public void setBannerURL(String bannerURL) {
        this.bannerURL = bannerURL;
    }

    public String getOtherFullName() {
        return otherFullName;
    }

    public void setOtherFullName(String otherFullName) {
        this.otherFullName = otherFullName;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public ArrayList<Institution> getInstitutions() {
        return institutions;
    }

    public void setInstitutions(ArrayList<Institution> institutions) {
        this.institutions = institutions;
    }

    public String getTagLine() {
        return tagLine;
    }

    public void setTagLine(String tagLine) {
        this.tagLine = tagLine;
    }

    public String getPrimaryLanguageID() {
        return primaryLanguageID;
    }

    public void setPrimaryLanguageID(String primaryLanguageID) {
        this.primaryLanguageID = primaryLanguageID;
    }
}
