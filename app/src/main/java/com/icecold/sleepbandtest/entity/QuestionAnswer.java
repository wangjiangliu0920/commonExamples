package com.icecold.sleepbandtest.entity;

/**
 * @Description: 调查问卷答案
 * @author: MrWang
 * @date: 2019/6/26
 */
public class QuestionAnswer {
    private String title;
    private String value;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "QuestionAnswer{" +
                "title='" + title + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
