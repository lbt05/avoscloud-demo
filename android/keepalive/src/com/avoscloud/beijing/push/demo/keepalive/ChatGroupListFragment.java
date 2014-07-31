package com.avoscloud.beijing.push.demo.keepalive;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.avos.avoscloud.Group;
import com.avos.avoscloud.SessionManager;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ChatGroupListFragment extends Fragment {

  ListView groupList;
  View joinGroup;
  String selfId;

  List<String> availableGroups;

  public static final String GROUP_TABLE_NAME = "AVOSRealtimeGroups";

  public ChatGroupListFragment() {
    super();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View rootView = inflater.inflate(R.layout.onlinelist, null);
    groupList = (ListView) rootView.findViewById(R.id.onlineList);
    joinGroup = rootView.findViewById(R.id.add_new);
    selfId = AVInstallation.getCurrentInstallation().getInstallationId();
    joinGroup.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        Group group = SessionManager.getInstance(selfId).getGroup();
        group.join();
      }
    });

    if (availableGroups == null) {
      availableGroups = new LinkedList<String>();
    }
    fetchAvailableChatGroup();
    return rootView;
  }

  private void fetchAvailableChatGroup() {
    // 此表是系统表，记载了当前应用内有多少聊天组，每个组内的peerIds
    AVQuery<AVObject> query = new AVQuery<AVObject>(GROUP_TABLE_NAME);
    query.findInBackground(new FindCallback<AVObject>() {

      @Override
      public void done(List<AVObject> parseObjects, AVException parseException) {
        if (parseException == null) {
          for (AVObject o : parseObjects) {
            availableGroups.add(o.getObjectId());
          }
          GroupListAdapter adapter = new GroupListAdapter(getActivity(), availableGroups);
          groupList.setAdapter(adapter);
          groupList.setOnItemClickListener(adapter);
        }
      }
    });
  }

  public static class GroupListAdapter extends BaseAdapter implements OnItemClickListener {

    public GroupListAdapter(Context context, List<String> users) {
      this.groups = users;
      this.mContext = context;
      random = new Random();
    }

    Context mContext;
    List<String> groups;
    Random random;

    @Override
    public int getCount() {
      return groups.size();
    }

    @Override
    public String getItem(int position) {
      // TODO Auto-generated method stub
      return groups.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      ViewHolder holder = null;
      if (convertView == null) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_chat_target, null);
        holder = new ViewHolder();
        holder.groupId = (TextView) convertView.findViewById(R.id.onlinetarget);
        holder.avatar = (ImageView) convertView.findViewById(R.id.online_icon);
        convertView.setTag(holder);
      } else {
        holder = (ViewHolder) convertView.getTag();
      }
      int avatarColor =
          Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));

      holder.avatar.setBackgroundColor(avatarColor);
      holder.groupId.setText(this.getItem(position));

      return convertView;
    }

    public class ViewHolder {
      TextView groupId;
      ImageView avatar;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long itemId) {
      String groupId = this.getItem(position);
      SessionManager.getInstance(AVInstallation.getCurrentInstallation().getInstallationId())
          .getGroup(groupId).join();
    }
  }
}
