package edu.wpi.cs4518_scavengerhunt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import edu.wpi.cs4518_scavengerhunt.exceptions.TypeError;

public class MainActivity extends AppCompatActivity {

    protected static int REQUEST_IMAGE_CAPTURE = 1;
    MLOnDeviceHelper onDeviceHelper;
    MLOffDeviceHelper offDeviceHelper;
    String imagePath;

    public String currentInference = "Nothing Yet";
    private HuntHelper helper;
    private int score = 0, skips = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        offDeviceHelper = new MLOffDeviceHelper(this);
        try {
            onDeviceHelper = new MLOnDeviceHelper(this, getAssets().openFd(MLHelper.getModelPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("Random Item:", offDeviceHelper.labels[(int) (Math.random() * 1000)]);
        //takePictureAndStartInference();
        helper = new HuntHelper(this);
        helper.huntList();

        nextItem();
    }


    public InputStream getLabelFileInputStream() throws IOException {
        return getAssets().open("model/labels.txt");
    }


    /**
     * Function to take picture with the device camera, the camera returning will kick off the inference
     */
    public void takePictureAndStartInference(View v) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File picturesFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File tempFile;
        try {
            tempFile = File.createTempFile(String.valueOf(R.string.app_name).replace(' ', '_'), ".jpg", picturesFolder);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Uri fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", tempFile);

        String absoluteImagePath = tempFile.getAbsolutePath();

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        this.imagePath = absoluteImagePath;
    }

    private boolean isOnDevice(){
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_IMAGE_CAPTURE)
            return;
        try {
            if (isOnDevice()) {
                onDeviceHelper.runImageClassification(Bitmap.createScaledBitmap(
                        BitmapFactory.decodeFile(imagePath),
                        onDeviceHelper.SIZE_X,
                        onDeviceHelper.SIZE_Y,
                        true));
            } else {
                offDeviceHelper.runImageClassification("image.jpg", new File(imagePath));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TypeError typeError) {
            typeError.printStackTrace();
        }
    }

    /**
     * This function is called by the inference code and will come back with the answer to the
     * inference after a second or so delay because inference takes a long time
     *
     * @param answer The string name for the identified object.
     */
    public void comeBackWithInferenceAnswer(String answer) {
        Log.d("Here Be The Answer!", answer);
        //TextView picOf = findViewById(R.id.textPicOf);
        //picOf.setText("Pic seen as:\n" + answer);
        if (answer.equals(curItem)){
            score += 100;
            nextItem();
            }
        else {
            score -= 50;
            updateScore();
            nextItem();
         }
    }

    private String curItem;

    public void updateScore(){
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper()); // Gives us a handle for running things in the main thread
        Runnable updateScoreRunnable = new Runnable() { // Make the task we need to run
            @Override
            public void run() {
                TextView scoreText = findViewById(R.id.scoreText);
                scoreText.setText("SCORE:" + score);

            }
        };
        mainHandler.post(updateScoreRunnable); // Post the task to the main thread for execution

    }
    public void nextItem(View v){
        nextItem();
    }

    public void nextItem(){
        updateScore();
        TextView curHunt = findViewById(R.id.curItem);
        curItem = helper.newHuntItem();
        curHunt.setText("Current Item:\n" + curItem);

    }

    public void addToImpossible(View v){
        helper.addToImp(curItem);
        nextItem();

    }

}
