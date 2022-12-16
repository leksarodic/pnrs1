package rodic.aleksa.miberchatapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class Binder extends IBinderNewMessage.Stub {

    private static final String LOG_TAG = "Binder";

    // Binder&callback stuff
    private ICallback mCallback;
    private CallbackCaller callbackCaller;

    public void stop() {
        callbackCaller.stop();
    }

    @Override
    public void setCallback(ICallback callback) {
        mCallback = callback;
        callbackCaller = new CallbackCaller();
        callbackCaller.start();
    }

    private class CallbackCaller implements Runnable{

        private static final long PERIOD = 5000L;

        private Handler mHandler = null;
        private boolean mRun = true;

        public void start() {
            mHandler = new Handler(Looper.getMainLooper());
            mHandler.postDelayed(this, PERIOD);
        }

        public void stop() {
            mRun = false;
            mHandler.removeCallbacks(this);
        }

        @Override
        public void run() {
            if (!mRun) {
                return;
            }

            try {
                mCallback.onCallbackCall();
            } catch (NullPointerException e) {
                // callback is null, do nothing
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "onCallbackCall failed", e);
            }

            mHandler.postDelayed(this, PERIOD);
        }
    }
}
