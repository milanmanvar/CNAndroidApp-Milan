package com.thecn.app.models.course;

import java.io.Serializable;

/**
 * Created by on 05-01-2016. modyfied by Milan
 */
public class GradebookCategory implements Serializable {

    private String catId,catName,catWeight="",avgGradePer,userGrade,avgWeightPer,catGrade,avgBonusWeight;

    public String getCatId() {
        return catId;
    }

    public String getAvgWeightPer() {
        return avgWeightPer;
    }

    public String getCatGrade() {
        return catGrade;
    }

    public void setCatGrade(String catGrade) {
        this.catGrade = catGrade;
    }

    public void setAvgWeightPer(String avgWeightPer) {
        this.avgWeightPer = avgWeightPer;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GradebookCategory that = (GradebookCategory) o;

        return catId.equals(that.catId);

    }

    @Override
    public int hashCode() {
        return catId.hashCode();
    }

    public void setCatId(String catId) {
        this.catId = catId;
    }

    public String getCatName() {
        return catName;
    }

    public void setCatName(String catName) {
        this.catName = catName;
    }

    public String getCatWeight() {
        return catWeight;
    }

    public void setCatWeight(String catWeight) {
        this.catWeight = catWeight;
    }

    public String getAvgGradePer() {
        return avgGradePer;
    }

    public void setAvgGradePer(String avgGradePer) {
        this.avgGradePer = avgGradePer;
    }

    public String getUserGrade() {
        return userGrade;
    }

    public void setUserGrade(String userGrade) {
        this.userGrade = userGrade;
    }

    public String getAvgBonusWeight() {
        return avgBonusWeight;
    }

    public void setAvgBonusWeight(String avgBonusWeight) {
        this.avgBonusWeight = avgBonusWeight;
    }
}
