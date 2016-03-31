package com.thecn.app.activities.conexus;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;
import com.thecn.app.R;
import com.thecn.app.fragments.BasePostListFragment;
import com.thecn.app.models.conexus.Conexus;
import com.thecn.app.models.conexus.UserScore;
import com.thecn.app.tools.volley.MyVolley;
import com.thecn.app.tools.network.PostLoader;

/**
 * Shows posts from a particular Conexus.
 */

public class ConexusPostsFragment extends BasePostListFragment {
    public static final String TAG = ConexusPostsFragment.class.getSimpleName();
    private static final String FRAGMENT_BUNDLE_CONEXUS_KEY = "conexus";

    private Conexus mConexus;

    private View headerView;

    ImageLoader imageLoader = MyVolley.getImageLoader();
    
    private static final String NEW_POSTS = "New Posts";
    private static final String NEW_REFLECTIONS = "New Reflections";
    private static final String MOST_LIKED = "Most Liked Posts";
    private static final String MOST_REFLECTED = "Most Reflected Posts";
    private static final String MOST_VISITED = "Most Visited Links";

    private static final String HIGHLIGHT = "Highlighted Posts";

    /**
     * @param mConexus must be given a conexus to put into the fragment's arguments.
     * @return a new instance of this class
     */
    public static ConexusPostsFragment newInstance(Conexus mConexus) {
        ConexusPostsFragment fragment = new ConexusPostsFragment();
        Bundle args = new Bundle();
        args.putSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY, mConexus);
        fragment.setArguments(args);

        return fragment;
    }

    /**
     * Construct a set of methods for getting posts that can be set
     * by the filter activity
     * @return a list of methods for retrieving posts
     */
    @Override
    public PostLoader.MethodList getPostLoaderMethodList() {
        PostLoader.MethodList list = new PostLoader.MethodList();
        //cannot show quizzes or classcasts in a conexus
        list.setContentChoice(
                PostLoader.CHOICE_ALL_CONTENT |
                PostLoader.CHOICE_POST        |
                PostLoader.CHOICE_POLL        |
                PostLoader.CHOICE_EVENT
        );
        list.startSecondList();
        list.ensureCapacity(2);

        PostLoader.Method baseMethod = new PostLoader.Method(PostLoader.SOURCE_CONEXUS);
        baseMethod.id = ((Conexus) getActivity().getIntent().getSerializableExtra("conexus")).getId();

        PostLoader.Method method;

        method = new PostLoader.Method(baseMethod);
        method.name = NEW_POSTS;
        list.add(method);

        method = new PostLoader.Method(baseMethod);
        method.name = NEW_REFLECTIONS;
        method.filterType = PostLoader.FILTER_NEW_REFLECTIONS;
        list.add(method);

        method = new PostLoader.Method(baseMethod);
        method.name = MOST_LIKED;
        method.filterType = PostLoader.FILTER_MOST_LIKED;
        list.add(method);

        method = new PostLoader.Method(baseMethod);
        method.name = MOST_REFLECTED;
        method.filterType = PostLoader.FILTER_MOST_REFLECTED;
        list.add(method);

        method = new PostLoader.Method(baseMethod);
        method.name = MOST_VISITED;
        method.filterType = PostLoader.FILTER_MOST_VISITED;
        list.add(method);

        method = new PostLoader.Method(PostLoader.SOURCE_HIGHLIGHT_CONEXUS);
        method.name = HIGHLIGHT;
        method.id = baseMethod.id;
        list.add(method);

        list.setAllowContentTypeChange(true);

        return list;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConexus = (Conexus) getArguments().getSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY);
    }

    /**
     * Additionally instantiates a header view before returning the root view.
     */
    @Override
    public View getRootView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        headerView = getLayoutInflater(savedInstanceState).inflate(R.layout.conexus_header, null);
        return inflater.inflate(R.layout.fragment_post_list, null);
    }

    /**
     * Adds the header view created in {@link com.thecn.app.activities.conexus.ConexusPostsFragment#getRootView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     * Also adds a post button below the header.
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getListView().addHeaderView(headerView, null, false);

        setUpHeaderView();

//        addPostButton(R.id.post_button, R.id.header_post_button);
        getView().findViewById(R.id.post_button).setVisibility(View.GONE);
    }

    /**
     * Sets the conexus image, name, and number.  Also sets the number of anar seeds this user has in this Conexus.
     */
    private void setUpHeaderView() {

        String avatarUrl = mConexus.getLogoURL() + ".w160.jpg";

        ImageView mImageView = (ImageView) headerView.findViewById(R.id.avatarImg);
        imageLoader.get(avatarUrl,
                ImageLoader.getImageListener(mImageView,
                        R.drawable.default_user_icon,
                        R.drawable.default_user_icon));

        TextView conexusNameTxtView = (TextView) headerView.findViewById(R.id.conexusName);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Roboto-Light.ttf");
        conexusNameTxtView.setTypeface(typeface);
        conexusNameTxtView.setText(mConexus.getName());

        String conexusNumber = mConexus.getConexusNumber();
        if (conexusNumber == null) conexusNumber = "";
        TextView conexusIdTxtView = (TextView) headerView.findViewById(R.id.conexus_number);
        conexusIdTxtView.setTypeface(typeface);
        conexusIdTxtView.setText(conexusNumber);

        setUserScore();
    }

    /**
     * Sets views that display the number of the logged in user's anar seeds
     */
    private void setUserScore() {
        UserScore score = mConexus.getUserScore();
        String scoreText = "";

        if (score != null) {
            scoreText = Integer.toString(score.getSubTotal());
            scoreText += " Anar Seeds";

            ((TextView) headerView.findViewById(R.id.anar_number_text))
                    .setText(scoreText);
        }

        if (scoreText.length() == 0) {
            headerView.findViewById(R.id.anar_display_parent)
                    .setVisibility(View.GONE);
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        position --; //account for header
        super.onListItemClick(l, v, position, id);
    }

    /**
     * Used to tell fragments apart
     * @return unique string for this fragment instance
     */
    @Override
    public String toString() {
        Conexus conexus = (Conexus) getArguments().getSerializable(FRAGMENT_BUNDLE_CONEXUS_KEY);
        return TAG + conexus.getId();
    }
}