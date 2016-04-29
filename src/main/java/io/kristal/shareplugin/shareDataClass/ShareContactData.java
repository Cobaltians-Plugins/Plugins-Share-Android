package io.kristal.shareplugin.shareDataClass;

import android.content.Intent;
import android.provider.ContactsContract;

import java.util.Map;

import io.kristal.shareplugin.interfaces.ShareDataInterface;

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
        this.name = data.get("name").toString();
        this.mobile = data.get("mobile").toString();

        // optional data
        if (data.containsKey("email")) this.email = data.get("email").toString();
        if (data.containsKey("company")) this.email = data.get("company").toString();
        if (data.containsKey("postal")) this.email = data.get("postal").toString();
        if (data.containsKey("job")) this.email = data.get("job").toString();
        if (data.containsKey("detail")) this.email = data.get("detail").toString();
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

        // not mandatory
        intent.putExtra(ContactsContract.Intents.Insert.EMAIL, this.email);
        intent.putExtra(ContactsContract.Intents.Insert.COMPANY, this.company);
        intent.putExtra(ContactsContract.Intents.Insert.POSTAL, this.postal);
        intent.putExtra(ContactsContract.Intents.Insert.JOB_TITLE, this.job);
        intent.putExtra(ContactsContract.Intents.Insert.NOTES, this.detail);

        return intent;
    }
}
