package edu.wpi.cs4518_scavengerhunt;

import android.graphics.Bitmap;

import java.io.File;
import java.io.IOException;

import edu.wpi.cs4518_scavengerhunt.exceptions.TypeError;

public class MLOffDeviceHelper extends MLHelper {
    private String hosturl = "http://35.243.243.163:54321/inception";
    private AsyncRequestMaker requestMaker;

    public MLOffDeviceHelper(MainActivity mainActivity) {
        super(mainActivity);

    }

    @Override
    public void runImageClassification(Bitmap image) throws TypeError {
        throw new TypeError("This Version Of This Method Is Not Supported For Off Device Processing");
    }

    @Override
    public void runImageClassification(String filename, File file) throws IOException, TypeError {
        this.requestMaker = new AsyncRequestMaker(hosturl, filename, file, mainActivity);
        this.requestMaker.execute();

    }


}
