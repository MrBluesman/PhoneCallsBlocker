package com.clearwaterrevival.ukasz.phonecallsblocker;

import com.clearwaterrevival.ukasz.phonecallsblocker.phone_number_helper.PhoneNumberHelper;

import org.junit.Test;

import static org.junit.Assert.*;

public class PhoneNumberValidatorUnitTest
{
    private PhoneNumberHelper phoneNumberHelper = new PhoneNumberHelper();

    @Test
    public void correctPhoneNumber() throws Exception
    {
        assertTrue(phoneNumberHelper.validateUsingLibphonenumber("48","721315778"));
    }

    @Test
    public void correctPhoneNumber_countryCodeInField() throws Exception
    {
        assertTrue(phoneNumberHelper.validateUsingLibphonenumber("48","+48 721 315 778"));
    }

    @Test
    public void correctPhoneNumber_ParenthesesInField() throws Exception
    {
        assertTrue(phoneNumberHelper.validateUsingLibphonenumber("48","(+48 721 315 778"));
    }

    @Test
    public void correctPhoneNumber_whiteSpaced() throws Exception
    {
        assertTrue(phoneNumberHelper.validateUsingLibphonenumber("48","7 213    157  78"));
    }

    @Test
    public void incorrectPhoneNumber_notNumbers() throws Exception
    {
        assertFalse(phoneNumberHelper.validateUsingLibphonenumber("48","7 213 das s   157  78"));
    }

    @Test
    public void nullPhoneNumber() throws Exception
    {
        assertFalse(phoneNumberHelper.validateUsingLibphonenumber("48",null));
    }

    @Test
    public void emptyPhoneNumber() throws Exception
    {
        assertFalse(phoneNumberHelper.validateUsingLibphonenumber("48",""));
    }

    @Test
    public void emptyCountryCode() throws Exception
    {
        assertFalse(phoneNumberHelper.validateUsingLibphonenumber("","721315778"));
    }

    @Test
    public void foreignPhoneCode() throws Exception
    {
        assertFalse(phoneNumberHelper.validateUsingLibphonenumber("1","721315778"));
    }
}