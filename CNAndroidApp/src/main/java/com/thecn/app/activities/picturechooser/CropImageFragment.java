package com.thecn.app.activities.picturechooser;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.edmodo.cropper.CropImageView;
import com.thecn.app.R;
import com.thecn.app.activities.profile.ProfileHeaderController;
import com.thecn.app.tools.images.BitmapUtil;
import com.thecn.app.tools.CallbackManager;
import com.thecn.app.tools.DisplayUtil;
import com.thecn.app.tools.images.ImageUtil;
import com.thecn.app.tools.controllers.LoadingViewController;

/**
* Created by philjay on 2/20/15.
*/
public class CropImageFragment extends BaseFragment {

    private static final String TITLE = "Crop picture";
    private static final String KEY = "file_path";

    private String mFilePath;
    private Bitmap mBitmap;
    private CropImageView mCropImageView;

    private LoadingViewController mViewController;

    private boolean loading = false;

    private CallbackManager<CropImageFragment> callbackManager;

    @Override
    protected String getTitle() {
        return TITLE;
    }

    public static CropImageFragment getInstance(String filePath) {
        Bundle args = new Bundle();
        args.putString(KEY, filePath);

        CropImageFragment fragment = new CropImageFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loading = true;
        mFilePath = getArguments().getString(KEY);

        callbackManager = new CallbackManager<>();

        new Thread(new Runnable() {
            @Override
            public void run() {
                mBitmap = getBitmap();
                callbackManager.addCallback(new CropImageCallback());
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

    private static class CropImageCallback extends CallbackManager.Callback<CropImageFragment> {
        @Override
        public void execute(CropImageFragment object) {
            object.onLoadComplete();
        }
    }

    private Bitmap getBitmap() {
        if (ImageUtil.isFilePathValid(mFilePath)) {
            int widthLimit = DisplayUtil.getDisplayWidth(getActivity());
            int heightLimit = DisplayUtil.getDisplayHeight(getActivity());

            if (widthLimit > heightLimit) {
                int temp = widthLimit;
                widthLimit = heightLimit;
                heightLimit = temp;
            }

            return BitmapUtil.scaledBitmapFromFile(mFilePath, widthLimit, heightLimit);
        } else {
            return null;
        }
    }

    public void onLoadComplete() {
        loading = false;

        if (mBitmap != null) {
            mViewController.crossFade();
            initContent(mViewController.getContentView());
        } else {
            initErrorContent();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View content = inflater.inflate(R.layout.fragment_crop_profile_pic, null, false);

        if (loading) {
            setViewController(content, getActivity());
            mViewController.showLoading(true);
            return mViewController.getRootView();

        } else if (mBitmap == null) {
            setViewController(content, getActivity());
            return initErrorContent();
        }

        return initContent(content);
    }

    private View initContent(View content) {
        mCropImageView = (CropImageView) content.findViewById(R.id.crop_image_view);
        mCropImageView.setImageBitmap(mBitmap);

        int type = getPictureChooseActivity().getType();
        if (type == PictureChooseActivity.TYPE_BANNER) {
            mCropImageView.setAspectRatio(
                    (int) ProfileHeaderController.BANNER_RATIO_NUMERATOR,
                    (int) ProfileHeaderController.BANNER_RATIO_DENOMINATOR
            );
        }

        content.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().onBackPressed();
            }
        });

        content.findViewById(R.id.done).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PictureChooseActivity activity = getPictureChooseActivity();
                RectF rect = mCropImageView.getActualCropRect();
                BitmapUtil.SectionRectF sectionRect = new BitmapUtil.SectionRectF(rect);
                sectionRect.originalWidth = mBitmap.getWidth();
                sectionRect.originalHeight = mBitmap.getHeight();

                Fragment fragment = ConfirmImageFragment.getInstance(mFilePath, sectionRect);
                activity.replace(fragment);
            }
        });

        return content;
    }

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

    private void setViewController(View content, Context context) {
        mViewController = new LoadingViewController(content, context);
        mViewController.setBackgroundColor(getResources().getColor(R.color.black));
        mViewController.setTextColor(getResources().getColor(R.color.white));
        mViewController.showLoadingView();
    }
}
