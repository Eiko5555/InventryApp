package com.udacitysubmission.eiko.inventryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.udacitysubmission.eiko.inventryapp.data.InventoryContract;

import java.io.File;
import java.io.IOException;

/**
 * Created by eiko on 12/26/2016.
 */
public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVENTRY_LOADER = 0;
    private static final int PICK_IMAGE_REQUEST = 1;
    private EditText mItemEditText;
    private EditText mPriceEditText;
    private TextView mQuantityTextView;
    private Button mButtonAddImage;
    private ImageView mImageView;
    private int quantity = 0;
    private Uri mCurrentUri;
    private Uri mImageUri;
    private boolean mInventryHasChanged = false;
    String image;
    String name;

    private View.OnTouchListener mTouchListner =
            new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mInventryHasChanged = true;
                    return false;
                }
            };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentUri = intent.getData();
        Log.v("EditorActivity", "passigonCreate and getting intent");
        if (mCurrentUri == null) {
            setTitle("Add Item");
            invalidateOptionsMenu();
            Log.v("EditorActivity","add item");
        } else {
            setTitle("Edit Item");
            getLoaderManager().initLoader(INVENTRY_LOADER, null, this);
            Log.v("EditorActivity","editing item");
        }

        mItemEditText = (EditText) findViewById(R.id.edit_item_name);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mQuantityTextView = (TextView) findViewById(R.id.quantity_text_view);
        mButtonAddImage = (Button) findViewById(R.id.addButtonImage);
        mImageView = (ImageView)findViewById(R.id.imageEditor);

        Button orderButton = (Button)findViewById(R.id.reorderButton);
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String orderMsg = orderSummery(name);
                Intent intentOrderRequest = new Intent(Intent.ACTION_SENDTO);
                intentOrderRequest.setData(Uri.parse("mailto: amazon@gmail.com"));
                intentOrderRequest.putExtra(Intent.EXTRA_SUBJECT,
                        "Re-order request for " + name);
                Log.v("cursoradapter", "order intent" + name);
                intentOrderRequest.putExtra(Intent.EXTRA_TEXT,
                        orderMsg);
                startActivity(intentOrderRequest);
            }
        });


        mButtonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });

        mItemEditText.setOnTouchListener(mTouchListner);
        mPriceEditText.setOnTouchListener(mTouchListner);
        mQuantityTextView.setOnTouchListener(mTouchListner);
        mButtonAddImage.setOnTouchListener(mTouchListner);
        mImageView.setOnTouchListener(mTouchListner);

    }
    private String orderSummery(String name) {
        String orderMsg = "Order Request\n" +
                "We would like to order 10 of " + name;
        return orderMsg;
    }

    public void openImageSelector() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(
                intent, "Select Image"), PICK_IMAGE_REQUEST);
    }

    public void increment(View view) {
        quantity++;
        displayqyantity(quantity);
    }

    public void decrement(View view) {
        quantity--;
        displayqyantity(quantity);
    }

    private void displayqyantity(int num) {
        mQuantityTextView.setText("" + num);
    }

    private void showUnsavedChengeDialog(DialogInterface.OnClickListener
                                                 discardButtonClickListner) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_dialog_message);
        builder.setPositiveButton(R.string.discard, discardButtonClickListner);
        builder.setNegativeButton(R.string.keep_editing,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void saveItem() {
        String nameString = mItemEditText.getText().toString().trim();
        Log.v("editor activity", "nameString  = " + nameString);
        String priceString = mPriceEditText.getText().toString().trim();
        String imageString = "";
        if (mImageUri != null){
            imageString = mImageUri.toString();
//            if (imageString == null){
////                    Bitmap bm = BitmapFactory.decodeResource(
////                            getResources(),R.drawable.l_e_others);
//                    mImageView.setImageResource(R.drawable.l_e_others);
        }else {
//            imageString = "drawable://" + R.drawable.l_e_others;
//            mImageView.setImageResource(R.drawable.l_e_others);
            imageString = image;
        }

        Log.v("editorvtivity", "image string : " + imageString);

        if (mCurrentUri == null
                && TextUtils.isEmpty(nameString)
                && TextUtils.isEmpty(priceString)
                && mImageUri == null) {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_IMAGE, imageString);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME, nameString);

        int price = 0;
        if (!TextUtils.isEmpty(priceString)) {
            price = Integer.parseInt(priceString);
        }
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE, price);
        values.put(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY, quantity);

        Log.v("EditorActivity", "saveing item");



        if (mCurrentUri == null) {
            getContentResolver().insert(
                    InventoryContract.InventoryEntry.CONTENT_URI, values);
            Toast.makeText(this,"Saved to the list. ", Toast.LENGTH_LONG).show();
        } else {
            getContentResolver().update(
                    mCurrentUri, values, null, null);
            Toast.makeText(this,"saved.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK){
            Uri imageSelected = data.getData();
            mImageUri = imageSelected;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        getContentResolver(),imageSelected );
                mImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mImageView.setImageURI(mImageUri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            case R.id.action_delete:
                showDeletecomfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mInventryHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonOnClickListner =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChengeDialog(discardButtonOnClickListner);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDeletecomfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_message);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        if (mCurrentUri != null) {
            int rowDeleted = getContentResolver().delete(
                    mCurrentUri, null, null);
            if (rowDeleted == 0) {
                Toast.makeText(this, "delete failed", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "delete successful", Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                InventoryContract.InventoryEntry._ID,
                InventoryContract.InventoryEntry.COLUMN_IMAGE,
                InventoryContract.InventoryEntry.COLUMN_ITEM_NAME,
                InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY,
        };
        return new CursorLoader(this, mCurrentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }
        if (cursor.moveToFirst()) {
            int imageColumIndex = cursor.getColumnIndex(
                    InventoryContract.InventoryEntry.COLUMN_IMAGE);
            int nameColumIndex = cursor.getColumnIndex(
                    InventoryContract.InventoryEntry.COLUMN_ITEM_NAME);
            int prieColumIndex = cursor.getColumnIndex(
                    InventoryContract.InventoryEntry.COLUMN_ITEM_PRICE);
            int quantityColumIndex = cursor.getColumnIndex(
                    InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY);

            image = cursor.getString(imageColumIndex);
            name = cursor.getString(nameColumIndex);
            int price = cursor.getInt(prieColumIndex);
            quantity = cursor.getInt(quantityColumIndex);

            mImageView.setImageURI(Uri.parse(image));
            mItemEditText.setText(name);
            mPriceEditText.setText(Integer.toString(price));
            mQuantityTextView.setText(Integer.toString(quantity));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mItemEditText.setText("");
        mPriceEditText.setText("");
        mQuantityTextView.setText("");

        Bitmap bm = BitmapFactory.decodeResource(
                    getResources(),R.drawable.l_e_others);
            mImageView.setImageBitmap(bm);
//            mImageView.setImageResource(R.drawable.l_e_others);
//            mImageView.setImageDrawable(getResources().getDrawable(R.drawable.l_e_others)
//            String string = "@drawable/drawable.l_e_others";
//            int imageresource = getResources().getIdentifier(string,null,
//                    getPackageName());
//            Drawable res = getResources().getDrawable(imageresource);
//            mImageView.setImageDrawable(res);

//            mImageView.getimageResource(Uri.parse("android.resource://com.udacitysubmission.eiko.inventryapp/l_e_others.png"));

//        }
//        File imageFile = new File(InventoryContract.NO_IMAGE);
//        if (imageFile.exists()){
//            Bitmap bitmap = BitmapFactory.decodeFile(
//                    imageFile.getAbsolutePath());
//            mImageView.setImageBitmap(bitmap);
//        }
    }
}
