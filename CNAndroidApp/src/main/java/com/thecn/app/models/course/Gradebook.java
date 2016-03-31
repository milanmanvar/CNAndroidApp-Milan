package com.thecn.app.models.course;

import java.io.Serializable;

/**
 * Created by utsav.k on 04-01-2016.
 */
public class Gradebook implements Serializable {

    private String id,item_grade,avgGrade,grade;
    private String item_name;
    private String grade_letter;
    private boolean isDeletable;
    private boolean isDisplayable;
    private boolean isGradeConvertible;
    private boolean isAnarSeed;
    private boolean isVisible;
    private String item_contentType;
    private String item_type;
    private double item_percentage;
    private String categoryId;

    private GradebookCategory gradebookCategory;

    public GradebookCategory getGradebookCategory() {
        return gradebookCategory;
    }

    public void setGradebookCategory(GradebookCategory gradebookCategory) {
        this.gradebookCategory = gradebookCategory;
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getItem_grade() {
        return item_grade;
    }

    public void setItem_grade(String item_grade) {
        this.item_grade = item_grade;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public String getGrade_letter() {
        return grade_letter;
    }

    public void setGrade_letter(String grade_letter) {
        this.grade_letter = grade_letter;
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    public void setIsDeletable(boolean isDeletable) {
        this.isDeletable = isDeletable;
    }

    public boolean isDisplayable() {
        return isDisplayable;
    }

    public void setIsDisplayable(boolean isDisplayable) {
        this.isDisplayable = isDisplayable;
    }

    public boolean isGradeConvertible() {
        return isGradeConvertible;
    }

    public void setIsGradeConvertible(boolean isGradeConvertible) {
        this.isGradeConvertible = isGradeConvertible;
    }

    public boolean isAnarSeed() {
        return isAnarSeed;
    }

    public void setIsAnarSeed(boolean isAnarSeed) {
        this.isAnarSeed = isAnarSeed;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setIsVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    public String getAvgGrade() {
        return avgGrade;
    }

    public void setAvgGrade(String avgGrade) {
        this.avgGrade = avgGrade;
    }

    public String getItem_contentType() {
        return item_contentType;
    }

    public void setItem_contentType(String item_contentType) {
        this.item_contentType = item_contentType;
    }

    public String getItem_type() {
        return item_type;
    }

    public void setItem_type(String item_type) {
        this.item_type = item_type;
    }

    public double getItem_percentage() {
        return item_percentage;
    }

    public void setItem_percentage(double item_percentage) {
        this.item_percentage = item_percentage;
    }
}
