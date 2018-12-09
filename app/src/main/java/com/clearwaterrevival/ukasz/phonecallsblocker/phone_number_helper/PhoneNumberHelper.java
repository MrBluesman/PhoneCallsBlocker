package com.clearwaterrevival.ukasz.phonecallsblocker.phone_number_helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Patterns;

import com.clearwaterrevival.ukasz.phonecallsblocker.CallDetector;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneNumberHelper
{
    /**
     * Mandatory empty constructor.
     * Creates a instance of {@link PhoneNumberHelper}
     */
    public PhoneNumberHelper()
    {

    }

    /**
     * Validates phoneNumber using basic patterns.
     *
     * @param phoneNumber phone number to validate
     * @return true if phone number is valid, false if is not
     */
    public boolean isValidPhoneNumber(CharSequence phoneNumber)
    {
        if (!TextUtils.isEmpty(phoneNumber))
        {
            return Patterns.PHONE.matcher(phoneNumber).matches();
        }
        return false;
    }

    /**
     * Validates phNumber using libphonenumber library including countryCode.
     * @param countryCode country code of the phNumber
     * @param phNumber phone number to validate
     * @return true if phone is valid, false if is not
     */
    public boolean validateUsingLibphonenumber(String countryCode, String phNumber)
    {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
        Phonenumber.PhoneNumber phoneNumber = null;
        try
        {
            //phoneNumber = phoneNumberUtil.parse(phNumber, "IN");  //if you want to pass region code
            phoneNumber = phoneNumberUtil.parse(phNumber, isoCode);
        }
        catch (NumberParseException e)
        {
            System.err.println(e);
        }

        return phoneNumberUtil.isValidNumber(phoneNumber);
    }

    /**
     * Formats phoneNumber including countryCode to specified format.
     *
     * @param phoneNumber phone number to format
     * @param countryCode country code of phone number
     * @param format goal format using {@link PhoneNumberUtil.PhoneNumberFormat}
     * @return formatted {@link String} phone number
     */
    public String formatPhoneNumber(String phoneNumber, String countryCode, PhoneNumberUtil.PhoneNumberFormat format)
    {
        PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();
        String isoCode = phoneNumberUtil.getRegionCodeForCountryCode(Integer.parseInt(countryCode));
        Phonenumber.PhoneNumber phoneNumberLib = null;
        try
        {
            phoneNumberLib = phoneNumberUtil.parse(phoneNumber, isoCode);
        }
        catch (NumberParseException e)
        {
            System.err.println(e);
        }

        assert phoneNumberLib != null;
        return phoneNumberUtil.format(phoneNumberLib, format);
    }

    /**
     * Gets the contact name of phone number.
     *
     * @param context context of the app for the {@link CallDetector} object
     * @param incomingNumber contains the number of incoming call
     * @return contact name or null if it's unknown phone number
     */
    public String getContactName(final Context context, final String incomingNumber)
    {
        ContentResolver cr = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));

        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) return null;

        String contactName = null;
        if(cursor.moveToFirst())
        {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if(!cursor.isClosed()) cursor.close();

        return contactName;
    }
}
