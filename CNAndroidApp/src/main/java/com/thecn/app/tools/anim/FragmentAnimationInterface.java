package com.thecn.app.tools.anim;

import android.view.animation.Animation;

/**
 * Interface for fragments to implement which can be used for for fragment animation.
 * There seems to be a bug in the support library that causes fragment transactions
 * to lose their animations on orientation change.  This is a fix for that.
 */
public interface FragmentAnimationInterface {
    /**
     * Get animation that will be used for a fragment during a replacement transaction
     * @param enter whether this fragment is being replaced or is replacing another fragment (entering)
     * @return appropriate replacement animation
     */
    public Animation getReplacementAnimation(boolean enter);

    /**
     * Get animation that will be used for a fragment during a pop transaction
     * @param enter whether fragment is getting popped or is coming out of the backstack.
     * @return appropriate pop animation
     */
    public Animation getPoppingAnimation(boolean enter);
}
