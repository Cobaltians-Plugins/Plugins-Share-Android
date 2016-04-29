package io.kristal.shareplugin.shareDataClass;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import org.cobaltians.cobalt.Cobalt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import io.kristal.shareplugin.interfaces.ShareDataInterface;
import io.kristal.shareplugin.SharePlugin;

/**
 * Created by Roxane P. on 4/22/16.
 * share from drawable
 */
public class ShareRemoteFile implements ShareDataInterface {

    private static final String TAG = "ShareRemoteFile";
    private final String rawUrl;
    private URL mUrl;
    private final String mType;
    private String mTitle = null; // TODO: 4/28/16 find a better approach
    private String mDetail = null;

    /**
     * ShareRemoteFile constructor
     * Load and send an intent from files data
     * @param data - hashMap containing file's data
     */
    public ShareRemoteFile(Map data) {
        // mandatory data
        this.mType = data.get("type").toString();
        this.rawUrl = data.get("path").toString();
        try {
            this.mUrl = new URL(this.rawUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error while parsing URL " + this.rawUrl + ".");
            e.printStackTrace();
        }
        // optional data
        if (data.containsKey("title")) {
            this.mTitle = SharePlugin.fileNameForFileSystem(data.get("title").toString());
        }
        if (data.containsKey("detail")) {
            this.mDetail = data.get("detail").toString();
        }
    }

    /**
     * return a ready-to-launch intent for different resource files
     * TODO: 4/22/16 other protocol (ftp, smb...)
     */
    @Override
    public Intent returnShareIntent() {
        Intent share;
        switch (mType) {
            case SharePlugin.TYPE_IMAGE_KEY:
                // Remote url contains image
                HttpURLConnection connection = null;
                InputStream input = null;
                try {
                    connection = (HttpURLConnection) mUrl.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    input = connection.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap immutableBpm = BitmapFactory.decodeStream(input);
                Bitmap mutableBitmap = immutableBpm.copy(Bitmap.Config.ARGB_8888, true);
                View view = new View(SharePlugin.currentFragment.getContext());
                view.draw(new Canvas(mutableBitmap));
                Uri uri = Uri.parse(MediaStore.Images.Media.insertImage(SharePlugin.currentFragment.getContext().getContentResolver(), mutableBitmap, "Nur", null));
                share = new Intent(Intent.ACTION_SEND);
                // set MimeType
                share.setType(SharePlugin.getMimeType(rawUrl));
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                share.putExtra(Intent.EXTRA_TEXT, mDetail);
                share.putExtra(Intent.EXTRA_STREAM, uri);
                // return intent for launching
                return share;
            case SharePlugin.TYPE_AUDIO_KEY:
                share = new Intent(Intent.ACTION_SEND);
                // set MimeType [audio/*]
                share.setType(SharePlugin.getMimeType(rawUrl));
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                share.putExtra(Intent.EXTRA_TEXT, mDetail);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadFileFromUrl()));
                return share;
            case SharePlugin.TYPE_DOCUMENT_KEY:
                // TODO: 4/22/16 document file (pdf, docx, xml) from url
                share = new Intent(Intent.ACTION_SEND);
                // set MimeType [application/*]
                share.setType(SharePlugin.getMimeType(rawUrl));
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                share.putExtra(Intent.EXTRA_TEXT, mDetail);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadFileFromUrl()));
                return share;
            case SharePlugin.TYPE_VIDEO_KEY:
                // TODO: 4/22/16 video file from url
                share = new Intent(Intent.ACTION_SEND);
                // set MimeType [video/*]
                share.setType(SharePlugin.getMimeType(rawUrl));
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                share.putExtra(Intent.EXTRA_TEXT, mDetail);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadFileFromUrl()));
                return share;
            case SharePlugin.TYPE_DATA_KEY:
                share = new Intent(Intent.ACTION_SEND);
                // set MimeType [unknown]
                share.setType(SharePlugin.getMimeType(rawUrl));
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                share.putExtra(Intent.EXTRA_TEXT, mDetail);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadFileFromUrl()));
                return share;
            default:
                Log.e(TAG, "No action defined for this type of file from this source.");
                break;
        }
        return null;
    }

    /**
     * downloadFileFromUrl
     * create a file from a direct URL to a file
     * @return file - the downloaded File object
     */
    private File downloadFileFromUrl() {
        File file = null;
        String fileName = this.mTitle + '.' + (MimeTypeMap.getFileExtensionFromUrl(rawUrl) == null ? "dat" : MimeTypeMap.getFileExtensionFromUrl(rawUrl));
        // Variable to store total downloaded bytes
        int downloadedSize = 0;
        Boolean startToastShowed = false;
        HttpURLConnection urlConnection = null;

        try {
            // Init connection
            urlConnection = (HttpURLConnection) this.mUrl.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            // Set the path where we want to save the file
            // In this case, going to save it on the root directory of the sd card. + Environment.getExternalStorageDirectory().getAbsolutePath() +
            File SDCardRoot = new File(SharePlugin.pathRemoteFile);
            if (Cobalt.DEBUG) Log.d(TAG, "File will be stored in " + SDCardRoot.getAbsolutePath());
            // Create a new file, specifying the path, and the filename
            // Which we want to save the file as.
            file = new File(SDCardRoot, SharePlugin.fileNameForFileSystem(fileName));
            // Stream for writing the downloaded data into the created file
            FileOutputStream fileOutput = new FileOutputStream(file);
            // Stream for reading the data from the internet
            InputStream inputStream = null;
            if (urlConnection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                if (Cobalt.DEBUG) Log.e(TAG, "HttpURLConnection return bad code: " + urlConnection.getResponseCode());
                file.delete();
                return null;
            } else {
                inputStream = urlConnection.getInputStream();
            }
            // Get size of the file
            int totalSize = urlConnection.getContentLength();
            // Creating an empty buffer
            byte[] buffer = new byte[1024];
            int bufferLength = 0; // used to store a temporary size of the buffer
            // Read through the input buffer and write the contents to the file
            // TODO: 4/29/16 thread with callback
            while ((bufferLength = inputStream.read(buffer)) > 0) {
                // Write data from the buffer to the file on the phone
                fileOutput.write(buffer, 0, bufferLength);
                // Updating downloadedSize
                downloadedSize += bufferLength;
                // Report the progress
                if (downloadedSize > 0 && !startToastShowed) {
                    if (Cobalt.DEBUG) Log.d(TAG, "Download of " + totalSize + " bytes started.");
                    Toast.makeText(SharePlugin.currentFragment.getContext(), "Downloading " + mType + " in progress...", Toast.LENGTH_LONG).show();
                    startToastShowed = true;
                }
                if (downloadedSize == totalSize) {
                    if (Cobalt.DEBUG)
                        Log.d(TAG, "Downloaded " + downloadedSize + " bytes stored in " + file.getPath());
                    Toast.makeText(SharePlugin.currentFragment.getContext(), "Downloading completed in " + file.getPath(), Toast.LENGTH_LONG).show();
                }
            }
            // Flush/close the output stream when done
            fileOutput.flush();
            fileOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
