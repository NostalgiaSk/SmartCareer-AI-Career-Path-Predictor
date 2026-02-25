package com.hanin.smartcareer.interfaces;

import com.hanin.smartcareer.models.Career;


public interface OnCareerClickListener {

    /** Called when the user taps "View Skill Gap" on a career card. */
    void onViewSkillGapClicked(Career career);
}
