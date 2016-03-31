package com.thecn.app.activities.picturechooser;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.thecn.app.R;
import com.thecn.app.tools.anim.FragmentAnimationInterface;
import com.thecn.app.tools.anim.FragmentAnimationUtil;

/**
* Fragment extended by most fragments that are used by {@link com.thecn.app.activities.picturechooser.PictureChooseActivity}
*/
public abstract class BaseFragment extends Fragment implements FragmentAnimationInterface {

    public static final String LOAD_ERROR = "Could not get image from file.";
    public static final String BACK = "Back";

    /**
     * Casts activity to {@link com.thecn.app.activities.picturechooser.PictureChooseActivity}
     * @return cast activity
     */
    protected PictureChooseActivity getPictureChooseActivity() {
        return (PictureChooseActivity) getActivity();
    }

    /**
     * Set the title of this activity.
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getPictureChooseActivity().setTitle(getTitle());
    }

    /**
     * Returns a valid animation for this fragment, based on whether it is entering or exiting.
     * If the activity is skipping animations, return the default (nothing).
     */
    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        if (getPictureChooseActivity().isSkippingAnimations()) {
            getPictureChooseActivity().setSkippingAnimations(false);
            return super.onCreateAnimation(transit, enter, nextAnim);
        }

        Animation animation = FragmentAnimationUtil.getAnimation(
                enter, this, getPictureChooseActivity()
        );

        if (animation == null) return super.onCreateAnimation(transit, enter, nextAnim);
        return animation;
    }

    @Override
    public Animation getReplacementAnimation(boolean enter) {
        int animID = enter ? R.anim.slide_in_left : R.anim.slide_out_left;
        return AnimationUtils.loadAnimation(getActivity(), animID);
    }

    @Override
    public Animation getPoppingAnimation(boolean enter) {
        int animID = enter ? R.anim.slide_in_right : R.anim.slide_out_right;
        return AnimationUtils.loadAnimation(getActivity(), animID);
    }

    /**
     * Get the title of this fragment
     * @return the title of this fragment
     */
    protected abstract String getTitle();
}
