package com.eggie5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import org.apache.commons.codec.binary.Base64;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;

public class post_to_eggie5 extends Activity {
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();

		// final String mimeType = intent.getType();
		String action = intent.getAction();

		Log.i("asf", "asdf");

		if (Intent.ACTION_SEND.equals(action)) {
			if (extras.containsKey(Intent.EXTRA_STREAM)) {
				Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);

				Toast toast = Toast.makeText(this, "!!Image path: " + uri,
						Toast.LENGTH_SHORT);
				toast.show();

				try {
					final InputStream is = getContentResolver()
							.openInputStream(uri);
					final AssetFileDescriptor assetFileDescriptor = getContentResolver()
							.openAssetFileDescriptor(uri, "r");
					final int totalFileLength = (int) assetFileDescriptor
							.getLength();
					assetFileDescriptor.close();

					byte[] b = getBytesFromFile(uri.toString(),
							totalFileLength, is);

					// String data = android.util.Base64.encodeToString(b);

					byte[] image_bytes = Base64.encodeBase64(b);

					String data_string = new String(image_bytes);

					sendpic(data_string);
					return;
				} catch (Exception e) {
					Log.e("asdf", e.toString());
				}

			} else if (extras.containsKey(Intent.EXTRA_TEXT)) {
				// mWorkingMessage.setText(extras.getString(Intent.EXTRA_TEXT));
				return;
			}
		}

	}

	private void sendpic(String data_string) {

		try {
			String xmldata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<photo><photo>" + data_string + "</photo><caption>via android</caption></photo>";

			// Create socket
			String hostname = "eggie5.com";
			String path = "/photos";
			int port = 80;
			InetAddress addr = InetAddress.getByName(hostname);
			Socket sock = new Socket(addr, port);

			// Send header
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
					sock.getOutputStream(), "UTF-8"));
			wr.write("POST " + path + " HTTP/1.1\r\n");
			wr.write("Host: eggie5.com\r\n");
			wr.write("Content-Length: " + xmldata.length() + "\r\n");
			wr.write("Content-Type: text/xml; charset=\"utf-8\"\r\n");
			wr.write("Accept: text/xml\r\n");
			wr.write("\r\n");

			// Send data
			wr.write(xmldata);
			wr.flush();

			// Response
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					sock.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				Log.v("eggie5", line);
				System.out.println(line);
			}
		} catch (Exception e) {
			Log.e(this.getClass().getName(), "Upload failed", e);
		}

	}

	public static byte[] getBytesFromFile(String uri, Integer length,
			InputStream is) {
		try {

			if (length > Integer.MAX_VALUE) {
				// File is too large
			}
			byte[] bytes = new byte[(int) length];

			// Read in the bytes
			int offset = 0;
			int numRead = 0;
			while (offset < bytes.length
					&& (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
				offset += numRead;
			}

			// Ensure all the bytes have been read in
			if (offset < bytes.length) {
				throw new IOException("Could not completely read file " + uri);
			}

			// Close the input stream and return bytes
			is.close();
			return bytes;
		} catch (Exception e) {
			Log.e("eggie5", " get file bytes didn't work!!!! - " + e.toString());
			System.out.println(e);
			return null;
		}
	}

}