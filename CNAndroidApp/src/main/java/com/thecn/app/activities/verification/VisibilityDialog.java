package com.thecn.app.activities.verification;

import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.widget.BaseAdapter;

import com.thecn.app.adapters.SimpleChoiceTextIconAdapter;
import com.thecn.app.fragments.common.ChoiceDialog;
import com.thecn.app.models.util.TextIconEntry;
import com.thecn.app.models.util.VisibilitySetting;

import java.util.ArrayList;

/**
* Shows list of visibility setting choices to user.
*/
public class VisibilityDialog extends ChoiceDialog {
    public static final String TAG = "visibility_dialog";

    /**
     * Gets action to perform when user clicks item in the dialog list.
     * Checks that fragments are a certain class and that objects in adapter are
     * a certain class.  If success, sets the visibility setting.
     * @return action to perform
     */
    @Override
    public DialogInterface.OnClickListener getListener() {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Fragment fragment = getContainerFragment();
                if (fragment != null && fragment instanceof VisibilityFragment) {
                    VisibilityFragment vFragment = (VisibilityFragment) fragment;
                    Object item = getAdapter().getItem(i);
                    if (item != null && item instanceof VisibilityTextIconEntry) {
                        VisibilityTextIconEntry entry = (VisibilityTextIconEntry) item;
                        vFragment.setVisibilitySetting(entry.getVisibilitySetting());
                    }
                }
            }
        };
    }

    /**
     * Construct an adapter to use for the dialog list view.
     * @return adapter to use for list
     */
    @Override
    public BaseAdapter getAdapter() {
        ArrayList<TextIconEntry> list = new ArrayList<>();
        list.add(constructEntry(0, VisibilitySetting.PUBLIC));
        list.add(constructEntry(1, VisibilitySetting.ONLY_ME));
        list.add(constructEntry(2, VisibilitySetting.INSTRUCTOR));
        list.add(constructEntry(3, VisibilitySetting.CLASSMATE));

        return new SimpleChoiceTextIconAdapter(getActivity(), list);
    }

    /**
     * Creates an entry to add to the adapter.
     * @param id id of the entry
     * @param type type of the entry (see {@link com.thecn.app.models.util.VisibilitySetting}
     * @return entry to add to an adapter
     */
    private VisibilityTextIconEntry constructEntry(int id, int type) {
        VisibilitySetting setting = new VisibilitySetting(type);
        int resourceID = setting.getIconResource();

        return new VisibilityTextIconEntry(id, setting, resourceID);
    }

    /**
     * Item to use with list adapter of dialog for showing visibility settings.
     * Associates visibility setting with {@link com.thecn.app.models.util.TextIconEntry}
     */
    public static class VisibilityTextIconEntry extends TextIconEntry {
        private VisibilitySetting data;

        public VisibilityTextIconEntry(int id, VisibilitySetting setting, int resourceID) {
            super(id, setting.toString(), resourceID);
            data = setting;
        }

        public VisibilitySetting getVisibilitySetting() {
            return data;
        }
    }
}
