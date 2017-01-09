package com.udacitysubmission.eiko.inventryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import com.udacitysubmission.eiko.inventryapp.R;
import com.udacitysubmission.eiko.inventryapp.data.InventoryContract.InventoryEntry;

/**
 * Created by eiko on 12/26/2016.
 */
public class Provider extends ContentProvider {

    private static final int ITEM = 100;
    private static final int ITEM_ID = 101;
    private static final UriMatcher sUriMatcher =
            new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_INTENTORY, ITEM);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,
                InventoryContract.PATH_INTENTORY + "/#", ITEM_ID);
    }

    private DatabaseHelper mDBhelpper;

    @Override
    public boolean onCreate() {
        mDBhelpper = new DatabaseHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase database = mDBhelpper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                cursor = database.query(InventoryEntry.TABLE_NAME,
                        projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            case ITEM_ID:

                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))
                };
                cursor = database.query(InventoryEntry.TABLE_NAME,
                        projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException(
                        "Can notreach query. this is uri:"+ uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case ITEM_ID:
                return InventoryEntry.CONTENTITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                return insertItem(uri, values);
            default:
                throw new IllegalArgumentException("Could not save...");
        }
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        SQLiteDatabase dbInsert = mDBhelpper.getWritableDatabase();

        String image = values.getAsString(InventoryEntry.COLUMN_IMAGE);
        if (image == null) {
//
//            image = InventoryContract.NO_IMAGE;
//            throw new IllegalArgumentException("please pick image.");
        }

        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item name reqired.");
        }
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_ITEM_PRICE);
        if (price == null) {
            throw new IllegalArgumentException("Item price required.");
        }
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
        if (quantity == null) {
            throw new IllegalArgumentException("Quantity needed.");
        }
        long id = dbInsert.insert(InventoryEntry.TABLE_NAME, null, values);
        if (id == -1) {
            Log.v("Provider", " failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDBhelpper.getWritableDatabase();
        int rowDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ITEM:
                rowDeleted = db.delete(InventoryEntry.TABLE_NAME, selection,
                        selectionArgs);
                break;
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("caon not deleted this.");
        }
        if (rowDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
      switch (match) {
            case ITEM:
                return updateItem(uri, values, selection, selectionArgs);
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{
                        String.valueOf(ContentUris.parseId(uri))};
                return updateItem(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("updatenot supprted for" + uri);
        }
    }

    private int updateItem(Uri uri, ContentValues values, String selection,
                           String[] selectionArgs) {

        if (values.containsKey(InventoryEntry.COLUMN_IMAGE)) {
            String image = values.getAsString(InventoryEntry.COLUMN_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Item name reqired.");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_ITEM_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Item name reqired.");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_PRICE)) {
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_ITEM_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("price required");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_ITEM_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
            if (quantity == null) {
                throw new IllegalArgumentException("quantity reqired.");
            }
        }

        SQLiteDatabase db = mDBhelpper.getWritableDatabase();
        int rowUpdate = db.update(InventoryEntry.TABLE_NAME, values,
                selection, selectionArgs);
        if (rowUpdate != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowUpdate;
    }
}