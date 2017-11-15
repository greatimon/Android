/*
 * Copyright 2016 Realm Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.realm.draw;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

import io.realm.ErrorCode;
import io.realm.ObjectServerError;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.SyncConfiguration;
import io.realm.SyncCredentials;
import io.realm.SyncUser;
import io.realm.draw.models.DrawPath;
import io.realm.draw.models.DrawPoint;
import io.realm.draw.sensor.ShakeSensorEventListener;


public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback,
                                                                View.OnClickListener,
                                                                SeekBar.OnSeekBarChangeListener {
    /** 원래 코드 */
//    private static final String REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/Draw";
//    private static final String AUTH_URL = "http://" + BuildConfig.OBJECT_SERVER_IP + ":9080/auth";
//    private static final String ID = "demo@realm.io";
//    private static final String PASSWORD = "password";

    /** 시도 코드_1  */
//    private static final String ID = "demo@realm.io";
//    private static final String PASSWORD = "password";
//    private static final String REALM_URL = "realm://" + "52.78.88.227" + ":9080/~/Draw";
//    private static final String AUTH_URL = "http://" + "52.78.88.227" + ":9080/auth";
    /** 시도 코드_2  */
    private static final String ID = "timon11";
    private static final String PASSWORD = "dydska11";
    private static final String REALM_URL = "realm://" + "52.78.88.227" + ":9080/~/Draw";
    private static final String AUTH_URL = "http://" + "52.78.88.227" + ":9080/auth";
//    String testRandom_ID = "IM100S";
//    String testRandom_ID = "IMA910S";
    String testRandom_ID = "emul_s8";
//    String testRandom_ID = "emul_im100";


    int stroke = 6;
    int alpha = 255;
    PencilView defaultItemClicked;
    RelativeLayout include_surView_REL;
    SurfaceHolder surfaceHolder;
    Button toBitmap;
    Bitmap scaled;
    float scale;
    Thread thread;
    String random;
    String file_path;
    Canvas canvas;
    ImageView test_img;
    static Handler handler;
    Bitmap bitmap11;

    SeekBar slider_thickness, slider_alpha;
    private static final int THICKNESS_STEP = 2;
    private static final int THICKNESS_MAX = 80;
    private static final int THICKNESS_MIN = 6;
    private static final int ALPHA_STEP = 1;
    private static final int ALPHA_MAX = 255;
    private static final int ALPHA_MIN = 0;

    private static final int EDGE_WIDTH = 683;
    private volatile Realm realm;
    private SurfaceView surfaceView;
    private double ratio = -1;
    private double marginLeft;
    private double marginTop;
    private DrawThread drawThread;
    private String currentColor = "Charcoal" + "&" +testRandom_ID + "&" + stroke + "&" + alpha;
    private DrawPath currentPath;
    private PencilView currentPencil;
    private HashMap<String, Integer> nameToColorMap = new HashMap<>();
    private HashMap<Integer, String> colorIdToName = new HashMap<>();

    private SensorManager sensorManager;
    private Sensor accelerometerSensor;
    private ShakeSensorEventListener shakeSensorEventListener;

    private static String TAG = "all_"+MainActivity.class.getSimpleName();
//    private ArrayList myTrace;
    Bitmap myBitmap01;
    PermissionListener permissionListener;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 기본 펜
        defaultItemClicked = findViewById(R.id.charcoal);
        // 선 두께, 투명도 조절 시크바
        slider_alpha = findViewById(R.id.slider_alpha);
        slider_thickness = findViewById(R.id.slider_thickness);
        // 포토뷰
//        send_img_PV = findViewById(R.id.send_img);
        // 테스트 이미지뷰
        test_img = findViewById(R.id.test_img);

        createUserIfNeededAndAndLogin();

        surfaceView = findViewById(R.id.surface_view);
        surfaceView.getHolder().addCallback(MainActivity.this);

        generateColorMap();
        bindButtons();
        initializeShakeSensor();

        // 기본 펜 클릭
        defaultItemClicked.performClick();

        // 시크바 조절 리스너
        slider_alpha.setMax((ALPHA_MAX - ALPHA_MIN) / ALPHA_STEP);
        int alphaProgress = ((alpha - ALPHA_MIN) / ALPHA_STEP);
        slider_alpha.setProgress(alphaProgress);
        slider_alpha.setOnSeekBarChangeListener(this);

        slider_thickness.setMax((THICKNESS_MAX - THICKNESS_MIN) / THICKNESS_STEP);
        int thicknessProgress = (int) ((stroke - THICKNESS_MIN) / THICKNESS_STEP);
        slider_thickness.setProgress(thicknessProgress);
        slider_thickness.setOnSeekBarChangeListener(this);

        // 백그라운드 이미지 비트맵
//        myBitmap01 = BitmapFactory.decodeResource(getResources(), R.drawable.back_6);

        /** 테스트 코드 -- 서피스뷰 투명하게 */
        surfaceView.setZOrderOnTop(true); // necessary
        surfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        /** 테스트 코드 -- 서피스뷰 스크린샷 관련 */
//        include_surView_REL = findViewById(R.id.include_surView_REL);
        surfaceHolder = surfaceView.getHolder();
//        surfaceViewGroup_FRA = findViewById(R.id.surfaceViewGroup);

        // 퍼미션 리스너(테드_ 라이브러리)
        permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(a_profile.this, "권한 허가", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
//                Toast.makeText(a_profile.this, "권한 거부", Toast.LENGTH_SHORT).show();
            }
        };

        permission_check();

        toBitmap = findViewById(R.id.toBitmap);
//        toBitmap.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//            }
//        });

        /**---------------------------------------------------------------------------
         핸들러 ==>
         ---------------------------------------------------------------------------*/
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 0) {
                    Log.d(TAG, "handleMessage" + msg.toString());
                    bitmap11 = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                    canvas.drawBitmap(bitmap11, 0, 0, null);
                    test_img.setImageBitmap(bitmap11);
                }
            }
        };
    }

//    /** surfaceView bitmap 생성 -- 방법1 */
//    public Bitmap drawBitmap(View view) {
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//
//        surfaceDestroyed(null); //Thread 잠시 멈춤(pause)
//        view.draw(canvas);
//        surfaceCreated(null); //Thread 재개(resume)
//
//        return bitmap;
//    }


    /** 스크린샷 bitmap 생성 -- 방법1 */
    public static Bitmap viewToBitmap(View view) {
        // 6개의 비트맵 객체를 비트맵 타입별(8888,565,4444)로 생성하고 있습니다.
        // 8888 은 알파값이 있고, 표현색상이 풍부합니다.
        // 565 는 알파값이 없고, 표현색상이 줄어듭니다.
        // 4444 는 알파값이 있고, 표현색상이 가장 적습니다.
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (view instanceof SurfaceView) {
            SurfaceView surfaceView = (SurfaceView) view;
            surfaceView.setZOrderOnTop(true);
            surfaceView.draw(canvas);
            surfaceView.setZOrderOnTop(false);
            return bitmap;
        } else {
            //For ViewGroup & View
            view.draw(canvas);
            return bitmap;
        }
    }

    /** 스크린샷 bitmap 생성 -- 방법2 */
    public Bitmap viewToBitmap_2() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        surfaceView.draw(canvas);
        return bitmap;
    }

    /** 스크린샷 bitmap 생성 -- 방법3 */
    public static Bitmap viewToBitmap_3(View view) {
        view.buildDrawingCache(); //optional if drawing cache is enabled.
        Bitmap bitmap = view.getDrawingCache();
        return bitmap;
    }

    /** 스크린샷 bitmap 생성 -- 방법4 */
    public static Bitmap viewToBitmap_4(View view) {
        try {
            view.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(view.getDrawingCache());
            view.setDrawingCacheEnabled(false);
            return bitmap;
        } catch (OutOfMemoryError error){
            Log.d(TAG, "Out of Memory while loadBitmapFromView");
            return null;
        }
    }

    /** 스크린샷 bitmap 생성 -- 방법5 */
    public Bitmap viewToBitmap_5() {
//        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
//        canvas.drawBitmap(bitmap, 0, 0, null);
//
////        Glide
////            .with(this)
////            .load(bitmap)
////            .asBitmap()
////            .into(test_img);
//
//        test_img.setImageBitmap(bitmap);

        return null;
    }

    /** bitMap Activity로 이동 */
    public void toBitmap_btn_clicked(View view) {
        int randomInt = new Random().nextInt(100000);
        random = String.valueOf(randomInt);
        // 방법1
//        file_path = saveBitmaptoJpeg(viewToBitmap(surfaceView), "draw", "testtest" + random);
        // 방법2
//        String file_path = saveBitmaptoJpeg(viewToBitmap_2(), "draw", "testtest" + random);
        // 방법3
//        String file_path = saveBitmaptoJpeg(viewToBitmap_3(surfaceView), "draw", "testtest" + random);
        // 방법4
//        String file_path = saveBitmaptoJpeg(viewToBitmap_4(surfaceView), "draw", "testtest" + random);
        // 방법5
//        file_path = saveBitmaptoJpeg(viewToBitmap_5(), "draw", "testtest" + random);
//
//
//        Intent intent = new Intent(this, bitmap_activity.class);
//        intent.putExtra("file_path", file_path);
//        intent.putExtra("file_name", "testtest" + random);
//        startActivity(intent);

        viewToBitmap_5();
    }

    /** 비트맵 -> 파일로 저장하기 */
    /**
     * Image SDCard Save (input Bitmap -> saved file JPEG)
     * Writer intruder(Kwangseob Kim)
     * @param bitmap : input bitmap file
     * @param folder : input folder name
     * @param name   : output file name
     */
    public static String saveBitmaptoJpeg(Bitmap bitmap, String folder, String name){
        String ex_storage = Environment.getExternalStorageDirectory().getAbsolutePath();
        // Get Absolute Path in External Sdcard

        String folder_name = "/"+folder+"/";
        String file_name = name+".png";
        String string_path = ex_storage+folder_name;
        Log.d(TAG, "string_path + file_name: " + string_path + file_name);

        File file_path;
        try{
            file_path = new File(string_path);
            if(!file_path.isDirectory()){
                file_path.mkdirs();
            }
            FileOutputStream out = new FileOutputStream(string_path+file_name);

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
            return string_path;

        }catch(FileNotFoundException exception){
            Log.e("FileNotFoundException", exception.getMessage());
        }catch(IOException exception){
            Log.e("IOException", exception.getMessage());
        }
        return null;
    }

    /**---------------------------------------------------------------------------
     메소드 ==> 퍼미션 체크
     ---------------------------------------------------------------------------*/
    public void permission_check() {
        // 퍼미션 확인(테드_ 라이브러리)
        new TedPermission(this)
                .setPermissionListener(permissionListener)
//                .setRationaleMessage("다음 작업을 허용하시겠습니까? 기기 사진, 미디어, 파일 액세스")
                .setDeniedMessage("[설정] > [권한] 에서 권한을 허용할 수 있습니다")
                .setGotoSettingButton(true)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                .check();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getId() == slider_thickness.getId()) {
            stroke = THICKNESS_MIN + (progress * THICKNESS_STEP);
            Log.d(TAG, "stroke: " + stroke);
        } else {
            alpha = ALPHA_MIN + (progress * ALPHA_STEP);
            Log.d(TAG, "alpha: " + alpha);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    private void createUserIfNeededAndAndLogin() {
        Log.d(TAG, "createUserIfNeededAndAndLogin");
        final SyncCredentials syncCredentials = SyncCredentials.usernamePassword(ID, PASSWORD, false);

        // Assume user exist already first time. If that fails, create it.
        SyncUser.loginAsync(syncCredentials, AUTH_URL, new SyncUser.Callback<SyncUser>() {
            @Override
            public void onSuccess(SyncUser user) {
                final SyncConfiguration syncConfiguration = new SyncConfiguration.Builder(user, REALM_URL).build();
                Realm.setDefaultConfiguration(syncConfiguration);
                realm = Realm.getDefaultInstance();
            }

            @Override
            public void onError(ObjectServerError error) {
                Log.d(TAG, "error.getErrorCode(): " + error.getErrorCode());
                if (error.getErrorCode() == ErrorCode.INVALID_CREDENTIALS) {
                    // User did not exist, create it
                    SyncUser.loginAsync(SyncCredentials.usernamePassword(ID, PASSWORD, true), AUTH_URL, this);
                } else {
                    String errorMsg = String.format("(%s) %s", error.getErrorCode(), error.getErrorMessage());
                    Toast.makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                    Log.d(TAG, errorMsg);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(shakeSensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(shakeSensorEventListener);
    }

    private void initializeShakeSensor() {

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        shakeSensorEventListener = new ShakeSensorEventListener();
        shakeSensorEventListener.setOnShakeListener(new ShakeSensorEventListener.OnShakeListener() {

            @Override
            public void onShake(int count) {
                wipeCanvas();
            }
        });
    }

    private void bindButtons() {
        int[] buttonIds = {
                R.id.charcoal,
                R.id.elephant,
                R.id.dove,
                R.id.ultramarine,
                R.id.indigo,
                R.id.grape_jelly,
                R.id.mulberry,
                R.id.flamingo,
                R.id.sexy_salmon,
                R.id.peach,
                R.id.melon
        };

        for (int id : buttonIds) {
            View view = findViewById(id);
            view.setOnClickListener(this);
        }

        currentPencil = findViewById(R.id.charcoal);
        currentPencil.setSelected(true);
    }

    private int indexForColorName = 0;

    /**
     * 원래는 컬러 이름만 들어가지만,
     * Realm 서버에 특정 컬럼을 추가하는 방법이 없어
     * colorName에다가 '&' 을 split 기호로서 다른 정보들을 추가하기로 함
     *
     * 추가하는 정보: user_id / 선두께 / 선투명도
     * */
    private String[] colorName = {
            "Charcoal"+ "&" + testRandom_ID,
            "Elephant"+ "&" + testRandom_ID,
            "Dove"+"&" + testRandom_ID,
            "ultramarine"+ "&" + testRandom_ID,
            "indigo"+"&" + testRandom_ID,
            "GrapeJelly"+ "&" + testRandom_ID,
            "mulberry"+ "&" + testRandom_ID,
            "flamingo"+ "&" + testRandom_ID,
            "sexy_salmon"+ "&" + testRandom_ID,
            "Peach"+ "&" + testRandom_ID,
            "Melon"+ "&" + testRandom_ID
    };
//    private String[] colorName = {
//            "Charcoal"+ "&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "Elephant"+ "&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "Dove"+"&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "ultramarine"+ "&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "indigo"+"&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "GrapeJelly"+ "&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "mulberry"+ "&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "flamingo"+ "&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "sexy_salmon"+ "&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "Peach"+ "&" + testRandom_ID + "&" + stroke + "&" + alpha,
//            "Melon"+ "&" + testRandom_ID + "&" +stroke + "&" + alpha
//    };

    private int[] colorInt = {
            0xff1c283f,
            0xff9a9ba5,
            0xffebebf2,
            0xff39477f,
            0xff59569e,
            0xff9a50a5,
            0xffd34ca3,
            0xfffe5192,
            0xfff77c88,
            0xfffc9f95,
            0xfffcc397
    };

    private int[] colorID = {
            R.id.charcoal,
            R.id.elephant,
            R.id.dove,
            R.id.ultramarine,
            R.id.indigo,
            R.id.grape_jelly,
            R.id.mulberry,
            R.id.flamingo,
            R.id.sexy_salmon,
            R.id.peach,
            R.id.melon
    };


    /** 원래코드 */
//    private void generateColorMap() {
//        nameToColorMap.put("Charcoal", 0xff1c283f);
//        nameToColorMap.put("Elephant", 0xff9a9ba5);
//        nameToColorMap.put("Dove", 0xffebebf2);
//        nameToColorMap.put("Ultramarine", 0xff39477f);
//        nameToColorMap.put("Indigo", 0xff59569e);
//        nameToColorMap.put("GrapeJelly", 0xff9a50a5);
//        nameToColorMap.put("Mulberry", 0xffd34ca3);
//        nameToColorMap.put("Flamingo", 0xfffe5192);
//        nameToColorMap.put("SexySalmon", 0xfff77c88);
//        nameToColorMap.put("Peach", 0xfffc9f95);
//        nameToColorMap.put("Melon", 0xfffcc397);
//        colorIdToName.put(R.id.charcoal, "Charcoal");
//        colorIdToName.put(R.id.elephant, "Elephant");
//        colorIdToName.put(R.id.dove, "Dove");
//        colorIdToName.put(R.id.ultramarine, "Ultramarine");
//        colorIdToName.put(R.id.indigo, "Indigo");
//        colorIdToName.put(R.id.grape_jelly, "GrapeJelly");
//        colorIdToName.put(R.id.mulberry, "Mulberry");
//        colorIdToName.put(R.id.flamingo, "Flamingo");
//        colorIdToName.put(R.id.sexy_salmon, "SexySalmon");
//        colorIdToName.put(R.id.peach, "Peach");
//        colorIdToName.put(R.id.melon, "Melon");
//    }

    /** 테스트 코드 */
    private void generateColorMap() {
        nameToColorMap.put(colorName[0], 0xff1c283f);
        nameToColorMap.put(colorName[1], 0xff9a9ba5);
        nameToColorMap.put(colorName[2], 0xffebebf2);
        nameToColorMap.put(colorName[3], 0xff39477f);
        nameToColorMap.put(colorName[4], 0xff59569e);
        nameToColorMap.put(colorName[5], 0xff9a50a5);
        nameToColorMap.put(colorName[6], 0xffd34ca3);
        nameToColorMap.put(colorName[7], 0xfffe5192);
        nameToColorMap.put(colorName[8], 0xfff77c88);
        nameToColorMap.put(colorName[9], 0xfffc9f95);
        nameToColorMap.put(colorName[10], 0xfffcc397);
        colorIdToName.put(colorID[0], colorName[0]);
        colorIdToName.put(colorID[1], colorName[1]);
        colorIdToName.put(colorID[2], colorName[2]);
        colorIdToName.put(colorID[3], colorName[3]);
        colorIdToName.put(colorID[4], colorName[4]);
        colorIdToName.put(colorID[5], colorName[5]);
        colorIdToName.put(colorID[6], colorName[6]);
        colorIdToName.put(colorID[7], colorName[7]);
        colorIdToName.put(colorID[8], colorName[8]);
        colorIdToName.put(colorID[9], colorName[9]);
        colorIdToName.put(colorID[10], colorName[10]);
    }




    private void addColorMap(String user_id, int i) {

//        // 기존 nameToColorMap 해쉬맵 아이템 삭제, 그러나 색상 int 값은 임시 보관
//        int temp_int = nameToColorMap.remove(colorName[indexForColorName]);

        // 새로운 nameToColorMap 해쉬맵 아이템 추가
//        nameToColorMap.put(colorName[i], colorInt[i]);

//        // 기존 colorIdToName 해쉬맵 아이템 삭제
//        Iterator<Integer> it = colorIdToName.keySet().iterator();
//        while(it.hasNext()) {
//            if(colorIdToName.get(it.next()).equals(colorName[i])) {
//                colorIdToName.remove(it.next());
//                break;
//            }
//        }

//        // 새로운 colorIdToName 해쉬맵 아이템 추가
//        colorIdToName.put(colorID[i], temp_name_2);
//
//
//        // currentColor 변경
//        currentColor = temp_name_2;
//        Log.d(TAG, "currentColor: " + temp_name_2);
//
//        // current indexForColorName 변경
//        this.indexForColorName = i;
//        Log.d(TAG, "indexForColorName: " + i);
    }

    /** id셋팅 */
    public void id_setting(View view) {
//        testRandom_ID = "Guyri";
//        id_setting_btn.setText(testRandom_ID);

//        testRandom_ID = "guyri";
//        currentColor = "Charcoal" + "&" +testRandom_ID;
//        generateColorMap();

    }

    private void wipeCanvas() {
        if(realm != null) {
            realm.executeTransactionAsync(new Realm.Transaction() {
                @Override
                public void execute(Realm r) {
                    r.deleteAll();
                }
            });
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
            realm = null;
        }
    }

    public void delete_btn_clicked(View view) {
        Realm temp_realm = Realm.getDefaultInstance();
        RealmResults<DrawPath> results = temp_realm.where(DrawPath.class).findAll(); // 모든 DrawPath 가져오기?//
        int realmCount = (int)temp_realm.where(DrawPath.class).count();
        Log.d(TAG, "results.size(): "+results.size());
        Log.d(TAG, "temp_realm.where(DrawPath.class).count(): " + temp_realm.where(DrawPath.class).count());

        for(int i=realmCount-1; i>=0; i--) {
//        for(int i=0; i<realmCount; i++) {
            String tempColorName[] = temp_realm.where(DrawPath.class).findAll().get(i).getColor().split("&");
            Log.d(TAG, "tempColorName[1]: " + tempColorName[1]);

            if(tempColorName[1].equals(testRandom_ID)) {
                realm.beginTransaction();
                temp_realm.where(DrawPath.class).findAll().deleteFromRealm(i);
                Log.d(TAG, "Yes, it's deleted!");
                Log.d(TAG, "tempColorName[0]: " + tempColorName[0]);
                Log.d(TAG, "tempColorName[1]: " + tempColorName[1]);
                Log.d(TAG, "tempColorName[2]: " + tempColorName[2]);
                Log.d(TAG, "tempColorName[3]: " + tempColorName[3]);
                realm.commitTransaction();
                break;
            }
        }

        // DrawPath가 0개이면 DrawPoint 정보도 다 같이 지우기
        if((int)temp_realm.where(DrawPath.class).count() == 0) {
            realm.beginTransaction();
            temp_realm.where(DrawPoint.class).findAll().deleteAllFromRealm();
            realm.commitTransaction();
        }

        Log.d(TAG, "results.size(): "+results.size());
        Log.d(TAG, "temp_realm.where(DrawPath.class).count(): " + temp_realm.where(DrawPath.class).count());
        results = null;
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(realm == null) {
            return false; // if we are in the middle of a rotation, realm may be null.
        }

        int[] viewLocation = new int[2];
        surfaceView.getLocationInWindow(viewLocation);
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            float x = event.getRawX();
            float y = event.getRawY();
            double pointX = (x - marginLeft - viewLocation[0]) * ratio;
            double pointY = (y - marginTop - viewLocation[1]) * ratio;

            if (action == MotionEvent.ACTION_DOWN) {
                Log.d(TAG, "ACTION_DOWN");
                realm.beginTransaction();

                currentPath = realm.createObject(DrawPath.class);   // currentPath 객체 생성
                /** 원래 코드 */
//                currentPath.setColor(currentColor);

                /** 시도 코드_1 */
//                String real_color[] = currentColor.split("&");
//                Log.d(TAG, real_color[0]);
//                currentPath.setColor(real_color[0]);
                /** 시도 코드_2 */
                currentPath.setColor(currentColor + "&" + stroke + "&" + alpha);
                Log.d(TAG, currentColor);

                DrawPoint point = realm.createObject(DrawPoint.class);
                point.setX(pointX);
                point.setY(pointY);

                currentPath.getPoints().add(point);
//                Log.d(TAG, "currentPath.getPoints(): " + currentPath.getPoints());  // 테스팅
                realm.commitTransaction();
            }

            else if (action == MotionEvent.ACTION_MOVE) {
                Log.d(TAG, "ACTION_MOVE");
                realm.beginTransaction();
                DrawPoint point = realm.createObject(DrawPoint.class);
                point.setX(pointX);
                point.setY(pointY);
                currentPath.getPoints().add(point);
//                Log.d(TAG, "currentPath.getPoints(): " + currentPath.getPoints());  // 테스팅
                realm.commitTransaction();
            }

            else if (action == MotionEvent.ACTION_UP) {
                Log.d(TAG, "ACTION_UP");
                realm.beginTransaction();
                currentPath.setCompleted(true); // currentPath 객체 마무리(완료)
                DrawPoint point = realm.createObject(DrawPoint.class);
                point.setX(pointX);
                point.setY(pointY);
                currentPath.getPoints().add(point);
//                Log.d(TAG, "currentPath.getPoints(): " + currentPath.getPoints());  // 테스팅
                realm.commitTransaction();
                Log.d(TAG, "DrawPath 크기: " + currentPath.getLength());

                /** Realm에 commit 하고 나서 최종 length 리턴 받기 */
                Realm temp_realm = Realm.getDefaultInstance();
                long results = temp_realm.where(DrawPath.class).count();
                Log.d(TAG, "realm.getPath().length(): " + results);

                /** 내 흔적 담기 */
//                myTrace.add("", (int)results-1);
//                Log.d(TAG, "myTrace.size(): " + myTrace.size());
//                for(int i=0; i<myTrace.size(); i++) {
//                    Log.d(TAG, "myTrace_value: " + myTrace.get(i));
//                }

                currentPath = null; // currentPath 객체 삭제
            }

            else {
                Log.d(TAG, "ELSE");
                realm.beginTransaction();
                currentPath.setCompleted(true);
                Log.d(TAG, "currentPath.getPoints(): " + currentPath.getPoints());  // 테스팅
                realm.commitTransaction();
                currentPath = null;
            }


            /** ========= 테스팅 코드 ========= */
            if(action == MotionEvent.ACTION_UP) {
//                realm.beginTransaction();
//                currentPath = realm.createObject(DrawPath.class);   // currentPath 객체 생성
//                currentPath.setColor(currentColor);
//                DrawPoint point = realm.createObject(DrawPoint.class);
//                currentPath.getPoints().remove(point);
//                Log.d(TAG, "currentPath.getPoints(): " + currentPath.getPoints());  // 테스팅
//                realm.commitTransaction();
//                currentPath = null;
            }
            /** ========= 테스팅 코드 ========= */


            Log.d(TAG, "x: " + x);
            Log.d(TAG, "y: " + y);
            Log.d(TAG, "pointX: " + pointX);
            Log.d(TAG, "pointY: " + pointY);
            Log.d(TAG, "=======================================");
            return true;

        }
        return false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (drawThread == null) {
            Log.d(TAG, "surfaceCreated_ drawThread.start()");
            drawThread = new DrawThread();
            drawThread.start();

            /** surfaceView 백그라운드에 이미지 넣기 */
//            Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.back_3);
//            float scale = (float)background.getHeight()/(float)background.getHeight();
//            int newWidth = Math.round(background.getWidth()/scale);
//            int newHeight = Math.round(background.getHeight()/scale);
//            scaled = Bitmap.createScaledBitmap(background, newWidth, newHeight, true);
//            onDraw();
        }
    }

    /** surfaceView 백그라운드에 이미지 넣기 */
//    public void onDraw() {
////        canvas.drawBitmap(scaled, 0, 0, null); // draw the background
//
//        thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                SurfaceHolder holder = surfaceView.getHolder();
//                Canvas canvas = holder.lockCanvas();
//                canvas.drawBitmap(scaled, 0, 0, null);
//                surfaceView.getHolder().unlockCanvasAndPost(canvas);
//            }
//        });
//        thread.start();
//    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        boolean isPortrait = width < height;
        if (isPortrait) {
            ratio = (double) EDGE_WIDTH / height;
        } else {
            ratio = (double) EDGE_WIDTH / width;
        }
        if (isPortrait) {
            marginLeft = (width - height) / 2.0;
            marginTop = 0;
        } else {
            marginLeft = 0;
            marginTop = (height - width) / 2.0;
        }
        Log.d(TAG, "ratio: " + ratio);
        Log.d(TAG, "marginTop: " + marginTop);
        Log.d(TAG, "marginLeft: " + marginLeft);
        Log.d(TAG, "stroke: " + stroke);
        Log.d(TAG, "alpha: " + alpha);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (drawThread != null) {
            Log.d(TAG, "surfaceDestroyed");
            drawThread.shutdown();
            drawThread = null;
        }
        ratio = -1;
    }

    @Override
    public void onClick(View view) {
        String colorName = colorIdToName.get(view.getId());
        if (colorName == null) {
            return;
        }
        currentColor = colorName;
        Log.d(TAG, "currentColor: " + currentColor);

        if (view instanceof PencilView) {
            currentPencil.setSelected(false);
            currentPencil.invalidate();
            PencilView pencil = (PencilView) view;
            pencil.setSelected(true);
            pencil.invalidate();
            currentPencil = pencil;
        }
    }


    /** 테스트용 클릭이벤트 -- 선 두께 증가 */
//    public void increase_strokeWitdh(View view) {
//        stroke = stroke + 3;
//        if(stroke == 30) {
//            stroke = 3;
//        }
//        Log.d(TAG, "stroke: " + stroke);
//    }

    /** 테스트용 클릭이벤트 -- 선 투명도 감소 */
//    public void decrease_strokeAlpha(View view) {
//        alpha = alpha - 15;
//        if(alpha < 20) {
//            alpha = 255;
//        }
//        Log.d(TAG, "alpha: " + alpha);
//    }


    class DrawThread extends Thread {

        private Realm bgRealm;

        public void shutdown() {
            synchronized(this) {
                if (bgRealm != null) {
                    Log.d(TAG, "DrawThread_ shutdown()");
                    bgRealm.stopWaitForChange();
                }
            }
            interrupt();
        }

        @Override
        public void run() {
            while (ratio < 0 && !isInterrupted()) {
            }

            if (isInterrupted()) {
                return;
            }

//            Canvas canvas = null;

            try {
                final SurfaceHolder holder = surfaceView.getHolder();
                canvas = holder.lockCanvas();
                canvas.drawColor(Color.WHITE); // 시작 시, 캔버스 색 변경?
//                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                canvas.drawBitmap(scaled, 0, 0, null);
            } finally {
                if (canvas != null) {
                    surfaceView.getHolder().unlockCanvasAndPost(canvas);
                }
            }

            while (realm == null && !isInterrupted()) {
            }

            if (isInterrupted()) {
                return;
            }

            bgRealm = Realm.getDefaultInstance();
            final RealmResults<DrawPath> results = bgRealm.where(DrawPath.class).findAll(); // 모든 DrawPath 가져오기?
            while (!isInterrupted()) {
                try {
                    final SurfaceHolder holder = surfaceView.getHolder();
                    canvas = holder.lockCanvas();

                    // 싱크로나이즈!
                    synchronized (holder) {
                        if(canvas != null) {
                            /** 원래 코드 */
//                            canvas.drawColor(Color.parseColor("#00ff0000"));

                            /** 테스트 코드 - 캔버스 투명하게 */
                            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

                            /** 테스트 코드 - 캔버스에 이미지 넣기_1 */
//                            canvas.drawBitmap(scaled, 0, 0, null);

                            /** 테스트 코드 - 캔버스에 이미지 넣기_2 */
                            // 1
//                            int w = myBitmap01.getWidth();
//                            int h = myBitmap01.getHeight();
//                            //Rect src = new Rect(0, 0, w, h);
//                            Rect dst = new Rect(400, 800, 400 + w / 2, 800 + h / 2);
//                            canvas.drawBitmap(myBitmap01, null, dst, null);

                            // 2
//                            canvas.drawBitmap(myBitmap01, 0, 0, null);

                            // 3
//                            canvas.drawBitmap(myBitmap01, 150, 300, null);


                            final Paint paint = new Paint();

                            for (DrawPath drawPath : results) {
                                Log.d(TAG, "drawPath.getColor(): " + drawPath.getColor());
                                final RealmList<DrawPoint> points = drawPath.getPoints();   // 해당 drawPath의 point 가져오기
                                
                                /**
                                 * index 0 - color
                                 * index 1 - user_id
                                 * index 2 - strokeWidth
                                 * index 3 - strokeAlpha
                                 * */
                                String color_split[] = drawPath.getColor().split("&");
                                String color_modified = color_split[0] + "&" + testRandom_ID;
                                Log.d(TAG, "color_split[0]: " + color_split[0]);
                                Log.d(TAG, "color_split[1]: " + color_split[1]);
                                Log.d(TAG, "color_split[2]: " + color_split[2]);
                                Log.d(TAG, "color_split[3]: " + color_split[3]);
                                Log.d(TAG, "color_modified: " + color_modified);

                                final Integer color = nameToColorMap.get(color_modified);// 해당 drawPath의 color 가져오기
                                Log.d(TAG, "color: " + color);

                                if (color != null) {
                                    // 색상 정보 찾아와서 적용
                                    paint.setColor(color);
                                } else {
                                    // 색상 정보 못찾을 시에 디폴트색 적용
                                    paint.setColor(nameToColorMap.get(currentColor));
                                }

                                // 선 종류
                                paint.setStyle(Paint.Style.STROKE);
                                // 선 두께
                                paint.setStrokeWidth((float) (Integer.parseInt(color_split[2])/ ratio));
                                // 선 투명도
                                paint.setAlpha(Integer.parseInt(color_split[3]));

                                final Iterator<DrawPoint> iterator = points.iterator();
                                final DrawPoint firstPoint = iterator.next();
                                final Path path = new Path();
                                final float firstX = (float) ((firstPoint.getX() / ratio) + marginLeft);
                                final float firstY = (float) ((firstPoint.getY() / ratio) + marginTop);
                                path.moveTo(firstX, firstY);

                                while(iterator.hasNext()) {
                                    DrawPoint point = iterator.next();
                                    final float x = (float) ((point.getX() / ratio) + marginLeft);
                                    final float y = (float) ((point.getY() / ratio) + marginTop);
                                    path.lineTo(x, y);
                                }
                                canvas.drawPath(path, paint);
                            }
                        }
                    }
                } finally {
                    if (canvas != null) {
//                        handler.sendEmptyMessage(0);
                        surfaceView.getHolder().unlockCanvasAndPost(canvas);
                    }
                }
                bgRealm.waitForChange();
            }

            synchronized(this) {
                bgRealm.close();
            }
        }
    }

//    public class CanvasToBitmap extends View {
//
//        Paint paint = new Paint();
//        Rect mRect = new Rect();
//        Bitmap bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
//
//        public CanvasToBitmap(Context context ) {
//            super(context);
//            Canvas canvas = new Canvas(bitmap);
//            draw(canvas);
//        }
//
//        @Override
//        public void onDraw(Canvas canvas) {
//
//            mRect.set(0, 0, 200, 200);
//            paint.setColor(Color.GREEN);
//            paint.setStyle(Paint.Style.FILL);
//            canvas.drawRect(mRect, paint);
//            canvas.setBitmap(bitmap);
//
//            ByteArrayOutputStream mByteArrayOutputStream = new ByteArrayOutputStream();
//            try{
//                bitmap.compress(Bitmap.CompressFormat.PNG, 100,    mByteArrayOutputStream);
//
//                bitmap = BitmapFactory.decodeStream(new ByteArrayInputStream(mByteArrayOutputStream.toByteArray()));
//                mByteArrayOutputStream.flush();
//                mByteArrayOutputStream.close();
//            } catch (Exception e) {e.printStackTrace();}
//        }
//    }

    public int getWidth() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;// 가로
        return width;
    }

    public int getHeight() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int height = displayMetrics.heightPixels;// 세로
        return height;
    }
}
