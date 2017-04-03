package com.rishabh.github.instagrabber.tabs;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.os.ResultReceiver;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.rishabh.github.instagrabber.MainActivity;
import com.rishabh.github.instagrabber.R;
import com.rishabh.github.instagrabber.database.DBController;
import com.rishabh.github.instagrabber.database.InstaImage;
import com.rishabh.github.instagrabber.service.DownloadService;
import com.rishabh.github.instagrabber.service.FileDownloaderService;
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

import static android.content.Context.CLIPBOARD_SERVICE;
import static com.rishabh.github.instagrabber.service.FileDownloaderService.RESPONSE_DOWNLOAD_PROGRESS;

public class DownloadFragment extends Fragment {

	DonutProgress circularProgress;
	private FragmentActivity mContext;
	private TextView tvCaption,tvCopy;
	private EditText etURL;
	static ProgressDialog mProgressDialog = null;
	Button btnCheckURL,btnPaste;
	ImageView ivImage, ivPlayBtn;
	private ClipboardManager clipBoard;
	private boolean type;
	FloatingActionButton fabDownload;
	//DB
	private DBController dbcon;
	private Activity activity;
	ProgressBar mProgressBar;

	DownloadService mService;
	boolean mBound = false;
	TextView tvProgress,tvCancel;
	LinearLayout llDownloadLayout;

	String mPreviousText="";
	private int progress;
	String pattern = "https://www.instagram.com/p/.";

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Tag1","GamesFrag");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_download, container, false);
		mContext =getActivity();

		circularProgress = (DonutProgress) rootView.findViewById(R.id.donut_progress);
		tvCaption = (TextView)rootView.findViewById(R.id.tv_caption);

		btnCheckURL= (Button) rootView.findViewById(R.id.btnCheckURL);
		etURL = (EditText) rootView.findViewById(R.id.edittxturl);
		ivImage = (ImageView) rootView.findViewById(R.id.ivImage);
		btnPaste = (Button) rootView.findViewById(R.id.btnPaste);
		fabDownload = (FloatingActionButton) rootView.findViewById(R.id.fab);
		ivPlayBtn = (ImageView) rootView.findViewById(R.id.ivPlayBtn);

		ivPlayBtn.setVisibility(View.INVISIBLE);
		clipBoard = (ClipboardManager)mContext.getSystemService(CLIPBOARD_SERVICE);
		mProgressBar= (ProgressBar) rootView.findViewById(R.id.progressBar);
		tvProgress = (TextView) rootView.findViewById(R.id.tvProgress);
		tvCancel= (TextView) rootView.findViewById(R.id.tvCancel);
		tvCopy= (TextView) rootView.findViewById(R.id.tvCopy);

		llDownloadLayout = (LinearLayout) rootView.findViewById(R.id.llDownloadLayout);

		mContext.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		Intent intent = new Intent(mContext, DownloadService.class);
		mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

		//DB
		dbcon = new DBController(mContext);


		btnCheckURL.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {

				//todo check using reg exp whether the url is correct
					new ValidateFileFromURL().execute(etURL.getText().toString());
			}
		});

		btnPaste.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {

				ClipData clipData = clipBoard.getPrimaryClip();
				ClipData.Item item = clipData.getItemAt(0);
				String clipURL = item.getText().toString();
				etURL.setText(clipURL+"");
			}
		});

		fabDownload.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {

				String link= etURL.getText().toString();
				if (!dbcon.isURLPresent(link)) {

					if (checkURL(link)) {

						FileDownloaderService.startAction(mContext, etURL.getText().toString(), new imageDownloadReceiver(new Handler()));
						Toast.makeText(mContext, "Post Already Downloaded", Toast.LENGTH_SHORT).show();
						((MainActivity) activity).viewPager.setCurrentItem(1, true);
					} else {
						Toast.makeText(mContext, "Wrong URL", Toast.LENGTH_SHORT).show();
					}
				}else {
					Toast.makeText(mContext, "Post Already Downloaded",Toast.LENGTH_SHORT).show();
					((MainActivity)activity).viewPager.setCurrentItem(1, true);
				}
				//new DownloadFileFromURL().execute(etURL.getText().toString());
			}
		});

		tvCopy.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				clipBoard.setPrimaryClip(ClipData.newPlainText("Caption", tvCaption.getText().toString()));
			}
		});


		final ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.addPrimaryClipChangedListener( new ClipboardManager.OnPrimaryClipChangedListener() {
			public void onPrimaryClipChanged() {
				String a = clipboard.getText().toString();
				Toast.makeText(mContext,"Copy:\n"+a,Toast.LENGTH_LONG).show();

				if(mPreviousText.equals(a)) {
					return;
				}else {

					//File direct = new File(Environment.getExternalStorageDirectory() + "/InstantInsta.mp4");

					if (!dbcon.isURLPresent(a)) {

						if (checkURL(a)) {
							Handler handler = new Handler();
							imageDownloadReceiver imageDownloadReceiver = new imageDownloadReceiver(handler);
							FileDownloaderService.startAction(mContext, a, imageDownloadReceiver);
							//mService.downloadAsynFile(a);
							mPreviousText = a;
						}
					}else {
							Toast.makeText(mContext, "Post Already Downloaded",Toast.LENGTH_SHORT).show();
						((MainActivity)activity).viewPager.setCurrentItem(1, true);
					}
				}
			}
		}
		);

		return rootView;
	}


	public class imageDownloadReceiver extends ResultReceiver
	{

		public imageDownloadReceiver(Handler handler)
		{
			super(handler);
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData)
		{
			super.onReceiveResult(resultCode, resultData);
			switch (resultCode)
			{
				case FileDownloaderService.RESPONSE_CODE_DOWNLOAD_RESULT:
					String outFilePath = resultData
							.getString(FileDownloaderService.ARGUMENT_TARGET_FILE);
					String caption =resultData.getString(FileDownloaderService.RESPONSE_CAPTION);
					if (outFilePath != null)
					{
						// outFilePath contains path of downloaded file. Do whatever you want to do with it.

						mProgressBar.setVisibility(View.GONE);



						int i = outFilePath.lastIndexOf('.');
						String extension = outFilePath.substring(i + 1);

						if (extension.equalsIgnoreCase("mp4")) {
							Glide.with(mContext)
									.load(outFilePath)
									.asBitmap()
									.placeholder(R.drawable.ic_insta_128)
									.into(ivImage);
							ivPlayBtn.setVisibility(View.VISIBLE);
						} else {

							ivPlayBtn.setVisibility(View.GONE);
							File file = new File(outFilePath);
							Uri imageUri = Uri.fromFile(file);

							Glide.with(mContext.getApplicationContext())
									.load(imageUri)
									.into(ivImage);

						}

						tvCaption.setText(Html.fromHtml(caption+""));

						tvProgress.setVisibility(View.GONE);
						tvCancel.setVisibility(View.GONE);
						((OnPostDownload) activity).refreshList();
								System.out.println("Downloaded " + outFilePath);

						//removing 100% and cancel

					}else {
						System.out.println("Failed");
					}
					break;

				case FileDownloaderService.RESPONSE_CODE_DOWNLOAD_PROGRESS:

					progress=0;
					progress = resultData.getInt(RESPONSE_DOWNLOAD_PROGRESS);

					//String extension=resultData.getString(RESPONSE_TYPE);
					//if (extension.equalsIgnoreCase("mp4")){
					//	ivPlayBtn.setVisibility(View.VISIBLE);
					//}else {
					//	ivPlayBtn.setVisibility(View.GONE);
					//}

					System.out.println("Progress:"+ progress);


					//circularProgress.setVisibility(View.VISIBLE);
					//circularProgress.setText( progress+ "%");
					//circularProgress.setDonut_progress(progress+"");

					llDownloadLayout.setVisibility(View.VISIBLE);
					tvProgress.setVisibility(View.VISIBLE);
					tvCancel.setVisibility(View.VISIBLE);
					tvProgress.setText(progress +"%");

					//progressbar not working
					mProgressBar.setMax(100);
					mProgressBar.setProgress(progress);
					mProgressBar.setProgress(0);
					//mProgressBar.post(new Runnable() {
					//	@Override public void run() {
					//		mProgressBar.setProgress(progress);
					//	}
					//});
					//ivImage.setImageBitmap();

					break;
				default:
					break;
			}
		}
	}





	/**
	 *  receiver for downloading insta share url
	 */

	// Flag if receiver is registered
	private boolean mReceiversRegistered = false;
	// Defiine a handler and a broadcast receiver
	private final Handler mHandler = new Handler();


	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(DownloadService.CUSTOM_INTENT)) {

				if (intent.getFlags()<100) {
					circularProgress.setVisibility(View.VISIBLE);
					circularProgress.setText(intent.getFlags() + "%");
					circularProgress.setDonut_progress(intent.getFlags() + "");
					circularProgress.setMax(100);
				}else{
						circularProgress.setVisibility(View.GONE);
						//mService.stopSelf();
						((OnPostDownload) activity).refreshList();
						String filePath=intent.getStringExtra("URL");

					if (filePath!=null){

						String extension = "";

						// recognizing weather its a image or video from file format
						int i = filePath.lastIndexOf('.');
						extension = filePath.substring(i + 1);

						if (extension.equalsIgnoreCase("mp4")) {
							Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
							ivImage.setImageBitmap(thumbnail);
							//ivPlayBtn.setVisibility(View.VISIBLE);
						} else {
							ivImage.setImageDrawable(Drawable.createFromPath(filePath));
						}
					}
				}
				}
		}
	};

	/** Defines callbacks for service binding, passed to bindService() */
	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
				IBinder service) {
			// We've bound to LocalService, cast the IBinder and get LocalService instance
			DownloadService.LocalBinder binder = (DownloadService.LocalBinder) service;
			mService = binder.getService();
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mBound = false;
		}
	};


	@Override public void onResume() {
		super.onResume();
		// Register Sync Recievers
		IntentFilter intentToReceiveFilter = new IntentFilter();
		intentToReceiveFilter.addAction(DownloadService.CUSTOM_INTENT);
		mContext.registerReceiver(mIntentReceiver, intentToReceiveFilter, null, mHandler);
		mReceiversRegistered = true;
	}

	@Override
	public void onPause() {
		super.onPause();

		// Make sure you unregister your receivers when you pause your activity
		if(mReceiversRegistered) {
			mContext.unregisterReceiver(mIntentReceiver);
			mReceiversRegistered = false;
		}
	}

	@Override public void onStop() {
		super.onStop();
		super.onStop();
		// Unbind from the service
		if (mBound) {
			mContext.unbindService(mConnection);
			mBound = false;
		}

		if(mReceiversRegistered) {
			mContext.unregisterReceiver(mIntentReceiver);
			mReceiversRegistered = false;
		}
	}

	/**
	 * Background Async Task to check validate file and get URL
	 * */
	class ValidateFileFromURL extends AsyncTask<String, String, Bitmap> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showDialog(mContext);

		}

		@Override
		protected Bitmap doInBackground(String... f_url) {
			try {

				Document doc = Jsoup.connect(f_url[0]).get();
				String html = doc.toString();

				type = false;

				//for caption
				int indexcaption = html.indexOf("\"caption\"");
				indexcaption += 9;

				int startCaption = html.indexOf("\"", indexcaption);
				startCaption += 1;
				int endCaption = html.indexOf("\"", startCaption);

				String strCaption = null;
				strCaption = html.substring(startCaption, endCaption);

				//setting caption flag=0 for caption flag=1 for vid flag=2 for image
				publishProgress("0",strCaption);

				//for video
				int indexVid = html.indexOf("\"video_url\"");
				indexVid += 11;
				int startVid = html.indexOf("\"", indexVid);
				startVid += 1;
				int endVid = html.indexOf("\"", startVid);

				String urlVid = null;
				urlVid=html.substring(startVid, endVid);

				if (urlVid.equalsIgnoreCase("en")){
					// it is a vid show play btn
					type=true;
				}

				//for image url
				int index = html.indexOf("display_src");
				index += 13;
				int start = html.indexOf("\"", index);
				start += 1;
				int end = html.indexOf("\"", start);
				//                System.out.println("start:"+start+ "end:"+ end);
				String urlImage = html.substring(start, end);



				Bitmap mIcon11 = null;
				try {
					InputStream in = new java.net.URL(urlImage).openStream();
					mIcon11 = BitmapFactory.decodeStream(in);
				} catch (Exception e) {
					Log.e("Error", e.getMessage());
					e.printStackTrace();
				}
				return mIcon11;

			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
			}

			return null;
		}

		protected void onProgressUpdate(String... progress) {

			if (progress[0]=="0") {
				tvCaption.setText(Html.fromHtml(progress[1]+""));
				dismissDialog();
			}
		}

		@Override
		protected void onPostExecute(Bitmap image) {
			dismissDialog();
			ivImage.setImageBitmap(image);
			if (type){
				ivPlayBtn.setVisibility(View.VISIBLE);
			}
		}

	}


	/**
	 * Background Async Task to download file
	 * */
	class DownloadFileFromURL extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread
		 * Show Progress Bar Dialog
		 */
		@Override protected void onPreExecute() {
			super.onPreExecute();


			circularProgress.setVisibility(View.VISIBLE);
			circularProgress.setText("0%");
			circularProgress.setDonut_progress("0");
			circularProgress.setMax(100);

		}

		/**
		 * Downloading file in background thread
		 */
		@Override protected String doInBackground(String... f_url) {
			int count;
			type=false;
			try {
				String strCaption= null;

				Document doc = Jsoup.connect(f_url[0]).get();
				URL url=null;
				String html = doc.toString();
				String urlVid=null;

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

				}else{

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

				strCaption= html.substring(startCaption, endCaption);


				URLConnection conection = url.openConnection();
				conection.connect();
				// getting file length
				int lenghtOfFile = conection.getContentLength();

				// input stream to read file - with 8k buffer
				InputStream input = new BufferedInputStream(url.openStream(), 8192);

				//generate a unique name

				SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss");
				//File myFile = null;


				// Output stream to write file

				File direct = new File(Environment.getExternalStorageDirectory() + "/InstantInsta");

				if (!direct.exists()) {
					direct = new File(Environment.getExternalStorageDirectory() + "/InstantInsta");
					direct.mkdirs();
				}

				String fileName=null;
				if(!type) {
					fileName = "Insta-"
							+ simpleDateFormat.format(new Date())
							+ ".jpg";
				}else{

					fileName = "Insta-"
							+ simpleDateFormat.format(new Date())
							+ ".mp4";
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
					publishProgress("" + ((total * 100) / lenghtOfFile));

					// writing data to file
					output.write(data, 0, count);
				}

				// flushing output
				output.flush();

				// closing streams
				output.close();
				input.close();

				// add image into the database

				int imageID=dbcon.getTotalImages()+1;

				InstaImage instaImage=new InstaImage(imageID, fileName, f_url[0], file.getAbsolutePath(), strCaption);
				dbcon.addimage(instaImage);

				return file.getAbsolutePath();
			} catch (Exception e) {
				Log.e("Error", e.getMessage());
			}

			return null;
		}

		/**
		 * Updating progress bar
		 */
		protected void onProgressUpdate(String... progress) {
			// setting progress percentage
			circularProgress.setText(progress[0] + "%");
			circularProgress.setDonut_progress(progress[0]);
		}

		/**
		 * After completing background task
		 * Dismiss the progress dialog
		 **/
		@Override protected void onPostExecute(String file_url) {

			circularProgress.setVisibility(View.GONE);
			Toast.makeText(mContext, "Post Saved", Toast.LENGTH_LONG).show();

			String extension = "";

			// recognizing weather its a image or video from file format
			int i = file_url.lastIndexOf('.');
			extension = file_url.substring(i + 1);

			if (extension.equalsIgnoreCase("mp4")) {
				Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(file_url, MediaStore.Images.Thumbnails.MINI_KIND);
				ivImage.setImageBitmap(thumbnail);
				//ivPlayBtn.setVisibility(View.VISIBLE);
			} else {
				ivImage.setImageDrawable(Drawable.createFromPath(file_url));
			}
			((OnPostDownload) activity).refreshList();
		}
	}

	public static void showDialog(Context context) {
		mProgressDialog = new ProgressDialog(context);
		mProgressDialog.setIndeterminate(true);
		mProgressDialog.setMessage("Please wait...");
		mProgressDialog.show();
	}

	public static void dismissDialog() {
		if (mProgressDialog.isShowing() && mProgressDialog != null)
			mProgressDialog.dismiss();
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i("Tag","DownloadFragment:onStart");
	}

	public interface OnPostDownload{
		void refreshList();
	}

	@Override public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = activity;
	}

	boolean checkURL(String url){

		Pattern r = Pattern.compile(pattern);

		// Now create matcher object.
		Matcher m = r.matcher(url);
		if (m.find( )) {
			System.out.println("Found value: " + m.group(0) );
			return true;
		}else {
			System.out.println("NO MATCH");
			return false;
		}
	}

}
