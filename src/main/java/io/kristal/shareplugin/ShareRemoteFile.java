package io.kristal.shareplugin;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.cobaltians.cobalt.Cobalt;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Roxane P. on 4/22/16.
 * share from drawable
 */
public class ShareRemoteFile implements ShareDataInterface {

    private static final String TAG = "ShareRemoteFile";
    private final String rawUrl;
    private URL mUrl;
    private final String mType;
    private final String mTitle;
    private final String mDetail;

    /**
     * Simple constructor
     * @param type - mType of the file (image / file / document ...)
     * @param url - remote link of the file
     * @param title - resource id
     * @param detail - context
     */
    public ShareRemoteFile(String type, String url, String title, String detail) {
        this.mType = type;
        this.mTitle = title;
        this.mDetail = detail;
        this.rawUrl = url;
        try {
            this.mUrl = new URL(url);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error while parsing URL " + url + ".");
            e.printStackTrace();
        }
    }

    /**
     * return a ready-to-launch intent for different resource files
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
                // set MimeType
                share.setType(SharePlugin.getMimeType(rawUrl));
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                share.putExtra(Intent.EXTRA_TEXT, mDetail);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadFileFromUrl()));
                return share;
            case SharePlugin.TYPE_DOCUMENT_KEY:
                // TODO: 4/22/16 document file (pdf, docx, xml) from url
                share = new Intent(Intent.ACTION_SEND);
                // set MimeType
                share.setType(SharePlugin.getMimeType(rawUrl));
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                share.putExtra(Intent.EXTRA_TEXT, mDetail);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadFileFromUrl()));
                return share;
            case SharePlugin.TYPE_VIDEO_KEY:
                // TODO: 4/22/16 video file from url
                share = new Intent(Intent.ACTION_SEND);
                // set MimeType
                share.setType(SharePlugin.getMimeType(rawUrl));
                // place extras
                share.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                share.putExtra(Intent.EXTRA_TEXT, mDetail);
                share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadFileFromUrl()));
                return share;
            case SharePlugin.TYPE_DATA_KEY:
                // TODO: 4/22/16 other data from other protocol (ftp, smb...)
                share = new Intent(Intent.ACTION_SEND);
                // set MimeType
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

    private File downloadFileFromUrl() {
        Boolean startToastShowed = false;
        //create the new connection
        HttpURLConnection urlConnection = null;
        File file = null;
        try {
            urlConnection = (HttpURLConnection) this.mUrl.openConnection();
        //set up some things on the connection
        urlConnection.setRequestMethod("GET");
        urlConnection.setDoOutput(true);
        //and connect!
        urlConnection.connect();
        //set the path where we want to save the file
        //in this case, going to save it on the root directory of the
        //sd card.
        File SDCardRoot = Environment.getExternalStorageDirectory();
        // create a new file, specifying the path, and the filename
        //which we want to save the file as.
        file = new File(SDCardRoot, this.mTitle);
        //this will be used to write the downloaded data into the file we created
        FileOutputStream fileOutput = new FileOutputStream(file);

        //this will be used in reading the data from the internet
        InputStream inputStream = urlConnection.getInputStream();

        //this is the total size of the file
        int totalSize = urlConnection.getContentLength();
        //variable to store total downloaded bytes
        int downloadedSize = 0;

        //create a buffer...
        byte[] buffer = new byte[1024];
        int bufferLength = 0; // used to store a temporary size of the buffer

        //now, read through the input buffer and write the contents to the file
        while ( (bufferLength = inputStream.read(buffer)) > 0 ) {
            //add the data in the buffer to the file in the file output stream (the file on the sd card
            fileOutput.write(buffer, 0, bufferLength);
            //add up the size so we know how much is downloaded
            downloadedSize += bufferLength;
            //this is where you would do something to report the prgress, like this maybe
            if (downloadedSize > 0 && !startToastShowed) {
                if (Cobalt.DEBUG) Log.d(TAG, "Download of " + totalSize + " bytes started.");
                Toast.makeText(SharePlugin.currentFragment.getContext(), "Downloading " + mType + "in progress...", Toast.LENGTH_LONG).show();
                startToastShowed = true;
            }
            if (downloadedSize == totalSize) {
                if (Cobalt.DEBUG) Log.d(TAG, "Downloaded " + downloadedSize + " bytes stored in " + file.getPath());
                Toast.makeText(SharePlugin.currentFragment.getContext(), "Downloading completed in " + file.getPath(), Toast.LENGTH_LONG).show();
            }
        }
        //close the output stream when done
        fileOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }
}
