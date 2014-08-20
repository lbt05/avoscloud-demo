package com.avoscloud.beijing.push.demo.keepalive.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import com.avoscloud.beijing.push.demo.keepalive.util.RecordUtil.FileCallback;

import android.os.AsyncTask;

public class DownloadTask extends AsyncTask<String, Integer, File> {

  FileCallback callback;

  public DownloadTask(FileCallback callback) {
    this.callback = callback;
  }

  @Override
  protected File doInBackground(String... params) {
    if (params.length == 2) {
      String urlStr = params[0];
      String type = params[1];
      // 这里是拿缓存文件
      File cacheFile = RecordUtil.getFile(urlStr, type);
      if (cacheFile.exists()) {
        return cacheFile;
      }
      URLConnection connection = null;
      InputStream input = null;
      ByteArrayOutputStream memOutput = null;
      try {
        URL url = new URL(urlStr);
        connection = url.openConnection();
        input = new BufferedInputStream(connection.getInputStream());
        memOutput = new ByteArrayOutputStream();

        byte data[] = new byte[8192];
        int count;
        while ((count = input.read(data)) != -1 && !isCancelled()) {
          memOutput.write(data, 0, count);
        }
        memOutput.flush();
        return RecordUtil.writeToFile(urlStr, type, memOutput.toByteArray());
      } catch (Exception e) {}
    }
    return null;
  }

  @Override
  public void onPostExecute(File file) {
    callback.onFileReady(file, null);
  }

}
