package com.clarifai.androidstarter;

/**
 * Created by Navid on 2/28/2016.
 */
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.clarifai.api.ClarifaiClient;
import com.clarifai.api.RecognitionRequest;
import com.clarifai.api.RecognitionResult;
import com.clarifai.api.Tag;


public class MainActivity extends Activity {

    Button searchBtn;
    EditText searchString;
    ImageAdapter myImageAdapter;
    File[] files;

    HashMap<String, ArrayList<File>> map;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchString = (EditText) findViewById(R.id.search_string);

        //StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        //StrictMode.setThreadPolicy(policy);
        // picturesRef = myFirebaseRef.child("Pictures");
        //picturesRef.addChildEventListener(new ChildEventListener();

        ArrayList<String> taggingList = new ArrayList<>();
        map = new HashMap<>();
        String ExternalStorageDirectoryPath = Environment
                .getExternalStorageDirectory()
                .getAbsolutePath();
        String targetPath = ExternalStorageDirectoryPath + "/DCIM/Camera/";
        ArrayList<File> fileArrayList = new ArrayList<File>();
        File targetDirector = new File(targetPath);
        files = targetDirector.listFiles();
        ClarifaiClient clarifai = new ClarifaiClient("QwyVMelyYYyB57bUcmAj5pNJ8DFbF2vX_otHSwP1", "uTvXRCBEaenYGwAbae56BIcxavMduS6TtJHMnYCZ");


        ImageAdapter imageAdapter = new ImageAdapter(getApplicationContext());
        GridView gridview = (GridView) findViewById(R.id.gridview);
        myImageAdapter = new ImageAdapter(this);
        gridview.setAdapter(myImageAdapter);


        new AsyncTask<File, Void, Tag>() {
            RecognitionActivity recognitionActivity = new RecognitionActivity();
            Bitmap bitmap = BitmapFactory.decodeFile(files[0].getAbsolutePath());
            @Override
            protected RecognitionResult doInBackground(Bitmap... bitmaps) {
                return recognitionActivity.recognizeBitmap(bitmap);
            }
            @Override protected void onPostExecute(RecognitionResult result) {
                //updateUIForResult(result);
            }
        }.execute();

        Toast.makeText(getApplicationContext(), targetPath, Toast.LENGTH_LONG).show();


        searchBtn = (Button) findViewById(R.id.select_button);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search = searchString.getText() + "";
                ArrayList<File> searchResults = new ArrayList<File>();
                if (map.containsKey(search)) {
                    searchResults = map.get(search);
                    for (int i = 0; i < searchResults.size(); i++) {
                        myImageAdapter.add(searchResults.get(i).getAbsolutePath());
                    }

                } else {
                    System.out.println("No images found");
                }

            }
        });


        //  for (Tag tag : results.get(0).getTags())
        //  results = clarifai.recognize(new RecognitionRequest(file));

        // taggingList = updateUForResult(recognitionActivity.recognizeBitmap(imageAdapter.decodeSampledBitmapFromUri(targetPath+file.getName(), 8, 8)));
        // recognitionActivity.connection();

          /* new AsyncTask<Bitmap, Void, RecognitionResult>() {

               RecognitionActivity recognitionActivity = new RecognitionActivity();
                ImageAdapter imageAdapter = new ImageAdapter(getApplicationContext());

                @Override protected RecognitionResult doInBackground(Bitmap... bitmaps) {

                    return recognitionActivity.recognizeBitmap(bitmaps[0]);
                }
                @Override protected void onPostExecute(RecognitionResult result) {
                    updateUForResult(result);
                }
            }.execute();
            taggingList = updateUForResult(recognitionActivity.recognizeBitmap(imageAdapter.decodeSampledBitmapFromUri(targetPath+file.getName(), 8, 8)));
        }
    }
    /*public void postData(){
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(https://api.clarifai.com/v1/tag/);
        try{

        }*/
    }

    public HashMap<Tag, ArrayList<File>> godMethod(File[] files, ClarifaiClient clarifai) {
        HashMap<Tag, ArrayList<File>> map = new HashMap<>();
        for (File file : files) {
            //myImageAdapter.add(file.getAbsolutePath());
            ArrayList<File> fileArrayList = new ArrayList<File>();
            fileArrayList.add(file);
            try {
                List<RecognitionResult> results =
                        clarifai.recognize(new RecognitionRequest(file));
                for (Tag tag : results.get(0).getTags())
                    if (map.containsKey(tag.getName())) {
                        map.get(tag.getName()).add(file);
                    } else {
                        ArrayList<File> temp = new ArrayList<>();
                        temp.add(file);
                        map.put(tag, temp);
                    }
            } catch (Exception e) {
                System.out.println(file.toString());
            }
        }

        return map;
    }


    private ArrayList<String> updateUForResult(RecognitionResult result) {
        ArrayList<String> tagList = new ArrayList<>();

        if (result != null) {
            if (result.getStatusCode() == RecognitionResult.StatusCode.OK) {
                // Display the list of tags in the UI.
                StringBuilder b = new StringBuilder();
                for (Tag tag : result.getTags()) {
                    tagList.add(tag.getName());
                }
            }
        }
        return tagList;
    }

    public class ImageAdapter extends BaseAdapter {

        private Context mContext;
        ArrayList<String> itemList = new ArrayList<String>();

        public ImageAdapter(Context c) {
            mContext = c;
        }

        void add(String path) {
            itemList.add(path);
        }

        @Override
        public int getCount() {
            return itemList.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(8, 8));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }

            Bitmap bm = decodeSampledBitmapFromUri(itemList.get(position), 220, 220);

            imageView.setImageBitmap(bm);
            return imageView;
        }

        public Bitmap decodeSampledBitmapFromUri(String path, int reqWidth, int reqHeight) {

            Bitmap bm = null;
            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            bm = BitmapFactory.decodeFile(path, options);

            return bm;
        }

        public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
            // Raw height and width of image
            final int height = options.outHeight;
            final int width = options.outWidth;
            int inSampleSize = 1;

            if (height > reqHeight || width > reqWidth) {
                if (width > height) {
                    inSampleSize = Math.round((float) height / (float) reqHeight);
                } else {
                    inSampleSize = Math.round((float) width / (float) reqWidth);
                }
            }

            return inSampleSize;
        }
    }
}

