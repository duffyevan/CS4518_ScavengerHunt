package edu.wpi.cs4518_scavengerhunt;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MLOffDeviceHelper offDeviceHelper = new MLOffDeviceHelper(this);
        Log.d("Random Item:" , offDeviceHelper.labels[(int)(Math.random()*1000)]);
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

}
