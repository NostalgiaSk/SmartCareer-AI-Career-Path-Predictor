package com.hanin.smartcareer.models;


public class SkillGap {

    private String skillName;
    private String explanation;

    public SkillGap(String skillName, String explanation) {
        this.skillName   = skillName;
        this.explanation = explanation;
    }

    public String getSkillName()   { return skillName; }
    public String getExplanation() { return explanation; }
}
