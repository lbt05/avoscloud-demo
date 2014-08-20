package com.avoscloud.beijing.push.demo.keepalive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.SaveCallback;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;
import com.avoscloud.beijing.push.demo.keepalive.data.ChatDemoMessage;
import com.avoscloud.beijing.push.demo.keepalive.data.ChatDemoMessage.MessageType;
import com.avoscloud.beijing.push.demo.keepalive.util.RecordUtil;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.avoscloud.beijing.push.demo.keepalive.util.RecordUtil.FileCallback;

public class PrivateConversationActivity extends Activity
    implements
      OnClickListener,
      MessageListener {
  public static final String DATA_EXTRA_SINGLE_DIALOG_TARGET = "single_target_peerId";

  String targetPeerId;
  private ImageButton sendBtn;
  private ImageButton addBtn;
  private EditText composeZone;
  String currentName;
  String selfId;
  ListView chatList;
  ChatDataAdapter adapter;
  List<ChatDemoMessage> messages = new LinkedList<ChatDemoMessage>();
  Session session;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.heartbeat);
    targetPeerId = this.getIntent().getStringExtra(DATA_EXTRA_SINGLE_DIALOG_TARGET);
    this.setTitle(HTBApplication.lookupname(targetPeerId));

    // 您可以在这里读取本地的聊天记录，并且加载进来。
    // 　我们会在未来加入这些代码

    chatList = (ListView) this.findViewById(R.id.avoscloud_chat_list);
    adapter = new ChatDataAdapter(this, messages);
    chatList.setAdapter(adapter);
    sendBtn = (ImageButton) this.findViewById(R.id.sendBtn);
    addBtn = (ImageButton) this.findViewById(R.id.chatPlus);
    composeZone = (EditText) this.findViewById(R.id.chatText);
    selfId = AVInstallation.getCurrentInstallation().getInstallationId();
    currentName = HTBApplication.lookupname(selfId);
    session = SessionManager.getInstance(selfId);
    sendBtn.setOnClickListener(this);

    addBtn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
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

    if (!AVUtils.isBlankString(getIntent().getExtras()
        .getString(Session.AV_SESSION_INTENT_DATA_KEY))) {
      String msg = getIntent().getExtras().getString(Session.AV_SESSION_INTENT_DATA_KEY);
      ChatDemoMessage message = JSON.parseObject(msg, ChatDemoMessage.class);
      messages.add(message);
      adapter.notifyDataSetChanged();
    }
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
  public void onMessage(String msg) {
    ChatDemoMessage message = JSON.parseObject(msg, ChatDemoMessage.class);
    messages.add(message);
    adapter.notifyDataSetChanged();
  }
}
