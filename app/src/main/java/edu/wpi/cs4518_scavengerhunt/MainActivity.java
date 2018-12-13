package edu.wpi.cs4518_scavengerhunt;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    protected static int REQUEST_IMAGE_CAPTURE = 1;
    MLOnDeviceHelper onDeviceHelper;
    MLOffDeviceHelper offDeviceHelper;
    String imagePath;

    public String currentInference = "Nothing Yet";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        offDeviceHelper = new MLOffDeviceHelper(this);
        Log.d("Random Item:", offDeviceHelper.labels[(int) (Math.random() * 1000)]);
        takePictureAndStartInference();
    }


    public InputStream getLabelFileInputStream() throws IOException {
        return getAssets().open("model/labels.txt");
    }


    /**
     * Function to take picture with the device camera, the camera returning will kick off the inference
     */
    public void takePictureAndStartInference() {
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
        // TODO Yo Ben Do Your Thing Here! :)
        Log.d("Here Be The Answer!", answer);
    }


}
