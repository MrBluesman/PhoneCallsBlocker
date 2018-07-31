package com.example.ukasz.androidsqlite;

import java.util.Date;

public class RegistryBlock
{
    private String nr_blocked;
    private boolean nr_rating;
    private Date nr_blocking_date;

    /**
     * Instance of RegistryBlock constructor.
     */
    RegistryBlock()
    {

    }

    /**
     * Instance of RegistryBlock constructor.
     *
     * @param nr_blo blocking number
     * @param nr_rat blocking number rating, positive or negative blocking
     * @param nr_blo_date date of the blocking
     */
    public RegistryBlock(String nr_blo, boolean nr_rat, Date nr_blo_date)
    {
        this.nr_blocked = nr_blo;
        this.nr_rating = nr_rat;
        this.nr_blocking_date = nr_blo_date;
    }

    /**
     * nr_blocked setter.
     *
     * @param nr_blo new nr_blocked
     */
    public void setNrBlocked(String nr_blo)
    {
        this.nr_blocked = nr_blo;
    }

    /**
     * nr_blocking_date setter.
     *
     * @param nr_blo_date new nr_blocking_date
     */
    public void setNrBlockingDate(Date nr_blo_date)
    {
        this.nr_blocking_date = nr_blo_date;
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
     * nr_blocked getter.
     *
     * @return nr_blocked of instance
     */
    public String getNrBlocked()
    {
        return this.nr_blocked;
    }

    /**
     * nr_blocking_date getter.
     *
     * @return nr_blocking_date of instance
     */
    public Date getNrBlockingDate()
    {
        return this.nr_blocking_date;
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
