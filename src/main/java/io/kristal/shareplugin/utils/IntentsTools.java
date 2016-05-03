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
        if (extension == null) {
            return "data"; // default extention
        }
        if (Cobalt.DEBUG)
            Log.d(TAG, "getExtension found extension ." + extension);
        return extension;
    }

    // get MimeType From resource Id
    public static String getExtension(int resourceId) {
        if (resourceId <= 0) return null;
        TypedValue value = new TypedValue();
        SharePlugin.currentContext.getResources().getValue(resourceId, value, true);
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
    public static final Uri getUriToResource(@NonNull Context context, @AnyRes int resId, String extention) throws Resources.NotFoundException {
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
                + '/' + res.getResourceEntryName(resId) + "." + extention);
        /** return uri */
        return resUri;
    }

}
