package com.hanin.smartcareer.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.hanin.smartcareer.models.Career;

/**
 * Stub activity for the Skill Gap detail screen.
 *
 * Receives a {@link Career} object via Intent extra "career_data".
 * A full implementation would display the skill gaps and recommended
 * certifications for the selected career.
 */
public class SkillGapActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve the Career object passed from CareerSuggestionsActivity
        Career career = (Career) getIntent().getSerializableExtra("career_data");

        // TODO: inflate layout and display career.getSkillGaps()
        //       and career.getCertifications() in a dedicated UI.
    }
}
