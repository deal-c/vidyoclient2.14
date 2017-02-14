package com.vidyo.utils;

public class Contants
{

	public static int screen_width = 0;
	public static int screen_height = 0;
	public static int Status_Height = 0;
	public static String portal = "vidyo-app-t01.ceb.com";
	//public static String username = "";
	//public static String userpass = "";
	
	public static String serverurl="http://192.168.5.49:8580/Every360/Every360Api";

	public static String serveruser="cust2";
	public static String serveruserpass = "";
	public static String managerId="6001";
	
	public static String innerUser="test5";  // 视频服务器用户
	public static String innerPass="123456";  // 视频服务器登录密码
	
	public static int  MainWeb_Tag=0; // 首页显示的界面图.

	
	
	public static String videoUserlist[] = new String[]{"vip1","vip2","cust1","cust2"};
	public static String serverUserlist[] = new String[]{"vip1","vip2","cust1","cust2"};
	
	
	public static String getVideoUser(String serveruser)
	{
		int pos =0;
		for(int i=0;i<serverUserlist.length;i++)
		{
			if(serverUserlist[i].equals(serveruser))
				pos = i;
		}
		return videoUserlist[pos];
	}
	
	
	/**
	 * 网路通讯指令.
	 * 
	 * @author kevin
	 * 
	 */
	public enum NetCommand
	{
		Login(1), Notifylist(2), CreateRoomService(3), CreateRoomManager(4), DeleteRoom(
				5), QueueNumber(6),InviteRoom(7),ErrorMsg(8);
		int commanid;

		NetCommand(int commid)
		{
			this.commanid = commid;
		}

		public int getValue()
		{
			return this.commanid;
		}

	}

	
	 public static void main(String[] args)
	{
		System.out.println (getVideoUser("cust1"));
	}
}
