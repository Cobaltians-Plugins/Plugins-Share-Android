package io.kristal.shareplugin.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.webkit.MimeTypeMap;

import org.cobaltians.cobalt.Cobalt;

import io.kristal.shareplugin.SharePlugin;

/**
 * Created by Roxane P. on 5/3/16.
 * IntentsTools
 * Some tools about MimeType, file extension an Uri
 */
public class IntentsTools {

    private static final String TAG = "IntentsTools";

    // get MimeType From url
    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (Cobalt.DEBUG)
            Log.d(TAG, "getMimeType found extension ." + extension + ", added MimeType: " + type);
        return type;
    }

    // get Extension From url
    public static String getExtension(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (FileSystemTools.stringIsBlank(extension)) {
            return "data"; // default extension
        }
        if (Cobalt.DEBUG)
            Log.d(TAG, "getMimeType found extension ." + extension);
        return extension;
    }

    // get MimeType From resource Id
    public static String getExtension(int resourceId) {
        String ret;
        if (resourceId <= 0) return null;
        TypedValue value = new TypedValue();
        SharePlugin.currentContext.getResources().getValue(resourceId, value, true);
        ret = getExtension(value.string.toString());
        if (FileSystemTools.stringIsBlank(ret)) {
            return "data";
        }
        return getExtension(value.string.toString());
    }

    // get Application Name
    public static String getApplicationName() {
        int stringId = SharePlugin.currentContext.getApplicationInfo().labelRes;
        return SharePlugin.currentContext.getString(stringId);
    }

    /**
     * get uri to any resource type
     *
     * @param context - context
     * @param resId   - resource id
     * @return - Uri to resource by given id
     * @throws Resources.NotFoundException if the given ID does not exist.
     */
    public static Uri getUriToResource(@NonNull Context context, @AnyRes int resId, String extention) throws Resources.NotFoundException {
        // Return a resource instance for your application's package.
        Resources res = context.getResources();
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId) + "." + extention);
    }

    /**
     * return true if str contain a numeric
     */
    public static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }
}
