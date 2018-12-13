package edu.wpi.cs4518_scavengerhunt;

import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.SystemClock;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import edu.wpi.cs4518_scavengerhunt.exceptions.TypeError;

public class MLOnDeviceHelper extends MLHelper {


    public MLOnDeviceHelper(MainActivity mainActivity, AssetFileDescriptor modelFile) {
        super(mainActivity);
        MappedByteBuffer tfliteModel = null;
        try {
            tfliteModel = loadModelFile(modelFile);
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert tfliteModel != null;
        tflite = new Interpreter(tfliteModel);

    }

    public void runImageClassification(final Bitmap image) throws IOException {
        Runnable runClassification = new Runnable() {
            @Override
            public void run() {
                final long startTime = SystemClock.uptimeMillis();

                imgData = ByteBuffer.allocateDirect(
                        SIZE_X
                                * SIZE_Y
                                * 3
                                * 4);
                imgData.order(ByteOrder.nativeOrder());

                convertBitmapToByteBuffer(image);

                final float[][] imageProbArray = new float[1][1001];
                tflite.run(imgData, imageProbArray);

                final int highestIndex = findHighestElement(imageProbArray[0]);

                final long endTime = SystemClock.uptimeMillis();

                Handler mainHandler = new Handler(mainActivity.getApplicationContext().getMainLooper());
                Runnable updateFields = new Runnable() {
                    @Override
                    public void run() {
                    }
                };
                mainHandler.post(updateFields);

            }
        };
        (new Thread(runClassification)).start();



    }

    @Override
    public void runImageClassification(String filename, File file) throws IOException, TypeError {
        throw new TypeError("This Version Of This Method Is Not Supported For On Device Processing");
    }

    private MappedByteBuffer loadModelFile(AssetFileDescriptor fileDescriptor) throws IOException {
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

}
