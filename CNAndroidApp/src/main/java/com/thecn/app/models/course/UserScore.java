package com.thecn.app.models.course;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for user score construct in server json
 */
public class UserScore implements Serializable {

    @SerializedName("sub_total")
    private int subTotal;

    @SerializedName("sub_total_seeds")
    private double subTotalSeeds;

    public int getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(int subTotal) {
        this.subTotal = subTotal;
    }

    public double getSubTotalSeeds() {
        return subTotalSeeds;
    }

    public void setSubTotalSeeds(double subTotalSeeds) {
        this.subTotalSeeds = subTotalSeeds;
    }
}
