package com.rishabh.github.instagrabber.adaptor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.rishabh.github.instagrabber.R;
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
  //private DBController dbcon;


  public ImageRecyclerAdaptor() {
  }

  public ImageRecyclerAdaptor(ArrayList<InstaImage> arrayList, Context context) {
    this.imageList = arrayList;
    mContext=context;
  }

  @Override public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_image, parent,
        false);
    //DB
    //dbcon = new DBController(mContext);

    return new ItemViewHolder(view);

  }

  @Override public void onBindViewHolder(ItemViewHolder holder, int position) {
//"/storage/emulated/0/DCIM/Camera/IMG_20151102_193132.jpg"
    InstaImage instaImage=imageList.get(position);

    //first row id in sqlite is 1

    File imgFile = new File(instaImage.get_phoneImageURL());

    if(imgFile.exists()){

      Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
      holder.imageView.setImageBitmap(myBitmap);
    }
    holder.tvCaption.setText(instaImage.get_caption());

    holder.ivSettings.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {

        PopupMenu popup = new PopupMenu(mContext, view);
        MenuInflater inflater = popup.getMenuInflater();
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          @Override public boolean onMenuItemClick(MenuItem item) {


            switch (item.getItemId()) {
              case R.id.menu_copyUrl:
                return true;
              case R.id.menu_caption:
                return true;
              case R.id.menu_share:
                return true;
              default:
                return false;
            }
          }
        });
        inflater.inflate(R.menu.image_setting, popup.getMenu());
        popup.show();

      }
    });



  }

  @Override public int getItemCount() {
    return imageList.size();
  }

  public class ItemViewHolder extends RecyclerView.ViewHolder {
    ImageView imageView,ivSettings;
    TextView tvCaption;

    public ItemViewHolder(View itemView) {
      super(itemView);
      imageView = (ImageView) itemView.findViewById(R.id.imageView);
      tvCaption = (TextView) itemView.findViewById(R.id.tvCaption);

      ivSettings= (ImageView) itemView.findViewById(R.id.ivsettings);
    }
  }

}
