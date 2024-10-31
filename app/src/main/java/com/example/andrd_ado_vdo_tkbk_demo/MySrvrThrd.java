package com.example.andrd_ado_vdo_tkbk_demo;

import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import HeavenTao.Data.HTString;
import HeavenTao.Data.Vstr;
import HeavenTao.Media.MediaPocsThrd;
import HeavenTao.Media.SrvrThrd;

public class MySrvrThrd extends SrvrThrd
{
	public static String m_CurClsNameStrPt = "MySrvrThrd"; //存放当前类名称字符串。

	public MainAct m_MainActPt; //存放主界面的指针。

	MySrvrThrd( MainAct MainActPt, byte LicnCodePt[] )
	{
		super( MainActPt, LicnCodePt );

		MediaPocsThrd.m_CtxPt = MainActPt;

		m_MainActPt = MainActPt;
	}

	//我的服务端线程初始化。
	public int Init()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。
		Vstr p_SrvrUrlVstrPt = null;
		Vstr p_ErrInfoVstrPt = null;
		HTString p_SrvrPrtclStrPt = new HTString();
		HTString p_SrvrNodeNameStrPt = new HTString();
		HTString p_SrvrNodeSrvcStrPt = new HTString();

		Out:
		{
			if( isAlive() == false ) //如果我的服务端线程未启动。
			{
				p_SrvrUrlVstrPt = new Vstr();
				if( p_SrvrUrlVstrPt.Init( ( ( TextView )m_MainActPt.m_MainLyotViewPt.findViewById( R.id.SrvrUrlEdTxtId ) ).getText().toString() ) != 0 )
				{
					String p_InfoStrPt = "初始化服务端Url动态字符串失败。";
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowLog, p_InfoStrPt );
					Toast.makeText( m_MainActPt, p_InfoStrPt, Toast.LENGTH_LONG ).show();
					break Out;
				}
				p_ErrInfoVstrPt = new Vstr();
				if( p_ErrInfoVstrPt.Init( "" ) != 0 )
				{
					String p_InfoStrPt = "初始化错误信息动态字符串失败。";
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowLog, p_InfoStrPt );
					break Out;
				}

				//解析服务端Url字符串。
				if( p_SrvrUrlVstrPt.UrlParse( p_SrvrPrtclStrPt, null, null, p_SrvrNodeNameStrPt, p_SrvrNodeSrvcStrPt, null, null, null, null, p_ErrInfoVstrPt ) != 0 )
				{
					String p_InfoStrPt = "解析服务端Url字符串失败。原因：" + p_ErrInfoVstrPt.GetStr();
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowLog, p_InfoStrPt );
					Toast.makeText( m_MainActPt, p_InfoStrPt, Toast.LENGTH_LONG ).show();
					break Out;
				}
				if( ( p_SrvrPrtclStrPt.m_Val.equals( "Tcp" ) == false ) && ( p_SrvrPrtclStrPt.m_Val.equals( "Audp" ) == false ) )
				{
					String p_InfoStrPt = "服务端Url字符串的协议不正确。";
					Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowLog, p_InfoStrPt );
					Toast.makeText( m_MainActPt, p_InfoStrPt, Toast.LENGTH_LONG ).show();
					break Out;
				}
				if( p_SrvrNodeSrvcStrPt.m_Val.equals( "" ) )
				{
					p_SrvrNodeSrvcStrPt.m_Val = "12345";
				}

				Log.i( m_CurClsNameStrPt, "我的服务端线程初始化开始。" );

				//设置在所有连接和服务端都销毁时自动请求退出。
				m_IsAutoRqirExit = 2;

				//设置是否打印Logcat日志、显示Toast。
				SetIsPrintLogcatShowToast( ( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsPrintLogcatShowToastCkBoxId ) ).isChecked() ) ? 1 : 0,
										   ( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsPrintLogcatShowToastCkBoxId ) ).isChecked() ) ? 1 : 0,
										   m_MainActPt );

				//设置是否使用唤醒锁。
				SendSetIsUseWakeLockMsg( 0,
										 ( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsUseWakeLockCkBoxId ) ).isChecked() ) ? 1 : 0 );

				//设置是否测试网络延迟。
				SendSetIsTstNtwkDlyMsg( 0,
										( ( ( CheckBox ) m_MainActPt.m_StngLyotViewPt.findViewById( R.id.IsTstNtwkDlyCkBoxId ) ).isChecked() ) ? 1 : 0,
										1000 );

				//设置服务端初始化。
				SendSrvrInitMsg( 1,
								 p_SrvrPrtclStrPt.m_Val.equals( "Tcp" ) ? 0 : 1,
								 p_SrvrNodeNameStrPt.m_Val,
								 p_SrvrNodeSrvcStrPt.m_Val,
								 Integer.parseInt( ( ( TextView ) m_MainActPt.m_SrvrStngLyotViewPt.findViewById( R.id.MaxCnctNumEdTxtId ) ).getText().toString() ), 2 );

				//启动我的服务端线程。
				start();

				Log.i( m_CurClsNameStrPt, "我的服务端线程初始化结束。" );
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_SrvrUrlVstrPt != null ) p_SrvrUrlVstrPt.Dstoy();
		if( p_ErrInfoVstrPt != null ) p_ErrInfoVstrPt.Dstoy();
		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			Dstoy();
		}
		return p_Rslt;
	}

	//我的服务端线程销毁。
	public void Dstoy()
	{
		Log.i( m_CurClsNameStrPt, "请求并等待我的服务端线程退出开始。" );
		SendRqirExitMsg( 1 );
		Log.i( m_CurClsNameStrPt, "请求并等待我的服务端线程退出结束。" );
	}

	//用户定义的服务端线程初始化函数。
	@Override public void UserSrvrThrdInit()
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MySrvrThrdInit );
	}

	//用户定义的服务端线程销毁函数。
	@Override public void UserSrvrThrdDstoy()
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MySrvrThrdDstoy );
	}

	//用户定义的显示日志函数。
	@Override public void UserShowLog( String InfoStrPt )
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowLog, InfoStrPt );
	}

	//用户定义的显示Toast函数。
	@Override public void UserShowToast( String InfoStrPt )
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowToast, InfoStrPt );
	}

	//用户定义的振动函数。
	@Override public void UserVibrate()
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.Vibrate );
	}

	//用户定义的消息函数。
	@Override public int UserMsg( int MsgTyp, Object[] MsgArgPt )
	{
		return 0;
	}

	//用户定义的服务端初始化函数。
	@Override public void UserSrvrInit()
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.SrvrInit );
	}

	//用户定义的服务端销毁函数。
	@Override public void UserSrvrDstoy()
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.SrvrDstoy );
	}

	//用户定义的连接初始化函数。
	@Override public void UserCnctInit( CnctInfo CnctInfoPt, int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstAddItem, IsTcpOrAudpPrtcl, RmtNodeNameStrPt, RmtNodeSrvcStrPt );
	}

	//用户定义的连接销毁函数。
	@Override public void UserCnctDstoy( CnctInfo CnctInfoPt )
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstDelItem, CnctInfoPt.m_Num );
	}

	//用户定义的连接状态函数。
	@Override public void UserCnctSts( CnctInfo CnctInfoPt, int CurCnctSts )
	{
		if( CurCnctSts == SrvrThrd.CnctSts.Wait )
		{
			m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "等待远端接受连接", null, null );
		}
		else if( CurCnctSts < SrvrThrd.CnctSts.Wait )
		{
			m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "第" + -CurCnctSts + "次连接", null, null );
		}
		else if( CurCnctSts == SrvrThrd.CnctSts.Cnct )
		{
			m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "已连接", null, null );
		}
		else if( CurCnctSts == SrvrThrd.CnctSts.Tmot )
		{
			m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "异常断开", null, null );
		}
		else if( CurCnctSts == SrvrThrd.CnctSts.Dsct )
		{
			m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "已断开", null, null );
		}
	}

	//用户定义的连接远端对讲模式函数。
	@Override public void UserCnctRmtTkbkMode( CnctInfo CnctInfoPt, int OldRmtTkbkMode, int NewRmtTkbkMode )
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, null, m_TkbkModeStrArrPt[ NewRmtTkbkMode ], null );
	}

	//用户定义的连接测试网络延迟函数。
	@Override public void UserCnctTstNtwkDly( CnctInfo CnctInfoPt, long NtwkDlyMsec )
	{
		m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, null, null, NtwkDlyMsec + "\nms" );
	}
}
