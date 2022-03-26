package com.example.andrd_ado_vdo_tkbk_demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedList;

import HeavenTao.Ado.*;
import HeavenTao.Vdo.*;
import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

//主界面消息处理类。
class MainActivityHandler extends Handler
{
	static String m_CurClsNameStrPt = "MainActivityHandler"; //当前类名称字符串的指针。

	MainActivity m_MainActivityPt; //存放主界面的指针。
	ServiceConnection m_FrgndSrvcCnctPt; //存放前台服务连接器的指针。
	AlertDialog m_RequestCnctDialogPt; //存放请求连接对话框的指针。

	public static final int INIT_MEDIA_PROC_THREAD = 1; //初始化媒体处理线程的消息。
	public static final int DSTOY_MEDIA_PROC_THREAD = 2; //销毁媒体处理线程的消息。
	public static final int SHOW_REQUEST_CNCT_DIALOG = 3; //显示请求连接对话框的消息。
	public static final int DSTOY_REQUEST_CNCT_DIALOG = 4; //销毁请求连接对话框的消息。
	public static final int SHOW_LOG = 5; //显示日志的消息。
	public static final int REBUILD_SURFACE_VIEW = 6; //重建Surface视图的消息。
	public static final int VIBRATE = 7; //振动的消息。

	public void handleMessage( Message MessagePt )
	{
		if( MessagePt.what == INIT_MEDIA_PROC_THREAD ) //如果是初始化媒体处理线程的消息。
		{
			if( m_MainActivityPt.m_MyMediaPocsThrdPt.m_IsCreateSrvrOrClnt == 1 ) //如果是创建服务端。
			{
				( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseTcpPrtclRdBtnId ) ).setEnabled( false ); //设置TCP协议按钮为不可用。
				( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseUdpPrtclRdBtnId ) ).setEnabled( false ); //设置UDP协议按钮为不可用。
				( ( Button ) m_MainActivityPt.findViewById( R.id.XfrPrtclStngBtnId ) ).setEnabled( false ); //设置传输协议设置按钮为不可用。
				( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdTxtId ) ).setEnabled( false ); //设置IP地址控件为不可用。
				( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdTxtId ) ).setEnabled( false ); //设置端口控件为不可用。
				( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtnId ) ).setText( "中断" ); //设置创建服务端按钮的内容为“中断”。
				( ( Button ) m_MainActivityPt.findViewById( R.id.CnctSrvrBtnId ) ).setEnabled( false ); //设置连接服务端按钮为不可用。
				( ( Button ) m_MainActivityPt.findViewById( R.id.StngBtnId ) ).setEnabled( false ); //设置设置按钮为不可用。
				if( m_MainActivityPt.m_MyMediaPocsThrdPt.m_XfrMode == 0 )
				{
					( ( Button ) m_MainActivityPt.findViewById( R.id.PttBtnId ) ).setVisibility( Button.VISIBLE ); //设置一键即按即通按钮为可见。
				}
			}
			else //如果是创建客户端。
			{
				( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseTcpPrtclRdBtnId ) ).setEnabled( false ); //设置TCP协议按钮为不可用。
				( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseUdpPrtclRdBtnId ) ).setEnabled( false ); //设置UDP协议按钮为不可用。
				( ( Button ) m_MainActivityPt.findViewById( R.id.XfrPrtclStngBtnId ) ).setEnabled( false ); //设置传输协议设置按钮为不可用。
				( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdTxtId ) ).setEnabled( false ); //设置IP地址控件为不可用。
				( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdTxtId ) ).setEnabled( false ); //设置端口控件为不可用。
				( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtnId ) ).setEnabled( false ); //设置创建服务端按钮为不可用。
				( ( Button ) m_MainActivityPt.findViewById( R.id.CnctSrvrBtnId ) ).setText( "中断" ); //设置连接服务端按钮的内容为“中断”。
				( ( Button ) m_MainActivityPt.findViewById( R.id.StngBtnId ) ).setEnabled( false ); //设置设置按钮为不可用。
				if( m_MainActivityPt.m_MyMediaPocsThrdPt.m_XfrMode == 0 )
				{
					( ( Button ) m_MainActivityPt.findViewById( R.id.PttBtnId ) ).setVisibility( Button.VISIBLE ); //设置一键即按即通按钮为可见。
				}
			}

			//创建并绑定前台服务，从而确保本进程在转入后台或系统锁屏时不会被系统限制运行，且只能放在主线程中执行，因为要使用界面。
			if( ( ( CheckBox ) m_MainActivityPt.m_StngLyotViewPt.findViewById( R.id.IsUseFrgndSrvcCkBoxId ) ).isChecked() && ( m_FrgndSrvcCnctPt == null ) )
			{
				m_FrgndSrvcCnctPt = new ServiceConnection() //创建存放前台服务连接器。
				{
					@Override public void onServiceConnected( ComponentName name, IBinder service ) //前台服务绑定成功。
					{
						( ( FrgndSrvc.FrgndSrvcBinder ) service ).SetForeground( m_MainActivityPt ); //将服务设置为前台服务。
					}

					@Override public void onServiceDisconnected( ComponentName name ) //前台服务解除绑定。
					{

					}
				};
				m_MainActivityPt.bindService( new Intent( m_MainActivityPt, FrgndSrvc.class ), m_FrgndSrvcCnctPt, Context.BIND_AUTO_CREATE ); //创建并绑定前台服务。
			}
		}
		else if( MessagePt.what == SHOW_REQUEST_CNCT_DIALOG ) //如果是显示请求连接对话框的消息。
		{
			AlertDialog.Builder builder = new AlertDialog.Builder( m_MainActivityPt );

			builder.setCancelable( false ); //点击对话框以外的区域是否让对话框消失
			builder.setTitle( R.string.app_name );

			if( m_MainActivityPt.m_MyMediaPocsThrdPt.m_IsCreateSrvrOrClnt == 1 ) //如果是创建服务端。
			{
				builder.setMessage( "您是否允许远端[" + MessagePt.obj + "]的连接？" );

				//设置正面按钮。
				builder.setPositiveButton( "允许", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick( DialogInterface dialog, int which )
					{
						m_MainActivityPt.m_MyMediaPocsThrdPt.m_RequestCnctRslt = 1;
						m_RequestCnctDialogPt = null;
					}
				} );
				//设置反面按钮。
				builder.setNegativeButton( "拒绝", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick( DialogInterface dialog, int which )
					{
						m_MainActivityPt.m_MyMediaPocsThrdPt.m_RequestCnctRslt = 2;
						m_RequestCnctDialogPt = null;
					}
				} );
			}
			else //如果是创建客户端。
			{
				builder.setMessage( "等待远端[" + MessagePt.obj + "]允许您的连接..." );

				//设置反面按钮。
				builder.setNegativeButton( "中断", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick( DialogInterface dialog, int which )
					{
						m_MainActivityPt.m_MyMediaPocsThrdPt.m_RequestCnctRslt = 2;
						m_RequestCnctDialogPt = null;
					}
				} );
			}

			m_RequestCnctDialogPt = builder.create(); //创建AlertDialog对象。
			m_RequestCnctDialogPt.show();
		}
		else if( MessagePt.what == DSTOY_REQUEST_CNCT_DIALOG ) //如果是销毁请求连接对话框的消息。
		{
			if( m_RequestCnctDialogPt != null )
			{
				m_RequestCnctDialogPt.cancel();
				m_RequestCnctDialogPt = null;
			}
		}
		else if( MessagePt.what == DSTOY_MEDIA_PROC_THREAD ) //如果是销毁媒体处理线程的消息。
		{
			m_MainActivityPt.m_MyMediaPocsThrdPt = null;

			if( m_FrgndSrvcCnctPt != null ) //如果已经创建并绑定了前台服务。
			{
				m_MainActivityPt.unbindService( m_FrgndSrvcCnctPt ); //解除绑定并销毁前台服务。
				m_FrgndSrvcCnctPt = null;
			}

			( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseTcpPrtclRdBtnId ) ).setEnabled( true ); //设置TCP协议按钮为可用。
			( ( RadioButton ) m_MainActivityPt.findViewById( R.id.UseUdpPrtclRdBtnId ) ).setEnabled( true ); //设置UDP协议按钮为可用。
			( ( Button ) m_MainActivityPt.findViewById( R.id.XfrPrtclStngBtnId ) ).setEnabled( true ); //设置传输协议设置按钮为不可用。
			( ( EditText ) m_MainActivityPt.findViewById( R.id.IPAddrEdTxtId ) ).setEnabled( true ); //设置IP地址控件为可用。
			( ( EditText ) m_MainActivityPt.findViewById( R.id.PortEdTxtId ) ).setEnabled( true ); //设置端口控件为可用。
			( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtnId ) ).setText( "创建服务端" ); //设置创建服务端按钮的内容为“创建服务端”。
			( ( Button ) m_MainActivityPt.findViewById( R.id.CnctSrvrBtnId ) ).setEnabled( true ); //设置连接服务端按钮为可用。
			( ( Button ) m_MainActivityPt.findViewById( R.id.CnctSrvrBtnId ) ).setText( "连接服务端" ); //设置连接服务端按钮的内容为“连接服务端”。
			( ( Button ) m_MainActivityPt.findViewById( R.id.CreateSrvrBtnId ) ).setEnabled( true ); //设置创建服务端按钮为可用。
			( ( Button ) m_MainActivityPt.findViewById( R.id.StngBtnId ) ).setEnabled( true ); //设置设置按钮为可用。
			( ( Button ) m_MainActivityPt.findViewById( R.id.PttBtnId ) ).setVisibility( Button.INVISIBLE ); //设置一键即按即通按钮为不可见。
		}
		else if( MessagePt.what == SHOW_LOG ) //如果是显示日志的消息。
		{
			TextView p_LogTextView = new TextView( m_MainActivityPt );
			p_LogTextView.setText( ( new SimpleDateFormat( "HH:mm:ss SSS" ) ).format( new Date() ) + "：" + MessagePt.obj );
			( ( LinearLayout ) m_MainActivityPt.m_MainLyotViewPt.findViewById( R.id.LogLinearLyotId ) ).addView( p_LogTextView );
		}
		else if( MessagePt.what == REBUILD_SURFACE_VIEW ) //如果是重建Surface视图的消息，用来清空残余画面。
		{
			m_MainActivityPt.m_VdoInptPrvwSurfaceViewPt.setVisibility( View.GONE ); //销毁视频输入预览Surface视图。
			m_MainActivityPt.m_VdoInptPrvwSurfaceViewPt.setVisibility( View.VISIBLE ); //创建视频输入预览Surface视图。
			m_MainActivityPt.m_VdoOtptDspySurfaceViewPt.setVisibility( View.GONE ); //销毁视频输出显示Surface视图。
			m_MainActivityPt.m_VdoOtptDspySurfaceViewPt.setVisibility( View.VISIBLE ); //创建视频输出显示Surface视图。
		}
		else if( MessagePt.what == VIBRATE ) //如果是振动消息。
		{
			( ( Vibrator ) m_MainActivityPt.getSystemService( m_MainActivityPt.VIBRATOR_SERVICE ) ).vibrate( 100 );
		}
	}
}

//我的媒体处理线程类。
class MyMediaPocsThrd extends MediaPocsThrd
{
	Activity m_MainActivityPt; //存放主界面的指针。
	Handler m_MainActivityHandlerPt; //存放主界面消息处理的指针。

	String m_IPAddrStrPt; //存放IP地址字符串的指针。
	String m_PortStrPt; //存放端口字符串的指针。
	int m_XfrMode; //存放传输模式，为0表示实时半双工（一键通），为1表示实时全双工。
	int m_PttBtnIsDown; //存放一键即按即通按钮是否按下，为0表示弹起，为1表示按下。
	int m_PttDownIsNoVibrate; //存放一键即按即通按钮按下后是否没有振动，为0表示已经振动，为1表示没有振动。
	int m_MaxCnctTimes; //存放最大连接次数，取值区间为[1,2147483647]。
	int m_UseWhatXfrPrtcl; //存放使用什么传输协议，为0表示TCP协议，为1表示UDP协议。
	int m_IsCreateSrvrOrClnt; //存放创建服务端或者客户端标记，为1表示创建服务端，为0表示创建客户端。
	TcpSrvrSokt m_TcpSrvrSoktPt; //存放本端TCP协议服务端套接字的指针。
	TcpClntSokt m_TcpClntSoktPt; //存放本端TCP协议客户端套接字的指针。
	UdpSokt m_UdpSoktPt; //存放本端UDP协议套接字的指针。
	long m_LastPktSendTime; //存放最后一个数据包的发送时间，用于判断连接是否中断。
	long m_LastPktRecvTime; //存放最后一个数据包的接收时间，用于判断连接是否中断。
	public static final byte PKT_TYP_RQST_CNCT   = 1; //数据包类型：请求连接包。
	public static final byte PKT_TYP_CNCT_ACK    = 2; //数据包类型：连接应答包。
	public static final byte PKT_TYP_ALLOW_CNCT  = 3; //数据包类型：允许连接包。
	public static final byte PKT_TYP_REFUSE_CNCT = 4; //数据包类型：拒绝连接包。
	public static final byte PKT_TYP_ADO_FRM     = 5; //数据包类型：音频输入输出帧。
	public static final byte PKT_TYP_VDO_FRM     = 6; //数据包类型：视频输入输出帧。
	public static final byte PKT_TYP_HTBT        = 7; //数据包类型：心跳包。
	public static final byte PKT_TYP_EXIT        = 8; //数据包类型：退出包。

	int m_IsAutoAllowCnct; //存放是否自动允许连接，为0表示手动，为1表示自动。
	int m_RequestCnctRslt; //存放请求连接的结果，为0表示没有选择，为1表示允许，为2表示拒绝。

	int m_LastSendAdoInptFrmIsAct; //存放最后一个发送的音频输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
	int m_LastSendAdoInptFrmTimeStamp; //存放最后一个发送音频输入帧的时间戳。
	int m_LastSendVdoInptFrmTimeStamp; //存放最后一个发送视频输入帧的时间戳。
	byte m_IsRecvExitPkt; //存放是否接收到退出包，为0表示否，为1表示是。

	int m_UseWhatRecvOtptFrm; //存放使用什么接收输出帧，为0表示链表，为1表示自适应抖动缓冲器。
	int m_LastGetAdoOtptFrmIsAct; //存放最后一个取出的音频输出帧是否为有语音活动，为0表示否，为非0表示是。
	int m_LastGetAdoOtptFrmVdoOtptFrmTimeStamp; //存放最后一个取出的音频输出帧对应视频输出帧的时间戳。

	LinkedList< byte[] > m_RecvAdoOtptFrmLnkLstPt; //存放接收音频输出帧链表的指针。
	LinkedList< byte[] > m_RecvVdoOtptFrmLnkLstPt; //存放接收视频输出帧链表的指针。

	AAjb m_AAjbPt; //存放音频自适应抖动缓冲器的指针。
	int m_AAjbMinNeedBufFrmCnt; //存放音频自适应抖动缓冲器的最小需缓冲帧的数量，单位为个帧，取值区间为[1,2147483647]。
	int m_AAjbMaxNeedBufFrmCnt; //音频自适应抖动缓冲器的最大需缓冲帧的数量，单位为个帧，取值区间为[1,2147483647]，必须大于等于最小需缓冲帧的数量。
	int m_AAjbMaxCntuLostFrmCnt; //音频自适应抖动缓冲器的最大连续丢失帧的数量，单位为个帧，取值区间为[1,2147483647]，当连续丢失帧的数量超过最大时，认为是对方中途暂停发送。
	float m_AAjbAdaptSensitivity; //存放音频自适应抖动缓冲器的自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。
	VAjb m_VAjbPt; //存放视频自适应抖动缓冲器的指针。
	int m_VAjbMinNeedBufFrmCnt; //存放视频自适应抖动缓冲器的最小需缓冲帧数量，单位个，必须大于0。
	int m_VAjbMaxNeedBufFrmCnt; //存放视频自适应抖动缓冲器的最大需缓冲帧数量，单位个，必须大于最小需缓冲数据帧的数量。
	float m_VAjbAdaptSensitivity; //存放视频自适应抖动缓冲器的自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。

	byte m_TmpBytePt[]; //存放临时数据。
	byte m_TmpByte2Pt[]; //存放临时数据。
	byte m_TmpByte3Pt[]; //存放临时数据。
	HTInt m_TmpHTIntPt; //存放临时数据。
	HTInt m_TmpHTInt2Pt; //存放临时数据。
	HTInt m_TmpHTInt3Pt; //存放临时数据。
	HTLong m_TmpHTLongPt; //存放临时数据。
	HTLong m_TmpHTLong2Pt; //存放临时数据。
	HTLong m_TmpHTLong3Pt; //存放临时数据。

	MyMediaPocsThrd( Activity MainActivityPt, Handler MainActivityHandlerPt )
	{
		super( MainActivityPt.getApplicationContext() );

		m_MainActivityPt = MainActivityPt; //设置主界面的指针。
		m_MainActivityHandlerPt = MainActivityHandlerPt; //设置主界面消息处理的指针。
	}

	//用户定义的初始化函数。
	@Override public int UserInit()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
		HTString p_LclNodeAddrPt = new HTString();
		HTString p_LclNodePortPt = new HTString();
		HTString p_RmtNodeAddrPt = new HTString();
		HTString p_RmtNodePortPt = new HTString();

		Out:
		{
			{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.INIT_MEDIA_PROC_THREAD;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送初始化媒体处理线程的消息。

			m_RequestCnctRslt = 0; //设置请求连接的结果为没有选择。
			m_IsRecvExitPkt = 0; //设置没有接收到退出包。
			if( m_TmpBytePt == null ) m_TmpBytePt = new byte[1024 * 1024]; //初始化临时数据。
			if( m_TmpByte2Pt == null ) m_TmpByte2Pt = new byte[1024 * 1024]; //初始化临时数据。
			if( m_TmpByte3Pt == null ) m_TmpByte3Pt = new byte[1024 * 1024]; //初始化临时数据。
			if( m_TmpHTIntPt == null ) m_TmpHTIntPt = new HTInt(); //初始化临时数据。
			if( m_TmpHTInt2Pt == null ) m_TmpHTInt2Pt = new HTInt(); //初始化临时数据。
			if( m_TmpHTInt3Pt == null ) m_TmpHTInt3Pt = new HTInt(); //初始化临时数据。
			if( m_TmpHTLongPt == null ) m_TmpHTLongPt = new HTLong(); //初始化临时数据。
			if( m_TmpHTLong2Pt == null ) m_TmpHTLong2Pt = new HTLong(); //初始化临时数据。
			if( m_TmpHTLong3Pt == null ) m_TmpHTLong3Pt = new HTLong(); //初始化临时数据。

			if( m_UseWhatXfrPrtcl == 0 ) //如果使用TCP协议。
			{
				if( m_IsCreateSrvrOrClnt == 1 ) //如果是创建本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接。
				{
					m_TcpSrvrSoktPt = new TcpSrvrSokt();

					if( m_TcpSrvrSoktPt.Init( 4, m_IPAddrStrPt, m_PortStrPt, 1, 1, m_ErrInfoVarStrPt ) == 0 ) //如果创建并初始化已监听的本端TCP协议服务端套接字成功。
					{
						if( m_TcpSrvrSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 ) //如果获取已监听的本端TCP协议服务端套接字绑定的本地节点地址和端口失败。
						{
							String p_InfoStrPt = "获取已监听的本端TCP协议服务端套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}

						String p_InfoStrPt = "创建并初始化已监听的本端TCP协议服务端套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]成功。";
						Log.i( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					}
					else //如果创建并初始化已监听的本端TCP协议服务端套接字失败。
					{
						String p_InfoStrPt = "创建并初始化已监听的本端TCP协议服务端套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}

					m_TcpClntSoktPt = new TcpClntSokt();

					while( true ) //循环接受远端TCP协议客户端套接字的连接。
					{
						if( m_TcpSrvrSoktPt.Accept( null, p_RmtNodeAddrPt, p_RmtNodePortPt, ( short ) 1, m_TcpClntSoktPt, 0, m_ErrInfoVarStrPt ) == 0 )
						{
							if( m_TcpClntSoktPt.m_TcpClntSoktPt != 0 ) //如果用已监听的本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接成功。
							{
								m_TcpSrvrSoktPt.Dstoy( null ); //关闭并销毁已创建的本端TCP协议服务端套接字，防止还有其他远端TCP协议客户端套接字继续连接。
								m_TcpSrvrSoktPt = null;

								String p_InfoStrPt = "用已监听的本端TCP协议服务端套接字接受远端TCP协议客户端套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]的连接成功。";
								Log.i( m_CurClsNameStrPt, p_InfoStrPt );
								Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
								break;
							}
							else //如果用已监听的本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接超时，就重新接受。
							{

							}
						}
						else
						{
							m_TcpClntSoktPt = null;

							String p_InfoStrPt = "用已监听的本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}

						if( m_ExitFlag != 0 ) //如果本线程接收到退出请求。
						{
							m_TcpClntSoktPt = null;

							Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
							break Out;
						}
					}
				}
				else if( m_IsCreateSrvrOrClnt == 0 ) //如果是创建本端TCP协议客户端套接字连接远端TCP协议服务端套接字。
				{
					//Ping一下远程节点地址，这样可以快速获取局域网的ARP条目，从而避免连接失败。
					try
					{
						Runtime.getRuntime().exec( "ping -c 1 -w 1 " + m_IPAddrStrPt );
					}
					catch( Exception ignored )
					{
					}

					m_TcpClntSoktPt = new TcpClntSokt();

					int p_CurCnctTimes = 1;
					LoopCnct:
					while( true ) //循环连接已监听的远端TCP协议服务端套接字。
					{
						{
							String p_InfoStrPt = "开始第 " + p_CurCnctTimes + "次连接。";
							Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						}

						if( m_TcpClntSoktPt.Init( 4, m_IPAddrStrPt, m_PortStrPt, null, null, ( short ) 5000, m_ErrInfoVarStrPt ) == 0 ) //如果创建并初始化本端TCP协议客户端套接字，并连接已监听的远端TCP协议服务端套接字成功。
						{
							if( m_TcpClntSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 )
							{
								String p_InfoStrPt = "获取已连接的本端TCP协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
								Log.e( m_CurClsNameStrPt, p_InfoStrPt );
								Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
								break Out;
							}
							if( m_TcpClntSoktPt.GetRmtAddr( null, p_RmtNodeAddrPt, p_RmtNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 )
							{
								String p_InfoStrPt = "获取已连接的本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远程节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
								Log.e( m_CurClsNameStrPt, p_InfoStrPt );
								Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
								break Out;
							}

							String p_InfoStrPt = "创建并初始化本端TCP协议客户端套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]，并连接已监听的远端TCP协议服务端套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]成功。";
							Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break LoopCnct; //跳出重连。
						}
						else
						{
							String p_InfoStrPt = "创建并初始化本端TCP协议客户端套接字，并连接已监听的远端TCP协议服务端套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						}

						p_CurCnctTimes++; //递增当前连接次数。
						if( p_CurCnctTimes > m_MaxCnctTimes )
						{
							m_TcpClntSoktPt = null;

							String p_InfoStrPt = "达到最大连接次数，中断连接。";
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}

						if( m_ExitFlag != 0 ) //如果本线程接收到退出请求。
						{
							m_TcpClntSoktPt = null;

							Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
							break Out;
						}
					}
				}

				if( m_TcpClntSoktPt.SetNoDelay( 1, 0, m_ErrInfoVarStrPt ) != 0 ) //如果设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态为禁用失败。
				{
					String p_InfoStrPt = "设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_ErrInfoVarStrPt.GetStr();
					Log.i( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					break Out;
				}

				if( m_TcpClntSoktPt.SetSendBufSz( 128 * 1024, 0, m_ErrInfoVarStrPt ) != 0 )
				{
					String p_InfoStrPt = "设置已连接的本端TCP协议客户端套接字的发送缓冲区内存大小失败。原因：" + m_ErrInfoVarStrPt.GetStr();
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					break Out;
				}

				if( m_TcpClntSoktPt.SetRecvBufSz( 128 * 1024, 0, m_ErrInfoVarStrPt ) != 0 )
				{
					String p_InfoStrPt = "设置已连接的本端TCP协议客户端套接字的接收缓冲区内存大小失败。原因：" + m_ErrInfoVarStrPt.GetStr();
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					break Out;
				}
			}
			else //如果使用UDP协议。
			{
				m_UdpSoktPt = new UdpSokt();

				if( m_IsCreateSrvrOrClnt == 1 ) //如果是创建本端UDP协议套接字接受远端UDP协议套接字的连接。
				{
					if( m_UdpSoktPt.Init( 4, m_IPAddrStrPt, m_PortStrPt, m_ErrInfoVarStrPt ) == 0 ) //如果创建并初始化已监听的本端UDP协议套接字成功。
					{
						if( m_UdpSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 ) //如果获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。
						{
							String p_InfoStrPt = "获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}

						String p_InfoStrPt = "创建并初始化已监听的本端UDP协议套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]成功。";
						Log.i( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					}
					else //如果创建并初始化已监听的本端UDP协议套接字失败。
					{
						String p_InfoStrPt = "创建并初始化已监听的本端UDP协议套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}

					UdpSrvrReAccept:
					while( true ) //循环接受远端UDP协议套接字的连接。
					{
						if( m_UdpSoktPt.RecvPkt( null, p_RmtNodeAddrPt, p_RmtNodePortPt, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 1, 0, m_ErrInfoVarStrPt ) == 0 )
						{
							if( m_TmpHTLongPt.m_Val != -1 ) //如果用已监听的本端UDP协议套接字接收一个远端UDP协议套接字发送的数据包成功。
							{
								if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[0] == PKT_TYP_RQST_CNCT ) ) //如果是请求连接包。
								{
									m_UdpSoktPt.Connect( 4, p_RmtNodeAddrPt.m_Val, p_RmtNodePortPt.m_Val, 0, null ); //用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字，已连接的本端UDP协议套接字只能接收连接的远端UDP协议套接字发送的数据包。

									//连接远端。
									UdpSrvrCnctRmt:
									{
										//发送请求连接包。
										m_TmpBytePt[0] = PKT_TYP_RQST_CNCT; //设置请求连接包。
										if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 10, 0, m_ErrInfoVarStrPt ) != 0 )
										{
											String p_InfoStrPt = "用已监听的本端UDP协议套接字发送请求连接包到远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
											Log.i( m_CurClsNameStrPt, p_InfoStrPt );
											Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
											break UdpSrvrCnctRmt;
										}

										//接收连接应答包。
										m_LastPktRecvTime = System.currentTimeMillis();
										while( true )
										{
											if( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 1, 0, m_ErrInfoVarStrPt ) == 0 )
											{
												if( m_TmpHTLongPt.m_Val != -1 ) //如果用已监听的本端UDP协议套接字接收一个远端UDP协议套接字发送的数据包成功。
												{
													if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[0] != PKT_TYP_RQST_CNCT ) ) //如果不是请求连接包。
													{
														//就表示连接已经成功建立。
														break UdpSrvrReAccept; //跳出接受循环。
													}
													else //如果是请求连接包。
													{
														//就重新接收连接应答包。
													}
												}
												else //如果用已监听的本端UDP协议套接字接收一个远端UDP协议套接字发送的数据包超时。
												{
													//就重新接收连接应答包。
												}
											}
											else
											{
												String p_InfoStrPt = "用已监听的本端UDP协议套接字接收远端UDP协议套接字发送的连接应答包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
												Log.e( m_CurClsNameStrPt, p_InfoStrPt );
												Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
												break UdpSrvrCnctRmt;
											}

											if( System.currentTimeMillis() - m_LastPktRecvTime > 5000 )
											{
												String p_InfoStrPt = "用已监听的本端UDP协议套接字接收远端UDP协议套接字发送的连接应答包失败。原因：接收超时。";
												Log.e( m_CurClsNameStrPt, p_InfoStrPt );
												Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
												break UdpSrvrCnctRmt;
											}

											if( m_ExitFlag != 0 ) //如果本线程接收到退出请求。
											{
												m_UdpSoktPt.Disconnect( 0, null );

												Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
												break Out;
											}
										}
									}

									m_UdpSoktPt.Disconnect( 0, null ); //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。

									String p_InfoStrPt = "本端UDP协议套接字继续保持监听来接受连接。";
									Log.i( m_CurClsNameStrPt, p_InfoStrPt );
									Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
								}
								else //如果是其他包。
								{
									//就重新接收。
								}
							}
							else //如果用已监听的本端UDP协议套接字接受到远端UDP协议套接字的连接请求超时。
							{
								//就重新接收。
							}
						}
						else
						{
							String p_InfoStrPt = "用已监听的本端UDP协议套接字接收远端UDP协议套接字发送的请求连接包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}

						if( m_ExitFlag != 0 ) //如果本线程接收到退出请求。
						{
							Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
							break Out;
						}
					}

					String p_InfoStrPt = "用已监听的本端UDP协议套接字接受远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]的连接成功。";
					Log.i( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
				}
				else if( m_IsCreateSrvrOrClnt == 0 ) //如果是创建本端UDP协议套接字连接远端UDP协议套接字。
				{
					//Ping一下远程节点地址，这样可以快速获取ARP条目。
					try
					{
						Runtime.getRuntime().exec( "ping -c 1 -w 1 " + m_IPAddrStrPt );
					}
					catch( Exception ignored )
					{
					}

					if( m_UdpSoktPt.Init( 4, null, null, m_ErrInfoVarStrPt ) == 0 ) //如果创建并初始化已监听的本端UDP协议套接字成功。
					{
						if( m_UdpSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 ) //如果获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。
						{
							String p_InfoStrPt = "获取已监听的本端UDP协议套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}

						String p_InfoStrPt = "创建并初始化已监听的本端UDP协议套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]成功。";
						Log.i( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					}
					else //如果创建并初始化已监听的本端UDP协议套接字失败。
					{
						String p_InfoStrPt = "创建并初始化已监听的本端UDP协议套接字失败。原因：" + m_ErrInfoVarStrPt.GetStr();
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}

					if( m_UdpSoktPt.Connect( 4, m_IPAddrStrPt, m_PortStrPt, 0, m_ErrInfoVarStrPt ) != 0 )
					{
						String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + m_IPAddrStrPt + ":" + m_PortStrPt + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
						Log.i( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}

					if( m_UdpSoktPt.GetRmtAddr( null, p_RmtNodeAddrPt, p_RmtNodePortPt, 0, m_ErrInfoVarStrPt ) != 0 )
					{
						m_UdpSoktPt.Disconnect( 0, null );
						String p_InfoStrPt = "获取已连接的本端UDP协议套接字连接的远端UDP协议套接字绑定的远程节点地址和端口失败。原因：" + m_ErrInfoVarStrPt.GetStr();
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}

					int p_CurCnctTimes = 1;
					UdpClntLoopCnct:
					while( true ) //循环连接已监听的远端UDP协议套接字。
					{
						//连接远端。
						UdpClntCnctRmt:
						{
							{
								String p_InfoStrPt = "开始第 " + p_CurCnctTimes + "次连接。";
								Log.i( m_CurClsNameStrPt, p_InfoStrPt );
								Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							}

							//发送请求连接包。
							m_TmpBytePt[0] = PKT_TYP_RQST_CNCT; //设置请求连接包。
							if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 10, 0, m_ErrInfoVarStrPt ) != 0 )
							{
								String p_InfoStrPt = "用已监听的本端UDP协议套接字发送请求连接包到已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
								Log.e( m_CurClsNameStrPt, p_InfoStrPt );
								Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
								break UdpClntCnctRmt;
							}

							//接收请求连接包。
							m_LastPktRecvTime = System.currentTimeMillis();
							while( true )
							{
								if( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 1, 0, m_ErrInfoVarStrPt ) == 0 )
								{
									if( m_TmpHTLongPt.m_Val != -1 ) //如果用已监听的本端UDP协议套接字接收一个远端UDP协议套接字发送的数据包成功。
									{
										if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[0] == PKT_TYP_RQST_CNCT ) ) //如果是请求连接包。
										{
											//发送连接应答包。
											m_TmpBytePt[0] = PKT_TYP_CNCT_ACK; //设置连接应答包。
											if( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 10, 0, m_ErrInfoVarStrPt ) != 0 )
											{
												String p_InfoStrPt = "用已监听的本端UDP协议套接字发送连接应答包到已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]失败。原因：" + m_ErrInfoVarStrPt.GetStr();
												Log.i( m_CurClsNameStrPt, p_InfoStrPt );
												Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
												break UdpClntCnctRmt;
											}
											break;
										}
										else //如果不是请求连接包。
										{
											//就重新接收请求连接包。
										}
									}
									else //如果用已监听的本端UDP协议套接字接收一个远端UDP协议套接字发送的数据包超时。
									{
										//就重新接收请求连接包。
									}
								}
								else
								{
									m_UdpSoktPt.Disconnect( 0, null ); //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。

									String p_InfoStrPt = "用已监听的本端UDP协议套接字接收已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]发送的请求连接包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
									Log.e( m_CurClsNameStrPt, p_InfoStrPt );
									Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
									break Out;
								}

								if( System.currentTimeMillis() - m_LastPktRecvTime > 5000 )
								{
									String p_InfoStrPt = "用已监听的本端UDP协议套接字接收已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]发送的请求连接包失败。原因：接收超时。";
									Log.e( m_CurClsNameStrPt, p_InfoStrPt );
									Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
									break UdpClntCnctRmt;
								}

								if( m_ExitFlag != 0 ) //如果本线程接收到退出请求。
								{
									m_UdpSoktPt.Disconnect( 0, null ); //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。

									Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
									break Out;
								}
							}

							String p_InfoStrPt = "用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]成功。";
							Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break UdpClntLoopCnct;
						}

						p_CurCnctTimes++; //递增当前连接次数。
						if( p_CurCnctTimes > m_MaxCnctTimes )
						{
							m_UdpSoktPt.Disconnect( 0, null );

							String p_InfoStrPt = "达到最大连接次数，中断连接。";
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}
					}
				}

				if( m_UdpSoktPt.SetSendBufSz( 128 * 1024, 0, m_ErrInfoVarStrPt ) != 0 )
				{
					String p_InfoStrPt = "设置已监听的本端UDP协议套接字的发送缓冲区内存大小失败。原因：" + m_ErrInfoVarStrPt.GetStr();
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					break Out;
				}

				if( m_UdpSoktPt.SetRecvBufSz( 128 * 1024, 0, m_ErrInfoVarStrPt ) != 0 )
				{
					String p_InfoStrPt = "设置已监听的本端UDP协议套接字的接收缓冲区内存大小失败。原因：" + m_ErrInfoVarStrPt.GetStr();
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					break Out;
				}
			} //协议连接结束。

			//等待允许连接。
			m_LastPktSendTime = System.currentTimeMillis() - 100;
			m_LastPktRecvTime = System.currentTimeMillis();
			if( ( m_IsCreateSrvrOrClnt == 1 ) && ( m_IsAutoAllowCnct != 0 ) ) m_RequestCnctRslt = 1;
			else m_RequestCnctRslt = 0;
			{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_REQUEST_CNCT_DIALOG;p_MessagePt.obj = p_RmtNodeAddrPt.m_Val;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送显示请求连接对话框的消息。
			WaitAllowCnct:
			while( true )
			{
				if( m_IsCreateSrvrOrClnt == 1 ) //如果是服务端。
				{
					if( m_RequestCnctRslt == 1 ) //如果允许连接。
					{
						m_TmpBytePt[0] = PKT_TYP_ALLOW_CNCT; //设置允许连接包。
						if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
							( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 10, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
						{
							{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

							String p_InfoStrPt = "发送一个允许连接包成功。";
							Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
							break WaitAllowCnct;
						}
						else
						{
							{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

							String p_InfoStrPt = "发送一个允许连接包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}
					}
					else if( m_RequestCnctRslt == 2 ) //如果拒绝连接。
					{
						m_TmpBytePt[0] = PKT_TYP_REFUSE_CNCT; //设置拒绝连接包。
						if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
							( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 10, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
						{
							{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

							String p_InfoStrPt = "发送一个拒绝连接包成功。";
							Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
							break Out;
						}
						else
						{
							{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

							String p_InfoStrPt = "发送一个拒绝连接包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}
					}
				}
				else //如果是客户端。
				{
					if( m_RequestCnctRslt == 2 ) //如果中断等待。
					{
						m_TmpBytePt[0] = PKT_TYP_REFUSE_CNCT; //设置拒绝连接包。
						if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
							( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 10, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
						{
							{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

							String p_InfoStrPt = "发送一个拒绝连接包成功。";
							Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
							break Out;
						}
						else
						{
							{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

							String p_InfoStrPt = "发送一个拒绝连接包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break Out;
						}
					}
				}

				//发送心跳包。
				if( System.currentTimeMillis() - m_LastPktSendTime >= 100 )
				{
					m_TmpBytePt[0] = PKT_TYP_HTBT; //设置心跳包。
					if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
						( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
					{
						m_LastPktSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。
						Log.i( m_CurClsNameStrPt, "发送一个心跳包成功。" );
					}
					else
					{
						{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

						String p_InfoStrPt = "发送一个心跳包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}
				}

				//接收一个远端发送的数据包。
				if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.RecvPkt( m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
					( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
				{
					if( m_TmpHTLongPt.m_Val != -1 ) //如果用已连接的本端套接字接收一个连接的远端套接字发送的数据包成功。
					{
						m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。

						if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[0] == PKT_TYP_HTBT ) ) //如果是心跳包。
						{
							Log.i( m_CurClsNameStrPt, "接收到一个心跳包。" );
						}
						else if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[0] == PKT_TYP_ALLOW_CNCT ) ) //如果是允许连接包。
						{
							if( m_IsCreateSrvrOrClnt == 0 ) //如果是客户端。
							{
								m_RequestCnctRslt = 1;

								{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

								String p_InfoStrPt = "接收到一个允许连接包。";
								Log.i( m_CurClsNameStrPt, p_InfoStrPt );
								Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
								//if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
								break WaitAllowCnct;
							}
							else //如果是服务端。
							{
								//就重新接收。
							}
						}
						else if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[0] == PKT_TYP_REFUSE_CNCT ) ) //如果是拒绝连接包。
						{
							m_RequestCnctRslt = 2;

							{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

							String p_InfoStrPt = "接收到一个拒绝连接包。";
							Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
							break Out;
						}
						else //如果是其他包。
						{
							//就重新接收。
						}
					}
					else //如果用已连接的本端套接字接收一个连接的远端套接字发送的数据包超时。
					{
						//就重新接收。
					}
				}
				else //如果用已连接的本端套接字接收一个连接的远端套接字发送的数据包失败。
				{
					{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁请求连接对话框的消息。

					String p_InfoStrPt = "用已连接的本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					break Out;
				}

				//判断套接字连接是否中断。
				if( System.currentTimeMillis() - m_LastPktRecvTime > 5000 ) //如果超过5000毫秒没有接收任何数据包，就判定连接已经断开了。
				{
					{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_REQUEST_CNCT_DIALOG;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁请求连接对话框的消息。

					String p_InfoStrPt = "超过5000毫秒没有接收任何数据包，判定套接字连接已经断开了。";
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					break Out;
				}
			}

			switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
			{
				case 0: //如果使用链表。
				{
					//初始化接收音频输出帧链表。
					m_RecvAdoOtptFrmLnkLstPt = new LinkedList< byte[] >(); //创建接收音频输出帧链表。
					Log.i( m_CurClsNameStrPt, "创建并初始化接收音频输出帧链表对象成功。" );

					//初始化接收视频输出帧链表。
					m_RecvVdoOtptFrmLnkLstPt = new LinkedList< byte[] >(); //创建接收视频输出帧链表。
					Log.i( m_CurClsNameStrPt, "创建并初始化接收视频输出帧链表对象成功。" );
					break;
				}
				case 1: //如果使用自适应抖动缓冲器。
				{
					//初始化音频自适应抖动缓冲器。
					m_AAjbPt = new AAjb();
					if( m_AAjbPt.Init( m_AdoOtptPt.m_SmplRate, m_AdoOtptPt.m_FrmLen, 1, 1, 0, m_AAjbMinNeedBufFrmCnt, m_AAjbMaxNeedBufFrmCnt, m_AAjbMaxCntuLostFrmCnt, m_AAjbAdaptSensitivity, m_ErrInfoVarStrPt ) == 0 )
					{
						Log.i( m_CurClsNameStrPt, "创建并初始化音频自适应抖动缓冲器成功。" );
					}
					else
					{
						String p_InfoStrPt = "创建并初始化音频自适应抖动缓冲器失败。原因：" + m_ErrInfoVarStrPt.GetStr();
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}

					//初始化视频自适应抖动缓冲器。
					m_VAjbPt = new VAjb();
					if( m_VAjbPt.Init( 1, m_VAjbMinNeedBufFrmCnt, m_VAjbMaxNeedBufFrmCnt, m_VAjbAdaptSensitivity, m_ErrInfoVarStrPt ) == 0 )
					{
						Log.i( m_CurClsNameStrPt, "创建并初始化视频自适应抖动缓冲器成功。" );
					}
					else
					{
						String p_InfoStrPt = "创建并初始化视频自适应抖动缓冲器失败。原因：" + m_ErrInfoVarStrPt.GetStr();
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}
					break;
				}
			}

			m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间为当前时间。
			m_LastPktRecvTime = m_LastPktSendTime; //设置最后一个数据包的接收时间为当前时间。

			m_LastSendAdoInptFrmIsAct = 0; //设置最后发送的一个音频输入帧为无语音活动。
			m_LastSendAdoInptFrmTimeStamp = 0 - 1; //设置最后一个发送音频输入帧的时间戳为0的前一个，因为第一次发送音频输入帧时会递增一个步进。
			m_LastSendVdoInptFrmTimeStamp = 0 - 1; //设置最后一个发送视频输入帧的时间戳为0的前一个，因为第一次发送视频输入帧时会递增一个步进。

			m_LastGetAdoOtptFrmIsAct = 0; //设置最后一个取出的音频输出帧为无语音活动，因为如果不使用音频输出，只使用视频输出时，可以保证视频正常输出。
			m_LastGetAdoOtptFrmVdoOtptFrmTimeStamp = 0; //设置最后一个取出的音频输出帧对应视频输出帧的时间戳为0。

			{
				String p_InfoStrPt = "开始对讲。";
				Log.i( m_CurClsNameStrPt, p_InfoStrPt );
				Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
				if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
			}

			{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.VIBRATE;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。

			p_Rslt = 0; //设置本函数执行成功。
		}

		return p_Rslt;
	}

	//用户定义的处理函数。
	@Override public int UserPocs()
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
		int p_TmpInt;

		Out:
		{
			//接收远端发送过来的一个数据包。
			if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.RecvPkt( m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
				( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 0, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
			{
				if( m_TmpHTLongPt.m_Val != -1 ) //如果用已连接的本端套接字接收一个连接的远端套接字发送的数据包成功。
				{
					m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。

					if( m_TmpHTLongPt.m_Val == 0 ) //如果数据包的数据长度为0。
					{
						Log.e( m_CurClsNameStrPt, "接收到一个数据包的数据长度为" + m_TmpHTLongPt.m_Val + "，表示没有数据，无法继续接收。" );
						break Out;
					}
					else if( m_TmpBytePt[0] == PKT_TYP_HTBT ) //如果是心跳包。
					{
						if( m_TmpHTLongPt.m_Val > 1 ) //如果心跳包的数据长度大于1。
						{
							Log.e( m_CurClsNameStrPt, "接收到一个心跳包的数据长度为" + m_TmpHTLongPt.m_Val + "大于1，表示还有其他数据，无法继续接收。" );
							break Out;
						}

						Log.i( m_CurClsNameStrPt, "接收到一个心跳包。" );
					}
					else if( m_TmpBytePt[0] == PKT_TYP_ADO_FRM ) //如果是音频输出帧包。
					{
						if( m_TmpHTLongPt.m_Val < 1 + 4 ) //如果音频输出帧包的数据长度小于1 + 4，表示没有音频输出帧时间戳。
						{
							Log.e( m_CurClsNameStrPt, "接收到一个音频输出帧包的数据长度为" + m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有音频输出帧时间戳，无法继续接收。" );
							break Out;
						}

						//读取音频输出帧时间戳。
						p_TmpInt = ( m_TmpBytePt[1] & 0xFF ) + ( ( m_TmpBytePt[2] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[3] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[4] & 0xFF ) << 24 );

						//将音频输出帧放入链表或自适应抖动缓冲器。
						switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
						{
							case 0: //如果使用链表。
							{
								if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
								{
									if( m_RecvAdoOtptFrmLnkLstPt.size() <= 50 )
									{
										synchronized( m_RecvAdoOtptFrmLnkLstPt )
										{
											m_RecvAdoOtptFrmLnkLstPt.addLast( Arrays.copyOfRange( m_TmpBytePt, 1 + 4, ( int ) ( m_TmpHTLongPt.m_Val ) ) );
										}
										Log.i( m_CurClsNameStrPt, "接收到一个有语音活动的音频输出帧包，并放入接收音频输出帧链表成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
									}
									else
									{
										Log.i( m_CurClsNameStrPt, "接收到一个有语音活动的音频输出帧包，但接收音频输出帧链表已满。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
									}
								}
								else //如果该音频输出帧为无语音活动。
								{
									Log.i( m_CurClsNameStrPt, "接收到一个无语音活动的音频输出帧包，无需放入接收音频输出帧链表。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
								}
								break;
							}
							case 1: //如果使用自适应抖动缓冲器。
							{
								if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
								{
									m_AAjbPt.PutOneByteFrm( p_TmpInt, m_TmpBytePt, 1 + 4, m_TmpHTLongPt.m_Val - 1 - 4, 1, null );
									Log.i( m_CurClsNameStrPt, "接收到一个有语音活动的音频输出帧包，并放入音频自适应抖动缓冲器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
								}
								else //如果该音频输出帧为无语音活动。
								{
									m_AAjbPt.PutOneByteFrm( p_TmpInt, m_TmpBytePt, 1 + 4, 0, 1, null );
									Log.i( m_CurClsNameStrPt, "接收到一个无语音活动的音频输出帧包，并放入音频自适应抖动缓冲器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
								}

								HTInt p_CurHaveBufActFrmCntPt = new HTInt(); //存放当前已缓冲有活动帧的数量。
								HTInt p_CurHaveBufInactFrmCntPt = new HTInt(); //存放当前已缓冲无活动帧的数量。
								HTInt p_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
								HTInt p_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
								HTInt p_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
								HTInt p_MaxCntuLostFrmCntPt = new HTInt(); //存放最大连续丢失帧的数量。
								HTInt p_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。
								m_AAjbPt.GetBufFrmCnt( p_CurHaveBufActFrmCntPt, p_CurHaveBufInactFrmCntPt, p_CurHaveBufFrmCntPt, p_MinNeedBufFrmCntPt, p_MaxNeedBufFrmCntPt, p_MaxCntuLostFrmCntPt, p_CurNeedBufFrmCntPt, 1, null );
								Log.i( m_CurClsNameStrPt, "音频自适应抖动缓冲器：有活动帧：" + p_CurHaveBufActFrmCntPt.m_Val + "，无活动帧：" + p_CurHaveBufInactFrmCntPt.m_Val + "，帧：" + p_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrmCntPt.m_Val + "，最大丢帧：" + p_MaxCntuLostFrmCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrmCntPt.m_Val + "。" );

								break;
							}
						}
					}
					else if( m_TmpBytePt[0] == PKT_TYP_VDO_FRM ) //如果是视频输出帧包。
					{
						if( m_TmpHTLongPt.m_Val < 1 + 4 ) //如果视频输出帧包的数据长度小于1 + 4，表示没有视频输出帧时间戳。
						{
							Log.e( m_CurClsNameStrPt, "接收到一个视频输出帧包的数据长度为" + m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有视频输出帧时间戳，无法继续接收。" );
							break Out;
						}

						//读取视频输出帧时间戳。
						p_TmpInt = ( m_TmpBytePt[1] & 0xFF ) + ( ( m_TmpBytePt[2] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[3] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[4] & 0xFF ) << 24 );

						//将视频输出帧放入链表或自适应抖动缓冲器。
						switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
						{
							case 0: //如果使用链表。
							{
								if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
								{
									if( m_RecvVdoOtptFrmLnkLstPt.size() <= 50 )
									{
										synchronized( m_RecvVdoOtptFrmLnkLstPt )
										{
											m_RecvVdoOtptFrmLnkLstPt.addLast( Arrays.copyOfRange( m_TmpBytePt, 1 + 4, ( int ) ( m_TmpHTLongPt.m_Val ) ) );
										}
										Log.i( m_CurClsNameStrPt, "接收到一个有图像活动的视频输出帧包，并放入接收视频输出帧链表成功。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
									}
									else
									{
										Log.i( m_CurClsNameStrPt, "接收到一个有图像活动的视频输出帧包，但接收视频输出帧链表已满。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
									}
								}
								else //如果该视频输出帧为无图像活动。
								{
									Log.i( m_CurClsNameStrPt, "接收到一个无图像活动的视频输出帧包，无需放入接收视频输出帧链表。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
								}
								break;
							}
							case 1: //如果使用自适应抖动缓冲器。
							{
								if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
								{
									m_VAjbPt.PutOneByteFrm( System.currentTimeMillis(), p_TmpInt, m_TmpBytePt, 1 + 4, m_TmpHTLongPt.m_Val - 1 - 4, 1, null );
									Log.i( m_CurClsNameStrPt, "接收到一个有图像活动的视频输出帧包，并放入视频自适应抖动缓冲器成功。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "，类型：" + ( m_TmpBytePt[13] & 0xff ) + "。" );
								}
								else //如果该视频输出帧为无图像活动。
								{
									Log.i( m_CurClsNameStrPt, "接收到一个无图像活动的视频输出帧包，无需放入视频自适应抖动缓冲器。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
								}

								HTInt p_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
								HTInt p_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
								HTInt p_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
								HTInt p_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。
								m_VAjbPt.GetBufFrmCnt( p_CurHaveBufFrmCntPt, p_MinNeedBufFrmCntPt, p_MaxNeedBufFrmCntPt, p_CurNeedBufFrmCntPt, 1, null );
								Log.i( m_CurClsNameStrPt, "视频自适应抖动缓冲器：帧：" + p_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrmCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrmCntPt.m_Val + "。" );

								break;
							}
						}
					}
					else if( m_TmpBytePt[0] == PKT_TYP_EXIT ) //如果是退出包。
					{
						if( m_TmpHTLongPt.m_Val > 1 ) //如果退出包的数据长度大于1。
						{
							Log.e( m_CurClsNameStrPt, "接收到一个退出包的数据长度为" + m_TmpHTLongPt.m_Val + "大于1，表示还有其他数据，无法继续接收。" );
							break Out;
						}

						m_IsRecvExitPkt = 1; //设置已经接收到退出包。
						RqirExit( 1, 0 ); //请求退出。

						String p_InfoStrPt = "接收到一个退出包。";
						Log.i( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
					}
				}
				else //如果用已连接的本端套接字接收一个连接的远端套接字发送的数据包超时。
				{

				}
			}
			else //如果用已连接的本端套接字接收一个连接的远端套接字发送的数据包失败。
			{
				String p_InfoStrPt = "用已连接的本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
				Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
				break Out;
			}

			//发送心跳包。
			if( System.currentTimeMillis() - m_LastPktSendTime >= 100 ) //如果超过100毫秒没有发送任何数据包，就发送一个心跳包。
			{
				m_TmpBytePt[0] = PKT_TYP_HTBT; //设置心跳包。
				if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
					( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
				{
					m_LastPktSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。
					Log.i( m_CurClsNameStrPt, "发送一个心跳包成功。" );
				}
				else
				{
					String p_InfoStrPt = "发送一个心跳包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					break Out;
				}
			}

			//判断套接字连接是否中断。
			if( System.currentTimeMillis() - m_LastPktRecvTime > 5000 ) //如果超过5000毫秒没有接收任何数据包，就判定连接已经断开了。
			{
				String p_InfoStrPt = "超过5000毫秒没有接收任何数据包，判定套接字连接已经断开了。";
				Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
				break Out;
			}

			if( m_XfrMode == 0 ) //如果传输模式为实时半双工（一键通）。
			{
				if( m_PttBtnIsDown == 0 ) //如果一键即按即通按钮为弹起。
				{
					if( ( m_AdoInptPt.m_IsUseAdoInpt != 0 ) && ( m_AdoOtptPt.m_IsUseAdoOtpt == 0 ) ) //如果要使用音频输入，且不使用音频输出。
					{
						m_AdoInptPt.m_IsUseAdoInpt = 0;
						m_AdoOtptPt.m_IsUseAdoOtpt = 1;
						RqirExit( 3, 0 ); //请求重启。
					}

					if( ( m_VdoInptPt.m_IsUseVdoInpt != 0 ) && ( m_VdoOtptPt.m_IsUseVdoOtpt == 0 ) ) //如果要使用视频输入，且不使用视频输出。
					{
						m_VdoInptPt.m_IsUseVdoInpt = 0;
						m_VdoOtptPt.m_IsUseVdoOtpt = 1;
						RqirExit( 3, 0 ); //请求重启。
						{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.REBUILD_SURFACE_VIEW;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送重建Surface视图消息。
					}
				}
				else //如果一键即按即通按钮为按下。
				{
					if( m_PttDownIsNoVibrate == 1 ) //如果一键即按即通按钮按下后还没有振动。
					{
						m_PttDownIsNoVibrate = 0;
						{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.VIBRATE;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
					}

					if( ( m_AdoInptPt.m_IsUseAdoInpt == 0 ) && ( m_AdoOtptPt.m_IsUseAdoOtpt != 0 ) ) //如果不使用音频输入，且要使用音频输出。
					{
						m_AdoInptPt.m_IsUseAdoInpt = 1;
						m_AdoOtptPt.m_IsUseAdoOtpt = 0;
						m_PttDownIsNoVibrate = 1;
						RqirExit( 3, 0 ); //请求重启。
					}

					if( ( m_VdoInptPt.m_IsUseVdoInpt == 0 ) && ( m_VdoOtptPt.m_IsUseVdoOtpt != 0 ) ) //如果不使用视频输入，且要使用视频输出。
					{
						m_VdoInptPt.m_IsUseVdoInpt = 1;
						m_VdoOtptPt.m_IsUseVdoOtpt = 0;
						m_PttDownIsNoVibrate = 1;
						RqirExit( 3, 0 ); //请求重启。
						{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.REBUILD_SURFACE_VIEW;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送重建Surface视图消息。
					}
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		return p_Rslt;
	}

	//用户定义的销毁函数。
	@Override public void UserDstoy()
	{
		if( ( m_ExitFlag == 1 ) && ( ( m_TcpClntSoktPt != null ) || ( ( m_UdpSoktPt != null ) && ( m_UdpSoktPt.GetRmtAddr( null, null, null, 0, null ) == 0 ) ) ) ) //如果本线程接收到退出请求，且本端TCP协议客户端套接字不为空或本端UDP协议套接字不为空且已连接远端。
		{
			SendExitPkt:
			{
				//发送退出包。
				m_TmpBytePt[0] = PKT_TYP_EXIT; //设置退出包。
				if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) != 0 ) ) ||
					( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, 1, ( short ) 0, 10, 0, m_ErrInfoVarStrPt ) != 0 ) ) )
				{
					String p_InfoStrPt = "发送一个退出包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
					break SendExitPkt;
				}

				m_LastPktSendTime = System.currentTimeMillis(); //记录最后一个数据包的发送时间。

				{
					String p_InfoStrPt = "发送一个退出包成功。";
					Log.i( m_CurClsNameStrPt, p_InfoStrPt );
					Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
				}

				if( m_IsRecvExitPkt == 0 ) //如果没有接收到退出包。
				{
					while( true ) //循环接收退出包。
					{
						if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.RecvPkt( m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 5000, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
							( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.RecvPkt( null, null, null, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 5000, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
						{
							if( m_TmpHTLongPt.m_Val != -1 ) //如果用已连接的本端套接字接收一个连接的远端套接字发送的数据包成功。
							{
								m_LastPktRecvTime = System.currentTimeMillis(); //记录最后一个数据包的接收时间。

								if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[0] == PKT_TYP_EXIT ) ) //如果是退出包。
								{
									String p_InfoStrPt = "接收到一个退出包。";
									Log.i( m_CurClsNameStrPt, p_InfoStrPt );
									Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
									break SendExitPkt;
								}
								else //如果是其他包，继续接收。
								{

								}
							}
							else //如果用已连接的本端套接字接收一个连接的远端套接字发送的数据包超时。
							{
								String p_InfoStrPt = "用已连接的本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
								Log.e( m_CurClsNameStrPt, p_InfoStrPt );
								Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
								break SendExitPkt;
							}
						}
						else //如果用已连接的本端套接字接收一个连接的远端套接字发送的数据包失败。
						{
							String p_InfoStrPt = "用已连接的本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_ErrInfoVarStrPt.GetStr();
							Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
							break SendExitPkt;
						}
					}
				}
			}

			String p_InfoStrPt = "中断对讲。";
			Log.i( m_CurClsNameStrPt, p_InfoStrPt );
			Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
			if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
		}

		//销毁本端TCP协议服务端套接字。
		if( m_TcpSrvrSoktPt != null )
		{
			m_TcpSrvrSoktPt.Dstoy( null ); //关闭并销毁已创建的本端TCP协议服务端套接字。
			m_TcpSrvrSoktPt = null;

			String p_InfoStrPt = "关闭并销毁已创建的本端TCP协议服务端套接字成功。";
			Log.i( m_CurClsNameStrPt, p_InfoStrPt );
			Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
		}

		//销毁本端TCP协议客户端套接字。
		if( m_TcpClntSoktPt != null )
		{
			m_TcpClntSoktPt.Dstoy( ( short ) -1, null ); //关闭并销毁已创建的本端TCP协议客户端套接字。
			m_TcpClntSoktPt = null;

			String p_InfoStrPt = "关闭并销毁已创建的本端TCP协议客户端套接字成功。";
			Log.i( m_CurClsNameStrPt, p_InfoStrPt );
			Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
		}

		//销毁本端UDP协议套接字。
		if( m_UdpSoktPt != null )
		{
			m_UdpSoktPt.Dstoy( null ); //关闭并销毁已创建的本端UDP协议套接字。
			m_UdpSoktPt = null;

			String p_InfoStrPt = "关闭并销毁已创建的本端UDP协议套接字成功。";
			Log.i( m_CurClsNameStrPt, p_InfoStrPt );
			Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
		}

		//销毁接收音频输出帧的链表。
		if( m_RecvAdoOtptFrmLnkLstPt != null )
		{
			m_RecvAdoOtptFrmLnkLstPt.clear();
			m_RecvAdoOtptFrmLnkLstPt = null;

			Log.i( m_CurClsNameStrPt, "销毁接收音频输出帧链表成功。" );
		}

		//销毁接收视频输出帧的链表。
		if( m_RecvVdoOtptFrmLnkLstPt != null )
		{
			m_RecvVdoOtptFrmLnkLstPt.clear();
			m_RecvVdoOtptFrmLnkLstPt = null;

			Log.i( m_CurClsNameStrPt, "销毁接收视频输出帧链表成功。" );
		}

		//销毁音频自适应抖动缓冲器。
		if( m_AAjbPt != null )
		{
			m_AAjbPt.Dstoy( null );
			m_AAjbPt = null;

			Log.i( m_CurClsNameStrPt, "销毁音频自适应抖动缓冲器成功。" );
		}

		//销毁视频自适应抖动缓冲器。
		if( m_VAjbPt != null )
		{
			m_VAjbPt.Dstoy( null );
			m_VAjbPt = null;

			Log.i( m_CurClsNameStrPt, "销毁视频自适应抖动缓冲器成功。" );
		}

		if( m_IsCreateSrvrOrClnt == 1 ) //如果是创建服务端。
		{
			if( ( m_ExitFlag == 1 ) && ( m_IsRecvExitPkt == 1 ) ) //如果本线程接收到退出请求，且接收到了退出包。
			{
				String p_InfoStrPt = "由于是创建服务端，且本线程接收到退出请求，且接收到了退出包，表示是远端TCP协议客户端套接字主动退出，本线程重新初始化来继续保持监听。";
				Log.i( m_CurClsNameStrPt, p_InfoStrPt );
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

				RqirExit( 2, 0 ); //请求重启。
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.REBUILD_SURFACE_VIEW;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送重建Surface视图消息。
			}
			else if( ( m_ExitFlag == 0 ) && ( m_ExitCode == -1 ) && ( m_RequestCnctRslt == 2 ) ) //如果本线程没收到退出请求，且退出代码为初始化失败，且请求连接的结果为拒绝。
			{
				String p_InfoStrPt = "由于是创建服务端，且本线程没收到退出请求，且初始化失败，且请求连接的结果为拒绝，表示是拒绝本次连接，本线程重新初始化来继续保持监听。";
				Log.i( m_CurClsNameStrPt, p_InfoStrPt );
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

				RqirExit( 2, 0 ); //请求重启。
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.REBUILD_SURFACE_VIEW;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送重建Surface视图消息。
			}
			else if( ( m_ExitFlag == 0 ) && ( m_ExitCode == -2 ) ) //如果本线程没收到退出请求，且退出代码为处理失败。
			{
				String p_InfoStrPt = "由于是创建服务端，且本线程没收到退出请求，且退出码为处理失败，表示是处理失败或连接异常断开，本线程重新初始化来继续保持监听。";
				Log.i( m_CurClsNameStrPt, p_InfoStrPt );
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

				RqirExit( 2, 0 ); //请求重启。
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.REBUILD_SURFACE_VIEW;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送重建Surface视图消息。
			}
			else //其他情况，本线程直接退出。
			{
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_MEDIA_PROC_THREAD;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁媒体处理线程的消息。
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.VIBRATE;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.REBUILD_SURFACE_VIEW;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送重建Surface视图消息。
			}
		}
		else if( m_IsCreateSrvrOrClnt == 0 ) //如果是创建客户端。
		{
			if( ( m_ExitFlag == 0 ) && ( m_ExitCode == -2 ) ) //如果本线程没收到退出请求，且退出代码为处理失败。
			{
				String p_InfoStrPt = "由于是创建客户端，且本线程没收到退出请求，且退出码为处理失败，表示是处理失败或连接异常断开，本线程重新初始化来重连服务端。";
				Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

				RqirExit( 2, 0 ); //请求重启。
			}
			else //其他情况，本线程直接退出。
			{
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.DSTOY_MEDIA_PROC_THREAD;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁媒体处理线程的消息。
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.VIBRATE;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
				{Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.REBUILD_SURFACE_VIEW;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送重建Surface视图消息。
			}
		}
	}

	//用户定义的读取音视频输入帧函数。
	@Override public int UserReadAdoVdoInptFrm( short PcmAdoInptFrmPt[], short PcmAdoRsltFrmPt[], HTInt VoiceActStsPt, byte EncdAdoInptFrmPt[], HTLong EncdAdoInptFrmLenPt, HTInt EncdAdoInptFrmIsNeedTransPt,
												byte YU12VdoInptFrmPt[], HTInt YU12VdoInptFrmWidthPt, HTInt YU12VdoInptFrmHeightPt, byte EncdVdoInptFrmPt[], HTLong EncdVdoInptFrmLenPt )
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
		int p_FrmPktLen = 0; //存放输入输出帧数据包的数据长度，单位字节。
		int p_TmpInt32 = 0;

		Out:
		{
			//发送音频输入帧。
			if( PcmAdoInptFrmPt != null ) //如果要使用音频输入。
			{
				if( EncdAdoInptFrmPt != null ) //如果要使用已编码格式音频输入帧。
				{
					if( VoiceActStsPt.m_Val != 0 && EncdAdoInptFrmIsNeedTransPt.m_Val != 0 ) //如果本次音频输入帧为有语音活动，且需要传输。
					{
						System.arraycopy( EncdAdoInptFrmPt, 0, m_TmpBytePt, 1 + 4 + 4, ( int ) EncdAdoInptFrmLenPt.m_Val ); //设置音频输入输出帧。
						p_FrmPktLen = 1 + 4 + 4 + ( int )EncdAdoInptFrmLenPt.m_Val; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 视频输入帧时间戳 + 已编码格式音频输入帧。
					}
					else //如果本次音频输入帧为无语音活动，或不需要传输。
					{
						p_FrmPktLen = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
					}
				}
				else //如果要使用PCM格式音频输入帧。
				{
					if( VoiceActStsPt.m_Val != 0 ) //如果本次音频输入帧为有语音活动。
					{
						for( p_TmpInt32 = 0; p_TmpInt32 < PcmAdoRsltFrmPt.length; p_TmpInt32++ ) //设置音频输入输出帧。
						{
							m_TmpBytePt[1 + 4 + 4 + p_TmpInt32 * 2] = ( byte ) ( PcmAdoRsltFrmPt[p_TmpInt32] & 0xFF );
							m_TmpBytePt[1 + 4 + 4 + p_TmpInt32 * 2 + 1] = ( byte ) ( ( PcmAdoRsltFrmPt[p_TmpInt32] & 0xFF00 ) >> 8 );
						}
						p_FrmPktLen = 1 + 4 + 4 + PcmAdoRsltFrmPt.length * 2; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 视频输入帧时间戳 + PCM格式音频输入帧。
					}
					else //如果本次音频输入帧为无语音活动，或不需要传输。
					{
						p_FrmPktLen = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
					}
				}

				//发送音频输入帧数据包。
				if( p_FrmPktLen != 1 + 4 ) //如果本音频输入帧为有语音活动，就发送。
				{
					m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

					//设置数据包类型为音频输入帧包。
					m_TmpBytePt[0] = PKT_TYP_ADO_FRM;
					//设置音频输入帧时间戳。
					m_TmpBytePt[1] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
					m_TmpBytePt[2] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
					m_TmpBytePt[3] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
					m_TmpBytePt[4] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );
					//设置视频输入帧时间戳。
					m_TmpBytePt[5] = ( byte ) ( m_LastSendVdoInptFrmTimeStamp & 0xFF );
					m_TmpBytePt[6] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
					m_TmpBytePt[7] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
					m_TmpBytePt[8] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

					if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, p_FrmPktLen, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
						( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, p_FrmPktLen, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
					{
						m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
						Log.i( m_CurClsNameStrPt, "发送一个有语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，视频输入帧时间戳：" + m_LastSendVdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。" );
					}
					else
					{
						String p_InfoStrPt = "发送一个有语音活动的音频输入帧包失败。原因：" + m_ErrInfoVarStrPt.GetStr() + "音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，视频输入帧时间戳：" + m_LastSendVdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。";
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}

					m_LastSendAdoInptFrmIsAct = 1; //设置最后一个发送的音频输入帧有语音活动。
				}
				else if( ( p_FrmPktLen == 1 + 4 ) && ( m_LastSendAdoInptFrmIsAct != 0 ) ) //如果本音频输入帧为无语音活动，但最后一个发送的音频输入帧为有语音活动，就发送。
				{
					m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

					//设置数据包类型为音频输入帧包。
					m_TmpBytePt[0] = PKT_TYP_ADO_FRM;
					//设置音频输入帧时间戳。
					m_TmpBytePt[1] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
					m_TmpBytePt[2] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
					m_TmpBytePt[3] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
					m_TmpBytePt[4] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

					if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, p_FrmPktLen, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
						( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, p_FrmPktLen, ( short ) 0, 10, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
					{
						m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
						Log.i( m_CurClsNameStrPt, "发送一个无语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。" );
					}
					else
					{
						String p_InfoStrPt = "发送一个无语音活动的音频输入帧包失败。原因：" + m_ErrInfoVarStrPt.GetStr() + "音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。";
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}

					m_LastSendAdoInptFrmIsAct = 0; //设置最后一个发送的音频输入帧无语音活动。
				}
				else //如果本音频输入帧为无语音活动，且最后一个发送的音频输入帧为无语音活动，无需发送。
				{
					Log.i( m_CurClsNameStrPt, "本音频输入帧为无语音活动，且最后一个发送的音频输入帧为无语音活动，无需发送。" );
				}
			}

			//发送视频输入帧。
			if( YU12VdoInptFrmPt != null ) //如果要使用视频输入。
			{
				if( EncdVdoInptFrmPt != null ) //如果要使用已编码格式视频输入帧。
				{
					if( EncdVdoInptFrmLenPt.m_Val != 0 ) //如果本次视频输入帧为有图像活动。
					{
						System.arraycopy( EncdVdoInptFrmPt, 0, m_TmpBytePt, 1 + 4 + 4, ( int ) EncdVdoInptFrmLenPt.m_Val ); //设置视频输入输出帧。
						p_FrmPktLen = 1 + 4 + 4 + ( int ) EncdVdoInptFrmLenPt.m_Val; //数据包长度 = 数据包类型 + 视频输入帧时间戳 + 音频输入帧时间戳 + 已编码格式视频输入帧。
					}
					else
					{
						p_FrmPktLen = 1 + 4; //数据包长度 = 数据包类型 + 视频输入帧时间戳。
					}
				}
				else //如果要使用YU12格式视频输入帧。
				{
					//设置视频输入帧宽度。
					m_TmpBytePt[9] = ( byte ) ( YU12VdoInptFrmWidthPt.m_Val & 0xFF );
					m_TmpBytePt[10] = ( byte ) ( ( YU12VdoInptFrmWidthPt.m_Val & 0xFF00 ) >> 8 );
					m_TmpBytePt[11] = ( byte ) ( ( YU12VdoInptFrmWidthPt.m_Val & 0xFF0000 ) >> 16 );
					m_TmpBytePt[12] = ( byte ) ( ( YU12VdoInptFrmWidthPt.m_Val & 0xFF000000 ) >> 24 );
					//设置视频输入帧高度。
					m_TmpBytePt[13] = ( byte ) ( YU12VdoInptFrmHeightPt.m_Val & 0xFF );
					m_TmpBytePt[14] = ( byte ) ( ( YU12VdoInptFrmHeightPt.m_Val & 0xFF00 ) >> 8 );
					m_TmpBytePt[15] = ( byte ) ( ( YU12VdoInptFrmHeightPt.m_Val & 0xFF0000 ) >> 16 );
					m_TmpBytePt[16] = ( byte ) ( ( YU12VdoInptFrmHeightPt.m_Val & 0xFF000000 ) >> 24 );

					System.arraycopy( YU12VdoInptFrmPt, 0, m_TmpBytePt, 1 + 4 + 4 + 4 + 4, YU12VdoInptFrmPt.length ); //设置视频输入输出帧。
					p_FrmPktLen = 1 + 4 + 4 + 4 + 4 + YU12VdoInptFrmPt.length; //数据包长度 = 数据包类型 + 视频输入帧时间戳 + 音频输入帧时间戳 + 视频输入帧宽度 + 视频输入帧高度 + YU12格式视频输入帧。
				}

				//发送视频输入帧数据包。
				if( p_FrmPktLen != 1 + 4 ) //如果本视频输入帧为有图像活动，就发送。
				{
					m_LastSendVdoInptFrmTimeStamp += 1; //视频输入帧的时间戳递增一个步进。

					//设置数据包类型为视频输入帧包。
					m_TmpBytePt[0] = PKT_TYP_VDO_FRM;
					//设置视频输入帧时间戳。
					m_TmpBytePt[1] = ( byte ) ( m_LastSendVdoInptFrmTimeStamp & 0xFF );
					m_TmpBytePt[2] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
					m_TmpBytePt[3] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
					m_TmpBytePt[4] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );
					//设置音频输入帧时间戳。
					m_TmpBytePt[5] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
					m_TmpBytePt[6] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
					m_TmpBytePt[7] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
					m_TmpBytePt[8] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

					if( ( ( m_UseWhatXfrPrtcl == 0 ) && ( m_TcpClntSoktPt.SendPkt( m_TmpBytePt, p_FrmPktLen, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) ||
						( ( m_UseWhatXfrPrtcl == 1 ) && ( m_UdpSoktPt.SendPkt( 4, null, null, m_TmpBytePt, p_FrmPktLen, ( short ) 0, 1, 0, m_ErrInfoVarStrPt ) == 0 ) ) )
					{
						m_LastPktSendTime = System.currentTimeMillis(); //设置最后一个数据包的发送时间。
						Log.i( m_CurClsNameStrPt, "发送一个有图像活动的视频输入帧包成功。视频输入帧时间戳：" + m_LastSendVdoInptFrmTimeStamp + "，音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "，类型：" + ( m_TmpBytePt[13] & 0xff ) + "。" );
					}
					else
					{
						String p_InfoStrPt = "发送一个有图像活动的视频输入帧包失败。视频输入帧时间戳：" + m_LastSendVdoInptFrmTimeStamp + "，音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "，类型：" + ( m_TmpBytePt[13] & 0xff ) + "。原因：" + m_ErrInfoVarStrPt.GetStr() + "。";
						Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
						break Out;
					}
				}
				else //如果本视频输入帧为无图像活动，无需发送。
				{
					Log.i( m_CurClsNameStrPt, "本视频输入帧为无图像活动，无需发送。" );
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		return p_Rslt;
	}

	//用户定义的写入音频输出帧函数。
	@Override public void UserWriteAdoOtptFrm( short PcmAdoOtptFrmPt[], byte EncdAdoOtptFrmPt[], HTLong AdoOtptFrmLenPt )
	{
		int p_AdoOtptFrmTimeStamp = 0;
		byte p_AdoOtptFrmPt[] = null;
		long p_AdoOtptFrmLen = 0;
		int p_TmpInt32;

		Out:
		{
			//取出并写入音频输出帧。
			{
				//从链表或自适应抖动缓冲器取出一个音频输出帧。
				switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
				{
					case 0: //如果使用链表。
					{
						if( m_RecvAdoOtptFrmLnkLstPt.size() != 0 ) //如果接收音频输出帧链表不为空。
						{
							synchronized( m_RecvAdoOtptFrmLnkLstPt )
							{
								p_AdoOtptFrmPt = m_RecvAdoOtptFrmLnkLstPt.getFirst(); //获取接收音频输出帧链表的第一个音频输出帧。
								m_RecvAdoOtptFrmLnkLstPt.removeFirst(); //删除接收音频输出帧链表的第一个音频输出帧。
							}
							p_AdoOtptFrmLen = p_AdoOtptFrmPt.length;
						}

						if( p_AdoOtptFrmLen != 0 ) //如果音频输出帧为有语音活动。
						{
							Log.i( m_CurClsNameStrPt, "从接收音频输出帧链表取出一个有语音活动的音频输出帧。数据长度：" + p_AdoOtptFrmLen + "。" );
						}
						else //如果音频输出帧为无语音活动。
						{
							Log.i( m_CurClsNameStrPt, "从接收音频输出帧链表取出一个无语音活动的音频输出帧。数据长度：" + p_AdoOtptFrmLen + "。" );
						}

						break;
					}
					case 1: //如果使用自适应抖动缓冲器。
					{
						//从音频自适应抖动缓冲器取出一个音频输出帧。
						m_AAjbPt.GetOneByteFrm( m_TmpHTInt2Pt, m_TmpByte2Pt, 0, m_TmpByte2Pt.length, m_TmpHTLong2Pt, 1, null );
						p_AdoOtptFrmTimeStamp = m_TmpHTInt2Pt.m_Val;
						p_AdoOtptFrmPt = m_TmpByte2Pt;
						p_AdoOtptFrmLen = m_TmpHTLong2Pt.m_Val;

						if( p_AdoOtptFrmLen > 0 ) //如果音频输出帧为有语音活动。
						{
							m_LastGetAdoOtptFrmVdoOtptFrmTimeStamp = ( p_AdoOtptFrmPt[0] & 0xFF ) + ( ( p_AdoOtptFrmPt[1] & 0xFF ) << 8 ) + ( ( p_AdoOtptFrmPt[2] & 0xFF ) << 16 ) + ( ( p_AdoOtptFrmPt[3] & 0xFF ) << 24 ); //设置最后一个取出的音频输出帧对应视频输出帧的时间戳。
							m_LastGetAdoOtptFrmIsAct = 1; //设置最后一个取出的音频输出帧为有语音活动。
							Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个有语音活动的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，视频输出帧时间戳：" + m_LastGetAdoOtptFrmVdoOtptFrmTimeStamp + "，数据长度：" + p_AdoOtptFrmLen + "。" );
						}
						else if( p_AdoOtptFrmLen == 0 ) //如果音频输出帧为无语音活动。
						{
							m_LastGetAdoOtptFrmIsAct = 0; //设置最后一个取出的音频输出帧为无语音活动。
							Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个无语音活动的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，数据长度：" + p_AdoOtptFrmLen + "。" );
						}
						else //如果音频输出帧为丢失。
						{
							m_LastGetAdoOtptFrmIsAct = 1; //设置最后一个取出的音频输出帧为有语音活动。
							Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个丢失的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，视频输出帧时间戳：" + m_LastGetAdoOtptFrmVdoOtptFrmTimeStamp + "，数据长度：" + p_AdoOtptFrmLen + "。" );
						}

						HTInt p_CurHaveBufActFrmCntPt = new HTInt(); //存放当前已缓冲有活动帧的数量。
						HTInt p_CurHaveBufInactFrmCntPt = new HTInt(); //存放当前已缓冲无活动帧的数量。
						HTInt p_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
						HTInt p_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
						HTInt p_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
						HTInt p_MaxCntuLostFrmCntPt = new HTInt(); //存放最大连续丢失帧的数量。
						HTInt p_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。
						m_AAjbPt.GetBufFrmCnt( p_CurHaveBufActFrmCntPt, p_CurHaveBufInactFrmCntPt, p_CurHaveBufFrmCntPt, p_MinNeedBufFrmCntPt, p_MaxNeedBufFrmCntPt, p_MaxCntuLostFrmCntPt, p_CurNeedBufFrmCntPt, 1, null );
						Log.i( m_CurClsNameStrPt, "音频自适应抖动缓冲器：有活动帧：" + p_CurHaveBufActFrmCntPt.m_Val + "，无活动帧：" + p_CurHaveBufInactFrmCntPt.m_Val + "，帧：" + p_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrmCntPt.m_Val + "，最大丢帧：" + p_MaxCntuLostFrmCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrmCntPt.m_Val + "。" );

						break;
					}
				}

				//写入音频输出帧。
				if( p_AdoOtptFrmLen > 0 ) //如果音频输出帧为有语音活动。
				{
					if( PcmAdoOtptFrmPt != null ) //如果要使用PCM格式音频输出帧。
					{
						if( p_AdoOtptFrmLen - 4 != PcmAdoOtptFrmPt.length * 2 )
						{
							Arrays.fill( PcmAdoOtptFrmPt, ( short ) 0 );
							Log.e( m_CurClsNameStrPt, "音频输出帧的数据长度不等于PCM格式的数据长度。音频输出帧：" + ( p_AdoOtptFrmLen - 4 ) + "，PCM格式：" + ( PcmAdoOtptFrmPt.length * 2 ) + "。" );
							break Out;
						}

						//写入PCM格式音频输出帧。
						for( p_TmpInt32 = 0; p_TmpInt32 < PcmAdoOtptFrmPt.length; p_TmpInt32++ )
						{
							PcmAdoOtptFrmPt[p_TmpInt32] = ( short ) ( ( p_AdoOtptFrmPt[4 + p_TmpInt32 * 2] & 0xFF ) | ( p_AdoOtptFrmPt[4 + p_TmpInt32 * 2 + 1] << 8 ) );
						}
					}
					else //如果要使用已编码格式音频输出帧。
					{
						if( p_AdoOtptFrmLen - 4 > EncdAdoOtptFrmPt.length )
						{
							AdoOtptFrmLenPt.m_Val = 0;
							Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度已超过已编码格式的数据长度。音频输出帧：" + ( p_AdoOtptFrmLen - 4 ) + "，已编码格式：" + EncdAdoOtptFrmPt.length + "。" );
							break Out;
						}

						//写入已编码格式音频输出帧。
						System.arraycopy( p_AdoOtptFrmPt, 4, EncdAdoOtptFrmPt, 0, ( int ) ( p_AdoOtptFrmLen - 4 ) );
						AdoOtptFrmLenPt.m_Val = p_AdoOtptFrmLen - 4;
					}
				}
				else if( p_AdoOtptFrmLen == 0 ) //如果音频输出帧为无语音活动。
				{
					if( PcmAdoOtptFrmPt != null ) //如果要使用PCM格式音频输出帧。
					{
						Arrays.fill( PcmAdoOtptFrmPt, ( short ) 0 );
					}
					else //如果要使用已编码格式音频输出帧。
					{
						AdoOtptFrmLenPt.m_Val = 0;
					}
				}
				else //如果音频输出帧为丢失。
				{
					if( PcmAdoOtptFrmPt != null ) //如果要使用PCM格式音频输出帧。
					{
						Arrays.fill( PcmAdoOtptFrmPt, ( short ) 0 );
					}
					else //如果要使用已编码格式音频输出帧。
					{
						AdoOtptFrmLenPt.m_Val = p_AdoOtptFrmLen;
					}
				}
			}
		}
	}

	//用户定义的获取PCM格式音频输出帧函数。
	@Override public void UserGetPcmAdoOtptFrm( short PcmOtptFrmPt[], long PcmAdoOtptFrmLen )
	{

	}

	//用户定义的写入视频输出帧函数。
	@Override public void UserWriteVdoOtptFrm( byte YU12VdoOtptFrmPt[], HTInt YU12VdoInptFrmWidthPt, HTInt YU12VdoInptFrmHeightPt, byte EncdVdoOtptFrmPt[], HTLong VdoOtptFrmLen )
	{
		int p_VdoOtptFrmTimeStamp = 0;
		int p_VdoOtptFrmAdoOtptFrmTimeStamp = 0;
		byte p_VdoOtptFrmPt[] = null;
		long p_VdoOtptFrmLen = 0;

		//从链表或自适应抖动缓冲器取出一个视频输出帧。
		switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
		{
			case 0: //如果使用链表。
			{
				if( m_RecvVdoOtptFrmLnkLstPt.size() != 0 ) //如果接收视频输出帧链表不为空。
				{
					synchronized( m_RecvVdoOtptFrmLnkLstPt )
					{
						p_VdoOtptFrmPt = m_RecvVdoOtptFrmLnkLstPt.getFirst(); //获取接收视频输出帧链表的第一个视频输出帧。
						m_RecvVdoOtptFrmLnkLstPt.removeFirst(); //删除接收视频输出帧链表的第一个视频输出帧。
					}
					p_VdoOtptFrmLen = p_VdoOtptFrmPt.length;
				}

				if( p_VdoOtptFrmLen != 0 ) //如果视频输出帧为有图像活动。
				{
					Log.i( m_CurClsNameStrPt, "从接收视频输出帧链表取出一个有图像活动的视频输出帧。数据长度：" + p_VdoOtptFrmLen + "。" );
				}
				else //如果视频输出帧为无图像活动。
				{
					Log.i( m_CurClsNameStrPt, "从接收视频输出帧链表取出一个无图像活动的视频输出帧。数据长度：" + p_VdoOtptFrmLen + "。" );
				}

				break;
			}
			case 1: //如果使用自适应抖动缓冲器。
			{
				HTInt p_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
				HTInt p_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
				HTInt p_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
				HTInt p_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。
				m_VAjbPt.GetBufFrmCnt( p_CurHaveBufFrmCntPt, p_MinNeedBufFrmCntPt, p_MaxNeedBufFrmCntPt, p_CurNeedBufFrmCntPt, 1, null );

				if( p_CurHaveBufFrmCntPt.m_Val != 0 ) //如果视频自适应抖动缓冲器不为空。
				{
					Log.i( m_CurClsNameStrPt, "视频自适应抖动缓冲器：帧：" + p_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrmCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrmCntPt.m_Val + "。" );

					//从视频自适应抖动缓冲器取出一个视频输出帧。
					if( m_AdoOtptPt.m_IsUseAdoOtpt != 0 && m_LastGetAdoOtptFrmIsAct != 0 ) //如果要使用音频输出，且最后一个取出的音频输出帧为有语音活动，就根据最后一个取出的音频输出帧对应视频输出帧的时间戳来取出。
					{
						m_VAjbPt.GetOneByteFrmWantTimeStamp( System.currentTimeMillis(), m_LastGetAdoOtptFrmVdoOtptFrmTimeStamp, m_TmpHTInt3Pt, m_TmpByte3Pt, 0, m_TmpByte3Pt.length, m_TmpHTLong3Pt, 1, null );
					}
					else //如果最后一个取出的音频输出帧为无语音活动，就根据直接取出。
					{
						m_VAjbPt.GetOneByteFrm( System.currentTimeMillis(), m_TmpHTInt3Pt, m_TmpByte3Pt, 0, m_TmpByte3Pt.length, m_TmpHTLong3Pt, 1, null );
					}
					p_VdoOtptFrmTimeStamp = m_TmpHTInt3Pt.m_Val;
					p_VdoOtptFrmPt = m_TmpByte3Pt;
					p_VdoOtptFrmLen = m_TmpHTLong3Pt.m_Val;

					if( p_VdoOtptFrmLen > 0 ) //如果视频输出帧为有图像活动。
					{
						Log.i( m_CurClsNameStrPt, "从视频自适应抖动缓冲器取出一个有图像活动的视频输出帧。时间戳：" + p_VdoOtptFrmTimeStamp + "，数据长度：" + p_VdoOtptFrmLen + "。" );
					}
					else //如果视频输出帧为无图像活动。
					{
						Log.i( m_CurClsNameStrPt, "从视频自适应抖动缓冲器取出一个无图像活动的视频输出帧。时间戳：" + p_VdoOtptFrmTimeStamp + "，数据长度：" + p_VdoOtptFrmLen + "。" );
					}
				}

				break;
			}
		}

		//写入视频输出帧。
		if( p_VdoOtptFrmLen > 0 ) //如果视频输出帧为有图像活动。
		{
			//读取视频输出帧对应音频输出帧的时间戳。
			p_VdoOtptFrmAdoOtptFrmTimeStamp = ( p_VdoOtptFrmPt[0] & 0xFF ) + ( ( p_VdoOtptFrmPt[1] & 0xFF ) << 8 ) + ( ( p_VdoOtptFrmPt[2] & 0xFF ) << 16 ) + ( ( p_VdoOtptFrmPt[3] & 0xFF ) << 24 );

			if( YU12VdoOtptFrmPt != null ) //如果要使用YU12格式视频输出帧。
			{
				//读取视频输出帧宽度。
				YU12VdoInptFrmWidthPt.m_Val = ( p_VdoOtptFrmPt[4] & 0xFF ) + ( ( p_VdoOtptFrmPt[5] & 0xFF ) << 8 ) + ( ( p_VdoOtptFrmPt[6] & 0xFF ) << 16 ) + ( ( p_VdoOtptFrmPt[7] & 0xFF ) << 24 );
				//读取视频输出帧高度。
				YU12VdoInptFrmHeightPt.m_Val = ( p_VdoOtptFrmPt[8] & 0xFF ) + ( ( p_VdoOtptFrmPt[9] & 0xFF ) << 8 ) + ( ( p_VdoOtptFrmPt[10] & 0xFF ) << 16 ) + ( ( p_VdoOtptFrmPt[11] & 0xFF ) << 24 );

				if( p_VdoOtptFrmLen - 4 - 4 - 4 != ( long )( YU12VdoInptFrmWidthPt.m_Val * YU12VdoInptFrmHeightPt.m_Val * 3 / 2 ) )
				{
					Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度不等于YU12格式的数据长度。视频输出帧：" + ( p_VdoOtptFrmLen - 4 - 4 - 4 ) + "，YU12格式：" + ( YU12VdoInptFrmWidthPt.m_Val * YU12VdoInptFrmHeightPt.m_Val * 3 / 2 ) + "。" );
					YU12VdoInptFrmWidthPt.m_Val = 0;
					YU12VdoInptFrmHeightPt.m_Val = 0;
					return;
				}

				//写入YU12格式视频输出帧。
				System.arraycopy( p_VdoOtptFrmPt, 4 + 4 + 4, YU12VdoOtptFrmPt, 0, ( int )( p_VdoOtptFrmLen - 4 - 4 - 4 ) );
			}
			else //如果要使用已编码格式视频输出帧。
			{
				if( p_VdoOtptFrmLen - 4 > VdoOtptFrmLen.m_Val )
				{
					VdoOtptFrmLen.m_Val = 0;
					Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度已超过已编码格式的数据长度。视频输出帧：" + ( p_VdoOtptFrmLen - 4 ) + "，已编码格式：" + VdoOtptFrmLen.m_Val + "。" );
					return;
				}

				//写入已编码格式视频输出帧。
				System.arraycopy( p_VdoOtptFrmPt, 4, EncdVdoOtptFrmPt, 0, ( int )( p_VdoOtptFrmLen - 4 ) );
				VdoOtptFrmLen.m_Val = p_VdoOtptFrmLen - 4;
			}
		}
		else if( p_VdoOtptFrmLen == 0 ) //如果视频输出帧为无图像活动。
		{
			if( YU12VdoOtptFrmPt != null ) //如果要使用YU12格式视频输出帧。
			{
				VdoOtptFrmLen.m_Val = 0;
			}
			else //如果要使用已编码格式视频输出帧。
			{
				VdoOtptFrmLen.m_Val = 0;
			}
		}
	}

	//用户定义的获取YU12格式视频输出帧函数。
	@Override public void UserGetYU12VdoOtptFrm( byte YU12VdoOtptFrmPt[], int YU12VdoOtptFrmWidth, int YU12VdoOtptFrmHeight )
	{

	}
}

//主界面类。
public class MainActivity extends AppCompatActivity implements View.OnTouchListener
{
	String m_CurClsNameStrPt = this.getClass().getSimpleName(); //存放当前类名称字符串。

	View m_MainLyotViewPt; //存放主布局视图的指针。
	View m_XfrPrtclStngLyotViewPt; //存放传输协议设置布局视图的指针。
	View m_StngLyotViewPt; //存放设置布局视图的指针。
	View m_AjbStngLyotViewPt; //存放音频自适应抖动缓冲器设置布局视图的指针。
	View m_SpeexAecStngLyotViewPt; //存放Speex声学回音消除器设置布局视图的指针。
	View m_WebRtcAecmStngLyotViewPt; //存放WebRtc定点版声学回音消除器设置布局视图的指针。
	View m_WebRtcAecStngLyotViewPt; //存放WebRtc浮点版声学回音消除器设置布局视图的指针。
	View m_SpeexWebRtcAecStngLyotViewPt; //存放SpeexWebRtc三重声学回音消除器设置布局视图的指针。
	View m_SpeexPrpocsNsStngLyotViewPt; //存放Speex预处理器的噪音抑制设置布局视图的指针。
	View m_WebRtcNsxStngLyotViewPt; //存放WebRtc定点版噪音抑制器设置布局视图的指针。
	View m_WebRtcNsStngLyotViewPt; //存放WebRtc浮点版噪音抑制器设置布局视图的指针。
	View m_SpeexPrpocsOtherStngLyotViewPt; //存放Speex预处理器的其他功能设置布局视图的指针。
	View m_SpeexCodecStngLyotViewPt; //存放Speex编解码器设置布局视图的指针。
	View m_OpenH264CodecStngLyotViewPt; //存放OpenH264编解码器设置布局视图的指针。
	View m_SystemH264CodecStngLyotViewPt; //存放系统自带H264编解码器设置布局视图的指针。

	View m_CurActivityLyotViewPt; //存放当前界面布局视图的指针。
	MyMediaPocsThrd m_MyMediaPocsThrdPt; //存放媒体处理线程的指针。
	MainActivityHandler m_MainActivityHandlerPt; //存放主界面消息处理的指针。

	HTSurfaceView m_VdoInptPrvwSurfaceViewPt; //存放视频输入预览SurfaceView视图的指针。
	HTSurfaceView m_VdoOtptDspySurfaceViewPt; //存放视频输出显示SurfaceView视图的指针。

	String m_ExternalDirFullAbsPathStrPt; //存放扩展目录完整绝对路径字符串的指针。

	//Activity创建消息。
	@Override protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		Log.i( m_CurClsNameStrPt, "onCreate" );

		//创建布局。
		{
			LayoutInflater p_LyotInflater = LayoutInflater.from( this );

			m_MainLyotViewPt = p_LyotInflater.inflate( R.layout.main_lyot, null );

			m_XfrPrtclStngLyotViewPt = p_LyotInflater.inflate( R.layout.xfr_prtcl_stng_lyot, null );
			m_StngLyotViewPt = p_LyotInflater.inflate( R.layout.stng_lyot, null );

			m_AjbStngLyotViewPt = p_LyotInflater.inflate( R.layout.ajb_stng_lyot, null );
			m_SpeexAecStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_aec_stng_lyot, null );
			m_WebRtcAecmStngLyotViewPt = p_LyotInflater.inflate( R.layout.webrtc_aecm_stng_lyot, null );
			m_WebRtcAecStngLyotViewPt = p_LyotInflater.inflate( R.layout.webrtc_aec_stng_lyot, null );
			m_SpeexWebRtcAecStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_webrtc_aec_stng_lyot, null );
			m_SpeexPrpocsNsStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_prpocs_ns_stng_lyot, null );
			m_WebRtcNsxStngLyotViewPt = p_LyotInflater.inflate( R.layout.webrtc_nsx_stng_lyot, null );
			m_WebRtcNsStngLyotViewPt = p_LyotInflater.inflate( R.layout.webrtc_ns_stng_lyot, null );
			m_SpeexPrpocsOtherStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_prpocs_other_stng_lyot, null );
			m_SpeexCodecStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_codec_stng_lyot, null );
			m_OpenH264CodecStngLyotViewPt = p_LyotInflater.inflate( R.layout.openh264_codec_stng_lyot, null );
			m_SystemH264CodecStngLyotViewPt = p_LyotInflater.inflate( R.layout.systemh264_codec_stng_lyot, null );
		}

		//显示布局。
		setContentView( m_MainLyotViewPt ); //设置主界面的内容为主布局。
		m_CurActivityLyotViewPt = m_MainLyotViewPt; //设置当前界面布局视图。
		( ( Button )findViewById( R.id.PttBtnId ) ).setOnTouchListener( this ); //设置一键即按即通按钮的触摸监听器。

		//请求权限。
		MediaPocsThrd.RqstPrmsn( this, 1, 1, 1, 1, 0, 1, 1, 1, 1 );

		//初始化消息处理。
		m_MainActivityHandlerPt = new MainActivityHandler();
		m_MainActivityHandlerPt.m_MainActivityPt = this;

		//设置AppID文本框。
		( ( TextView ) m_MainLyotViewPt.findViewById( R.id.AppIDTxtId ) ).setText( "AppID：" + getApplicationContext().getPackageName() );

		//设置IP地址编辑框的内容。
		try
		{
			OutSetIPAddrEdit:
			{
				//遍历所有的网络接口设备。
				for( Enumeration<NetworkInterface> clEnumerationNetworkInterface = NetworkInterface.getNetworkInterfaces(); clEnumerationNetworkInterface.hasMoreElements(); )
				{
					NetworkInterface clNetworkInterface = clEnumerationNetworkInterface.nextElement();
					if( clNetworkInterface.getName().compareTo( "usbnet0" ) != 0 ) //如果该网络接口设备不是USB接口对应的网络接口设备。
					{
						//遍历该网络接口设备所有的IP地址。
						for( Enumeration<InetAddress> enumIpAddr = clNetworkInterface.getInetAddresses(); enumIpAddr.hasMoreElements(); )
						{
							InetAddress clInetAddress = enumIpAddr.nextElement();
							if( ( !clInetAddress.isLoopbackAddress() ) && ( clInetAddress.getAddress().length == 4 ) ) //如果该IP地址不是回环地址，且是IPv4的。
							{
								( ( EditText ) m_MainLyotViewPt.findViewById( R.id.IPAddrEdTxtId ) ).setText( clInetAddress.getHostAddress() );
								break OutSetIPAddrEdit;
							}
						}
					}
				}

				( ( EditText ) m_MainLyotViewPt.findViewById( R.id.IPAddrEdTxtId ) ).setText( "0.0.0.0" ); //如果没有获取到IP地址，就设置为本地地址。
			}
		}
		catch( SocketException e )
		{
		}

		//设置端口编辑框的内容。
		( ( EditText ) m_MainLyotViewPt.findViewById( R.id.PortEdTxtId ) ).setText( "12345" );

		//设置系统音频输出音量拖动条。
		{
			SeekBar p_AdoOtptVolumePt = ( SeekBar ) m_MainLyotViewPt.findViewById( R.id.SystemAdoOtptVolmSkBarId ); //获取系统音频输出音量拖动条的指针。
			AudioManager p_AudioManagerPt = ( AudioManager ) getSystemService( Context.AUDIO_SERVICE ); //获取音频服务的指针。

			p_AdoOtptVolumePt.setMax( p_AudioManagerPt.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL ) ); //设置系统音频输出音量拖动条的最大值。
			p_AdoOtptVolumePt.setProgress( p_AudioManagerPt.getStreamVolume( AudioManager.STREAM_VOICE_CALL ) ); //设置系统音频输出音量拖动条的当前值。

			//设置系统音频输出音量拖动条变化消息监听器。
			p_AdoOtptVolumePt.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener()
			{
				@Override public void onProgressChanged( SeekBar seekBar, int progress, boolean fromUser )
				{
					( ( AudioManager ) getSystemService( Context.AUDIO_SERVICE ) ).setStreamVolume( AudioManager.STREAM_VOICE_CALL, progress, AudioManager.FLAG_PLAY_SOUND );
					p_AdoOtptVolumePt.setProgress( p_AudioManagerPt.getStreamVolume( AudioManager.STREAM_VOICE_CALL ) );
				}

				@Override public void onStartTrackingTouch( SeekBar seekBar )
				{

				}

				@Override public void onStopTrackingTouch( SeekBar seekBar )
				{

				}
			} );

			//设置系统音量变化消息监听器。
			IntentFilter p_VolumeChangedActionIntentFilterPt = new IntentFilter();
			p_VolumeChangedActionIntentFilterPt.addAction( "android.media.VOLUME_CHANGED_ACTION" );
			registerReceiver(
					new BroadcastReceiver()
					{
						@Override public void onReceive( Context context, Intent intent )
						{
							p_AdoOtptVolumePt.setProgress( p_AudioManagerPt.getStreamVolume( AudioManager.STREAM_VOICE_CALL ) );
						}
					},
					p_VolumeChangedActionIntentFilterPt );
		}

		//设置默认设置。
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseEffectSuperRdBtnId ) ).performClick(); //默认效果等级：超。
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseBitrateSuperRdBtnId ) ).performClick(); //默认比特率等级：超。

		//设置视频输入预览Surface。
		m_VdoInptPrvwSurfaceViewPt = ( ( HTSurfaceView )findViewById( R.id.VdoInptPrvwSurfaceId ) );
		m_VdoInptPrvwSurfaceViewPt.getHolder().setType( SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS );
		m_VdoInptPrvwSurfaceViewPt.getHolder().addCallback( new SurfaceHolder.Callback() //添加视频输入预览Surface的回调函数。
		{
			@Override public void surfaceCreated( SurfaceHolder holder )
			{
				Log.i( m_CurClsNameStrPt, "VdoInptPrvwSurface Created" );
				if( m_MyMediaPocsThrdPt != null && m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt != 0 && m_MyMediaPocsThrdPt.m_RunFlag == MediaPocsThrd.RUN_FLAG_POCS ) //如果SurfaceView已经重新创建，且媒体处理线程已经启动，且要使用视频输入，并处于初始化完毕正在循环处理帧。
				{
					m_MyMediaPocsThrdPt.RqirExit( 3, 1 ); //请求重启媒体处理线程，来保证正常的视频输入，否则视频输入会中断。
				}
			}

			@Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
			{
				Log.i( m_CurClsNameStrPt, "VdoInptPrvwSurface Changed" );
			}

			@Override public void surfaceDestroyed( SurfaceHolder holder )
			{
				Log.i( m_CurClsNameStrPt, "VdoInptPrvwSurface Destroyed" );
			}
		} );

		//设置视频输出显示Surface。
		m_VdoOtptDspySurfaceViewPt = ( ( HTSurfaceView )findViewById( R.id.VdoOtptDspySurfaceId ) );
		m_VdoOtptDspySurfaceViewPt.getHolder().setType( SurfaceHolder.SURFACE_TYPE_NORMAL );
		m_VdoOtptDspySurfaceViewPt.getHolder().addCallback( new SurfaceHolder.Callback() //添加视频输出显示Surface的回调函数。
		{
			@Override public void surfaceCreated( SurfaceHolder holder )
			{
				Log.i( m_CurClsNameStrPt, "VdoOtptDspySurface Created" );
			}

			@Override public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
			{
				Log.i( m_CurClsNameStrPt, "VdoOtptDspySurface Changed" );
			}

			@Override public void surfaceDestroyed( SurfaceHolder holder )
			{
				Log.i( m_CurClsNameStrPt, "VdoOtptDspySurface Destroyed" );
			}
		} );

		//获取扩展目录完整绝对路径字符串。
		if( getExternalFilesDir( null ) != null )
		{
			m_ExternalDirFullAbsPathStrPt = getExternalFilesDir( null ).getPath();
		}
		else
		{
			m_ExternalDirFullAbsPathStrPt = Environment.getExternalStorageDirectory().getPath() + "/Android/data/" + getApplicationContext().getPackageName();
		}

		String p_InfoStrPt = "扩展目录完整绝对路径：" + m_ExternalDirFullAbsPathStrPt;
		Log.i( m_CurClsNameStrPt, p_InfoStrPt );
		Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.SHOW_LOG;p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
	}

	//主界面的从遮挡恢复消息。
	@Override public void onStart()
	{
		super.onStart();
		Log.i( m_CurClsNameStrPt, "onStart" );
	}

	//主界面从后台恢复消息。
	@Override public void onRestart()
	{
		super.onRestart();
		Log.i( m_CurClsNameStrPt, "onRestart" );
	}

	//主界面恢复运行消息。
	@Override public void onResume()
	{
		super.onResume();
		Log.i( m_CurClsNameStrPt, "onResume" );
	}

	//主界面被遮挡消息。
	@Override public void onPause()
	{
		super.onPause();
		Log.i( m_CurClsNameStrPt, "onPause" );
	}

	//主界面转入后台消息。
	@Override public void onStop()
	{
		super.onStop();
		Log.i( m_CurClsNameStrPt, "onStop" );
	}

	//主界面被销毁消息。
	@Override public void onDestroy()
	{
		super.onDestroy();
		Log.i( m_CurClsNameStrPt, "onDestroy" );
	}

	//主界面返回键消息。
	@Override public void onBackPressed()
	{
		Log.i( m_CurClsNameStrPt, "onBackPressed" );

		if( m_CurActivityLyotViewPt == m_MainLyotViewPt )
		{
			Log.i( m_CurClsNameStrPt, "用户在主界面按下返回键，本软件退出。" );
			if( m_MyMediaPocsThrdPt != null )
			{
				Log.i( m_CurClsNameStrPt, "开始请求并等待媒体处理线程退出。" );
				m_MyMediaPocsThrdPt.RqirExit( 1, 1 );
				Log.i( m_CurClsNameStrPt, "结束请求并等待媒体处理线程退出。" );
			}
			System.exit(0);
		}
		else if( m_CurActivityLyotViewPt == m_XfrPrtclStngLyotViewPt )
		{
			OnClickXfrPrtclStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_StngLyotViewPt )
		{
			OnClickStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SpeexAecStngLyotViewPt )
		{
			OnClickSpeexAecStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_WebRtcAecmStngLyotViewPt )
		{
			OnClickWebRtcAecmStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_WebRtcAecStngLyotViewPt )
		{
			OnClickWebRtcAecStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SpeexWebRtcAecStngLyotViewPt )
		{
			OnClickSpeexWebRtcAecStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SpeexPrpocsNsStngLyotViewPt )
		{
			OnClickSpeexPrpocsNsStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_WebRtcNsxStngLyotViewPt )
		{
			OnClickWebRtcNsxStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_WebRtcNsStngLyotViewPt )
		{
			OnClickWebRtcNsStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SpeexPrpocsOtherStngLyotViewPt )
		{
			OnClickSpeexPrpocsOtherStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SpeexCodecStngLyotViewPt )
		{
			OnClickSpeexCodecStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_OpenH264CodecStngLyotViewPt )
		{
			OnClickOpenH264CodecStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SystemH264CodecStngLyotViewPt )
		{
			OnClickSystemH264CodecStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_AjbStngLyotViewPt )
		{
			this.OnClickAjbStngOkBtn( null );
		}
	}

	//主界面横竖屏切换消息。
	@Override public void onConfigurationChanged( Configuration newConfig )
	{
		super.onConfigurationChanged( newConfig );

		if( m_MyMediaPocsThrdPt != null && m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt != 0 && m_MyMediaPocsThrdPt.m_RunFlag == MediaPocsThrd.RUN_FLAG_POCS ) //如果媒体处理线程已经启动，且要使用视频输入，并处于初始化完毕正在循环处理帧。
		{
			m_MyMediaPocsThrdPt.SetIsUseVdoInpt(
					( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseVdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 :
							( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseAdoVdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 : 0,
					( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate12RdBtnId ) ).isChecked() ) ? 12 :
							( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).isChecked() ) ? 15 :
									( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate24RdBtnId ) ).isChecked() ) ? 24 :
											( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate30RdBtnId ) ).isChecked() ) ? 30 : 0,
					( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize120_160RdBtnId ) ).isChecked() ) ? 120 :
							( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize240_320RdBtnId ) ).isChecked() ) ? 240 :
									( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize480_640RdBtnId ) ).isChecked() ) ? 480 :
											( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize960_1280RdBtnId ) ).isChecked() ) ? 960 : 0,
					( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize120_160RdBtnId ) ).isChecked() ) ? 160 :
							( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize240_320RdBtnId ) ).isChecked() ) ? 320 :
									( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize480_640RdBtnId ) ).isChecked() ) ? 640 :
											( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize960_1280RdBtnId ) ).isChecked() ) ? 1280 : 0,
					getWindowManager().getDefaultDisplay().getRotation() * 90,
					m_VdoInptPrvwSurfaceViewPt
			);

			m_MyMediaPocsThrdPt.RqirExit( 3, 1 ); //请求重启媒体处理线程，来保证正常的视频输入，否则视频输入会中断。
		}
	}

	//传输协议设置按钮。
	public void OnClickXfrPrtclStngBtn( View ViewPt )
	{
		setContentView( m_XfrPrtclStngLyotViewPt );
		m_CurActivityLyotViewPt = m_XfrPrtclStngLyotViewPt;
	}

	//使用音频对讲模式单选按钮。
	public void OnClickUseAdoTkbkModeRdBtn( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			if( m_MyMediaPocsThrdPt.m_XfrMode == 0 ) //如果传输模式为实时半双工。
			{
				m_MyMediaPocsThrdPt.m_AdoInptPt.m_IsUseAdoInpt = 0;
				m_MyMediaPocsThrdPt.m_AdoOtptPt.m_IsUseAdoOtpt = 1;
				m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt = 0;
				m_MyMediaPocsThrdPt.m_VdoOtptPt.m_IsUseVdoOtpt = 0;
			}
			else //如果传输模式为实时全双工。
			{
				m_MyMediaPocsThrdPt.m_AdoInptPt.m_IsUseAdoInpt = 1;
				m_MyMediaPocsThrdPt.m_AdoOtptPt.m_IsUseAdoOtpt = 1;
				m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt = 0;
				m_MyMediaPocsThrdPt.m_VdoOtptPt.m_IsUseVdoOtpt = 0;
			}

			if( m_MyMediaPocsThrdPt.m_RunFlag > MediaPocsThrd.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
			{
				m_MyMediaPocsThrdPt.RqirExit( 3, 1 ); //请求重启并阻塞等待。
			}
		}
	}

	//使用视频对讲模式按钮。
	public void OnClickUseVdoTkbkModeRdBtn( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			if( m_MyMediaPocsThrdPt.m_XfrMode == 0 ) //如果传输模式为实时半双工。
			{
				m_MyMediaPocsThrdPt.m_AdoInptPt.m_IsUseAdoInpt = 0;
				m_MyMediaPocsThrdPt.m_AdoOtptPt.m_IsUseAdoOtpt = 0;
				m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt = 0;
				m_MyMediaPocsThrdPt.m_VdoOtptPt.m_IsUseVdoOtpt = 1;
			}
			else //如果传输模式为实时全双工。
			{
				m_MyMediaPocsThrdPt.m_AdoInptPt.m_IsUseAdoInpt = 0;
				m_MyMediaPocsThrdPt.m_AdoOtptPt.m_IsUseAdoOtpt = 0;
				m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt = 1;
				m_MyMediaPocsThrdPt.m_VdoOtptPt.m_IsUseVdoOtpt = 1;
			}

			if( m_MyMediaPocsThrdPt.m_RunFlag > MediaPocsThrd.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
			{
				m_MyMediaPocsThrdPt.RqirExit( 3, 1 ); //请求重启并阻塞等待。
			}
		}
	}

	//使用音视频对讲模式按钮。
	public void OnClickUseAdoVdoTkbkModeRdBtn( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			if( m_MyMediaPocsThrdPt.m_XfrMode == 0 ) //如果传输模式为实时半双工。
			{
				m_MyMediaPocsThrdPt.m_AdoInptPt.m_IsUseAdoInpt = 0;
				m_MyMediaPocsThrdPt.m_AdoOtptPt.m_IsUseAdoOtpt = 1;
				m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt = 0;
				m_MyMediaPocsThrdPt.m_VdoOtptPt.m_IsUseVdoOtpt = 1;
			}
			else //如果传输模式为实时全双工。
			{
				m_MyMediaPocsThrdPt.m_AdoInptPt.m_IsUseAdoInpt = 1;
				m_MyMediaPocsThrdPt.m_AdoOtptPt.m_IsUseAdoOtpt = 1;
				m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt = 1;
				m_MyMediaPocsThrdPt.m_VdoOtptPt.m_IsUseVdoOtpt = 1;
			}

			if( m_MyMediaPocsThrdPt.m_RunFlag > MediaPocsThrd.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
			{
				m_MyMediaPocsThrdPt.RqirExit( 3, 1 ); //请求重启并阻塞等待。
			}
		}
	}

	//使用扬声器单选按钮。
	public void onClickUseSpeakerRdBtn( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			m_MyMediaPocsThrdPt.SetAdoOtptUseDvc( 0, 0 );

			if( m_MyMediaPocsThrdPt.m_AdoOtptPt.m_IsUseAdoOtpt != 0 && m_MyMediaPocsThrdPt.m_RunFlag > MediaPocsThrd.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
			{
				m_MyMediaPocsThrdPt.RqirExit( 3, 1 ); //请求重启并阻塞等待。
			}
		}
	}

	//使用听筒或耳机单选按钮。
	public void onClickUseHeadsetRdBtn( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			m_MyMediaPocsThrdPt.SetAdoOtptUseDvc( 1, 0 );

			if( m_MyMediaPocsThrdPt.m_AdoOtptPt.m_IsUseAdoOtpt != 0 && m_MyMediaPocsThrdPt.m_RunFlag > MediaPocsThrd.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
			{
				m_MyMediaPocsThrdPt.RqirExit( 3, 1 ); //请求重启并阻塞等待。
			}
		}
	}

	//使用前置摄像头单选按钮。
	public void onClickUseFrontCamereRdBtn( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			m_MyMediaPocsThrdPt.SetVdoInptUseDvc( 0, -1, -1 );

			if( m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt != 0 && m_MyMediaPocsThrdPt.m_RunFlag > MediaPocsThrd.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
			{
				m_MyMediaPocsThrdPt.RqirExit( 3, 1 ); //请求重启并阻塞等待。
			}
		}
	}

	//使用后置摄像头单选按钮。
	public void onClickUseBackCamereRdBtn( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			m_MyMediaPocsThrdPt.SetVdoInptUseDvc( 1, -1, -1 );

			if( m_MyMediaPocsThrdPt.m_VdoInptPt.m_IsUseVdoInpt != 0 && m_MyMediaPocsThrdPt.m_RunFlag > MediaPocsThrd.RUN_FLAG_INIT ) //如果要使用音频输出，且媒体处理线程已经初始化完毕。
			{
				m_MyMediaPocsThrdPt.RqirExit( 3, 1 ); //请求重启并阻塞等待。
			}
		}
	}

	//音频输入是否静音复选框。
	public void onClickAdoInptIsMuteCkBox( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			m_MyMediaPocsThrdPt.SetAdoInptIsMute( ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.AdoInptIsMuteCkBoxId ) ).isChecked() ) ? 1 : 0 );
		}
	}

	//音频输出是否静音复选框。
	public void onClickAdoOtptIsMuteCkBox( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			m_MyMediaPocsThrdPt.SetAdoOtptIsMute( ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.AdoOtptIsMuteCkBoxId ) ).isChecked() ) ? 1 : 0 );
		}
	}

	//视频输入是否黑屏复选框。
	public void onClickVdoInptIsBlackCkBox( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			m_MyMediaPocsThrdPt.SetVdoInptIsBlack( ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.VdoInptIsBlackCkBoxId ) ).isChecked() ) ? 1 : 0 );
		}
	}

	//视频输出是否黑屏复选框。
	public void onClickVdoOtptIsBlackCkBox( View ViewPt )
	{
		if( m_MyMediaPocsThrdPt != null )
		{
			m_MyMediaPocsThrdPt.SetVdoOtptIsBlack( ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.VdoOtptIsBlackCkBoxId ) ).isChecked() ) ? 1 : 0 );
		}
	}

	//创建服务器和连接服务器按钮。
	public void OnClickCreateSrvrAndCnctSrvrBtn( View ViewPt )
	{
		int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_MyMediaPocsThrdPt == null ) //如果媒体处理线程还没有启动。
			{
				Log.i( m_CurClsNameStrPt, "开始启动媒体处理线程。" );

				//创建媒体处理线程。
				m_MyMediaPocsThrdPt = new MyMediaPocsThrd( this, m_MainActivityHandlerPt );

				//设置网络。
				{
					//设置IP地址字符串。
					m_MyMediaPocsThrdPt.m_IPAddrStrPt = ( ( EditText ) m_MainLyotViewPt.findViewById( R.id.IPAddrEdTxtId ) ).getText().toString();

					//设置端口字符串。
					m_MyMediaPocsThrdPt.m_PortStrPt = ( ( EditText ) m_MainLyotViewPt.findViewById( R.id.PortEdTxtId ) ).getText().toString();

					//设置使用什么传输协议。
					m_MyMediaPocsThrdPt.m_UseWhatXfrPrtcl = ( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseTcpPrtclRdBtnId ) ).isChecked() ) ? 0 : 1;

					//设置传输模式。
					if( ( ( RadioButton ) m_XfrPrtclStngLyotViewPt.findViewById( R.id.UsePttRdBtnId ) ).isChecked() )
					{
						m_MyMediaPocsThrdPt.m_XfrMode = 0;
					}
					else
					{
						m_MyMediaPocsThrdPt.m_XfrMode = 1;
					}

					//设置最大连接次数。
					try
					{
						m_MyMediaPocsThrdPt.m_MaxCnctTimes = Integer.parseInt( ( ( TextView ) m_XfrPrtclStngLyotViewPt.findViewById( R.id.MaxCnctTimesEdTxtId ) ).getText().toString() );
					}
					catch( NumberFormatException e )
					{
						Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
						break Out;
					}

					//设置创建服务端或者客户端标记。
					m_MyMediaPocsThrdPt.m_IsCreateSrvrOrClnt = ( ViewPt.getId() == R.id.CreateSrvrBtnId ) ? 1 : 0; //标记创建服务端接受客户端。

					//设置是否自动允许连接。
					m_MyMediaPocsThrdPt.m_IsAutoAllowCnct = ( ( ( CheckBox ) m_XfrPrtclStngLyotViewPt.findViewById( R.id.IsAutoAllowCnctCkBoxId ) ).isChecked() ) ? 1 : 0;
				}

				//设置是否使用链表。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseLnkLstRecvOtptFrmRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.m_UseWhatRecvOtptFrm = 0;
				}

				//设置是否使用自己设计的音频自适应抖动缓冲器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAjbRecvOtptFrmRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.m_UseWhatRecvOtptFrm = 1;

					try
					{
						m_MyMediaPocsThrdPt.m_AAjbMinNeedBufFrmCnt = Integer.parseInt( ( ( TextView ) m_AjbStngLyotViewPt.findViewById( R.id.AAjbMinNeedBufFrmCntEdTxtId ) ).getText().toString() );
						m_MyMediaPocsThrdPt.m_AAjbMaxNeedBufFrmCnt = Integer.parseInt( ( ( TextView ) m_AjbStngLyotViewPt.findViewById( R.id.AAjbMaxNeedBufFrmCntEdTxtId ) ).getText().toString() );
						m_MyMediaPocsThrdPt.m_AAjbMaxCntuLostFrmCnt = Integer.parseInt( ( ( TextView ) m_AjbStngLyotViewPt.findViewById( R.id.AAjbMaxCntuLostFrmCntEdTxtId ) ).getText().toString() );
						m_MyMediaPocsThrdPt.m_AAjbAdaptSensitivity = Float.parseFloat( ( ( TextView ) m_AjbStngLyotViewPt.findViewById( R.id.AAjbAdaptSensitivityEdTxtId ) ).getText().toString() );

						m_MyMediaPocsThrdPt.m_VAjbMinNeedBufFrmCnt = Integer.parseInt( ( ( TextView ) m_AjbStngLyotViewPt.findViewById( R.id.VAjbMinNeedBufFrmCntEdTxtId ) ).getText().toString() );
						m_MyMediaPocsThrdPt.m_VAjbMaxNeedBufFrmCnt = Integer.parseInt( ( ( TextView ) m_AjbStngLyotViewPt.findViewById( R.id.VAjbMaxNeedBufFrmCntEdTxtId ) ).getText().toString() );
						m_MyMediaPocsThrdPt.m_VAjbAdaptSensitivity = Float.parseFloat( ( ( TextView ) m_AjbStngLyotViewPt.findViewById( R.id.VAjbAdaptSensitivityEdTxtId ) ).getText().toString() );
					}
					catch( NumberFormatException e )
					{
						Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
						break Out;
					}
				}

				//设置是否保存设置到文件。
				m_MyMediaPocsThrdPt.SetIsSaveStngToFile(
						( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsSaveStngToFileCkBoxId ) ).isChecked() ) ? 1 : 0,
						m_ExternalDirFullAbsPathStrPt + "/Setting.txt" );

				//设置是否打印Logcat日志、显示Toast。
				m_MyMediaPocsThrdPt.SetIsPrintLogcatShowToast(
						( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsPrintLogcatShowToastCkBoxId ) ).isChecked() ) ? 1 : 0,
						( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsPrintLogcatShowToastCkBoxId ) ).isChecked() ) ? 1 : 0,
						this );

				//设置是否使用唤醒锁。
				m_MyMediaPocsThrdPt.SetIsUseWakeLock( ( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseWakeLockCkBoxId ) ).isChecked() ) ? 1 : 0 );

				//设置是否使用音频输入。
				m_MyMediaPocsThrdPt.SetIsUseAdoInpt(
						( m_MyMediaPocsThrdPt.m_XfrMode == 0 ) ? 0 :
								( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseAdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 :
										( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseAdoVdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 : 0,
						( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate8000RdBtnId ) ).isChecked() ) ? 8000 :
								( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).isChecked() ) ? 16000 :
										( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate32000RdBtnId ) ).isChecked() ) ? 32000 :
												( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate48000RdBtnId ) ).isChecked() ) ? 48000 : 0,
						( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen10msRdBtnId ) ).isChecked() ) ? 10 :
								( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).isChecked() ) ? 20 :
										( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen30msRdBtnId ) ).isChecked() ) ? 30 : 0 );

				//设置音频输入是否使用系统自带的声学回音消除器、噪音抑制器和自动增益控制器。
				m_MyMediaPocsThrdPt.SetAdoInptIsUseSystemAecNsAgc(
						( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).isChecked() ) ? 1 : 0 );

				if( m_MyMediaPocsThrdPt.m_XfrMode == 0 ) //如果传输模式为实时半双工。
				{
					m_MyMediaPocsThrdPt.SetAdoInptUseNoAec();
				}
				else //如果传输模式为实时全双工。
				{
					//设置音频输入是否不使用声学回音消除器。
					if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseNoAecRdBtnId ) ).isChecked() )
					{
						m_MyMediaPocsThrdPt.SetAdoInptUseNoAec();
					}

					//设置音频输入是否使用Speex声学回音消除器。
					if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexAecRdBtnId ) ).isChecked() )
					{
						try
						{
							m_MyMediaPocsThrdPt.SetAdoInptUseSpeexAec(
									Integer.parseInt( ( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenEdTxtId ) ).getText().toString() ),
									( ( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).isChecked() ) ? 1 : 0,
									Float.parseFloat( ( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).getText().toString() ),
									Float.parseFloat( ( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).getText().toString() ),
									Integer.parseInt( ( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).getText().toString() ),
									Integer.parseInt( ( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).getText().toString() ),
									( ( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).isChecked() ) ? 1 : 0,
									m_ExternalDirFullAbsPathStrPt + "/SpeexAecMem"
							);
						}
						catch( NumberFormatException e )
						{
							Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
							break Out;
						}
					}

					//设置音频输入是否使用WebRtc定点版声学回音消除器。
					if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecmRdBtnId ) ).isChecked() )
					{
						try
						{
							m_MyMediaPocsThrdPt.SetAdoInptUseWebRtcAecm(
									( ( ( CheckBox ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).isChecked() ) ? 1 : 0,
									Integer.parseInt( ( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).getText().toString() ),
									Integer.parseInt( ( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).getText().toString() )
							);
						}
						catch( NumberFormatException e )
						{
							Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
							break Out;
						}
					}

					//设置音频输入是否使用WebRtc浮点版声学回音消除器。
					if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecRdBtnId ) ).isChecked() )
					{
						try
						{
							m_MyMediaPocsThrdPt.SetAdoInptUseWebRtcAec(
									Integer.parseInt( ( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).getText().toString() ),
									Integer.parseInt( ( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).getText().toString() ),
									( ( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).isChecked() ) ? 1 : 0,
									( ( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).isChecked() ) ? 1 : 0,
									( ( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).isChecked() ) ? 1 : 0,
									( ( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).isChecked() ) ? 1 : 0,
									( ( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).isChecked() ) ? 1 : 0,
									m_ExternalDirFullAbsPathStrPt + "/WebRtcAecMem"
							);
						}
						catch( NumberFormatException e )
						{
							Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
							break Out;
						}
					}

					//设置音频输入是否使用SpeexWebRtc三重声学回音消除器。
					if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexWebRtcAecRdBtnId ) ).isChecked() )
					{
						try
						{
							m_MyMediaPocsThrdPt.SetAdoInptUseSpeexWebRtcAec(
									( ( RadioButton ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmRdBtnId ) ).isChecked() ? 1 :
											( ( RadioButton ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeWebRtcAecmWebRtcAecRdBtnId ) ).isChecked() ? 2 :
													( ( RadioButton ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRdBtnId ) ).isChecked() ? 3 : 0,
									Integer.parseInt( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdTxtId ) ).getText().toString() ),
									( ( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).isChecked() ) ? 1 : 0,
									Float.parseFloat( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).getText().toString() ),
									Float.parseFloat( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).getText().toString() ),
									Integer.parseInt( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).getText().toString() ),
									Integer.parseInt( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).getText().toString() ),
									( ( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).isChecked() ) ? 1 : 0,
									Integer.parseInt( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).getText().toString() ),
									Integer.parseInt( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).getText().toString() ),
									Integer.parseInt( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).getText().toString() ),
									Integer.parseInt( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).getText().toString() ),
									( ( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).isChecked() ) ? 1 : 0,
									( ( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).isChecked() ) ? 1 : 0,
									( ( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).isChecked() ) ? 1 : 0,
									( ( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).isChecked() ) ? 1 : 0,
									( ( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).isChecked() ) ? 1 : 0,
									Integer.parseInt( ( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).getText().toString() )
							);
						}
						catch( NumberFormatException e )
						{
							Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
							break Out;
						}
					}
				}

				//设置音频输入是否不使用噪音抑制器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseNoNsRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetAdoInptUseNoNs();
				}

				//设置音频输入是否使用Speex预处理器的噪音抑制。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexPrpocsNsRdBtnId ) ).isChecked() )
				{
					try
					{
						m_MyMediaPocsThrdPt.SetAdoInptUseSpeexPrpocsNs(
								( ( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).isChecked() ) ? 1 : 0,
								Integer.parseInt( ( ( TextView ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).getText().toString() ),
								( ( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).isChecked() ) ? 1 : 0
						);
					}
					catch( NumberFormatException e )
					{
						Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
						break Out;
					}
				}

				//设置音频输入是否使用WebRtc定点版噪音抑制器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsxRdBtnId ) ).isChecked() )
				{
					try
					{
						m_MyMediaPocsThrdPt.SetAdoInptUseWebRtcNsx(
								Integer.parseInt( ( ( TextView ) m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).getText().toString() )
						);
					}
					catch( NumberFormatException e )
					{
						Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
						break Out;
					}
				}

				//设置音频输入是否使用WebRtc浮点版噪音抑制器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsRdBtnId ) ).isChecked() )
				{
					try
					{
						m_MyMediaPocsThrdPt.SetAdoInptUseWebRtcNs(
								Integer.parseInt( ( ( TextView ) m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).getText().toString() )
						);
					}
					catch( NumberFormatException e )
					{
						Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
						break Out;
					}
				}

				//设置音频输入是否使用RNNoise噪音抑制器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseRNNoiseRdBtnId ) ).isChecked() )
				{
					try
					{
						m_MyMediaPocsThrdPt.SetAdoInptUseRNNoise();
					}
					catch( NumberFormatException e )
					{
						Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
						break Out;
					}
				}

				//设置音频输入是否使用Speex预处理器的其他功能。
				try
				{
					m_MyMediaPocsThrdPt.SetAdoInptIsUseSpeexPrpocsOther(
							( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsOtherCkBoxId ) ).isChecked() ) ? 1 : 0,
							( ( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).isChecked() ) ? 1 : 0,
							Integer.parseInt( ( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).getText().toString() ),
							Integer.parseInt( ( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).getText().toString() ),
							( ( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).isChecked() ) ? 1 : 0,
							Integer.parseInt( ( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).getText().toString() ),
							Integer.parseInt( ( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).getText().toString() ),
							Integer.parseInt( ( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).getText().toString() ),
							Integer.parseInt( ( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).getText().toString() )
					);
				}
				catch( NumberFormatException e )
				{
					Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
					break Out;
				}

				//设置音频输入是否使用PCM原始数据。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UsePcmRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetAdoInptUsePcm();
				}

				//设置音频输入是否使用Speex编码器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).isChecked() )
				{
					try
					{
						m_MyMediaPocsThrdPt.SetAdoInptUseSpeexEncd(
								( ( ( RadioButton ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseCbrRdBtnId ) ).isChecked() ) ? 0 : 1,
								Integer.parseInt( ( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).getText().toString() ),
								Integer.parseInt( ( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).getText().toString() ),
								Integer.parseInt( ( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).getText().toString() )
						);
					}
					catch( NumberFormatException e )
					{
						Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
						break Out;
					}
				}

				//设置音频输入是否使用Opus编码器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseOpusCodecRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetAdoInptUseOpusEncd();
				}

				//设置音频输入是否保存音频到文件。
				m_MyMediaPocsThrdPt.SetAdoInptIsSaveAdoToFile(
						( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsSaveAdoToFileCkBoxId ) ).isChecked() ) ? 1 : 0,
						m_ExternalDirFullAbsPathStrPt + "/AdoInpt.wav",
						m_ExternalDirFullAbsPathStrPt + "/AdoRslt.wav"
				);

				//设置音频输入是否绘制音频波形到Surface。
				m_MyMediaPocsThrdPt.SetAdoInptIsDrawAdoWavfmToSurface(
						( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).isChecked() ) ? 1 : 0,
						( ( SurfaceView )findViewById( R.id.AdoInptOscilloSurfaceId ) ),
						( ( SurfaceView )findViewById( R.id.AdoRsltOscilloSurfaceId ) )
				);

				//设置音频输入是否静音。
				m_MyMediaPocsThrdPt.SetAdoInptIsMute(
						( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.AdoInptIsMuteCkBoxId ) ).isChecked() ) ? 1 : 0 );

				//设置是否使用音频输出。
				m_MyMediaPocsThrdPt.SetIsUseAdoOtpt(
						( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseAdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 :
								( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseAdoVdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 : 0,
						( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate8000RdBtnId ) ).isChecked() ) ? 8000 :
								( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).isChecked() ) ? 16000 :
										( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate32000RdBtnId ) ).isChecked() ) ? 32000 :
												( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate48000RdBtnId ) ).isChecked() ) ? 48000 : 0,
						( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen10msRdBtnId ) ).isChecked() ) ? 10 :
								( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).isChecked() ) ? 20 :
										( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen30msRdBtnId ) ).isChecked() ) ? 30 : 0 );

				//设置音频输出是否使用PCM原始数据。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UsePcmRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetAdoOtptUsePcm();
				}

				//设置音频输出是否使用Speex解码器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).isChecked() )
				{
					try
					{
						m_MyMediaPocsThrdPt.SetAdoOtptUseSpeexDecd(
								( ( ( CheckBox ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).isChecked() ) ? 1 : 0
						);
					}
					catch( NumberFormatException e )
					{
						Toast.makeText( this, "请输入数字", Toast.LENGTH_LONG ).show();
						break Out;
					}
				}

				//设置音频输出是否使用Opus解码器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseOpusCodecRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetAdoOtptUseOpusDecd();
				}

				//设置音频输出是否保存音频到文件。
				m_MyMediaPocsThrdPt.SetAdoOtptIsSaveAdoToFile(
						( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsSaveAdoToFileCkBoxId ) ).isChecked() ) ? 1 : 0,
						m_ExternalDirFullAbsPathStrPt + "/AdoOtpt.wav" );

				//设置音频输出是否绘制音频波形到Surface。
				m_MyMediaPocsThrdPt.SetAdoOtptIsDrawAdoWavfmToSurface(
						( ( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).isChecked() ) ? 1 : 0,
						( ( SurfaceView )findViewById( R.id.AdoOtptOscilloSurfaceId ) ) );

				//设置音频输出使用的设备。
				m_MyMediaPocsThrdPt.SetAdoOtptUseDvc(
						( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseSpeakerRdBtnId ) ).isChecked() ) ? 0 : 1,
						0 );

				//设置音频输出是否静音。
				m_MyMediaPocsThrdPt.SetAdoOtptIsMute(
						( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.AdoOtptIsMuteCkBoxId ) ).isChecked() ) ? 1 : 0 );

				//设置是否使用视频输入。
				m_MyMediaPocsThrdPt.SetIsUseVdoInpt(
						( m_MyMediaPocsThrdPt.m_XfrMode == 0 ) ? 0 :
								( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseVdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 :
										( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseAdoVdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 : 0,
						( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate12RdBtnId ) ).isChecked() ) ? 12 :
								( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).isChecked() ) ? 15 :
										( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate24RdBtnId ) ).isChecked() ) ? 24 :
												( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate30RdBtnId ) ).isChecked() ) ? 30 : 0,
						( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize120_160RdBtnId ) ).isChecked() ) ? 120 :
								( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize240_320RdBtnId ) ).isChecked() ) ? 240 :
										( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize480_640RdBtnId ) ).isChecked() ) ? 480 :
												( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize960_1280RdBtnId ) ).isChecked() ) ? 960 : 0,
						( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize120_160RdBtnId ) ).isChecked() ) ? 160 :
								( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize240_320RdBtnId ) ).isChecked() ) ? 320 :
										( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize480_640RdBtnId ) ).isChecked() ) ? 640 :
												( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize960_1280RdBtnId ) ).isChecked() ) ? 1280 : 0,
						getWindowManager().getDefaultDisplay().getRotation() * 90,
						m_VdoInptPrvwSurfaceViewPt
				);

				//设置视频输入是否使用YU12原始数据。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseYU12RdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetVdoInptUseYU12();
				}

				//设置视频输入是否使用OpenH264编码器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetVdoInptUseOpenH264Encd(
							Integer.parseInt( ( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).getText().toString() ),
							Integer.parseInt( ( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).getText().toString() ) * 1024 * 8,
							Integer.parseInt( ( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).getText().toString() ),
							Integer.parseInt( ( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).getText().toString() ),
							Integer.parseInt( ( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).getText().toString() )
					);
				}

				//设置视频输入是否使用系统自带H264编码器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSystemH264CodecRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetVdoInptUseSystemH264Encd(
							Integer.parseInt( ( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).getText().toString() ) * 1024 * 8,
							Integer.parseInt( ( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).getText().toString() ),
							Integer.parseInt( ( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).getText().toString() ),
							Integer.parseInt( ( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).getText().toString() )
					);
				}

				//设置视频输入使用的设备。
				m_MyMediaPocsThrdPt.SetVdoInptUseDvc(
						( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseFrontCamereRdBtnId ) ).isChecked() ) ? 0 : 1,
						-1,
						-1 );

				//设置视频输入是否黑屏。
				m_MyMediaPocsThrdPt.SetVdoInptIsBlack(
						( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.VdoInptIsBlackCkBoxId ) ).isChecked() ) ? 1 : 0 );

				//设置是否使用视频输出。
				m_MyMediaPocsThrdPt.SetIsUseVdoOtpt(
						( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseVdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 :
								( ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseAdoVdoTkbkModeRdBtnId ) ).isChecked() ) ? 1 : 0,
						m_VdoOtptDspySurfaceViewPt,
						( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoDspyScale1_0RdBtnId ) ).isChecked() ) ? 1.0f :
								( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoDspyScale1_5RdBtnId ) ).isChecked() ) ? 1.5f :
										( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoDspyScale2_0RdBtnId ) ).isChecked() ) ? 2.0f :
												( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoDspyScale3_0RdBtnId ) ).isChecked() ) ? 3.0f : 1.0f
				);

				//设置视频输出是否使用YU12原始数据。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseYU12RdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetVdoOtptUseYU12();
				}

				//设置视频输出是否使用OpenH264解码器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetVdoOtptUseOpenH264Decd( 0 );
				}

				//设置视频输出是否使用系统自带H264解码器。
				if( ( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSystemH264CodecRdBtnId ) ).isChecked() )
				{
					m_MyMediaPocsThrdPt.SetVdoOtptUseSystemH264Decd();
				}

				//设置视频输出是否黑屏。
				m_MyMediaPocsThrdPt.SetVdoOtptIsBlack(
						( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.VdoOtptIsBlackCkBoxId ) ).isChecked() ) ? 1 : 0 );

				//启动媒体处理线程。
				m_MyMediaPocsThrdPt.start();

				Log.i( m_CurClsNameStrPt, "启动媒体处理线程完毕。" );
			}
			else
			{
				Log.i( m_CurClsNameStrPt, "开始请求并等待媒体处理线程退出。" );
				m_MyMediaPocsThrdPt.RqirExit( 1, 1 );
				Log.i( m_CurClsNameStrPt, "结束请求并等待媒体处理线程退出。" );
			}

			p_Rslt = 0;
		}

		if( p_Rslt != 0 ) //如果启动媒体处理线程失败。
		{
			m_MyMediaPocsThrdPt = null;
		}
	}

	//设置按钮。
	public void OnClickStngBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//必读说明按钮。
	public void OnClickReadMeBtn( View ViewPt )
	{
		startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "https://github.com/cyz7758520/Android_audio_talkback_demo_program" ) ) );
	}

	//视频输入预览或视频输出显示Surface。
	public void onClickVdoInptPrvwAndVdoOtptDspySurface( View ViewPt )
	{
		if( ( ( LinearLayout )ViewPt.getParent().getParent() ).getOrientation() == LinearLayout.HORIZONTAL )
		{
			( ( LinearLayout )ViewPt.getParent().getParent() ).setOrientation( LinearLayout.VERTICAL );
		}
		else
		{
			( ( LinearLayout )ViewPt.getParent().getParent() ).setOrientation( LinearLayout.HORIZONTAL );
		}
	}

	//清空日志按钮。
	public void OnClickClearLogBtn( View ViewPt )
	{
		( ( LinearLayout ) m_MainLyotViewPt.findViewById( R.id.LogLinearLyotId ) ).removeAllViews();
	}

	//一键即按即通按钮。
	@Override public boolean onTouch( View ViewPt, MotionEvent EventPt )
	{
		if( ViewPt.getId() == R.id.PttBtnId ) //如果是一键即按即通按钮。
		{
			switch( EventPt.getAction() )
			{
				case MotionEvent.ACTION_DOWN: //如果是按下消息。
				{
					if( m_MyMediaPocsThrdPt != null )
					{
						m_MyMediaPocsThrdPt.m_PttBtnIsDown = 1;
					}
					break;
				}
				case MotionEvent.ACTION_UP: //如果是弹起消息。
				{
					if( m_MyMediaPocsThrdPt != null )
					{
						m_MyMediaPocsThrdPt.m_PttBtnIsDown = 0;
					}
					break;
				}
			}
		}
		return false;
	}

	//音频自适应抖动缓冲器设置按钮。
	public void OnClickAjbStngBtn( View ViewPt )
	{
		setContentView( m_AjbStngLyotViewPt );
		m_CurActivityLyotViewPt = m_AjbStngLyotViewPt;
	}

	//效果等级：低。
	public void OnClickUseEffectLowRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseEffectLowRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate8000RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecmRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexPrpocsNsRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsOtherCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsSaveAdoToFileCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).setChecked( false );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate12RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize120_160RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoDspyScale1_0RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( CheckBox ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( RadioButton ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( false );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

		( ( TextView ) m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

		( ( TextView ) m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( false );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "25" );

		( ( RadioButton ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseCbrRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "1" );
		( ( CheckBox ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "12" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "0" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "0" );
	}

	//效果等级：中。
	public void OnClickUseEffectMidRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseEffectMidRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseWebRtcAecRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsxRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsOtherCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsSaveAdoToFileCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).setChecked( false );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize240_320RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoDspyScale1_0RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( CheckBox ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( RadioButton ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( false );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

		( ( TextView ) m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

		( ( TextView ) m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "25" );

		( ( RadioButton ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseCbrRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "4" );
		( ( CheckBox ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "15" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "0" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "1" );
	}

	//效果等级：高。
	public void OnClickUseEffectHighRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseEffectHighRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexWebRtcAecRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseWebRtcNsRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsOtherCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsSaveAdoToFileCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).setChecked( false );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate15RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize480_640RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoDspyScale1_0RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( CheckBox ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( RadioButton ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( false );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

		( ( TextView ) m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

		( ( TextView ) m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "25" );

		( ( RadioButton ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseVbrRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "8" );
		( ( CheckBox ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "15" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "0" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "2" );
	}

	//效果等级：超。
	public void OnClickUseEffectSuperRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseEffectSuperRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate16000RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexWebRtcAecRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseRNNoiseRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsOtherCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsSaveAdoToFileCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).setChecked( false );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate24RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize480_640RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoDspyScale1_0RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( CheckBox ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( RadioButton ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

		( ( TextView ) m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

		( ( TextView ) m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "25" );

		( ( RadioButton ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseVbrRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "10" );
		( ( CheckBox ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "24" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "1" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "2" );
	}

	//效果等级：特。
	public void OnClickUseEffectPremiumRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseEffectPremiumRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoSmplRate32000RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseAdoFrmLen20msRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSystemAecNsAgcCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexWebRtcAecRdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseRNNoiseRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsUseSpeexPrpocsOtherCkBoxId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseSpeexCodecRdBtnId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsSaveAdoToFileCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_StngLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).setChecked( false );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoSmplRate30RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoFrmSize960_1280RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseVdoDspyScale1_0RdBtnId ) ).setChecked( true );
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseOpenH264CodecRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoMutpEdTxtId ) ).setText( "3.0" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoCntuEdTxtId ) ).setText( "0.65" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexAecStngLyotViewPt.findViewById( R.id.SpeexAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( CheckBox ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_WebRtcAecmStngLyotViewPt.findViewById( R.id.WebRtcAecmDelayEdTxtId ) ).setText( "0" );

		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_WebRtcAecStngLyotViewPt.findViewById( R.id.WebRtcAecIsSaveMemFileCkBoxId ) ).setChecked( false );

		( ( RadioButton ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWorkModeSpeexAecWebRtcAecmWebRtcAecRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecFilterLenEdTxtId ) ).setText( "500" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecIsUseRecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoMutpEdTxtId ) ).setText( "1.0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoCntuEdTxtId ) ).setText( "0.6" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesEdTxtId ) ).setText( "-32768" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSpeexAecEchoSupesActEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmIsUseCNGModeCkBoxId ) ).setChecked( false );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmEchoModeEdTxtId ) ).setText( "4" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecmDelayEdTxtId ) ).setText( "0" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecEchoModeEdTxtId ) ).setText( "2" );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecDelayEdTxtId ) ).setText( "0" );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseDelayAgstcModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseExtdFilterModeCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseRefinedFilterAdaptAecModeCkBoxId ) ).setChecked( false );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecWebRtcAecIsUseAdaptAdjDelayCkBoxId ) ).setChecked( true );
		( ( CheckBox ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecIsUseSameRoomAecCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexWebRtcAecStngLyotViewPt.findViewById( R.id.SpeexWebRtcAecSameRoomEchoMinDelayEdTxtId ) ).setText( "380" );

		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseNsCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsNoiseSupesEdTxtId ) ).setText( "-32768" );
		( ( CheckBox ) m_SpeexPrpocsNsStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseDereverbCkBoxId ) ).setChecked( true );

		( ( TextView ) m_WebRtcNsxStngLyotViewPt.findViewById( R.id.WebRtcNsxPolicyModeEdTxtId ) ).setText( "3" );

		( ( TextView ) m_WebRtcNsStngLyotViewPt.findViewById( R.id.WebRtcNsPolicyModeEdTxtId ) ).setText( "3" );

		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseVadCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbStartEdTxtId ) ).setText( "95" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsVadProbCntuEdTxtId ) ).setText( "95" );
		( ( CheckBox ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsIsUseAgcCkBoxId ) ).setChecked( true );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcLevelEdTxtId ) ).setText( "30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcIncrementEdTxtId ) ).setText( "10" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcDecrementEdTxtId ) ).setText( "-30000" );
		( ( TextView ) m_SpeexPrpocsOtherStngLyotViewPt.findViewById( R.id.SpeexPrpocsAgcMaxGainEdTxtId ) ).setText( "25" );

		( ( RadioButton ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdUseVbrRdBtnId ) ).setChecked( true );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdCmplxtEdTxtId ) ).setText( "10" );
		( ( CheckBox ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexDecdIsUsePrcplEnhsmtCkBoxId ) ).setChecked( true );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdVdoTypeEdTxtId ) ).setText( "0" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdBitrateCtrlModeEdTxtId ) ).setText( "3" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdIDRFrmIntvlEdTxtId ) ).setText( "30" );
		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdCmplxtEdTxtId ) ).setText( "2" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdBitrateCtrlModeEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdIDRFrmIntvlEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdCmplxtEdTxtId ) ).setText( "2" );
	}

	//比特率等级：低。
	public void OnClickUseBitrateLowRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseBitrateLowRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "1" );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "1" );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "10" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "10" );
	}

	//比特率等级：中。
	public void OnClickUseBitrateMidRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseBitrateMidRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "4" );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "40" );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "20" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "20" );
	}

	//比特率等级：高。
	public void OnClickUseBitrateHighRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseBitrateHighRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "8" );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "80" );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "40" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "40" );
	}

	//比特率等级：超。
	public void OnClickUseBitrateSuperRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseBitrateSuperRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "10" );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "100" );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "60" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "60" );
	}

	//比特率等级：特。
	public void OnClickUseBitratePremiumRdBtn( View ViewPt )
	{
		( ( RadioButton ) m_StngLyotViewPt.findViewById( R.id.UseBitratePremiumRdBtnId ) ).setChecked( true );

		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdQualtEdTxtId ) ).setText( "10" );
		( ( TextView ) m_SpeexCodecStngLyotViewPt.findViewById( R.id.SpeexEncdPlcExptLossRateEdTxtId ) ).setText( "100" );

		( ( TextView ) m_OpenH264CodecStngLyotViewPt.findViewById( R.id.OpenH264EncdEncdBitrateEdTxtId ) ).setText( "80" );

		( ( TextView ) m_SystemH264CodecStngLyotViewPt.findViewById( R.id.SystemH264EncdEncdBitrateEdTxtId ) ).setText( "80" );
	}

	//Speex声学回音消除器设置按钮。
	public void OnClickSpeexAecStngBtn( View ViewPt )
	{
		setContentView( m_SpeexAecStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SpeexAecStngLyotViewPt;
	}

	//WebRtc定点版声学回音消除器设置按钮。
	public void OnClickWebRtcAecmStngBtn( View ViewPt )
	{
		setContentView( m_WebRtcAecmStngLyotViewPt );
		m_CurActivityLyotViewPt = m_WebRtcAecmStngLyotViewPt;
	}

	//WebRtc浮点版声学回音消除器设置按钮。
	public void OnClickWebRtcAecStngBtn( View ViewPt )
	{
		setContentView( m_WebRtcAecStngLyotViewPt );
		m_CurActivityLyotViewPt = m_WebRtcAecStngLyotViewPt;
	}

	//SpeexWebRtc三重声学回音消除器设置按钮。
	public void OnClickSpeexWebRtcAecStngBtn( View ViewPt )
	{
		setContentView( m_SpeexWebRtcAecStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SpeexWebRtcAecStngLyotViewPt;
	}

	//Speex预处理器的噪音抑制设置按钮。
	public void OnClickSpeexPrpocsNsStngBtn( View ViewPt )
	{
		setContentView( m_SpeexPrpocsNsStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SpeexPrpocsNsStngLyotViewPt;
	}

	//WebRtc定点版噪音抑制器设置按钮。
	public void OnClickWebRtcNsxStngBtn( View ViewPt )
	{
		setContentView( m_WebRtcNsxStngLyotViewPt );
		m_CurActivityLyotViewPt = m_WebRtcNsxStngLyotViewPt;
	}

	//WebRtc浮点版噪音抑制器设置按钮。
	public void OnClickWebRtcNsStngBtn( View ViewPt )
	{
		setContentView( m_WebRtcNsStngLyotViewPt );
		m_CurActivityLyotViewPt = m_WebRtcNsStngLyotViewPt;
	}

	//Speex预处理器的其他功能设置按钮。
	public void OnClickSpeexPrpocsOtherStngBtn( View ViewPt )
	{
		setContentView( m_SpeexPrpocsOtherStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SpeexPrpocsOtherStngLyotViewPt;
	}

	//Speex编解码器设置按钮。
	public void OnClickSpeexCodecStngBtn( View ViewPt )
	{
		setContentView( m_SpeexCodecStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SpeexCodecStngLyotViewPt;
	}

	//Opus编解码器设置按钮。
	public void OnClickOpusCodecStngBtn( View ViewPt )
	{

	}

	//OpenH264编解码器设置按钮。
	public void OnClickOpenH264CodecStngBtn( View ViewPt )
	{
		setContentView( m_OpenH264CodecStngLyotViewPt );
		m_CurActivityLyotViewPt = m_OpenH264CodecStngLyotViewPt;
	}

	//系统自带H264编解码器设置按钮。
	public void OnClickSystemH264CodecStngBtn( View ViewPt )
	{
		setContentView( m_SystemH264CodecStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SystemH264CodecStngLyotViewPt;
	}

	//传输协议设置确定按钮。
	public void OnClickXfrPrtclStngOkBtn( View ViewPt )
	{
		setContentView( m_MainLyotViewPt );
		m_CurActivityLyotViewPt = m_MainLyotViewPt;
	}

	//设置布局的确定按钮。
	public void OnClickStngOkBtn( View ViewPt )
	{
		setContentView( m_MainLyotViewPt );
		m_CurActivityLyotViewPt = m_MainLyotViewPt;
	}

	//音频自适应抖动缓冲器设置布局的确定按钮。
	public void OnClickAjbStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Speex声学回音消除器设置布局的删除内存块文件按钮。
	public void OnClickSpeexAecDelMemFileBtn( View ViewPt )
	{
		String p_pclSpeexAecMemoryFullPath = m_ExternalDirFullAbsPathStrPt + "/SpeexAecMem";
		File file = new File( p_pclSpeexAecMemoryFullPath );
		if( file.exists() )
		{
			if( file.delete() )
			{
				Toast.makeText( this, "删除Speex声学回音消除器的内存块文件 " + p_pclSpeexAecMemoryFullPath + " 成功。", Toast.LENGTH_LONG ).show();
			}
			else
			{
				Toast.makeText( this, "删除Speex声学回音消除器的内存块文件 " + p_pclSpeexAecMemoryFullPath + " 失败。", Toast.LENGTH_LONG ).show();
			}
		}
		else
		{
			Toast.makeText( this, "Speex声学回音消除器的内存块文件 " + p_pclSpeexAecMemoryFullPath + " 不存在。", Toast.LENGTH_LONG ).show();
		}
	}

	//Speex声学回音消除器设置布局的确定按钮。
	public void OnClickSpeexAecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//WebRtc定点版声学回音消除器设置布局的确定按钮。
	public void OnClickWebRtcAecmStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//WebRtc浮点版声学回音消除器设置布局的删除内存块文件按钮。
	public void OnClickWebRtcAecDelMemFileBtn( View ViewPt )
	{
		String p_pclWebRtcAecMemoryFullPath = m_ExternalDirFullAbsPathStrPt + "/WebRtcAecMem";
		File file = new File( p_pclWebRtcAecMemoryFullPath );
		if( file.exists() )
		{
			if( file.delete() )
			{
				Toast.makeText( this, "删除WebRtc浮点版声学回音消除器的内存块文件 " + p_pclWebRtcAecMemoryFullPath + " 成功。", Toast.LENGTH_LONG ).show();
			}
			else
			{
				Toast.makeText( this, "删除WebRtc浮点版声学回音消除器的内存块文件 " + p_pclWebRtcAecMemoryFullPath + " 失败。", Toast.LENGTH_LONG ).show();
			}
		}
		else
		{
			Toast.makeText( this, "WebRtc浮点版声学回音消除器的内存块文件 " + p_pclWebRtcAecMemoryFullPath + " 不存在。", Toast.LENGTH_LONG ).show();
		}
	}

	//WebRtc浮点版声学回音消除器设置布局的确定按钮。
	public void OnClickWebRtcAecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//SpeexWebRtc三重声学回音消除器设置布局的确定按钮。
	public void OnClickSpeexWebRtcAecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Speex预处理器的噪音抑制设置布局的确定按钮。
	public void OnClickSpeexPrpocsNsStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//WebRtc定点版噪音抑制器设置布局的确定按钮。
	public void OnClickWebRtcNsxStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//WebRtc浮点版噪音抑制器设置布局的确定按钮。
	public void OnClickWebRtcNsStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Speex预处理器的其他功能设置布局的确定按钮。
	public void OnClickSpeexPrpocsOtherStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Speex编解码器设置布局的确定按钮。
	public void OnClickSpeexCodecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Opus编解码器设置布局的确定按钮。
	public void OnOpusCodecSettingOkClick( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//OpenH264编解码器设置布局的确定按钮。
	public void OnClickOpenH264CodecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//系统自带H264编解码器设置布局的确定按钮。
	public void OnClickSystemH264CodecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}
}