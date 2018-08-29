package com.intranet.model;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Encryptor {
	
	private static final Logger log = LoggerFactory.getLogger(Encryptor.class.getClass());
    
	private final static String key = "muitordoodrotium"; //128 bit key
	private final static String initVector = "whataninitvector"; //16 bytes IV
	
	public static String encrypt(String value) {
		try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
            
            String response = Base64.encodeBase64String(cipher.doFinal(value.getBytes()));
            response = response.replace('/', '-');
            response = response.replace('+', '_');
            response = response.substring(0, response.length()-2);
            return response;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String encrypted) {
        try {
        	encrypted = encrypted.replace('-', '/');
        	encrypted = encrypted.replace('_', '+');
        	encrypted += "==";
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            return new String(cipher.doFinal(Base64.decodeBase64(encrypted)));
        } catch (Exception ex) {
            log.error("Encryptor: " + ex.getMessage());
        }
        return null;
    }

}