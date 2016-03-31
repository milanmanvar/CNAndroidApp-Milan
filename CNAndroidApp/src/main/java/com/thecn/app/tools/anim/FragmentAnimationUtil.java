package com.thecn.app.tools.anim;

import android.view.animation.Animation;

/**
 * Utility class used for fragment animation along with
 * {@link com.thecn.app.tools.anim.ActivityFragmentAnimationInterface} and
 * {@link com.thecn.app.tools.anim.FragmentAnimationInterface}.
 * There seems to be a bug in the support library that causes fragment transactions
 * to lose their animations on orientation change.  This is a fix for that.
 */
public class FragmentAnimationUtil {

    /**
     * Gets appropriate animation using {@link com.thecn.app.tools.anim.ActivityFragmentAnimationInterface}
     * and {@link com.thecn.app.tools.anim.FragmentAnimationInterface}
     * @param enter whether the fragment implementing {@link com.thecn.app.tools.anim.FragmentAnimationInterface} is entering or exiting
     * @param faInterface fragment implementing this interface
     * @param afaInterface activity implementing this interface
     * @return an animation for the fragment
     */
    public static Animation getAnimation(boolean enter, FragmentAnimationInterface faInterface, ActivityFragmentAnimationInterface afaInterface) {

        Animation animation = null;

        if (afaInterface.isSkippingAnimations()) {
            //skip animations once and then set flag to false
            afaInterface.setSkippingAnimations(false);
        } else {
            //get popping or replacement animation
            if (afaInterface.isPoppingFragment()) {
                animation = faInterface.getPoppingAnimation(enter);
            } else {
                animation = faInterface.getReplacementAnimation(enter);
            }
        }

        return animation;
    }
}
