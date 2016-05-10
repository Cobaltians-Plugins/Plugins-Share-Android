package io.kristal.shareplugin.shareDataClass;

import android.content.Intent;
import android.provider.ContactsContract;

import java.util.Map;

import io.kristal.shareplugin.interfaces.ShareDataInterface;
import io.kristal.shareplugin.utils.Tokens;

/**
 * Created by Roxane P. on 4/18/16.
 * ShareContactData
 * Share from a person data
 */
public class ShareContactData implements ShareDataInterface {
    private String name;
    private String mobile;
    private String email = null;
    private String company = null;
    private String postal = null;
    private String job = null;
    private String detail = null;
    // TODO: 4/28/16 image and note for contact

    /**
     * ShareContactData constructor
     * @param data - Map data of the people
     */
    public ShareContactData(Map data) {
        // mandatory data
        this.name = data.get(Tokens.JS_TOKEN_CONTACT_NAME).toString();
        this.mobile = data.get(Tokens.JS_TOKEN_CONTACT_MOBILE).toString();

        // optional data
        if (data.containsKey(Tokens.JS_TOKEN_CONTACT_EMAIL)) this.email = data.get(Tokens.JS_TOKEN_CONTACT_EMAIL).toString();
        if (data.containsKey(Tokens.JS_TOKEN_CONTACT_COMPANY)) this.company = data.get(Tokens.JS_TOKEN_CONTACT_COMPANY).toString();
        if (data.containsKey(Tokens.JS_TOKEN_CONTACT_POSTAL)) this.postal = data.get(Tokens.JS_TOKEN_CONTACT_POSTAL).toString();
        if (data.containsKey(Tokens.JS_TOKEN_CONTACT_JOB)) this.job = data.get(Tokens.JS_TOKEN_CONTACT_JOB).toString();
        if (data.containsKey(Tokens.JS_TOKEN_DETAIL)) this.detail = data.get(Tokens.JS_TOKEN_DETAIL).toString();
    }

    /**
     * return a ready-to-launch intent for a contact
     */
    public Intent returnShareIntent() {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        // Sets the MIME type
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        // mandatory data
        intent.putExtra(ContactsContract.Intents.Insert.NAME, this.name);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, this.mobile);
        // others data
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, this.email);
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, this.company);
        intent.putExtra(ContactsContract.Intents.Insert.POSTAL, this.postal);
        intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, this.job);
        intent.putExtra(ContactsContract.Intents.Insert.NOTES, this.detail);
        return intent;
    }
}
