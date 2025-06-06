package com.example.andrd_ado_vdo_tkbk_demo;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import HeavenTao.Ado.*;
import HeavenTao.Vdo.*;
import HeavenTao.Sokt.*;
import HeavenTao.Data.*;
import HeavenTao.Media.*;

//主界面。
public class MainAct extends AppCompatActivity implements View.OnTouchListener
{
	String m_CurClsNameStrPt = this.getClass().getSimpleName(); //存放当前类名称字符串。

	Vstr m_ErrInfoVstrPt; //存放错误信息动态字符串的指针。
	View m_MainLyotViewPt; //存放主布局视图的指针。
	Drawable m_PttBtnBackgroundPt; //存放一键即按即通按钮背景的指针。
	Drawable m_PtbBtnBackgroundPt; //存放一键即按即广播按钮背景的指针。
	View m_SrvrStngLyotViewPt; //存放服务端设置布局视图的指针。
	ListView m_CnctLstViewPt; //存放连接列表视图的指针。
	ArrayList< Map< String, String > > m_CnctLstItemArrayLstPt; //存放连接列表项目数组列表的指针。
	View m_ClntStngLyotViewPt; //存放客户端设置布局视图的指针。
	ListView m_ClntLstViewPt; //存放客户端列表视图的指针。
	ArrayList< Map< String, String > > m_ClntLstItemArrayLstPt; //存放客户端列表项目数组列表的指针。
	Spinner m_AdoInptOtptDvcSpinnerPt; //存放音频输入输出设备下拉框的指针。
	ArrayList< MediaPocsThrd.AdoInptOtptDvcInfo > m_AdoInptOtptDvcInfoLstPt; //存放音频输入输出设备信息列表的指针。
	ArrayList< String > m_AdoInptOtptDvcSpinnerItemArrayLst; //存放音频输入输出设备下拉框项目数组列表的指针。
	MediaPocsThrd.AdoInptOtptDvcInfo m_AdoInptOtptUseDvcInfoPt; //存放音频输入输出使用的设备信息的指针。
	View m_StngLyotViewPt; //存放设置布局视图的指针。
	View m_AjbStngLyotViewPt; //存放音频自适应抖动缓冲器设置布局视图的指针。
	View m_SaveStsToTxtFileStngLyotViewPt; //存放保存状态到Txt文件设置布局视图的指针。
	View m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt; //存放保存音视频输入输出到Avi文件设置布局视图的指针。
	View m_SpeexAecStngLyotViewPt; //存放Speex声学回音消除器设置布局视图的指针。
	View m_WebRtcAecmStngLyotViewPt; //存放WebRtc定点版声学回音消除器设置布局视图的指针。
	View m_WebRtcAecStngLyotViewPt; //存放WebRtc浮点版声学回音消除器设置布局视图的指针。
	View m_WebRtcAec3StngLyotViewPt; //存放WebRtc第三版声学回音消除器设置布局视图的指针。
	View m_SpeexWebRtcAecStngLyotViewPt; //存放SpeexWebRtc三重声学回音消除器设置布局视图的指针。
	View m_SpeexPrpocsNsStngLyotViewPt; //存放Speex预处理器的噪音抑制设置布局视图的指针。
	View m_WebRtcNsxStngLyotViewPt; //存放WebRtc定点版噪音抑制器设置布局视图的指针。
	View m_WebRtcNsStngLyotViewPt; //存放WebRtc浮点版噪音抑制器设置布局视图的指针。
	View m_SpeexPrpocsStngLyotViewPt; //存放Speex预处理器设置布局视图的指针。
	View m_SpeexCodecStngLyotViewPt; //存放Speex编解码器设置布局视图的指针。
	View m_SaveAdoInptOtptToWaveFileStngLyotViewPt; //存放保存音频输入输出到Wave文件设置布局视图的指针。
	View m_OpenH264CodecStngLyotViewPt; //存放OpenH264编解码器设置布局视图的指针。
	View m_SystemH264CodecStngLyotViewPt; //存放系统自带H264编解码器设置布局视图的指针。

	View m_CurActivityLyotViewPt; //存放当前界面布局视图的指针。
	MySrvrThrd m_MySrvrThrdPt; //存放我的服务端线程的指针。
	MyClntMediaPocsThrd m_MyClntMediaPocsThrdPt; //存放我的客户端媒体处理线程的指针。
	MainAct m_MainActPt; //存放主界面的指针。

	String m_ExternalDirFullAbsPathStrPt; //存放扩展目录完整绝对路径字符串的指针。

	ServiceConnection m_FrgndSrvcCnctPt; //存放前台服务连接器的指针。

	class MainActMsgTyp //主界面消息类型。
	{
		public static final int ShowLog                        = 0; //显示日志。
		public static final int ShowToast                      = 1; //显示Toast。
		public static final int Vibrate                        = 2; //振动。

		public static final int MySrvrThrdInit                 = 3; //我的服务端线程初始化。
		public static final int MySrvrThrdDstoy                = 4; //我的服务端线程销毁。
		public static final int SrvrInit                       = 5; //服务端初始化。
		public static final int SrvrDstoy                      = 6; //服务端销毁。

		public static final int MyClntMediaPocsThrdInit        = 7; //我的客户端媒体处理线程初始化。
		public static final int MyClntMediaPocsThrdDstoy       = 8; //我的客户端媒体处理线程销毁。
		public static final int TkbkClntCnctInit               = 9; //对讲客户端连接初始化。
		public static final int TkbkClntCnctDstoy              = 10; //对讲客户端连接销毁。

		public static final int CnctLstAddItem                 = 11; //连接列表添加项目。
		public static final int CnctLstDelItem                 = 12; //连接列表删除项目。
		public static final int CnctLstModifyItem              = 13; //连接列表修改项目。

		public static final int ClntLstAddItem                 = 14; //客户端列表添加项目。
		public static final int ClntLstDelItem                 = 15; //客户端列表删除项目。
		public static final int ClntLstModifyItem              = 16; //客户端列表修改项目。

		public static final int GetAdoInptOtptDvcInfo          = 17; //获取音频输入输出设备信息。

		public static final int VdoInptOtptViewInit            = 18; //视频输入输出视图初始化。
		public static final int VdoInptOtptViewDstoy           = 19; //视频输入输出视图销毁。
		public static final int VdoInptOtptViewSetTitle        = 20; //视频输入输出视图设置标题。

		public static final int AdoInptOtptDvcChg              = 21; //音频输入输出设备改变。
		public static final int VdoInptDvcChg                  = 22; //视频输入设备改变。
	}

	//主界面消息处理。
	class MainActHandler extends Handler
	{
		public void handleMessage( Message MessagePt )
		{
			switch( MessagePt.what )
			{
				case MainActMsgTyp.ShowLog:
				{
					TextView p_LogTextView = new TextView( m_MainActPt );
					p_LogTextView.setText( ( new SimpleDateFormat( "HH:mm:ss SSS" ) ).format( new Date() ) + "：" + ( ( Object[] ) MessagePt.obj )[ 0 ] );
					( ( LinearLayout ) m_MainActPt.m_MainLyotViewPt.findViewById( R.id.LogLinearLyotId ) ).addView( p_LogTextView );
					break;
				}
				case MainActMsgTyp.ShowToast:
				{
					Toast.makeText( m_MainActPt, ( String ) ( ( Object[] ) MessagePt.obj )[ 0 ], Toast.LENGTH_LONG ).show();
					break;
				}
				case MainActMsgTyp.Vibrate:
				{
					( ( Vibrator ) m_MainActPt.getSystemService( Context.VIBRATOR_SERVICE ) ).vibrate( 100 );
					break;
				}
				case MainActMsgTyp.MySrvrThrdInit:
				{
					( ( Button ) m_MainActPt.findViewById( R.id.SrvrStngBtnId ) ).setEnabled( false ); //设置服务端设置按钮为不可用。

					if( m_MyClntMediaPocsThrdPt == null ) //如果我的客户端媒体处理线程未初始化。
					{
						( ( Button ) m_MainActPt.findViewById( R.id.StngBtnId ) ).setEnabled( false ); //设置设置按钮为不可用。
						( ( Button ) m_MainActPt.findViewById( R.id.SaveStngBtnId ) ).setEnabled( false ); //设置保存设置按钮为不可用。
						( ( Button ) m_MainActPt.findViewById( R.id.ReadStngBtnId ) ).setEnabled( false ); //设置读取设置按钮为不可用。
						( ( Button ) m_MainActPt.findViewById( R.id.DelStngBtnId ) ).setEnabled( false ); //设置删除设置按钮为不可用。
						( ( Button ) m_MainActPt.findViewById( R.id.ResetStngBtnId ) ).setEnabled( false ); //设置重置设置按钮为不可用。

						//创建并绑定前台服务，从而确保本进程在转入后台或系统锁屏时不会被系统限制运行，且只能放在主线程中执行，因为要使用界面。
						if( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsUseFrgndSrvcCkBoxId ) ).isChecked() && ( m_FrgndSrvcCnctPt == null ) )
						{
							m_FrgndSrvcCnctPt = new ServiceConnection() //创建存放前台服务连接器。
							{
								@Override public void onServiceConnected( ComponentName name, IBinder service ) //前台服务绑定成功。
								{
									( ( FrgndSrvc.FrgndSrvcBinder ) service ).SetForeground( m_MainActPt ); //将服务设置为前台服务。
								}

								@Override public void onServiceDisconnected( ComponentName name ) //前台服务解除绑定。
								{

								}
							};
							m_MainActPt.bindService( new Intent( m_MainActPt, FrgndSrvc.class ), m_FrgndSrvcCnctPt, Context.BIND_AUTO_CREATE ); //创建并绑定前台服务。
						}
					}
					break;
				}
				case MainActMsgTyp.MySrvrThrdDstoy:
				{
					( ( Button ) m_MainActPt.findViewById( R.id.SrvrStngBtnId ) ).setEnabled( true ); //设置服务端设置按钮为可用。

					if( m_MyClntMediaPocsThrdPt == null ) //如果我的客户端媒体处理线程未初始化。
					{
						( ( Button ) m_MainActPt.findViewById( R.id.StngBtnId ) ).setEnabled( true ); //设置设置按钮为可用。
						( ( Button ) m_MainActPt.findViewById( R.id.SaveStngBtnId ) ).setEnabled( true ); //设置保存设置按钮为可用。
						( ( Button ) m_MainActPt.findViewById( R.id.ReadStngBtnId ) ).setEnabled( true ); //设置读取设置按钮为可用。
						( ( Button ) m_MainActPt.findViewById( R.id.DelStngBtnId ) ).setEnabled( true ); //设置删除设置按钮为可用。
						( ( Button ) m_MainActPt.findViewById( R.id.ResetStngBtnId ) ).setEnabled( true ); //设置重置设置按钮为可用。

						if( m_FrgndSrvcCnctPt != null ) //如果已经创建并绑定了前台服务。
						{
							m_MainActPt.unbindService( m_FrgndSrvcCnctPt ); //解除绑定并销毁前台服务。
							m_FrgndSrvcCnctPt = null;
						}
					}

					m_MySrvrThrdPt = null;
					break;
				}
				case MainActMsgTyp.SrvrInit:
				{
					( ( EditText ) m_MainActPt.findViewById( R.id.SrvrUrlEdTxtId ) ).setEnabled( false ); //设置服务端Url编辑框为不可用。
					( ( Spinner ) m_MainActPt.findViewById( R.id.SrvrUrlSpinnerId ) ).setEnabled( false ); //设置服务端Url下拉框为不可用。
					( ( Button ) m_MainActPt.findViewById( R.id.SrvrCreateOrDstoyBtnId ) ).setText( "销毁" ); //设置服务端创建或销毁按钮的内容为“销毁”。
					( ( Button ) m_MainActPt.findViewById( R.id.CnctDstoyBtnId ) ).setEnabled( true ); //设置服务端连接销毁按钮为可用。
					break;
				}
				case MainActMsgTyp.SrvrDstoy:
				{
					( ( EditText ) m_MainActPt.findViewById( R.id.SrvrUrlEdTxtId ) ).setEnabled( true ); //设置服务端Url编辑框为可用。
					( ( Spinner ) m_MainActPt.findViewById( R.id.SrvrUrlSpinnerId ) ).setEnabled( true ); //设置服务端Url下拉框为可用。
					( ( Button ) m_MainActPt.findViewById( R.id.SrvrCreateOrDstoyBtnId ) ).setText( "创建" ); //设置服务端创建或销毁按钮的内容为“创建”。
					( ( Button ) m_MainActPt.findViewById( R.id.CnctDstoyBtnId ) ).setEnabled( false ); //设置服务端连接销毁按钮为不可用。
					break;
				}
				case MainActMsgTyp.MyClntMediaPocsThrdInit:
				{
					( ( Button ) m_MainActPt.findViewById( R.id.ClntAddBtnId ) ).setEnabled( false ); //设置客户端添加按钮为不可用。
					( ( Button ) m_MainActPt.findViewById( R.id.ClntStngBtnId ) ).setEnabled( false ); //设置客户端设置按钮为不可用。
					( ( Button ) m_MainActPt.findViewById( R.id.ClntDelBtnId ) ).setEnabled( false ); //设置客户端删除按钮为不可用。
					if( m_MainActPt.m_MyClntMediaPocsThrdPt.m_TkbkClntPt.m_XfrMode == 0 ) ( ( Button ) m_MainActPt.findViewById( R.id.PttBtnId ) ).setVisibility( Button.VISIBLE ); //设置一键即按即通按钮为可见。

					if( m_MySrvrThrdPt == null ) //如果我的服务端线程未初始化。
					{
						( ( Button ) m_MainActPt.findViewById( R.id.StngBtnId ) ).setEnabled( false ); //设置设置按钮为不可用。
						( ( Button ) m_MainActPt.findViewById( R.id.SaveStngBtnId ) ).setEnabled( false ); //设置保存设置按钮为不可用。
						( ( Button ) m_MainActPt.findViewById( R.id.ReadStngBtnId ) ).setEnabled( false ); //设置读取设置按钮为不可用。
						( ( Button ) m_MainActPt.findViewById( R.id.DelStngBtnId ) ).setEnabled( false ); //设置删除设置按钮为不可用。
						( ( Button ) m_MainActPt.findViewById( R.id.ResetStngBtnId ) ).setEnabled( false ); //设置重置设置按钮为不可用。

						//创建并绑定前台服务，从而确保本进程在转入后台或系统锁屏时不会被系统限制运行，且只能放在主线程中执行，因为要使用界面。
						if( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsUseFrgndSrvcCkBoxId ) ).isChecked() && ( m_FrgndSrvcCnctPt == null ) )
						{
							m_FrgndSrvcCnctPt = new ServiceConnection() //创建存放前台服务连接器。
							{
								@Override public void onServiceConnected( ComponentName name, IBinder service ) //前台服务绑定成功。
								{
									( ( FrgndSrvc.FrgndSrvcBinder ) service ).SetForeground( m_MainActPt ); //将服务设置为前台服务。
								}

								@Override public void onServiceDisconnected( ComponentName name ) //前台服务解除绑定。
								{

								}
							};
							m_MainActPt.bindService( new Intent( m_MainActPt, FrgndSrvc.class ), m_FrgndSrvcCnctPt, Context.BIND_AUTO_CREATE ); //创建并绑定前台服务。
						}
					}
					break;
				}
				case MainActMsgTyp.MyClntMediaPocsThrdDstoy:
				{
					( ( Button ) m_MainActPt.findViewById( R.id.ClntAddBtnId ) ).setEnabled( true ); //设置客户端添加按钮为可用。
					( ( Button ) m_MainActPt.findViewById( R.id.ClntStngBtnId ) ).setEnabled( true ); //设置客户端设置按钮为可用。
					( ( Button ) m_MainActPt.findViewById( R.id.ClntDelBtnId ) ).setEnabled( true ); //设置客户端删除按钮为可用。
					if( m_MainActPt.m_MyClntMediaPocsThrdPt.m_TkbkClntPt.m_XfrMode == 0 ) ( ( Button ) m_MainActPt.findViewById( R.id.PttBtnId ) ).setVisibility( Button.INVISIBLE ); //设置一键即按即通按钮为不可见。

					if( m_MySrvrThrdPt == null ) //如果我的服务端线程未初始化。
					{
						( ( Button ) m_MainActPt.findViewById( R.id.StngBtnId ) ).setEnabled( true ); //设置设置按钮为可用。
						( ( Button ) m_MainActPt.findViewById( R.id.SaveStngBtnId ) ).setEnabled( true ); //设置保存设置按钮为可用。
						( ( Button ) m_MainActPt.findViewById( R.id.ReadStngBtnId ) ).setEnabled( true ); //设置读取设置按钮为可用。
						( ( Button ) m_MainActPt.findViewById( R.id.DelStngBtnId ) ).setEnabled( true ); //设置删除设置按钮为可用。
						( ( Button ) m_MainActPt.findViewById( R.id.ResetStngBtnId ) ).setEnabled( true ); //设置重置设置按钮为可用。

						if( m_FrgndSrvcCnctPt != null ) //如果已经创建并绑定了前台服务。
						{
							m_MainActPt.unbindService( m_FrgndSrvcCnctPt ); //解除绑定并销毁前台服务。
							m_FrgndSrvcCnctPt = null;
						}
					}
					m_MainActPt.m_MyClntMediaPocsThrdPt = null;
					m_AdoInptOtptUseDvcInfoPt = null;
					UpdateAdoInptOtptDvcSpinner(); //更新音频输入输出设备下拉框。
					break;
				}
				case MainActMsgTyp.TkbkClntCnctInit:
				{
					( ( Button ) m_MainActPt.findViewById( R.id.ClntCnctOrDstoyBtnId ) ).setText( "销毁" ); //设置客户端创建或销毁按钮的内容为“销毁”。
					break;
				}
				case MainActMsgTyp.TkbkClntCnctDstoy:
				{
					( ( Button ) m_MainActPt.findViewById( R.id.ClntCnctOrDstoyBtnId ) ).setText( "连接" ); //设置客户端创建或销毁按钮的内容为“连接”。
					break;
				}
				case MainActMsgTyp.CnctLstAddItem:
				{
					Map< String, String > p_CnctLstItemPt;

					p_CnctLstItemPt = new HashMap< String, String >();
					p_CnctLstItemPt.put( "CnctAndClntLstItemPrtclTxtId", ( ( Integer ) ( ( Object[] ) MessagePt.obj )[ 0 ] == 0 ) ? "Tcp" : "Audp" );
					p_CnctLstItemPt.put( "CnctAndClntLstItemRmtNodeNameTxtId", ( String ) ( ( Object[] ) MessagePt.obj )[ 1 ] );
					p_CnctLstItemPt.put( "CnctAndClntLstItemRmtNodeSrvcTxtId", ( String ) ( ( Object[] ) MessagePt.obj )[ 2 ] );
					p_CnctLstItemPt.put( "Rand", Long.toString( new Random().nextLong() ) ); //必须添加一个随机数，防止有些设备在大量连接时出现连接列表视图项目的指针出现重复，从而导致删除重复项目错误。

					m_CnctLstItemArrayLstPt.add( p_CnctLstItemPt );
					( ( SimpleAdapter ) m_CnctLstViewPt.getAdapter() ).notifyDataSetChanged(); //通知连接列表视图数据集被改变。
					break;
				}
				case MainActMsgTyp.CnctLstModifyItem:
				{
					Map< String, String > p_CnctLstItemPt;

					p_CnctLstItemPt = m_CnctLstItemArrayLstPt.get( ( Integer ) ( ( Object[] ) MessagePt.obj )[ 0 ] );
					if( ( ( Object[] ) MessagePt.obj )[ 1 ] != null ) p_CnctLstItemPt.put( "CnctAndClntLstItemTxt1TxtId", ( String ) ( ( Object[] ) MessagePt.obj )[ 1 ] );
					if( ( ( Object[] ) MessagePt.obj )[ 2 ] != null ) p_CnctLstItemPt.put( "CnctAndClntLstItemTxt2TxtId", ( String ) ( ( Object[] ) MessagePt.obj )[ 2 ] );
					if( ( ( Object[] ) MessagePt.obj )[ 3 ] != null ) p_CnctLstItemPt.put( "CnctAndClntLstItemTxt3TxtId", ( String ) ( ( Object[] ) MessagePt.obj )[ 3 ] );

					( ( SimpleAdapter ) m_CnctLstViewPt.getAdapter() ).notifyDataSetChanged(); //通知连接列表视图数据集被改变。
					break;
				}
				case MainActMsgTyp.CnctLstDelItem:
				{
					m_CnctLstItemArrayLstPt.remove( ( ( Integer ) ( ( Object[] ) MessagePt.obj )[ 0 ] ).intValue() );
					( ( SimpleAdapter ) m_CnctLstViewPt.getAdapter() ).notifyDataSetChanged(); //通知连接列表视图数据集被改变。
					break;
				}
				case MainActMsgTyp.ClntLstAddItem:
				{
					ClntLstAddItemOut:
					{
						String p_PrtclStrPt = ( String ) ( ( Object[] ) MessagePt.obj )[ 0 ];
						String p_RmtNodeNameStrPt = ( String ) ( ( Object[] ) MessagePt.obj )[ 1 ];
						String p_RmtNodeSrvcStrPt = ( String ) ( ( Object[] ) MessagePt.obj )[ 2 ];

						for( Map< String, String > p_ClntLstItemPt : m_ClntLstItemArrayLstPt )
						{
							if( ( p_ClntLstItemPt.get( "CnctAndClntLstItemPrtclTxtId" ).equals( p_PrtclStrPt ) ) &&
								( p_ClntLstItemPt.get( "CnctAndClntLstItemRmtNodeNameTxtId" ).equals( p_RmtNodeNameStrPt ) ) &&
								( p_ClntLstItemPt.get( "CnctAndClntLstItemRmtNodeSrvcTxtId" ).equals( p_RmtNodeSrvcStrPt ) ) )
							{
								Toast.makeText( m_MainActPt, "已存在相同的客户端的服务端，无需重复添加。", Toast.LENGTH_SHORT ).show();
								break ClntLstAddItemOut;
							}
						}

						Map< String, String > p_ClntLstItemPt = new HashMap< String, String >();
						p_ClntLstItemPt.put( "CnctAndClntLstItemPrtclTxtId", p_PrtclStrPt );
						p_ClntLstItemPt.put( "CnctAndClntLstItemRmtNodeNameTxtId", p_RmtNodeNameStrPt );
						p_ClntLstItemPt.put( "CnctAndClntLstItemRmtNodeSrvcTxtId", p_RmtNodeSrvcStrPt );
						m_ClntLstItemArrayLstPt.add( p_ClntLstItemPt );
						( ( SimpleAdapter ) m_ClntLstViewPt.getAdapter() ).notifyDataSetChanged(); //通知客户端列表视图数据集被改变。
					}
					break;
				}
				case MainActMsgTyp.ClntLstDelItem:
				{
					ClntLstDelItemOut:
					{
						int p_Num = ( Integer ) ( ( Object[] ) MessagePt.obj )[ 0 ];

						if( ( p_Num < 0 ) || ( p_Num >= m_ClntLstItemArrayLstPt.size() ) )
						{
							Toast.makeText( m_MainActPt, "客户端列表不存在索引为" + p_Num + "的项目，无法删除。", Toast.LENGTH_SHORT ).show();
							break ClntLstDelItemOut;
						}

						m_ClntLstItemArrayLstPt.remove( p_Num );
						( ( SimpleAdapter ) m_ClntLstViewPt.getAdapter() ).notifyDataSetChanged(); //通知客户端列表视图数据集被改变。
						m_ClntLstViewPt.setItemChecked( p_Num, false ); //设置该项目为未Checked。
					}
					break;
				}
				case MainActMsgTyp.ClntLstModifyItem:
				{
					Map< String, String > p_CnctLstItemPt;

					p_CnctLstItemPt = m_ClntLstItemArrayLstPt.get( ( Integer ) ( ( Object[] ) MessagePt.obj )[ 0 ] );
					if( ( ( Object[] ) MessagePt.obj )[ 1 ] != null ) p_CnctLstItemPt.put( "CnctAndClntLstItemTxt1TxtId", ( String ) ( ( Object[] ) MessagePt.obj )[ 1 ] );
					if( ( ( Object[] ) MessagePt.obj )[ 2 ] != null ) p_CnctLstItemPt.put( "CnctAndClntLstItemTxt2TxtId", ( String ) ( ( Object[] ) MessagePt.obj )[ 2 ] );
					if( ( ( Object[] ) MessagePt.obj )[ 3 ] != null ) p_CnctLstItemPt.put( "CnctAndClntLstItemTxt3TxtId", ( String ) ( ( Object[] ) MessagePt.obj )[ 3 ] );

					( ( SimpleAdapter ) m_ClntLstViewPt.getAdapter() ).notifyDataSetChanged(); //通知客户端列表视图数据集被改变。
					break;
				}
				case MainActMsgTyp.GetAdoInptOtptDvcInfo:
				{
					( ( HTObject )( ( Object[] ) MessagePt.obj )[ 0 ] ).m_Val = m_AdoInptOtptDvcInfoLstPt.get( m_AdoInptOtptDvcSpinnerPt.getSelectedItemPosition() );
					break;
				}
				case MainActMsgTyp.VdoInptOtptViewInit:
				{
					( ( HTObject )( ( Object[] ) MessagePt.obj )[ 1 ] ).m_Val = VdoInptOtptViewInit( ( String ) ( ( Object[] ) MessagePt.obj )[ 0 ] );
					break;
				}
				case MainActMsgTyp.VdoInptOtptViewDstoy:
				{
					VdoInptOtptViewDstoy( ( HTSurfaceView ) MessagePt.obj );
					break;
				}
				case MainActMsgTyp.VdoInptOtptViewSetTitle:
				{
					VdoInptOtptViewSetTitle( ( HTSurfaceView ) ( ( Object[] ) MessagePt.obj )[ 0 ], ( String ) ( ( Object[] ) MessagePt.obj )[ 1 ] );
					break;
				}
				case MainActMsgTyp.AdoInptOtptDvcChg:
				{
					m_AdoInptOtptUseDvcInfoPt = ( MediaPocsThrd.AdoInptOtptDvcInfo )( ( Object[] ) MessagePt.obj )[ 0 ]; //设置音频输入输出使用的设备信息的指针。
					UpdateAdoInptOtptDvcSpinner(); //更新音频输入输出设备下拉框。
					break;
				}
				case MainActMsgTyp.VdoInptDvcChg:
				{
					MediaPocsThrd.VdoInptDvcInfo p_VdoInptUseDvcInfoPt = ( MediaPocsThrd.VdoInptDvcInfo )( ( Object[] ) MessagePt.obj )[ 0 ]; //设置视频输入使用的设备信息的指针。
					if( p_VdoInptUseDvcInfoPt.m_DvcTyp == MediaPocsThrd.VdoInptDvcInfo.DvcTyp.FrontCamera ) ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseFrontCamereRdBtnId ) ).setChecked( true );
					else ( ( RadioButton ) m_MainLyotViewPt.findViewById( R.id.UseBackCamereRdBtnId ) ).setChecked( true );
					break;
				}
			}
		}
	}
	MainActHandler m_MainActHandlerPt; //存放主界面消息处理的指针。

	//发送主界面消息到主界面线程。
	public void SendMainActMsg( int MainActMsgTyp, Object... MsgParmPt )
	{
		Message p_MessagePt = new Message();
		p_MessagePt.what = MainActMsgTyp;
		p_MessagePt.obj = MsgParmPt;
		m_MainActHandlerPt.sendMessage( p_MessagePt );
	}

	//主界面的创建消息。
	@Override protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate( savedInstanceState );
		Log.i( m_CurClsNameStrPt, "onCreate" );

		m_ErrInfoVstrPt = new Vstr();
		if( m_ErrInfoVstrPt.Init( "" ) != 0 )
		{
			String p_InfoStrPt = "初始化错误信息动态字符串失败。";
			Log.e( m_CurClsNameStrPt, p_InfoStrPt );
			SendMainActMsg( MainActMsgTyp.ShowLog, p_InfoStrPt );
		}

		MediaPocsThrd.m_CtxPt = this;
		HTLong p_LmtTimeSecPt = new HTLong();
		HTLong p_RmnTimeSecPt = new HTLong();

		if( AudpSokt.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "本端高级UDP协议套接字限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "本端高级UDP协议套接字剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取本端高级UDP协议套接字的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( SpeexAec.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "Speex声学回音消除器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "Speex声学回音消除器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取Speex声学回音消除器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( WebRtcAec.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "WebRtc浮点版声学回音消除器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "WebRtc浮点版声学回音消除器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取WebRtc浮点版声学回音消除器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( WebRtcAec3.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "WebRtc第三版声学回音消除器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "WebRtc第三版声学回音消除器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取WebRtc第三版声学回音消除器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( SpeexWebRtcAec.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "SpeexWebRtc三重声学回音消除器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "SpeexWebRtc三重声学回音消除器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取SpeexWebRtc三重声学回音消除器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( RNNoise.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "RNNoise噪音抑制器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "RNNoise噪音抑制器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取RNNoise噪音抑制器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( OpenH264Encd.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "OpenH264编码器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "OpenH264编码器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取OpenH264编码器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( OpenH264Decd.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "OpenH264解码器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "OpenH264解码器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取OpenH264解码器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( android.os.Build.VERSION.SDK_INT >= 20 ) //低版本的系统无法调用SystemH264库的GetAppLmtInfo函数。
		{
			if( SystemH264Encd.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
			{
				Log.i( m_CurClsNameStrPt, "系统自带H264编码器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
				Log.i( m_CurClsNameStrPt, "系统自带H264编码器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
			}
			else
			{
				Log.e( m_CurClsNameStrPt, "获取系统自带H264编码器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
			}

			if( SystemH264Decd.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
			{
				Log.i( m_CurClsNameStrPt, "系统自带H264解码器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
				Log.i( m_CurClsNameStrPt, "系统自带H264解码器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
			}
			else
			{
				Log.e( m_CurClsNameStrPt, "获取系统自带H264解码器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
			}
		}

		if( AAjb.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "音频自适应抖动缓冲器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "音频自适应抖动缓冲器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取音频自适应抖动缓冲器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( VAjb.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "视频自适应抖动缓冲器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "视频自适应抖动缓冲器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取视频自适应抖动缓冲器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		if( AviFileWriter.GetAppLmtInfo( LicnCode.m_LicnCodePt, p_LmtTimeSecPt, p_RmnTimeSecPt, m_ErrInfoVstrPt ) == 0 )
		{
			Log.i( m_CurClsNameStrPt, "Avi文件写入器限制时间：" + p_LmtTimeSecPt.m_Val + "。" );
			Log.i( m_CurClsNameStrPt, "Avi文件写入器剩余时间：" + p_RmnTimeSecPt.m_Val + "，约" + ( p_RmnTimeSecPt.m_Val / 24 / 60 / 60 ) + "天。" );
		}
		else
		{
			Log.e( m_CurClsNameStrPt, "获取Avi文件写入器的应用程序限制信息失败。原因：" + m_ErrInfoVstrPt.GetStr() );
		}

		//创建布局。
		{
			LayoutInflater p_LyotInflater = LayoutInflater.from( this );

			m_MainLyotViewPt = p_LyotInflater.inflate( R.layout.main_lyot, null );

			m_SrvrStngLyotViewPt = p_LyotInflater.inflate( R.layout.srvr_stng_lyot, null );
			m_CnctLstViewPt = m_MainLyotViewPt.findViewById( R.id.CnctLstId );
			m_CnctLstItemArrayLstPt = new ArrayList< Map< String, String > >();
			m_CnctLstViewPt.setAdapter( new SimpleAdapter( this, m_CnctLstItemArrayLstPt, R.layout.cnct_and_clnt_lst_item, new String[]{ "CnctAndClntLstItemPrtclTxtId", "CnctAndClntLstItemRmtNodeNameTxtId", "CnctAndClntLstItemRmtNodeSrvcTxtId", "CnctAndClntLstItemTxt1TxtId", "CnctAndClntLstItemTxt2TxtId", "CnctAndClntLstItemTxt3TxtId" }, new int[]{ R.id.CnctAndClntLstItemPrtclTxtId, R.id.CnctAndClntLstItemRmtNodeNameTxtId, R.id.CnctAndClntLstItemRmtNodeSrvcTxtId, R.id.CnctAndClntLstItemTxt1TxtId, R.id.CnctAndClntLstItemTxt2TxtId, R.id.CnctAndClntLstItemTxt3TxtId } ) );
			m_ClntStngLyotViewPt = p_LyotInflater.inflate( R.layout.clnt_stng_lyot, null );
			m_ClntLstViewPt = m_MainLyotViewPt.findViewById( R.id.ClntLstId );
			m_ClntLstItemArrayLstPt = new ArrayList< Map< String, String > >();
			m_ClntLstViewPt.setAdapter( new SimpleAdapter( this, m_ClntLstItemArrayLstPt, R.layout.cnct_and_clnt_lst_item, new String[]{ "CnctAndClntLstItemPrtclTxtId", "CnctAndClntLstItemRmtNodeNameTxtId", "CnctAndClntLstItemRmtNodeSrvcTxtId", "CnctAndClntLstItemTxt1TxtId", "CnctAndClntLstItemTxt2TxtId", "CnctAndClntLstItemTxt3TxtId" }, new int[]{ R.id.CnctAndClntLstItemPrtclTxtId, R.id.CnctAndClntLstItemRmtNodeNameTxtId, R.id.CnctAndClntLstItemRmtNodeSrvcTxtId, R.id.CnctAndClntLstItemTxt1TxtId, R.id.CnctAndClntLstItemTxt2TxtId, R.id.CnctAndClntLstItemTxt3TxtId } ) );
			m_AdoInptOtptDvcSpinnerPt = ( ( Spinner ) m_MainLyotViewPt.findViewById( R.id.AdoInptOtptDvcSpinnerId ) );
			m_AdoInptOtptDvcSpinnerItemArrayLst = new ArrayList< String >();
			m_AdoInptOtptDvcSpinnerPt.setAdapter( new ArrayAdapter< String >( MainAct.this, android.R.layout.simple_spinner_dropdown_item, m_AdoInptOtptDvcSpinnerItemArrayLst ) );
			m_AdoInptOtptDvcSpinnerPt.setOnItemSelectedListener( new AdapterView.OnItemSelectedListener() //设置音频输入输出设备下拉框的选择监听器。
			{
				@Override
				public void onItemSelected( AdapterView< ? > parent, View view, int position, long id )
				{
					if( m_MyClntMediaPocsThrdPt != null ) //如果是因为用户手动选择触发的。
					{
						m_MyClntMediaPocsThrdPt.SetAdoInptOtptUseDvc( 1, m_AdoInptOtptDvcInfoLstPt.get( position ), 0 );
					}
				}

				@Override
				public void onNothingSelected( AdapterView< ? > parent )
				{

				}
			} );
			UpdateAdoInptOtptDvcSpinner(); //更新音频输入输出设备下拉框。
			if( android.os.Build.VERSION.SDK_INT >= 23 ) //注册音频输入输出设备修改回调。
			{
				android.media.AudioDeviceCallback p_AdoInptOtptDvcChgCallbackPt = new android.media.AudioDeviceCallback()
				{
					public void onAudioDevicesAdded( AudioDeviceInfo[] addedDevices )
					{
						UpdateAdoInptOtptDvcSpinner(); //更新音频输入输出设备下拉框。
					}

					public void onAudioDevicesRemoved( AudioDeviceInfo[] removedDevices )
					{
						UpdateAdoInptOtptDvcSpinner(); //更新音频输入输出设备下拉框。
					}
				};

				( ( AudioManager ) getSystemService( Context.AUDIO_SERVICE ) ).registerAudioDeviceCallback( p_AdoInptOtptDvcChgCallbackPt, null );
			}
			m_StngLyotViewPt = p_LyotInflater.inflate( R.layout.stng_lyot, null );
			m_AjbStngLyotViewPt = p_LyotInflater.inflate( R.layout.ajb_stng_lyot, null );
			m_SaveStsToTxtFileStngLyotViewPt = p_LyotInflater.inflate( R.layout.save_sts_to_txt_file_stng_lyot, null );
			m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt = p_LyotInflater.inflate( R.layout.save_ado_vdo_inpt_otpt_to_avi_file_stng_lyot, null );
			m_SpeexAecStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_aec_stng_lyot, null );
			m_WebRtcAecmStngLyotViewPt = p_LyotInflater.inflate( R.layout.webrtc_aecm_stng_lyot, null );
			m_WebRtcAecStngLyotViewPt = p_LyotInflater.inflate( R.layout.webrtc_aec_stng_lyot, null );
			m_WebRtcAec3StngLyotViewPt = p_LyotInflater.inflate( R.layout.webrtc_aec3_stng_lyot, null );
			m_SpeexWebRtcAecStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_webrtc_aec_stng_lyot, null );
			m_SpeexPrpocsNsStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_prpocs_ns_stng_lyot, null );
			m_WebRtcNsxStngLyotViewPt = p_LyotInflater.inflate( R.layout.webrtc_nsx_stng_lyot, null );
			m_WebRtcNsStngLyotViewPt = p_LyotInflater.inflate( R.layout.webrtc_ns_stng_lyot, null );
			m_SpeexPrpocsStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_prpocs_stng_lyot, null );
			m_SpeexCodecStngLyotViewPt = p_LyotInflater.inflate( R.layout.speex_codec_stng_lyot, null );
			m_SaveAdoInptOtptToWaveFileStngLyotViewPt = p_LyotInflater.inflate( R.layout.save_ado_inpt_otpt_to_wave_file_stng_lyot, null );
			m_OpenH264CodecStngLyotViewPt = p_LyotInflater.inflate( R.layout.openh264_codec_stng_lyot, null );
			m_SystemH264CodecStngLyotViewPt = p_LyotInflater.inflate( R.layout.systemh264_codec_stng_lyot, null );
		}

		//显示布局。
		setContentView( m_MainLyotViewPt ); //设置主界面的内容为主布局。
		m_CurActivityLyotViewPt = m_MainLyotViewPt; //设置当前界面布局视图。
		m_PttBtnBackgroundPt = ( ( Button ) findViewById( R.id.PttBtnId ) ).getBackground(); //设置一键即按即通按钮背景。
		m_PtbBtnBackgroundPt = ( ( Button ) findViewById( R.id.PtbBtnId ) ).getBackground(); //设置一键即按即广播按钮背景。
		( ( Button ) findViewById( R.id.PttBtnId ) ).setOnTouchListener( this ); //设置一键即按即通按钮的触摸监听器。
		( ( Button ) findViewById( R.id.PtbBtnId ) ).setOnTouchListener( this ); //设置一键即按广播按钮的触摸监听器。

		//请求权限。
		MediaPocsThrd.RqstPrmsn( this, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1 );

		//初始化主界面消息处理。
		m_MainActPt = this;
		m_MainActHandlerPt = new MainActHandler();

		//设置AppId文本框。
		( ( TextView ) m_MainLyotViewPt.findViewById( R.id.AppIdTxtId ) ).setText( "AppId：" + getApplicationContext().getPackageName() );

		//设置SysInfo文本框。
		( ( TextView ) m_MainLyotViewPt.findViewById( R.id.SysInfoTxtId ) ).setText( "CpuAbi：" + android.os.Build.CPU_ABI + "  Sdk：" + android.os.Build.VERSION.RELEASE + "  ApiLvl：" + android.os.Build.VERSION.SDK_INT );

		//设置扩展目录完整绝对路径字符串。
		{
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
			SendMainActMsg( MainActMsgTyp.ShowLog, p_InfoStrPt );
		}

		//重置设置。
		AndrdAdoVdoTkbkStng.ResetStng( this );

		//读取设置。
		if( new File( m_ExternalDirFullAbsPathStrPt + "/Stng.xml" ).exists() ) AndrdAdoVdoTkbkStng.ReadStngFromXmlFile( this );
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
			if( m_MySrvrThrdPt != null ) m_MySrvrThrdPt.Dstoy();
			if( m_MyClntMediaPocsThrdPt != null ) m_MyClntMediaPocsThrdPt.Dstoy();
			System.exit( 0 );
		}
		else if( m_CurActivityLyotViewPt == m_SrvrStngLyotViewPt )
		{
			OnClickSrvrStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_ClntStngLyotViewPt )
		{
			OnClickClntStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_StngLyotViewPt )
		{
			OnClickStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_AjbStngLyotViewPt )
		{
			this.OnClickAjbStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SaveStsToTxtFileStngLyotViewPt )
		{
			this.OnClickSaveStsToTxtFileStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt )
		{
			this.OnClickSaveAdoVdoInptOtptToAviFileStngOkBtn( null );
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
		else if( m_CurActivityLyotViewPt == m_WebRtcAec3StngLyotViewPt )
		{
			OnClickWebRtcAec3StngOkBtn( null );
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
		else if( m_CurActivityLyotViewPt == m_SpeexPrpocsStngLyotViewPt )
		{
			OnClickSpeexPrpocsStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SpeexCodecStngLyotViewPt )
		{
			OnClickSpeexCodecStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SaveAdoInptOtptToWaveFileStngLyotViewPt )
		{
			OnClickSaveAdoInptOtptToWaveFileStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_OpenH264CodecStngLyotViewPt )
		{
			OnClickOpenH264CodecStngOkBtn( null );
		}
		else if( m_CurActivityLyotViewPt == m_SystemH264CodecStngLyotViewPt )
		{
			OnClickSystemH264CodecStngOkBtn( null );
		}
	}

	//主界面横竖屏切换消息。
	@Override public void onConfigurationChanged( Configuration newConfig )
	{
		super.onConfigurationChanged( newConfig );

		if( m_MyClntMediaPocsThrdPt != null && m_MyClntMediaPocsThrdPt.m_VdoInptPt.m_IsInit != 0 ) //如果我的网络媒体处理线程已经启动，且已初始化视频输入。
		{
			m_MyClntMediaPocsThrdPt.SetVdoInpt( 1,
												m_MyClntMediaPocsThrdPt.m_VdoInptPt.m_MaxSmplRate,
												m_MyClntMediaPocsThrdPt.m_VdoInptPt.m_FrmWidth,
												m_MyClntMediaPocsThrdPt.m_VdoInptPt.m_FrmHeight,
												0,
												0,
												( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseScreenRotateAutoRdBtnId ) ).isChecked() ) ? m_MainActPt.getWindowManager().getDefaultDisplay().getRotation() * 90 :
												( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseScreenRotate0RdBtnId ) ).isChecked() ) ? 0 :
												( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseScreenRotate90RdBtnId ) ).isChecked() ) ? 90 :
												( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseScreenRotate180RdBtnId ) ).isChecked() ) ? 180 :
												( ( ( RadioButton ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.UseScreenRotate270RdBtnId ) ).isChecked() ) ? 270 : 0,
												m_MyClntMediaPocsThrdPt.m_VdoInptPt.m_DvcPt.m_PrvwSurfaceViewPt );
		}
	}

	//服务端初始化或销毁按钮。
	public void OnClickSrvrCreateOrDstoyBtn( View ViewPt )
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_MySrvrThrdPt == null ) //如果我的服务端线程未初始化。
			{
				m_MySrvrThrdPt = new MySrvrThrd( this, LicnCode.m_LicnCodePt );
				if( m_MySrvrThrdPt.Init() != 0 ) m_MySrvrThrdPt = null;
			}
			else //如果我的服务端线程已初始化。
			{
				m_MySrvrThrdPt.Dstoy();
			}

			p_Rslt = 0;
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//服务端设置按钮。
	public void OnClickSrvrStngBtn( View ViewPt )
	{
		setContentView( m_SrvrStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SrvrStngLyotViewPt;
	}

	//连接销毁按钮。
	public void OnClickCnctDstoyBtn( View ViewPt )
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			int p_Num;

			if( m_MySrvrThrdPt == null ) //如果我的服务端线程未初始化。
			{
				break Out;
			}

			p_Num = m_CnctLstViewPt.getCheckedItemPosition();
			if( p_Num != -1 )
			{
				m_MySrvrThrdPt.SendCnctDstoyMsg( 1, p_Num );
			}

			p_Rslt = 0;
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//客户端添加按钮。
	public void OnClickClntAddBtn( View ViewPt )
	{
		Vstr p_ClntSrvrUrlVstrPt = null;
		HTString p_SrvrPrtclStrPt = new HTString();
		HTString p_SrvrNodeNameStrPt = new HTString();
		HTString p_SrvrNodeSrvcStrPt = new HTString();

		Out:
		{
			p_ClntSrvrUrlVstrPt = new Vstr();
			if( p_ClntSrvrUrlVstrPt.Init( ( ( TextView )m_MainLyotViewPt.findViewById( R.id.ClntSrvrUrlEdTxtId ) ).getText().toString() ) != 0 )
			{
				String p_InfoStrPt = "初始化客户端的服务端Url动态字符串失败。";
				Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				SendMainActMsg( MainActMsgTyp.ShowLog, p_InfoStrPt );
				Toast.makeText( this, p_InfoStrPt, Toast.LENGTH_LONG ).show();
				break Out;
			}

			//解析服务端Url字符串。
			if( p_ClntSrvrUrlVstrPt.UrlParse( p_SrvrPrtclStrPt, null, null, p_SrvrNodeNameStrPt, p_SrvrNodeSrvcStrPt, null, null, null, null, m_ErrInfoVstrPt ) != 0 )
			{
				String p_InfoStrPt = "解析客户端的服务端Url字符串失败。原因：" + m_ErrInfoVstrPt.GetStr();
				Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				SendMainActMsg( MainActMsgTyp.ShowLog, p_InfoStrPt );
				Toast.makeText( this, p_InfoStrPt, Toast.LENGTH_LONG ).show();
				break Out;
			}
			if( ( p_SrvrPrtclStrPt.m_Val.equals( "Tcp" ) == false ) && ( p_SrvrPrtclStrPt.m_Val.equals( "Audp" ) == false ) )
			{
				String p_InfoStrPt = "客户端的服务端Url字符串的协议不正确。";
				Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				SendMainActMsg( MainActMsgTyp.ShowLog, p_InfoStrPt );
				Toast.makeText( this, p_InfoStrPt, Toast.LENGTH_LONG ).show();
				break Out;
			}
			if( p_SrvrNodeSrvcStrPt.m_Val.equals( "" ) )
			{
				p_SrvrNodeSrvcStrPt.m_Val = "12345";
			}

			SendMainActMsg( MainActMsgTyp.ClntLstAddItem, p_SrvrPrtclStrPt.m_Val, p_SrvrNodeNameStrPt.m_Val, p_SrvrNodeSrvcStrPt.m_Val );
		}

		if( p_ClntSrvrUrlVstrPt != null ) p_ClntSrvrUrlVstrPt.Dstoy();
	}

	//客户端设置按钮。
	public void OnClickClntStngBtn( View ViewPt )
	{
		setContentView( m_ClntStngLyotViewPt );
		m_CurActivityLyotViewPt = m_ClntStngLyotViewPt;
	}

	//客户端连接或销毁按钮。
	public void OnClickClntCnctOrDstoyBtn( View ViewPt )
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_MyClntMediaPocsThrdPt == null ) //如果我的客户端媒体处理线程未初始化。
			{
				m_MyClntMediaPocsThrdPt = new MyClntMediaPocsThrd( this, LicnCode.m_LicnCodePt );
			}

			if( m_MyClntMediaPocsThrdPt.m_TkbkClntPt.m_CnctIsInit == 0 ) //如果对讲客户端连接未初始化。
			{
				m_MyClntMediaPocsThrdPt.TkbkInit();
			}
			else //如果对讲客户端端连接已初始化。
			{
				m_MyClntMediaPocsThrdPt.TkbkDstoy();
			}

			p_Rslt = 0;
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//客户端删除按钮。
	public void OnClickClntDelBtn( View ViewPt )
	{
		int p_Num = m_ClntLstViewPt.getCheckedItemPosition();
		if( p_Num != -1 )
		{
			SendMainActMsg( MainActMsgTyp.ClntLstDelItem, p_Num );
		}
	}

	//使用音视频输入输出对讲模式复选框。
	public void OnClickUseAdoVdoInptOtptTkbkModeCkBox( View ViewPt )
	{
		if( m_MyClntMediaPocsThrdPt != null )
		{
			m_MyClntMediaPocsThrdPt.SendTkbkClntLclTkbkModeMsg( 0,
																( ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.UseAdoInptTkbkModeCkBoxId ) ).isChecked() ) ? MyClntMediaPocsThrd.TkbkMode.AdoInpt : 0 ) +
																( ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.UseAdoOtptTkbkModeCkBoxId ) ).isChecked() ) ? MyClntMediaPocsThrd.TkbkMode.AdoOtpt : 0 ) +
																( ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.UseVdoInptTkbkModeCkBoxId ) ).isChecked() ) ? MyClntMediaPocsThrd.TkbkMode.VdoInpt : 0 ) +
																( ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.UseVdoOtptTkbkModeCkBoxId ) ).isChecked() ) ? MyClntMediaPocsThrd.TkbkMode.VdoOtpt : 0 ) );

		}
	}

	//使用前置摄像头单选按钮。
	public void onClickUseFrontCamereRdBtn( View ViewPt )
	{
		if( m_MyClntMediaPocsThrdPt != null )
		{
			m_MyClntMediaPocsThrdPt.VdoInptSetUseDvc( 1, 0, -1, -1 );
		}
	}

	//使用后置摄像头单选按钮。
	public void onClickUseBackCamereRdBtn( View ViewPt )
	{
		if( m_MyClntMediaPocsThrdPt != null )
		{
			m_MyClntMediaPocsThrdPt.VdoInptSetUseDvc( 1, 1, -1, -1 );
		}
	}

	//音频输入是否静音复选框。
	public void onClickAdoInptIsMuteCkBox( View ViewPt )
	{
		if( m_MyClntMediaPocsThrdPt != null )
		{
			m_MyClntMediaPocsThrdPt.AdoInptSetIsMute( 1, ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.AdoInptIsMuteCkBoxId ) ).isChecked() ) ? 1 : 0 );
		}
	}

	//音频输出是否静音复选框。
	public void onClickAdoOtptIsMuteCkBox( View ViewPt )
	{
		if( m_MyClntMediaPocsThrdPt != null )
		{
			m_MyClntMediaPocsThrdPt.AdoOtptSetIsMute( 1, ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.AdoOtptIsMuteCkBoxId ) ).isChecked() ) ? 1 : 0 );
		}
	}

	//视频输入是否黑屏复选框。
	public void onClickVdoInptIsBlackCkBox( View ViewPt )
	{
		if( m_MyClntMediaPocsThrdPt != null )
		{
			m_MyClntMediaPocsThrdPt.VdoInptSetIsBlack( 1, ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.VdoInptIsBlackCkBoxId ) ).isChecked() ) ? 1 : 0 );
		}
	}

	//视频输出是否黑屏复选框。
	public void onClickVdoOtptIsBlackCkBox( View ViewPt )
	{
		if( m_MyClntMediaPocsThrdPt != null )
		{
			for( TkbkClnt.TkbkInfo p_TkbkInfoTmpPt : m_MyClntMediaPocsThrdPt.m_TkbkClntPt.m_TkbkInfoCntnrPt )
			{
				m_MyClntMediaPocsThrdPt.VdoOtptSetStrmIsBlack( 1, p_TkbkInfoTmpPt.m_TkbkIdx, ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.VdoOtptIsBlackCkBoxId ) ).isChecked() ) ? 1 : 0 );
			}
		}
	}

	//是否绘制音频波形到Surface复选框。
	public void onClickIsDrawAdoWavfmToSurfaceCkBox( View ViewPt )
	{
		if( m_MyClntMediaPocsThrdPt != null )
		{
			//设置音频输入是否绘制音频波形到Surface。
			m_MyClntMediaPocsThrdPt.AdoInptSetIsDrawAdoWavfmToSurface( 1,
																	   ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).isChecked() ) ? 1 : 0,
																	   ( ( SurfaceView ) findViewById( R.id.AdoInptWavfmSurfaceId ) ),
																	   ( ( SurfaceView ) findViewById( R.id.AdoRsltWavfmSurfaceId ) ) );

			//设置音频输出是否绘制音频波形到Surface。
			m_MyClntMediaPocsThrdPt.AdoOtptSetIsDrawAdoWavfmToSurface( 1,
																	   ( ( ( CheckBox ) m_MainLyotViewPt.findViewById( R.id.IsDrawAdoWavfmToSurfaceCkBoxId ) ).isChecked() ) ? 1 : 0,
																	   ( ( SurfaceView ) findViewById( R.id.AdoOtptWavfmSurfaceId ) ) );
		}
	}

	//设置按钮。
	public void OnClickStngBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//保存设置按钮。
	public void OnClickSaveStngBtn( View ViewPt )
	{
		AndrdAdoVdoTkbkStng.SaveStngToXmlFile( this );
	}

	//读取设置按钮。
	public void OnClickReadStngBtn( View ViewPt )
	{
		AndrdAdoVdoTkbkStng.ReadStngFromXmlFile( this );
	}

	//删除设置按钮。
	public void OnClickDelStngBtn( View ViewPt )
	{
		AndrdAdoVdoTkbkStng.DelStngXmlFile( this );
	}

	//重置设置按钮。
	public void OnClickResetStngBtn( View ViewPt )
	{
		AndrdAdoVdoTkbkStng.ResetStng( this );
	}

	//必读说明按钮。
	public void OnClickReadMeBtn( View ViewPt )
	{
		startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( "https://github.com/cyz7758520/Android_audio_talkback_demo_program" ) ) );
	}

	//视频输入视频输出Surface视图。
	public void onClickVdoInptOtptSurfaceView( View ViewPt )
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

	//一键即按即通或一键即按即广播按钮。
	@Override public boolean onTouch( View ViewPt, MotionEvent EventPt )
	{
		if( ViewPt.getId() == R.id.PttBtnId ) //如果是一键即按即通按钮。
		{
			switch( EventPt.getAction() )
			{
				case MotionEvent.ACTION_DOWN: //如果是按下消息。
				{
					if( m_MyClntMediaPocsThrdPt != null ) m_MyClntMediaPocsThrdPt.SendTkbkClntPttBtnDownMsg( 1 );

					ViewPt.setBackgroundColor( Color.GREEN );
					break;
				}
				case MotionEvent.ACTION_UP: //如果是弹起消息。
				{
					if( m_MyClntMediaPocsThrdPt != null ) m_MyClntMediaPocsThrdPt.SendTkbkClntPttBtnUpMsg( 1 );

					ViewPt.setBackgroundDrawable( m_PttBtnBackgroundPt );
					break;
				}
			}
		}
		else if( ViewPt.getId() == R.id.PtbBtnId ) //如果是一键即按广播按钮。
		{
			switch( EventPt.getAction() )
			{
				case MotionEvent.ACTION_DOWN: //如果是按下消息。
				{
					if( m_MyClntMediaPocsThrdPt == null ) //如果我的客户端媒体处理线程未初始化。
					{
						m_MyClntMediaPocsThrdPt = new MyClntMediaPocsThrd( this, LicnCode.m_LicnCodePt );
					}

					m_MyClntMediaPocsThrdPt.BdctInit();

					ViewPt.setBackgroundColor( Color.GREEN );
					break;
				}
				case MotionEvent.ACTION_UP: //如果是弹起消息。
				{
					m_MyClntMediaPocsThrdPt.BdctDstoy();

					ViewPt.setBackgroundDrawable( m_PtbBtnBackgroundPt );
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

	//保存状态到Txt文件设置按钮。
	public void OnClickSaveStsToTxtFileStngBtn( View ViewPt )
	{
		setContentView( m_SaveStsToTxtFileStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SaveStsToTxtFileStngLyotViewPt;
	}

	//保存音视频输入输出到Avi文件设置按钮。
	public void OnClickSaveAdoVdoInptOtptToAviFileStngBtn( View ViewPt )
	{
		setContentView( m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SaveAdoVdoInptOtptToAviFileStngLyotViewPt;
	}

	//效果等级和声学回音消除倾向单选按钮。
	public void OnClickEffectAecTendRdBtn( View ViewPt )
	{
		AndrdAdoVdoTkbkStng.SetEffectAecTendStng( this );
	}

	//是否使用调试信息。
	public void OnClickIsUseDebugInfoRdBtn( View ViewPt )
	{
		AndrdAdoVdoTkbkStng.SetDebugInfoStng( this );
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

	//WebRtc第三版声学回音消除器设置按钮。
	public void OnClickWebRtcAec3StngBtn( View ViewPt )
	{
		setContentView( m_WebRtcAec3StngLyotViewPt );
		m_CurActivityLyotViewPt = m_WebRtcAec3StngLyotViewPt;
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

	//Speex预处理器设置按钮。
	public void OnClickSpeexPrpocsStngBtn( View ViewPt )
	{
		setContentView( m_SpeexPrpocsStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SpeexPrpocsStngLyotViewPt;
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

	//保存音频输入输出到Wave文件设置按钮。
	public void OnClickSaveAdoInptOtptToWaveFileStngBtn( View ViewPt )
	{
		setContentView( m_SaveAdoInptOtptToWaveFileStngLyotViewPt );
		m_CurActivityLyotViewPt = m_SaveAdoInptOtptToWaveFileStngLyotViewPt;
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

	//设置服务端设置确定按钮。
	public void OnClickSrvrStngOkBtn( View ViewPt )
	{
		setContentView( m_MainLyotViewPt );
		m_CurActivityLyotViewPt = m_MainLyotViewPt;
	}

	//设置客户端设置确定按钮。
	public void OnClickClntStngOkBtn( View ViewPt )
	{
		setContentView( m_MainLyotViewPt );
		m_CurActivityLyotViewPt = m_MainLyotViewPt;
	}

	//设置布局确定按钮。
	public void OnClickStngOkBtn( View ViewPt )
	{
		setContentView( m_MainLyotViewPt );
		m_CurActivityLyotViewPt = m_MainLyotViewPt;
	}

	//音频自适应抖动缓冲器设置布局确定按钮。
	public void OnClickAjbStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//保存状态到Txt文件设置布局确定按钮。
	public void OnClickSaveStsToTxtFileStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//保存音视频输入输出到Avi文件设置布局确定按钮。
	public void OnClickSaveAdoVdoInptOtptToAviFileStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Speex声学回音消除器设置布局确定按钮。
	public void OnClickSpeexAecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//WebRtc定点版声学回音消除器设置布局确定按钮。
	public void OnClickWebRtcAecmStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//WebRtc浮点版声学回音消除器设置布局确定按钮。
	public void OnClickWebRtcAecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//WebRtc第三版声学回音消除器设置布局确定按钮。
	public void OnClickWebRtcAec3StngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//SpeexWebRtc三重声学回音消除器设置布局确定按钮。
	public void OnClickSpeexWebRtcAecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Speex预处理器的噪音抑制设置布局确定按钮。
	public void OnClickSpeexPrpocsNsStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//WebRtc定点版噪音抑制器设置布局确定按钮。
	public void OnClickWebRtcNsxStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//WebRtc浮点版噪音抑制器设置布局确定按钮。
	public void OnClickWebRtcNsStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Speex预处理器的其他功能设置布局确定按钮。
	public void OnClickSpeexPrpocsStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Speex编解码器设置布局确定按钮。
	public void OnClickSpeexCodecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//Opus编解码器设置布局确定按钮。
	public void OnOpusCodecSettingOkClick( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//保存音频输入输出到Wave文件设置布局确定按钮。
	public void OnClickSaveAdoInptOtptToWaveFileStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//OpenH264编解码器设置布局确定按钮。
	public void OnClickOpenH264CodecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//系统自带H264编解码器设置布局确定按钮。
	public void OnClickSystemH264CodecStngOkBtn( View ViewPt )
	{
		setContentView( m_StngLyotViewPt );
		m_CurActivityLyotViewPt = m_StngLyotViewPt;
	}

	//更新音频输入输出设备下拉框。
	public void UpdateAdoInptOtptDvcSpinner()
	{
		MediaPocsThrd.AdoInptOtptDvcInfo p_CurSelAdoInptOtptDvcInfoPt = null; //存放当前选择的音频输入输出设备信息。

		if( m_AdoInptOtptDvcSpinnerPt.getSelectedItemPosition() != AdapterView.INVALID_POSITION ) p_CurSelAdoInptOtptDvcInfoPt = m_AdoInptOtptDvcInfoLstPt.get( m_AdoInptOtptDvcSpinnerPt.getSelectedItemPosition() ); //设置当前选择的音频输入输出设备信息。

		m_AdoInptOtptDvcInfoLstPt = MediaPocsThrd.GetAllAdoInptOtptDvcInfo(); //更新音频输入输出设备信息列表。

		//更新音频输入输出设备下拉框项目数组列表。
		m_AdoInptOtptDvcSpinnerItemArrayLst.clear();
		for( MediaPocsThrd.AdoInptOtptDvcInfo m_AdoInptOtptDvcInfoPt : m_AdoInptOtptDvcInfoLstPt )
		{
			m_AdoInptOtptDvcSpinnerItemArrayLst.add( m_AdoInptOtptDvcInfoPt.m_NameStrPt );
		}
		if( android.os.Build.VERSION.SDK_INT >= 23 )
		{
			if( m_AdoInptOtptUseDvcInfoPt != null )
			{
				if( m_AdoInptOtptUseDvcInfoPt.m_NameStrPt.equals( "默认扬声器" ) ) //如果要使用默认扬声器。
				{
					if( m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt != null ) m_AdoInptOtptDvcSpinnerItemArrayLst.set( 0, "默认扬声器：" + MediaPocsThrd.GetAdoDvcInfoTypeName( m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getType() ) + m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getProductName() );
					else if( m_AdoInptOtptUseDvcInfoPt.m_AdoInptDvcInfoPt != null ) m_AdoInptOtptDvcSpinnerItemArrayLst.set( 0, "默认扬声器：" + MediaPocsThrd.GetAdoDvcInfoTypeName( m_AdoInptOtptUseDvcInfoPt.m_AdoInptDvcInfoPt.getType() ) + m_AdoInptOtptUseDvcInfoPt.m_AdoInptDvcInfoPt.getProductName() );
					else m_AdoInptOtptDvcSpinnerItemArrayLst.set( 0, "默认扬声器" );
					m_AdoInptOtptDvcSpinnerItemArrayLst.set( 1, "默认听筒" );
				}
				else if( m_AdoInptOtptUseDvcInfoPt.m_NameStrPt.equals( "默认听筒" ) ) //如果要使用默认听筒。
				{
					m_AdoInptOtptDvcSpinnerItemArrayLst.set( 0, "默认扬声器" );
					if( m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt != null ) m_AdoInptOtptDvcSpinnerItemArrayLst.set( 1, "默认听筒：" + MediaPocsThrd.GetAdoDvcInfoTypeName( m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getType() ) + m_AdoInptOtptUseDvcInfoPt.m_AdoOtptDvcInfoPt.getProductName() );
					else if( m_AdoInptOtptUseDvcInfoPt.m_AdoInptDvcInfoPt != null ) m_AdoInptOtptDvcSpinnerItemArrayLst.set( 1, "默认听筒：" + MediaPocsThrd.GetAdoDvcInfoTypeName( m_AdoInptOtptUseDvcInfoPt.m_AdoInptDvcInfoPt.getType() ) + m_AdoInptOtptUseDvcInfoPt.m_AdoInptDvcInfoPt.getProductName() );
					else m_AdoInptOtptDvcSpinnerItemArrayLst.set( 1, "默认听筒" );
				}
				else //如果要使用指定的设备。
				{

				}
			}
		}
		( ( ArrayAdapter< String > ) m_AdoInptOtptDvcSpinnerPt.getAdapter() ).notifyDataSetChanged();

		//继续选择之前选择的音频输入输出设备。
		if( p_CurSelAdoInptOtptDvcInfoPt != null )
		{
			OutFindAdoInptOtptDvcInfo:
			{
				for( int i = 0; i < m_AdoInptOtptDvcInfoLstPt.size(); i++ ) //查找要使用的设备。
				{
					if( m_AdoInptOtptDvcInfoLstPt.get( i ).m_NameStrPt.equals( p_CurSelAdoInptOtptDvcInfoPt.m_NameStrPt ) )
					{
						m_AdoInptOtptDvcSpinnerPt.setSelection( i );
						break OutFindAdoInptOtptDvcInfo;
					}
				}
				m_AdoInptOtptDvcSpinnerPt.setSelection( 0 ); //如果没有找到就使用默认扬声器。
			}
		}
	}

	//发送视频输入输出视图初始化消息。
	public MediaPocsThrd.AdoInptOtptDvcInfo SendGetAdoInptOtptDvcInfoMsg()
	{
		Message p_MessagePt = new Message();
		HTObject p_HTObjectPt = new HTObject();
		p_MessagePt.what = MainActMsgTyp.GetAdoInptOtptDvcInfo;
		p_MessagePt.obj = new Object[]{ p_HTObjectPt };
		m_MainActHandlerPt.sendMessage( p_MessagePt );
		while( p_HTObjectPt.m_Val == null ) SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
		return ( MediaPocsThrd.AdoInptOtptDvcInfo ) p_HTObjectPt.m_Val;
	}

	//视频输入输出视图初始化。
	public HTSurfaceView VdoInptOtptViewInit( String TitleStrPt )
	{
		LinearLayout p_LinearLyotPt = new LinearLayout( this );
		TextView p_TxtViewPt = new TextView( this );
		HTSurfaceView p_HTSurfaceViewPt = new HTSurfaceView( this );
		ViewGroup.LayoutParams p_LayoutParamsPt;

		if( TitleStrPt.substring( 0, 4 ).equals( "视频输入" ) ) ( ( LinearLayout )findViewById( R.id.VdoInptOtptLinearLyotId ) ).addView( p_LinearLyotPt, 0 );
		else ( ( LinearLayout )findViewById( R.id.VdoInptOtptLinearLyotId ) ).addView( p_LinearLyotPt );
		p_LinearLyotPt.addView( p_TxtViewPt );
		p_LinearLyotPt.addView( p_HTSurfaceViewPt );

		p_LinearLyotPt.setOrientation( LinearLayout.VERTICAL );
		p_LayoutParamsPt = p_LinearLyotPt.getLayoutParams();
		p_LayoutParamsPt.width = LinearLayout.LayoutParams.MATCH_PARENT;
		p_LayoutParamsPt.height = LinearLayout.LayoutParams.WRAP_CONTENT;
		( ( LinearLayout.LayoutParams )p_LayoutParamsPt ).weight = 1;
		p_LinearLyotPt.setLayoutParams( p_LayoutParamsPt );

		p_TxtViewPt.setText( TitleStrPt );
		p_TxtViewPt.setGravity( Gravity.CENTER );
		p_LayoutParamsPt = p_TxtViewPt.getLayoutParams();
		p_LayoutParamsPt.width = LinearLayout.LayoutParams.MATCH_PARENT;
		p_LayoutParamsPt.height = LinearLayout.LayoutParams.WRAP_CONTENT;
		p_TxtViewPt.setLayoutParams( p_LayoutParamsPt );

		p_HTSurfaceViewPt.setBackgroundColor( Color.TRANSPARENT );
		p_LayoutParamsPt = p_HTSurfaceViewPt.getLayoutParams();
		p_LayoutParamsPt.width = LinearLayout.LayoutParams.MATCH_PARENT;
		p_LayoutParamsPt.height = 1;
		( ( LinearLayout.LayoutParams )p_LayoutParamsPt ).weight = 1;
		p_HTSurfaceViewPt.setLayoutParams( p_LayoutParamsPt );
		p_HTSurfaceViewPt.setOnClickListener( v -> onClickVdoInptOtptSurfaceView( p_HTSurfaceViewPt ) );

		return p_HTSurfaceViewPt;
	}

	//发送视频输入输出视图初始化消息。
	public HTSurfaceView SendVdoInptOtptViewInitMsg( String TitleStrPt )
	{
		Message p_MessagePt = new Message();
		HTObject p_HTObjectPt = new HTObject();
		p_MessagePt.what = MainActMsgTyp.VdoInptOtptViewInit;
		p_MessagePt.obj = new Object[]{ TitleStrPt, p_HTObjectPt };
		m_MainActHandlerPt.sendMessage( p_MessagePt );
		while( p_HTObjectPt.m_Val == null ) SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
		return ( HTSurfaceView ) p_HTObjectPt.m_Val;
	}

	//视频输入输出视图销毁。
	public void VdoInptOtptViewDstoy( HTSurfaceView DspyHTSurfaceViewPt )
	{
		if( DspyHTSurfaceViewPt != null ) ( ( LinearLayout )findViewById( R.id.VdoInptOtptLinearLyotId ) ).removeView( ( View )DspyHTSurfaceViewPt.getParent() );
	}

	//发送视频输入输出视图销毁消息。
	public void SendVdoInptOtptViewDstoyMsg( HTSurfaceView DspyHTSurfaceViewPt )
	{
		Message p_MessagePt = new Message();
		p_MessagePt.what = MainActMsgTyp.VdoInptOtptViewDstoy;
		p_MessagePt.obj = DspyHTSurfaceViewPt;
		m_MainActHandlerPt.sendMessage( p_MessagePt );
	}

	//视频输入输出视图设置标题。
	public void VdoInptOtptViewSetTitle( HTSurfaceView DspyHTSurfaceViewPt, String TitleStrPt )
	{
		if( ( DspyHTSurfaceViewPt != null ) && ( TitleStrPt != null ) ) ( ( TextView )( ( LinearLayout )DspyHTSurfaceViewPt.getParent() ).getChildAt( 0 ) ).setText( TitleStrPt );
	}

	//发送视频输入输出视图设置标题消息。
	public void SendVdoInptOtptViewSetTitleMsg( HTSurfaceView DspyHTSurfaceViewPt, String TitleStrPt )
	{
		Message p_MessagePt = new Message();
		p_MessagePt.what = MainActMsgTyp.VdoInptOtptViewSetTitle;
		p_MessagePt.obj = new Object[]{ DspyHTSurfaceViewPt, TitleStrPt };
		m_MainActHandlerPt.sendMessage( p_MessagePt );
	}
}
