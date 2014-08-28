package com.avoscloud.beijing.push.demo.keepalive;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

public class PicDetailActivity extends Activity {

  ImageView imgDetails;
  DisplayImageOptions options;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.layout_pic_details);

    this.getActionBar().setDisplayShowHomeEnabled(true);
    this.getActionBar().setDisplayHomeAsUpEnabled(true);

    imgDetails = (ImageView) this.findViewById(R.id.imageDetails);
    options =
        new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
            .showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_empty)
            .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565).build();
    Uri imageUri = this.getIntent().getData();
    ImageLoader.getInstance().displayImage(imageUri.toString(), imgDetails, options);
  }
}
