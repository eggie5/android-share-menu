package com.eggie5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.apache.commons.codec.binary.Base64;
import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;

public class post_to_eggie5 extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		String action = intent.getAction();

		// if this is from the share menu
		if (Intent.ACTION_SEND.equals(action))
		{
			if (extras.containsKey(Intent.EXTRA_STREAM))
			{
				try
				{
					// Get resource path from intent callee
					Uri uri = (Uri) extras.getParcelable(Intent.EXTRA_STREAM);

					// Query gallery for camera picture via
					// Android ContentResolver interface
					ContentResolver cr = getContentResolver();
					InputStream is = cr.openInputStream(uri);
					// Get binary bytes for encode
					byte[] data = getBytesFromFile(is);

					// base 64 encode for text transmission (HTTP)
					byte[] encoded_data = Base64.encodeBase64(data);
					String data_string = new String(encoded_data); // convert to
																	// string

					SendRequest(data_string);

					return;
				} catch (Exception e)
				{
					Log.e(this.getClass().getName(), e.toString());
				}

			} else if (extras.containsKey(Intent.EXTRA_TEXT))
			{
				return;
			}
		}

	}

	private void SendRequest(String data_string)
	{

		try
		{
			String xmldata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
					+ "<photo><photo>" + data_string
					+ "</photo><caption>via android - " + new Date().toString()
					+ "</caption></photo>";

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
			while ((line = rd.readLine()) != null)
			{
				Log.v(this.getClass().getName(), line);
			}

		} catch (Exception e)
		{
			Log.e(this.getClass().getName(), "Upload failed", e);
		}

	}

	public static byte[] getBytesFromFile(InputStream is)
	{
		try
		{
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1)
			{
				buffer.write(data, 0, nRead);
			}

			buffer.flush();

			return buffer.toByteArray();
		} catch (IOException e)
		{
			Log.e("com.eggie5.post_to_eggie5", e.toString());
			return null;
		}
	}

}