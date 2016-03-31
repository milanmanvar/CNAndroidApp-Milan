package com.thecn.app.activities.poll;

import android.content.Context;
import android.widget.RadioButton;

import com.thecn.app.models.content.PollItem;

/**
* Associates a {@link com.thecn.app.models.content.PollItem.Choice} with a radio button.
*/
public class PollRadioButton extends RadioButton {

    private PollItem.Choice mChoice;

    public void setChoiceSelected(boolean selected) {
        mChoice.setSelected(selected);
    }

    public PollRadioButton(Context context, PollItem.Choice choice) {
        super(context);

        mChoice = choice;
        setText(choice.getSubject());
    }
}
