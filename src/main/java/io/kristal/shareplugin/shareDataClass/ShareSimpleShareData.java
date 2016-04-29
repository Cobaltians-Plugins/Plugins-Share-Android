package io.kristal.shareplugin.shareDataClass;

import android.content.Intent;

import java.util.Map;

import io.kristal.shareplugin.interfaces.ShareDataInterface;

/**
 * Created by Roxane P. on 4/22/16.
 * share from Strings
 */
public class ShareSimpleShareData implements ShareDataInterface {
    private String text = null;
    private String title = null;

    /**
     * ShareContactData constructor
     * @param data - hashMap containing data text to share
     */
    public ShareSimpleShareData(Map data) {
        // mandatory data
        this.text = data.get("content").toString();
        // optional data
        if (data.containsKey("title")) this.title = data.get("title").toString();
    }

    /**
     * return a ready-to-launch intent for a text
     */
    public Intent returnShareIntent() {
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.putExtra(Intent. EXTRA_TEXT, text);
        if (title != null) share.putExtra(Intent.EXTRA_SUBJECT, title);
        share.setType("text/plain");
        return share;
    }
}
