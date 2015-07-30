package org.refugerestrooms.servers;

/**
 * Created by Refuge Restrooms on 7/13/2015.
 */
import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

public class JavaScriptInterface {
    Context mContext;

    /** Instantiate the interface and set the context */
    public JavaScriptInterface(Context c) {
        mContext = c;
    }

    /** Show a toast from the web page */
    //do the call to the javascript file here

    @JavascriptInterface
    public void showToast(String toast) {
        Toast.makeText(mContext, toast, Toast.LENGTH_SHORT).show();
    }
}
