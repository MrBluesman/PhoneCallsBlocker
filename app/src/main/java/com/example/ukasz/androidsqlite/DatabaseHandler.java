package com.example.ukasz.androidsqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * DatabaseHandler for keeping blocks in database
 * Extends SQLiteOpenHelper
 * Created by Łukasz on 2017-03-20.
 */
public class DatabaseHandler extends SQLiteOpenHelper
{
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "blockedNumbers";
    //table blocking
    private static final String TABLE_BLOCKING = "blocking";
    //columns
    private static final String DECLARANT_KEY_T_B = "nr_declarant";
    private static final String BLOCKED_KEY_T_B = "nr_blocked";
    private static final String REASON_CATEGORY_T_B = "reason_category";
    private static final String REASON_DESCRIPTION_T_B = "reason_description";
    private static final String RATING_T_B = "nr_rating";

    //table category
    private static final String TABLE_CATEGORY = "category";
    //columns
    private static final String ID_KEY_T_C = "id";
    private static final String NAME_T_C = "name";

    /**
     * Constructor which create a new instance od DatabaseHandler by call extended super
     * SQLiteOpenHelper
     *
     * @param context Context of application
     */
    public DatabaseHandler(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    /**
     * Method which runs on creating a SQLite database
     * Contains definition and structure of tables in database
     *
     * @param db Database which will be keeping SQLite tables defined in this method
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        //creating a category table
        String createCategoryTable = "CREATE TABLE " + TABLE_CATEGORY
                + "("
                + ID_KEY_T_C + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NAME_T_C + " VARCHAR(30) NOT NULL"
                + ")";
        db.execSQL(createCategoryTable);

        String fillCategory = "INSERT INTO " + TABLE_CATEGORY + " (" + NAME_T_C + ") VALUES " +
                "('Kategoria1'),('Kategoria2')";
        db.execSQL(fillCategory);

        //create a blocking table
        String createBlockingTable = "CREATE TABLE " + TABLE_BLOCKING
                + "("
                + DECLARANT_KEY_T_B + " VARCHAR(9) NOT NULL, "
                + BLOCKED_KEY_T_B + " VARCHAR(9) NOT NULL, "
                + REASON_CATEGORY_T_B + " INTEGER NOT NULL, "
                + REASON_DESCRIPTION_T_B + " TEXT, "
                + RATING_T_B + " BOOLEAN NOT NULL, "
                + "FOREIGN KEY (" + REASON_CATEGORY_T_B + ") REFERENCES " + TABLE_CATEGORY + "(" + ID_KEY_T_C + ") "
                + "PRIMARY KEY (" + DECLARANT_KEY_T_B + ", " + BLOCKED_KEY_T_B + ") "
                + ")";
        db.execSQL(createBlockingTable);
    }

    /**
     * Method which desroy database structure and creating new by call onCreate(db)
     *
     * @param db Database which will be upgraded
     * @param oldVersion id of old database version
     * @param newVersion id of new database version
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLOCKING);

        onCreate(db);
    }

    /**
     * Method which adds a new blocking to database
     *
     * @param block Instance of block which will be add to database
     */
    public void addBlocking(Block block)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DECLARANT_KEY_T_B, block.getNrDeclarant());
        values.put(BLOCKED_KEY_T_B, block.getNrBlocked());
        values.put(REASON_CATEGORY_T_B, block.getReasonCategory());
        values.put(REASON_DESCRIPTION_T_B, block.getReasonDescription());
        values.put(RATING_T_B, block.getNrRating());

        db.insert(TABLE_BLOCKING, null, values);
        db.close();
    }

    /**
     * Method which return Block instance from database
     *
     * @param nr_declarant Phone number of declarant
     * @param nr_blocked Blocked phone number
     * @return Block instance from database
     */
    public Block getBlocking(String nr_declarant, String nr_blocked)
    {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectBlockings = "SELECT * FROM " + TABLE_BLOCKING
                + " WHERE " + DECLARANT_KEY_T_B + "=" + nr_declarant
                + " AND " + BLOCKED_KEY_T_B + "=" + nr_blocked
                + " LIMIT 1";

        Cursor cursor = db.rawQuery(selectBlockings, null);
        if(cursor != null)
        {
            cursor.moveToFirst();
        }

        Block toReturn = new Block(cursor.getString(0), cursor.getString(1), Integer.parseInt(cursor.getString(2)),
                cursor.getString(3), Boolean.parseBoolean(cursor.getString(4)));

        return toReturn;

    }

    /**
     * Method which returns a list of all blockings for blocked phone number
     *
     * @param nr_blocked Blocked phone number
     * @return List of all Block instances for blocked number in database
     */
    public List<Block> getNumberBlockings(String nr_blocked)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Block> toReturnList = new ArrayList<Block>();

        String selectNumberBlockings = "SELECT * FROM " + TABLE_BLOCKING
                + " WHERE " + BLOCKED_KEY_T_B + "=" + nr_blocked +";";

        Cursor cursor = db.rawQuery(selectNumberBlockings, null);
        if(cursor.moveToFirst())
        {
            do
            {
                Block block = new Block();
                block.setNrDeclarant(cursor.getString(0));
                block.setNrBlocked(cursor.getString(1));
                block.setReasonCategory(Integer.parseInt(cursor.getString(2)));
                block.setReasonDescription(cursor.getString(3));
                block.setNrRating(Boolean.parseBoolean(cursor.getString(4)));

                toReturnList.add(block);
            }
            while (cursor.moveToNext());
        }

        return toReturnList;
    }

    /**
     * Methoch which returns all Block instances from database
     *
     * @return List of all Blocks instances from database
     */
    public List<Block> getAllBlockings()
    {
        List<Block> toReturnList = new ArrayList<Block>();

        String selectAllBlockings = "SELECT * FROM " + TABLE_BLOCKING;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectAllBlockings, null);
        if(cursor.moveToFirst())
        {
            do
            {
                Block block = new Block();
                block.setNrDeclarant(cursor.getString(0));
                block.setNrBlocked(cursor.getString(1));
                block.setReasonCategory(Integer.parseInt(cursor.getString(2)));
                block.setReasonDescription(cursor.getString(3));
                block.setNrRating(Boolean.parseBoolean(cursor.getString(4)));

                toReturnList.add(block);
            }
            while (cursor.moveToNext());
        }

        return toReturnList;
    }

    /**
     * Method which count blockings for block number
     *
     * @param nr_blocked Blocked phone number
     * @return count of blockings for nr_blocked in database
     */
    public int getNumberBlockingsCount(String nr_blocked)
    {
        String countBlockings = "SELECT * FROM " + TABLE_BLOCKING
                + " WHERE " + BLOCKED_KEY_T_B + "=" + nr_blocked +";";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countBlockings, null);

        return cursor.getCount();
    }


    /**
     * Method which checks if block exists in database
     *
     * @param block Block instance which will be checked if exist in database
     * @return true if block exists or false if not exist
     */
    public boolean existBlock(Block block)
    {

        SQLiteDatabase db = this.getWritableDatabase();

        String selectBlockings = "SELECT * FROM " + TABLE_BLOCKING
                + " WHERE " + DECLARANT_KEY_T_B + "=" + "'" + block.getNrDeclarant() + "'"
                + " AND " + BLOCKED_KEY_T_B + "=" + "'" + block.getNrBlocked() + "'";

        Log.d("Exist: ", block.getNrDeclarant());
        Log.d("Exist: ", block.getNrBlocked());

        Cursor cursor = db.rawQuery(selectBlockings, null);
        boolean toReturn = cursor.getCount() > 0;

        Log.d("Exist: ", ""+cursor.getCount());

        return toReturn;
    }

    /**
     * Method which returns count of all blockings in database
     *
     * @return count of all blockings in database
     */
    public int getBlockingsCount()
    {
        String countNumberBlockings = "SELECT * FROM " + TABLE_BLOCKING;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countNumberBlockings, null);

        return cursor.getCount();
    }

    /**
     * Method which update data for block in database
     *
     * @param block Block instance which will be updated
     * @return 1 of updated, 0 if not updated
     */
    public int updateBlocking(Block block)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(DECLARANT_KEY_T_B, block.getNrDeclarant());
        values.put(BLOCKED_KEY_T_B, block.getNrBlocked());
        values.put(REASON_CATEGORY_T_B, block.getReasonCategory());
        values.put(REASON_DESCRIPTION_T_B, block.getReasonDescription());
        values.put(RATING_T_B, block.getNrRating());

        int toReturn = db.update(TABLE_BLOCKING, values, DECLARANT_KEY_T_B + " = ?" +
                    " AND " + BLOCKED_KEY_T_B + " = ?",
            new String[] { String.valueOf(block.getNrDeclarant()), String.valueOf(block.getNrBlocked()) });

        return toReturn;

    }

    /**
     * Method which delete instance of block from database
     *
     * @param block Block instance which will be deleted from database
     */
    public void deleteBlocking(Block block)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_BLOCKING, DECLARANT_KEY_T_B + " = ?" +
                        " AND " + BLOCKED_KEY_T_B + " = ?",
                new String[] { String.valueOf(block.getNrDeclarant()), String.valueOf(block.getNrBlocked()) });

        db.close();
    }

    /**
     * Method which returns count of all categories in database
     *
     * @return count of all categories in database
     */
    public int getCategoriesCount()
    {
        String countCategories = "SELECT * FROM " + TABLE_CATEGORY;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(countCategories, null);
        return cursor.getCount();
    }

    /**
     * Method which deletes all categories from database
     */
    public void clearCategories()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        String createCategoryTable = "CREATE TABLE " + TABLE_CATEGORY
                + "("
                + ID_KEY_T_C + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NAME_T_C + " VARCHAR(30) NOT NULL"
                + ")";
        db.execSQL(createCategoryTable);
    }

    /**
     * Method which update categories by clear and fill again
     */
    public void updateCategories()
    {
        clearCategories();
        fillCategories();
    }

    /**
     * Method which fill categories table in database with predefined categories
     */
    public void fillCategories()
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String fillCategory = "INSERT INTO " + TABLE_CATEGORY + " (" + NAME_T_C + ") VALUES " +
                "('Kategoria1'),('Kategoria2')";
        db.execSQL(fillCategory);
    }

    /**
     * Method which returns list of all categories in database
     *
     * @return list of all categories in database (List of String)
     */
    public List<String> getAllCategories()
    {
        List<String> toReturnList = new ArrayList<String>();

        String selectAllCategories = "SELECT * FROM " + TABLE_CATEGORY;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectAllCategories, null);
        if(cursor.moveToFirst())
        {
            do
            {
                String category = new String(cursor.getString(1));

                toReturnList.add(category);
            }
            while (cursor.moveToNext());
        }

        return toReturnList;
    }
}
