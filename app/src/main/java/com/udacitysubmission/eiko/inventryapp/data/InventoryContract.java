package com.udacitysubmission.eiko.inventryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by eiko on 12/26/2016.
 */
public final class InventoryContract {

    private InventoryContract() {
    }

    public static final String CONTENT_AUTHORITY =
            "com.udacitysubmission.eiko.inventryapp";
    public static final Uri BASE_CONTENT_URI =
            Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_INTENTORY = "inventoryapp";
    public static final String NO_IMAGE =
            "res/drawable/ic_launcher_for_drawable.png";

    public static abstract class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(
                BASE_CONTENT_URI, PATH_INTENTORY);

        public static final String TABLE_NAME = "inventryapp";

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/"
                        + CONTENT_AUTHORITY + "/" + PATH_INTENTORY;

        public static final String CONTENTITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"
                        + CONTENT_AUTHORITY + PATH_INTENTORY;

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_IMAGE = "image";
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";

    }
}
