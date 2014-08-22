package com.avoscloud.beijing.push.demo.keepalive.data;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import android.content.Context;
import android.content.SharedPreferences;

import com.alibaba.fastjson.JSON;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUtils;

public class LocalMessageQueue implements List<ChatDemoMessage> {

  List<ChatDemoMessage> messages;
  private final String queueKey;
  private static final String MESSAGE_ZONE = "com.avoscloud.beijing.push.demo.keepalive.message";
  private static final String QUEUE_KEY = "com.avoscloud.beijing.push.demo.keepalive.message.queue";

  public LocalMessageQueue(String conversationId) {
    messages = new LinkedList<ChatDemoMessage>();
    this.queueKey = QUEUE_KEY + "." + conversationId;
    List<ChatDemoMessage> storedMessages = restoreMessageQueue();
    if (storedMessages != null && storedMessages.size() > 0) {
      messages.addAll(storedMessages);
    }
  }


  @Override
  public void clear() {
    messages.clear();
    this.storeMessageQueue();
  }

  @Override
  public boolean contains(Object object) {
    return messages.contains(object);
  }

  @Override
  public boolean containsAll(Collection<?> collection) {
    return messages.containsAll(collection);
  }

  @Override
  public boolean isEmpty() {
    return messages.isEmpty();
  }

  @Override
  public Iterator<ChatDemoMessage> iterator() {
    return messages.iterator();
  }

  @Override
  public boolean remove(Object object) {
    boolean result = messages.remove(object);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public boolean removeAll(Collection<?> collection) {
    boolean result = messages.removeAll(collection);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public boolean retainAll(Collection<?> collection) {
    boolean result = messages.retainAll(collection);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public int size() {
    return messages.size();
  }

  @Override
  public Object[] toArray() {
    return messages.toArray();
  }

  @Override
  public <T> T[] toArray(T[] array) {
    return messages.toArray(array);
  }

  @Override
  public boolean add(ChatDemoMessage e) {
    boolean result = messages.add(e);
    this.storeMessageQueue();
    return result;
  }

  private synchronized void storeMessageQueue() {
    String queueString = JSON.toJSONString(messages);
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(MESSAGE_ZONE, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.putString(queueKey, queueString);
    editor.commit();
  }

  private synchronized List<ChatDemoMessage> restoreMessageQueue() {
    List<ChatDemoMessage> storedMessages = null;
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(MESSAGE_ZONE, Context.MODE_PRIVATE);

    String queueString = settings.getString(queueKey, null);
    if (!AVUtils.isBlankString(queueString)) {
      storedMessages = JSON.parseArray(queueString, ChatDemoMessage.class);
    }
    return storedMessages;
  }

  @Override
  public void add(int location, ChatDemoMessage object) {
    // TODO Auto-generated method stub

  }

  @Override
  public ChatDemoMessage get(int location) {
    return messages.get(location);
  }

  @Override
  public int indexOf(Object object) {
    return messages.indexOf(object);
  }

  @Override
  public int lastIndexOf(Object object) {
    return messages.lastIndexOf(object);
  }

  @Override
  public ListIterator<ChatDemoMessage> listIterator() {
    return messages.listIterator();
  }

  @Override
  public ListIterator<ChatDemoMessage> listIterator(int location) {
    return messages.listIterator(location);
  }

  @Override
  public ChatDemoMessage remove(int location) {
    ChatDemoMessage result = messages.remove(location);
    this.storeMessageQueue();
    return result;
  }

  @Override
  public ChatDemoMessage set(int location, ChatDemoMessage object) {
    ChatDemoMessage element = this.messages.set(location, object);
    this.storeMessageQueue();
    return element;
  }

  /**
   * 这里由于为了防止副作用，所以做成了deep copy
   */
  @Override
  public List<ChatDemoMessage> subList(int start, int end) {
    List<ChatDemoMessage> sub = new LinkedList<ChatDemoMessage>();
    sub.addAll(messages.subList(start, end));
    return sub;
  }

  @Override
  public boolean addAll(Collection<? extends ChatDemoMessage> collection) {
    boolean result = messages.addAll(collection);
    this.storeMessageQueue();
    return result;
  }


  @Override
  public boolean addAll(int arg0, Collection<? extends ChatDemoMessage> collection) {
    boolean result = messages.addAll(arg0, collection);
    this.storeMessageQueue();
    return result;
  }

  public static void cleanAllData() {
    SharedPreferences settings =
        AVOSCloud.applicationContext.getSharedPreferences(MESSAGE_ZONE, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = settings.edit();
    editor.clear();
    editor.commit();
  }
}
