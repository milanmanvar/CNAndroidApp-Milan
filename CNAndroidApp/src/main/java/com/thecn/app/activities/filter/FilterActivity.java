package com.thecn.app.activities.filter;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.models.util.TextIconEntry;
import com.thecn.app.tools.network.PostLoader;

/**
 * Allows the user to pick filters to apply to an activity with posts
 */
public class FilterActivity extends ActionBarActivity {

    //used in onSaveInstanceState and onRestoreInstanceState
    public static final String POST_METHODS_KEY = "post_params_key";
    public static final String SHOW_HIDE_KEY = "show_hide_key";

    //used by content choice dialog
    public static final String CONTENT_TYPE_KEY = "content_type_key";
    public static final String CONTENT_CHOICE_KEY = "content_choice_key";

    //display strings
    private static final String SHOW_PP = "Show public posts";
    private static final String HIDE_PP = "Hide public posts";
    private static final String SHOW_GP = "Show posts from global classmates";
    private static final String HIDE_GP = "Hide posts from global classmates";

    //display strings
    public static final String SORT = "Sort";
    public static final String FILTER = "Filter";
    public static final String SORT_FILTER = "Sort/Filter";

    public static final String UNCHANGED = "Sort/Filter unchanged";

    //lists of methods that apply to this filter
    private PostLoader.MethodList list;

    //whether to hide content, either public or global classmates
    //depending on usage
    private boolean hideContents;
    private int contentChoice;

    //used to toggle hiding or showing types of content
    private TextView contentToggleTextView;

    //grouped by sorting and by filtering
    private LinearLayout sortGroup, filterGroup;
    private View contentButton;

    //called by all radio buttons
    private View.OnClickListener radioButtonListener;

    private String[] hideContentsButtonText;

    /**
     * Set title, get view references, set up on click listeners.
     * Set up radio buttons to reflect {@link com.thecn.app.tools.network.PostLoader.MethodList} passed in intent.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_layout);

        ActionBar bar = getSupportActionBar();
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(true);

        list = getIntent().getParcelableExtra(POST_METHODS_KEY);

        sortGroup = (LinearLayout) findViewById(R.id.first_group);
        filterGroup = (LinearLayout) findViewById(R.id.second_group);

        radioButtonListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setOtherRadioButtonsUnchecked((RadioButton) view);
            }
        };

        //dynamically add filter choices to the layout as radio buttons
        //we want a visual separation between the groups but still only
        //want one radio button group, so this has to be done manually
        //because android doesn't support radio groups spread across different views
        int i;
        int firstBound = list.getSecondListStartIndex() >= 0 ? list.getSecondListStartIndex() : list.size();
        for (i = 0; i < firstBound; i++) {
            PostLoader.Method method = list.get(i);
            addRadioButton(sortGroup, method, i);
        }

        for(; i < list.size(); i++) {
            PostLoader.Method method = list.get(i);
            addRadioButton(filterGroup, method, i);
        }

        boolean hasSortGroup = sortGroup.getChildCount() > 0;
        boolean hasFilterGroup = filterGroup.getChildCount() > 0;
        boolean showContentChangeButton = list.contentTypeChangeAllowed();

        int currentFilter = list.getSelectedIndex();
        if (currentFilter > -1) checkRadioButton(currentFilter);

        //check if there is only one type of group, set view appropriately
        if (!hasSortGroup) {
            hideSubTitles();
            sortGroup.setVisibility(View.GONE);

            bar.setTitle(FILTER);
        } else if (!hasFilterGroup) {
            hideSubTitles();
            filterGroup.setVisibility(View.GONE);

            bar.setTitle(SORT);
        } else {
            bar.setTitle(SORT_FILTER);
        }

        //not all filters can filter by content type (Post, Poll, etc.)
        //robust API methods do not exist for the Home Feed, for example.
        contentChoice = list.getContentChoice();
        if (showContentChangeButton) {
            contentButton = findViewById(R.id.content_filter_button);
            contentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showContentDialog();
                }
            });
            sortGroup.setBackgroundResource(R.drawable.selector_white_object);

            setAllButtonIconVisibility();
        } else {
            findViewById(R.id.content_filter_button).setVisibility(View.GONE);
        }

        if (list.showContentToggle()) {
            if (list.interpretAsGlobalPosts()) {
                //applicable to courses global classmates
                hideContents = !list.showGlobalPosts();
                hideContentsButtonText = new String[] {
                        SHOW_GP,
                        HIDE_GP
                };
            } else {
                //applicable to public contents on the home feed
                hideContents = AppSession.getInstance().getUser().isHidePublicContents();
                hideContentsButtonText = new String[] {
                        SHOW_PP,
                        HIDE_PP
                };
            }

            //clicking the view that displays this will toggle it between two states
            contentToggleTextView = (TextView) findViewById(R.id.visibility_text);
            findViewById(R.id.content_toggle).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    hideContents = !hideContents;
                    setContentToggleButtonContent();
                }
            });

            setContentToggleButtonContent();
        } else {
            findViewById(R.id.content_toggle).setVisibility(View.GONE);
        }

        int numSeparateItems = 0;
        numSeparateItems += showContentChangeButton ? 1 : 0;
        numSeparateItems += list.showContentToggle() ? 1 : 0;
        numSeparateItems += hasSortGroup ? 1 : 0;
        numSeparateItems += hasFilterGroup ? 1 : 0;

        //if there are more than one visual groups in the layout, set backgrounds
        //to help divide them visually
        if (numSeparateItems > 1) {
            sortGroup.setBackgroundResource(R.drawable.selector_white_object);
            filterGroup.setBackgroundResource(R.drawable.selector_white_object);
        }

        //set padding
        int vertPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics());
        sortGroup.setPadding(
                sortGroup.getPaddingLeft(),
                vertPadding,
                sortGroup.getPaddingRight(),
                vertPadding
        );

        filterGroup.setPadding(
                filterGroup.getPaddingLeft(),
                0,
                filterGroup.getPaddingRight(),
                0
        );
    }

    //these are labels for different sections

    /**
     * Hide titles for the different sections,
     * e.g. there is only one section, so show the title in the ActionBar instead
     */
    private void hideSubTitles() {
        findViewById(R.id.filter_text).setVisibility(View.GONE);
        findViewById(R.id.sort_text).setVisibility(View.GONE);
    }

//   These methods are necessary in order to have a visual
//   separation inside a radio button group.
//   This is manual implementation of radio buttons

    /**
     * Get the id of the currently checked radio button
     * @return id of the checked radio button
     */
    private int getCheckedRadioButtonId() {
        Integer id;
        id = getCheckedRadioButtonIdFromGroup(sortGroup);
        if (id != null) return id;

        return getCheckedRadioButtonIdFromGroup(filterGroup);
    }

    /**
     * Searches a group for the checked radio button
     * @param group group to search
     * @return index of the checked radio button or null if not found.
     */
    private Integer getCheckedRadioButtonIdFromGroup(ViewGroup group) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton button = (RadioButton) group.getChildAt(i);
            if (button.isChecked()) return button.getId();
        }

        return null;
    }

    /**
     * Check a radio button.
     * @param index index of button to check
     */
    private void checkRadioButton(int index) {
        if (checkRadioButtonIfFoundInGroup(sortGroup, index)) {
            return;
        }

        checkRadioButtonIfFoundInGroup(filterGroup, index);
    }

    /**
     * Check a radio button if found in a radio group.
     * @param group group to check
     * @param index index to check
     * @return true if found, false if not
     */
    private boolean checkRadioButtonIfFoundInGroup(ViewGroup group, int index) {
        for (int i = 0; i < group.getChildCount(); i++) {
            RadioButton button = (RadioButton) group.getChildAt(i);
            if (button.getId() == index) {
                button.setChecked(true);
                setOtherRadioButtonsUnchecked(button);
                return true;
            }
        }

        return false;
    }

    /**
     * Uses mod to uncheck all but the specified radio button.
     * @param button button that should still be checked.
     */
    private void setOtherRadioButtonsUnchecked(RadioButton button) {
        ViewGroup layout = (ViewGroup) button.getParent();

        int childCount, buttonIndex, i;

        if (layout == sortGroup) {

            //buttons parent is sortGroup
            childCount = sortGroup.getChildCount();
            buttonIndex = sortGroup.indexOfChild(button);
            i = (buttonIndex + 1) % sortGroup.getChildCount();

            for (; i != buttonIndex; i = (i + 1) % childCount) {
                setButtonUnchecked(sortGroup, i);
            }

            childCount = filterGroup.getChildCount();
            for (i = 0; i < childCount; i++) {
                setButtonUnchecked(filterGroup, i);
            }
        } else if (layout == filterGroup) {

            //buttons parent is filterGroup
            childCount = sortGroup.getChildCount();
            for (i = 0; i < childCount; i++) {
                setButtonUnchecked(sortGroup, i);
            }

            childCount = filterGroup.getChildCount();
            buttonIndex = filterGroup.indexOfChild(button);
            i = (buttonIndex + 1) % filterGroup.getChildCount();

            for (; i != buttonIndex; i = (i + 1) % childCount) {
                setButtonUnchecked(filterGroup, i);
            }
        }
    }

    /**
     * Make sure View at index is radio button.  If so, uncheck it.
     * @param parent parent that contains the View.
     * @param indexOfChild index where the View should be.
     */
    private void setButtonUnchecked(ViewGroup parent, int indexOfChild) {
        View view = parent.getChildAt(indexOfChild);
        if (!(view instanceof RadioButton)) return;

        RadioButton other = (RadioButton) view;
        other.setChecked(false);
    }

    /**
     * Dynamically add a radio button to a group.
     * Sets the post loader method as the view's tag and displays the method's name.
     * @param group group where the radio button should be added.
     * @param method method this button will be associated with
     * @param index index to use as the button's id (DIFFERENT FROM CHILD INDEX IN GROUP).
     */
    private void addRadioButton(LinearLayout group, PostLoader.Method method, int index) {
        int verticalPadding = (int) getResources().getDimension(R.dimen.dialog_padding);

        RadioButton button = new RadioButton(this);
        button.setId(index);
        button.setLayoutParams(
                new RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                )
        );
        button.setPadding(
                button.getPaddingLeft(),
                verticalPadding,
                button.getPaddingRight(),
                verticalPadding
        );
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        button.setText(method.name);
        button.setTag(method);
        button.setOnClickListener(radioButtonListener);

        group.addView(button);
    }

    /**
     * Sets the content (posts, polls, etc.) filter.
     * Updates view to reflect change.
     * @param contentType specifies content filter type
     */
    public void setContentFilter(int contentType) {
        if (contentButton == null) return;

        list.setContentType(contentType);
        setAllButtonIconVisibility();
    }

    //shows the icons for the type of content being filtered
    //e.g., blue feather for posts, red pie chart for polls, etc.

    /**
     * Shows the icons for the type of content being filtered.
     * e.g., blue feather for posts, red pie chart for polls, etc.
     * Uses {@link com.thecn.app.tools.network.PostLoader.MethodList#getContentChoiceArray(int)} to
     * tell which icons to show.
     *
     * Shows either all possible content types or only one content type.
     */
    private void setAllButtonIconVisibility() {
        int v = View.VISIBLE;
        int g = View.GONE;
        //begin by setting all to gone
        int[] vals = new int[] {g, g, g, g, g};
        int contentType = list.getContentType();
        TextIconEntry[] entries = PostLoader.MethodList.getContentChoiceArray(contentChoice);
        if (contentType == PostLoader.CONTENT_ALL) {
            for (int i = 0; i < 5; i++) {
                //if entry not null, set to visible
                vals[i] = entries[i + 1] != null ? v : g;
            }
        } else {
            //set only one type to visible
            switch (contentType) {
                case PostLoader.CONTENT_POST:
                    vals[0] = v;
                    break;
                case PostLoader.CONTENT_POLL:
                    vals[1] = v;
                    break;
                case PostLoader.CONTENT_QUIZ:
                    vals[2] = v;
                    break;
                case PostLoader.CONTENT_EVENT:
                    vals[3] = v;
                    break;
                case PostLoader.CONTENT_CLASSCAST:
                    vals[4] = v;
                    break;
            }
        }

        setButtonIconVisibility(vals[0], R.id.post_ic);
        setButtonIconVisibility(vals[1], R.id.poll_ic);
        setButtonIconVisibility(vals[2], R.id.quiz_ic);
        setButtonIconVisibility(vals[3], R.id.event_ic);
        setButtonIconVisibility(vals[4], R.id.classcast_ic);
    }

    /**
     * Sets the visibility of a content icon inside the content display button.
     * @param visibility either {@link View#VISIBLE} or {@link View#GONE}
     * @param id id of View whose visibility will be changed.
     */
    private void setButtonIconVisibility(int visibility, int id) {
        View view = contentButton.findViewById(id);
        if (view == null) return;

        view.setVisibility(visibility);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(POST_METHODS_KEY, list);
        if (list.showContentToggle())
            outState.putBoolean(SHOW_HIDE_KEY, hideContents);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        list = savedInstanceState.getParcelable(POST_METHODS_KEY);
        if (list.showContentToggle()) {
            hideContents = savedInstanceState.getBoolean(SHOW_HIDE_KEY);
            setContentToggleButtonContent();
        }
    }

    /**
     * Uses {@link #hideContentsButtonText} to display text
     * based on the usage of this filter activity
     */
    private void setContentToggleButtonContent() {
        int index = hideContents ? 1 : 0;
        contentToggleTextView.setText(hideContentsButtonText[index]);
    }

    /**
     * Inform user that nothing was changed.
     */
    @Override
    public void onBackPressed() {
        AppSession.showShortToast(UNCHANGED);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            AppSession.showShortToast(UNCHANGED);
            finish();
            return true;
        } else if (id == R.id.action_confirm) {
            finishWithResult();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Construct result to give back to calling activity.
     * Passes back {@link com.thecn.app.tools.network.PostLoader.MethodList} in intent.
     */
    private void finishWithResult() {
        list.setSelectedIndex(getCheckedRadioButtonId());

        if (list.interpretAsGlobalPosts()) {
            list.setShowGlobalPosts(!hideContents);
        } else {
            AppSession.getInstance().getUser().setHidePublicContents(hideContents);
        }

        Intent intent = new Intent();
        intent.putExtra(POST_METHODS_KEY, list);

        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     * Shows content dialog so user can set it.
     */
    public void showContentDialog() {
        ContentDialogFragment f = ContentDialogFragment.getInstance(list.getContentType(), contentChoice);
        f.show(getSupportFragmentManager(), ContentDialogFragment.TAG);
    }
}
