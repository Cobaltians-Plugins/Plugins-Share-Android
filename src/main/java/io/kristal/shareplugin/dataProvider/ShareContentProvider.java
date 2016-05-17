package io.kristal.shareplugin.dataProvider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.webkit.URLUtil;

import org.cobaltians.cobalt.Cobalt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.kristal.shareplugin.SharePlugin;
import io.kristal.shareplugin.utils.IntentsTools;

/**
 * Created by Roxane P. on 4/28/16.
 * ShareContentProvider
 * A basic content provider to read files from assets
 */
public class ShareContentProvider extends ContentProvider {

    static final String TAG = "ShareContentProvider";

    /**************************************************************************************
     * AUTO GENERATED METHODS
     **************************************************************************************/

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * openAssetFile
     * [do not call this method directly]
     * Open a file from assets, copy it to cache directory (to avoid compression) and return file descriptor
     *
     * @param uri  The URI whose file is to be opened. (ex: content://io.kristal.shareplugin.SharePlugin/files/sample.pdf)
     * @param mode Access mode for the file. (ex: "r", "w", "rw" ...)
     * @return Returns a new AssetFileDescriptor which you can use to access
     * the file.
     * @throws FileNotFoundException Throws FileNotFoundException if there is
     *                               no file associated with the given URI or the mode is invalid.
     * @throws SecurityException     Throws SecurityException if the caller does
     *                               not have permission to access the file.
     */
    @Override
    public AssetFileDescriptor openAssetFile(@NonNull final Uri uri, @NonNull final String mode) throws FileNotFoundException {
        // we need to determinate absolute path in assets so file.pdf and Directory/file.pdf both work
        String token = SharePlugin.AUTHORITY + "/";
        String fileUri = uri.toString();
        // search for token which is the contentProvider name
        int indexOfToken = fileUri.indexOf(token);
        if (indexOfToken < 0) {
            Log.e(TAG, "Content provider Internal error.");
            return null;
        }
        indexOfToken = indexOfToken + token.length();
        // keep only the absolute path
        final String assetPath = fileUri.substring(indexOfToken, fileUri.length());
        try {
            //final File cacheFile = new File(getContext().getCacheDir(), assetPath);
            final File cacheFile = new File(SharePlugin.pathFileStorage, assetPath);
            cacheFile.getParentFile().mkdirs();
            copyToCacheFile(assetPath, cacheFile);
            return new AssetFileDescriptor(ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_WRITE), 0, -1);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    /**
     * copyToCacheFile
     * copy file to cache directory (to avoid compression) and return file descriptor
     *
     * @param assetPath path of the file in the assets
     * @param cacheFile destination file in the cache
     * @throws IOException Throws IOException if there is
     */
    private void copyToCacheFile(final String assetPath, final File cacheFile) throws IOException {
        // Copy assetPath to cacheFile.getAbsolutePath());
        if (Cobalt.DEBUG) Log.d(TAG, "copyToCacheFile copy " + assetPath + " to " + cacheFile.toString() + "...");
        InputStream inputStream = null;
        try {
            inputStream = getContext().getAssets().open(assetPath, AssetManager.ACCESS_BUFFER);
        } catch (FileNotFoundException exception) {
            Log.e(TAG, "File Not Found in assets: " + assetPath + ", check sent data.");
            exception.getStackTrace();
            return;
        }
        try {
            final FileOutputStream fileOutputStream = new FileOutputStream(cacheFile, false);
            try {
                // Creating an empty buffer
                byte[] buffer = new byte[1024];
                int bufferLength = 0; // used to store a temporary size of the buffer
                // Read through the input buffer and write the contents to the file
                while ((bufferLength = inputStream.read(buffer)) > 0) {
                    // Write data from the buffer to the file on the phone
                    fileOutputStream.write(buffer, 0, bufferLength);
                }
            } finally {
                // Flush/close the output stream when done
                fileOutputStream.flush();
                fileOutputStream.close();
            }
        } finally {
            inputStream.close();
        }
    }
}
