package com.example.gaeun.rainbow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;


/*
.setConnectTime(10000) - 해당 웹 페이지 연결시간을 10초로 설정
.setRequestMethod("POST") - Get/Post 방식 설정
.setDoOutput(true) - outputStream으로 데이터를 넘겨주겠다고 설정
.setDoInput(true) - InputStream으로 데이터를 읽겠다고 설정
.getResponseCode() - 연결 상태 확인
*/

public class API {
    public String file_name_json; //확장자 포함 파일네임
    public String sub_file_name; //확장자 미포함 파일네임
    public File result;

    public String get_file_name(){
        return sub_file_name;
    }

    public Bitmap upload_jsonres(String file_path){

        String TAG="upload_jsonres";
        String postURL="http://rainbowpic.tk:3000/upload/api/jsonres";
        String getURL="http://rainbowpic.tk:3000/api/get/after/";
        ok_http_POST_Thread thread = new ok_http_POST_Thread(postURL, file_path);

        thread.start();
        try {
            thread.join();
            //response가 너무 늦게와서 스레드를 재움...
            thread.sleep(15000);
            Log.d("thread join","finish!~~~~~~~~~~~~~~~~~~~~");
        }
        catch(Exception e){
            Log.e(TAG,"exception TT");
        }
        int idx = file_name_json.indexOf(".");
        sub_file_name=file_name_json.substring(0,idx);
        Log.d(TAG,sub_file_name);

        ok_http_GET_thread thread2 = new ok_http_GET_thread(getURL+sub_file_name);
        thread2.start();
        try{
            thread2.join();
            thread2.sleep(15000);
            Log.d("thread join","finish~!~!~!~!~!");
        }
        catch(Exception e){
            Log.e(TAG,"exception TT");
        }
        return thread2.getBitmap();

    }

    private class ok_http_POST_Thread extends Thread{
        private String TAG="okhttp_post_Thread";
        private String Thread_URL;
        private String Thread_path;


        public ok_http_POST_Thread(String URL, String file_path){
            Thread_URL=URL;
            Thread_path=file_path;

            Log.i(TAG,"URL, file_path value init!");
        }

        public void run(){
            try{
                File image= new File(Thread_path);
                Log.i(TAG,"file maked");
                RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                        .addFormDataPart("Inputimg", Thread_path, RequestBody.create(MediaType.parse("image/jpg"), image))
                        .build();
                Log.i(TAG,"requestBody execute...");
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(Thread_URL)
                        .post(requestBody)
                        .build();
                OkHttpClient client = new OkHttpClient.Builder()
                        .retryOnConnectionFailure(true)
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30,TimeUnit.SECONDS)
                        .writeTimeout(30,TimeUnit.SECONDS)
                        .build();

                Log.i(TAG,"request execute.......");
                client.newCall(request).enqueue(new Callback(){
                    public void onFailure(Call call, IOException e){
                        Log.d("error", "Connect server Error is "+e.toString());
                    }
                    public void onResponse(Call call, Response response) throws IOException{


                        //Log.d("conection_sucess", "Response Body is "+ response.body().string());
                        //response는 한번 받으면 closed 됨.
                        try{
                            JSONObject json= new JSONObject(response.body().string());
                            file_name_json=json.getString("filename");
                            Log.d("~~~~file_name_json~~~",file_name_json);
                        } catch(JSONException e){
                            Log.e(TAG,"JSONException");
                            e.printStackTrace();
                        }
                        //result=aa.get_after();
                    }
                });

                Log.i(TAG,"client call... execute........");

            }catch(Exception e){
                Log.i(TAG,"Unknown Exception!");
            }

        }

    }

    private class ok_http_GET_thread extends Thread{

        private String TAG="okhttp_get_Thread";
        private String Thread_URL;
        public Bitmap result_bitmap;


        public ok_http_GET_thread(String URL){
            Thread_URL=URL;



            Log.i(TAG,"URL init!");
        }

        public void run(){
            try{
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(Thread_URL)
                        .build();

                //OkHttpClient client = new OkHttpClient.Builder()
                //        .retryOnConnectionFailure(true)
                //        .connectTimeout(30, TimeUnit.SECONDS)
                //        .readTimeout(30,TimeUnit.SECONDS)
                //        .writeTimeout(30,TimeUnit.SECONDS)
                //        .build();
                OkHttpClient client = new OkHttpClient();

                Log.i(TAG,"request execute.......");
                client.newCall(request).enqueue(new Callback(){
                    public void onFailure(Call call, IOException e){
                        Log.d("error", "Connect server Error is "+e.toString());
                    }
                    public void onResponse(Call call, Response response) throws IOException{
                        if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                        InputStream in = response.body().byteStream();
                        result_bitmap = BitmapFactory.decodeStream(in);
                        Log.d(TAG,"bitmapFactory sucess~");


                    }
                });

                Log.i(TAG,"client call... execute........");

            }catch(Exception e){
                Log.i(TAG,"Unknown Exception!");
            }
        }

        public Bitmap getBitmap(){
            return result_bitmap;
        }

    }


}

