package io.kristal.shareplugin.shareDataClass;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.util.TypedValue;

import org.cobaltians.cobalt.Cobalt;

import java.util.Map;

import io.kristal.shareplugin.SharePlugin;
import io.kristal.shareplugin.dataProvider.ShareContentProvider;
import io.kristal.shareplugin.interfaces.ShareDataInterface;
import io.kristal.shareplugin.utils.IntentsTools;

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
        TypedValue value = new TypedValue();
        SharePlugin.currentFragment.getContext().getResources().getValue(resourceId, value, true);
        if (this.resourceId > 0) {
            // file comes from drawable id
            ShareContentProvider.resourceId = this.resourceId;
            share = new Intent(Intent.ACTION_SEND);
            // generate uri
            uri = IntentsTools.getUriToResource(SharePlugin.currentFragment.getContext(), resourceId, IntentsTools.getExtension(resourceId));
            if (Cobalt.DEBUG) {
                Log.d(TAG, "Found image view " + uri.toString());
            }
            // set MimeType from resource, we need to use TypedValue to get the extension
            share.setType(IntentsTools.getMimeType(value.string.toString()));
            // place extras
            share.putExtra(Intent.EXTRA_SUBJECT, (title == null ? "Untitled " + type : title));
            if (detail != null) {
                share.putExtra(Intent.EXTRA_TEXT, detail);
            }
            //share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
            share.putExtra(Intent.EXTRA_STREAM, uri);
            // return intent for launching
            return share;
        } else if (path != null) {
            // file comes from assets
            uri = Uri.parse(SharePlugin.SCHEME + SharePlugin.AUTHORITY + "/" + path + IntentsTools.getExtension(value.toString()));
            Log.v(TAG, "uri " + SharePlugin.SCHEME + SharePlugin.AUTHORITY + "/" + path + IntentsTools.getExtension(value.toString()));
            share = new Intent(Intent.ACTION_SEND);
            // set MimeType
            share.setType(IntentsTools.getMimeType(path));
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
