package com.qy.sp.fee.common.utils;

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

public class DesUtil {

    private static final char[] Digit = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static String ENCRYPT_ALGORITHM = "DES/ECB/NoPadding";

    private Cipher encryptor = null;
    private Cipher decryptor = null;
    private String key;

    public void setKey(String password) {
        SecureRandom random = new SecureRandom();
        DESKeySpec desKey;
        try {
            this.key = password;
            desKey = new DESKeySpec(password.getBytes());
            // 创建一个密匙工厂，然后用它把DESKeySpec转换成
            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            SecretKey securekey = keyFactory.generateSecret(desKey);
            // Cipher对象实际完成加密操作
            encryptor = Cipher.getInstance(ENCRYPT_ALGORITHM);
            // 用密匙初始化Cipher对象
            encryptor.init(Cipher.ENCRYPT_MODE, securekey, random);
            decryptor = Cipher.getInstance(ENCRYPT_ALGORITHM);
            decryptor.init(Cipher.DECRYPT_MODE, securekey, random);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getKey() {
        return this.key;
    }

    private String getHEXfromString(byte[] result) {
        int len = result.length;
        char str[] = new char[len * 2];
        int k = 0;
        for (int i = 0; i < len; i++) {
            byte byte0 = result[i];
            str[k++] = Digit[byte0 >>> 4 & 0xf];
            str[k++] = Digit[byte0 & 0xf];
        }
        return new String(str);
    }

    private byte[] hexStr2ByteArr(String hexStr) {
        int iLen = hexStr.length();

        byte[] arrOut = new byte[iLen / 2];
        for (int i = 0; i < iLen; i = i + 2) {
            String strTmp = hexStr.substring(i, i + 2);
            arrOut[i / 2] = (byte) Integer.parseInt(strTmp, 16);
        }
        return arrOut;
    }

    /**
     * @param src
     * @return
     */
    public String Encode(String src) {
        try {
            byte[] raw = src.getBytes();
            byte[] in = null;
            if (raw.length % 8 > 0) {
                int newlen = 8 * (raw.length / 8) + 8;
                in = Arrays.copyOf(raw, newlen);
            } else {
                in = raw;
            }

            byte[] e = encryptor.doFinal(in);
            return getHEXfromString(e);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param src
     * @return 解密后数据
     */
    public String Decode(String src) {
        try {
            byte[] raw = hexStr2ByteArr(src);
            byte[] decryResult = decryptor.doFinal(raw);
            return new String(decryResult).trim();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
