package com.thecn.app.activities.verification;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.fragments.common.MultiErrorDialogFragment;
import com.thecn.app.fragments.common.ProgressDialogFragment;
import com.thecn.app.fragments.common.SingleErrorDialogFragment;
import com.thecn.app.tools.anim.ActivityFragmentAnimationInterface;
import com.thecn.app.tools.anim.FragmentAnimationInterface;
import com.thecn.app.tools.anim.FragmentAnimationUtil;

import java.util.ArrayList;

/**
* Base fragment that other fragments inherit from in this package
*/
public abstract class BaseFragment extends Fragment implements FragmentAnimationInterface {

    private static final String SESSION_END = "Session has timed out";

    public boolean loading = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    //casting methods for activity

    /**
     * Cast activity
     * @return cast activity
     */
    public ActionBarActivity getActionBarActivity() {
        return (ActionBarActivity) getActivity();
    }

    /**
     * Cast activity
     * @return cast activity
     */
    public ActivityFragmentAnimationInterface getAFAInterface() {
        return (ActivityFragmentAnimationInterface) getActivity();
    }

    //utility methods

    /**
     * Show dialog with one error
     * @param error error to show
     */
    public void showErrorDialog(String error) {
        SingleErrorDialogFragment fragment = SingleErrorDialogFragment.getInstance(error);
        fragment.show(getActionBarActivity().getSupportFragmentManager(), SingleErrorDialogFragment.TAG);
    }

    /**
     * Show dialog with multiple errors
     * @param errors errors to show
     */
    public void showErrorDialog(ArrayList<String> errors) {
        MultiErrorDialogFragment fragment = MultiErrorDialogFragment.getInstance(errors);
        fragment.show(getActionBarActivity().getSupportFragmentManager(), MultiErrorDialogFragment.TAG);
    }

    /**
     * Show visibility dialog
     */
    public void showVisibilityDialog() {
        VisibilityDialog dialog = new VisibilityDialog();
        dialog.show(getActionBarActivity().getSupportFragmentManager(), VisibilityDialog.TAG);
    }

    /**
     * Show time zone dialog
     */
    public void showTimeZoneDialog() {
        TimeZoneDialog dialog = new TimeZoneDialog();
        dialog.show(getActionBarActivity().getSupportFragmentManager(), TimeZoneDialog.TAG);
    }

    /**
     * Get loading dialog if exists
     * @return loading dialog
     */
    protected DialogFragment getLoadingDialog() {
        return ProgressDialogFragment.get(getActivity());
    }

    /**
     * Finish activity with session time out message
     */
    public void finishWithSessionEndMessage() {
        AppSession.showLongToast(SESSION_END);
        getActivity().finish();
    }

    //consitent animation for all fragments

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        Animation animation = FragmentAnimationUtil.getAnimation(
                enter, this, getAFAInterface()
        );

        if (animation == null) return super.onCreateAnimation(transit, enter, nextAnim);
        return animation;
    }

    /**
     * Slide in slide out anim
     */
    @Override
    public Animation getReplacementAnimation(boolean enter) {
        int animID = enter ? R.anim.slide_in_left : R.anim.slide_out_left;
        return AnimationUtils.loadAnimation(getActivity(), animID);
    }

    /**
     * Slide in slide out anim
     */
    @Override
    public Animation getPoppingAnimation(boolean enter) {
        int animID = enter ? R.anim.slide_in_right : R.anim.slide_out_right;
        return AnimationUtils.loadAnimation(getActivity(), animID);
    }

}
