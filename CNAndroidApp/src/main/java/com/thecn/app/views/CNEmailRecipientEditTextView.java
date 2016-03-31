package com.thecn.app.views;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.Spannable;
import android.text.method.QwertyKeyListener;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Patterns;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ex.chips.RecipientEditTextView;
import com.android.ex.chips.RecipientEntry;
import com.android.ex.chips.recipientchip.DrawableRecipientChip;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.adapters.CNEmailRecipientAdapter;
import com.thecn.app.adapters.CNEmailRecipientAdapter.CNRecipientEntry;
import com.thecn.app.models.content.Email;
import com.thecn.app.models.user.User;
import com.thecn.app.tools.volley.MyVolley;

import java.util.ArrayList;

/**
 * Superclass extended to interface with CNEmailRecipientAdapter.
 * A lot of small changes and duplicated code from super class.
 */
public class CNEmailRecipientEditTextView extends RecipientEditTextView {

    private PopupWindow mLoadingPopup;
    private final float mLoadingViewHalfWidth;

    private Toast mInvalidToast, mDuplicateToast;

    public CNEmailRecipientEditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        setThreshold(0);
        mLoadingViewHalfWidth = getResources().getDimension(R.dimen.loading_popup_width) / 2;

        mInvalidToast = Toast.makeText(context, "Not a CN member or an email address", Toast.LENGTH_LONG);//no I didn't forget
        mInvalidToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        mDuplicateToast = Toast.makeText(context, "This entry is already submitted", Toast.LENGTH_LONG);
        mDuplicateToast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);

        TextView loadingText = (TextView) LayoutInflater.from(context).inflate(R.layout.popup_text, null);
        mLoadingPopup = new PopupWindow(loadingText, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, false);
        mLoadingPopup.setBackgroundDrawable(null);
    }

    public ArrayList<Email.Address> getRecipients() {
        DrawableRecipientChip[] chips = getSortedRecipients();
        ArrayList<Email.Address> addresses = new ArrayList<Email.Address>();

        for (DrawableRecipientChip chip : chips) {
            addIfValid(chip.getEntry(), addresses);
        }

        ArrayList<DrawableRecipientChip> removedChips = getRemovedSpans();

        if (removedChips != null) {
            for (DrawableRecipientChip chip : removedChips) {
                addIfValid(chip.getEntry(), addresses);
            }
        }

        return addresses;
    }

    /**
     * Helper for getRecipients()
     * @param entry entry to add if valid
     * @param addresses list to add recipient to
     */
    private void addIfValid(RecipientEntry entry, ArrayList<Email.Address> addresses) {
        String id;

        if (entry != null) {

            if (entry instanceof CNRecipientEntry) {
                CNRecipientEntry cnEntry = (CNRecipientEntry) entry;

                try {
                    User user = cnEntry.getUser();
                    id = user.getId();

                    if (id != null) {
                        addresses.add(new Email.Address(id, "user"));
                    }
                } catch(NullPointerException e) {
                    // do nothing
                }
            } else {
                try {
                    id = entry.getDestination();

                    if (id != null) {
                        addresses.add(new Email.Address(id, "email"));
                    }
                } catch (NullPointerException e) {
                    //do nothing
                }
            }
        }
    }

    @Override
    public void onSizeChanged(int width, int height, int oldw, int oldh) {
        super.onSizeChanged(width, height, oldw, oldh);
        //redraws popup underneath textview if its size changed
        if (mLoadingPopup.isShowing()) {
            int offsetX = (int) (getWidth() / 2 - mLoadingViewHalfWidth);
            int wrapContent = ViewGroup.LayoutParams.WRAP_CONTENT;
            mLoadingPopup.update(this, offsetX, 0, wrapContent, wrapContent);
        }
    }

    //don't show these errors if the user has selected a chip
    private void showInvalidError() {
        if (mSelectedChip == null) {
            mDuplicateToast.cancel();
            mInvalidToast.show();
        }
    }

    private void showDuplicateError() {
        if (mSelectedChip == null) {
            mInvalidToast.cancel();
            mDuplicateToast.show();
        }
    }

    private Handler mHandler = new Handler(); //should be created on UI thread

    //only UI thread should touch loading popup, so handler is used
    public void showLoading(boolean show) {
        Log.d("OBS", "handler null: " + (mHandler == null));
        if (mHandler != null) {
            if (show) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("OBS", "loading popup null: " + (mLoadingPopup == null));
                        if (mLoadingPopup != null) {
                            int offsetX = (int) (getWidth() / 2 - mLoadingViewHalfWidth);
                            mLoadingPopup.showAsDropDown(CNEmailRecipientEditTextView.this, offsetX, 0);
                        }
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mLoadingPopup != null) {
                            mLoadingPopup.dismiss();
                        }
                    }
                });
            }
        }
    }

    //only UI thread should touch drop down, so handler is used
    public void showDropDown(boolean show) {
        if (mHandler != null) {
            if (show) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mSelectedChip == null) {
                            showDropDown();
                        } else {
                            dismissDropDown();
                        }
                    }
                });
            } else {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        dismissDropDown();
                    }
                });
            }
        }
    }

    @Override
    public <T extends ListAdapter & Filterable> void setAdapter(T adapter) {
        if (adapter instanceof CNEmailRecipientAdapter) {
            super.setAdapter(adapter);
            getAdapter().registerSearchingCallbacks(mSearchingCallbacks);

        } else {
            throw new IllegalArgumentException("Adapter must be a CNEmailRecipientAdapter");
        }
    }

    private CNEmailRecipientAdapter.SearchingCallbacks mSearchingCallbacks
            = new CNEmailRecipientAdapter.SearchingCallbacks() {

        @Override
        public void onSearchStart() {
            showDropDown(false);
            showLoading(true);
        }

        @Override
        public void onSearchComplete() {
            //do nothing
        }

        @Override
        public void onSearchResultsSubmitted() {
            showLoading(false);
            if (hasFocus()) {
                showDropDown(true);
            }
        }

        @Override
        public void onSearchingCancelled() {
            showLoading(false);
            showDropDown(false);
        }
    };

    @Override
    public Parcelable onSaveInstanceState() {
        mLoadingPopup.dismiss(); //prevents WindowLeak exception
        CNEmailRecipientAdapter adapter = getAdapter();

        expand(); //prevents a bug in superclass code:
        //if view is shrunken, then chips are not properly restored when view state is restored
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);

        ss.entries = adapter.getEntries();
        ss.searchKeywords = adapter.getSearchKeywords();
        ss.entriesKeywords = adapter.getEntriesSearchKeywords();
        ss.wasPopupShowing = isPopupShowing();

        return ss;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getAdapter().registerSearchingCallbacks(mSearchingCallbacks);
    }

    @Override
    protected void onDetachedFromWindow() {
        getAdapter().removeSearchCallbacks(); //prevents views from old instance being accessed
        super.onDetachedFromWindow();
    }

    @Override
    public void onScreenStateChanged(int screenState) {
        super.onScreenStateChanged(screenState);
        if (screenState == View.SCREEN_STATE_ON) {
            onReturnToView();
        }
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());

        //restore adapter state
        final CNEmailRecipientAdapter adapter = getAdapter();
        adapter.setEntries(ss.entries);
        adapter.setSearchKeywords(ss.searchKeywords);
        adapter.setEntriesSearchKeywords(ss.entriesKeywords);

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                onReturnToView();

                //replace chips with new chips in case orientation was changed
                replaceAllChips();

                //check all chips to see if they need images, load them if needed, and replace when loaded
                tryLoadChipImages();
            }
        });
    }

    private void onReturnToView() {
        if (hasFocus()) {
            expand();
        } else {
            shrink();
        }

        giveFocus();
    }

    private void giveFocus() {
        if (hasFocus()) {
            //perform filtering if view has focus
            getAdapter().performFiltering();
        } else {
            showLoading(false);
            showDropDown(false);
        }
    }

    private static class SavedState extends BaseSavedState {
        ArrayList<RecipientEntry> entries;
        String searchKeywords;
        String entriesKeywords;
        boolean wasPopupShowing;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            entries = (ArrayList<RecipientEntry>) in.readSerializable();

            String[] stringArray = new String[2];
            in.readStringArray(stringArray);
            searchKeywords = stringArray[0];
            entriesKeywords = stringArray[1];

            boolean[] boolArray = new boolean[1];
            in.readBooleanArray(boolArray);
            wasPopupShowing = boolArray[0];
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeSerializable(entries);
            dest.writeStringArray(new String[]{searchKeywords, entriesKeywords});
            dest.writeBooleanArray(new boolean[]{wasPopupShowing});
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel parcel) {
                        return new SavedState(parcel);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

    @Override
    public void onFocusChanged(boolean hasFocus, int direction, Rect previous) {
        super.onFocusChanged(hasFocus, direction, previous);

        giveFocus();
    }

    private void replaceAllChips() {
        DrawableRecipientChip[] chips = getSortedRecipients();

        for (DrawableRecipientChip chip : chips) {
            replaceChip(chip, chip.getEntry());
        }
    }

    private void tryLoadChipImages() {
        DrawableRecipientChip[] chips = getSortedRecipients();

        for (DrawableRecipientChip chip : chips) {
            if (chip.getEntry() instanceof CNRecipientEntry) {
                CNRecipientEntry entry = (CNRecipientEntry) chip.getEntry();

                if (entry.getIconBitmap() == null) {
                    loadIcon(entry);
                }
            }
        }
    }

    @Override
    public void initDropdownChipLayouter(Context context) {
        setDropdownChipLayouter(new CNEmailRecipientAdapter.CNDropdownChipLayouter(LayoutInflater.from(context), context));
    }

    @Override
    public CNEmailRecipientAdapter getAdapter() {
        return (CNEmailRecipientAdapter) super.getAdapter();
    }

    private boolean userAddedCharacter = true;

    @Override
    protected void performFiltering(final CharSequence text, int keyCode) {
        if (userAddedCharacter) {
            getAdapter().performFiltering(text.toString());
        }
    }

    @Override
    protected void showAlternates(final DrawableRecipientChip currentChip,
                                  final ListPopupWindow alternatesPopup, final int width) {
        //do nothing
    }

    @Override
    protected Bitmap getAvatarIcon(RecipientEntry contact) {
        if (contact instanceof CNRecipientEntry) {
            CNRecipientEntry entry =
                    (CNRecipientEntry) contact;

            if (entry.getIconBitmap() != null) {
                return entry.getIconBitmap();
            }
            return super.getAvatarIcon(entry);

        } else {
            return null;
        }
    }

    private void loadIcon(final CNRecipientEntry contact) {

        try {
            String avatarUrl = contact.getUser().getAvatar().getView_url() + ".w160.jpg";

            MyVolley.getImageLoader().get(avatarUrl, new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                    if (imageContainer.getBitmap() != null) {
                        contact.setIconBitmap(imageContainer.getBitmap());
                        final DrawableRecipientChip chip = findChipForEntry(contact);
                        if (chip != null) {
                            try {
                                final CNRecipientEntry entry = (CNRecipientEntry) chip.getEntry();
                                entry.setIconBitmap(imageContainer.getBitmap());
                                replaceChip(chip, entry);
                            } catch (ClassCastException e) {
                                //do nothing
                            } catch (NullPointerException e) {
                                //do nothing
                            }
                        }
                    }
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    //do nothing
                }
            });

        } catch (NullPointerException e) {
            //data not there, do nothing
        }
    }

    private DrawableRecipientChip findChipForEntry(CNRecipientEntry entry) {
        DrawableRecipientChip[] chips = getSortedRecipients();

        for (DrawableRecipientChip chip : chips) {
            if (chip.getEntry() instanceof CNRecipientEntry) {
                CNRecipientEntry cEntry = (CNRecipientEntry) chip.getEntry();

                try {
                    if (cEntry.getUser().getId().equals(entry.getUser().getId())) {
                        return chip;
                    }
                } catch (NullPointerException e) {
                    //do nothing
                }
            }
        }

        return null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_UP && getEditableText().length() > 0 && mSelectedChip == null) {
            //dirty trick for allowing the popup list to show up again when a piece of text is clicked.
            //append three spaces to end before calculating offset for given touch location
            //if touch was on the text or two of the three-space "touch buffer zones," perform filtering on
            //the text as if new text was just added.
            userAddedCharacter = false; //don't perform filtering when text added programmatically
            Editable editable = getEditableText();
            editable.append("   ");

            if (selectingNonChipText(event)) {
                getAdapter().performFiltering(editable.toString());
            }

            int length = editable.length();
            getEditableText().delete(length - 3, length); //remove spaces after calculation

            userAddedCharacter = true;
        }

        return super.onTouchEvent(event);
    }

    private boolean selectingNonChipText(MotionEvent event) {
        int lastChipEnd = getLastChipEnd();

        float x = event.getX();
        float y = event.getY();
        int offset = putOffsetInRange(x, y);

        return lastChipEnd < offset && offset < getEditableText().length();
    }

    private int getLastChipEnd() {
        DrawableRecipientChip[] chips = getSortedRecipients();

        int lastChipEnd;

        if (chips.length > 0) {
            lastChipEnd = 0;
            Spannable spannable = getSpannable();

            for (DrawableRecipientChip chip : chips) {
                int chipEnd = spannable.getSpanEnd(chip);
                lastChipEnd = Math.max(lastChipEnd, chipEnd);
            }
        } else {
            lastChipEnd = -1;
        }

        return lastChipEnd;
    }

    @Override
    protected boolean commitDefault() {

        // If there is no tokenizer, don't try to commit.
        if (mTokenizer == null) {
            return false;
        }
        Editable editable = getText();
        int end = getSelectionEnd();
        int start = mTokenizer.findTokenStart(editable, end);

        if (shouldCreateChip(start, end)) {
            int whatEnd = mTokenizer.findTokenEnd(getText(), start);

            whatEnd = movePastTerminators(whatEnd);
            if (whatEnd != getSelectionEnd()) {
                //commit chip instead of "handling an edit" like superclass does
                setSelection(editable.length());
                return commitChip(start, whatEnd, editable);
            }
            return commitChip(start, end , editable);
        }
        return false;
    }

    @Override
    protected boolean commitChip(int start, int end, Editable editable) {

        int tokenEnd = mTokenizer.findTokenEnd(editable, start);

        String text = editable.toString().substring(start, tokenEnd).trim().replaceAll("[,;]", "").toLowerCase();
        if (text.length() > 0) {
            char charAt = text.charAt(0);
            if (charAt == '@') {
                text = text.substring(1);
            }
        }

        CNEmailRecipientAdapter adapter = getAdapter();
        if (adapter != null && adapter.getCount() > 0 && enoughToFilter()
                && end == getSelectionEnd() && !isPhoneQuery() && adapter.wereEntriesFoundBy(text)) {
            // choose the first entry.
            submitItemAtPosition(0);
            return true;
        } else {
            clearComposingText();
            //changes made here to superclass code
            if (text != null) {
                if (isValidEmail(text)) {
                    if (!isEmailAddressPresent(text)) {
                        RecipientEntry entry = createTokenizedEntry(text);
                        if (entry != null) {
                            QwertyKeyListener.markAsReplaced(editable, start, end, "");
                            CharSequence chipText = createChip(entry, false);
                            if (chipText != null && start > -1 && end > -1) {
                                editable.replace(start, end, chipText);
                            }
                        }
                        // Only dismiss the dropdown if it is related to the text we
                        // just committed.
                        // For paste, it may not be as there are possibly multiple
                        // tokens being added.
                        if (end == getSelectionEnd()) {
                            dismissDropDown();
                        }
                        sanitizeBetween();
                    } else {
                        removeTrailingSubmissionCharacter();
                        showDuplicateError();
                    }
                } else {
                    removeTrailingSubmissionCharacter();

                    if (!adapter.isWaitingForReply()) {
                        if (hasFocus()) {
                            showInvalidError();
                        }
                    } else {
                        mInvalidToast.cancel();
                    }
                }

                return true;
            }
        }

        return false;
    }

    @Override
    protected void shrink() {
        userAddedCharacter = false; //ignore any changes in text during this method

        if (mTokenizer == null) {
            return;
        }
        long contactId = mSelectedChip != null ? mSelectedChip.getEntry().getContactId() : -1;
        if (mSelectedChip != null && contactId != RecipientEntry.INVALID_CONTACT
                && (!isPhoneQuery() && contactId != RecipientEntry.GENERATED_CONTACT)) {
            clearSelectedChip();
        } else {
            if (getWidth() <= 0) {
                // We don't have the width yet which means the view hasn't been drawn yet
                // and there is no reason to attempt to commit chips yet.
                // This focus lost must be the result of an orientation change
                // or an initial rendering.
                // Re-post the shrink for later.
                mHandler.removeCallbacks(mDelayedShrink);
                mHandler.post(mDelayedShrink);
                return;
            }
            // Reset any pending chips as they would have been handled
            // when the field lost focus.
            if (mPendingChipsCount > 0) {
                postHandlePendingChips();
            } else {
                Editable editable = getText();
                int end = getSelectionEnd();
                int start = mTokenizer.findTokenStart(editable, end);
                DrawableRecipientChip[] chips =
                        getSpannable().getSpans(start, end, DrawableRecipientChip.class);
                if ((chips == null || chips.length == 0)) {

                    commitChip(start, end, editable);
                }
            }
            mHandler.post(mAddTextWatcher);
        }
        createMoreChip();

        userAddedCharacter = true;
    }

    @Override
    protected void expand() {
        userAddedCharacter = false; //ignore changes in text during this method
        super.expand();
        replaceAllChips(); //in case any images loaded, or orientation was changed
        userAddedCharacter = true;
    }

    private void removeTrailingSubmissionCharacter() {
        Editable editable = getEditableText();

        if (editable != null && editable.length() > 0) {
            int lastIndex = editable.length() - 1;
            char charAt = editable.charAt(lastIndex);

            if (charAt == COMMIT_CHAR_COMMA || charAt == COMMIT_CHAR_SEMICOLON) {
                editable.delete(lastIndex, editable.length());
            }
        }
    }

    private boolean isEmailAddressPresent(String text) {
        DrawableRecipientChip[] chips = getSortedRecipients();

        for (DrawableRecipientChip chip : chips) {
            try {
                String destination = chip.getEntry().getDestination();

                if (destination.equals(text)) {
                    return true;
                }
            } catch (NullPointerException e) {
                //well that's weird
            }
        }

        return false;
    }

    public void addUser(User user) {
        CNRecipientEntry entry = CNRecipientEntry.constructEntryFromUser(user);
        submitEntry(entry);
    }

    @Override
    protected void submitItemAtPosition(int position) {

        RecipientEntry tempEntry = getAdapter().getItem(position);

        //any item in the adapter should be a CNRecipientEntry
        if (tempEntry != null && tempEntry instanceof CNRecipientEntry) {
            CNRecipientEntry entry =
                    (CNRecipientEntry) tempEntry;

            submitEntry(entry);

        } else {
            removeTrailingSubmissionCharacter();
        }
    }

    private void submitEntry(CNRecipientEntry entry) {
        String id;
        try {
            id = entry.getUser().getId();
            if (id == null) throw new NullPointerException();
        } catch (NullPointerException e) {
            removeTrailingSubmissionCharacter();
            return; //if no id, then something is wrong here, GET OUT NOW!
        }

        String destination;
        try {
            destination = entry.getDestination();
            if (destination == null) throw new NullPointerException();
        } catch (NullPointerException e) {
            removeTrailingSubmissionCharacter();
            return; //every entry should have a destination (email)
        }

        Log.d("OBS", "id: " + id + "\ndest: " + destination);

        //Go through all chips and check if this is a duplicate
        DrawableRecipientChip[] chips = getSortedRecipients();
        for (DrawableRecipientChip chip : chips) {
            RecipientEntry chipEntry = chip.getEntry();

            if (chipEntry instanceof CNRecipientEntry) {
                CNRecipientEntry cnChipEntry = (CNRecipientEntry) chipEntry;

                try {
                    String chipID = cnChipEntry.getUser().getId();

                    if (id.equals(chipID)) {
                        //don't submit chip if it is the same
                        removeTrailingSubmissionCharacter();
                        showDuplicateError();
                        if (mSelectedChip == null) {
                            showDropDown(true); //bring the drop down back up if it was closed
                            //only do this if this commit attempt wasn't triggered by the user selecting a chip
                        }
                        return;
                    }
                } catch (NullPointerException e) {
                    removeTrailingSubmissionCharacter();
                    return; //something's wrong if a CNRecipientEntry has no user or user id
                }
            } //else, it is an email only chip (not associated with a cn member)
        }

        //if everything's hunky dory, submit the chip!

        clearComposingText();

        Editable editable = getText();
        int end = editable.length();
        int start = mTokenizer.findTokenStart(editable, end);

        QwertyKeyListener.markAsReplaced(editable, start, end, "");
        CharSequence chip = createChip(entry, false);
        if (chip != null && start >= 0 && end >= 0) {
            editable.replace(start, end, chip);
        }
        sanitizeBetween();

        dismissDropDown();

        if (entry.getIconBitmap() == null) {
            //if no icon for this entry, try to load it
            loadIcon(entry);
        }
    }

    public static boolean isValidEmail(CharSequence email) {
        return email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    @Override
    protected void handlePasteClip(ClipData clip) {
        //superclass code...
        if (clip != null && clip.getDescription().hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)){
            for (int i = 0; i < clip.getItemCount(); i++) {
                CharSequence paste = clip.getItemAt(i).getText();
                if (paste != null) {

                    paste = paste.toString().replaceAll(";|,", "");//get rid of any chip separators

                    int start = getSelectionStart();
                    int end = getSelectionEnd();
                    Editable editable = getText();
                    if (start >= 0 && end >= 0 && start != end) {
                        editable.append(paste, start, end);
                    } else {
                        editable.insert(end, paste);
                    }

                    //took out handlePasteAndReplace
                }
            }
        }
    }
}
