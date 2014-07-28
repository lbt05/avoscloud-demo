package com.avoscloud.beijing.push.demo.keepalive;

import com.avos.avoscloud.AVInstallation;
import com.avos.avoscloud.SessionManager;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;

public class ChatTargetActivity extends FragmentActivity {
  ViewPager mPager;
  PagerAdapter adapter;
  PagerTabStrip pagerTabStrip;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    this.setContentView(R.layout.chat_collection);
    mPager = (ViewPager) findViewById(R.id.pager);
    mPager.setAdapter(new ChatTargetFragmentAdapter(getSupportFragmentManager()));
    mPager.setPageMargin(16);

    pagerTabStrip = (PagerTabStrip) findViewById(R.id.pager_tab_strip);
    pagerTabStrip.setTabIndicatorColorResource(R.color.avoscloud_tab_indicator_color);
    pagerTabStrip.setTextColor(getResources().getColor(R.color.avoscloud_tab_text_color));
  }

  @Override
  public void onBackPressed() {

    SessionManager.getInstance(AVInstallation.getCurrentInstallation().getInstallationId()).close();
    super.onBackPressed();

  }

  public class ChatTargetFragmentAdapter extends FragmentStatePagerAdapter {

    public ChatTargetFragmentAdapter(FragmentManager fm) {
      super(fm);
    }

    @Override
    public Fragment getItem(int position) {
      Fragment frag = null;
      switch (position) {
        case 0:
          // TODO;
          frag = new UserListFragment();
          break;
        case 1:
          frag = new ChatGroupListFragment();
          break;
      }
      return frag;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      String title = null;
      switch (position) {
        case 0:
          title = getResources().getString(R.string.online_users);
          break;
        case 1:
          title = getResources().getString(R.string.available_group);
          break;
      }
      return title;
    }

    @Override
    public int getCount() {
      return 2;
    }
  }
}
