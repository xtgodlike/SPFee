package com.qy.sp.fee.common.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AES {

	private static final String key = "SIGN";
	
	/**
	 * AES加密算法
	 */
	public AES() {}

	/**
	 * 加密
	 * 
	 * @param content
	 *            需要加密的内容
	 * @param keyWord
	 *            加密密钥
	 * @return byte[] 加密后的字节数组
	 */
	public static byte[] encrypt(String content, String keyWord) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(keyWord.getBytes());
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			byte[] byteContent = content.getBytes("utf-8");
			cipher.init(Cipher.ENCRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(byteContent);
			return result; // 加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param content
	 *            需要加密的内容
	 * @param password
	 *            加密密钥
	 * @return String 加密后的字符串
	 */
	public static String encrypt2str(String content,String wkey) {
		return parseByte2HexStr(encrypt(content, wkey));
	}

	/**
	 * 解密
	 * 
	 * @param content
	 *            待解密内容
	 * @param keyWord
	 *            解密密钥
	 * @return byte[]
	 */
	public static byte[] decrypt(byte[] content, String keyWord) {
		try {
			KeyGenerator kgen = KeyGenerator.getInstance("AES");
			SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG");
			secureRandom.setSeed(keyWord.getBytes());
			kgen.init(128, secureRandom);
			SecretKey secretKey = kgen.generateKey();
			byte[] enCodeFormat = secretKey.getEncoded();
			SecretKeySpec key = new SecretKeySpec(enCodeFormat, "AES");
			Cipher cipher = Cipher.getInstance("AES");// 创建密码器
			cipher.init(Cipher.DECRYPT_MODE, key);// 初始化
			byte[] result = cipher.doFinal(content);
			return result; // 加密
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param content
	 *            待解密内容(字符串)
	 * @param keyWord
	 *            解密密钥
	 * @return byte[]
	 */
	public static byte[] decrypt(String content, String keyWord) {
		return decrypt(parseHexStr2Byte(content), keyWord);
	}
	
	/**
	 * @param content
	 *            待解密内容(字符串)
	 * @param keyWord
	 *            解密密钥
	 * @return byte[]
	 */
	public static String decrypt2str(String content) {
		byte[] byt = decrypt(parseHexStr2Byte(content), key);
		return new String(byt);
	}
	
	
	/**
	 * 将二进制转换成16进制
	 * 
	 * @param buf
	 * @return String
	 */
	public static String parseByte2HexStr(byte buf[]) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < buf.length; i++) {
			String hex = Integer.toHexString(buf[i] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			sb.append(hex.toUpperCase());
		}
		return sb.toString();
	}

	/**
	 * 将16进制转换为二进制
	 * 
	 * @param hexStr
	 * @return byte[]
	 */
	public static byte[] parseHexStr2Byte(String hexStr) {
		if (hexStr.length() < 1)
			return null;
		byte[] result = new byte[hexStr.length() / 2];
		for (int i = 0; i < hexStr.length() / 2; i++) {
			int high = Integer.parseInt(hexStr.substring(i * 2, i * 2 + 1), 16);
			int low = Integer.parseInt(hexStr.substring(i * 2 + 1, i * 2 + 2),
					16);
			result[i] = (byte) (high * 16 + low);
		}
		return result;
	}

	public static void main(String[] args) {

		String SERVICEAUTHPASSWD = "qyly1234";
		// 加密
		System.out.println("加密前：" + SERVICEAUTHPASSWD);
//		System.out.println("加密后：" + encrypt2str(SERVICEAUTHPASSWD));
		System.out.println("解密后：" + decrypt2str("E4F01482B75A9A966B5E080414CCDD05"));
		
	}
}
