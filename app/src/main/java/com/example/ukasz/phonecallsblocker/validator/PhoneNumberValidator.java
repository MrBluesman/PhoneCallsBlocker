package com.example.ukasz.phonecallsblocker.validator;

import android.text.TextUtils;
import android.util.Patterns;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

public class PhoneNumberValidator
{
    /**
     * Mandatory empty constructor.
     * Creates a instance of {@link PhoneNumberValidator}
     */
    public PhoneNumberValidator()
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
    public String formatPhoneNuber(String phoneNumber, String countryCode, PhoneNumberUtil.PhoneNumberFormat format)
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
}