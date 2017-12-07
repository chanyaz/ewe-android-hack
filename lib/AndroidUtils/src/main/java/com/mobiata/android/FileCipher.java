package com.mobiata.android;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class FileCipher {

	// Some of this code is used from http://code.google.com/p/secrets-for-android/
	// It is under Apache 2.0 license.

	// The salt can be hardcoded, because the secrets file is never transmitted
	// off the phone.  Generating a random salt would not provide any real extra
	// protection, because if an attacker can get to the secrets file, then he
	// has broken into the phone, and therefore would be able to get to the
	// random salt too.
	private static final byte[] SALT = { (byte) 0xA4, (byte) 0x0B, (byte) 0xC8, (byte) 0x34, (byte) 0xD6, (byte) 0x95,
			(byte) 0xF3, (byte) 0x12 };

	private static final int KEY_ITERATION_COUNT = 100;

	private static final int KEY_LENGTH = 32;

	private static final String KEY_FACTORY = "PBEWITHSHA-256AND256BITAES-CBC-BC";

	private boolean mInitialized;
	private Cipher mEncryptCipher;
	private Cipher mDecryptCipher;

	public FileCipher(String password) {
		mInitialized = false;
		try {
			PBEKeySpec keyspec = new PBEKeySpec(password.toCharArray(), SALT, KEY_ITERATION_COUNT, KEY_LENGTH);
			SecretKeyFactory skf = SecretKeyFactory.getInstance(KEY_FACTORY);
			SecretKey key = skf.generateSecret(keyspec);
			AlgorithmParameterSpec aps = new PBEParameterSpec(SALT, KEY_ITERATION_COUNT);

			mEncryptCipher = Cipher.getInstance(KEY_FACTORY);
			mEncryptCipher.init(Cipher.ENCRYPT_MODE, key, aps);

			mDecryptCipher = Cipher.getInstance(KEY_FACTORY);
			mDecryptCipher.init(Cipher.DECRYPT_MODE, key, aps);

			mInitialized = true;
		}
		catch (Exception e) {
			Log.w(Params.LOGGING_TAG, "Could not create encryption/decryption ciphers.", e);
		}
	}

	public boolean isInitialized() {
		return mInitialized;
	}

	public boolean saveSecureData(File file, String data) {
		if (!mInitialized) {
			Log.w(Params.LOGGING_TAG, "File cipher is not initialized.");
			return false;
		}

		File parent = file.getParentFile();
		File temp = new File(parent, "tmp");

		// Write data to a temporary file
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new OutputStreamWriter(new CipherOutputStream(
					new FileOutputStream(temp), mEncryptCipher)));
			writer.write(data);
			writer.close();
		}
		catch (IOException e) {
			Log.e(Params.LOGGING_TAG, "Could not save secure data.", e);
			return false;
		}
		finally {
			try {
				if (writer != null) {
					writer.close();
				}
			}
			catch (IOException e) {
				Log.e(Params.LOGGING_TAG, "Error opening the writer for saving secure data.");
				return false;
			}
		}

		// Move temp file to actual file location
		return temp.renameTo(file);
	}

	public String loadSecureData(File file) {
		if (!mInitialized) {
			Log.w(Params.LOGGING_TAG, "File cipher is not initialized.");
			return null;
		}

		BufferedReader in = null;
		try {
			in = new BufferedReader(new InputStreamReader(new CipherInputStream(
					new FileInputStream(file), mDecryptCipher)));
			StringBuffer sb = new StringBuffer();
			String line;
			while ((line = in.readLine()) != null) {
				sb.append(line);
			}
			return sb.toString();
		}
		catch (IOException e) {
			Log.e(Params.LOGGING_TAG, "Could not load secure data.", e);
			return null;
		}
		finally {
			try {
				if (in!=null) {
					in.close();
				}
			}
			catch (IOException e) {
				Log.e(Params.LOGGING_TAG, "Error opening BufferReader to load secure data.");
			}
		}
	}
}
