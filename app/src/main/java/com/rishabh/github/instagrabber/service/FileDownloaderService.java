package com.rishabh.github.instagrabber.service;

/**
 * Created by rishabh on 10/3/17.
 */

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.os.ResultReceiver;
import com.rishabh.github.instagrabber.database.DBController;
import com.rishabh.github.instagrabber.database.InstaImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.net.ssl.HttpsURLConnection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class FileDownloaderService extends IntentService
{
  public static final String ARGUMENT_FILE_URL = "sourceFileUrl";
  public static final String ARGUMENT_TARGET_FILE = "targetFilePath";
  public static final String ARGUMENT_RESUTL_RECIEVER = "resultReceiver";

  public static final String RESPONSE_TARGET_FILE = "targetFilePath";
  public static final String RESPONSE_DOWNLOAD_PROGRESS = "downloadProgress";

  public static final int RESPONSE_CODE_DOWNLOAD_RESULT = 100;
  public static final int RESPONSE_CODE_DOWNLOAD_PROGRESS = 101;
  public static final String RESPONSE_CAPTION = "caption";
  public static final String RESPONSE_TYPE = "type";
  public static boolean stopDownload;
  private InstaImage instaImage;
  InputStream in;
  private long fileSize;
  private DBController dbcon;
  private Context mContext;

  public static final String CUSTOM_INTENT = "es.tempos21.sync.client.ProgressReceiver";
  //private String extension;

  public FileDownloaderService()
  {
    super("ImageDownloader");
  }

  public static void startAction(Context activity, String postUrl,ResultReceiver receiver)
  {
    Intent intent = new Intent(activity, FileDownloaderService.class);
    Bundle bundle = new Bundle();
    bundle.putString(ARGUMENT_FILE_URL, postUrl);
    //bundle.putString(ARGUMENT_TARGET_FILE, targetFile);
    bundle.putParcelable(ARGUMENT_RESUTL_RECIEVER, receiver);

    intent.putExtras(bundle);
    activity.startService(intent);
  }

  public static String getFileName(String uri)
  {
    return uri.substring(uri.lastIndexOf("/"));
  }

  @Override
  protected void onHandleIntent(Intent intent)
  {

    mContext = getApplicationContext();
    dbcon = new DBController(mContext);
    instaImage = new InstaImage();


    stopDownload = false;
    String fileUrl = intent.getStringExtra(ARGUMENT_FILE_URL);
    instaImage.set_instaImageURL(fileUrl);

    //String targetFile = intent.getStringExtra(ARGUMENT_TARGET_FILE);
    ResultReceiver receiver = intent.getParcelableExtra(ARGUMENT_RESUTL_RECIEVER);

    String contentType = openConnection(fileUrl);
    String outputFilePath = null;
    String targetFile="";
    String fileName="";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss");
    File direct = new File(Environment.getExternalStorageDirectory() + "/InstantInsta");

    File file=null;


    if (!direct.exists()) {
      direct = new File(Environment.getExternalStorageDirectory() + "/InstantInsta");
      direct.mkdirs();
    }

    if (contentType == null)
    {
      outputFilePath = null;
    } else if (contentType.contains("image")) {

      fileName = "Insta-" + simpleDateFormat.format(new Date()) + ".jpg";
      file = new File(direct, fileName);
      if (file.exists()) {
        file.delete();
        // some code to stop duplication
      }
      instaImage.set_name(fileName);
      outputFilePath = downloadImage(file.getAbsolutePath());

      //extension = "jpg";

    } else {
      fileName = "Insta-" + simpleDateFormat.format(new Date()) + ".mp4";

      file = new File(direct, fileName);
      if (file.exists()) {
        file.delete();
        // some code to stop duplication
      }
      instaImage.set_name(fileName);
      outputFilePath = downloadFile(file.getAbsolutePath(), receiver);
      //extension = "mp4";
    }

    Bundle bundle = new Bundle();
    bundle.putString(RESPONSE_TARGET_FILE, outputFilePath);
    bundle.putString(RESPONSE_CAPTION, instaImage.get_caption());
    receiver.send(RESPONSE_CODE_DOWNLOAD_RESULT, bundle);

  }

  /**
   * publish progress to main thread to update progress UI.
   *
   * @param progress - percent value out of 100.
   */
  private void publishProgress(ResultReceiver receiver, int progress)
  {
    if (receiver != null)
    {
      Bundle bundle = new Bundle();
      bundle.putInt(RESPONSE_DOWNLOAD_PROGRESS, progress);
      //bundle.putString(RESPONSE_TYPE,extension);
      receiver.send(RESPONSE_CODE_DOWNLOAD_PROGRESS, bundle);
    }
  }

  private String openConnection(String fileUrl)
  {
    try
    {

      String imgOrVidUrl=setInstaResourses();
      URL url;
      url = new URL(imgOrVidUrl);
      HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
      connection.setDoInput(true);
      connection.connect();
      if (connection.getResponseCode() != HttpsURLConnection.HTTP_OK)
      {
        return null;
      }
      fileSize = connection.getContentLength();
      String contentType = connection.getHeaderFields().get("Content-Type").get(0);
      in = connection.getInputStream();
      return contentType;
    } catch (MalformedURLException e)
    {
      e.printStackTrace();
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  private String setInstaResourses() {

    String strCaption = null;
    String downloadURL=null;
    URL url = null;
   boolean type=false;
    try {
      String postURL= instaImage.get_instaImageURL();
      Document doc = Jsoup.connect(postURL).get();

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

      // for image url
      int index = html.indexOf("display_src");
      index += 13;
      int start = html.indexOf("\"", index);
      start += 1;
      int end = html.indexOf("\"", start);

      String urlImage = html.substring(start, end);
      type = false;

      downloadURL = urlImage;
    } else {

      //for video url
      downloadURL = urlVid;
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
      instaImage.set_caption(strCaption);

      return downloadURL;
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  private String downloadImage(String outputFile)
  {
    try
    {

      File outFile = new File(outputFile);

      if (!outFile.getParentFile().exists())
        outFile.getParentFile().mkdirs();
      Bitmap bitmap = BitmapFactory.decodeStream(in);
      if (bitmap == null)
        return null;
      FileOutputStream out = new FileOutputStream(outFile);
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      bitmap.compress(Bitmap.CompressFormat.PNG, 85, outStream);
      byte[] buf = outStream.toByteArray();

      out.write(buf);

      instaImage.set_phoneImageURL(outputFile);

      int id=dbcon.getTotalImages()+1;

      instaImage.set_id(id);

      dbcon.addimage(instaImage);

      out.close();
      in.close();
      return outputFile;
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }

  private String downloadFile(String outputFile, ResultReceiver receiver)
  {
    try
    {
      File outFile = new File(outputFile);
      File tempfile = new File(outputFile + "_temp.pdf");
      if (!outFile.getParentFile().exists())
        outFile.getParentFile().mkdirs();

      FileOutputStream out = new FileOutputStream(tempfile);
      long downloaded = 0;
      byte[] buffer = new byte[1024];
      int len;
      while ((len = in.read(buffer)) != -1)
      {
        downloaded += len;
        out.write(buffer, 0, len);
        publishProgress(receiver, (int) (((downloaded * 100) / fileSize)));
        if (stopDownload)
          break;
      }
      out.flush();
      out.close();
      if (stopDownload)
        tempfile.delete();
      else
        tempfile.renameTo(outFile);

      instaImage.set_phoneImageURL(outputFile);
      int id=dbcon.getTotalImages()+1;

      instaImage.set_id(id);

      dbcon.addimage(instaImage);
      in.close();
      return outputFile;
    } catch (IOException e)
    {
      e.printStackTrace();
    }
    return null;
  }

}