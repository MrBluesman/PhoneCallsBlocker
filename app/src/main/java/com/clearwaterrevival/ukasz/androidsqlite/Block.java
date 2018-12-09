package com.clearwaterrevival.ukasz.androidsqlite;

/**
 * Created by Łukasz on 2017-03-20.
 */

public class Block
{
    private String nr_declarant;
    private String nr_blocked;
    private String nr_declarant_blocked;
    private int reason_category;
    private String reason_description;
    private boolean nr_rating;

    /**
     * Instance of Block Constructor.
     */
    Block()
    {

    }

    /**
     * Instance of Block Constructor.
     *
     * @param nr_dec    Declarant number, number which blocking
     * @param nr_bloc   Blocking number
     * @param r_cat     Category number
     * @param r_desc    Description (optional)
     * @param nr_rat    Rating of blocked number, positive or negative blocking
     */
    public Block(String nr_dec, String nr_bloc, int r_cat, String r_desc, boolean nr_rat)
    {
        this.nr_declarant = nr_dec;
        this.nr_blocked = nr_bloc;
        this.nr_declarant_blocked = nr_dec + "_" + nr_bloc;
        this.reason_category = r_cat;
        this.reason_description = r_desc;
        this.nr_rating = nr_rat;
    }

    /**
     * Instance of Block Constructor.
     *
     * @param nr_dec    Declarant number, number which blocking
     * @param nr_bloc   Blocking number
     * @param r_cat     Category number
     * @param nr_rat    Rating of blocked number, positive or negative blocking
     */
    public Block(String nr_dec, String nr_bloc, int r_cat, boolean nr_rat)
    {
        this.nr_declarant = nr_dec;
        this.nr_blocked = nr_bloc;
        this.nr_declarant_blocked = nr_dec + "_" + nr_bloc;
        this.reason_category = r_cat;
        this.reason_description = "";
        this.nr_rating = nr_rat;
    }

    /**
     * nr_declarant setter.
     *
     * @param nr_dec new nr_declatant
     */
    public void setNrDeclarant(String nr_dec)
    {
        this.nr_declarant = nr_dec;
    }

    /**
     * nr_blocked setter.
     *
     * @param nr_bloc new nr_blocked
     */
    public void setNrBlocked(String nr_bloc)
    {
        this.nr_blocked = nr_bloc;
    }

    /**
     * nr_declarant_blocked setter.
     *
     * @param nr_dec_bloc new nr_declarant_blocked
     */
    public void setNrDeclarantBlocked(String nr_dec_bloc)
    {
        this.nr_declarant_blocked = nr_dec_bloc;
    }

    /**
     * reason_category setter.
     *
     * @param r_cat new reason_category
     */
    public void setReasonCategory(int r_cat)
    {
        this.reason_category = r_cat;
    }

    /**
     * reason_description setter.
     *
     * @param r_desc new reason_description
     */
    public void setReasonDescription(String r_desc)
    {
        this.reason_description = r_desc;
    }

    /**
     * nr_rating setter.
     *
     * @param nr_rat new nr_rating
     */
    public void setNrRating(boolean nr_rat)
    {
        this.nr_rating = nr_rat;
    }

    /**
     * nr_declarant getter.
     *
     * @return nr_declarant of instance
     */
    public String getNrDeclarant()
    {
        return this.nr_declarant;
    }

    /**
     * nr_blocked getter.
     *
     * @return nr_blocked of instance
     */
    public String getNrBlocked()
    {
        return this.nr_blocked;
    }

    /**
     * nr_declarant_blocked getter.
     *
     * @return nr_declarant_blocked of instance
     */
    public String getNrDeclarantBlocked()
    {
        return this.nr_declarant_blocked;
    }

    /**
     * reason_category getter.
     *
     * @return reason_category of instance
     */
    public int getReasonCategory()
    {
        return this.reason_category;
    }

    /**
     * reason_description getter.
     *
     * @return reason_description of instance
     */
    public String getReasonDescription()
    {
        return this.reason_description;
    }

    /**
     * nr_rating getter.
     *
     * @return nr_rating of instance
     */
    public boolean getNrRating()
    {
        return this.nr_rating;
    }

}
