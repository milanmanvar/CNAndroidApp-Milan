package com.thecn.app.tools.anim;

/**
 * Interface for activities to implement which can be used for for fragment animation.
 * There seems to be a bug in the support library that causes fragment transactions
 * to lose their animations on orientation change.  This is a fix for that.
*/
public interface ActivityFragmentAnimationInterface {
    /**
     * Activity should set a skipping animation global variable initially to true.
     * This way, the first fragment transaction won't be animated with the custom animation
     * whenever the activity is recreated.  After the first transaction, skipping animations variable
     * should be set to false.
     * @return whether activity is skipping custom fragment animations.
     */
    public boolean isSkippingAnimations();

    /**
     * Set the value of the skipping animation variable.
     * @param skippingAnimations whether activity should skip animations
     */
    public void setSkippingAnimations(boolean skippingAnimations);

    /**
     * Tells whether the activity is popping a fragment.  This can be implemented
     * by setting a flag to true in onBackPressed, calling super, then setting it back to false.
     * @return whether activity is popping a fragment.
     */
    public boolean isPoppingFragment();
}
