package com.example.gaeun.rainbow;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;

import java.io.File;
import java.io.IOException;
import java.util.Date;

//resultImage 사용하면 됨.

public class Rainbow_camera extends AppCompatActivity {

    private String imageFilePath;
    private Uri photoUri;
    private ImageView resultImage;
    public Bitmap resultb;
    public String sub_file_name;


    static final int REQUEST_IMAGE_CAPTURE = 1111;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convert);
        sendTakePhotoIntent();
    }

    //카메라로 사진 찍어 이미지 띄우기
    private void sendTakePhotoIntent(){
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(takePictureIntent.resolveActivity(getPackageManager())!= null){
            File photoFile = null;
            try{
                photoFile = createImageFile(); //파일 리턴받음
            } catch (IOException e){
            }

            if(photoFile != null){ //파일이 있으면!
                photoUri = FileProvider.getUriForFile(this, getPackageName(), photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
            Bitmap bitmap = BitmapFactory.decodeFile(imageFilePath); //비트맵 생성
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

            //회전한 이미지를 셋
            ((ImageView)findViewById(R.id.gellary_image)).setImageBitmap(rotate(bitmap, exifDegree)); //비트맵 사진, 회전할 각
            resultImage = findViewById(R.id.gellary_image); //resultImage에 회전된 사진이 저장.
        }
    }

    //찍은 이미지를 jpg 파일로 생성
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "TEST_"+timeStamp+"_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        System.out.println("storageDir의 경로 : "+storageDir.getAbsolutePath());
        //외부 저장소의 최상위 경로!! 를 반환

        File image = File.createTempFile(
                imageFileName, //yyyyMMdd_HHmmss식으로 이름이 저장
                ".jpg", //jpg형으로 이미지 저장
                storageDir //저장되는 경로
        );
        imageFilePath = image.getAbsolutePath(); //이미지 경로를 전역변수에 저장
        return image; //파일 리턴
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

    //save 버튼을 눌렀을 때
    public void ck_convert(View v){
        String TAG="ck_convert";
        Log.d("start",imageFilePath);

        Log.d(TAG,"로딩액티비티");

        setContentView(R.layout.activity_load);
        ImageView loading_gif=findViewById(R.id.load);
        GlideDrawableImageViewTarget gifImage = new GlideDrawableImageViewTarget(loading_gif);
        Glide.with(this).load(R.drawable.loading_2).into(gifImage);

        API api= new API();
        resultb=api.upload_jsonres(imageFilePath);

        sub_file_name=api.get_file_name();

        removeAPI remove = new removeAPI();
        remove.removeall(sub_file_name);

        setContentView(R.layout.activity_save);
        ((ImageView)findViewById(R.id.result)).setImageBitmap(resultb);

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

    public void ck_save(View v){
        final Context context = this;
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
