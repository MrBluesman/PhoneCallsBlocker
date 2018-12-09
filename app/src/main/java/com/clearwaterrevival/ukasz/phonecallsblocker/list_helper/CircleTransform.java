package com.clearwaterrevival.ukasz.phonecallsblocker.list_helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;

public class CircleTransform extends BitmapTransformation
{
    /**
     * Instance of CircleTransform constructor.
     *
     * @param context Context of the application.
     */
    public CircleTransform(Context context)
    {
        super(context);
    }

    /**
     * Transforms a BitmapPool to different Bitmap.
     *
     * @param pool BitmapPool which describes in what pool transform the Bitmap.
     * @param toTransform Bitmap source of the cropping.
     * @param outWidth Transforms out width.
     * @param outHeight Transforms out height.
     * @return Cropped Bitmap.
     */
    @Override
    protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight)
    {
        return circleCrop(pool, toTransform);
    }

    /**
     * Crops A BitmapPool pool to the circle.
     *
     * @param pool Shape of the cropping.
     * @param source Source Bitmap to crop.
     * @return
     */
    private static Bitmap circleCrop(BitmapPool pool, Bitmap source)
    {
        if (source == null) return null;

        int size = Math.min(source.getWidth(), source.getHeight());
        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squared = Bitmap.createBitmap(source, x, y, size, size);

        Bitmap result = pool.get(size, size, Bitmap.Config.ARGB_8888);
        if (result == null)
        {
            result = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setShader(new BitmapShader(squared, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);
        float r = size / 2f;
        canvas.drawCircle(r, r, r, paint);
        return result;
    }

    @Override
    public String getId()
    {
        return getClass().getName();
    }
}
