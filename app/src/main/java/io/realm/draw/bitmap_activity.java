package io.realm.draw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.io.File;

/**
 * Created by JYN on 2017-11-15.
 */

public class bitmap_activity extends Activity {

    ImageView bitmap_IV;
    private static String TAG = "all_"+bitmap_activity.class.getSimpleName();
    Intent intent;
    String file_path;
    String file_name;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bitmap);

        bitmap_IV = findViewById(R.id.bitmap_img);

//        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
//        String file_path = ex_storage + "/draw";
//        Log.d(TAG, "file_path: " + file_path);

        intent = getIntent();
        file_path = intent.getExtras().getString("file_path");
        file_name = intent.getExtras().getString("file_name");

        file = new File(file_path, file_name + ".png");

        Log.d(TAG, "file: " + file_name + ".png");

        Glide
            .with(this)
            .load(file)
            .into(bitmap_IV);

//        Glide.with(bitmap_activity.this).load(bitmap).asBitmap().
//            into(new SimpleTarget<Bitmap>() {
//                @Override
//                public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
//                    bitmap_IV.setImageBitmap(resource);
//                }
//            });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        intent = null;
        file_path = null;
        file = null;
    }
}
