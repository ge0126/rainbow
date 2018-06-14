package com.example.gaeun.rainbow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class removeAPI {
    String removeURL="http://rainbowpic.tk:3000/api/removeall/";
    public void removeall(String file_name){
        ok_http_remove_thread thread = new ok_http_remove_thread(removeURL+file_name);
        thread.start();
    }

    private class ok_http_remove_thread extends Thread {

        private String TAG = "okhttp_removeall_Thread";
        private String Thread_URL;


        public ok_http_remove_thread(String URL) {
            Thread_URL = URL;
            Log.i(TAG, "URL init!");
        }

        public void run() {
            try {
                okhttp3.Request request = new okhttp3.Request.Builder()
                        .url(Thread_URL)
                        .build();

                OkHttpClient client = new OkHttpClient();

                Log.i(TAG, "request execute.......");
                client.newCall(request).enqueue(new Callback() {
                    public void onFailure(Call call, IOException e) {
                        Log.d("error", "Connect server Error is " + e.toString());
                    }

                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful())
                            throw new IOException("Unexpected code " + response);
                        Log.d(TAG,"response success~");

                    }
                });

                Log.i(TAG, "client call... execute........");

            } catch (Exception e) {
                Log.i(TAG, "Unknown Exception!");
            }
        }
    }
}
