package com.thecn.app.models.user;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Model for score construct in server json.
 * This represents a user's overall anar seed score.
 */
public class Score implements Serializable {
    @SerializedName("total_seeds")
    private double totalSeeds;

    @SerializedName("total")
    private int total;

    @SerializedName("sub_total_seeds")
    private double sub_totalSeeds;

    @SerializedName("sub_total")
    private int subtotal;

    public double getSub_totalSeeds() {
        return sub_totalSeeds;
    }

    public void setSub_totalSeeds(double sub_totalSeeds) {
        this.sub_totalSeeds = sub_totalSeeds;
    }

    public int getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(int subtotal) {
        this.subtotal = subtotal;
    }

    public double getTotalSeeds() {
        return totalSeeds;
    }

    public void setTotalSeeds(double totalSeeds) {
        this.totalSeeds = totalSeeds;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
