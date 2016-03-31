package com.thecn.app.models.conexus;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for user score json object (number of anar seeds user has in conexus)
 */
public class UserScore implements Serializable {

    @SerializedName("sub_total_seeds")
    private double subTotalSeeds;

    @SerializedName("sub_total")
    private int subTotal;

    public double getSubTotalSeeds() {
        return subTotalSeeds;
    }

    public void setSubTotalSeeds(double subTotalSeeds) {
        this.subTotalSeeds = subTotalSeeds;
    }

    public int getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(int subTotal) {
        this.subTotal = subTotal;
    }
}
