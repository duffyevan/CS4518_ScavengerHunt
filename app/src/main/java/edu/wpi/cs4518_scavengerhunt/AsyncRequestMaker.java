package edu.wpi.cs4518_scavengerhunt;

import android.os.AsyncTask;
import android.os.SystemClock;

import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.MultipartBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.IOException;

public class AsyncRequestMaker extends AsyncTask<String, Float, String> {
    private final String hosturl;
    private OkHttpClient client;
    private File file;
    private String filename;
    private MainActivity mainActivity;
    private long startTime,endTime;

    public AsyncRequestMaker(String hosturl, String filename, File file, MainActivity mainActivity){
        this.hosturl = hosturl;
        this.client = new OkHttpClient();
        this.file = file;
        this.filename = filename;
        this.mainActivity = mainActivity;
    }

    @Override
    protected String doInBackground(String... image_files) {
        startTime = SystemClock.uptimeMillis();
        Response r = null;
        try {
            r = makeRequest(filename, file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        endTime = SystemClock.uptimeMillis();
        String result = null;
        try {
            result = r.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final String handoffResult = result;
        mainActivity.currentInference = handoffResult;
        mainActivity.comeBackWithInferenceAnswer(handoffResult);
        return result;
    }

    private Response makeRequest(String filename, File file) throws IOException {
        RequestBody requestBody = new MultipartBuilder()
                .type(MultipartBuilder.FORM)
                .addFormDataPart("file", filename, RequestBody.create(MediaType.parse("image/jpg"), file))
                .build();

        Request request = new Request.Builder()
                .url(hosturl)
                .post(requestBody)
                .build();

        return client.newCall(request).execute();
    }

    long getTotalExecTime(){
        return endTime-startTime;
    }
}
