package edu.wpi.cs4518_scavengerhunt;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class HuntHelper {

    private Context mContext;
    private int NUM_CLASSES = 1001;
    private String[] labelList = new String[NUM_CLASSES];
    private String[] impList = new String[NUM_CLASSES];

    private String path = "model/labels.txt";

    public HuntHelper(Context context) {
        this.mContext = context;
    }

    public void huntList() {
        AssetManager am = mContext.getAssets();
        int i = 0;
        try {

            InputStream is = am.open(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = reader.readLine()) != null) {
                labelList[i] = line;
                i++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String newHuntItem() {
        boolean allowed = true;
        int random = (int) (Math.random() * (NUM_CLASSES + 1) + 1);
        String gen = labelList[random];
        for (String s : impList) {
            if (gen.equals(s))
                allowed = false;

        }
        if (allowed)
            return gen;
        else
            return newHuntItem();
    }

    public void addToImp(String curItem) {
        impList[impList.length - 1] = curItem;
    }
}

