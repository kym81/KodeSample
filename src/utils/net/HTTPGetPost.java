package utils.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.drawable.Drawable;
import android.util.Log;

public class HTTPGetPost {
	
	public static String getStringByUrl(String url) {

		StringBuilder str = new StringBuilder();
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		//Log.d("KYM", "getStringByUrl url = " + url);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			//Log.d("KYM", "getStringByUrl statusCode = " + statusCode);
			if (statusCode == 200) { // Status OK
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				BufferedReader reader = new BufferedReader(	new InputStreamReader(content));
				String line;
				while ((line = reader.readLine()) != null) {
					str.append(line);
				}
			} else {
				Log.e("Log", "Failed to download result..");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str.toString();

	}
	
	public static Drawable getDrawableByUrl(String url) {

		Drawable image = null;
		HttpClient client = new DefaultHttpClient();
		HttpGet httpGet = new HttpGet(url);
		//Log.d("KYM", "getDrawableByUrl url = " + url);
		try {
			HttpResponse response = client.execute(httpGet);
			StatusLine statusLine = response.getStatusLine();
			int statusCode = statusLine.getStatusCode();
			if (statusCode == 200) { // Status OK
				HttpEntity entity = response.getEntity();
				InputStream content = entity.getContent();
				image = Drawable.createFromStream(content, url);
				//Log.d("KYM", "getDrawableByUrl entity.getContentType().toString() = " + entity.getContentType().toString());
			} else {
			}
		} catch (ClientProtocolException e) {
			//Log.d("KYM", "getDrawableByUrl ClientProtocolException = " + e.toString());
			e.printStackTrace();
		} catch (IOException e) {
			//Log.d("KYM", "getDrawableByUrl IOException = " + e.toString());
			e.printStackTrace();
		}
		return image;

	}
}
