package com.rishabh.github.instagrabber.adaptor;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.rishabh.github.instagrabber.R;
import com.rishabh.github.instagrabber.WebViewActivity;
import com.rishabh.github.instagrabber.database.DBController;
import com.rishabh.github.instagrabber.database.InstaImage;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by rishabh on 25/2/17.
 */

public class ImageRecyclerAdaptor  extends RecyclerView.Adapter<ImageRecyclerAdaptor.ItemViewHolder>{

  ArrayList<InstaImage> imageList;
  Context mContext;

  //DB
  private DBController dbcon;
  private String extension;

  public ImageRecyclerAdaptor() {
  }

  public ImageRecyclerAdaptor( Context context) {
    mContext=context;
    dbcon = new DBController(mContext);
    imageList = dbcon.getAllInstaImages();
  }

  @Override public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_image, parent,
        false);
    //DB
    //dbcon = new DBController(mContext);

    return new ItemViewHolder(view);

  }

  @Override public void onBindViewHolder(ItemViewHolder holder, final int position) {
//"/storage/emulated/0/DCIM/Camera/IMG_20151102_193132.jpg"

    //show images in reverse order
    InstaImage instaImage=imageList.get(imageList.size()-1-position);

    //first row id in sqlite is 1

    File imgFile = new File(instaImage.get_phoneImageURL());

    if(imgFile.exists()){

      // recognizing weather its a image or video from file format
        int i = instaImage.get_name().lastIndexOf('.');
        extension = instaImage.get_name().substring(i + 1);

      if (extension.equalsIgnoreCase("mp4")) {

        Glide.with(mContext)
            .load(imgFile.getAbsoluteFile())
            .asBitmap()
            .placeholder(R.drawable.ic_insta_128)
            .into(holder.imageView);

        //Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(imgFile.getAbsolutePath(), MediaStore.Images.Thumbnails.MINI_KIND);
        //holder.imageView.setImageBitmap(thumbnail);
        holder.ivItemPlay.setVisibility(View.VISIBLE);
      } else {


        File file = new File(imgFile.getAbsolutePath());
        Uri imageUri = Uri.fromFile(file);

        Glide.with(mContext)
            .load(imageUri)
            .into(holder.imageView);

        //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        //holder.imageView.setImageBitmap(myBitmap);
        holder.ivItemPlay.setVisibility(View.GONE);
      }


      //Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
      //holder.imageView.setImageBitmap(myBitmap);
    }

    holder.tvCaption.setText(instaImage.get_caption());

    holder.tvRepost.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        //send intent to instagram app
        String postFileName= imageList.get(imageList.size()-1-position).get_name();

        Intent instagram = new Intent(android.content.Intent.ACTION_SEND);
        instagram.setType("image/*");
        instagram.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/InstantInsta/"+postFileName));
        instagram.setPackage("com.instagram.android");
        mContext.startActivity(instagram);

      }
    });

    holder.tvDelete.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        alertDialogBuilder.setTitle(R.string.app_name)
            .setMessage("Are you sure?")
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {

                dbcon.deleteInstaImage(imageList.get(imageList.size()-position-1));

                //delete from phone
                deleteImage( imageList.get(imageList.size()-1-position).get_phoneImageURL());

                imageList.remove(imageList.size()-position-1);
                notifyDataSetChanged();
              }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
              }
            })
            .show();
      }
    });

    holder.ivSettings.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {

        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.image_setting, popup.getMenu());


        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          @Override public boolean onMenuItemClick(MenuItem item) {

            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip;
            String postURL;
            switch (item.getItemId()) {
              case R.id.menu_copyUrl:
                postURL = imageList.get(imageList.size()-1-position).get_instaImageURL();
                clip = ClipData.newPlainText("URL", postURL);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext,"Post Url:"+postURL,Toast.LENGTH_LONG).show();
                return true;
              case R.id.menu_caption:

                String postCaption= imageList.get(imageList.size()-1-position).get_caption();
                clip = ClipData.newPlainText("Caption", postCaption);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(mContext,"Post Caption: "+postCaption,Toast.LENGTH_LONG).show();

                return true;
              case R.id.menu_share:
                final Intent shareIntent = new Intent(Intent.ACTION_SEND);

                shareIntent.setType("image/jpg");
                String postFileName= imageList.get(imageList.size()-1-position).get_name();
                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file:///sdcard/InstantInsta/"+postFileName));
                mContext.startActivity(Intent.createChooser(shareIntent, "Share image using"));
                return true;

              case R.id.menu_post_insta:

                Intent webActivity= new Intent(mContext, WebViewActivity.class);

                postURL= imageList.get(imageList.size()-1-position).get_instaImageURL();
                webActivity.putExtra("POST_URL", postURL);
                mContext.startActivity(webActivity);
                return true;
              default:
                return false;
            }
          }
        });

        popup.show();

      }
    });

    holder.imageView.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);

        String postFileName= imageList.get(imageList.size()-1-position).get_name();

        intent.setDataAndType(Uri.parse("file:///sdcard/InstantInsta/"+postFileName), "image/*");
        mContext.startActivity(intent);
      }
    });


  }

  @Override public int getItemCount() {
    return imageList.size();
  }

  public class ItemViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView,ivSettings, ivItemPlay;
    TextView tvCaption,tvRepost, tvDelete;

    public ItemViewHolder(View itemView) {
      super(itemView);
      imageView = (ImageView) itemView.findViewById(R.id.imageView);
      tvCaption = (TextView) itemView.findViewById(R.id.tvCaption);
      tvRepost = (TextView) itemView.findViewById(R.id.tvRepost);
      tvDelete = (TextView) itemView.findViewById(R.id.tvDelete);

      ivSettings= (ImageView) itemView.findViewById(R.id.ivsettings);
      ivItemPlay = (ImageView) itemView.findViewById(R.id.ivItemPlay);


    }
  }


  public void deleteImage(String filePath) {
    File fdelete = new File(filePath);
    if (fdelete.exists()) {
      if (fdelete.delete()) {
        Log.e("-->", "file Deleted :" + filePath);
        callBroadCast();
      } else {
        Log.e("-->", "file not Deleted :" + filePath);
      }
    }
  }


  // to make gallery aware of the new image
  public void callBroadCast() {
    if (Build.VERSION.SDK_INT >= 14) {
      Log.e("-->", " >= 14");
      MediaScannerConnection.scanFile(mContext, new String[]{ Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
        /*
         *   (non-Javadoc)
         * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
         */
        public void onScanCompleted(String path, Uri uri) {
          Log.e("ExternalStorage", "Scanned " + path + ":");
          Log.e("ExternalStorage", "-> uri=" + uri);
        }
      });
    } else {
      Log.e("-->", " < 14");
      mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
          Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }
  }

  public void onRefreshh(){
      imageList= dbcon.getAllInstaImages();
    notifyDataSetChanged();
  }
}
