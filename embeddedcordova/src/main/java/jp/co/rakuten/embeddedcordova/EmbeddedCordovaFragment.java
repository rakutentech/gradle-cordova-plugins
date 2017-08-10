package jp.co.rakuten.embeddedcordova;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewEngine;
import org.json.JSONException;

public class EmbeddedCordovaFragment extends Fragment {
    private SystemWebView webView;
    private CordovaWebView webInterface;
    private CordovaInterfaceImpl cordovaInterface;
    private ConfigXmlParser parser;
    private String mWebviewUrl;

    public void loadUrl(String url) {
        if (webView != null) {
            webView.loadUrl(url);
        }
    }

    @Override
    public void onInflate(Context context, AttributeSet attributeSet, Bundle savedInstanceState) {
        super.onInflate(context, attributeSet, savedInstanceState);

        TypedArray webviewAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.embedded_cordova__webview);
        mWebviewUrl = webviewAttributes.getString(R.styleable.embedded_cordova__webview_cordova_url);
        webviewAttributes.recycle();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        cordovaInterface = new EmbeddedCordovaInterfaceImpl(this.getActivity(), this);

        parser = new ConfigXmlParser();
        parser.parse(this.getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.embedded_cordova__fragment_webview, container, false);

        webView = view.findViewById(R.id.embedded_cordova__webview);
        webInterface = new CordovaWebViewImpl(new SystemWebViewEngine(webView));

        webInterface.init(cordovaInterface, parser.getPluginEntries(), parser.getPreferences());
        this.loadUrl(this.getWebviewUrl());

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        cordovaInterface.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            cordovaInterface.onRequestPermissionResult(requestCode, permissions, grantResults);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void onStart() {
        super.onStart();

        if (this.webInterface == null) {
            return;
        }
        this.webInterface.handleStart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (this.webInterface == null) {
            return;
        }

        webInterface.handleDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (this.webInterface == null) {
            return;
        }

        this.webInterface.handleResume(true);
    }

    @Override
    public void onPause() {
        super.onPause();

        if (this.webInterface == null) {
            return;
        }

        this.webInterface.handlePause(true);
    }

    private String getWebviewUrl() {
        if(mWebviewUrl != null) {
            return "file:///android_asset/www/" + mWebviewUrl;
        } else {
            return parser.getLaunchUrl();
        }
    }
}
