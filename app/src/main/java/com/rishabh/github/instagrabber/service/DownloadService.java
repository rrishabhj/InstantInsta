package com.rishabh.github.instagrabber.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.Environment;
import android.os.IBinder;
import android.provider.SyncStateContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;
import com.rishabh.github.instagrabber.database.DBController;
import com.rishabh.github.instagrabber.database.InstaImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Created by rishabh on 9/3/17.
 */

public class DownloadService extends Service {

  private DBController dbcon;
  private Context ctx;
  private boolean type;
  // Binder given to clients
  private final IBinder mBinder = new LocalBinder();
  String pattern = "https://www.instagram.com/p/.";


  @Nullable @Override public IBinder onBind(Intent intent) {
    ctx = getApplicationContext();
    return mBinder;
  }

  public class LocalBinder extends Binder {
    public DownloadService getService() {
      // Return this instance of LocalService so clients can call public methods
      return DownloadService.this;
    }
  }

  private class DownloadFile extends AsyncTask<String, String, String> {

    @Override protected void onPreExecute() {
      super.onPreExecute();
      //DB
      dbcon = new DBController(ctx);

    }

    @Override protected String doInBackground(String... f_url) {
      int count;
      type = false;
      try {
        String strCaption = null;

        Document doc = Jsoup.connect(f_url[0]).get();
        URL url = null;
        String html = doc.toString();
        String urlVid = null;

        //for video
        int indexVid = html.indexOf("\"video_url\"");
        indexVid += 11;
        int startVid = html.indexOf("\"", indexVid);
        startVid += 1;
        int endVid = html.indexOf("\"", startVid);

        urlVid = html.substring(startVid, endVid);

        if (urlVid.equalsIgnoreCase("en")) {
          //
          //	url = new URL(urlVid);
          //	type =false;
          //}else {
          // for image url

          int index = html.indexOf("display_src");
          index += 13;
          int start = html.indexOf("\"", index);
          start += 1;
          int end = html.indexOf("\"", start);
          //                System.out.println("start:"+start+ "end:"+ end);
          String urlImage = html.substring(start, end);
          type = false;
          url = new URL(urlImage);
        } else {

          url = new URL(urlVid);
          type = true;
        }

        // true is for video and false is image

        //for caption
        int indexcaption = html.indexOf("\"caption\"");
        indexcaption += 9;

        int startCaption = html.indexOf("\"", indexcaption);
        startCaption += 1;
        int endCaption = html.indexOf("\"", startCaption);

        strCaption = html.substring(startCaption, endCaption);

        URLConnection conection = url.openConnection();
        conection.connect();
        // getting file length
        int lenghtOfFile = conection.getContentLength();

        // input stream to read file - with 8k buffer
        InputStream input = new BufferedInputStream(url.openStream(), 8192);

        //generate a unique name

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss");
        //File myFile = null;

        // Output stream to write file

        File direct = new File(Environment.getExternalStorageDirectory() + "/InstantInsta");

        if (!direct.exists()) {
          direct = new File(Environment.getExternalStorageDirectory() + "/InstantInsta");
          direct.mkdirs();
        }

        String fileName = null;
        if (!type) {
          fileName = "Insta-" + simpleDateFormat.format(new Date()) + ".jpg";
        } else {

          fileName = "Insta-" + simpleDateFormat.format(new Date()) + ".mp4";
        }

        File file = new File(direct, fileName);
        if (file.exists()) {
          file.delete();
        }

        OutputStream output = new FileOutputStream(file);

        byte data[] = new byte[1024];

        long total = 0;

        while ((count = input.read(data)) != -1) {
          total += count;
          // publishing the progress....
          // After this onProgressUpdate will be called
          publishProgress(""+((total * 100) / lenghtOfFile));

          // writing data to file
          output.write(data, 0, count);
        }

        // flushing output
        output.flush();

        // closing streams
        output.close();
        input.close();

        // add image into the database

        int imageID = dbcon.getTotalImages() + 1;

        InstaImage instaImage =
            new InstaImage(imageID, fileName, f_url[0], file.getAbsolutePath(), strCaption);
        dbcon.addimage(instaImage);

        return file.getAbsolutePath();
      } catch (Exception e) {
        Log.e("Error", e.getMessage());
      }

      return null;
    }

    @Override protected void onProgressUpdate(String... progress) {
      Intent i = new Intent();
      i.setAction(CUSTOM_INTENT);
      i.putExtra("PROGRESS",progress[0]);
      ctx.sendBroadcast(i);
    }

    @Override protected void onPostExecute(String s) {
      super.onPostExecute(s);

      Intent i = new Intent();
      i.setAction(CUSTOM_INTENT);
      i.putExtra("URL", s);
      ctx.sendBroadcast(i);
      stopSelf();
    }

    boolean checkURL(String url){

      Pattern r = Pattern.compile(pattern);

      // Now create matcher object.
      Matcher m = r.matcher("https://www.google.com");
      if (m.find( )) {
        System.out.println("Found value: " + m.group(0) );
        return true;
      }else {
        System.out.println("NO MATCH");
        return false;
      }
    }
  }




  public static final String CUSTOM_INTENT = "es.tempos21.sync.client.ProgressReceiver";

  public void downloadAsynFile(String url) {
    try {
      DownloadFile d = new DownloadFile();
      d.execute(url);
    } catch (Exception e) {
      Toast.makeText(ctx, " Download Failed", Toast.LENGTH_SHORT).show();
    }
  }

}