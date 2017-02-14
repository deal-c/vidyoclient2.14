package com.vidyo.utils;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
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
import org.xml.sax.InputSource;

import android.os.AsyncTask;
import android.util.Log;

import com.vidyo.utils.Contants.NetCommand;

public class EnterRoomHttp extends
		AsyncTask<EnterRoomHttp.Arguments, Integer, EnterRoomHttp.Arguments>
{
	private final String TAG = "EnterRoomHttp";
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
		
		String portal = "http://";

		Arguments args = params[0];
		String EntityID = args.roomid;
		Log.i(TAG, "doInBackground Begin roomid:" + EntityID);
		
		String urlString = String.format("%1$s/services/v1_1/VidyoPortalUserService/", args.portalString);
		portal += urlString;
		String SOAPRequestXML = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:v1=\"http://portal.vidyo.com/user/v1_1\">" +
			"<soapenv:Header/>" +
			"<soapenv:Body>" +
			"<v1:JoinConferenceRequest>" +
				"<v1:conferenceID>" + EntityID +
			"</v1:conferenceID>"+
			"</v1:JoinConferenceRequest>"+
			"</soapenv:Body>" +
			"</soapenv:Envelope>";
		Log.d(TAG, "SOAP Request = " + SOAPRequestXML);
		publishProgress(PROGRESS_INITNETWORK);

		try {
			HttpPost httppost = new HttpPost(portal);
			StringEntity se = new StringEntity(SOAPRequestXML, HTTP.UTF_8);

			se.setContentType("text/xml");

			httppost.setHeader("Content-Type","text/xml;charset=UTF-8");

			httppost.setHeader("SOAPAction", "\"JoinConference\"");

			String auth = "Basic " + android.util.Base64.encodeToString((args.userString + ":" + args.passwordString).getBytes(), android.util.Base64.NO_WRAP);
			httppost.setHeader("Authorization", auth);

			httppost.setEntity(se);

			HttpClient httpclient = new DefaultHttpClient();
			HttpResponse httpResponse = (HttpResponse) httpclient.execute(httppost);

			StatusLine status = httpResponse.getStatusLine();
			Log.d(TAG, "Join status code = " + status.getStatusCode());

			Log.d(TAG, httpResponse.getStatusLine().toString());
			Log.d(TAG, "status="+httpResponse.getEntity().toString());

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			InputStream is = httpResponse.getEntity().getContent();
			
			InputSource isrc = new InputSource();
			Document doc = db.parse(is);
			
			
				Log.d(TAG, "EntityID = "+getSoapValue(doc,"ns1:entityID"));
			//   Log.d(TAG, "OwnerID = "+getSoapValue(doc,"ns1:ownerID"));
			//  Log.d(TAG, "DisplayName = "+getSoapValue(doc,"ns1:displayName"));
			//  Log.d(TAG, "extension = "+getSoapValue(doc,"ns1:extension"));
			
			
			
	    } catch (Exception e) {
	        e.printStackTrace();
	    }

    	Log.i(TAG, "JoinConference End"); 

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
		String roomid;
		INetRequest callback;
		public JSONObject result=null; 
		
		public Arguments(String portal, String user, String password,String roomid,
				INetRequest callback)
		{
			this.portalString = portal;
			this.userString = user;
			this.roomid = roomid;
			this.passwordString = password;
			this.callback = callback;

		}
	}
}
