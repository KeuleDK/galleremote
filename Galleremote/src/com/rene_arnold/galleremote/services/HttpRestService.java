package com.rene_arnold.galleremote.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import com.rene_arnold.galleremote.util.Utilities;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class HttpRestService {

	private static final String LOG_TAG = HttpRestService.class.getSimpleName();

	private static int READ_TIMEOUT = 10000;
	private static int CONNECT_TIMEOUT = 15000;

	private static HttpRestService instance;

	private Context context;

	private HttpRestService(Context context) {
		this.context = context;
	}

	public static HttpRestService getInstance(Context context) {
		if (instance == null) {
			instance = new HttpRestService(context);
		}
		return instance;
	}

	/**
	 * Creates a HttpURLConnection to the given action.
	 * 
	 * @param action
	 * @return
	 * @throws IOException
	 */
	private HttpURLConnection createConnection(String action) throws IOException {
		// Create connection and set parameters.
		String deviceIdentifier = getDeviceIdentifier();
		URL url = new URL("http://galleremote.rene-arnold.com/api/" + action + "?device=" + deviceIdentifier);

		Log.d(LOG_TAG, "createConnection: " + url);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(READ_TIMEOUT);
		conn.setConnectTimeout(CONNECT_TIMEOUT);
		conn.setUseCaches(false);
		return conn;
	}

	private String getDeviceIdentifier() {
		WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = manager.getConnectionInfo();
		if (info == null || info.getMacAddress() == null) {
			return "noMAC";
		} else
			return info.getMacAddress().replace(":", "");
	}

	/**
	 * make http connection Type=Get
	 * 
	 * @param action
	 * @return http result as String
	 * @throws IOException
	 */
	private String makeGetConnection(String action) throws IOException {
		String result = null;
		int errStatus = 0;
		String errMessage = null;

		HttpURLConnection conn = createConnection(action);
		try {
			conn.setRequestMethod("GET");
			conn.connect();
			int rc = conn.getResponseCode();
			result = Utilities.inputStream2string(conn.getInputStream());
			String logResult = result.length() > 110 ? result.substring(0, 100) : result;
			Log.d(LOG_TAG, "makeGetConnection result: " + rc + " : " + logResult);
		} catch (IOException e) {
			errStatus = conn.getResponseCode();
			errMessage = conn.getResponseMessage();
			Log.w(LOG_TAG, "makeGetConnection error: http-code=" + errStatus + ", exception: " + e);
		} finally {
			conn.disconnect();
		}
		if (errStatus > 0) {
			throw new IOException("HTTP-Error " + errStatus + " " + errMessage);
		}
		return result;
	}

	public List<URL> getImages() {
		try {
			List<URL> urls = new ArrayList<URL>();
			String action = "getImages.php";
			String result = makeGetConnection(action);
			JSONArray array = new JSONArray(result);
			for (int i = 0; i < array.length(); i++) {
				String value = array.getString(i);
				URL url = new URL(value);
				urls.add(url);
			}
			return urls;
		} catch (JSONException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public Long getDelay() {
		try {
			String action = "getDelay.php";
			String result = makeGetConnection(action);
			Long delay = Long.valueOf(result);
			return delay;
		} catch (IOException e) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public File downloadDocument(URL url) {

		// create the new connection
		HttpURLConnection urlConnection = null;
		try {
			urlConnection = (HttpURLConnection) url.openConnection();
			try {
				// set up some things on the connection
				urlConnection.setRequestMethod("GET");
				urlConnection.setDoOutput(true);
				// and connect!
				urlConnection.connect();
				String filePath = context.getExternalFilesDir(null).getAbsolutePath() + File.separator
						+ Long.valueOf(System.currentTimeMillis()).toString();
				File file = new File(filePath);
				FileOutputStream fileOutput = new FileOutputStream(file);
				InputStream inputStream = urlConnection.getInputStream();
				byte[] buffer = new byte[1024];
				int bufferLength = 0;
				while ((bufferLength = inputStream.read(buffer)) > 0) {
					fileOutput.write(buffer, 0, bufferLength);
				}
				fileOutput.close();
				urlConnection.disconnect();
				return file;
			} catch (MalformedURLException e) {
				Log.w(LOG_TAG, e);
			}
		} catch (IOException e) {
			Log.w(LOG_TAG, e.getMessage());
			if (urlConnection != null)
				urlConnection.disconnect();
		}
		return null;
	}
}
