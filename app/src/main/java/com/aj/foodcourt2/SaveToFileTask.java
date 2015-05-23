package com.aj.foodcourt2;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Joost on 22/04/2015.
 *
 */
public class SaveToFileTask extends AsyncTask <String, Integer, Long> {
    Context context;
    String fileName = "accelerometerData.txt";

    public SaveToFileTask(Context context) {
        this.context = context;
        Log.d("fileLocation", context.getExternalFilesDir(null).getAbsolutePath() );
    }

    public SaveToFileTask(Context context, String fileName) {
        this.context = context;
        this.fileName = fileName;
        Log.d("fileLocation", context.getExternalFilesDir(null).getAbsolutePath() );
    }

    @Override
    protected Long doInBackground(String... params) {
        long a = 0;
        String fcontent = params[0];
        File traceFile = new File(context.getExternalFilesDir(null), fileName);


        try{
            // If file does not exists, then create it
            if (!traceFile.exists()) {
                traceFile.createNewFile();
            }

            BufferedWriter writer = new BufferedWriter(new FileWriter(traceFile, true /*true for append*/));
            writer.write(fcontent);
            writer.close();

            //refresh data (see link in evernote)
            MediaScannerConnection.scanFile(context, new String[] { traceFile.toString()}, null, null);

        } catch (IOException e) {
            e.printStackTrace();
        }


        return a;
    }


    @Override
    protected void onPostExecute(Long aLong) {
        super.onPostExecute(aLong);
    }
}
