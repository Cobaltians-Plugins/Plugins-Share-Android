package io.kristal.shareplugin.utils;

import android.util.Log;

import org.cobaltians.cobalt.Cobalt;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.kristal.shareplugin.SharePlugin;

/**
 * Created by Roxane P. on 4/18/16.
 * ParsingShareData
 * Take a Json Object Message from web side
 * Parse it, and return an HashMap containing the data
 */

public class ParsingShareData {

    public static final String TAG = "ParsingShareData";

    private JSONObject message;

    public ParsingShareData(JSONObject message) {
        this.message = message;
    }

    /**
     * returnDataFromWeb get hash map with data from any resource type
     *
     * @return - a Map<String, String> data containing the file data
     * @throws JSONException if the given JSON message is malformed.
     */
    public Map<String, String> returnDataFromWeb() throws JSONException {
        // Data store all the key-value for the file in data
        Map<String, String> data = new HashMap<String, String>();

        // Extract data in JSONObject from JSONArray
        JSONArray messageJSONArray = message.getJSONArray("data");
        JSONObject shareData = messageJSONArray.getJSONObject(0);

        // Type is a mandatory value
        if (!shareData.has("type")) return null;
        String typeFile = shareData.getString("type");
        // Set type of file
        data.put("type", typeFile);

        // Set data of this file in map
        switch (typeFile) {
            /**
             * Return share data for a bloc of text
             * mandatory params: type, content
             * optional params: title
             */
            case SharePlugin.TYPE_TEXT_KEY:
                // mandatory data:
                data.put("content", shareData.getString("content"));
                if (shareData.has("title")) {
                    data.put("title", shareData.getString("title"));
                }
                if (Cobalt.DEBUG)
                    Log.d(TAG, SharePlugin.TYPE_TEXT_KEY + " Json parsed: " + data.toString());
                return data;
            /**
             * Return share data for a contact
             * mandatory params: type, name, mobile
             * optional params: email, company, postal, job, detail
             */
            case SharePlugin.TYPE_CONTACT_KEY:
                // mandatory data:
                if (shareData.has("name") && shareData.has("mobile")) {
                    data.put("name", shareData.getString("name"));
                    data.put("mobile", shareData.getString("mobile"));
                } else {
                    return null; // TODO: 4/28/16 error catching
                }
                // others data:
                if (shareData.has("email")) {
                    data.put("email", shareData.getString("email"));
                }
                if (shareData.has("company")) {
                    data.put("company", shareData.getString("company"));
                }
                if (shareData.has("postal")) {
                    data.put("postal", shareData.getString("postal"));
                }
                if (shareData.has("job")) {
                    data.put("job", shareData.getString("job"));
                }
                if (shareData.has("detail")) {
                    data.put("detail", shareData.getString("detail"));
                }
                if (Cobalt.DEBUG)
                    Log.d(TAG, SharePlugin.TYPE_CONTACT_KEY + " Json parsed: " + data.toString());
                return data;
            /**
             * Return share data for a image
             * mandatory params: type, source, path / id
             * optional params: title, detail
             */
            case SharePlugin.TYPE_IMAGE_KEY:
                // mandatory data:
                if (shareData.has("source") && shareData.has("path")) {
                    data.put("source", shareData.getString("source"));
                    data.put("path", shareData.getString("path"));
                } else if (shareData.has("source") && shareData.has("id")) {
                    data.put("source", shareData.getString("source"));
                    data.put("id", shareData.getString("id"));
                } else {
                    return null; // TODO: 4/28/16 error catching
                }
                // others data:
                if (shareData.has("title")) {
                    data.put("title", shareData.getString("title"));
                }
                if (shareData.has("detail")) {
                    data.put("detail", shareData.getString("detail"));
                }
                if (Cobalt.DEBUG)
                    Log.d(TAG, SharePlugin.TYPE_IMAGE_KEY + " Json parsed: " + data.toString());
                return data;
            /**
             * Return share data for type of file (data/audio/video/document)
             * mandatory params: type, source, path / id
             * optional params: title, detail
             */
            case SharePlugin.TYPE_DATA_KEY:
            case SharePlugin.TYPE_AUDIO_KEY:
            case SharePlugin.TYPE_VIDEO_KEY:
            case SharePlugin.TYPE_DOCUMENT_KEY:
                // mandatory data:
                if (shareData.has("source") && shareData.has("path")) {
                    data.put("source", shareData.getString("source"));
                    data.put("path", shareData.getString("path"));
                } else if (shareData.has("source") && shareData.has("id")) {
                    data.put("source", shareData.getString("source"));
                    data.put("id", shareData.getString("id"));
                } else {
                    return null; // TODO: 4/28/16 error catching
                }
                // others data:
                if (shareData.has("title")) {
                    data.put("title", shareData.getString("title"));
                }
                if (shareData.has("detail")) {
                    data.put("detail", shareData.getString("detail"));
                }
                if (Cobalt.DEBUG)
                    Log.d(TAG, SharePlugin.TYPE_DATA_KEY + " Json parsed: " + data.toString());
                return data;
        }
        return null;
    }
}
