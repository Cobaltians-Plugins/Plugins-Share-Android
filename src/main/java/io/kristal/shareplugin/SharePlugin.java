/**
 * SharePlugin
 * The MIT License (MIT)
 * Copyright (c) 2016 Cobaltians
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 **/

package io.kristal.shareplugin;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import org.cobaltians.cobalt.Cobalt;
import org.cobaltians.cobalt.fragments.CobaltFragment;
import org.cobaltians.cobalt.plugin.CobaltAbstractPlugin;
import org.cobaltians.cobalt.plugin.CobaltPluginWebContainer;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import io.kristal.shareplugin.shareDataClass.ShareContactData;
import io.kristal.shareplugin.shareDataClass.ShareLocalFile;
import io.kristal.shareplugin.shareDataClass.ShareRemoteFile;
import io.kristal.shareplugin.shareDataClass.ShareSimpleShareData;
import io.kristal.shareplugin.utils.FileSystemTools;
import io.kristal.shareplugin.utils.ParsingShareData;
import io.kristal.shareplugin.utils.Tokens;

/**
 * Created by Roxane P. on 4/18/16.
 * SharePlugin
 * Start an intent for sharing a file from data given by a Json Object Message
 */
public class SharePlugin extends CobaltAbstractPlugin {

    protected final static String TAG = SharePlugin.class.getSimpleName();
    private static CobaltPluginWebContainer mWebContainer;
    private static final String SHARE_ME_APP = "share";

    /**************************************************************************************
     * MEMBERS
     **************************************************************************************/

    protected static SharePlugin sInstance;
    // fragment handler
    public static CobaltFragment currentFragment;
    public static Context currentContext;
    private static String mType;

    /**************************************************************************************
     * CONSTANTS MEMBERS
     **************************************************************************************/

    // File path of the downloaded remote files
    public static String pathFileStorage;

    // Content provider path
    public static String providerAuthority;
    public static final String SCHEME = "content://";

    /**************************************************************************************
     * CONFIGURATION
     * *************************************************************************************/

    private final String StorageDirectoryName = "CobaltiansStorage";
    private static Boolean forceChooser = true;

    /**************************************************************************************
     * CONSTRUCTORS
     **************************************************************************************/

    public static CobaltAbstractPlugin getInstance()
    {
        if (sInstance == null)
        {
            sInstance = new SharePlugin();
        }
        return sInstance;
    }
    
    @Override
    public void onMessage(@NonNull CobaltPluginWebContainer webContainer, @NonNull String action,
            @Nullable JSONObject data, @Nullable String callbackChannel)
    {
        //if (Cobalt.DEBUG) Log.d(TAG, "onMessage called with message: " + message.toString());
        // TODO: check nullabillity
        mWebContainer = webContainer;
        currentFragment = webContainer.getFragment();
        currentContext = currentFragment.getContext();
        providerAuthority = currentContext.getPackageName() + ".SharePlugin";
        // Create or set the path of the storage directory in the phone
        pathFileStorage = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Android/data/" +
                currentFragment.getContext().getPackageName() +
                "/" + StorageDirectoryName + "/";
        if (!FileSystemTools.makeDirs(pathFileStorage)) {
            if (Cobalt.DEBUG) Log.e(TAG, "Can't create directory at " + pathFileStorage);
        }
        // will parse message, create and launching intent
        if (action.equals(SHARE_ME_APP)) {
            // parse JSON, put into an hashMap
            if (data == null)
            {
                Log.e(TAG, "onMessage: missing data.");
                return;
            }
            //Map data
            ParsingShareData psd = new ParsingShareData(data);
            Map parsedData;
            try
            {
                parsedData = psd.returnDataFromWeb();
                if (parsedData == null)
                {
                    Log.e(TAG, "Fatal: Parsed data is null.");
                    return;
                }
            }
            catch(JSONException e)
            {
                Log.e(TAG, "Fatal: Parsed data is null.");
                e.printStackTrace();
                return;
            }
            // mType is used for intent title
            mType = parsedData.get(Tokens.JS_TOKEN_TYPE).toString();
            // web side return data file to get from a source
            if (parsedData.containsKey(Tokens.JS_TOKEN_SOURCE)) {
                String source = parsedData.get(Tokens.JS_TOKEN_SOURCE).toString();
                // send intents for items with sources
                switch (source) {
                    case Tokens.JS_TOKEN_LOCAL:
                        doShare(new ShareLocalFile(parsedData).returnShareIntent());
                        break;
                    case Tokens.JS_TOKEN_REMOTE:
                        // intent called asynchronously
                        new ShareRemoteFile(parsedData);
                        break;
                    // case Tokens.JS_TOKEN_PHONE:
                    // TODO: 5/10/16 file comes from internal (dd) or external (sdcard) phone storage
                    // break;
                    default:
                        Log.e(TAG, "onMessage: invalid source " + source
                                   + ". Available sources are: local, url");
                        break;
                }
            }
            else {
                switch (mType) {
                    case Tokens.JS_TOKEN_CONTACT_TYPE:
                        doShare(new ShareContactData(parsedData).returnShareIntent());
                        break;
                    case Tokens.JS_TOKEN_TEXT_TYPE:
                        doShare(new ShareSimpleShareData(parsedData).returnShareIntent());
                        break;
                    default:
                        Log.e(TAG, "onMessage: invalid type " + mType
                                   + ". Available types are: contact, text");
                        break;
                }
            }
        }
        else if (Cobalt.DEBUG)
        {
            Log.e(TAG, "onMessage: invalid action " + action);
        }
    }

    public static void doShare(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "File data not found: intent is null");
            return;
        }
        if (Cobalt.DEBUG) {
            Log.d(TAG, "cobalt.share will share intent " + intent.toString() + " with extras:");
            for (String key : intent.getExtras().keySet()) {
                Object value = intent.getExtras().get(key);
                if (value != null) {
                    Log.d(TAG, String.format("%s %s (%s)", key,
                            value.toString(), value.getClass().getName()));
                }
            }
        }
        // launch share activity
        if (forceChooser) {
            // with chooser
            mWebContainer.getFragment().getContext().startActivity(Intent.createChooser(intent, "Share " + mType + " with..."));
        } else {
            // without chooser
            mWebContainer.getFragment().getContext().startActivity(intent);
        }
    }
}
