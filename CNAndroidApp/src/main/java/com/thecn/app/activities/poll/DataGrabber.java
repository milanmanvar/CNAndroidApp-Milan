package com.thecn.app.activities.poll;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.thecn.app.models.content.Post;
import com.thecn.app.stores.PostStore;
import com.thecn.app.stores.StoreUtil;
import com.thecn.app.tools.CallbackManager;

/**
* Gets poll data from the server when {@link com.thecn.app.activities.poll.PollActivity} first starts.
*/
public class DataGrabber extends Fragment {

    public boolean loading = false;
    private CallbackManager<DataGrabber> manager;

    public static final String ID_KEY = "id_key";

    /**
     * Make an instance of DataGrabber
     * @param id the id of the poll
     * @return new instance of this class
     */
    public static DataGrabber getInstance(String id) {
        Bundle args = new Bundle();
        args.putString(ID_KEY, id);

        DataGrabber grabber = new DataGrabber();
        grabber.setArguments(args);
        return grabber;
    }

    /**
     * Set up and start loading data from the server.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        manager = new CallbackManager<>();

        loading = true;
        String id = getArguments().getString(ID_KEY);
        PostStore.getPostById(id,PostStore.taskId, new Callback(manager));
    }

    @Override
    public void onResume() {
        super.onResume();
        manager.resume(this);
    }

    @Override
    public void onPause() {
        manager.pause();
        super.onPause();
    }

    /**
     * Used when data is returned from the server or an error has occurred.
     */
    private static class Callback extends CallbackManager.NetworkCallback<DataGrabber> {
        public Callback(CallbackManager<DataGrabber> grabber) {
            super(grabber);
        }

        @Override
        public void onResumeWithResponse(DataGrabber object) {
            Post post = PostStore.getData(response);
            PollActivity a = (PollActivity) object.getActivity();

            if (post != null) {
                a.onLoadSuccess(post);
            } else {
                a.finishWithError();
            }
        }

        @Override
        public void onResumeWithError(DataGrabber object) {
            StoreUtil.showExceptionMessage(error);
            object.getActivity().finish();
        }
    }
}
