package com.example.ukasz.phonecallsblocker.notification_helper;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton class for generating notifications unique id.
 */
public class NotificationID
{
    private final static AtomicInteger c = new AtomicInteger(0);

    /**
     * Gets the ID of new notification.
     *
     * @return ID of new notification
     */
    public static int getID()
    {
        return c.incrementAndGet();
    }
}
