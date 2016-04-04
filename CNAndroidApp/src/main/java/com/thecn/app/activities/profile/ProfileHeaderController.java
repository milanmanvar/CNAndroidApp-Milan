package com.thecn.app.activities.profile;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.DialogFragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Patterns;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.activities.composeemail.ComposeEmailActivity;
import com.thecn.app.activities.navigation.NavigationActivity;
import com.thecn.app.activities.picturechooser.PictureChooseActivity;
import com.thecn.app.activities.profile.basicinfo.EditBasicInfoActivity;
import com.thecn.app.broadcastreceivers.FollowChangeReceiver;
import com.thecn.app.models.profile.Avatar;
import com.thecn.app.models.profile.Institution;
import com.thecn.app.models.content.Picture;
import com.thecn.app.models.profile.SocialNetwork;
import com.thecn.app.models.user.Score;
import com.thecn.app.models.user.User;
import com.thecn.app.models.user.UserProfile;
import com.thecn.app.models.content.Website;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.stores.UserStore;
import com.thecn.app.tools.text.CNNumberLinker;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.text.InternalURLSpan;
import com.thecn.app.tools.volley.MyVolley;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to control the profile header on a user's profile page.  Sets up header and
 * handles displaying changes to data.
 */
public class ProfileHeaderController {

    //to be used with broadcast receivers
    public static final String INTRO_CHANGE_SUCCESS = ProfileHeaderController.class.getName() + ".intro_change";
    public static final String INFO_CHANGE_SUCCESS = ProfileHeaderController.class.getName() + ".info_change";

    //ratio of width to height for a banner image
    public static final float BANNER_RATIO_NUMERATOR = 256;
    public static final float BANNER_RATIO_DENOMINATOR = 49;

    private static final String EDIT = "Edit";
    private static final String INTRO_ADD = "+ Add an introduction and a tagline to state your academic interests, career goals, research achievements, etc.";

    private static final String FOLLOW = "Follow";
    private static final String FOLLOWING = "Following";

    private static final String NO_FOLLOWING = "No Following";
    private static final String NO_FOLLOWERS = "No Followers";

    private static final String LIST_MEMBERS = "Show in list";
    private static final String SHOW_ALL = "Show all ";

    private CallbackManager<ProfileHomeFragment> callbackManager;

    //custom typeface
    private Typeface typeface;
    //dimensions for loading images
    private int smallImgDimen;
    private int smallImgMargin;

    private View rootView; //root view of header

    private View followButton;
    private ImageView followButtonImg;
    private TextView followButtonText;
    private TextView followingError, followerError;
    private ImageView homeBannerImgView;
    private TextView addCoverTextView;
    private ImageView avatarImgView;
    private String avatarUrl;
    private ImageView flagImgView;
    private ImageView rankImgView;
    private TextView diamondNumberText;
    private LinearLayout institutionSection;
    private TextView introTitle, infoTitle;
    private TextView userNameTxtView, cnNumTxtView, headlineTxtView;
    private TextView anarNumberTxt;
    private View anarSection;
    private View emailButton;
    private View interactionPane, editProfilePicImg;
    private TextView aboutTitleView;
    private View aboutSection;
    private TextView introText;
    private Button introButton, infoButton;
    private View basicInfoSection, aboutNoInfo, findMeNoInfo;
    private TextView basicInfoText;
    private ViewGroup networksView, networkSubView, websitesView;
    private View followingSection, followerSection;
    private ViewGroup followingImgs, followerImgs;
    private TextView followingMore, followerMore;

    //references to callbacks for cancelling, etc.
    private CallbackManager.NetworkCallback<ProfileHomeFragment> followingCallback;
    private CallbackManager.NetworkCallback<ProfileHomeFragment> followerCallback;

    /**
     * Gets references to views and appropriate dimensions for images.
     * DO NOT use the same instance across Activity/Fragment recreation
     */
    public ProfileHeaderController(User user, View view, CallbackManager<ProfileHomeFragment> manager) {
        rootView = view;
        this.callbackManager = manager;

        homeBannerImgView = (ImageView) rootView.findViewById(R.id.cover_img);
        addCoverTextView = (TextView) rootView.findViewById(R.id.add_cover_text);

        avatarImgView = (ImageView) rootView.findViewById(R.id.avatarImg);
        flagImgView = (ImageView) rootView.findViewById(R.id.flag);
        rankImgView = (ImageView) rootView.findViewById(R.id.rank);
        diamondNumberText = (TextView) rootView.findViewById(R.id.diamond_number_text);

        followButton = rootView.findViewById(R.id.follow_button);
        followButtonImg = (ImageView) followButton.findViewById(R.id.follow_img);
        followButtonText = (TextView) followButton.findViewById(R.id.follow_text);

        emailButton = rootView.findViewById(R.id.email_button);

        userNameTxtView = (TextView) rootView.findViewById(R.id.userName);
        cnNumTxtView = (TextView) rootView.findViewById(R.id.cn_number);
        headlineTxtView = (TextView) rootView.findViewById(R.id.headline);

        institutionSection = (LinearLayout) rootView.findViewById(R.id.institution_section);

        anarNumberTxt = (TextView) rootView.findViewById(R.id.anar_number_text);
        anarSection = rootView.findViewById(R.id.anar_display_parent);

        interactionPane = rootView.findViewById(R.id.interaction_pane);
        editProfilePicImg = rootView.findViewById(R.id.edit_profile_pic_img);

        aboutTitleView = (TextView) rootView.findViewById(R.id.about_title);
        aboutSection = rootView.findViewById(R.id.intro_section);

        introTitle = (TextView) aboutSection.findViewById(R.id.intro_title);
        introText = (TextView) aboutSection.findViewById(R.id.intro_text);
        introButton = (Button) aboutSection.findViewById(R.id.edit_about_1);

        basicInfoSection = rootView.findViewById(R.id.info_section);
        infoTitle = (TextView) basicInfoSection.findViewById(R.id.info_title);
        infoButton = (Button) basicInfoSection.findViewById(R.id.edit_about_2);

        aboutNoInfo = rootView.findViewById(R.id.about_no_info);
        findMeNoInfo = rootView.findViewById(R.id.find_me_no_info);

        networksView = (ViewGroup) rootView.findViewById(R.id.networks_layout);
        websitesView = (ViewGroup) rootView.findViewById(R.id.websites_layout);

        networkSubView = (ViewGroup) networksView.findViewById(R.id.networks_sub_layout);

        followingImgs = (ViewGroup) rootView.findViewById(R.id.following_imgs_layout);
        followerImgs = (ViewGroup) rootView.findViewById(R.id.follower_imgs_layout);

        followingSection = rootView.findViewById(R.id.following_section);
        followerSection = rootView.findViewById(R.id.followers_section);

        followingMore = (TextView) rootView.findViewById(R.id.following_more);
        followingError = (TextView) followingSection.findViewById(R.id.following_error_msg);
        followerMore = (TextView) rootView.findViewById(R.id.follower_more);
        followerError = (TextView) followingSection.findViewById(R.id.follower_error_msg);

        basicInfoText = (TextView) rootView.findViewById(R.id.info_text);

        Resources r = getProfileActivity().getResources();
        typeface = Typeface.createFromAsset(getProfileActivity().getAssets(), "fonts/Roboto-Light.ttf");
        smallImgDimen = (int) r.getDimension(R.dimen.fifty_dip);
        smallImgMargin = (int) r.getDimension(R.dimen.five_dip);

        if (user.isMe()) {
            AppSession session = AppSession.getInstance();
            session.setUserProfile(user.getUserProfile());
            session.setUserScore(user.getScore());
            session.setUserRelations(user.getRelations());
        }
    }

    /**
     * Casts activity into profile activity
     * @return cast activity
     */
    private ProfileActivity getProfileActivity() {
        return (ProfileActivity) callbackManager.getActivity();
    }

    /**
     * Cancel callback actions for follow/following network calls
     */
    public void resetFollowingUsers() {
        if (followingCallback != null) followingCallback.cancel();
        if (followerCallback != null) followerCallback.cancel();
    }

    /**
     * Get the user associated with the profile activity
     * @return a user
     */
    private User getUser() {
        return callbackManager.getObject().getUser();
    }

    /**
     * Configure the views to display information about user.
     * Set on click listeners and call layout methods.
     * @param user the user to use to set up the header
     */
    public void setUpHeader(final User user) {

        setProfilePicture(user);
        setFlag(user);

        avatarImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                User user = getUser();

                String viewURL;
                try {
                    viewURL = user.getAvatar().getView_url();
                } catch (Exception e) {
                    return;
                }

                //if user is me, show dialog asking whether to change profile picture or view it.
                if (user.getRelations().isMyself()) {
                    PictureDialogFragment fragment = PictureDialogFragment.getInstance(viewURL);
                    fragment.show(getProfileActivity().getSupportFragmentManager(), "pic_dialog_fragment");
                } else {
                    ProfileHeaderController.openGalleryActivity(getProfileActivity(), viewURL);
                }
            }
        });

        if (user.getUserProfile() != null) {
            layoutBanner(user);
        }

        layoutName(user);
        layoutCNNumber(user);
        layoutHeadline(user);

        doLayoutForInstitutionSection(user);
        setUserScore(user);

        if (user.isMe()) {
            //don't show "Email" or "Follow" buttons if this is me
            interactionPane.setVisibility(View.GONE);
            editProfilePicImg.setVisibility(View.VISIBLE);
        } else {
            User.Relations r = user.getRelations();
            setFollowButtonState(r.isFollowing());
            followButton.setEnabled(!r.isPendingFollowing());

            followButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (AppSession.checkVerification(getProfileActivity())) {
                        return;
                    }

                    User user = getUser();

                    User.Relations r = user.getRelations();
                    if (r == null) return;

                    r.setPendingFollowing(true);
                    followButton.setEnabled(false);

                    boolean following = r.isFollowing();

                    //follow or unfollow depending on state
                    String id = user.getId();
                    if (following) {
                        UserStore.stopFollowingUser(id, new FollowCallback(callbackManager, id, false));
                    } else {
                        UserStore.followUser(id, new FollowCallback(callbackManager, id, true));
                    }
                }
            });

            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //open email activity with this user as the first recipient
                    Activity activity = callbackManager.getObject().getActivity();

                    Intent intent = new Intent(activity, ComposeEmailActivity.class);
                    intent.putExtra(ComposeEmailActivity.RECIPIENT_TAG, getUser());
                    activity.startActivity(intent);
                }
            });
        }

        aboutTitleView.setText("About " + user.getCNNumber());

        layoutIntro(user);
        layoutInfo(user);

        UserProfile profile = user.getUserProfile();
        if (profile == null) return;

        ArrayList<SocialNetwork> networks = profile.getSocialNetworks();
        ArrayList<Website> websites = profile.getWebsites();

        LayoutInflater inflater = callbackManager.getActivity().getLayoutInflater();

        boolean showNetworks = false;
        boolean showWebsites = false;

        if (networks != null) {
            ImageButton imageButtonHolder;

            for (SocialNetwork n : networks) {
                int imageResource = getResourceIdForName(n.getName());

                if (n.getLink() != null && imageResource != -1) {
                    showNetworks = true;

                    //todo make sure all json "name" fields are correct, had to guess and due to bugs on site could not check them all

                    imageButtonHolder = (ImageButton) inflater.inflate(R.layout.image_button_no_bg, networkSubView, false);
                    imageButtonHolder.setImageResource(imageResource);
                    imageButtonHolder.setOnClickListener(new LinkClickListener(n.getLink()));

                    networkSubView.addView(imageButtonHolder);
                }
            }
        }

        if (showNetworks) {
            networksView.setVisibility(View.VISIBLE);
        }

        if (websites != null) {
            View viewHolder;
            TextView textViewHolder;

            for (Website w : websites) {
                if (w.getLink() != null) {
                    showWebsites = true;

                    viewHolder = inflater.inflate(R.layout.website_chip, websitesView, false);
                    viewHolder.setOnClickListener(new LinkClickListener(w.getLink()));

                    textViewHolder = (TextView) viewHolder.findViewById(R.id.website_text);
                    textViewHolder.setText(w.getLink());

                    websitesView.addView(viewHolder);
                }
            }
        }

        if (showWebsites) {
            websitesView.setVisibility(View.VISIBLE);
        }

        boolean noInfo = !showNetworks && !showWebsites;
        if (noInfo) {
            findMeNoInfo.setVisibility(View.VISIBLE);
        }

        doLayoutForFollowSection(user, FollowActivity.FollowFragment.TYPE_FOLLOWING);
        doLayoutForFollowSection(user, FollowActivity.FollowFragment.TYPE_FOLLOWERS);
    }

    /**
     * Handles nullptr when trying to get about information
     * @param user user to get the about of
     * @return the about or null
     */
    private String getAbout(User user) {
        String about = null;
        if (user.getUserProfile() != null) {
            about = user.getUserProfile().getAbout();
        }
        return about == null ? "" : about;
    }

    /**
     * Handles nullptr when trying to get tagline
     * @param user the user to get the tagline from
     * @return tagline or null
     */
    private String getTagline(User user) {
        String tagLine = null;
        if (user.getUserProfile() != null) {
            tagLine = user.getUserProfile().getTagLine();
        }
        return tagLine == null ? "" : tagLine;
    }

    /**
     * Tests whether user has an introduction
     * @param user user to test
     * @return true if yes, false otherwise
     */
    private boolean hasIntro(User user) {
        String about = getAbout(user);
        String tagline = getTagline(user);

        return !about.isEmpty() || !tagline.isEmpty();
    }

    /**
     * Layout the introduction of the user in the about section.
     * @param user The user whose introduction to use
     */
    public void layoutIntro(final User user) {
        boolean noIntro = !hasIntro(user);

        if (user.isMe()) {
            //if user is me, show the about section
            //even if there is no about, so user can edit it.
            aboutSection.setVisibility(View.VISIBLE);

            if (noIntro) {
                introTitle.setVisibility(View.GONE);
                introText.setVisibility(View.GONE);
                introText.setText("");

                //show text for when there is no intro
                introButton.setText(INTRO_ADD);

            } else {
                introTitle.setVisibility(View.VISIBLE);
                introText.setVisibility(View.VISIBLE);

                //show text for when there is an intro
                introButton.setText(EDIT);

                layoutIntroText(user);
            }

            introButton.setVisibility(View.VISIBLE);
            introButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //start intro edit activity

                    Activity activity = callbackManager.getActivity();
                    Intent intent = new Intent(activity, EditIntroActivity.class);
                    String intro = getUser().getUserProfile().getAbout();
                    if (intro != null && !intro.isEmpty()) {
                        intent.putExtra(EditIntroActivity.INTRO_TAG, intro);
                    }
                    String tagLine = getUser().getUserProfile().getTagLine();
                    if (tagLine != null && !tagLine.isEmpty()) {
                        intent.putExtra(EditIntroActivity.TAGLINE_TAG, tagLine);
                    }

                    Bundle translateBundle = ActivityOptionsCompat.makeCustomAnimation(
                            activity, R.anim.slide_in_left, R.anim.slide_out_left
                    ).toBundle();

                    ActivityCompat.startActivity(activity, intent, translateBundle);
                }
            });
        } else if (noIntro) {
            //hide about, no info and user is not me
            aboutSection.setVisibility(View.GONE);
        } else {
            //show about, user is not me
            aboutSection.setVisibility(View.VISIBLE);
            introTitle.setVisibility(View.VISIBLE);
            introText.setVisibility(View.VISIBLE);

            layoutIntroText(user);
        }
    }

    /**
     * Layout the intro text, including the intro and
     * the tag line, applying different look to text using
     * different spans.  Truncates text if too long.
     * @param user User to use for information
     */
    private void layoutIntroText(final User user) {
        CNNumberLinker linker = new CNNumberLinker();
        linker.setCallbackManager(callbackManager);

        final SpannableStringBuilder text = linker.linkify(user.getUserProfile().getAbout());

        introButton.setText(EDIT);

        ViewTreeObserver observer = introText.getViewTreeObserver();
        //listener called before view drawn, all views have their final values (but not drawn yet)
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                introText.getViewTreeObserver().removeOnPreDrawListener(this);

                SpannableStringBuilder newText;

                if (introText.getLineCount() > 5) {

                    //truncate the text, this is too long.
                    View.OnClickListener listener = new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            callbackManager.getObject().showIntroFragment();
                        }
                    };

                    introText.setOnClickListener(listener);

                    InternalURLSpan clicker = new InternalURLSpan(listener);

                    SpannableStringBuilder b = new SpannableStringBuilder();
                    b.append("...(read more)");
                    b.setSpan(clicker, 3, b.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    int end = introText.getLayout().getLineEnd(4) - b.length();
                    newText = new SpannableStringBuilder(text, 0, end);
                    newText.append(b);

                } else {
                    //don't need to truncate text
                    newText = new SpannableStringBuilder(text);
                }

                String tagLine = user.getUserProfile().getTagLine();

                if (!tagLine.isEmpty()) {
                    //add the tag line

                    if (newText.length() > 0) {
                        newText.append("\n\n");
                    }

                    //tag line text is gray, is two times larger, and is in bold.
                    SpannableStringBuilder tagBuilder = new SpannableStringBuilder();
                    tagBuilder.append("\"");
                    tagBuilder.append(tagLine);
                    tagBuilder.append("\"");
                    int len = tagBuilder.length();
                    tagBuilder.setSpan(new RelativeSizeSpan(2f), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tagBuilder.setSpan(new ForegroundColorSpan(Color.GRAY), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    tagBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, len, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    newText.append(tagBuilder);
                }

                introText.setText(newText);

                return true;
            }
        });

        if (introText.getMovementMethod() == null) {
            introText.setMovementMethod(LinkMovementMethod.getInstance());
        }
        introText.setText(text);
    }

    /**
     * Layout the user's basic information using a single text view.
     * Uses different spans to apply differences in font style, size, etc.
     * @param user user to get info from
     */
    public void layoutInfo(final User user) {
        Spannable basicInfo = user.getBasicInfoSpannable();

        if (user.isMe()) {
            if (basicInfo == null) {
                //user has not added basic info, set button
                //to ask user to add some
                infoTitle.setVisibility(View.GONE);
                basicInfoText.setText("");
                basicInfoText.setVisibility(View.GONE);
                infoButton.setText("Add some basic info");

            } else {
                //user has added basic info.  Set button
                //to display "edit"
                infoTitle.setVisibility(View.VISIBLE);
                basicInfoText.setText(basicInfo);
                basicInfoText.setVisibility(View.VISIBLE);
                infoButton.setText(EDIT);
            }

            //edit button visible if user is me
            infoButton.setVisibility(View.VISIBLE);
            infoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //open basic info activity
                    Activity activity = callbackManager.getActivity();
                    Intent intent = new Intent(activity, EditBasicInfoActivity.class);

                    Bundle translateBundle = ActivityOptionsCompat.makeCustomAnimation(
                            activity, R.anim.slide_in_left, R.anim.slide_out_left
                    ).toBundle();

                    ActivityCompat.startActivity(activity, intent, translateBundle);
                }
            });
            basicInfoSection.setVisibility(View.VISIBLE);

        } else if (basicInfo == null) {
            //user not me, no basic info
            basicInfoSection.setVisibility(View.GONE);

            if (!hasIntro(user)) {
                aboutNoInfo.setVisibility(View.VISIBLE);
            }
        } else {
            //user not me, basic info available
            basicInfoSection.setVisibility(View.VISIBLE);
            infoTitle.setVisibility(View.VISIBLE);
            basicInfoText.setText(basicInfo);
            basicInfoText.setVisibility(View.VISIBLE);
            infoButton.setVisibility(View.GONE);
        }
    }

    /**
     * Sets the user's flag into the flag view next to profile image.
     * @param user user to get flag info from
     */
    private void setFlag(User user) {
        if (user.getCountry() == null || user.getCountry().getFlagURL() == null) return;

        MyVolley.ImageParams params = new MyVolley.ImageParams(
                user.getCountry().getFlagURL(),
                flagImgView
        );
        DisplayMetrics dm = callbackManager.getActivity().getResources().getDisplayMetrics();
        params.maxWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, dm);
        params.maxHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14.4f, dm);
        params.placeHolderID = params.errorImageResourceID = R.color.white;

        MyVolley.loadImage(params);
    }

    /**
     * Sets the banner into the banner image view.  Shows editing options if the user is me.
     * Shows grayish gradient if no banner found
     * @param user
     */
    public void layoutBanner(User user) {
        final String bannerURL = user.getUserProfile().getBannerURL();
        if (bannerURL == null || bannerURL.isEmpty()) return;

        homeBannerImgView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                homeBannerImgView.getViewTreeObserver().removeOnPreDrawListener(this);

                int w = homeBannerImgView.getWidth();
                int h = homeBannerImgView.getHeight();

                //make sure bounds of picture allow for highest resolution possible
                float ratio = (BANNER_RATIO_NUMERATOR * h) / (BANNER_RATIO_DENOMINATOR * w);
                if (ratio < 1) {
                    h = (int)(w * BANNER_RATIO_DENOMINATOR / BANNER_RATIO_NUMERATOR);
                } else if (ratio > 1) {
                    w = (int)(h * BANNER_RATIO_NUMERATOR / BANNER_RATIO_DENOMINATOR);
                }

                MyVolley.ImageParams params = new MyVolley.ImageParams(bannerURL, homeBannerImgView);
                params.placeHolderID = params.errorImageResourceID = R.drawable.banner_gradient;
                params.maxWidth = w;
                params.maxHeight = h;

                MyVolley.loadImage(params);

                return true;
            }
        });

        if (!user.isMe()) return;

        //show text that tells user to click to change banner
        addCoverTextView.setVisibility(View.VISIBLE);

        homeBannerImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = callbackManager.getActivity();
                Intent intent = new Intent(activity, PictureChooseActivity.class);
                intent.putExtra(PictureChooseActivity.TYPE_TAG, PictureChooseActivity.TYPE_BANNER);
                activity.startActivity(intent);
            }
        });
    }

    /**
     * Layout the user's name.
     * @param user user to get the name from
     */
    private void layoutName(User user) {
        String text = "";

        String userName = user.getDisplayName();
        if (userName != null) {
            text += userName;
        }

        String otherFullName = null;
        if (user.getUserProfile() != null) {
            otherFullName = user.getUserProfile().getOtherFullName();
        }
        if (otherFullName != null && !otherFullName.isEmpty()) {
            text += " (" + otherFullName + ")";
        }

        userNameTxtView.setTypeface(typeface);
        userNameTxtView.setText(text);
    }

    /**
     * Layout user's cn number
     * @param user user to get info from
     */
    private void layoutCNNumber(User user) {
        if (user.getCNNumber() != null) {
            cnNumTxtView.setText("@" + user.getCNNumber());
        } else {
            cnNumTxtView.setText("");
        }
    }

    /**
     * Layout the user's headline (displayed below profile picture).
     * @param user user to get the headline from.
     */
    private void layoutHeadline(User user) {
        String headline = null;

        if (user.getUserProfile() != null) {
            headline = user.getUserProfile().getHeadline();
            if (headline != null && !headline.isEmpty()) {
                headlineTxtView.setTypeface(typeface);
                headlineTxtView.setText(headline);
                headlineTxtView.setVisibility(View.VISIBLE);
            } else {
                headline = null;
            }
        }

        if (headline == null) {
            headlineTxtView.setVisibility(View.GONE);
        }
    }

    /**
     * Layout the user's institutions.  Circular images that are clickable
     * if a link is associated with them.
     * @param user user to get institutions from.
     */
    private void doLayoutForInstitutionSection(final User user) {
        ArrayList<Institution> institutions = null;

        if (user.getUserProfile() != null) {
            institutions = user.getUserProfile().getInstitutions();
        }
        if (institutions == null || institutions.size() == 0) {
            institutionSection.setVisibility(View.GONE);
            return;
        }

        institutionSection.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                institutionSection.getViewTreeObserver().removeOnPreDrawListener(this);

                ArrayList<Institution> institutions = user.getUserProfile().getInstitutions();
                View dummyView = institutionSection.findViewById(R.id.institution_dummy);

                //only get image as large as needed
                int imgWidth = smallImgDimen + smallImgMargin;
                int maxImgsInRow = dummyView.getWidth() / imgWidth;

                institutionSection.removeAllViews();

                if (institutions.size() <= maxImgsInRow) {
                    //only one row needed
                    institutionSection.setOrientation(LinearLayout.HORIZONTAL);
                    layoutInstitutionRow(institutions, institutionSection);
                } else {
                    //more than one row needed (use nested linear layouts)
                    int numRows = (institutions.size() + maxImgsInRow - 1) / maxImgsInRow;
                    int start = 0;
                    int end = maxImgsInRow;
                    List<Institution> subList;

                    for (int i = 0; i < numRows - 1; i++) {
                        subList = institutions.subList(start, end);
                        LinearLayout layout = getLinearLayout();
                        LinearLayout.LayoutParams p = getParams();
                        p.bottomMargin = smallImgMargin;
                        institutionSection.addView(layout, p);
                        layoutInstitutionRow(subList, layout);

                        start = end;
                        end += maxImgsInRow;
                    }

                    subList = institutions.subList(start, institutions.size());
                    LinearLayout layout = getLinearLayout();
                    institutionSection.addView(layout, getParams());
                    layoutInstitutionRow(subList, layout);
                }

                return false;
            }

            /**
             * Get an instantiated linear layout
             * @return linear layout
             */
            private LinearLayout getLinearLayout() {
                LinearLayout layout = new LinearLayout(callbackManager.getActivity());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                return layout;
            }

            /**
             * Get layout params
             * @return layout params
             */
            private LinearLayout.LayoutParams getParams() {
                return new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
            }

            /**
             * Sets up row of image views to show some of a user's institutions.
             * @param institutions list of institutions to show in image views
             * @param layout parent layout of the image views.
             */
            private void layoutInstitutionRow(List<Institution> institutions, LinearLayout layout) {
                //padding between image views.
                int padding = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        2f,
                        callbackManager.getActivity().getResources().getDisplayMetrics()
                );

                for (Institution inst : institutions) {
                    //set up a new image view (add dynamically
                    ImageView imgView = new ImageView(callbackManager.getActivity());
                    imgView.setScaleType(ImageView.ScaleType.FIT_XY);
                    layout.addView(imgView);
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) imgView.getLayoutParams();
                    mlp.width = mlp.height = smallImgDimen;
                    mlp.rightMargin = smallImgMargin;
                    imgView.setPadding(padding, padding, padding, padding);
                    imgView.setBackgroundResource(R.drawable.circle_bg);

                    String holder;

                    holder = inst.getLink();
                    if (holder != null && !holder.isEmpty()) {
                        //on click link to external web app
                        imgView.setOnClickListener(new LinkClickListener(holder));
                    }

                    holder = inst.getLogoURL();
                    if (holder != null && !holder.isEmpty()) {
                        //load image if exists
                        MyVolley.ImageParams params = new MyVolley.ImageParams(holder, imgView);
                        params.placeHolderID = params.errorImageResourceID = R.color.white;
                        params.maxWidth = params.maxHeight = smallImgDimen;
                        params.circle = true;

                        MyVolley.loadImage(params);
                    }
                }
            }
        });
    }

    /**
     * Layout a row of the user's followers or following.
     * @param user user to get info from.
     * @param type either follower or following
     */
    private void doLayoutForFollowSection(final User user, final int type) {
        final View dummyView;     //forces view to have its max possible width
        final ViewGroup imgGroup; //group that holds images
        final TextView moreView;  //shows message that tells user there is more
        final View sectionView;   //section of layout to add on click listener to
        final Integer userCount;  //count of users
        String noneMessage;       //message to display if there are no users

        //get references to proper set of views
        if (type == FollowActivity.FollowFragment.TYPE_FOLLOWING) {
            dummyView = rootView.findViewById(R.id.following_dummy);
            imgGroup = followingImgs;
            moreView = followingMore;
            sectionView = followingSection;
            noneMessage = NO_FOLLOWING;

            if (user.getCount() != null ) {
                userCount = user.getCount().following;
            } else {
                userCount = 0;
            }
        } else {
            dummyView = rootView.findViewById(R.id.follower_dummy);
            imgGroup = followerImgs;
            moreView = followerMore;
            sectionView = followerSection;
            noneMessage = NO_FOLLOWERS;

            if (user.getCount() != null) {
                userCount = user.getCount().follower;
            } else {
                userCount = 0;
            }
        }

        //no users, show none message
        if (userCount <= 0) {
            imgGroup.setVisibility(View.GONE);
            moreView.setText(noneMessage);
            return;
        }

        dummyView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                dummyView.getViewTreeObserver().removeOnPreDrawListener(this);

                //get the number of users that can fit in the view
                int imgWidth = smallImgDimen + smallImgMargin;
                int numFollowingToGet = dummyView.getWidth() / imgWidth;

                if (userCount <= numFollowingToGet) {
                    //message tells that users can be shown in a list
                    numFollowingToGet = userCount;
                    moreView.setText(LIST_MEMBERS);
                } else {
                    //message tells that there are more users (will be shown in list)
                    moreView.setText(SHOW_ALL + userCount);
                }

                imgGroup.removeAllViews();

                for (int i = 0; i < numFollowingToGet; i++) {
                    //create image view dynamically
                    ImageView imgView = new ImageView(callbackManager.getActivity());
                    imgGroup.addView(imgView);
                    ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) imgView.getLayoutParams();
                    mlp.width = mlp.height = smallImgDimen;
                    mlp.rightMargin = smallImgMargin;
                }

                sectionView.setOnClickListener(
                        new FollowButtonListener(type)
                );

                //set appropriate on click listener
                if (type == FollowActivity.FollowFragment.TYPE_FOLLOWING) {
                    followingCallback = new UserListCallback(type, callbackManager);
                    UserStore.getUserFollowing(user.getId(), numFollowingToGet, 0, followingCallback);
                } else {
                    followerCallback = new UserListCallback(type, callbackManager);
                    UserStore.getUserFollowers(user.getId(), numFollowingToGet, 0, followerCallback);
                }

                return false;
            }
        });
    }

    /**
     * Used to make decisions when a list of following or follower users is grabbed from server
     */
    private static class UserListCallback extends CallbackManager.NetworkCallback<ProfileHomeFragment> {
        private int type;
        private View sectionView;
        private ViewGroup imgGroup;
        private TextView moreView, errorView;

        private static final String DEFAULT_ERROR_MSG = "Could not load data";

        public UserListCallback(int type, CallbackManager<ProfileHomeFragment> manager) {
            super(manager);
            this.type = type;
        }

        /**
         * Get the proper views to change.
         */
        @Override
        public void onResumeBefore(ProfileHomeFragment object) {
            ProfileHeaderController controller = object.getProfileHeaderController();
            if (type == FollowActivity.FollowFragment.TYPE_FOLLOWING) {
                imgGroup = controller.followingImgs;
                sectionView = controller.followingSection;
                moreView = controller.followingMore;
                errorView = controller.followingError;
            } else {
                imgGroup = controller.followerImgs;
                sectionView = controller.followerSection;
                moreView = controller.followerMore;
                errorView = controller.followerError;
            }
        }

        /**
         * Add follower or following users to their respective views.
         */
        @Override
        public void onResumeWithResponse(ProfileHomeFragment object) {
            ArrayList<User> users = UserStore.getListData(response);

            int numViews = imgGroup.getChildCount();

            //count of users and number of views should be same
            if (users == null || users.size() != numViews) {
                onResumeWithError(object);
                return;
            }

            ProfileHeaderController controller = object.getProfileHeaderController();
            int imgDimen = controller.smallImgDimen;

            for (int i = 0; i < numViews; i++) {
                final User user = users.get(i);
                Avatar avatar = user.getAvatar();

                if (avatar.getView_url() != null) {
                    ImageView imgView = (ImageView) imgGroup.getChildAt(i);
                    imgView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //open user's profile
                            manager.getObject().getNavigationActivity()
                                    .openProfileByID(user.getId());
                        }
                    });

                    //get user's profile picture
                    MyVolley.ImageParams params = new MyVolley.ImageParams(
                            avatar.getView_url(),
                            imgView
                    );
                    params.placeHolderID = params.errorImageResourceID = R.drawable.default_user_icon;
                    params.maxWidth = params.maxHeight = imgDimen;
                    params.circle = true;

                    MyVolley.loadImage(params);
                }
            }
        }

        @Override
        public void onResumeWithError(ProfileHomeFragment object) {
            //show an error for why users not shown
            imgGroup.setVisibility(View.GONE);
            String error = StoreUtil.getFirstResponseError(response);
            if (error == null) error = DEFAULT_ERROR_MSG;
            moreView.setVisibility(View.GONE);
            if(errorView!=null) {
                errorView.setText(error);
                errorView.setVisibility(View.VISIBLE);
            }
            sectionView.setOnClickListener(null);
        }
    }

    /**
     * Called when user clicks on either the follower or following section
     */
    private class FollowButtonListener implements View.OnClickListener {
        private int type;

        /**
         * Create new follow button listener
         * @param type denotes either following or follower
         */
        public FollowButtonListener(int type) {
            this.type = type;
        }

        /**
         * Open FollowActivity with proper type.
         */
        @Override
        public void onClick(View v) {
            ProfileActivity activity = getProfileActivity();
            Intent intent = new Intent(activity, FollowActivity.class);
            intent.putExtra(FollowActivity.ARG_ID, getUser().getId());
            intent.putExtra(FollowActivity.ARG_CN_NUMBER, getUser().getCNNumber());
            intent.putExtra(FollowActivity.ARG_TYPE, type);

            Bundle translateBundle = ActivityOptionsCompat.makeCustomAnimation(
                    activity, R.anim.slide_in_left, R.anim.slide_out_left
            ).toBundle();

            ActivityCompat.startActivity(activity, intent, translateBundle);
        }
    }

    /**
     * Called when a link is clicked from either a website listed in "Find me"
     * or from an institution of the user.
     */
    private class LinkClickListener implements View.OnClickListener {
        private Uri uri;
        private static final String INSTALL = "Please install a web browser";
        private static final String INVALID = "Invalid URL";
        private static final String SCHEME = "http://";

        public LinkClickListener(String link) {
            //add scheme if missing
            uri = Uri.parse(link);
            String scheme = uri.getScheme();
            if (scheme == null || scheme.isEmpty()) {
                uri = Uri.parse(SCHEME + link);
            }
        }

        /**
         * Open web app.  If none, give error.
         */
        @Override
        public void onClick(View v) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                callbackManager.getObject().startActivity(intent);

            } catch (ActivityNotFoundException e) {
                String message =
                        Patterns.WEB_URL.matcher(uri.toString()).matches() ?
                                INSTALL : INVALID;

                AppSession.showLongToast(message);
            }
        }
    }

    /**
     * Gets image to display for social networks by their name.
     * @param name name of social network
     * @return image id
     */
    private int getResourceIdForName(String name) {
        if (name == null) return -1;

        switch (name) {
            case "facebook":
                return R.drawable.ic_facebook;
            case "twitter":
                return R.drawable.ic_twitter;
            case "linkedin":
                return R.drawable.ic_linkedin;
            case "googleplus":
                return R.drawable.ic_googleplus;
            case "tumblr":
                return R.drawable.ic_tumblr;
            case "youtube":
                return R.drawable.ic_youtube;
            case "vimeo":
                return R.drawable.ic_vimeo;
            case "blogger":
                return R.drawable.ic_blogger;
            case "pinterest":
                return R.drawable.ic_pinterest;
            default:
                return -1;
        }
    }

    /**
     * Sets the user's profile picture into image view.
     * @param user user to get the profile pic url from
     */
    public void setProfilePicture(User user) {
        String newAvatarUrl = user.getAvatar().getView_url() + ".w160.jpg";

        if (avatarUrl != null && avatarUrl.equals(newAvatarUrl)) return;

        avatarUrl = newAvatarUrl;

        //only get image as large as needed.
        int dimen = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                65f,
                getProfileActivity().getResources().getDisplayMetrics()
        );

        //get image
        MyVolley.ImageParams params = new MyVolley.ImageParams(avatarUrl, avatarImgView);
        params.placeHolderID = params.errorImageResourceID = R.drawable.default_user_icon;
        params.circle = true;
        params.maxWidth = params.maxHeight = dimen;

        MyVolley.loadImage(params);
    }

    /**
     * Shows choice to user allowing them to either change their profile picture
     * or view it.
     */
    public static class PictureDialogFragment extends DialogFragment {

        private static final String URL_KEY = "view_url";

        /**
         * Get fragment with arguments.
         * @param imgViewURL location of profile pic
         * @return new instance of this class
         */
        public static PictureDialogFragment getInstance(String imgViewURL) {
            Bundle args = new Bundle();
            args.putString(URL_KEY, imgViewURL);

            PictureDialogFragment fragment = new PictureDialogFragment();
            fragment.setArguments(args);
            return fragment;
        }

        /**
         * Create dialog
         */
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            String[] options = new String[] {"View picture", "Change picture"};
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    dismiss();

                    switch (which) {
                        case 0:
                            openGalleryActivity();
                            break;
                        case 1:
                            openProfilePictureActivity();
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                    .setItems(options, listener);

            return builder.create();
        }

        /**
         * Opens {@link com.thecn.app.activities.PhotoGalleryViewerActivity}
         */
        private void openGalleryActivity() {
            String viewUrl = getArguments().getString(URL_KEY);
            if (viewUrl == null) return;

            ProfileHeaderController.openGalleryActivity(getNavigationActivity(), viewUrl);
        }

        /**
         * Opens {@link com.thecn.app.activities.picturechooser.PictureChooseActivity}
         */
        private void openProfilePictureActivity() {
            Intent intent = new Intent(getActivity(), PictureChooseActivity.class);
            getActivity().startActivity(intent);
        }

        /**
         * Casts activity to {@link com.thecn.app.activities.navigation.NavigationActivity}
         * @return cast activity
         */
        private NavigationActivity getNavigationActivity() {
            return (NavigationActivity) getActivity();
        }
    }

    /**
     * Opens {@link com.thecn.app.activities.PhotoGalleryViewerActivity}
     * @param activity navigation activity to use to call opening method
     * @param viewURL view url of picture to show
     */
    public static void openGalleryActivity(NavigationActivity activity, String viewURL) {
        Picture pic = new Picture();
        pic.setPictureURL(viewURL);
        ArrayList<Picture> picsArr = new ArrayList<Picture>();
        picsArr.add(pic);
        activity.openPhotoGalleryViewerActivity(picsArr, 0);
    }

    /**
     * Custom on click listener interface
     */
    public static interface OnClickListener {
        public void onClick(ProfileHeaderController controller);
    }

    /**
     * Set the follow button enabled or disabled
     * @param enabled whether to enable button
     */
    public void setFollowButtonEnabled(boolean enabled) {
        if (followButton == null) return;
        followButton.setEnabled(enabled);
    }

    /**
     * Used to make decisions for when a network request to follow or unfollow a user returns
     */
    private static class FollowCallback extends CallbackManager.NetworkCallback<ProfileHomeFragment> {
        private static final String FAIL_FOLLOW = "Could not follow user";
        private static final String FAIL_UNFOLLOW = "Could not unfollow user";

        private String userID;
        private boolean follow;

        /**
         * Create new instance
         * @param manager manages callbacks
         * @param userID id of user to follow/unfollow
         * @param follow whether user is attempted to follow or unfollow
         */
        public FollowCallback(CallbackManager<ProfileHomeFragment> manager, String userID, boolean follow) {
            super(manager);
            this.userID = userID;
            this.follow = follow;
        }

        /**
         * Send broadcast if success, show error if unsuccessful
         */
        @Override
        public void onImmediateResponse(JSONObject response) {
            if (wasSuccessful()) {
                Context appContext = AppSession.getInstance().getApplicationContext();
                FollowChangeReceiver.sendFollowChangeBroadcast(userID, follow, appContext);
            } else {
                String message = follow ? FAIL_FOLLOW : FAIL_UNFOLLOW;
                AppSession.showLongToast(message);
            }
        }

        /**
         * Show error
         */
        @Override
        public void onImmediateError(VolleyError error) {
            StoreUtil.showExceptionMessage(error);
        }

        /**
         * Update data and follow button view
         */
        @Override
        public void onResumeWithResponse(ProfileHomeFragment object) {
            User.Relations r = object.getUser().getRelations();
            r.setPendingFollowing(false);

            if (wasSuccessful()) {
                r.setFollowing(follow);
            }

            object.getProfileHeaderController().setFollowButtonState(follow);
        }

        /**
         * Enable button and specifiy that a request is not pending anymore
         */
        @Override
        public void onResumeAfter(ProfileHomeFragment object) {
            User.Relations r = object.getUser().getRelations();
            r.setPendingFollowing(false);
            object.getProfileHeaderController().setFollowButtonEnabled(true);
        }
    }

    /**
     * Sets the view of the follow button (either blue or green)
     * @param following whether logged in user is following this user
     */
    public void setFollowButtonState(boolean following) {
        int bgResource;
        int imgResource;
        String text;

        if (following) {
            bgResource = R.drawable.standard_green_button;
            imgResource = R.drawable.ic_accept;
            text = FOLLOWING;
        } else {
            bgResource = R.drawable.standard_blue_button;
            imgResource = R.drawable.ic_plus;
            text = FOLLOW;
        }

        followButton.setBackgroundResource(bgResource);
        followButtonImg.setImageResource(imgResource);
        followButtonText.setText(text);
    }

    /**
     * Set the anar score for this user and set the medal (none, silver, gold, diamonds, etc.)
     * @param user user to use to get the score
     */
    private void setUserScore(User user) {
        Score score = user.getScore();
        String scoreText = "";

        if (score != null) {
            int total = score.getTotal();

            DecimalFormat formatter = new DecimalFormat("#,###");
            scoreText = formatter.format(total);
            anarNumberTxt.setText(scoreText);

            int numDiamonds = total / 2500;

            if (numDiamonds > 1) {
                //multiple diamonds, show number text
                rankImgView.setImageResource(R.drawable.ic_diamond);
                rankImgView.setPadding(0, 0, 0, 0);
                diamondNumberText.setVisibility(View.VISIBLE);
                diamondNumberText.setText(Integer.toString(numDiamonds));
            } else if (numDiamonds == 1) {
                //one diamond
                rankImgView.setImageResource(R.drawable.ic_diamond);
                rankImgView.setPadding(0, 0, 0, 0);
                diamondNumberText.setVisibility(View.GONE);
            } else if (total >= 1000) {
                //show gold
                rankImgView.setImageResource(R.drawable.gold_circle);
            } else if (total >= 500) {
                //show silver
                rankImgView.setImageResource(R.drawable.silver_circle);
            } else {
                //show nothing
                rankImgView.setVisibility(View.GONE);
            }

        } else {
            rankImgView.setVisibility(View.GONE);
        }

        if (scoreText.length() == 0) {
            anarSection.setVisibility(View.GONE);
        }
    }
}
