package com.avoscloud.beijing.push.demo.keepalive.data;

import java.util.HashMap;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVMessage;
import com.avos.avoscloud.AVUtils;

public class ChatDemoMessage {
  public enum MessageType {
    Status(0), Text(1), Image(2), Audio(3);

    private final int type;

    MessageType(int type) {
      this.type = type;
    }

    public int getType() {
      return this.type;
    }
  }

  MessageType messageType;
  String messageContent;
  String messageFrom;
  private String localPath;
  AVMessage internalMessage;

  public ChatDemoMessage() {
    internalMessage = new AVMessage();
  }

  public MessageType getMessageType() {
    return messageType;
  }

  public void setMessageType(MessageType messageType) {
    this.messageType = messageType;
  }

  public String getMessageContent() {
    return messageContent;
  }

  public void setMessageContent(String messageContent) {
    this.messageContent = messageContent;
  }

  public String getMessageFrom() {
    return messageFrom;
  }

  public void setMessageFrom(String messageFrom) {
    this.messageFrom = messageFrom;
  }

  public String getGroupId() {
    return this.internalMessage.getGroupId();
  }

  public void setGroupId(String groupId) {
    this.internalMessage.setGroupId(groupId);
  }

  public void setToPeerIds(List<String> peerIds) {
    this.internalMessage.setToPeerIds(peerIds);
  }

  public List<String> getToPeerIds() {
    return this.internalMessage.getToPeerIds();
  }

  public String getLocalPath() {
    return localPath;
  }

  public void setLocalPath(String localPath) {
    this.localPath = localPath;
  }

  public void fromAVMessage(AVMessage message) {
    this.internalMessage = message;
    if (!AVUtils.isBlankString(internalMessage.getMessage())) {
      HashMap<String, Object> params =
          JSON.parseObject(internalMessage.getMessage(), HashMap.class);
      this.messageContent = (String) params.get("content");
      this.messageFrom = (String) params.get("dn");
      this.messageType = (MessageType.valueOf((String) params.get("type")));
    }
  }

  public AVMessage makeMessage() {
    HashMap<String, Object> params = new HashMap<String, Object>();
    params.put("type", this.messageType);
    params.put("content", this.messageContent);
    params.put("dn", this.messageFrom);
    internalMessage.setMessage(JSON.toJSONString(params));
    return internalMessage;
  }

}
