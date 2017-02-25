package com.rishabh.github.instagrabber.tabs;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.github.lzyzsd.circleprogress.DonutProgress;
import com.rishabh.github.instagrabber.MainActivity;
import com.rishabh.github.instagrabber.R;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import static android.content.Context.CLIPBOARD_SERVICE;

public class DownloadFragment extends Fragment {

	DonutProgress circularProgress;
	private FragmentActivity mContext;
	private TextView tvCaption;
	private EditText etURL;
	static ProgressDialog mProgressDialog = null;
	Button btnCheckURL,btnPaste;
	ImageView ivImage;
	private ClipboardManager clipBoard;
	private boolean type;
	FloatingActionButton fabDownload;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Tag1","GamesFrag");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_download, container, false);
		circularProgress = (DonutProgress) rootView.findViewById(R.id.donut_progress);
		tvCaption = (TextView)rootView.findViewById(R.id.tv_caption);
		btnCheckURL= (Button) rootView.findViewById(R.id.btnCheckURL);
		etURL = (EditText) rootView.findViewById(R.id.edittxturl);
		ivImage = (ImageView) rootView.findViewById(R.id.ivImage);
		btnPaste = (Button) rootView.findViewById(R.id.btnPaste);
		fabDownload = (FloatingActionButton) rootView.findViewById(R.id.fab);
		mContext =getActivity();

		clipBoard = (ClipboardManager)mContext.getSystemService(CLIPBOARD_SERVICE);


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
				new DownloadFileFromURL().execute(etURL.getText().toString());
			}
		});

		//final ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
		//clipboard.addPrimaryClipChangedListener( new ClipboardManager.OnPrimaryClipChangedListener() {
		//	public void onPrimaryClipChanged() {
		//		String a = clipboard.getText().toString();
		//		Toast.makeText(mContext,"Copy:\n"+a,Toast.LENGTH_LONG).show();
    //
		//		DownloadFileFromURL downloadFileFromURL=new DownloadFileFromURL();
    //
		//		//first perform check whether it is a valid URL
		//		//TODO
    //
		//		downloadFileFromURL.execute(a);
		//	}
		//});
		return rootView;
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

				if (urlVid!=null){


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
				tvCaption.setText(progress[1]);
				dismissDialog();
			}
		}

		@Override
		protected void onPostExecute(Bitmap image) {
			dismissDialog();
			ivImage.setImageBitmap(image);
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
			try {

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

				if (urlVid!=null) {

					url = new URL(urlVid);
					type =false;
				}else {//for image url
					int index = html.indexOf("display_src");
					index += 13;
					int start = html.indexOf("\"", index);
					start += 1;
					int end = html.indexOf("\"", start);
					//                System.out.println("start:"+start+ "end:"+ end);
					String urlImage = html.substring(start, end);

					url = new URL(urlImage);
					type = true;
				}

				URLConnection conection = url.openConnection();
				conection.connect();
				// getting file length
				int lenghtOfFile = conection.getContentLength();

				// input stream to read file - with 8k buffer
				InputStream input = new BufferedInputStream(url.openStream(), 8192);

				//generate a unique name
				// if type = false it is vid else img

				SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
				File myFile = null;
				if(type) {
					myFile =
							new File(Environment.getExternalStorageDirectory().getAbsolutePath()
									+
									File.separator
									+ "InstagramImageDownloader"
									+ File.separator
									+ "Insta-"
									+ simpleDateFormat.format(new Date())
									+ ".jpg");
				}else{
					myFile =
							new File(Environment.getExternalStorageDirectory().getAbsolutePath()
									+
									File.separator
									+ "InstagramImageDownloader"
									+ File.separator
									+ "Insta-"
									+ simpleDateFormat.format(new Date())
									+ ".mp4");

				}

				// Output stream to write file


				OutputStream output = new FileOutputStream(myFile);

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

				return myFile.getAbsolutePath();
			} catch (Exception e) {
				Log.e("Error: ", e.getMessage());
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
			// dismiss the dialog after the file was downloaded
			//dismissDialog(progress_bar_type);

			circularProgress.setVisibility(View.GONE);

			// Displaying downloaded image into image view
			// Reading image path from sdcard


			//String imagePath = Environment.getExternalStorageDirectory().toString() + "/downloadedfile.mp4";
			Toast.makeText(mContext,"Vid Saved",Toast.LENGTH_LONG).show();
			// setting downloaded into image view
			ivImage.setImageDrawable(Drawable.createFromPath(file_url));
			//my_image.setVideoPath(imagePath);
			//my_image.start();

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

		Log.i("Tag","GamesFragemnt:onActivityCreated");
	}

	@Override
	public void onStart() {
		super.onStart();
		Log.i("Tag","DownloadFragment:onStart");
	}



}
