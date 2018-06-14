package com.example.gaeun.rainbow;

import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Rainbow_start extends AppCompatActivity {
    private final int GALLERY_CODE=1112;
    public String imageFilePath;
    public Bitmap resultb;
    public String sub_file_name;
    final Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);
        selectGallery();
    }


    private void selectGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==GALLERY_CODE && resultCode == RESULT_OK){
            try{
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());

                Uri uri = data.getData();
                imageFilePath=getRealPathFromURI(uri);
                ExifInterface exif = null;

                //exif : exchangeable image file format 사진정보. -> 사진의 위도, 경도, 시간, 방향

                try{
                    exif = new ExifInterface(imageFilePath); // 이미지의 정보를 생성.
                }catch(IOException e){
                    e.printStackTrace();
                }

                int exifOrientation;
                int exifDegree;

                //이미지가 있을 때(exif != null)
                if(exif!=null){
                    exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL);
                    exifDegree = exifOrientationToDegrees(exifOrientation);
                } else{
                    exifDegree=0;
                }

                ((ImageView)findViewById(R.id.gellary_image)).setImageBitmap(rotate(bitmap, exifDegree));
            }catch(IOException e){
                Log.e("Start menu","gellary bitmap load error");
            }
        }

    }

    //회전해야 할 각도 구하기
    private int exifOrientationToDegrees(int exifOrientation){
        if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_90){
            return 90;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_180){
            return 180;
        } else if(exifOrientation == ExifInterface.ORIENTATION_ROTATE_270){
            return 270;
        }
        return 0;
    }

    //회전
    private Bitmap rotate(Bitmap bitmap, float degree){
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap,0, 0, bitmap.getWidth(),bitmap.getHeight(),matrix,true);

    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };

        CursorLoader cursorLoader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public void ck_cancle(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    public void ck_cancle2(View v){
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
    public void ck_convert(View v){
        String TAG="ck_convert";

        Log.d(TAG,"로딩액티비티");
        setContentView(R.layout.activity_load);
        ImageView loading_gif=findViewById(R.id.load);
        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(loading_gif);
        Glide.with(this).load(R.drawable.loading_2).into(gifImage);

        Log.d("start",imageFilePath);

        API api= new API();
        resultb=api.upload_jsonres(imageFilePath);
        sub_file_name=api.get_file_name();

        removeAPI remove = new removeAPI();
        remove.removeall(sub_file_name);

        setContentView(R.layout.activity_save);
        ((ImageView)findViewById(R.id.result)).setImageBitmap(resultb);

    }

    public void ck_save(View v){

        String TAG="ck_save";
        String directory;
        directory= Environment.getExternalStorageDirectory().getAbsolutePath()+"/DCIM/Rainbow_Result";
        Log.d(TAG,directory);

        File result = new File(directory);
        if(!result.isDirectory()){
            result.mkdirs();
            Log.d(TAG,"폴더가 생성되었습니다.");
        }
        else
            Log.d(TAG,"이미 폴더가 존재합니다");

        SimpleDateFormat day = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        result = new File(directory+"/img"+day.format(date)+".jpg");
        Log.d(TAG,"파일생성");

        try{
            result.createNewFile();  // 파일을 생성해주고
            FileOutputStream out = new FileOutputStream(result);
            resultb.compress(Bitmap.CompressFormat.JPEG, 90 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌
            out.close(); // 마무리로 닫아줍니다.
            Log.d(TAG,"파일저장완료");

        } catch (FileNotFoundException e) {
            Log.e(TAG,"FileNotFoundException~");
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(TAG,"IOException~");
            e.printStackTrace();
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("확인");
        alertDialogBuilder.setMessage("파일이 저장되었습니다.")
                .setCancelable(false)
                .setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(context, MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


}
