/**
 * SharePlugin
 * ShareMe
 * <p/>
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2014 Kristal
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.kristal.shareplugin;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.MimeTypeMap;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import org.cobaltians.cobalt.plugin.CobaltPluginWebContainer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SharePlugin extends CobaltAbstractPlugin {

    protected final static String TAG = SharePlugin.class.getSimpleName();
    private CobaltPluginWebContainer mWebContainer;
    private static final String SHARE_ME_APP = "share";
    // fragment handler
    public static CobaltFragment currentFragment;

    /*******************************************************************************************************
     * MEMBERS
     *******************************************************************************************************/

    protected static SharePlugin sInstance;
    private String mType;
    public static Boolean forceChooser = true;

    // data extension types - string
    public static final String TYPE_IMAGE_KEY = "image";
    public static final String TYPE_TEXT_KEY = "text";
    public static final String TYPE_VIDEO_KEY = "video";
    public static final String TYPE_AUDIO_KEY = "audio";
    public static final String TYPE_DOCUMENT_KEY = "document";
    public static final String TYPE_DATA_KEY = "data";

    // data source types
    public static final int dataFromUrl = 0;
    public static final int dataFromDrawable = 1;
    public static final int dataFromSDCard = 2;
    public static final int dataFromContentProvider = 3;
    public static final int dataFromAssets = 4;
    public static final int dataFromString = 5;

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static CobaltAbstractPlugin getInstance(CobaltPluginWebContainer webContainer) {
        if (sInstance == null) sInstance = new SharePlugin();
        sInstance.addWebContainer(webContainer);
        return sInstance;
    }

    @Override
    public void onMessage(CobaltPluginWebContainer webContainer, JSONObject message) {
        Log.d(TAG, "onMessage called with message: " + message.toString());
        mWebContainer = webContainer;
        currentFragment = webContainer.getFragment();

        try {
            String action = message.getString(Cobalt.kJSAction);
            //Log.d(TAG, "check action, got " + SHARE_ME_APP + " receveid " + action);
            if (action.equals(SHARE_ME_APP)) {
                // setting up share
                CobaltFragment fragment = webContainer.getFragment();

                int sourceFile;
                String typeFile;
                String title;
                String detail;
                try {
                    JSONArray data = message.getJSONArray("data");
                    typeFile = data.getJSONObject(0).getString("type");
                    mType = typeFile;
                    if (typeFile.equals(SharePlugin.TYPE_TEXT_KEY)) {
                        sourceFile = SharePlugin.dataFromString;
                    } else {
                        sourceFile = setSourceFromType(data.getJSONObject(0).getString("source"));
                    }
                    switch (sourceFile) {
                        case SharePlugin.dataFromDrawable:
                            int resourceId = data.getJSONObject(0).getInt("id");
                            // optionals types
                            title = data.getJSONObject(0).getString("title");
                            detail = data.getJSONObject(0).getString("detail");
                            if (Cobalt.DEBUG) {
                                Log.d(TAG, "Extracted data are: type:" + typeFile + " resID:" + resourceId + " title:" + title + " detail:" + detail);
                            }
                            doShare(new ShareLocalFile(typeFile, resourceId, title, detail).returnShareIntent());
                            break;
                        case SharePlugin.dataFromUrl:
                            String url = data.getJSONObject(0).getString("path");
                            // optionals types
                            title = data.getJSONObject(0).getString("title");
                            detail = data.getJSONObject(0).getString("detail");
                            if (Cobalt.DEBUG) {
                                Log.d(TAG, "Extracted data are: type:" + typeFile + " url:" + url + " title:" + title + " detail:" + detail);
                            }
                            doShare(new ShareRemoteFile(typeFile, url, title, detail).returnShareIntent());
                            break;
                        case SharePlugin.dataFromAssets:
                            // TODO: 4/22/16 create class for assets sources
                            break;
                        case SharePlugin.dataFromSDCard:
                            // TODO: 4/22/16 create class for SDCard source
                            break;
                        case SharePlugin.dataFromString:
                            // mandatory data
                            title = data.getJSONObject(0).getString("title");
                            detail = data.getJSONObject(0).getString("content");
                            doShare(new ShareSimpleShareData(title, detail).returnShareIntent());
                            break;
                        case SharePlugin.dataFromContentProvider:
                            // TODO: 4/22/16 create class for ContentProvider source
                            break;
                        default:
                            Log.e(TAG, "onMessage: invalid action " + action + " in message " + message.toString() + ".");
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // send callback
                JSONObject data = new JSONObject();
                data.put("cobalt.share", "share completed");
                // send callback
                fragment.sendCallback(message.getString(Cobalt.kJSCallback), data);
            } else if (Cobalt.DEBUG)
                Log.e(TAG, "onMessage: invalid action " + action + " in message " + message.toString() + ".");
        } catch (JSONException exception) {
            if (Cobalt.DEBUG) {
                Log.d(TAG, "onMessage: missing action key in message " + message.toString() + ".");
                exception.printStackTrace();
            }
        }
    }

    private void doShare(Intent intent) {
        if (Cobalt.DEBUG) {
            Log.d(TAG, "cobalt.share will share intent " + intent.toString() + " " + intent.getData() + " " + intent.getPackage() + " " + intent.getScheme());
        }

        for (String key : intent.getExtras().keySet()) {
            Object value = intent.getExtras().get(key);
            if (value != null) {
                Log.d(TAG, String.format("%s %s (%s)", key,
                        value.toString(), value.getClass().getName()));
            }
        }
        // launch share activity
        if (forceChooser) {
            // with chooser
            mWebContainer.getFragment().getContext().startActivity(Intent.createChooser(intent, "Share " + this.mType + " with..."));
        } else {
            // without chooser
            mWebContainer.getFragment().getContext().startActivity(intent);
        }
    }

    private int setSourceFromType(String type) {
        int source;
        switch (type) {
            case "drawable":
                source = SharePlugin.dataFromDrawable;
                break;
            case "url":
                source = SharePlugin.dataFromUrl;
                break;
            case "sdcard":
                source = SharePlugin.dataFromSDCard;
                break;
            case "assets":
                source = SharePlugin.dataFromAssets;
                break;
            default:
                source = SharePlugin.dataFromContentProvider;
                break;
        }
        return source;
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (Cobalt.DEBUG) Log.d(TAG, "getMimeType found extension ."+ extension + ", added MimeType: " + type);
        return type;
    }

    /**
     * get uri to any resource type
     * @param context - context
     * @param resId - resource id
     * @throws Resources.NotFoundException if the given ID does not exist.
     * @return - Uri to resource by given id
     */
    public static final Uri getUriToResource(@NonNull Context context, @AnyRes int resId) throws Resources.NotFoundException {
        /** Return a Resources instance for your application's package. */
        Resources res = context.getResources();
        /**
         * Creates a Uri which parses the given encoded URI string.
         * @param uriString an RFC 2396-compliant, encoded URI
         * @throws NullPointerException if uriString is null
         * @return Uri for this given uri string
         */
        Uri resUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId));
        /** return uri */
        return resUri;
    }
}
