package jp.co.rakuten.embeddedcordova;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;

import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaPlugin;

import java.util.concurrent.ExecutorService;

public class EmbeddedCordovaInterfaceImpl extends CordovaInterfaceImpl {
    private Fragment mFragment;

    public EmbeddedCordovaInterfaceImpl(Activity activity, Fragment fragment) {
        super(activity);

        mFragment = fragment;
    }

    public EmbeddedCordovaInterfaceImpl(Activity activity, ExecutorService threadPool, Fragment fragment) {
        super(activity, threadPool);

        mFragment = fragment;
    }

    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        this.setActivityResultCallback(command);

        try {
            this.mFragment.startActivityForResult(intent, requestCode);
        } catch (RuntimeException e) {
            this.activityResultCallback = null;
            throw e;
        }
    }

}
