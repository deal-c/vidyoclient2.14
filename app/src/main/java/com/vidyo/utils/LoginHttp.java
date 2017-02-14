package com.vidyo.utils;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.os.AsyncTask;
import android.util.Log;

import com.vidyo.utils.Contants.NetCommand;

class LoginHttp extends
		AsyncTask<LoginHttp.Arguments, Integer, LoginHttp.Arguments>
{
	private final String TAG = "LoginHttp";
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
		String EntityID = "";

		String portal = "http://";

		Arguments args = params[0];
		String urlString = String
				.format("%1$s/services/v1_1/VidyoPortalUserService/",
						args.portalString);
		portal += urlString;
		Log.d(TAG, "Sending request to " + portal);

		String SOAPRequestXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://portal.vidyo.com/user/v1_1\">"
				+ "<soapenv:Header/>"
				+ "<soapenv:Body>"
				+ "<v1:MyAccountRequest/>"
				+ "</soapenv:Body>"
				+ "</soapenv:Envelope>";
		Log.d(TAG, "SOAP Request = " + SOAPRequestXML);

		String msgLength = String.format("%1$d", SOAPRequestXML.length());
		publishProgress(PROGRESS_INITNETWORK);

		try
		{
			

			
			HttpPost httppost = new HttpPost(portal);
			StringEntity se = new StringEntity(SOAPRequestXML, HTTP.UTF_8);

			se.setContentType("text/xml");
			httppost.setHeader("Content-Type", "text/xml;charset=UTF-8");
			httppost.setHeader("SOAPAction", "\"myAccount\"");
			String auth = "Basic "
					+ android.util.Base64.encodeToString(
							(args.userString + ":" + args.passwordString)
									.getBytes(), android.util.Base64.NO_WRAP);
			httppost.setHeader("Authorization", auth);
			httppost.setEntity(se);

			Log.i(TAG,"===="+ args.userString + ":" + args.passwordString);
			
			HttpClient httpclient = new DefaultHttpClient();
			
			//	请求超时
			httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000); 
			//	读取超时
			httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
			
				
			HttpResponse httpResponse = (HttpResponse) httpclient
					.execute(httppost);

			Log.d(TAG, httpResponse.getStatusLine().toString());
			Log.d(TAG, "EntityID=" + httpResponse.getEntity().toString());

			if(httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
			{
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					dbf.setNamespaceAware(true);
					DocumentBuilder db = dbf.newDocumentBuilder();
					InputStream is = httpResponse.getEntity().getContent();
		
					
					Document doc = db.parse(is);
		
					EntityID = getSoapValue(doc, "entityID");
					if (EntityID == null)
					{
						Log.e(TAG, "EntityID tag not found!");
					}
					
					String roomid = getSoapValue(doc, "entityID");
					String owerid = getSoapValue(doc, "ownerID");
					String displayname = getSoapValue(doc, "displayName");
					String  extension = getSoapValue(doc, "extension");
					
					JSONObject json = new JSONObject();
					json.put("roomid", roomid);
					json.put("owerid", owerid);
					json.put("displayname", displayname);
					json.put("extension", extension);
					args.result = json;
			}
			
		} catch (Exception e)
		{
			e.printStackTrace();
			
		}

		Log.i(TAG, "doInBackground End");
		return (args);
	}

	public String getSoapValue(Document doc, String name)
	{
		NodeList nodes = doc.getElementsByTagNameNS("*", name);
		if (nodes.getLength() > 0)
		{
			Element element = (Element) nodes.item(0);
			NodeList entityIDs = element.getChildNodes();
			Node entityID = entityIDs.item(0);
			String nodevalue = entityID.getNodeValue();
			return nodevalue;
		}
		return null;
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

	@Override
	protected void onPostExecute(Arguments args)
	{
		publishProgress(PROGRESS_DONE);

		if (args == null)
			return;
		
		if(args.callback !=null)
		{
			 args.callback.NetExecutePost(args.result, (args.result==null ? 0 : 1), NetCommand.Login.getValue());
		}
		
		Log.i(TAG, "onPostExecute End");
	}

	public static class Arguments
	{
		String portalString;
		String userString;
		String passwordString;
		INetRequest callback;
		public JSONObject result=null; 
		
		public Arguments(String portal, String user, String password,
				INetRequest callback)
		{
			portalString = portal;
			userString = user;
			passwordString = password;
			this.callback = callback;

		}
	}
}
