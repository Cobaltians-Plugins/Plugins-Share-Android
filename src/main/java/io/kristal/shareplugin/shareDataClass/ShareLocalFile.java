package io.kristal.shareplugin.shareDataClass;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import org.cobaltians.cobalt.Cobalt;

import java.util.Map;

import io.kristal.shareplugin.interfaces.ShareDataInterface;
import io.kristal.shareplugin.SharePlugin;

/**
 * Created by Roxane P. on 4/22/16.
 * share from drawable
 */
public class ShareLocalFile implements ShareDataInterface {

    private static final String TAG = "ShareLocalFile";
    private final String type;
    private int resourceId;
    private String path;
    private String title;
    private String detail;

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
        switch (type) {
            case SharePlugin.TYPE_IMAGE_KEY:
                // image comes from R.drawable
                share = new Intent(Intent.ACTION_SEND);
                // get bitmap image from drawable
                Drawable bitmap = SharePlugin.currentFragment.getContext().getResources().getDrawable(resourceId);
                if (Cobalt.DEBUG) {
                    Log.d(TAG, "Found image view " + SharePlugin.getUriToResource(SharePlugin.currentFragment.getContext(), resourceId) + " drawable to string " + bitmap.toString());
                }
                // generate uri
                uri = SharePlugin.getUriToResource(SharePlugin.currentFragment.getContext(), resourceId);
                // place file type
                share.setType("image/*");
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, title);
                share.putExtra(Intent.EXTRA_TEXT, detail);
                //share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                share.putExtra(Intent.EXTRA_STREAM, uri);
                // return intent for launching
                return share;
            case SharePlugin.TYPE_AUDIO_KEY:
                return null; // TODO: 4/22/16 audio from drawable
            case SharePlugin.TYPE_DOCUMENT_KEY:
                return null; // TODO: 4/22/16 document file (pdf, docx, xml) from drawable
            case SharePlugin.TYPE_VIDEO_KEY:
                return null; // TODO: 4/22/16 video file from drawable
            case SharePlugin.TYPE_DATA_KEY:
                return null; // TODO: 4/22/16 other data from drawable
            default:
                break;
        }

        return null;
    }
}
