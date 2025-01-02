package HeavenTao.Media;

import android.app.Activity;
import android.content.Context;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

public abstract class SrvrThrd extends Thread //服务端线程。
{
	public static String m_CurClsNameStrPt = "SrvrThrd"; //存放当前类名称字符串。

	public class ThrdMsgTyp
	{
		public static final int SetIsUseWakeLock = 0;
		public static final int SetIsTstNtwkDly  = 1;

		public static final int SrvrInit = 2;
		public static final int SrvrDstoy = 3;

		public static final int CnctDstoy = 4;

		public static final int RqirExit = 5;

		public static final int UserMsgMinVal = 100; //用户消息的最小值。
	}
	public final MsgQueue m_ThrdMsgQueuePt = new MsgQueue( this ) //存放线程消息队列的指针。
	{
		@Override public int UserMsgPocs( int MsgTyp, Object[] MsgArgPt )
		{
			return ThrdMsgPocs( MsgTyp, MsgArgPt );
		}
	};

	public class PktTyp //数据包类型。
	{
		public static final int TkbkIdx = 0; //对讲索引包。
		public static final int TkbkMode = 1; //对讲模式包。
		public static final int AdoFrm = 2; //音频输入输出帧包。
		public static final int VdoFrm = 3; //视频输入输出帧包。
		public static final int TstNtwkDly = 4; //测试网络延迟包。
		public static final int TstNtwkDlyRply = 5; //测试网络延迟应答包。
		public static final int Exit = 6; //退出包。
	}

	public class TkbkMode //对讲模式。
	{
		public static final int None = 0; //挂起。
		public static final int AdoInpt = 1; //音频输入。
		public static final int AdoOtpt = 2; //音频输出。
		public static final int VdoInpt = 4; //视频输入。
		public static final int VdoOtpt = 8; //视频输出。
		public static final int Ado = AdoInpt | AdoOtpt; //音频。
		public static final int Vdo = VdoInpt | VdoOtpt; //视频。
		public static final int AdoVdo = Ado | Vdo; //音视频。
		public static final int NoChg = VdoOtpt << 1; //不变。
	}
	public static String m_TkbkModeStrArrPt[] = {
			"挂起", //0：挂起。
			"音入", //1：音频输入。
			"音出", //2：音频输出。
			"音入出", //3：音频输入、音频输出。
			"视入", //4：视频输入。
			"音入视入", //5：音频输入、视频输入。
			"音出视入", //6：音频输出、视频输入。
			"音入出视入", //7：音频输入、音频输出、视频输入。
			"视出", //8：视频输出。
			"音入视出", //9：音频输入、视频输出。
			"音出视出", //10：音频输出、视频输出。
			"音入出视出", //11：音频输入、音频输出、视频输出。
			"视入出", //12：视频输入、视频输出。
			"音入视入出", //13：音频输入、视频输入、视频输出。
			"音出视入出", //14：音频输出、视频输入、视频输出。
			"音入出视入出", //15：音频输入、音频输出、视频输入、视频输出。
			"不变", //16：不变。
	};

	public class CnctSts //连接状态。
	{
		public static final int Wait = 0; //等待远端接受连接。
		public static final int Cnct = 1; //已连接。
		public static final int Tmot = 2; //超时未接收任何数据包。异常断开。
		public static final int Dsct = 3; //已断开。
	}

	public int m_IsRqirExit; //存放是否请求退出，为0表示未请求退出，为1表示已请求退出。

	public static Context m_CtxPt; //存放上下文的指针。

	public static byte m_LicnCodePt[]; //存放授权码。

	public int m_IsPrintLogcat; //存放是否打印Logcat日志，为非0表示要打印，为0表示不打印。
	public int m_IsShowToast; //存放是否显示Toast，为非0表示要显示，为0表示不显示。
	public Activity m_ShowToastActPt; //存放显示Toast界面的指针。

	int m_IsUseWakeLock; //存放是否使用唤醒锁，为非0表示要使用，为0表示不使用。
	PowerManager.WakeLock m_FullWakeLockPt; //存放屏幕键盘全亮唤醒锁的指针。

	public class TstNtwkDly //存放测试网络延迟。
	{
		public int m_IsTstNtwkDly; //存放是否测试网络延迟。
		public long m_SendIntvlMsec; //存放发送间隔，单位为毫秒。
	}
	public TstNtwkDly m_TstNtwkDlyPt = new TstNtwkDly();

	public int m_SrvrIsInit; //存放服务端是否初始化，为0表示未初始化，为1表示已初始化。
	public TcpSrvrSokt m_TcpSrvrSoktPt; //存放本端Tcp协议服务端套接字的指针。
	public AudpSokt m_AudpSrvrSoktPt; //存放本端高级Udp协议服务端套接字的指针。
	public int m_IsAutoRqirExit; //存放是否自动请求退出，为0表示手动，为1表示在所有连接销毁时自动请求退出，为2表示在所有连接和服务端都销毁时自动请求退出。

	public class CnctInfo //连接信息。
	{
		public int m_Idx; //存放对讲索引，从0开始，连接信息容器的唯一对讲索引，连接中途不会改变。
		public int m_IsInit; //存放连接信息是否初始化，为0表示未初始化，为非0表示已初始化。
		public int m_Num; //存放序号，从0开始，随着前面的连接销毁而递减。

		public int m_IsTcpOrAudpPrtcl; //存放是否是Tcp或Udp协议，为0表示Tcp协议，为1表示高级Udp协议。
		public String m_RmtNodeNameStrPt; //存放远端套接字绑定的远端节点名称字符串的指针，
		public String m_RmtNodeSrvcStrPt; //存放远端套接字绑定的远端节点服务字符串的指针，
		public TcpClntSokt m_TcpClntSoktPt; //存放本端Tcp协议客户端套接字的指针。
		public long m_AudpClntCnctIdx; //存放本端高级Udp协议客户端连接索引。
		public int m_IsRqstDstoy; //存放是否请求销毁，为0表示未请求，为1表示已请求。

		public int m_CurCnctSts; //存放当前连接状态，为[-m_MaxCnctTimes,0]表示等待远端接受连接。
		public int m_RmtTkbkMode; //存放远端对讲模式。

		public int m_IsRecvExitPkt; //存放是否接收退出包，为0表示未接收，为1表示已接收。

		public class TstNtwkDly //存放测试网络延迟。
		{
			public long m_LastSendTickMsec; //存放最后发送的嘀嗒钟，单位为毫秒。
			public int m_IsRecvRplyPkt; //存放是否接收应答包，为0表示未接收，为1表示已接收。
		}
		public TstNtwkDly m_TstNtwkDlyPt = new TstNtwkDly();
	}
	public ArrayList< CnctInfo > m_CnctInfoCntnrPt = new ArrayList<>(); //存放连接信息容器的指针。
	public int m_CnctInfoCurMaxNum; //存放连接信息的当前最大序号。
	public int m_MaxCnctNum; //存放最大连接数。

	class Thrd //线程。
	{
		HTString m_LclNodeAddrPt = new HTString(); //存放本端节点名称字符串的指针。
		HTString m_LclNodePortPt = new HTString(); //存放本端节点端口字符串的指针。
		HTString m_RmtNodeAddrPt = new HTString(); //存放远端节点名称字符串的指针。
		HTString m_RmtNodePortPt = new HTString(); //存放远端节点端口字符串的指针。

		byte m_TmpBytePt[] = new byte[ 1024 * 1024 ]; //存放临时数据。
		HTInt m_TmpHTIntPt = new HTInt(); //存放临时数据。
		HTLong m_TmpHTLongPt = new HTLong(); //存放临时数据。
	}
	Thrd m_ThrdPt = new Thrd(); //存放线程。

	public Vstr m_ErrInfoVstrPt = new Vstr(); //存放错误信息动态字符串的指针。

	//用户定义的相关回调函数。

	//用户定义的初始化函数。
	public abstract void UserInit();

	//用户定义的销毁函数。
	public abstract void UserDstoy();

	//用户定义的处理函数。
	public abstract void UserPocs();

	//用户定义的消息函数。
	public abstract int UserMsg( int MsgTyp, Object MsgArgPt[] );

	//用户定义的显示日志函数。
	public abstract void UserShowLog( String InfoStrPt );

	//用户定义的显示Toast函数。
	public abstract void UserShowToast( String InfoStrPt );

	//用户定义的振动函数。
	public abstract void UserVibrate();

	//用户定义的服务端初始化函数。
	public abstract void UserSrvrInit();

	//用户定义的服务端销毁函数。
	public abstract void UserSrvrDstoy();

	//用户定义的连接初始化函数。
	public abstract void UserCnctInit( CnctInfo CnctInfoPt, int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt );

	//用户定义的连接销毁函数。
	public abstract void UserCnctDstoy( CnctInfo CnctInfoPt );

	//用户定义的连接状态函数。
	public abstract void UserCnctSts( CnctInfo CnctInfoPt, int CurCnctSts );

	//用户定义的连接远端对讲模式函数。
	public abstract void UserCnctRmtTkbkMode( CnctInfo CnctInfoPt, int OldRmtTkbkMode, int NewRmtTkbkMode );

	//用户定义的连接测试网络延迟函数。
	public abstract void UserCnctTstNtwkDly( CnctInfo CnctInfoPt, long NtwkDlyMsec );

	//构造函数。
	public SrvrThrd( Context CtxPt, byte LicnCodePt[] )
	{
		m_CtxPt = CtxPt; //设置上下文的指针。

		m_LicnCodePt = LicnCodePt; //设置授权码。

		m_CnctInfoCurMaxNum = -1; //设置连接信息的当前最大序号。

		//初始化错误信息动态字符串。
		m_ErrInfoVstrPt.Init( null );
	}

	//设置是否打印Logcat日志、显示Toast。
	public int SetIsPrintLogcatShowToast( int IsPrintLogcat, int IsShowToast, Activity ShowToastActPt )
	{
		if( ( IsShowToast != 0 ) && ( ShowToastActPt == null ) ) //如果显示Toast界面的指针不正确。
		{
			return -1;
		}

		m_IsPrintLogcat = IsPrintLogcat;
		m_IsShowToast = IsShowToast;
		m_ShowToastActPt = ShowToastActPt;

		return 0;
	}

	//发送设置是否使用唤醒锁消息。
	public int SendSetIsUseWakeLockMsg( int IsBlockWait, int IsUseWakeLock )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SetIsUseWakeLock, IsUseWakeLock );
	}

	//发送设置是否测试网络延迟消息。
	public int SendSetIsTstNtwkDlyMsg( int IsBlockWait, int IsTstNtwkDly, long SendIntvlMsec )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SetIsTstNtwkDly, IsTstNtwkDly, SendIntvlMsec );
	}

	//唤醒锁初始化或销毁。
	private void WakeLockInitOrDstoy( int IsInitWakeLock )
	{
		if( IsInitWakeLock != 0 ) //如果要初始化唤醒锁。
		{
			if( m_FullWakeLockPt == null ) //如果屏幕键盘全亮唤醒锁还没有初始化。
			{
				m_FullWakeLockPt = ( ( PowerManager ) m_CtxPt.getSystemService( Activity.POWER_SERVICE ) ).newWakeLock( PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, m_CurClsNameStrPt );
				if( m_FullWakeLockPt != null )
				{
					m_FullWakeLockPt.acquire();
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：初始化屏幕键盘全亮唤醒锁成功。" );
				}
				else
				{
					if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：初始化屏幕键盘全亮唤醒锁失败。" );
				}
			}
		}
		else //如果要销毁唤醒锁。
		{
			//销毁唤醒锁。
			if( m_FullWakeLockPt != null )
			{
				try
				{
					m_FullWakeLockPt.release();
				}
				catch( RuntimeException ignored )
				{
				}
				m_FullWakeLockPt = null;
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：销毁屏幕键盘全亮唤醒锁成功。" );
			}
		}
	}

	//发送请求退出消息。
	public int SendRqirExitMsg( int IsBlockWait )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.RqirExit );
	}

	//发送用户消息。
	public int SendUserMsg( int IsBlockWait, int MsgTyp, Object... MsgArgPt )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.UserMsgMinVal + MsgTyp, MsgArgPt );
	}

	//发送服务端初始化消息。
	public int SendSrvrInitMsg( int IsBlockWait, int IsTcpOrAudpPrtcl, String LclNodeNameStrPt, String LclNodeSrvcStrPt, int MaxCnctNum, int IsAutoRqirExit )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SrvrInit, IsTcpOrAudpPrtcl, LclNodeNameStrPt, LclNodeSrvcStrPt, MaxCnctNum, IsAutoRqirExit );
	}

	//发送服务端销毁消息。
	public int SendSrvrDstoyMsg( int IsBlockWait )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.SrvrDstoy );
	}

	//发送连接销毁消息。
	public int SendCnctDstoyMsg( int IsBlockWait, int CnctNum )
	{
		return m_ThrdMsgQueuePt.SendMsg( IsBlockWait, 1, ThrdMsgTyp.CnctDstoy, CnctNum );
	}

	//服务端初始化。
	private int SrvrInit( int IsTcpOrAudpPrtcl, String LclNodeNameStrPt, String LclNodeSrvcStrPt, int MaxCnctNum, int IsAutoRqirExit )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		int p_LclNodeAddrFamly; //存放本端节点的地址族，为4表示IPv4，为6表示IPv6，为0表示自动选择。

		Out:
		{
			if( m_SrvrIsInit != 0 ) //如果服务端已初始化。
			{
				String p_InfoStrPt = "服务端线程：服务端已初始化，无法再次初始化。";
				if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				UserShowLog( p_InfoStrPt );
				break Out;
			}

			UserSrvrInit(); //调用用户定义的服务端初始化函数。

			m_SrvrIsInit = 1; //设置服务端已初始化。

			{ //设置远端节点的地址族。
				try
				{
					InetAddress inetAddress = InetAddress.getByName( LclNodeNameStrPt );
					if( inetAddress.getAddress().length == 4 ) p_LclNodeAddrFamly = 4;
					else p_LclNodeAddrFamly = 6;
				}
				catch( UnknownHostException e )
				{
					p_LclNodeAddrFamly = 0;
				}
			}

			if( IsTcpOrAudpPrtcl == 0 ) //如果要使用Tcp协议。
			{
				m_TcpSrvrSoktPt = new TcpSrvrSokt();

				if( m_TcpSrvrSoktPt.Init( p_LclNodeAddrFamly, LclNodeNameStrPt, LclNodeSrvcStrPt, 1, 1, m_ErrInfoVstrPt ) == 0 ) //如果初始化本端Tcp协议服务端套接字成功。
				{
					if( m_TcpSrvrSoktPt.GetLclAddr( null, m_ThrdPt.m_LclNodeAddrPt, m_ThrdPt.m_LclNodePortPt, 0, m_ErrInfoVstrPt ) != 0 ) //如果获取本端Tcp协议服务端套接字绑定的本地节点地址和端口失败。
					{
						String p_InfoStrPt = "服务端线程：获取本端Tcp协议服务端套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVstrPt.GetStr();
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						UserShowLog( p_InfoStrPt );
						break Out;
					}

					String p_InfoStrPt = "服务端线程：初始化本端Tcp协议服务端套接字[" + m_ThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_ThrdPt.m_LclNodePortPt.m_Val + "]成功。";
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
					UserShowLog( p_InfoStrPt );
				}
				else //如果初始化本端Tcp协议服务端套接字失败。
				{
					m_TcpSrvrSoktPt = null;

					String p_InfoStrPt = "服务端线程：初始化本端Tcp协议服务端套接字[" + LclNodeNameStrPt + ":" + LclNodeSrvcStrPt + "]失败。原因：" + m_ErrInfoVstrPt.GetStr();
					if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					UserShowLog( p_InfoStrPt );
					break Out;
				}
			}
			else //如果要使用高级Udp协议。
			{
				m_AudpSrvrSoktPt = new AudpSokt();

				if( m_AudpSrvrSoktPt.Init( m_LicnCodePt, p_LclNodeAddrFamly, LclNodeNameStrPt, LclNodeSrvcStrPt, ( short )1, ( short )5000, m_ErrInfoVstrPt ) == 0 ) //如果初始化本端高级Udp协议服务端套接字成功。
				{
					if( m_AudpSrvrSoktPt.SetSendBufSz( 1024 * 1024, m_ErrInfoVstrPt ) != 0 )
					{
						String p_InfoStrPt = "服务端线程：设置本端高级Udp协议服务端套接字的发送缓冲区大小失败。原因：" + m_ErrInfoVstrPt.GetStr();
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						UserShowLog( p_InfoStrPt );
						break Out;
					}

					if( m_AudpSrvrSoktPt.SetRecvBufSz( 1024 * 1024 * 3, m_ErrInfoVstrPt ) != 0 )
					{
						String p_InfoStrPt = "服务端线程：设置本端高级Udp协议服务端套接字的接收缓冲区大小失败。原因：" + m_ErrInfoVstrPt.GetStr();
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						UserShowLog( p_InfoStrPt );
						break Out;
					}

					if( m_AudpSrvrSoktPt.GetLclAddr( null, m_ThrdPt.m_LclNodeAddrPt, m_ThrdPt.m_LclNodePortPt, m_ErrInfoVstrPt ) != 0 ) //如果获取本端高级Udp协议套接字绑定的本地节点地址和端口失败。
					{
						String p_InfoStrPt = "服务端线程：获取本端高级Udp协议服务端套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVstrPt.GetStr();
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						UserShowLog( p_InfoStrPt );
						break Out;
					}

					String p_InfoStrPt = "服务端线程：初始化本端高级Udp协议服务端套接字[" + m_ThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_ThrdPt.m_LclNodePortPt.m_Val + "]成功。";
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
					UserShowLog( p_InfoStrPt );
				}
				else //如果初始化本端高级Udp协议服务端套接字失败。
				{
					String p_InfoStrPt = "服务端线程：初始化本端高级Udp协议服务端套接字[" + LclNodeNameStrPt + ":" + LclNodeSrvcStrPt + "]失败。原因：" + m_ErrInfoVstrPt.GetStr();
					if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					UserShowLog( p_InfoStrPt );
					break Out;
				}
			}

			m_MaxCnctNum = MaxCnctNum; //设置最大连接数。
			m_IsAutoRqirExit = IsAutoRqirExit; //设置是否自动请求退出。

			UserVibrate(); //调用用户定义的振动函数。

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			SrvrDstoy();
		}
		return p_Rslt;
	}

	//服务端销毁。
	private void SrvrDstoy()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		CnctInfo p_CnctInfoTmpPt;

		Out:
		{
			if( m_SrvrIsInit == 0 ) //如果服务端未初始化。
			{
				String p_InfoStrPt = "服务端线程：服务端未初始化，无法继续销毁。";
				if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				UserShowLog( p_InfoStrPt );
				break Out;
			}

			//连接信息全部销毁。
			for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
			{
				p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

				if( p_CnctInfoTmpPt.m_IsInit != 0 )
				{
					CnctInfoDstoy( p_CnctInfoTmpPt );
				}
			}

			//销毁本端Tcp协议服务端套接字。
			if( m_TcpSrvrSoktPt != null )
			{
				m_TcpSrvrSoktPt.Dstoy( null ); //关闭并销毁本端Tcp协议服务端套接字。
				m_TcpSrvrSoktPt = null;

				String p_InfoStrPt = "服务端线程：关闭并销毁本端Tcp协议服务端套接字成功。";
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
				UserShowLog( p_InfoStrPt );
			}

			//销毁本端高级Udp协议服务端套接字。
			if( m_AudpSrvrSoktPt != null )
			{
				m_AudpSrvrSoktPt.Dstoy( null ); //关闭并销毁本端高级Udp协议服务端套接字。
				m_AudpSrvrSoktPt = null;

				String p_InfoStrPt = "服务端线程：关闭并销毁本端高级Udp协议服务端套接字成功。";
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
				UserShowLog( p_InfoStrPt );
			}

			m_SrvrIsInit = 0; //设置服务端未初始化。

			UserSrvrDstoy(); //调用用户定义的服务端销毁函数。

			IsAutoRqirExit(); //判断是否自动请求退出。

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//连接信息初始化。
	public CnctInfo CnctInfoInit( int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt, TcpClntSokt TcpClntSoktPt, long AudpClntCnctIdx, int CurCnctSts )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		CnctInfo p_CnctInfoTmpPt = null;
		CnctInfo p_CnctInfoTmp2Pt;

		Out:
		{
			if( m_CnctInfoCntnrPt.size() >= m_MaxCnctNum ) //如果已达到最大连接数。
			{
				String p_InfoStrPt = "服务端线程：已达到最大连接数，无法进行连接信息初始化。";
				if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				UserShowLog( p_InfoStrPt );
				if( m_IsShowToast != 0 ) UserShowToast( p_InfoStrPt );
				break Out;
			}

			CnctInfoFindOut:
			{
				//查找是否有未初始化的连接信息。
				for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
				{
					p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

					if( p_CnctInfoTmpPt.m_IsInit == 0 ) break CnctInfoFindOut; //如果找到了未初始化的连接信息。
				}

				//如果没找到未初始化的连接信息。
				p_CnctInfoTmpPt = new CnctInfo();
				p_CnctInfoTmpPt.m_Idx = m_CnctInfoCntnrPt.size();
				m_CnctInfoCntnrPt.add( p_CnctInfoTmpPt ); //添加到连接信息容器。
			}
			p_CnctInfoTmpPt.m_IsInit = 1; //设置连接信息已初始化。
			p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_LastSendTickMsec = 0; //设置测试网络延迟包最后发送的嘀嗒钟为0，这样可以立即开始发送。
			p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_IsRecvRplyPkt = 1; //设置已接收测试网络延迟应答包，这样可以立即开始发送。
			m_CnctInfoCurMaxNum++; //递增连接信息的当前最大序号。
			p_CnctInfoTmpPt.m_Num = m_CnctInfoCurMaxNum; //设置序号。

			p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl = IsTcpOrAudpPrtcl; //设置协议为Tcp协议或高级Udp协议。
			p_CnctInfoTmpPt.m_RmtNodeNameStrPt = RmtNodeNameStrPt; //设置远端套接字绑定的远端节点名称字符串的指针。
			p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt = RmtNodeSrvcStrPt; //设置远端套接字绑定的远端节点服务字符串的指针。
			p_CnctInfoTmpPt.m_TcpClntSoktPt = TcpClntSoktPt; //设置本端Tcp协议客户端套接字的指针。
			p_CnctInfoTmpPt.m_AudpClntCnctIdx = AudpClntCnctIdx; //设置本端高级Udp协议客户端连接索引。
			p_CnctInfoTmpPt.m_IsRqstDstoy = 0; //设置是否请求销毁。

			p_CnctInfoTmpPt.m_CurCnctSts = CurCnctSts; //设置当前连接状态。
			p_CnctInfoTmpPt.m_RmtTkbkMode = TkbkMode.None; //设置远端对讲模式。

			p_CnctInfoTmpPt.m_IsRecvExitPkt = 0; //设置未接收退出包。

			UserCnctInit( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl, p_CnctInfoTmpPt.m_RmtNodeNameStrPt, p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt ); //调用用户定义的连接添加函数。

			//全部发送对讲索引包。
			{
				m_ThrdPt.m_TmpBytePt[ 0 ] = PktTyp.TkbkIdx; //设置对讲索引包。
				m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) p_CnctInfoTmpPt.m_Idx; //设置对讲索引。

				for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
				{
					p_CnctInfoTmp2Pt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

					if( ( p_CnctInfoTmp2Pt.m_IsInit != 0 ) && ( p_CnctInfoTmp2Pt.m_CurCnctSts == CnctSts.Cnct ) ) //如果连接信息已初始化，且当前连接状态为已连接。
					{
						if( ( ( p_CnctInfoTmp2Pt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmp2Pt.m_TcpClntSoktPt.SendApkt( m_ThrdPt.m_TmpBytePt, 2, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
							( ( p_CnctInfoTmp2Pt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.SendApkt( p_CnctInfoTmp2Pt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, 2, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
						{
							String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmp2Pt.m_Idx + "：发送对讲索引包成功。对讲索引：" + p_CnctInfoTmpPt.m_Idx + "。总长度：2。";
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
						}
						else
						{
							String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmp2Pt.m_Idx + "：发送对讲索引包失败。对讲索引：" + p_CnctInfoTmpPt.m_Idx + "。总长度：2。原因：" + m_ErrInfoVstrPt.GetStr();
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
						}
					}
				}
			}

			//补发之前的对讲索引包。
			{
				m_ThrdPt.m_TmpBytePt[ 0 ] = PktTyp.TkbkIdx; //设置对讲索引包。

				for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
				{
					p_CnctInfoTmp2Pt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

					if( ( p_CnctInfoTmp2Pt.m_IsInit != 0 ) && ( p_CnctInfoTmp2Pt.m_CurCnctSts == CnctSts.Cnct ) && ( p_CnctInfoTmp2Pt.m_Idx != p_CnctInfoTmpPt.m_Idx ) ) //如果连接信息已初始化，且当前连接状态为已连接，且不是本次连接信息。
					{
						m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) p_CnctInfoTmp2Pt.m_Idx; //设置对讲索引。

						if( ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmpPt.m_TcpClntSoktPt.SendApkt( m_ThrdPt.m_TmpBytePt, 2, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
							( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.SendApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, 2, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
						{
							String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送对讲索引包成功。对讲索引：" + p_CnctInfoTmp2Pt.m_Idx + "。总长度：2。";
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
						}
						else
						{
							String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送对讲索引包失败。对讲索引：" + p_CnctInfoTmp2Pt.m_Idx + "。总长度：2。原因：" + m_ErrInfoVstrPt.GetStr();
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
						}
					}
				}
			}

			//补发之前的对讲模式包。
			{
				m_ThrdPt.m_TmpBytePt[ 0 ] = PktTyp.TkbkMode; //设置对讲模式包。

				for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
				{
					p_CnctInfoTmp2Pt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

					if( ( p_CnctInfoTmp2Pt.m_IsInit != 0 ) && ( p_CnctInfoTmp2Pt.m_CurCnctSts == CnctSts.Cnct ) && ( p_CnctInfoTmp2Pt.m_Idx != p_CnctInfoTmpPt.m_Idx ) ) //如果连接信息已初始化，且当前连接状态为已连接。
					{
						m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) p_CnctInfoTmp2Pt.m_Idx; //设置对讲索引。
						m_ThrdPt.m_TmpBytePt[ 2 ] = ( byte ) p_CnctInfoTmp2Pt.m_RmtTkbkMode; //设置对讲模式。
						if( ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmpPt.m_TcpClntSoktPt.SendApkt( m_ThrdPt.m_TmpBytePt, 3, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
							( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.SendApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, 3, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
						{
							String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送对讲模式包成功。对讲索引：" + p_CnctInfoTmp2Pt.m_Idx + "。对讲模式：" + m_TkbkModeStrArrPt[ p_CnctInfoTmp2Pt.m_RmtTkbkMode ] + "。总长度：3。";
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
						}
						else
						{
							String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送对讲模式包失败。对讲索引：" + p_CnctInfoTmp2Pt.m_Idx + "。对讲模式：" + m_TkbkModeStrArrPt[ p_CnctInfoTmp2Pt.m_RmtTkbkMode ] + "。总长度：3。原因：" + m_ErrInfoVstrPt.GetStr();
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
						}
					}
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			if( p_CnctInfoTmpPt != null )
			{
				p_CnctInfoTmpPt.m_IsInit = 0; //设置连接信息未初始化。
			}
		}
		return p_CnctInfoTmpPt;
	}

	//连接信息销毁。
	public void CnctInfoDstoy( CnctInfo CnctInfoPt )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		CnctInfo p_CnctInfoTmpPt;

		Out:
		{
			if( CnctInfoPt.m_IsInit != 0 )
			{
				//全部发送退出包。
				m_ThrdPt.m_TmpBytePt[ 0 ] = PktTyp.Exit; //设置退出包。
				for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
				{
					p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

					if( ( p_CnctInfoTmpPt.m_IsInit != 0 ) && ( p_CnctInfoTmpPt.m_IsRecvExitPkt == 0 ) && ( p_CnctInfoTmpPt.m_CurCnctSts == CnctSts.Cnct ) ) //如果连接信息已初始化，且未接收退出包，且当前连接状态为已连接。
					{
						m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) CnctInfoPt.m_Idx; //设置对讲索引。
						if( ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmpPt.m_TcpClntSoktPt.SendApkt( m_ThrdPt.m_TmpBytePt, 2, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
							( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.SendApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, 2, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
						{
							String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送退出包成功。对讲索引：" + CnctInfoPt.m_Idx + "。总长度：2。";
							if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
						}
						else
						{
							String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送退出包失败。对讲索引：" + CnctInfoPt.m_Idx + "。总长度：2。原因：" + m_ErrInfoVstrPt.GetStr();
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
						}
					}
				}

				//销毁本端Tcp协议客户端套接字。
				if( CnctInfoPt.m_TcpClntSoktPt != null )
				{
					CnctInfoPt.m_TcpClntSoktPt.Dstoy( ( short ) -1, null );
					CnctInfoPt.m_TcpClntSoktPt = null;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + CnctInfoPt.m_Idx + "：销毁本端Tcp协议客户端套接字成功。" );
				}

				//销毁本端高级Udp协议客户端连接。
				if( CnctInfoPt.m_AudpClntCnctIdx != -1 )
				{
					m_AudpSrvrSoktPt.ClosCnct( CnctInfoPt.m_AudpClntCnctIdx, null );
					CnctInfoPt.m_AudpClntCnctIdx = -1;
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + CnctInfoPt.m_Idx + "：销毁本端高级Udp协议客户端连接成功。" );
				}

				//设置当前连接状态。
				if( CnctInfoPt.m_CurCnctSts <= CnctSts.Wait )
				{
					CnctInfoPt.m_CurCnctSts = CnctSts.Tmot;
					UserCnctSts( CnctInfoPt, CnctInfoPt.m_CurCnctSts ); //调用用户定义的连接状态函数。
				}
				else if( CnctInfoPt.m_CurCnctSts == CnctSts.Cnct )
				{
					CnctInfoPt.m_CurCnctSts = CnctSts.Dsct;
					UserCnctSts( CnctInfoPt, CnctInfoPt.m_CurCnctSts ); //调用用户定义的连接状态函数。
				}

				UserCnctDstoy( CnctInfoPt ); //调用用户定义的连接销毁函数。
				CnctInfoPt.m_IsInit = 0; //设置连接信息未初始化。

				//递减后面连接信息的序号。
				for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
				{
					p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

					if( ( p_CnctInfoTmpPt.m_IsInit != 0 ) && ( p_CnctInfoTmpPt.m_Num > CnctInfoPt.m_Num ) )
					{
						p_CnctInfoTmpPt.m_Num--; //设置后面连接信息的序号全部递减1。
					}
				}
				m_CnctInfoCurMaxNum--; //递减连接信息的当前最大序号。

				String p_InfoStrPt = "服务端线程：连接" + CnctInfoPt.m_Idx + "：已销毁。";
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
				UserShowLog( p_InfoStrPt );

				IsAutoRqirExit(); //判断是否自动请求退出。
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//判断是否自动请求退出。
	public void IsAutoRqirExit()
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_IsAutoRqirExit == 0 )
			{

			}
			else if( m_IsAutoRqirExit == 1 )
			{
				if( m_CnctInfoCurMaxNum == -1 )
				{
					SendRqirExitMsg( 0 );

					String p_InfoStrPt = "服务端线程：所有连接已销毁，自动请求退出。";
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
					UserShowLog( p_InfoStrPt );
				}
			}
			else if( m_IsAutoRqirExit == 2 )
			{
				if( ( m_CnctInfoCurMaxNum == -1 ) && ( m_SrvrIsInit == 0 ) )
				{
					SendRqirExitMsg( 0 );

					String p_InfoStrPt = "服务端线程：所有连接和服务端已销毁，自动请求退出。";
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
					UserShowLog( p_InfoStrPt );
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//连接处理，包括接受连接、接收发送数据包、删除连接。
	public void CnctPocs()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		CnctInfo p_CnctInfoTmpPt;
		CnctInfo p_CnctInfoTmp2Pt;
		int p_TmpInt;

		Out:
		{
			//调用用户定义的处理函数。
			{
				UserPocs();
				if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：调用用户定义的处理函数成功。" );
			}

			//用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字的连接。
			if( m_TcpSrvrSoktPt != null )
			{
				int p_PocsRslt = -1; //存放本处理段执行结果，为0表示成功，为非0表示失败。
				TcpClntSokt p_TcpClntSoktTmpPt = null;

				TcpSrvrSoktAcptOut:
				{
					while( true )
					{
						p_TcpClntSoktTmpPt = new TcpClntSokt();
						if( m_TcpSrvrSoktPt.Acpt( p_TcpClntSoktTmpPt, null, m_ThrdPt.m_RmtNodeAddrPt, m_ThrdPt.m_RmtNodePortPt, ( short ) 0, 0, m_ErrInfoVstrPt ) == 0 )
						{
							if( p_TcpClntSoktTmpPt.m_TcpClntSoktPt != 0 ) //如果用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字的连接成功。
							{
								if( p_TcpClntSoktTmpPt.SetNoDelay( 1, 0, m_ErrInfoVstrPt ) != 0 ) //如果设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。
								{
									String p_InfoStrPt = "服务端线程：设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_ErrInfoVstrPt.GetStr();
									if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
									UserShowLog( p_InfoStrPt );
									break TcpSrvrSoktAcptOut;
								}

								if( p_TcpClntSoktTmpPt.SetSendBufSz( 1024 * 1024, 0, m_ErrInfoVstrPt ) != 0 )
								{
									String p_InfoStrPt = "服务端线程：设置本端Tcp协议客户端套接字的发送缓冲区大小失败。原因：" + m_ErrInfoVstrPt.GetStr();
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
									UserShowLog( p_InfoStrPt );
									break TcpSrvrSoktAcptOut;
								}

								if( p_TcpClntSoktTmpPt.SetRecvBufSz( 1024 * 1024 * 3, 0, m_ErrInfoVstrPt ) != 0 )
								{
									String p_InfoStrPt = "服务端线程：设置本端Tcp协议客户端套接字的接收缓冲区大小失败。原因：" + m_ErrInfoVstrPt.GetStr();
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
									UserShowLog( p_InfoStrPt );
									break TcpSrvrSoktAcptOut;
								}

								if( p_TcpClntSoktTmpPt.SetKeepAlive( 1, 1, 1, 5, 0, m_ErrInfoVstrPt ) != 0 )
								{
									String p_InfoStrPt = "服务端线程：设置本端Tcp协议客户端套接字的保活机制失败。原因：" + m_ErrInfoVstrPt.GetStr();
									if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
									UserShowLog( p_InfoStrPt );
									break TcpSrvrSoktAcptOut;
								}

								if( ( p_CnctInfoTmpPt = CnctInfoInit( 0, m_ThrdPt.m_RmtNodeAddrPt.m_Val, m_ThrdPt.m_RmtNodePortPt.m_Val, p_TcpClntSoktTmpPt, -1, CnctSts.Cnct ) ) == null ) break TcpSrvrSoktAcptOut; //如果连接信息初始化失败。
								UserCnctSts( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_CurCnctSts ); //调用用户定义的连接状态函数。
								UserCnctRmtTkbkMode( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_RmtTkbkMode, p_CnctInfoTmpPt.m_RmtTkbkMode ); //调用用户定义的连接远端对讲模式函数。

								String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字[" + m_ThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_ThrdPt.m_RmtNodePortPt.m_Val + "]的连接成功。";
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
								UserShowLog( p_InfoStrPt );
								break;
							}
							else //如果用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字的连接超时，就跳出接受。
							{
								break TcpSrvrSoktAcptOut;
							}
						}
						else
						{
							p_TcpClntSoktTmpPt = null;

							String p_InfoStrPt = "服务端线程：用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字的连接失败。原因：" + m_ErrInfoVstrPt.GetStr();
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
							break TcpSrvrSoktAcptOut;
						}
					}

					p_PocsRslt = 0; //设置本处理段执行成功。
				}

				if( p_PocsRslt != 0 ) //如果本处理段执行失败。
				{
					if( p_TcpClntSoktTmpPt != null )
					{
						m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) PktTyp.Exit; //设置退出包。
						p_TcpClntSoktTmpPt.SendApkt( m_ThrdPt.m_TmpBytePt, 1, ( short ) 0, 1, 0, null ); //发送退出包。
						p_TcpClntSoktTmpPt.Dstoy( ( short ) -1, null );
					}
				}
			}

			//用本端高级Udp协议服务端套接字接受远端高级Udp协议客户端套接字的连接。
			if( m_AudpSrvrSoktPt != null )
			{
				int p_PocsRslt = -1; //存放本处理段的执行结果，为0表示成功，为非0表示失败。
				m_ThrdPt.m_TmpHTLongPt.m_Val = -1;

				AudpSrvrSoktAcptOut:
				{
					while( true ) //循环接受远端高级Udp协议套接字的连接。
					{
						if( m_AudpSrvrSoktPt.Acpt( m_ThrdPt.m_TmpHTLongPt, null, m_ThrdPt.m_RmtNodeAddrPt, m_ThrdPt.m_RmtNodePortPt, ( short ) 0, m_ErrInfoVstrPt ) == 0 )
						{
							if( m_ThrdPt.m_TmpHTLongPt.m_Val != -1 ) //如果用本端高级Udp协议套接字接受远端高级Udp协议套接字的连接成功。
							{
								if( ( p_CnctInfoTmpPt = CnctInfoInit( 1,  m_ThrdPt.m_RmtNodeAddrPt.m_Val, m_ThrdPt.m_RmtNodePortPt.m_Val, null, m_ThrdPt.m_TmpHTLongPt.m_Val, CnctSts.Cnct ) ) == null ) break AudpSrvrSoktAcptOut; //如果连接信息初始化失败。
								UserCnctSts( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_CurCnctSts ); //调用用户定义的连接状态函数。
								UserCnctRmtTkbkMode( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_RmtTkbkMode, p_CnctInfoTmpPt.m_RmtTkbkMode ); //调用用户定义的连接远端对讲模式函数。

								String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：用本端高级Udp协议服务端套接字接受远端高级Udp协议客户端套接字[" + m_ThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_ThrdPt.m_RmtNodePortPt.m_Val + "]的连接成功。";
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
								UserShowLog( p_InfoStrPt );
								break;
							}
							else //如果用本端高级Udp协议服务端套接字接受远端高级Udp协议客户端套接字的连接超时，就跳出接受。
							{
								break AudpSrvrSoktAcptOut;
							}
						}
						else
						{
							String p_InfoStrPt = "服务端线程：用本端高级Udp协议服务端套接字接受远端高级Udp协议客户端套接字的连接失败。原因：" + m_ErrInfoVstrPt.GetStr();
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
							break AudpSrvrSoktAcptOut;
						}
					}

					p_PocsRslt = 0; //设置本处理段执行成功。
				}

				if( p_PocsRslt != 0 ) //如果本处理段执行失败。
				{
					if( m_ThrdPt.m_TmpHTLongPt.m_Val != -1 )
					{
						m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) PktTyp.Exit; //设置退出包。
						m_AudpSrvrSoktPt.SendApkt( m_ThrdPt.m_TmpHTLongPt.m_Val, m_ThrdPt.m_TmpBytePt, 1, 1, 1, null ); //发送退出包。
						m_AudpSrvrSoktPt.ClosCnct( m_ThrdPt.m_TmpHTLongPt.m_Val, null );
					}
				}
			}

			//遍历连接信息容器。
			for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
			{
				p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

				if( p_CnctInfoTmpPt.m_IsInit != 0 )
				{
					//用本端套接字接收连接的远端套接字发送的数据包。
					if( ( p_CnctInfoTmpPt.m_IsRqstDstoy == 0 ) && ( p_CnctInfoTmpPt.m_CurCnctSts == CnctSts.Cnct ) ) //如果该连接未请求销毁，且当前连接状态为已连接。
					{
						RecvPktOut:
						{
							if( ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmpPt.m_TcpClntSoktPt.RecvApkt( m_ThrdPt.m_TmpBytePt, m_ThrdPt.m_TmpBytePt.length, m_ThrdPt.m_TmpHTLongPt, ( short ) 0, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
								( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.RecvApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, m_ThrdPt.m_TmpBytePt.length, m_ThrdPt.m_TmpHTLongPt, m_ThrdPt.m_TmpHTIntPt, ( short ) 0, m_ErrInfoVstrPt ) == 0 ) ) )
							{
								if( m_ThrdPt.m_TmpHTLongPt.m_Val != -1 ) //如果用本端套接字接收连接的远端套接字发送的数据包成功。
								{
									if( m_ThrdPt.m_TmpHTLongPt.m_Val == 0 ) //如果数据包的长度为0。
									{
										if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收数据包。长度为" + m_ThrdPt.m_TmpHTLongPt.m_Val + "，表示没有数据，无法继续接收。" );
										break RecvPktOut;
									}
									else if( m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) PktTyp.TkbkMode ) //如果是对讲模式包。
									{
										if( m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 + 1 ) //如果对讲模式包的长度小于1 + 1 + 1，表示没有对讲索引和对讲模式。
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收对讲模式包。长度为" + m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1 + 1，表示没有对讲索引和对讲模式，无法继续接收。" );
											break RecvPktOut;
										}
										if( m_ThrdPt.m_TmpBytePt[ 1 ] != p_CnctInfoTmpPt.m_Idx )
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收对讲模式包。对讲索引为" + m_ThrdPt.m_TmpBytePt[ 1 ] + "与发送端的对讲索引" + p_CnctInfoTmpPt.m_Idx + "不一致，无法继续接收。" );
											break RecvPktOut;
										}
										if( ( m_ThrdPt.m_TmpBytePt[ 2 ] < ( byte ) TkbkMode.None ) || ( m_ThrdPt.m_TmpBytePt[ 2 ] >= ( byte ) TkbkMode.NoChg ) )
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收对讲模式包。对讲模式为" + m_ThrdPt.m_TmpBytePt[ 2 ] + "不正确，无法继续接收。" );
											break RecvPktOut;
										}

										int p_OldRmtTkbkMode = p_CnctInfoTmpPt.m_RmtTkbkMode; //设置旧远端对讲模式。
										p_CnctInfoTmpPt.m_RmtTkbkMode = m_ThrdPt.m_TmpBytePt[ 2 ]; //设置远端对讲模式。
										UserCnctRmtTkbkMode( p_CnctInfoTmpPt, p_OldRmtTkbkMode, p_CnctInfoTmpPt.m_RmtTkbkMode ); //调用用户定义的连接远端对讲模式函数。
										if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收对讲模式包。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。对讲模式：" + m_TkbkModeStrArrPt[ p_CnctInfoTmpPt.m_RmtTkbkMode ] + "。" );

										//全部发送对讲模式包。
										for( int p_CnctInfoLstIdx2 = 0; p_CnctInfoLstIdx2 < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx2++ )
										{
											p_CnctInfoTmp2Pt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx2 );

											if( ( p_CnctInfoTmp2Pt.m_IsInit != 0 ) && ( p_CnctInfoTmp2Pt.m_CurCnctSts == CnctSts.Cnct ) && ( p_CnctInfoTmp2Pt.m_Idx != p_CnctInfoTmpPt.m_Idx ) ) //如果连接信息已初始化，且当前连接状态为已连接，且不是发送端的连接信息。
											{
												if( ( ( p_CnctInfoTmp2Pt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmp2Pt.m_TcpClntSoktPt.SendApkt( m_ThrdPt.m_TmpBytePt, m_ThrdPt.m_TmpHTLongPt.m_Val, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
													( ( p_CnctInfoTmp2Pt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.SendApkt( p_CnctInfoTmp2Pt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, m_ThrdPt.m_TmpHTLongPt.m_Val, 1, m_ThrdPt.m_TmpHTIntPt.m_Val, m_ErrInfoVstrPt ) == 0 ) ) )
												{
													if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmp2Pt.m_Idx + "：发送对讲模式包成功。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。对讲模式：" + m_TkbkModeStrArrPt[ p_CnctInfoTmpPt.m_RmtTkbkMode ] + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
												}
												else
												{
													if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmp2Pt.m_Idx + "：发送对讲模式包失败。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。对讲模式：" + m_TkbkModeStrArrPt[ p_CnctInfoTmpPt.m_RmtTkbkMode ] + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。原因：" + m_ErrInfoVstrPt.GetStr() );
												}
											}
										}
									}
									else if( m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) PktTyp.AdoFrm ) //如果是音频输出帧包。
									{
										if( m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 + 4 ) //如果音频输出帧包的长度小于1 + 1 + 4，表示没有对讲索引和音频输出帧时间戳。
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收音频输出帧包。长度为" + m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1 + 4，表示没有对讲索引和音频输出帧时间戳，无法继续接收。" );
											break RecvPktOut;
										}
										if( m_ThrdPt.m_TmpBytePt[ 1 ] != p_CnctInfoTmpPt.m_Idx )
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收音频输出帧包。对讲索引为" + m_ThrdPt.m_TmpBytePt[ 1 ] + "与发送端的对讲索引" + p_CnctInfoTmpPt.m_Idx + "不一致，无法继续接收。" );
											break RecvPktOut;
										}

										//读取音频输出帧时间戳。
										p_TmpInt = ( m_ThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) + ( ( m_ThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 8 ) + ( ( m_ThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 16 ) + ( ( m_ThrdPt.m_TmpBytePt[ 5 ] & 0xFF ) << 24 );

										if( m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该音频输出帧为有语音活动。
										{
											if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收有语音活动的音频输出帧包。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
										}
										else //如果该音频输出帧为无语音活动。
										{
											if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收无语音活动的音频输出帧包。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
										}

										//全部发送音频输出帧包。
										if( ( p_CnctInfoTmpPt.m_RmtTkbkMode & TkbkMode.AdoInpt ) != 0 ) //如果远端对讲模式有音频输入。
										{
											for( int p_CnctInfoLstIdx2 = 0; p_CnctInfoLstIdx2 < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx2++ )
											{
												p_CnctInfoTmp2Pt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx2 );

												if( ( p_CnctInfoTmp2Pt.m_IsInit != 0 ) && ( p_CnctInfoTmp2Pt.m_CurCnctSts == CnctSts.Cnct ) && ( ( p_CnctInfoTmp2Pt.m_RmtTkbkMode & TkbkMode.AdoOtpt ) != 0 ) && ( p_CnctInfoTmp2Pt.m_Idx != p_CnctInfoTmpPt.m_Idx ) ) //如果连接信息已初始化，且当前连接状态为已连接，且对讲模式有音频输出，且不是发送端的连接信息。
												{
													if( ( ( p_CnctInfoTmp2Pt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmp2Pt.m_TcpClntSoktPt.SendApkt( m_ThrdPt.m_TmpBytePt, m_ThrdPt.m_TmpHTLongPt.m_Val, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
														( ( p_CnctInfoTmp2Pt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.SendApkt( p_CnctInfoTmp2Pt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, m_ThrdPt.m_TmpHTLongPt.m_Val, 1, m_ThrdPt.m_TmpHTIntPt.m_Val, m_ErrInfoVstrPt ) == 0 ) ) )
													{
														if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmp2Pt.m_Idx + "：发送音频输出帧包成功。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
													}
													else
													{
														if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmp2Pt.m_Idx + "：发送音频输出帧包失败。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。原因：" + m_ErrInfoVstrPt.GetStr() );
													}
												}
											}
										}
										else //如果远端对讲模式无音频输入。
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：远端对讲模式无音频输入，不进行全部发送音频输出帧包。" );
										}
									}
									else if( m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) PktTyp.VdoFrm ) //如果是视频输出帧包。
									{
										if( m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 + 4 ) //如果视频输出帧包的长度小于1 + 1 + 4，表示没有对讲索引和视频输出帧时间戳。
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收视频输出帧包。长度为" + m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1 + 4，表示没有对讲索引和视频输出帧时间戳，无法继续接收。" );
											break RecvPktOut;
										}
										if( m_ThrdPt.m_TmpBytePt[ 1 ] != p_CnctInfoTmpPt.m_Idx )
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收音频输出帧包。对讲索引为" + m_ThrdPt.m_TmpBytePt[ 1 ] + "与发送端的对讲索引" + p_CnctInfoTmpPt.m_Idx + "不一致，无法继续接收。" );
											break RecvPktOut;
										}

										//读取视频输出帧时间戳。
										p_TmpInt = ( m_ThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) + ( ( m_ThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 8 ) + ( ( m_ThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 16 ) + ( ( m_ThrdPt.m_TmpBytePt[ 5 ] & 0xFF ) << 24 );

										if( m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该视频输出帧为有图像活动。
										{
											if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收有图像活动的视频输出帧包。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。类型：" + ( m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。" );
										}
										else //如果该视频输出帧为无图像活动。
										{
											if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收无图像活动的视频输出帧包。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
										}

										//全部发送视频输出帧包。
										if( ( p_CnctInfoTmpPt.m_RmtTkbkMode & TkbkMode.VdoInpt ) != 0 ) //如果远端对讲模式有视频输入。
										{
											for( int p_CnctInfoLstIdx2 = 0; p_CnctInfoLstIdx2 < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx2++ )
											{
												p_CnctInfoTmp2Pt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx2 );

												if( ( p_CnctInfoTmp2Pt.m_IsInit != 0 ) && ( p_CnctInfoTmp2Pt.m_CurCnctSts == CnctSts.Cnct ) && ( ( p_CnctInfoTmp2Pt.m_RmtTkbkMode & TkbkMode.VdoOtpt ) != 0 ) && ( p_CnctInfoTmp2Pt.m_Idx != p_CnctInfoTmpPt.m_Idx ) ) //如果连接信息已初始化，且当前连接状态为已连接，且对讲模式有视频输出，且不是发送端的连接信息。
												{
													if( ( ( p_CnctInfoTmp2Pt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmp2Pt.m_TcpClntSoktPt.SendApkt( m_ThrdPt.m_TmpBytePt, m_ThrdPt.m_TmpHTLongPt.m_Val, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
														( ( p_CnctInfoTmp2Pt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.SendApkt( p_CnctInfoTmp2Pt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, m_ThrdPt.m_TmpHTLongPt.m_Val, 1, m_ThrdPt.m_TmpHTIntPt.m_Val, m_ErrInfoVstrPt ) == 0 ) ) )
													{
														if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmp2Pt.m_Idx + "：发送视频输出帧包成功。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
													}
													else
													{
														if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmp2Pt.m_Idx + "：发送视频输出帧包失败。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。原因：" + m_ErrInfoVstrPt.GetStr() );
													}
												}
											}
										}
										else //如果远端对讲模式无视频输入。
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：远端对讲模式无视频输入，不进行全部发送视频输出帧包。" );
										}
									}
									else if( m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) PktTyp.TstNtwkDly ) //如果是测试网络延迟包。
									{
										if( m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果测试网络延迟包的长度小于1 + 1，表示没有对讲索引。
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收测试网络延迟包。长度为" + m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲索引，无法继续接收。" );
											break RecvPktOut;
										}
										if( m_ThrdPt.m_TmpBytePt[ 1 ] != p_CnctInfoTmpPt.m_Idx )
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收测试网络延迟包。对讲索引为" + m_ThrdPt.m_TmpBytePt[ 1 ] + "与发送端的对讲索引" + p_CnctInfoTmpPt.m_Idx + "不一致，无法继续接收。" );
											break RecvPktOut;
										}

										if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收测试网络延迟包。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );

										m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) PktTyp.TstNtwkDlyRply; //设置测试网络延迟应答包。
										if( ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmpPt.m_TcpClntSoktPt.SendApkt( m_ThrdPt.m_TmpBytePt, 2, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
											( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.SendApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, 2, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
										{
											if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送测试网络延迟应答包成功。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。总长度：2。" );
										}
										else
										{
											String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送测试网络延迟应答包失败。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。总长度：2。原因：" + m_ErrInfoVstrPt.GetStr();
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
											UserShowLog( p_InfoStrPt );
										}
									}
									else if( m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) PktTyp.TstNtwkDlyRply ) //如果是测试网络延迟应答包。
									{
										if( m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果测试网络延迟应答包的长度小于1 + 1，表示没有对讲索引。
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收测试网络延迟应答包。长度为" + m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲索引，无法继续接收。" );
											break RecvPktOut;
										}
										if( m_ThrdPt.m_TmpBytePt[ 1 ] != p_CnctInfoTmpPt.m_Idx )
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收测试网络延迟应答包。对讲索引为" + m_ThrdPt.m_TmpBytePt[ 1 ] + "与发送端的对讲索引" + p_CnctInfoTmpPt.m_Idx + "不一致，无法继续接收。" );
											break RecvPktOut;
										}

										long p_NtwkDlyMsec = SystemClock.uptimeMillis() - p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_LastSendTickMsec; //存放网络延迟，单位为毫秒。

										if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收测试网络延迟应答包。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。延迟：" + p_NtwkDlyMsec + "。总长度：" + m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );

										if( m_TstNtwkDlyPt.m_IsTstNtwkDly != 0 ) //如果要测试网络延迟。
										{
											p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_IsRecvRplyPkt = 1; //设置已接收测试网络延迟应答包。
											UserCnctTstNtwkDly( p_CnctInfoTmpPt, p_NtwkDlyMsec ); //调用用户定义的连接测试网络延迟函数。
										}
									}
									else if( m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) PktTyp.Exit ) //如果是退出包。
									{
										if( m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果退出包的长度小于1 + 1，表示没有对讲索引。
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收退出包。长度为" + m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲索引，无法继续接收。" );
											break RecvPktOut;
										}
										if( m_ThrdPt.m_TmpBytePt[ 1 ] != p_CnctInfoTmpPt.m_Idx )
										{
											if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收退出包。对讲索引为" + m_ThrdPt.m_TmpBytePt[ 1 ] + "与发送端的对讲索引" + p_CnctInfoTmpPt.m_Idx + "不一致，无法继续接收。" );
											break RecvPktOut;
										}

										p_CnctInfoTmpPt.m_IsRecvExitPkt = 1; //设置已接收退出包。
										p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。

										String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：接收退出包。对讲索引：" + m_ThrdPt.m_TmpBytePt[ 1 ] + "。";
										if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
										UserShowLog( p_InfoStrPt );
										if( m_IsShowToast != 0 ) UserShowToast( p_InfoStrPt );
									}
								} //如果用本端套接字接收连接的远端套接字发送的数据包超时，就重新接收。
							}
							else //如果用本端套接字接收连接的远端套接字发送的数据包失败。
							{
								p_CnctInfoTmpPt.m_CurCnctSts = CnctSts.Tmot; //设置当前连接状态为异常断开。
								UserCnctSts( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_CurCnctSts ); //调用用户定义的连接状态函数。
								p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。

								String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：用本端套接字接收连接的远端套接字发送的数据包失败。原因：" + m_ErrInfoVstrPt.GetStr();
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
								UserShowLog( p_InfoStrPt );
								break RecvPktOut;
							}
						}
					}

					//用本端客户端套接字测试网络延迟。
					if( ( m_TstNtwkDlyPt.m_IsTstNtwkDly != 0 ) && ( p_CnctInfoTmpPt.m_IsRqstDstoy == 0 ) && ( p_CnctInfoTmpPt.m_CurCnctSts == CnctSts.Cnct ) ) //如果要测试网络延迟，且连接未请求销毁，且当前连接状态为已连接。
					{
						long p_CurTickMsec = SystemClock.uptimeMillis(); //存放当前嘀嗒钟，单位为毫秒。

						if( ( p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_IsRecvRplyPkt != 0 ) && ( p_CurTickMsec - p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_LastSendTickMsec >= m_TstNtwkDlyPt.m_SendIntvlMsec ) ) //如果已接收测试网络延迟应答包，且最后发送测试网络延迟包已超过间隔时间。
						{
							m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) PktTyp.TstNtwkDly; //设置数据包类型为测试网络延迟包。
							m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) p_CnctInfoTmpPt.m_Idx; //设置对讲索引。
							if( ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmpPt.m_TcpClntSoktPt.SendApkt( m_ThrdPt.m_TmpBytePt, 2, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
								( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_AudpSrvrSoktPt.SendApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_ThrdPt.m_TmpBytePt, 2, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
							{
								if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送测试网络延迟包成功。对讲索引：" + p_CnctInfoTmpPt.m_Idx + "。总长度：2。" );

								p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_LastSendTickMsec = p_CurTickMsec; //设置测试网络延迟包最后发送的嘀嗒钟。
								p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_IsRecvRplyPkt = 0; //设置未接收测试网络延迟应答包。
							}
							else
							{
								if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：发送测试网络延迟包成功。对讲索引：" + p_CnctInfoTmpPt.m_Idx + "。总长度：2。原因：" + m_ErrInfoVstrPt.GetStr() );
							}
						}
					}

					//销毁连接信息。
					if( p_CnctInfoTmpPt.m_IsRqstDstoy == 1 ) //如果该连接已请求销毁。
					{
						CnctInfoDstoy( p_CnctInfoTmpPt );
					}
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//线程消息处理。
	private int ThrdMsgPocs( int MsgTyp, Object[] MsgArgPt )
	{
		int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。
		int p_TmpInt32;
		CnctInfo p_CnctInfoTmpPt = null;

		Out:
		{
			switch( MsgTyp )
			{
				case ThrdMsgTyp.SetIsUseWakeLock:
				{
					m_IsUseWakeLock = ( Integer ) MsgArgPt[ 0 ];
					WakeLockInitOrDstoy( m_IsUseWakeLock ); //重新初始化唤醒锁。
					break;
				}
				case ThrdMsgTyp.SetIsTstNtwkDly:
				{
					m_TstNtwkDlyPt.m_IsTstNtwkDly = ( int ) MsgArgPt[ 0 ]; //设置是否测试网络延迟。
					m_TstNtwkDlyPt.m_SendIntvlMsec = ( long ) MsgArgPt[ 1 ]; //设置测试网络延迟包的发送间隔。

					for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
					{
						p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

						if( p_CnctInfoTmpPt.m_IsInit != 0 )
						{
							p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_LastSendTickMsec = 0; //设置测试网络延迟包最后发送的嘀嗒钟为0，这样可以立即开始发送。
							p_CnctInfoTmpPt.m_TstNtwkDlyPt.m_IsRecvRplyPkt = 1; //设置已接收测试网络延迟应答包，这样可以立即开始发送。
						}
					}
					break;
				}
				case ThrdMsgTyp.SrvrInit:
				{
					p_Rslt = SrvrInit( ( Integer ) MsgArgPt[ 0 ], ( String ) MsgArgPt[ 1 ], ( String ) MsgArgPt[ 2 ], ( Integer ) MsgArgPt[ 3 ], ( Integer ) MsgArgPt[ 4 ] );
					break Out;
				}
				case ThrdMsgTyp.SrvrDstoy:
				{
					SrvrDstoy();
					break;
				}
				case ThrdMsgTyp.CnctDstoy:
				{
					int p_CnctNum = ( int ) MsgArgPt[ 0 ];

					OutCnctDstoy:
					{
						if( ( p_CnctNum > m_CnctInfoCurMaxNum ) || ( p_CnctNum < 0 ) )
						{
							String p_InfoStrPt = "服务端线程：没有序号为" + p_CnctNum + "]的连接，无法删除。";
							if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							UserShowLog( p_InfoStrPt );
							break OutCnctDstoy;
						}

						for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
						{
							p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

							if( ( p_CnctInfoTmpPt.m_IsInit != 0 ) && ( p_CnctInfoTmpPt.m_Num == p_CnctNum ) )
							{
								p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。
								break;
							}
						}

						String p_InfoStrPt = "服务端线程：连接" + p_CnctInfoTmpPt.m_Idx + "：请求销毁远端节点" + ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]的连接。";
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
						UserShowLog( p_InfoStrPt );

						p_Rslt = 0; //设置本函数执行成功。
					}

					if( p_Rslt != 0 ) //如果本函数执行失败。
					{

					}
					break;
				}
				case ThrdMsgTyp.RqirExit:
				{
					if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：接收退出请求。" );
					m_IsRqirExit = 1; //设置已请求退出。
					if( m_SrvrIsInit != 0 ) SrvrDstoy(); //服务端销毁。
					break;
				}
				default: //用户消息。
				{
					p_TmpInt32 = UserMsg( MsgTyp - ThrdMsgTyp.UserMsgMinVal, MsgArgPt );
					if( p_TmpInt32 == 0 )
					{
						if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：调用用户定义的消息函数成功。返回值：" + p_TmpInt32 );
					}
					else
					{
						if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "服务端线程：调用用户定义的消息函数失败。返回值：" + p_TmpInt32 );
						break Out;
					}
					break;
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return p_Rslt;
	}

	//本线程执行函数。
	public void run()
	{
		long p_LastTickMsec = 0;

		if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：本地代码的指令集名称（CPU类型+ ABI约定）：" + android.os.Build.CPU_ABI + "，设备型号：" + android.os.Build.MODEL + "。" );

		UserInit(); //调用用户定义的初始化函数。

		//媒体处理循环开始。
		while( true )
		{
			while( m_ThrdMsgQueuePt.MsgPocsThrdMsgPocs() == 0 ); //进行线程消息处理，直到线程消息队列为空。
			if( m_IsRqirExit != 0 ) break; //如果已请求退出，就表示本线程需要退出。

			if( m_IsPrintLogcat != 0 ) p_LastTickMsec = SystemClock.uptimeMillis();

			CnctPocs(); //连接处理。

			if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：连接处理全部完毕，耗时 " + ( SystemClock.uptimeMillis() - p_LastTickMsec ) + " 毫秒。" );

			SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
		} //媒体处理循环结束。

		UserDstoy(); //调用用户定义的销毁函数。

		WakeLockInitOrDstoy( 0 ); //销毁唤醒锁。

		if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "服务端线程：本线程已退出。" );
	}
}
