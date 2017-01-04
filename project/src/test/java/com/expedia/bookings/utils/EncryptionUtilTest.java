package com.expedia.bookings.utils;

import java.io.File;
import java.io.FileWriter;

import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

import android.content.Context;
import android.os.Build;
import android.util.Base64;

import com.expedia.bookings.test.robolectric.RobolectricRunner;

import okio.BufferedSource;
import okio.Okio;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

@RunWith(RobolectricRunner.class)
public class EncryptionUtilTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	private File storage;
	private File keystore;
	private TestEncryptionUtil encryptionUtil;
	private String text = "Kayne for President, 2020";
	private String delimiter = "]";

	private Context getContext() {
		return RuntimeEnvironment.application;
	}

	@Before
	public void before() throws Throwable {
		storage = folder.newFile();
		storage.delete();

		keystore = folder.newFile();
		keystore.delete();
	}

	@Test
	public void encryptStringDecryptString() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String string = encryptionUtil.encryptStringToBase64CipherText(text);
		FileWriter writer = new FileWriter(storage);
		writer.write(string, 0, string.length());
		writer.close();

		BufferedSource source = Okio.buffer(Okio.source(storage));
		String encryptedText = source.readUtf8();

		String decryptedText =  encryptionUtil.decryptStringFromBase64CipherText(encryptedText);
		assertEquals(text, decryptedText);
	}

	@Test
	public void keyPersists() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String string = encryptionUtil.encryptStringToBase64CipherText(text);
		FileWriter writer = new FileWriter(storage);
		writer.write(string, 0, string.length());
		writer.close();

		BufferedSource source = Okio.buffer(Okio.source(storage));
		String encryptedText = source.readUtf8();

		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String decryptedText =  encryptionUtil.decryptStringFromBase64CipherText(encryptedText);
		assertEquals(text, decryptedText);
	}


	@Test(expected = BadPaddingException.class)
	public void diffKeyFails() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String string = encryptionUtil.encryptStringToBase64CipherText(text);
		FileWriter writer = new FileWriter(storage);
		writer.write(string, 0, string.length());
		writer.close();

		BufferedSource source = Okio.buffer(Okio.source(storage));
		String encryptedText = source.readUtf8();

		encryptionUtil.clear();

		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "diffKey", true);
		encryptionUtil.decryptStringFromBase64CipherText(encryptedText);
	}

	@Test(expected = IllegalArgumentException.class)
	public void modifiedEncryptedTextFails() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String string = encryptionUtil.encryptStringToBase64CipherText(text);
		FileWriter writer = new FileWriter(storage);
		writer.write(string, 0, string.length());
		writer.close();

		BufferedSource source = Okio.buffer(Okio.source(storage));
		String encryptedText = source.readUtf8();
		encryptedText += "cmFuZG9tdGV4dA0K";

		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		encryptionUtil.decryptStringFromBase64CipherText(encryptedText);
	}

	@Test
	public void aesKeyLengthMatches() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		SecretKey key = encryptionUtil.generateAESKey(128);
		assertEquals(16, key.getEncoded().length);
	}

	@Test
	public void ivLengthMatches() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		byte[] key = encryptionUtil.generateIv(12);
		assertEquals(12, key.length);
	}

	@Test
	public void encryptingSameTextResultsInDiffCipher() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String string1 = encryptionUtil.encryptStringToBase64CipherText(text);
		String string2 = encryptionUtil.encryptStringToBase64CipherText(text);

		assertFalse(string1.equals(string2));
	}

	@Test
	public void cipherTextIsNotJustPlainText() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String cipherText = encryptionUtil.encryptStringToBase64CipherText(text);
		String base64Text = Base64.encodeToString(text.getBytes(), Base64.DEFAULT);

		assertFalse(cipherText.equals(base64Text));
	}

	@Test
	public void encryptionShortTextDoesNotFail() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String cipherText = encryptionUtil.encryptStringToBase64CipherText("1");
		String decryptedText = encryptionUtil.decryptStringFromBase64CipherText(cipherText);
		assertEquals("1", decryptedText);
	}

	@Test
	public void encryptionEmptyDoesNotFail() throws Throwable {
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String cipherText = encryptionUtil.encryptStringToBase64CipherText("");
		String decryptedText = encryptionUtil.decryptStringFromBase64CipherText(cipherText);
		assertEquals("", decryptedText);
	}

	@Test
	public void upgradeWorks() throws Throwable {
		ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 19);
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String encrypted = encryptionUtil.encryptStringToBase64CipherText(text);

		BufferedSource source = Okio.buffer(Okio.source(keystore));
		String encryptedText = source.readUtf8();
		String[] fields = encryptedText.split(delimiter);

		String algorithm = new String(Base64.decode(fields[1], Base64.DEFAULT));

		assertEquals("RSA/ECB/PKCS1Padding", algorithm);

		ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 23);
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		String decrypted = encryptionUtil.decryptStringFromBase64CipherText(encrypted);

		source = Okio.buffer(Okio.source(keystore));
		encryptedText = source.readUtf8();
		fields = encryptedText.split(delimiter);
		algorithm =  new String(Base64.decode(fields[1], Base64.DEFAULT));

		assertEquals(text, decrypted);
		assertEquals("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", algorithm);

		ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", 23);
		encryptionUtil = new TestEncryptionUtil(getContext(), keystore, "testAlias", true);
		decrypted = encryptionUtil.decryptStringFromBase64CipherText(encrypted);

		source = Okio.buffer(Okio.source(keystore));
		encryptedText = source.readUtf8();
		fields = encryptedText.split(delimiter);
		algorithm =  new String(Base64.decode(fields[1], Base64.DEFAULT));

		assertEquals(text, decrypted);
		assertEquals("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", algorithm);

	}
}
