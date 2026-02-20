package com.hanin.smartcareer.models;

/**
 * Represents a single skill gap — a skill the student is missing
 * for a particular career path.
 */
public class SkillGap {

    private String skillName;
    private String explanation;

    /**
     * @param skillName   The missing skill (e.g. "TensorFlow").
     * @param explanation Brief context on why this skill matters for the career.
     */
    public SkillGap(String skillName, String explanation) {
        this.skillName   = skillName;
        this.explanation = explanation;
    }

    public String getSkillName()   { return skillName; }
    public String getExplanation() { return explanation; }
}
