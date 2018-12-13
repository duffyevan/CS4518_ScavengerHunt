package edu.wpi.cs4518_scavengerhunt;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    protected static int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MLOffDeviceHelper offDeviceHelper = new MLOffDeviceHelper(this);
        Log.d("Random Item:", offDeviceHelper.labels[(int) (Math.random() * 1000)]);
        String path = takePicture();
        Log.d("Path", path);
    }


    public InputStream getLabelFileInputStream() throws IOException {
        return getAssets().open("model/labels.txt");
    }


    // Some Help From StackOverflow: https://stackoverflow.com/questions/41340422/how-to-create-file-object-from-assets-folder/41340513
    public File cacheImageFileFromAssets(String filename) throws IOException {

        File f = new File(getCacheDir() + "/" + filename);

        InputStream is = getAssets().open("cat_images/" + filename);
        byte[] buffer = new byte[1024];

        FileOutputStream fos = new FileOutputStream(f);
        while (is.read(buffer) != -1)
            fos.write(buffer);

        is.close();
        fos.close();

        return f;

    }

    /**
     * Function to take picture with the device camera
     *
     * @return The absolute path to the cached image taken
     */
    public String takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File picturesFolder = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File tempFile;
        try {
            tempFile = File.createTempFile(String.valueOf(R.string.app_name).replace(' ', '_'), ".jpg", picturesFolder);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        Uri fileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", tempFile);

        String absoluteImagePath = tempFile.getAbsolutePath();

        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        return absoluteImagePath;
    }

}
