package io.kristal.shareme;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import org.cobaltians.cobalt.Cobalt;

/**
 * Created by Roxane P. on 4/22/16.
 * share from drawable
 */
public class ShareLocalFile implements ShareDataInterface {

    private static final String TAG = "ShareLocalFile";
    private final int resourceId;
    private final String type;
    private final String title;
    private final String detail;

    /**
     * Simple constructor
     * @param type - type of the file (image / file / document ...)
     * @param resourceId - context
     * @param title - resource id
     * @param detail - context
     */
    public ShareLocalFile(String type, int resourceId, String title, String detail) {
        this.type = type;
        this.resourceId = resourceId;
        this.title = title;
        this.detail = detail;
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
                Uri bmpUri = SharePlugin.getUriToResource(SharePlugin.currentFragment.getContext(), resourceId);
                // place file type
                share.setType("image/*");
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, title);
                share.putExtra(Intent.EXTRA_TEXT, detail);
                //share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
                share.putExtra(Intent.EXTRA_STREAM, bmpUri);
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
