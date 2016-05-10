package io.kristal.shareplugin.shareDataClass;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;

import org.cobaltians.cobalt.Cobalt;

import java.util.Map;

import io.kristal.shareplugin.SharePlugin;
import io.kristal.shareplugin.dataProvider.ShareContentProvider;
import io.kristal.shareplugin.interfaces.ShareDataInterface;
import io.kristal.shareplugin.utils.IntentsTools;
import io.kristal.shareplugin.utils.Tokens;

/**
 * Created by Roxane P. on 4/22/16.
 * share from assets
 */
public class ShareLocalFile implements ShareDataInterface {

    private static final String TAG = "ShareLocalFile";
    private final String type;
    private String path = null;
    private String title = null;
    private String detail = null;

    /**
     * ShareLocalFile constructor
     * @param data - Map data of the file (image / file / document ...)
     */
    public ShareLocalFile(Map data) {
        // mandatory data
        this.type = data.get(Tokens.JS_TOKEN_TYPE).toString();
        this.path = data.get(Tokens.JS_TOKEN_PATH).toString();
        // optional data
        if (data.containsKey(Tokens.JS_TOKEN_TITLE)) {
            this.title = data.get(Tokens.JS_TOKEN_TITLE).toString();
        }
        if (data.containsKey(Tokens.JS_TOKEN_DETAIL)) {
            this.detail = data.get(Tokens.JS_TOKEN_DETAIL).toString();
        }
    }

    /**
     * return a ready-to-launch intent for different resource files
     */
    @Override
    public Intent returnShareIntent() {
        // todo: create Intent from sdcard
        return createIntentFromPath();
    }

    /**
     * return intent from a file path
     */
    private Intent createIntentFromPath() {
        Uri uri;
        Intent share = new Intent(Intent.ACTION_SEND);
        // file comes from assets
        uri = Uri.parse(SharePlugin.SCHEME + SharePlugin.AUTHORITY + "/" + path);
        if (Cobalt.DEBUG) {
            Log.d(TAG, "Uri to file " + uri.toString());
        }
        // set MimeType
        share.setType(IntentsTools.getMimeType(path));
        // place extras
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(android.content.Intent.EXTRA_SUBJECT, (title == null ? "Subject for message" : title));
        share.putExtra(android.content.Intent.EXTRA_TEXT, (detail == null ? "Body for message" : title));
        // return intent for launching
        return share;
    }
}
