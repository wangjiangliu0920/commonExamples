package com.icecold.sleepbandtest.entity;

import java.util.List;

/**
 * @Description: 调查问卷题目
 * @author: MrWang
 * @date: 2019/6/26
 */
public class QuestionProblem {
    private List<QuestionAnswer> result;
    private String title;
    private int type;
    private String picture;
    private int id;

    public List<QuestionAnswer> getResult() {
        return result;
    }

    public void setResult(List<QuestionAnswer> result) {
        this.result = result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "QuestionProblem{" +
                "result=" + result +
                ", title='" + title + '\'' +
                ", type=" + type +
                ", picture='" + picture + '\'' +
                ", id=" + id +
                '}';
    }
}
