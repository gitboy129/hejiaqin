package com.customer.framework.utils.cryptor;

import com.customer.framework.utils.LogUtil;
import com.customer.framework.utils.StringUtil;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA256加密
 * 单向加密算法（不可解密）
 */
public final class SHA256 {
    private static final String TAG = "SHA256";

    /***/
    public byte[] encrypt(byte[] input) {
        if (null == input) {
            return null;
        }

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            md.update(input);
            return md.digest(input);
        } catch (NoSuchAlgorithmException e) {
            LogUtil.w(TAG, "encrypt, no SHA-256 method");
        }
        return null;
    }

    /***/
    public String encrypt2Str(byte[] input) {
        byte[] output = encrypt(input);
        if (null == output) {
            return null;
        }
        return new String(output);
    }

    /***/
    public String encrypt2Hex(byte[] input) {
        return StringUtil.bytes2Hex(encrypt(input));
    }

    /***/
    public String encrypt2Base64(byte[] input) {
        return Base64.encode(input);
    }
}
