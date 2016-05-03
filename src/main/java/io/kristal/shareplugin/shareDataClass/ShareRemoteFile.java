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

import io.kristal.shareplugin.interfaces.ShareDataInterface;
import io.kristal.shareplugin.SharePlugin;
import io.kristal.shareplugin.utils.FileSystemTools;
import io.kristal.shareplugin.utils.IntentsTools;

/**
 * Created by Roxane P. on 4/22/16.
 * share from drawable
 */
public class ShareRemoteFile implements ShareDataInterface {

    private static final String TAG = "ShareRemoteFile";
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
        this.mType = data.get("type").toString();
        this.mRawUrl = data.get("path").toString();
        this.mFileName =  URLUtil.guessFileName(this.mRawUrl, null, null); // parse url finding name
        try {
            this.mUrl = new URL(this.mRawUrl);
        } catch (MalformedURLException e) {
            Log.e(TAG, "Error while parsing URL " + this.mRawUrl + ".");
            e.printStackTrace();
        }
        // optional data
        if (data.containsKey("title")) {
            this.mTitle = FileSystemTools.fileNameForFileSystem(data.get("title").toString());
        }
        if (data.containsKey("detail")) {
            this.mDetail = data.get("detail").toString();
        }

        File file = new File(SharePlugin.pathFileStorage + mFileName);
        if(file.exists()) { // no need to download a new one
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
        Intent share;
        String applicationName = IntentsTools.getApplicationName();
        StringBuilder stringBuilder = new StringBuilder(56).append("A ");
        switch (mType) {
            case SharePlugin.TYPE_IMAGE_KEY:
                stringBuilder.append("Picture");
            case SharePlugin.TYPE_AUDIO_KEY:
                stringBuilder.append("Music");
            case SharePlugin.TYPE_DOCUMENT_KEY:
                stringBuilder.append("Document");
            case SharePlugin.TYPE_VIDEO_KEY:
                stringBuilder.append("Video");
            case SharePlugin.TYPE_DATA_KEY:
                stringBuilder.append("File");
                stringBuilder.append(" from ").append(applicationName).append("...");
                if (mTitle == null) mTitle = stringBuilder.toString();
                // file comes from assets
                Uri uri = Uri.parse(SharePlugin.SCHEME + SharePlugin.AUTHORITY + "/" + mFileName);
                share = new Intent(Intent.ACTION_SEND);
                // set MimeType
                share.setType(IntentsTools.getMimeType(mRawUrl));
                // place extras
                share.putExtra(Intent.EXTRA_STREAM, uri);
                share.putExtra(android.content.Intent.EXTRA_SUBJECT, (mTitle == null ? "Subject for message" : mTitle));
                share.putExtra(android.content.Intent.EXTRA_TEXT, (mDetail == null ? "Body for message" : mTitle));
                // return intent for launching
                return share;
            default:
                Log.e(TAG, "No action defined for this type of file from this source.");
                break;
        }
        return null;
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
                // Init connection
                URL url = mUrl;
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setDoOutput(true);
                urlConnection.connect();
                // create file
                File file = new File(SharePlugin.pathFileStorage, mFileName);
                if (Cobalt.DEBUG) Log.d(TAG, mFileName + " will be stored in " + file.getAbsolutePath());
                // check if connection return proper response
                if (urlConnection.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST) {
                    if (Cobalt.DEBUG)
                        Log.e(TAG, "HttpURLConnection return bad code: " + urlConnection.getResponseCode());
                    FileSystemTools.deleteFile(file.getAbsolutePath());
                    return null;
                }
                // init stream to be copied
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(SharePlugin.pathFileStorage + mFileName);
                // write file
                int count;
                long total = 0;
                byte data[] = new byte[1024];
                while ((count = input.read(data)) > 0) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) /  urlConnection.getContentLength()));
                    output.write(data, 0, count);
                }
                // close stream
                output.flush();
                output.close();
                input.close();
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
                Log.e(TAG, "Download error " + result);
            } else {
                Log.i(TAG, "File downloaded");
                Toast.makeText(SharePlugin.currentFragment.getContext(), "Downloading completed in " + SharePlugin.pathFileStorage + mFileName, Toast.LENGTH_LONG).show();
            }
            // finally, launch share
            SharePlugin.doShare(returnShareIntent());
        }
    }
}
