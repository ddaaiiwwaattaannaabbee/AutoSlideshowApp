package jp.techacademy.watanabe.dai.autoslideshowapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private int fieldIndex;
    private Timer mTimer;
    private Handler mHandler = new Handler();

    private boolean hasPush;

    private Button back;
    private Button auto;
    private Button next;

    Cursor mCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        back = findViewById(R.id.back);
        back.setOnClickListener(this);

        auto = findViewById(R.id.auto);
        auto.setOnClickListener(this);

        next = findViewById(R.id.next);
        next.setOnClickListener(this);

        hasPush = true;

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getCursor();
                getContentsInfo();
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getCursor();
            getContentsInfo();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getCursor();
                    getContentsInfo();
                }else{
                    Toast toast = Toast.makeText(MainActivity.this, "権限がないため操作できません", Toast.LENGTH_LONG);
                    toast.show();
                    canNotButton();
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo() {

        if (mCursor.moveToFirst()) {
            showImage();
        }

    }

    private void getCursor() {
        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        mCursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );
    }

    private void showImage() {
        int fieldIndex = mCursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = mCursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }

    private void getContentsNext() {

        if (mCursor.moveToNext()) {
            showImage();
        } else {
            mCursor.moveToFirst();
            showImage();
        }
    }

    private void getContentsBack() {

        if (mCursor.moveToPrevious()) {
            showImage();
        } else {
            mCursor.moveToLast();
            showImage();
        }

    }

    private void changeButton() {

        hasPush = !hasPush;
        back.setEnabled(hasPush);
        next.setEnabled(hasPush);

        if (hasPush) {
            auto.setText("再生");
        } else {
            auto.setText("停止");
        }
    }

    private void canNotButton() {

        back.setEnabled(false);
        auto.setEnabled(false);
        next.setEnabled(false);

    }

    private void autoRun() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        } else {
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {

                    mHandler.post(new Runnable() {

                        @Override
                        public void run() {
                            getContentsNext();
                        }
                    });
                }
            }, 2000, 2000);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // 戻る
            case R.id.back:
                Log.d("button", "戻る");
                getContentsBack();
                Log.d("fieldIndex", String.valueOf(this.fieldIndex));
                break;

            // 再生/停止
            case R.id.auto:
                Log.d("button", "再生/停止");
                changeButton();
                autoRun();
                break;

            // 進む
            case R.id.next:
                Log.d("button", "進む");
                getContentsNext();
                Log.d("fieldIndex", String.valueOf(this.fieldIndex));
                break;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        mCursor.close();
        Log.v("LifeCycle", "onDestroy");
    }
}