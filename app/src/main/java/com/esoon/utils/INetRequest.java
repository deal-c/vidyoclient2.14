package com.esoon.utils;

import org.json.JSONObject;

public interface INetRequest
{
	
/**
 * 
 * 网络请求结果返回 
 * @param result
 * @param resultcode  0:失败, 1:成功.
 * 
 */
	public void NetExecutePost(JSONObject result, int resultcode, int commandid);
}
