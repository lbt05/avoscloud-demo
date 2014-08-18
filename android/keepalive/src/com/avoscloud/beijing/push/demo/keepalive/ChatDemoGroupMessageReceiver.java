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
import com.avos.avoscloud.AVGroupMessageReceiver;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.LogUtil;
import com.avos.avoscloud.Session;
import com.avos.avospush.notification.NotificationCompat;
import com.avoscloud.beijing.push.demo.keepalive.data.ChatDemoMessage;

public class ChatDemoGroupMessageReceiver extends AVGroupMessageReceiver {

  @Override
  public void onJoined(Context context, Group group) {
    LogUtil.avlog.d(group.getGroupId() + " Joined");
    Intent i = new Intent(context, GroupChatActivity.class);
    i.putExtra(GroupChatActivity.DATA_EXTRA_SINGLE_DIALOG_TARGET, group.getGroupId());
    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    context.startActivity(i);
  }

  @Override
  public void onInvited(Context context, Group group, String byPeerId) {
    LogUtil.avlog.d("you're invited to " + group.getGroupId() + " by " + byPeerId);
  }

  @Override
  public void onKicked(Context context, Group group, String byPeerId) {
    LogUtil.avlog.d("you're kicked from " + group.getGroupId() + " by " + byPeerId);
  }

  @Override
  public void onMessageSent(Context context, Group group, AVMessage message) {
    LogUtil.avlog.d(message.getMessage() + " sent");
  }

  @Override
  public void onMessageFailure(Context context, Group group, AVMessage message) {
    LogUtil.avlog.d(message.getMessage() + " failure");
  }

  @Override
  public void onMessage(Context context, Group group, AVMessage msg) {
    JSONObject j = JSONObject.parseObject(msg.getMessage());
    ChatDemoMessage message = new ChatDemoMessage();
    MessageListener listener = groupMessageDispatchers.get(group.getGroupId());
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
        Intent resultIntent = new Intent(context, GroupChatActivity.class);
        resultIntent
            .putExtra(GroupChatActivity.DATA_EXTRA_SINGLE_DIALOG_TARGET, group.getGroupId());
        resultIntent.putExtra(Session.AV_SESSION_INTENT_DATA_KEY, JSON.toJSONString(message));
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);

        PendingIntent pi =
            PendingIntent.getActivity(context, -1, resultIntent, PendingIntent.FLAG_ONE_SHOT);

        Notification notification =
            new NotificationCompat.Builder(context)
                .setContentTitle(context.getString(R.string.notif_group))
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
    LogUtil.avlog.d(message + " receiver");
  }

  @Override
  public void onQuit(Context context, Group group) {
    LogUtil.avlog.d(group.getGroupId() + " quit");
  }

  @Override
  public void onReject(Context context, Group group, String op, List<String> targetIds) {
    LogUtil.avlog.d(op + ":" + targetIds + " rejected");
  }

  @Override
  public void onMemberJoin(Context context, Group group, List<String> joinedPeerIds) {
    LogUtil.avlog.d(joinedPeerIds + " join " + group.getGroupId());

  }

  @Override
  public void onMemberLeft(Context context, Group group, List<String> leftPeerIds) {
    LogUtil.avlog.d(leftPeerIds + " left " + group.getGroupId());
  }

  @Override
  public void onError(Context context, Group group, Throwable e) {
    LogUtil.log.e("", (Exception) e);
  }

  public static void registerGroupListener(String groupId, MessageListener listener) {
    groupMessageDispatchers.put(groupId, listener);
  }

  public static void unregisterGroupListener(String groupId) {
    groupMessageDispatchers.remove(groupId);
  }

  static HashMap<String, MessageListener> groupMessageDispatchers =
      new HashMap<String, MessageListener>();
}
