package com.avoscloud.beijing.push.demo.keepalive;

import java.util.HashMap;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVMessageReceiver;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.Session;
import com.avos.avospush.notification.NotificationCompat;
import com.avoscloud.beijing.push.demo.keepalive.data.ChatDemoMessage;

public class ChatDemoMessageReceiver extends AVMessageReceiver {


  @Override
  public void onSessionOpen(Context context, Session session) {

    this.sendOpenIntent(context);
  }

  @Override
  public void onSessionPaused(Context context, Session session) {
    LogUtil.avlog.d("这里掉线了");
  }

  @Override
  public void onSessionResumed(Context context, Session session) {
    LogUtil.avlog.d("重新连接上了");
  }

  @Override
  public void onMessage(Context context, Session session, AVMessage msg) {
    JSONObject j = JSONObject.parseObject(msg.getMessage());
    ChatDemoMessage message = new ChatDemoMessage();
    MessageListener listener = sessionMessageDispatchers.get(msg.getFromPeerId());
    /*
     * 这里是demo中自定义的数据格式，在你自己的实现中，可以完全自由的通过json来定义属于你自己的消息格式
     * 
     * 用户发送的消息 {"msg":"这是一个消息","dn":"这是消息来源者的名字"}
     * 
     * 用户的状态消息 {"st":"用户触发的状态信息","dn":"这是消息来源者的名字"}
     */

    if (j.containsKey("content")) {

      message.fromAVMessage(msg);
      // 如果Activity在屏幕上不是active的时候就选择发送 通知

      if (listener == null) {
        LogUtil.avlog.d("Activity inactive, about to send notification.");
        NotificationManager nm =
            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String ctnt = message.getMessageFrom() + "：" + message.getMessageContent();
        Intent resultIntent = new Intent(context, PrivateConversationActivity.class);
        resultIntent.putExtra(PrivateConversationActivity.DATA_EXTRA_SINGLE_DIALOG_TARGET,
            msg.getFromPeerId());
        resultIntent.putExtra(Session.AV_SESSION_INTENT_DATA_KEY, JSON.toJSONString(message));
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        PendingIntent pi =
            PendingIntent.getActivity(context, -1, resultIntent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification =
            new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.notif_title))
                .setContentText(ctnt)
                .setContentIntent(pi)
                .setSmallIcon(R.drawable.ic_launcher)
                .setLargeIcon(
                    BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher))
                .setAutoCancel(true).build();
        nm.notify(233, notification);
        LogUtil.avlog.d("notification sent");
      } else {
        listener.onMessage(JSON.toJSONString(message));
      }
    }
  }

  @Override
  public void onMessageSent(Context context, Session session, AVMessage msg) {
    LogUtil.avlog.d("message sent :" + msg);
  }

  @Override
  public void onMessageFailure(Context context, Session session, AVMessage msg) {
    LogUtil.avlog.d("message failed :" + msg.getMessage());
  }

  @Override
  public void onStatusOnline(Context context, Session session, List<String> peerIds) {
    LogUtil.avlog.d("status online :" + peerIds.toString());
  }

  @Override
  public void onStatusOffline(Context context, Session session, List<String> peerIds) {
    LogUtil.avlog.d("status offline :" + peerIds.toString());
  }

  @Override
  public void onError(Context context, Session session, Throwable e) {
    LogUtil.log.e("session error", (Exception) e);
  }

  private void sendOpenIntent(Context context) {
    Intent intent = new Intent(context, ChatTargetActivity.class);
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(intent);
  }

  public static void registerSessionListener(String peerId, MessageListener listener) {
    sessionMessageDispatchers.put(peerId, listener);
  }

  public static void unregisterSessionListener(String peerId) {
    sessionMessageDispatchers.remove(peerId);
  }

  static HashMap<String, MessageListener> sessionMessageDispatchers =
      new HashMap<String, MessageListener>();
}
