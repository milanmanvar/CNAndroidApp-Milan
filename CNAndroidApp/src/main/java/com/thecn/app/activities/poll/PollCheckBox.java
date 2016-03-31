package com.thecn.app.activities.poll;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;

import com.thecn.app.models.content.PollItem;

/**
* Associates a {@link com.thecn.app.models.content.PollItem.Choice} with a check box.
*/
public class PollCheckBox extends CheckBox {

    private PollItem.Choice mChoice;

    public PollCheckBox(Context context, PollItem.Choice choice) {
        super(context);

        mChoice = choice;
        setText(choice.getSubject());
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mChoice.setSelected(isChecked());
            }
        });
    }
}
