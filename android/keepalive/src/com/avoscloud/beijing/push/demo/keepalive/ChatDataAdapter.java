package com.avoscloud.beijing.push.demo.keepalive;

import java.io.File;
import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVUtils;
import com.avoscloud.beijing.push.demo.keepalive.data.ChatDemoMessage;
import com.avoscloud.beijing.push.demo.keepalive.data.ChatDemoMessage.MessageType;
import com.avoscloud.beijing.push.demo.keepalive.util.DownloadTask;
import com.avoscloud.beijing.push.demo.keepalive.util.RecordUtil;
import com.avoscloud.beijing.push.demo.keepalive.util.RecordUtil.FileCallback;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Bitmap;

public class ChatDataAdapter extends BaseAdapter {

  List<ChatDemoMessage> messages;
  Context mContext;
  DisplayImageOptions options;

  public ChatDataAdapter(Context context, List<ChatDemoMessage> messages) {
    this.messages = messages;
    this.mContext = context;

    options =
        new DisplayImageOptions.Builder().showImageOnLoading(R.drawable.ic_stub)
            .showImageForEmptyUri(R.drawable.ic_empty).showImageOnFail(R.drawable.ic_empty)
            .cacheInMemory(true).cacheOnDisk(true).considerExifParams(true)
            .bitmapConfig(Bitmap.Config.RGB_565).build();

  }

  @Override
  public int getCount() {

    return messages.size();
  }

  @Override
  public ChatDemoMessage getItem(int position) {
    return messages.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public int getViewTypeCount() {
    return 4;
  }

  @Override
  public int getItemViewType(int position) {
    return messages.get(position).getMessageType().getType();
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder = null;
    final ChatDemoMessage m = getItem(position);
    switch (getItemViewType(position)) {
      case 0:
        if (convertView == null) {
          convertView = LayoutInflater.from(mContext).inflate(R.layout.item_info, null);
          holder = new ViewHolder();
          holder.message = (TextView) convertView.findViewById(R.id.avoscloud_chat_demo_info);
          convertView.setTag(holder);
        } else {
          holder = (ViewHolder) convertView.getTag();
        }
        holder.message.setText(m.getMessageContent());
        break;
      case 1:
        if (convertView == null) {
          convertView = LayoutInflater.from(mContext).inflate(R.layout.item_message, null);
          holder = new ViewHolder();
          holder.message = (TextView) convertView.findViewById(R.id.avoscloud_chat_demo_message);
          holder.username = (TextView) convertView.findViewById(R.id.avoscloud_chat_demo_user_id);
          holder.timestamp = (TextView) convertView.findViewById(R.id.avoscloud_feedback_timestamp);
          convertView.setTag(holder);
        } else {
          holder = (ViewHolder) convertView.getTag();
        }
        holder.username.setText(m.getMessageFrom());
        holder.message.setText(m.getMessageContent());
        break;
      case 2:
        if (convertView == null) {
          convertView = LayoutInflater.from(mContext).inflate(R.layout.item_pic_message, null);
          holder = new ViewHolder();
          holder.username = (TextView) convertView.findViewById(R.id.avoscloud_chat_demo_user_id);
          holder.pic = (ImageView) convertView.findViewById(R.id.avoscloud_chat_demo_pic);
          holder.timestamp = (TextView) convertView.findViewById(R.id.avoscloud_feedback_timestamp);
          convertView.setTag(holder);
        } else {
          holder = (ViewHolder) convertView.getTag();
        }
        final String displayImageUrl =
            m.getLocalPath() == null ? m.getMessageContent() : m.getLocalPath();
        ImageLoader.getInstance().displayImage(displayImageUrl, holder.pic, options);
        holder.pic.setOnClickListener(new View.OnClickListener() {

          @Override
          public void onClick(View v) {
            Intent i = new Intent(mContext, PicDetailActivity.class);
            i.setData(Uri.parse(displayImageUrl));
            mContext.startActivity(i);
          }
        });
        holder.username.setText(m.getMessageFrom());
        break;
      case 3:
        if (convertView == null) {
          convertView = LayoutInflater.from(mContext).inflate(R.layout.item_audio_message, null);
          holder = new ViewHolder();
          holder.username = (TextView) convertView.findViewById(R.id.avoscloud_chat_demo_user_id);
          holder.audio =
              (ImageView) convertView.findViewById(R.id.avoscloud_chat_demo_audio_message);
          holder.timestamp = (TextView) convertView.findViewById(R.id.avoscloud_feedback_timestamp);
          convertView.setTag(holder);
        } else {
          holder = (ViewHolder) convertView.getTag();
        }
        holder.username.setText(m.getMessageFrom());
        holder.audio.setOnClickListener(new AudioPlayListener(m.getMessageContent()));
        break;
    }
    return convertView;
  }

  public class ViewHolder {
    TextView message;
    TextView username;
    ImageView audio;
    TextView timestamp;
    ImageView pic;
  }

  public static class AudioPlayListener implements View.OnClickListener {
    String url;

    public AudioPlayListener(String url) {
      this.url = url;
    }

    @Override
    public void onClick(final View v) {
      final ImageView audioPlayView = (ImageView) v;
      audioPlayView.setImageResource(R.drawable.audio_play_animation);
      // show loading animation
      if (!AVUtils.isBlankString(url)) {
        new DownloadTask(new FileCallback() {
          @Override
          public void onFileReady(File file, AVException e) {
            if (e == null) {
              // stop loading animation
              // start playing animation
              final AnimationDrawable animation = (AnimationDrawable) audioPlayView.getDrawable();
              animation.start();
              RecordUtil.playAudioFile(file, new MediaPlayer.OnCompletionListener() {

                @Override
                public void onCompletion(MediaPlayer mp) {
                  // stop play animation
                  animation.stop();
                  audioPlayView.setImageResource(R.drawable.audio_play);
                }
              });
            } else {
              // show error tips
            }
          }
        }).execute(url, MessageType.Audio.toString());
      }
    }
  }

}
