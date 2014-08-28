package com.avoscloud.beijing.push.demo.keepalive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;
import com.avoscloud.beijing.push.demo.keepalive.data.ChatDemoMessage;
import com.avoscloud.beijing.push.demo.keepalive.data.LocalMessageQueue;
import com.avoscloud.beijing.push.demo.keepalive.data.ChatDemoMessage.MessageType;
import com.avoscloud.beijing.push.demo.keepalive.util.RecordUtil;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.avoscloud.beijing.push.demo.keepalive.util.RecordUtil.FileCallback;

/**
 * 请妥善阅读此段代码
 * 
 * 由于此段代码仅仅是作为demo使用，在聊天记录，语音、视频、图片等信息保存本地过程中并没有进行严格的信息加密处理。如果您将此段代码复用到生产环境中，并且由此引发的信息安全问题和相关责任，
 * 本人及AVOS Cloud概不负责。请慎重使用本代码
 * 
 * @author lbt05
 * 
 */
public class PrivateConversationActivity extends Activity
    implements
      OnClickListener,
      MessageListener {
  public static final String DATA_EXTRA_SINGLE_DIALOG_TARGET = "single_target_peerId";
  private static final int IMAGE_REQUEST = 70743561;

  String targetPeerId;
  private ImageButton sendBtn;
  private ImageButton addBtn;
  View micButton;
  View picButton;
  View moreFuncView;
  private EditText composeZone;
  String currentName;
  String selfId;
  ListView chatList;
  ChatDataAdapter adapter;
  List<ChatDemoMessage> messages;
  Session session;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.heartbeat);
    targetPeerId = this.getIntent().getStringExtra(DATA_EXTRA_SINGLE_DIALOG_TARGET);
    this.setTitle(HTBApplication.lookupname(targetPeerId));
    selfId = AVUser.getCurrentUser().getObjectId();
    currentName = HTBApplication.lookupname(selfId);
    messages = new LocalMessageQueue(getPrivateConversationId(selfId, targetPeerId));
    // 您可以在这里读取本地的聊天记录，并且加载进来。
    // 　我们会在未来加入这些代码

    chatList = (ListView) this.findViewById(R.id.avoscloud_chat_list);
    adapter = new ChatDataAdapter(this, messages);
    chatList.setAdapter(adapter);

    sendBtn = (ImageButton) this.findViewById(R.id.sendBtn);
    addBtn = (ImageButton) this.findViewById(R.id.chatPlus);
    composeZone = (EditText) this.findViewById(R.id.chatText);
    micButton = this.findViewById(R.id.chat_mic_btn);
    picButton = this.findViewById(R.id.chat_pic_btn);
    moreFuncView = this.findViewById(R.id.chat_more_wrapper);

    session = SessionManager.getInstance(selfId);
    sendBtn.setOnClickListener(this);

    addBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        moreFuncView.setVisibility(View.VISIBLE);
      }
    });

    // 出现音频采集交互
    micButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        moreFuncView.setVisibility(View.GONE);
        Point p = new Point(0, 0);
        new RecordUtil(PrivateConversationActivity.this).showRecordWindows(p, new FileCallback() {
          @Override
          public void onFileReady(File file, AVException e) {
            if (e == null) {
              sendFileAsMessage(file, MessageType.Audio);
            }
          }
        });
      }
    });
    // 从外部挑选照片
    picButton.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        moreFuncView.setVisibility(View.GONE);
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_REQUEST);
      }
    });
  }

  public void sendFileAsMessage(final File file, final MessageType type) {
    try {
      final AVFile avFile = AVFile.withFile(file.getName(), file);
      avFile.saveInBackground(new SaveCallback() {

        @Override
        public void done(AVException e) {
          if (e == null) {
            ChatDemoMessage msg = new ChatDemoMessage();
            msg.setMessageType(type);
            msg.setMessageFrom(currentName);
            msg.setMessageContent(avFile.getUrl());
            msg.setToPeerIds(Arrays.asList(targetPeerId));
            messages.add(msg);
            adapter.notifyDataSetChanged();
            session.sendMessage(msg.makeMessage());
            RecordUtil.moveFile(file, AVUtils.md5(avFile.getUrl()), MessageType.Audio);
          }
        }
      });
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onClick(View v) {
    String text = composeZone.getText().toString();

    if (TextUtils.isEmpty(text)) {
      return;
    }


    composeZone.getEditableText().clear();
    ChatDemoMessage message = new ChatDemoMessage();
    message.setMessageContent(text);
    message.setMessageType(MessageType.Text);
    message.setMessageFrom(currentName);
    message.setToPeerIds(Arrays.asList(targetPeerId));
    session.sendMessage(message.makeMessage());

    messages.add(message);
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onBackPressed() {
    if (moreFuncView.getVisibility() == View.VISIBLE) {
      moreFuncView.setVisibility(View.GONE);
      return;
    }
    super.onBackPressed();
    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
  }

  @Override
  public void onResume() {
    super.onResume();
    ChatDemoMessageReceiver.registerSessionListener(targetPeerId, this);
  }

  @Override
  public void onPause() {
    super.onPause();
    ChatDemoMessageReceiver.unregisterSessionListener(targetPeerId);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {

    if (IMAGE_REQUEST == requestCode && resultCode == RESULT_OK && data.getData() != null) {
      Uri _uri = data.getData();
      String[] filePathColumn = {MediaStore.Images.Media.DATA};

      Cursor cursor = getContentResolver().query(_uri, filePathColumn, null, null, null);
      cursor.moveToFirst();

      int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
      String filePath = cursor.getString(columnIndex);
      cursor.close();
      try {
        LogUtil.avlog.d("img picked:" + filePath);
        File imgFile = new File(filePath);
        final AVFile uploadFile = AVFile.withFile(imgFile.getName(), imgFile);
        final ChatDemoMessage msg = new ChatDemoMessage();
        msg.setToPeerIds(Arrays.asList(targetPeerId));
        msg.setLocalPath("file://" + imgFile.getAbsolutePath());
        msg.setMessageFrom(currentName);
        msg.setMessageType(MessageType.Image);
        messages.add(msg);
        adapter.notifyDataSetChanged();
        uploadFile.saveInBackground(new SaveCallback() {

          @Override
          public void done(AVException e) {
            if (e == null) {
              msg.setMessageContent(uploadFile.getUrl());
              session.sendMessage(msg.makeMessage());
            }
          }
        });
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void onMessage(String msg) {
    ChatDemoMessage message = JSON.parseObject(msg, ChatDemoMessage.class);
    messages.add(message);
    adapter.notifyDataSetChanged();
  }

  public static String getPrivateConversationId(String selfId, String targetId) {
    return AVUtils.md5(selfId + ":" + targetId);
  }
}
