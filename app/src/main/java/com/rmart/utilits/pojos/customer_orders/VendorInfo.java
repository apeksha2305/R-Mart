package com.rmart.utilits.pojos.customer_orders;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.rmart.utilits.pojos.AddressResponse;

import java.io.Serializable;
import java.util.ArrayList;

public class VendorInfo implements Serializable {

    @SerializedName("id")
    @Expose
    Integer userID;

    @SerializedName("first_name")
    @Expose
    String firstName;

    @SerializedName("last_name")
    @Expose
    String lastName;

    @SerializedName("middle_name")
    @Expose
    String middleName;

    @SerializedName("mobile_number")
    @Expose
    String mobileNumber;

    @SerializedName("number")
    @Expose
    String number;

    @SerializedName("address")
    @Expose
    String address;

    @SerializedName("country")
    @Expose
    String country;

    @SerializedName("city")
    @Expose
    String city;

    @SerializedName("state")
    @Expose
    String state;
    @SerializedName("shop_name")
    @Expose
    String shopName;

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
    public String getCompleteAddress() {
        String data = "";
        if(address != null) {
            data += address;
        }
        if(city != null) {
            data += ", "+city;
        }
        if(state != null) {
            data += ", "+state;
        }
        if(country != null) {
            data += ", "+country;
        }
        return data;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }
}
