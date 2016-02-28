package com.hackillinoisproj2016.android.image_test;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
//import android.net.Credentials;
import android.net.Uri;
//import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;
import com.clarifai.api.exception.ClarifaiException;
import com.firebase.client.Firebase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static android.provider.MediaStore.Images.Media;

public class MainActivity extends Activity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int CODE_PICK = 1;

    private final ClarifaiClient client = new ClarifaiClient(Credentials.CLIENT_ID,
            Credentials.CLIENT_SECRET);
    //ArrayList<Bitmap> img_bitmap = new ArrayList<Bitmap>();


    private Button selectButton;
    private ImageView imageView;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       Firebase.setAndroidContext(this);
        setContentView(R.layout.activity_main);
        Firebase myFirebaseRef = new Firebase("https://<blazing-inferno-4590>.firebaseio.com/");
        imageView = (ImageView) findViewById(R.id.image_view);
        textView = (TextView) findViewById(R.id.textdisp);
        //Bitmap bMap = BitmapFactory.decodeFile("/storage/emulated/0/DCIM/Camera" + "/20160227_010011.jpg");
        selectButton = (Button) findViewById(R.id.select_button);
        myFirebaseRef.child("message").setValue("Do you have data? You'll love Firebase.");

        selectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Send an intent to launch the media picker.
                //final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(intent, CODE_PICK);
               /* String[] projection = new String[]{
                        MediaStore.Images.Media._ID,
                        MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_TAKEN
                };


                Uri uri_1 = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;


                Cursor cur = getContentResolver().query(uri_1,
                        projection, // Which columns to return
                        null,       // Which rows to return (all rows)
                        null,       // Selection arguments (none)
                        null        // Ordering
                );
                if(cur.moveToFirst()){
                    do{
                        Uri uri = cur.getNotificationUri()
                    }
                }

            }
        });*/
                //Bitmap bitmap = loadBitmapFromUri(intent.getData());
                //imageView.setImageBitmap(bitmap);
               /* String[] projection = {MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.Media._ID);
                int count = cursor.getCount();
                int image_column_index = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int image_path_index = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                String[] path = new String[count];
                Bitmap[] bt = new Bitmap[count];
                for (int i = 0; i < count; i++)
                {
                    cursor.moveToPosition(i);
                    int id = cursor.getInt(image_column_index);
                    path[i] = cursor.getString(image_path_index);
                    bt[i] = MediaStore.Images.Thumbnails.getThumbnail(getApplicationContext().getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null)*/;
                    final Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(intent, CODE_PICK);


                }

        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

                Bitmap bitmap = loadBitmapFromUri(intent.getData());
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap);
                    selectButton.setEnabled(false);
                    new AsyncTask<Bitmap, Void, RecognitionResult>() {
                        @Override
                        protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                            return recognizeBitmap(bitmaps[0]);
                        }

                        @Override
                        protected void onPostExecute(RecognitionResult result) {
                            updateUIForResult(result);
                        }
                    }.execute(bitmap);
                }
            }





    /** Loads a Bitmap from a content URI returned by the media picker. */
   private Bitmap loadBitmapFromUri(Uri uri) {
        try {
            // The image may be large. Load an image that is sized for display. This follows best
            // practices from http://developer.android.com/training/displaying-bitmaps/load-bitmap.html
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
           /* int sampleSize = 1;
            while (opts.outWidth / (2 * sampleSize) >= imageView.getWidth() &&
                    opts.outHeight / (2 * sampleSize) >= imageView.getHeight()) {
                sampleSize *= 2;
            }*/

            opts = new BitmapFactory.Options();
            //opts.inSampleSize = sampleSize;
            return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), null, opts);
        } catch (IOException e) {
            Log.e(TAG, "Error loading image: " + uri, e);
        }
        return null;
   }
   private RecognitionResult recognizeBitmap(Bitmap bitmap) {
        try {
            // Scale down the image. This step is optional. However, sending large images over the
            // network is slow and  does not significantly improve recognition performance.
            Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 320,
                    320 * bitmap.getHeight() / bitmap.getWidth(), true);

            // Compress the image as a JPEG.
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            scaled.compress(Bitmap.CompressFormat.JPEG, 90, out);
            byte[] jpeg = out.toByteArray();

            // Send the JPEG to Clarifai and return the result.
            return client.recognize(new RecognitionRequest(jpeg)).get(0);
        } catch (ClarifaiException e) {
            Log.e(TAG, "Clarifai error", e);
            return null;
        }
   }

    /** Updates the UI by displaying tags for the given result. */
    private void updateUIForResult(RecognitionResult result) {
        if (result != null) {
            if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
                // Display the list of tags in the UI.
                StringBuilder b = new StringBuilder();

                for (Tag tag : result.getTags()) {
                 //  if(tag.getName().equals("wood") || tag.getName().equals("symbol"))
                        b.append(b.length() > 0 ? ", " : "").append(tag.getName());

                }
                textView.setText("Tags:\n" + b);
            } else {
                Log.e(TAG, "Clarifai: " + result.getStatusMessage());
                textView.setText("Sorry, there was an error recognizing your image.");
            }
        } else {
          //  textView.setText("Sorry, there was an error recognizing your image.");
        }
        selectButton.setEnabled(true);
    }

}
