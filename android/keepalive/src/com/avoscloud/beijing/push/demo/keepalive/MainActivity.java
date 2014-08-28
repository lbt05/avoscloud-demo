package com.avoscloud.beijing.push.demo.keepalive;

import java.util.LinkedList;
import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.LogInCallback;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

  private EditText nameInput;
  private EditText passwordInput;
  private TextView registerButton;
  private Button joinButton;
  private View progress;

  /**
   * Called when the activity is first created.
   */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.login);

    joinButton = (Button) findViewById(R.id.button);
    nameInput = (EditText) findViewById(R.id.chat_username);
    passwordInput = (EditText) findViewById(R.id.chat_passowrd);
    registerButton = (TextView) findViewById(R.id.chat_register);
    progress = findViewById(R.id.chat_loading);


    if (AVUser.getCurrentUser() != null && AVUser.getCurrentUser().isAuthenticated()) {
      progress.setVisibility(View.VISIBLE);
      joinButton.setVisibility(View.GONE);
      HTBApplication.registerLocalNameCache(AVUser.getCurrentUser().getObjectId(), AVUser
          .getCurrentUser().getUsername());

      if (!SessionManager.getInstance(AVUser.getCurrentUser().getObjectId()).isOpen()) {
        final String selfId = AVUser.getCurrentUser().getObjectId();
        List<String> peerIds = new LinkedList<String>();
        Session session = SessionManager.getInstance(selfId);
        session.setSignatureFactory(new KeepAliveSignatureFactory(AVOSCloud.applicationId, selfId));
        session.open(peerIds);
      } else {
        Intent intent = new Intent(this, ChatTargetActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
      }
    }

    joinButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {

        String name = nameInput.getText().toString().trim();
        if (name == null || name.trim().isEmpty()) {
          nameInput.setError("怎么连名字都没有");
          nameInput.requestFocus();
          return;
        }
        String password = passwordInput.getText().toString().trim();
        if (AVUtils.isBlankString(password)) {
          passwordInput.setError("空密码怎么登录啊");
          passwordInput.requestFocus();
          return;
        }

        progress.setVisibility(View.VISIBLE);
        joinButton.setVisibility(View.GONE);

        AVUser.logInInBackground(name, password, new LogInCallback<AVUser>() {
          @Override
          public void done(AVUser user, AVException e) {

            if (e != null) {
              passwordInput.setText("");
              passwordInput.setError(e.getMessage());
              passwordInput.requestFocus();
              passwordInput.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
              progress.setVisibility(View.GONE);
              joinButton.setVisibility(View.VISIBLE);
            } else {
              final String selfId = user.getObjectId();
              HTBApplication.registerLocalNameCache(selfId, user.getUsername());
              List<String> peerIds = new LinkedList<String>();
              Session session = SessionManager.getInstance(selfId);
              session.setSignatureFactory(new KeepAliveSignatureFactory(AVOSCloud.applicationId,
                  selfId));
              session.open(peerIds);
            }
          }
        });
      }
    });


    String predefinedName =
        PreferenceManager.getDefaultSharedPreferences(this).getString("username", null);
    if (predefinedName != null) {
      nameInput.setText(predefinedName);
    }

    registerButton.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        startActivity(new Intent(MainActivity.this, RegisterActivity.class));
      }
    });
  }
}
