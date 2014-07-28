package com.avoscloud.beijing.push.demo.keepalive;

import android.text.TextUtils;
import android.util.Log;
import com.avos.avoscloud.Signature;
import com.avos.avoscloud.SignatureFactory;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by nsun on 5/20/14.
 * 
 * 本方法仅仅是签名的示范代码，仅用于介绍签名方法，请一定不要实际运用在生产环境中间
 * 
 * 如有因为直接使用本段代码到生产环境中间，产生的安全隐患和后续的安全问题，本公司概不负责！！！
 * 
 */
public class KeepAliveSignatureFactory implements SignatureFactory {

  private static final String TAG = KeepAliveSignatureFactory.class.getCanonicalName();

  private final String appId;

  private final String selfPeerId;

  private static final String SUPER_POWER_KEY = "Your app master key";

  public KeepAliveSignatureFactory(String appId, String selfPeerId) {
    this.appId = appId;
    this.selfPeerId = selfPeerId;
  }

  @Override
  public Signature createSignature(String peerId, List<String> watchIds) {
    Signature s = new Signature();

    long timestamp = System.currentTimeMillis() / 1000;
    String nonce = "ForeverAlone";
    List<String> watchIdsCopy = new ArrayList<String>();
    watchIdsCopy.addAll(watchIds);

    s.setTimestamp(timestamp);
    s.setNonce(nonce);
    s.setSignedPeerIds(watchIdsCopy);

    List<String> signatureElements = new ArrayList<String>();
    signatureElements.add(appId);

    signatureElements.add(selfPeerId);


    Collections.sort(watchIdsCopy);
    signatureElements.add(TextUtils.join(":", watchIdsCopy));

    signatureElements.add(String.valueOf(timestamp));

    signatureElements.add(nonce);
    /*
     * 此处请一定不要直接复制黏贴到您的代码中
     * 
     * 在真实的产品中间，请尽量使用服务器端数据，完成这一步signature赋值操作，以保证服务器安全性
     */
    String sig = hmacSHA1(TextUtils.join(":", signatureElements), SUPER_POWER_KEY);
    s.setSignature(sig);

    return s;
  }

  private String hmacSHA1(String msg, String key) {
    Log.d(TAG, msg);
    String result = null;
    try {
      // get an hmac_sha1 key from the raw key bytes
      SecretKeySpec signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1");

      // get an hmac_sha1 Mac instance and initialize with the signing key
      Mac mac = Mac.getInstance("HmacSHA1");
      mac.init(signingKey);

      // compute the hmac on input data bytes
      byte[] rawHmac = mac.doFinal(msg.getBytes());

      result = bytesToHex(rawHmac);

    } catch (Exception e) {}
    return result;
  }

  final protected static char[] hexArray = "0123456789abcdef".toCharArray();

  private static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  @Override
  public Signature createGroupSignature(String groupId, String peerId, List<String> targetPeerIds,
      String action) {
    Signature s = new Signature();

    long timestamp = System.currentTimeMillis() / 1000;
    String nonce = "ForeverAlone";
    List<String> watchIdsCopy = new ArrayList<String>();
    watchIdsCopy.addAll(targetPeerIds);

    s.setTimestamp(timestamp);
    s.setNonce(nonce);
    s.setSignedPeerIds(watchIdsCopy);

    List<String> signatureElements = new ArrayList<String>();
    signatureElements.add(appId);

    signatureElements.add(selfPeerId);


    Collections.sort(watchIdsCopy);

    signatureElements.add(groupId);

    signatureElements.add(action);

    signatureElements.add(TextUtils.join(":", watchIdsCopy));

    signatureElements.add(String.valueOf(timestamp));

    signatureElements.add(nonce);

    /*
     * 此处请一定不要直接复制黏贴到您的代码中
     * 
     * 在真实的产品中间，请尽量使用服务器端数据，完成这一步signature赋值操作，以保证服务器安全性
     */
    String sig = hmacSHA1(TextUtils.join(":", signatureElements), SUPER_POWER_KEY);
    s.setSignature(sig);

    return s;
  }

}
