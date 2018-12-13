package edu.wpi.cs4518_scavengerhunt;

import android.graphics.Bitmap;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

import edu.wpi.cs4518_scavengerhunt.exceptions.TypeError;

public abstract class MLHelper {
    ByteBuffer imgData;
    String[] labels;
    Interpreter tflite;

    protected final MainActivity mainActivity;

    final int SIZE_X = 299;
    final int SIZE_Y = 299;
    final int IMAGE_MEAN = 128;
    final float IMAGE_STD = (float) 128.0;

    public MLHelper(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        try {
            labels = loadLablesFromFile(mainActivity.getLabelFileInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract void runImageClassification(Bitmap image) throws IOException, TypeError;
    public abstract void runImageClassification(String filename, File file) throws IOException, TypeError;

    protected int findHighestElement(float[] data) {
        int highest = 0;
        for (int index = 0; index < data.length; index++) {
            if (data[index] > data[highest]) {
                highest = index;
            }
        }
        return highest;
    }


    private String[] loadLablesFromFile(InputStream finput) throws IOException {
        BufferedReader buffer = new BufferedReader(new InputStreamReader(finput));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = buffer.readLine()) != null) {
            sb.append(line);
            sb.append(',');
        }
        buffer.close();
        finput.close();
        return sb.toString().split(",");
    }

    static String getModelPath() {
        return "model/inception_v3.tflite";
    }

    protected void convertBitmapToByteBuffer(Bitmap bitmap) {
        int[] intValues = new int[SIZE_X * SIZE_Y];

        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        // Convert the image to floating point.
        int pixel = 0;
        for (int i = 0; i < SIZE_X; ++i) {
            for (int j = 0; j < SIZE_Y; ++j) {
                final int val = intValues[pixel++];
                addPixelValue(val);
            }
        }
    }

    protected void addPixelValue(int pixelValue) {
        imgData.putFloat((((pixelValue >> 16) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat((((pixelValue >> 8) & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
        imgData.putFloat(((pixelValue & 0xFF) - IMAGE_MEAN) / IMAGE_STD);
    }


}