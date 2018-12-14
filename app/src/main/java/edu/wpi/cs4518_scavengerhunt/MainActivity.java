package edu.wpi.cs4518_scavengerhunt;

import android.content.Intent;
import android.content.SharedPreferences;
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

public class MainActivity extends AppCompatActivity {

    protected static int REQUEST_IMAGE_CAPTURE = 1;
    MLOnDeviceHelper onDeviceHelper;
    MLOffDeviceHelper offDeviceHelper;
    String imagePath;

    public String currentInference = "Nothing Yet";
    private HuntHelper helper;
    private int score, skips;
    SharedPreferences sharedPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        offDeviceHelper = new MLOffDeviceHelper(this);
        Log.d("Random Item:", offDeviceHelper.labels[(int) (Math.random() * 1000)]);
        //takePictureAndStartInference();

        sharedPref = getPreferences(this.MODE_PRIVATE);
        sharedPref = this.getPreferences(this.MODE_PRIVATE);
        sharedPref.getString("key", "");
        score = sharedPref.getInt("score", 0);
        skips = sharedPref.getInt("skips", 0);

        helper = new HuntHelper(this);
        helper.huntList();


        TextView scoreText = findViewById(R.id.scoreText);
        scoreText.setText("SCORE: " + score);
        TextView skipText = findViewById(R.id.skipsText);
        skipText.setText("Skips: " + score);

        /*
        TextView curHunt = findViewById(R.id.curItem);
        curItem = "bookcase";
        curHunt.setText("Current Item:\n" + curItem);
        */
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_IMAGE_CAPTURE)
            return;
        try {
            onDeviceHelper = new MLOnDeviceHelper(this, getAssets().openFd(MLHelper.getModelPath()));
            onDeviceHelper.runImageClassification(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeFile(imagePath),
                    onDeviceHelper.SIZE_X,
                    onDeviceHelper.SIZE_Y,
                    true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This function is called by the inference code and will come back with the answer to the
     * inference after a second or so delay because inference takes a long time
     *
     * @param answer The string name for the identified object.
     */
    public void comeBackWithInferenceAnswer(String answer) {
        Log.d("Model answer", answer);
        //TextView picOf = findViewById(R.id.textPicOf);
        //picOf.setText("Pic seen as:\n" + answer);
        if (answer.equals(curItem)) {
            score += 100;
            skips += 1;
            nextItem();
        } else {
            score -= 25;
            updateScore();
            setWrongAnswer(answer);
        }
    }

    private String curItem;

    public void setWrongAnswer(final String actualAn) {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper()); // Gives us a handle for running things in the main thread
        Runnable updateScoreRunnable = new Runnable() { // Make the task we need to run
            @Override
            public void run() {
                TextView wrongText = findViewById(R.id.wrongAnsText);
                if (!actualAn.equals("")) {
                    wrongText.setText("That was a " + actualAn + "!\n -10 penalty");
                    wrongText.setVisibility(View.VISIBLE);
                } else {
                    wrongText.setText("");
                    wrongText.setVisibility(View.INVISIBLE);
                }
            }
        };
        mainHandler.post(updateScoreRunnable); // Post the task to the main thread for execution
    }


    public void updateScore() {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper()); // Gives us a handle for running things in the main thread
        Runnable updateScoreRunnable = new Runnable() { // Make the task we need to run
            @Override
            public void run() {
                TextView scoreText = findViewById(R.id.scoreText);
                scoreText.setText("SCORE: " + score);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("score", score);
                editor.commit();

            }
        };
        mainHandler.post(updateScoreRunnable); // Post the task to the main thread for execution
    }

    public void updateSkips() {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper()); // Gives us a handle for running things in the main thread
        Runnable updateScoreRunnable = new Runnable() { // Make the task we need to run
            @Override
            public void run() {
                TextView skipsText = findViewById(R.id.skipsText);
                skipsText.setText("Skips: " + skips);

                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("skips", skips);
                editor.commit();
            }
        };
        mainHandler.post(updateScoreRunnable); // Post the task to the main thread for execution
    }


    public void updateItem() {
        Handler mainHandler = new Handler(getApplicationContext().getMainLooper()); // Gives us a handle for running things in the main thread
        Runnable updateScoreRunnable = new Runnable() { // Make the task we need to run
            @Override
            public void run() {
                TextView curHunt = findViewById(R.id.curItem);
                curHunt.setText("Current Item:\n" + curItem);
            }
        };
        mainHandler.post(updateScoreRunnable); // Post the task to the main thread for execution
    }

    public void nextItem(View v) {
        skips -= 1;
        if (skips < 0)
            score += 10 * skips; //actually subtracting
        nextItem();
    }

    public void nextItem() {
        curItem = helper.newHuntItem();
        setWrongAnswer("");
        updateScore();
        updateSkips();
        updateItem();

    }

    public void addToImpossible(View v) {
        helper.addToImp(curItem);
        nextItem();

    }

}
