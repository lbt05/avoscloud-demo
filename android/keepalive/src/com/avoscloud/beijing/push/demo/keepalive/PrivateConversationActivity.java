package com.avoscloud.beijing.push.demo.keepalive;

import java.util.LinkedList;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

public class PrivateConversationActivity extends Activity
    implements
      OnClickListener,
      MessageListener {
  public static final String DATA_EXTRA_SINGLE_DIALOG_TARGET = "single_target_peerId";

  String targetPeerId;
  private ImageButton sendBtn;
  private EditText composeZone;
  String currentName;
  String selfId;
  ListView chatList;
  ChatDataAdapter adapter;
  List<ChatMessage> messages = new LinkedList<ChatMessage>();
  Session session;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.heartbeat);
    targetPeerId = this.getIntent().getStringExtra(DATA_EXTRA_SINGLE_DIALOG_TARGET);
    this.setTitle(HTBApplication.lookupname(targetPeerId));

    chatList = (ListView) this.findViewById(R.id.avoscloud_chat_list);
    adapter = new ChatDataAdapter(this, messages);
    chatList.setAdapter(adapter);
    sendBtn = (ImageButton) this.findViewById(R.id.sendBtn);
    composeZone = (EditText) this.findViewById(R.id.chatText);
    selfId = AVInstallation.getCurrentInstallation().getInstallationId();
    currentName = HTBApplication.lookupname(selfId);
    session = SessionManager.getInstance(selfId);
    sendBtn.setOnClickListener(this);

    if (!AVUtils.isBlankString(getIntent().getExtras()
        .getString(Session.AV_SESSION_INTENT_DATA_KEY))) {
      String msg = getIntent().getExtras().getString(Session.AV_SESSION_INTENT_DATA_KEY);
      ChatMessage message = JSON.parseObject(msg, ChatMessage.class);
      messages.add(message);
      adapter.notifyDataSetChanged();
    }
  }

  @Override
  public void onClick(View v) {
    String text = composeZone.getText().toString();

    if (TextUtils.isEmpty(text)) {
      return;
    }

    session.sendMessage(makeMessage(text), session.getAllPeers());

    composeZone.getEditableText().clear();
    ChatMessage message = new ChatMessage();
    message.setMessage(text);
    message.setType(1);
    message.setUsername(currentName);
    messages.add(message);
    adapter.notifyDataSetChanged();
  }


  private String makeMessage(String msg) {
    JSONObject obj = new JSONObject();
    obj.put("msg", msg);
    obj.put("dn", currentName);

    return obj.toJSONString();
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
    ChatMessage message = JSON.parseObject(msg, ChatMessage.class);
    messages.add(message);
    adapter.notifyDataSetChanged();
  }
}
