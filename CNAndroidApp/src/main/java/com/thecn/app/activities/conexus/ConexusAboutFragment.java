package com.thecn.app.activities.conexus;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thecn.app.R;
import com.thecn.app.models.conexus.Conexus;

/**
 * Shows a small amount of information about the Conexus.
 */

public class ConexusAboutFragment extends Fragment{

    public static final String TAG = ConexusAboutFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_CONEXUS_KEY = "conexus";

    private Conexus mConexus;

    /**
     * Fragment must have a Conexus in its arguments
     * @param mConexus conexus to show info about
     * @return a new instance
     */
    public static ConexusAboutFragment newInstance(Conexus mConexus) {
        ConexusAboutFragment fragment = new ConexusAboutFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY, mConexus);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mConexus = (Conexus) getArguments().getSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY);
    }

    /**
     * Initializes name, conexus number, and about views
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conexus_about, container, false);

        ((TextView) view.findViewById(R.id.name_content))
                .setText(mConexus.getName());

        ((TextView) view.findViewById(R.id.conexus_number_content))
                .setText(mConexus.getConexusNumber());

        String conexusAbout = mConexus.getAbout();
        if (conexusAbout != null) {
            ((TextView) view.findViewById(R.id.about_message))
                    .setText(Html.fromHtml(conexusAbout));
        } else {
            view.findViewById(R.id.about_layout)
                    .setVisibility(View.GONE);
        }

        return view;
    }
}
