package io.kristal.shareme;

import android.content.Intent;

/**
 * Created by Roxane P. on 4/18/16.
 */
public class ShareSimpleShareData implements ShareDataInterface {
    private String text;
    private String title;

    public ShareSimpleShareData(String title, String text) {
        this.text = text;
        this.title = title;
    }

    public Intent returnShareIntent() {
        Intent share = new Intent();
        share.setAction(Intent.ACTION_SEND);
        share.putExtra(Intent. EXTRA_TEXT, text);
        share.putExtra(Intent.EXTRA_SUBJECT, title);
        share.setType("text/plain");
        return share;
    }
}
