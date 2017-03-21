package com.example.ukasz.androidsqlite;

/**
 * Created by Łukasz on 2017-03-20.
 */

public class Block
{
    private String nr_declarant;
    private String nr_blocked;
    private int reason_category;
    private String reason_description;
    private boolean nr_rating;

    public Block(){

    }

    public Block(String nr_dec, String nr_bloc, int r_cat, String r_desc, boolean nr_rat)
    {
        this.nr_declarant = nr_dec;
        this.nr_blocked = nr_bloc;
        this.reason_category = r_cat;
        this.reason_description = r_desc;
        this.nr_rating = nr_rat;
    }

    public Block(String nr_dec, String nr_bloc, int r_cat, boolean nr_rat)
    {
        this.nr_declarant = nr_dec;
        this.nr_blocked = nr_bloc;
        this.reason_category = r_cat;
        this.reason_description = "";
        this.nr_rating = nr_rat;
    }

    public void setNrDeclarant(String nr_dec)
    {
        this.nr_declarant = nr_dec;
    }

    public void setNrBlocked(String nr_bloc)
    {
        this.nr_blocked = nr_bloc;
    }

    public void setReasonCategory(int r_cat)
    {
        this.reason_category = r_cat;
    }

    public void setReasonDescription(String r_desc)
    {
        this.reason_description = r_desc;
    }

    public void setNrRating(boolean nr_rat)
    {
        this.nr_rating = nr_rat;
    }

    public String getNrDeclarant()
    {
        return this.nr_declarant;
    }

    public String getNrBlocked()
    {
        return this.nr_blocked;
    }

    public int getReasonCategory()
    {
        return this.reason_category;
    }

    public String getReasonDescription()
    {
        return this.reason_description;
    }

    public boolean getNrRating()
    {
        return this.nr_rating;
    }

}
