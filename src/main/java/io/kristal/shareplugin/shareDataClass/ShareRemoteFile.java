package io.kristal.shareplugin.shareDataClass;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.webkit.URLUtil;
import android.widget.Toast;

import org.cobaltians.cobalt.Cobalt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import io.kristal.shareplugin.SharePlugin;
import io.kristal.shareplugin.interfaces.ShareDataInterface;
import io.kristal.shareplugin.utils.FileSystemTools;
import io.kristal.shareplugin.utils.IntentsTools;
import io.kristal.shareplugin.utils.Tokens;

/**
 * Created by Roxane P. on 4/22/16.
 * share from drawable
 */
public class ShareRemoteFile implements ShareDataInterface {

    private static final String TAG = "ShareRemoteFile";
    private String mPath;
    private URL mUrl;
    private final String mRawUrl;
    private final String mType;
    private String mTitle = null;
    private String mFileName;
    private String mDetail = null;

    /**
     * ShareRemoteFile constructor
     * Load and send an intent from files data
     *
     * @param data - hashMap containing file's data
     */
    public ShareRemoteFile(Map data) {
        // mandatory data
        this.mType = data.get(Tokens.JS_TOKEN_TYPE).toString();
        this.mRawUrl = data.get(Tokens.JS_TOKEN_PATH).toString();
        this.mFileName = URLUtil.guessFileName(this.mRawUrl, null, null); // parse url finding name
        try {
            this.mUrl = new URL(this.mRawUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error while parsing URL " + this.mRawUrl + ".");
            e.printStackTrace();
        }
        // optional data
        if (data.containsKey(Tokens.JS_TOKEN_TITLE)) {
            this.mTitle = data.get(Tokens.JS_TOKEN_TITLE).toString();
        }
        if (data.containsKey(Tokens.JS_TOKEN_DETAIL)) {
            this.mDetail = data.get(Tokens.JS_TOKEN_DETAIL).toString();
        }

        File file = new File(SharePlugin.pathFileStorage + mFileName);
        if (file.exists()) { // no need to download a new one
            SharePlugin.doShare(returnShareIntent());
        } else {
            SharePlugin.currentFragment.getActivity().runOnUiThread(new Runnable() {
                public void run() {
                    new DownloadFileAsync().execute(mRawUrl);
                }
            });
        }
    }

    /**
     * return a ready-to-launch intent for different resource files
     * TODO: 4/22/16 other protocol (ftp, smb...)
     */
    @Override
    public Intent returnShareIntent() {
        switch (mType) {
            case Tokens.JS_TOKEN_IMAGE_TYPE:
                return createIntent("Picture");
            case Tokens.JS_TOKEN_AUDIO_TYPE:
                return createIntent("Music");
            case Tokens.JS_TOKEN_DOCUMENT_TYPE:
                return createIntent("Document");
            case Tokens.JS_TOKEN_VIDEO_TYPE:
                return createIntent("Video");
            case Tokens.JS_TOKEN_DATA_TYPE:
                return createIntent("File");
            default:
                Log.e(TAG, "No action defined for this type of file from this source.");
                return null;
        }
    }

    /**
     * return intent for a type of file
     **/
    private Intent createIntent(String fileType) {
        Intent share;
        String applicationName = IntentsTools.getApplicationName();
        StringBuilder stringBuilder = new StringBuilder(56).append("A ");
        stringBuilder.append(fileType);
        stringBuilder.append(" from ").append(applicationName).append("...");
        if (mTitle == null) mTitle = stringBuilder.toString();
        // check if file exist
        File file = new File(mPath);
        if(!file.exists()) {
            Log.e(TAG, "Error when writing file at " + mPath);
            return null;
        }
        // parse path to uri
        Uri uri = Uri.fromFile(file);
        share = new Intent(Intent.ACTION_SEND);
        // set MimeType
        share.setType(IntentsTools.getMimeType(mRawUrl));
        // place extras
        share.putExtra(Intent.EXTRA_STREAM, uri);
        share.putExtra(android.content.Intent.EXTRA_SUBJECT, (mTitle == null ? "Subject for message" : mTitle));
        share.putExtra(android.content.Intent.EXTRA_TEXT, (mDetail == null ? "Body for message" : mDetail));
        // return intent for launching
        return share;
    }

    /**
     * DownloadFileAsync asynchronous thread
     * downloading a file from a direct url
     */
    public class DownloadFileAsync extends AsyncTask<String, String, String> {
        private PowerManager.WakeLock mWakeLock;
        ProgressDialog mProgressDialog = null;

        public DownloadFileAsync() {
            mProgressDialog = new ProgressDialog(SharePlugin.currentContext);
            mProgressDialog.setMessage("Downloading attachment in progress..."); // TODO: 5/3/16 multilingual
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            mProgressDialog.setCancelable(true);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) SharePlugin.currentContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... urls) {
            HttpURLConnection urlConnection;
            try {
                urlConnection = (HttpURLConnection) mUrl.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout (5000) ;
                urlConnection.connect();
                // create file
                File file = new File(SharePlugin.pathFileStorage, mFileName);
                if (Cobalt.DEBUG)
                    Log.d(TAG, mFileName + " will be stored in " + file.getAbsolutePath());
                // check if connection return proper response
                if (urlConnection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    FileSystemTools.deleteFile(file.getAbsolutePath());
                    Log.e(TAG, "HttpURLConnection return bad code: " + urlConnection.getResponseCode() + " when downloading from url " + mRawUrl);
                    return null;
                }
                // init stream to be copied
                mPath = SharePlugin.pathFileStorage + mFileName;
                InputStream input = new BufferedInputStream(mUrl.openStream());
                OutputStream output = new FileOutputStream(mPath);
                // write file
                int count;
                long total = 0;
                byte data[] = new byte[1024];
                while ((count = input.read(data)) > 0) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / urlConnection.getContentLength()));
                    output.write(data, 0, count);
                }
                // close stream
                output.flush();
                output.close();
                input.close();
                return String.valueOf(urlConnection.getContentLength());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate(progress);
            // update dialog progress
            mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String result) {
            // remove CPU lock
            mWakeLock.release();
            // remove dialog as the download is complete
            mProgressDialog.dismiss();
            if (result != null) {
                if (Cobalt.DEBUG) Log.d(TAG, "File downloaded length " + result);
                Toast.makeText(SharePlugin.currentFragment.getContext(), "Downloading completed in " + mPath, Toast.LENGTH_LONG).show();
            } else {
                Log.e(TAG, "Download error.");
            }
            // finally, launch share
            SharePlugin.doShare(returnShareIntent());
        }
    }
}
