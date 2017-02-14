package com.esoon.utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

class JsonHttp extends
		AsyncTask<JsonHttp.Arguments, Integer, JsonHttp.Arguments>
{
	private final String TAG = "JsonHttp";
	private final int PROGRESS_STARTING = 100;
	private final int PROGRESS_READINGCERTIFICATES = 70;
	private final int PROGRESS_MERGINGCERTIFICATES = 40;
	private final int PROGRESS_INITNETWORK = 10;
	private final int PROGRESS_DONE = 0;

	@Override
	protected void onPreExecute()
	{
		// start the progress
		publishProgress(PROGRESS_STARTING);
	}

	@Override
	protected Arguments doInBackground(Arguments... params)
	{
		Log.i(TAG, "doInBackground Begin");
		Arguments args = params[0];

		HttpClient client = new DefaultHttpClient();
		
	//	请求超时
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 60000); 
	//	读取超时
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 60000);
		
		// 和GET方式一样，先将参数放入List
		LinkedList<BasicNameValuePair> urlparams = new LinkedList<BasicNameValuePair>();
		for (Map.Entry<String, Object> en : args.params.entrySet())
		{
			urlparams.add(new BasicNameValuePair(en.getKey(), en.getValue()
					.toString()));
		}
		
		try
		{
			HttpPost postMethod = new HttpPost(args.baseurl);
			postMethod.setEntity(new UrlEncodedFormEntity(urlparams, "utf-8")); // 将参数填入POST
			//postMethod.setHeader(header)																	// Entity中
			postMethod.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
			HttpResponse response = client.execute(postMethod); // 执行POST方法
			Log.i(TAG,response.getStatusLine().toString() );
			Log.i(TAG,args.baseurl );
			
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
				String data = EntityUtils.toString(response.getEntity());
				System.out.println("netdata:" + data);
				JSONTokener jsonParser = new JSONTokener(data);
				args.result = (JSONObject) jsonParser.nextValue();

			}
		} catch (Exception e)
		{
			publishProgress(PROGRESS_DONE);
			e.printStackTrace();
		}
		
		Log.i(TAG, "doInBackground End");
		return (args);
	}

	@Override
	protected void onCancelled()
	{
		// stop the progress
		publishProgress(PROGRESS_DONE);
	}

	@Override
	protected void onProgressUpdate(Integer... progress)
	{
		switch (progress[0]) {
		case PROGRESS_DONE:
		case PROGRESS_READINGCERTIFICATES:
		case PROGRESS_MERGINGCERTIFICATES:
		case PROGRESS_INITNETWORK:
		case PROGRESS_STARTING:
			break;
		}
	}

	//请求结果处理.
	@Override
	protected void onPostExecute(Arguments args)
	{
		publishProgress(PROGRESS_DONE);

		if (args == null)
			return;
		JSONObject objJson = args.result;
		
		if(args.callback !=null)
		{
			 int  resultcode = (objJson ==null)? 0 :1;
			 args.callback.NetExecutePost(objJson, resultcode, args.CommandId);
		}
		Log.i(TAG, "onPostExecute End");
	}

	public static class Arguments
	{
		public String baseurl;
		public HashMap<String, Object> params;
		public Context context;
		public JSONObject result;
		public INetRequest callback;
		public int CommandId; // 
		
		public Arguments(String baseurl, HashMap params, Context context,
				INetRequest callback, int CommandId)
		{
			this.baseurl = baseurl;
			this.params = params;
			this.context = context;
			this.callback = callback;
			this.CommandId = CommandId;
		}
	}
}
