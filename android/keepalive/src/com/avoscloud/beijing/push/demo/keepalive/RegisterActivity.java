package com.avoscloud.beijing.push.demo.keepalive;

import java.util.LinkedList;
import java.util.List;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVUser;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.Session;
import com.avos.avoscloud.SessionManager;
import com.avos.avoscloud.SignUpCallback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends Activity {

  EditText username;
  EditText password;
  EditText confirmPassword;
  Button join;
  View progress;
  volatile boolean isRegistering = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.register);
    username = (EditText) findViewById(R.id.register_username);
    password = (EditText) findViewById(R.id.register_passowrd);
    confirmPassword = (EditText) findViewById(R.id.register_confirm_passowrd);
    join = (Button) findViewById(R.id.register_button);
    progress = findViewById(R.id.register_loading);

    join.setOnClickListener(new View.OnClickListener() {

      @Override
      public void onClick(View v) {
        String usernameStr = username.getText().toString().trim();
        String passwordStr = password.getText().toString().trim();
        String confirmPasswordStr = confirmPassword.getText().toString().trim();

        if (AVUtils.isBlankString(usernameStr)) {
          username.setError("用户名怎么是空的！");
          username.requestFocus();
          return;
        }

        if (AVUtils.isBlankString(passwordStr)) {
          password.setError("密码不能是空的！");
          password.requestFocus();
          return;
        }

        if (AVUtils.isBlankString(confirmPasswordStr)) {
          confirmPassword.setError("密码很重要，所以要输两遍");
          confirmPassword.requestFocus();
          return;
        }
        if (!passwordStr.equals(confirmPasswordStr)) {
          confirmPassword.setError("确认密码按错了吧");
          confirmPassword.requestFocus();
          return;
        }

        AVUser newUser = new AVUser();
        newUser.setUsername(usernameStr);
        newUser.setPassword(passwordStr);
        progress.setVisibility(View.VISIBLE);
        join.setVisibility(View.GONE);
        isRegistering = true;
        newUser.signUpInBackground(new SignUpCallback() {

          @Override
          public void done(AVException e) {
            progress.setVisibility(View.GONE);
            join.setVisibility(View.VISIBLE);
            isRegistering = false;
            if (e != null) {
              password.setText("");
              confirmPassword.setText("");
              password.setError(e.getMessage());
            } else {
              final String selfId = AVUser.getCurrentUser().getObjectId();
              List<String> peerIds = new LinkedList<String>();
              Session session = SessionManager.getInstance(selfId);
              session.setSignatureFactory(new KeepAliveSignatureFactory(AVOSCloud.applicationId,
                  selfId));
              session.open(selfId, peerIds);
            }
          }
        });
      }
    });
  }


  @Override
  public void onBackPressed() {
    if (isRegistering) {
      join.setVisibility(View.VISIBLE);
      progress.setVisibility(View.GONE);
      return;
    }
    this.startActivity(new Intent(this, MainActivity.class));
    this.finish();
  }
}
