package com.hanin.smartcareer.interfaces;

import com.hanin.smartcareer.models.Career;

/**
 * Callback interface for career card interactions.
 *
 * Implement this in your activity, then pass the listener into
 * {@code CareerAdapter} to decouple the adapter from concrete
 * activity classes. Example usage:
 *
 * <pre>
 * // In CareerSuggestionsActivity:
 * CareerAdapter adapter = new CareerAdapter(careerList, career -> {
 *     Intent intent = new Intent(this, SkillGapActivity.class);
 *     intent.putExtra("career_data", career);
 *     startActivity(intent);
 * });
 * </pre>
 */
public interface OnCareerClickListener {

    /** Called when the user taps "View Skill Gap" on a career card. */
    void onViewSkillGapClicked(Career career);
}
