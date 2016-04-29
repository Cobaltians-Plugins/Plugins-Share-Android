package io.kristal.shareplugin.shareDataClass;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;

import org.cobaltians.cobalt.Cobalt;

import java.util.Map;

import io.kristal.shareplugin.interfaces.ShareDataInterface;
import io.kristal.shareplugin.SharePlugin;

/**
 * Created by Roxane P. on 4/22/16.
 * share from assets
 */
public class ShareLocalFile implements ShareDataInterface {

    private static final String TAG = "ShareLocalFile";
    private final String type;
    private int resourceId = -1;
    private String path = null;
    private String title = null;
    private String detail = null;

    /**
     * ShareLocalFile constructor
     * @param data - Map data of the file (image / file / document ...)
     */
    public ShareLocalFile(Map data) {
        // mandatory data
        this.type = data.get("type").toString();
        if (data.containsKey("path")) {
            this.path = data.get("path").toString();
        } else if (data.containsKey("id")) {
            this.resourceId = Integer.decode(data.get("id").toString());
            if (Cobalt.DEBUG) Log.d(TAG, "Constructor decoded resource id, found " + resourceId + ".");
        }
        // optional data
        if (data.containsKey("title")) {
            this.title = data.get("title").toString();
        }
        if (data.containsKey("detail")) {
            this.detail = data.get("detail").toString();
        }
    }

    /**
     * return a ready-to-launch intent for different resource files
     */
    @Override
    public Intent returnShareIntent() {
        Uri uri;
        Intent share;
        if (this.resourceId > 0) {
            // file comes from drawable id
            share = new Intent(Intent.ACTION_SEND);
            // generate uri
            uri = SharePlugin.getUriToResource(SharePlugin.currentFragment.getContext(), resourceId);
            if (Cobalt.DEBUG) {
                Log.d(TAG, "Found image view " + SharePlugin.getUriToResource(SharePlugin.currentFragment.getContext(), resourceId));
            }
            // TODO: 4/29/16 bug: file from resources id does not have extensions
            // set MimeType from resource, we need to use TypedValue to get the extension
            TypedValue value = new TypedValue();
            SharePlugin.currentFragment.getContext().getResources().getValue(resourceId, value, true);
            share.setType(SharePlugin.getMimeType(value.string.toString()));
            // place extras
            share.putExtra(Intent.EXTRA_SUBJECT, title);
            share.putExtra(Intent.EXTRA_TEXT, detail);
            //share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
            share.putExtra(Intent.EXTRA_STREAM, uri);
            // return intent for launching
            return share;
        } else if (path != null) {
            // file comes from assets
            uri = Uri.parse(SharePlugin.SCHEME + SharePlugin.AUTHORITY + "/" + path);
            Log.v(TAG, "uri " + SharePlugin.SCHEME + SharePlugin.AUTHORITY + "/" + path);
            share = new Intent(Intent.ACTION_SEND);
            // set MimeType
            share.setType(SharePlugin.getMimeType(path));
            // place extras
            share.putExtra(Intent.EXTRA_STREAM, uri);
            share.putExtra(android.content.Intent.EXTRA_SUBJECT, (title == null ? "Subject for message" : title));
            share.putExtra(android.content.Intent.EXTRA_TEXT, (detail == null ? "Body for message" : title));
            // return intent for launching
            return share;
        }
        return null;
    }
}
