package com.avoscloud.beijing.push.demo.keepalive.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import com.avoscloud.beijing.push.demo.keepalive.R;
import com.avoscloud.beijing.push.demo.keepalive.data.ChatDemoMessage.MessageType;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVFile;
import com.avos.avoscloud.AVOSCloud;
import com.avos.avoscloud.AVPersistenceUtils;
import com.avos.avoscloud.AVUtils;
import com.avos.avoscloud.GetDataCallback;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.view.View.OnTouchListener;

@SuppressLint("NewApi")
public class RecordUtil {

  private static final String AUDIO_FILE_DIR = "audio";

  Context mContext;
  ImageView mic;
  ImageView micWave;
  Random random;
  View processProgress;
  volatile boolean isRecording = false;
  PopupWindow popup;
  @SuppressLint("NewApi")
  private final AnimatorSet waveAnimationSet = new AnimatorSet();
  private final AnimatorSet prepareAnimationSet = new AnimatorSet();
  File targetAudioFile;
  Runnable animationTask = new Runnable() {
    @Override
    public void run() {
      if (isRecording) {
        animateMicWave(getRandomScale());
        animationHandler.postDelayed(this, 500);
      } else {
        animationHandler.removeCallbacks(this);
      }
    }
  };

  public RecordUtil(Context context) {
    this.mContext = context;
    random = new Random();
  }

  Handler animationHandler = new Handler(Looper.getMainLooper());
  private MediaRecorder mRecorder;


  public void showRecordWindows(Point p, final FileCallback callback) {
    LayoutInflater inflater = LayoutInflater.from(mContext);
    View convertView = inflater.inflate(R.layout.record_windows, null);
    popup = new PopupWindow(mContext);
    popup.setContentView(convertView);
    popup.setWidth(600);
    popup.setHeight(600);
    popup.setFocusable(true);

    popup.showAtLocation(convertView, Gravity.CENTER, p.x, p.y);

    popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

      @Override
      public void onDismiss() {
        if (targetAudioFile != null) {
          stopRecording(false, callback);
        }
      }
    });

    mic = (ImageView) convertView.findViewById(R.id.chat_mic);
    micWave = (ImageView) convertView.findViewById(R.id.mic_wave);
    processProgress = convertView.findViewById(R.id.upload_progress);

    mic.setOnTouchListener(new OnTouchListener() {

      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            isRecording = true;
            prepareMicWave(true);
            animationHandler.postDelayed(animationTask, 500);
            startRecording();
            break;
          case MotionEvent.ACTION_UP:
            isRecording = false;
            animationHandler.removeCallbacks(animationTask);
            prepareMicWave(false);
            processProgress.setVisibility(View.VISIBLE);
            stopRecording(true, callback);
            break;
        }
        return false;
      }
    });

  }

  public void prepareMicWave(boolean isStart) {
    ObjectAnimator scaleXAnimator =
        ObjectAnimator.ofFloat(micWave, "scaleX", isStart ? 2 : 1).setDuration(200);
    ObjectAnimator scaleYAnimator =
        ObjectAnimator.ofFloat(micWave, "scaleY", isStart ? 2 : 1).setDuration(200);

    if (waveAnimationSet.isRunning()) {
      waveAnimationSet.cancel();
    }
    prepareAnimationSet.play(scaleXAnimator).with(scaleYAnimator);
    prepareAnimationSet.start();
  }

  private void animateMicWave(float scale) {
    ObjectAnimator scaleXAnimator =
        ObjectAnimator.ofFloat(micWave, "scaleX", 2, scale + 2f).setDuration(200);
    ObjectAnimator scaleYAnimator =
        ObjectAnimator.ofFloat(micWave, "scaleY", 2, scale + 2f).setDuration(200);

    ObjectAnimator resetXAnimator =
        ObjectAnimator.ofFloat(micWave, "scaleX", scale + 2f, 2).setDuration(200);
    ObjectAnimator resetYAnimator =
        ObjectAnimator.ofFloat(micWave, "scaleY", scale + 2f, 2).setDuration(200);
    if (waveAnimationSet.isRunning()) {
      waveAnimationSet.cancel();
    }
    waveAnimationSet.play(scaleXAnimator).with(scaleYAnimator);
    waveAnimationSet.play(resetXAnimator).after(scaleXAnimator);
    waveAnimationSet.play(resetYAnimator).after(scaleYAnimator);
    if (isRecording) {
      waveAnimationSet.start();
    }
  }

  private float getRandomScale() {
    float scale = random.nextFloat();
    return scale;
  }

  public interface FileCallback {
    public void onFileReady(File file, AVException e);
  }


  private synchronized void startRecording() {
    targetAudioFile = RecordUtil.getRandomFile(AUDIO_FILE_DIR);
    mRecorder = new MediaRecorder();
    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
    mRecorder.setOutputFile(targetAudioFile.getAbsolutePath());
    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

    try {
      mRecorder.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }

    mRecorder.start();
  }

  private synchronized void stopRecording(boolean success, final FileCallback callback) {
    try {
      processProgress.setVisibility(View.VISIBLE);
      if (mRecorder != null) {
        mRecorder.stop();
        mRecorder.release();

      }
      if (success && callback != null) {
        callback.onFileReady(new File(targetAudioFile.getAbsolutePath()), null);
      } else {
        throw new RuntimeException("Use dimissed audio recording");
      }
    } catch (Exception e) {
      if (callback != null) {
        callback.onFileReady(null, new AVException(e));
        targetAudioFile.delete();
      } else {
        e.printStackTrace();
      }
    } finally {
      processProgress.setVisibility(View.GONE);
      mRecorder = null;
      targetAudioFile = null;
      popup.dismiss();
    }
  }

  public static File getRandomFile(String folder) {
    File dir = AVOSCloud.applicationContext.getDir("tmp", Context.MODE_PRIVATE);
    File folderFile = new File(dir, folder);
    if (!folderFile.exists()) {
      folderFile.mkdirs();
    }
    File file = new File(folderFile, AVUtils.getRandomString(16) + ".ad");
    return file;
  }

  public static File getFile(String url, String folder) {
    File dir = AVOSCloud.applicationContext.getDir(folder, Context.MODE_PRIVATE);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File file = new File(dir, AVUtils.md5(url));
    return file;
  }

  public static File writeToFile(String url, String folder, byte[] data) {
    File file = getFile(url, folder);
    FileOutputStream fileOS = null;
    try {
      fileOS = new FileOutputStream(file, true);
      fileOS.write(data, 0, data.length);
      fileOS.flush();
    } catch (Exception e) {
      return null;
    } finally {
      AVPersistenceUtils.closeQuietly(fileOS);
    }
    return file;
  }

  /**
   * 把音频文件从远端拉到本地来
   * 
   * @param remoteFile
   * @param callback
   */

  public static void getFile(AVFile remoteFile, MessageType type, final FileCallback callback) {
    File dir = AVOSCloud.applicationContext.getDir(type.toString(), Context.MODE_PRIVATE);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    final File audioFile = new File(dir, remoteFile.getObjectId());
    if (audioFile.exists()) {
      if (callback != null) {
        callback.onFileReady(audioFile, null);
      }
    } else {
      remoteFile.getDataInBackground(new GetDataCallback() {

        @Override
        public void done(byte[] data, AVException e) {
          if (e != null) {
            if (callback != null) {
              callback.onFileReady(null, new AVException(e));
            }
          } else {
            OutputStream output = null;
            try {
              output = new FileOutputStream(audioFile);
              output.write(data, 0, data.length);
              output.flush();
              if (callback != null) {
                callback.onFileReady(audioFile, null);
              }
            } catch (Exception e1) {
              if (callback != null) {
                callback.onFileReady(null, new AVException(e1));
              }
            } finally {
              AVPersistenceUtils.closeQuietly(output);
            }
          }
        }
      });
    }
  }

  public static void playAudioFile(File file, MediaPlayer.OnCompletionListener callback) {
    MediaPlayer mPlayer = new MediaPlayer();
    try {
      mPlayer.setDataSource(file.getAbsolutePath());
      mPlayer.setOnCompletionListener(callback);
      mPlayer.prepare();
      mPlayer.start();
    } catch (IOException e) {

    }
  }

  public static void moveFile(File sourceFile, String destFileName, MessageType type) {
    File dir = AVOSCloud.applicationContext.getDir(type.toString(), Context.MODE_PRIVATE);
    if (!dir.exists()) {
      dir.mkdirs();
    }
    File destFile = new File(dir, destFileName);
    sourceFile.renameTo(destFile);
  }
}
