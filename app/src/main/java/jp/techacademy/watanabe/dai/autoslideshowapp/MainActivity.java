package jp.techacademy.watanabe.dai.autoslideshowapp;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSIONS_REQUEST_CODE = 100;

    private int fieldIndex;
    private Timer mTimer;
    private Handler mHandler = new Handler();

    private boolean hasPush;

    private Button back;
    private Button auto;
    private Button next;

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

        fieldIndex = getFieldIndex();

        hasPush = true;

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo(fieldIndex);
            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo(fieldIndex);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo(fieldIndex);
                }
                break;
            default:
                break;
        }
    }

    private void getContentsInfo(int fieldIndex) {

        Cursor cursor = getCursor();

        if (cursor.moveToFirst()) {
            showImage(cursor);
        }

        cursor.close();
    }

    private int getFieldIndex() {
        return fieldIndex = getCursor().getColumnIndex(MediaStore.Images.Media._ID);
    }

    private Cursor getCursor() {
        // 画像の情報を取得する
        ContentResolver resolver = getContentResolver();
        return resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
                null, // 項目(null = 全項目)
                null, // フィルタ条件(null = フィルタなし)
                null, // フィルタ用パラメータ
                null // ソート (null ソートなし)
        );
    }

    private void showImage(Cursor cursor) {
        int fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
        Long id = cursor.getLong(fieldIndex);
        Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

        ImageView imageVIew = (ImageView) findViewById(R.id.imageView);
        imageVIew.setImageURI(imageUri);
    }

    private void getContentsNext() {

        Cursor cursor = getCursor();
        cursor.moveToPosition(this.fieldIndex);

        if (cursor.moveToNext()) {
            showImage(cursor);
        } else {
            cursor.moveToFirst();
            showImage(cursor);
        }

        this.fieldIndex = cursor.getPosition();
        cursor.close();
    }

    private void getContentsBack() {

        Cursor cursor = getCursor();
        cursor.moveToPosition(this.fieldIndex);

        if (cursor.moveToPrevious()) {
            showImage(cursor);
        } else {
            cursor.moveToLast();
            showImage(cursor);
        }

        this.fieldIndex = cursor.getPosition();
        cursor.close();
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
}