package com.rishabh.github.instagrabber;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import com.github.lzyzsd.circleprogress.DonutProgress;
import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by rishabh on 25/2/17.
 */

public class DownloadFileFromURL extends AsyncTask<String, String, String> {

DonutProgress circularProgress;

  /**
   * Before starting background thread
   * Show Progress Bar Dialog
   * */
  @Override
  protected void onPreExecute() {
    super.onPreExecute();
    //showDialog(progress_bar_type);
    circularProgress.setVisibility(View.VISIBLE);
    circularProgress.setText("0%");
    circularProgress.setDonut_progress("0");
    circularProgress.setMax(100);

  }

  /**
   * Downloading file in background thread
   * */
  @Override
  protected String doInBackground(String... f_url) {
    int count;
    try {
      URL url = new URL(f_url[0]);
      URLConnection conection = url.openConnection();
      conection.connect();
      // getting file length
      int lenghtOfFile = conection.getContentLength();

      // input stream to read file - with 8k buffer
      InputStream input = new BufferedInputStream(url.openStream(), 8192);

      // Output stream to write file
      OutputStream output = new FileOutputStream("/sdcard/downloadedfile.mp4");

      byte data[] = new byte[1024];

      long total = 0;

      while ((count = input.read(data)) != -1) {
        total += count;
        // publishing the progress....
        // After this onProgressUpdate will be called
        publishProgress(""+((total*100)/lenghtOfFile));

        // writing data to file
        output.write(data, 0, count);
      }

      // flushing output
      output.flush();

      // closing streams
      output.close();
      input.close();

    } catch (Exception e) {
      Log.e("Error: ", e.getMessage());
    }

    return null;
  }

  /**
   * Updating progress bar
   * */
  protected void onProgressUpdate(String... progress) {
    // setting progress percentage
    circularProgress.setText(progress[0]+"%");
    circularProgress.setDonut_progress(progress[0]);
  }

  /**
   * After completing background task
   * Dismiss the progress dialog
   * **/
  @Override
  protected void onPostExecute(String file_url) {
    // dismiss the dialog after the file was downloaded
    //dismissDialog(progress_bar_type);

    circularProgress.setVisibility(View.GONE);

    // Displaying downloaded image into image view
    // Reading image path from sdcard
    String imagePath = Environment.getExternalStorageDirectory().toString() + "/downloadedfile.mp4";
    // setting downloaded into image view
    //my_image.setImageDrawable(Drawable.createFromPath(imagePath));
    //my_image.setVideoPath(imagePath);
    //my_image.start();
  }

}
