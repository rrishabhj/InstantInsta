//package com.rishabh.github.instagrabber.service;
//
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.AsyncTask;
//import android.os.DeadObjectException;
//import android.os.Environment;
//import android.os.IBinder;
//import android.provider.SyncStateContract;
//import android.support.annotation.Nullable;
//import android.util.Log;
//
///**
// * Created by rishabh on 9/3/17.
// */
//
//public class DownloadService extends Service{
//
//  private static final String CLASS_NAME = DownloadService.class.getSimpleName();
//  private List<Download> downloads = new ArrayList<Download>();
//  private int currentPosition;
//  public static final String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
//  private Context ctx;
//
//  @Nullable @Override public IBinder onBind(Intent intent) {
//    ctx = getApplicationContext();
//    return mBinder;
//  }
//
//  private class DownloadFile extends AsyncTask<String, Integer, String> {
//
//    @Override
//    protected String doInBackground(String... _url) {
//      Log.d(Constants.LOG_TAG, CLASS_NAME + " Start the background GetNewsTask \nURL :" + _url[0]);
//      int count;
//      File finalFile = new File(sdcardPath + Constants.APK_LOCAL_PATH + "/" + splitName(_url[0]));
//      try {
//        if (!finalFile.exists()) {
//          Log.i(Constants.LOG_TAG, CLASS_NAME + " Donwloading apk from the Web");
//          URL url = new URL(_url[0]);
//          URLConnection conexion = url.openConnection();
//          conexion.connect();
//          // this will be useful so that you can show a tipical 0-100%
//          // progress bar
//          int lenghtOfFile = conexion.getContentLength();
//          // downlod the file
//          InputStream input = new BufferedInputStream(url.openStream());
//          File dir = new File(sdcardPath + Constants.APK_LOCAL_PATH);
//          if (!dir.exists())
//            dir.mkdirs();
//          OutputStream output = new FileOutputStream(sdcardPath + Constants.APK_LOCAL_PATH + "/" + splitName(_url[0]));
//          byte data[] = new byte[1024];
//          long total = 0;
//          while ((count = input.read(data)) != -1) {
//            total += count;
//            // publishing the progress....
//            publishProgress((int) (total * 100 / lenghtOfFile));
//            output.write(data, 0, count);
//          }
//          output.flush();
//          output.close();
//          input.close();
//        } else {
//          Log.i(Constants.LOG_TAG, CLASS_NAME + " Apk in SDcard");
//          publishProgress(100);
//        }
//      } catch (Exception e) {
//      }
//
//      return null;
//
//    }
//
//    @Override
//    protected void onProgressUpdate(Integer... progress) {
//      Intent i = new Intent();
//      i.setAction(CUSTOM_INTENT);
//      i.setFlags(progress[0]);
//      ctx.sendBroadcast(i);
//    }
//  }
//
//  private String splitName(String url) {
//    String[] output = url.split("/");
//    return output[output.length - 1];
//  }
//
//  public static final String CUSTOM_INTENT = "es.tempos21.sync.client.ProgressReceiver";
//
//  private final IDownloadService.Stub mBinder = new IDownloadService.Stub() {
//
//    public void downloadAsynFile(String url) throws DeadObjectException {
//      try {
//        DownloadFile d = new DownloadFile();
//        d.execute(url);
//      } catch (Exception e) {
//        Log.e(SyncStateContract.Constants.LOG_TAG, CLASS_NAME + " " +e.getMessage());         }
//    }
//
//
//  }
//};
//
//
//interface IDownloadService {
//
//  void downloadAsynFile(String url);
//}
//}
