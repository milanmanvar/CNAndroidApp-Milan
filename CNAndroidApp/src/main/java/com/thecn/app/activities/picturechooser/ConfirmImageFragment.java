package com.thecn.app.activities.picturechooser;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.thecn.app.AppSession;
import com.thecn.app.R;
import com.thecn.app.tools.images.BitmapUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.DisplayUtil;
import com.thecn.app.tools.images.ImageUtil;
import com.thecn.app.tools.controllers.LoadingViewController;

/**
* Used to show the final image to the user to confirm before sending it to the server.
*/
public class ConfirmImageFragment extends BaseFragment {

    private static final String TITLE = "Confirm picture";
    private static final String FILE_KEY = "file_path";
    private static final String RECT_KEY = "rect";

    //filepath to img
    private String mFilePath;
    //bitmap of img in mem
    private Bitmap mBitmap;
    //section of bitmap that was chosen by CropImageFragment
    private BitmapUtil.SectionRectF mRect;
    //displays the image
    private ImageView mImageView;

    private LoadingViewController mViewController;

    private boolean loading = false;

    private CallbackManager<ConfirmImageFragment> callbackManager;

    @Override
    public String getTitle() {
        return TITLE;
    }

    /**
     * Must give specified arguments.
     * @param filePath to image in file
     * @param rect section of image to crop with CN attachment_picture_thumb api
     * @return new instance of this class
     */
    public static ConfirmImageFragment getInstance(String filePath, BitmapUtil.SectionRectF rect) {
        Bundle args = new Bundle();
        args.putString(FILE_KEY, filePath);
        args.putSerializable(RECT_KEY, rect);

        ConfirmImageFragment fragment = new ConfirmImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Get bitmap from file, cropping it using the specified {@link com.thecn.app.tools.images.BitmapUtil.SectionRectF}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        callbackManager = new CallbackManager<>();

        loading = true;
        mFilePath = getArguments().getString(FILE_KEY);
        mRect = (BitmapUtil.SectionRectF) getArguments().getSerializable(RECT_KEY);

        new Thread(new Runnable() {
            @Override
            public void run() {
                mBitmap = getBitmap();
                callbackManager.addCallback(new ConfirmImageCallback());
            }
        }).start();
    }


    @Override
    public void onResume() {
        super.onResume();
        callbackManager.resume(this);
    }

    @Override
    public void onPause() {
        callbackManager.pause();
        super.onPause();
    }

    /**
     * Used when bitmap is ready to display
     */
    private static class ConfirmImageCallback extends CallbackManager.Callback<ConfirmImageFragment> {
        @Override
        public void execute(ConfirmImageFragment object) {
            object.onLoadComplete();
        }
    }

    /**
     * Gets the largest size bitmap that will fit inside the screen from the file.
     * @return an appropriately sized bitmap
     */
    private Bitmap getBitmap() {
        if (ImageUtil.isFilePathValid(mFilePath) && mRect != null) {
            mRect.maxOutputWidth = DisplayUtil.getDisplayWidth(getActivity());
            mRect.maxOutputHeight = DisplayUtil.getDisplayHeight(getActivity());

            if (mRect.maxOutputWidth > mRect.maxOutputHeight) {
                //switch these if necessary
                int temp = mRect.maxOutputWidth;
                mRect.maxOutputWidth = mRect.maxOutputHeight;
                mRect.maxOutputHeight = temp;
            }

            return BitmapUtil.scaledBitmapSectionFromFile(mFilePath, mRect);
        } else {
            return null;
        }
    }

    /**
     * Called from {@link com.thecn.app.activities.picturechooser.ConfirmImageFragment.ConfirmImageCallback}
     * when bitmap loaded (or error has occurred).
     */
    public void onLoadComplete() {
        loading = false;

        if (mBitmap != null) {
            mViewController.crossFade();
            initContent(mViewController.getContentView());
        } else {
            initErrorContent();
        }
    }

    /**
     * Uses {@link com.thecn.app.tools.controllers.LoadingViewController}
     * to show a loading view until the bitmap is loaded from file.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View content = inflater.inflate(R.layout.fragment_confirm_profile_pic, null, false);
        Button uploadButton = (Button) content.findViewById(R.id.done);
        uploadButton.setText("Upload");

        if (loading) {
            initViewController(content, getActivity());
            mViewController.showLoading(true);
            return mViewController.getRootView();

        } else if (mBitmap == null) {
            initViewController(content, getActivity());
            return initErrorContent();
        }

        return initContent(content);
    }

    /**
     * Shows the image and two buttons, "cancel" and "upload".
     * Sets the buttons' onClickListeners
     * @param content the view to initialize
     * @return the same view that was initialized
     */
    private View initContent(View content) {
        mImageView = (ImageView) content.findViewById(R.id.image_view);
        mImageView.setImageBitmap(mBitmap);

        content.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        content.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //upload the image with an UploadTask.
                PictureChooseActivity activity = getPictureChooseActivity();
                UploadTask uploadTask = new UploadTask(mFilePath, activity.getType(), mRect);

                AppSession.showShortToast("Uploading...");
                uploadTask.execute();
                activity.setResult(Activity.RESULT_OK);
                activity.finish();
            }
        });

        return content;
    }

    /**
     * Show an error message and a button that will bring the user back to the gallery fragment.
     * @return the error view
     */
    private View initErrorContent() {
        mViewController.showLoading(false);
        mViewController.showMessage(LOAD_ERROR);
        mViewController.showButton(BACK, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });
        return mViewController.getRootView();
    }

    /**
     * Set up the view controller.  Initially shown as loading.
     * @param content content view for when the data loads
     * @param context context to initialize internal view objects.
     */
    private void initViewController(View content, Context context) {
        mViewController = new LoadingViewController(content, context);
        mViewController.setBackgroundColor(getResources().getColor(R.color.black));
        mViewController.setTextColor(getResources().getColor(R.color.white));
        mViewController.showLoadingView();
    }
}
