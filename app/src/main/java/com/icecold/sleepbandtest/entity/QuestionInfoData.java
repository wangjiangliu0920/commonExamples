package com.icecold.sleepbandtest.entity;

import java.util.List;

/**
 * @Description: 调查问卷数据信息
 * @author: MrWang
 * @date: 2019/6/26
 */
public class QuestionInfoData {
    private String templateTitle;
    private List<QuestionProblem> detail;

    public String getTemplateTitle() {
        return templateTitle;
    }

    public void setTemplateTitle(String templateTitle) {
        this.templateTitle = templateTitle;
    }

    public List<QuestionProblem> getDetail() {
        return detail;
    }

    public void setDetail(List<QuestionProblem> detail) {
        this.detail = detail;
    }

    @Override
    public String toString() {
        return "QuestionInfoData{" +
                "templateTitle='" + templateTitle + '\'' +
                ", detail=" + detail +
                '}';
    }
}
