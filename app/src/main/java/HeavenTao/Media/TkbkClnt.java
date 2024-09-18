package HeavenTao.Media;

import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import HeavenTao.Ado.*;
import HeavenTao.Vdo.*;
import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

public class TkbkClnt //对讲客户端。
{
	public static String m_CurClsNameStrPt = "TkbkClnt"; //存放当前类名称字符串。

	public ClntMediaPocsThrd m_ClntMediaPocsThrdPt; //存放客户端媒体处理线程的指针。

	public class TstNtwkDly //存放测试网络延迟。
	{
		public int m_IsTstNtwkDly; //存放是否测试网络延迟。
		public long m_SendIntvlMsec; //存放发送间隔，单位为毫秒。
		public long m_LastSendTickMsec; //存放最后发送的嘀嗒钟，单位为毫秒。
		public int m_IsRecvRplyPkt; //存放是否接收应答包，为0表示未接收，为1表示已接收。
	}
	public TstNtwkDly m_TstNtwkDlyPt = new TstNtwkDly();

	public int m_XfrMode; //存放传输模式，为0表示实时半双工（一键通），为1表示实时全双工。
	public int m_PttBtnIsDown; //存放一键即按即通按钮是否按下，为0表示弹起，为非0表示按下。

	public class TkbkInfo //对讲信息。
	{
		public int m_TkbkIdx; //存放对讲索引，从0开始，对讲信息容器的唯一索引，对讲中途不会改变。
		public int m_IsInit; //存放对讲信息是否初始化，为0表示未初始化，为非0表示已初始化。
		public int m_Num; //存放序号，从0开始，随着前面的对讲信息销毁而递减。

		public int m_RmtTkbkMode; //存放远端对讲模式。

		ConcurrentLinkedQueue< byte[] > m_RecvAdoOtptFrmCntnrPt; //存放接收音频输出帧容器的指针。
		ConcurrentLinkedQueue< byte[] > m_RecvVdoOtptFrmCntnrPt; //存放接收视频输出帧容器的指针。

		public AAjb m_AAjbPt; //存放音频自适应抖动缓冲器的指针。
		public VAjb m_VAjbPt; //存放视频自适应抖动缓冲器的指针。

		public class AdoOtptTmpVar //音频输出临时变量。
		{
			public HTInt m_CurHaveBufActFrmCntPt = new HTInt(); //存放当前已缓冲有活动帧的数量。
			public HTInt m_CurHaveBufInactFrmCntPt = new HTInt(); //存放当前已缓冲无活动帧的数量。
			public HTInt m_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
			public HTInt m_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
			public HTInt m_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
			public HTInt m_MaxCntuLostFrmCntPt = new HTInt(); //存放最大连续丢失帧的数量。
			public HTInt m_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。

			public byte m_TmpBytePt[] = new byte[ 1024 * 1024 ]; //存放临时数据。
			public HTInt m_TmpHTIntPt = new HTInt(); //存放临时数据。
			public HTLong m_TmpHTLongPt = new HTLong(); //存放临时数据。
		}
		public AdoOtptTmpVar m_AdoOtptTmpVarPt = new AdoOtptTmpVar();

		public class VdoOtptTmpVar //视频输出临时变量。
		{
			public HTInt m_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
			public HTInt m_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
			public HTInt m_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
			public HTInt m_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。

			public byte m_TmpBytePt[] = new byte[ 1024 * 1024 ]; //存放临时数据。
			public HTInt m_TmpHTIntPt = new HTInt(); //存放临时数据。
			public HTLong m_TmpHTLongPt = new HTLong(); //存放临时数据。
		}
		public VdoOtptTmpVar m_VdoOtptTmpVarPt = new VdoOtptTmpVar();
	}
	public ArrayList< TkbkInfo > m_TkbkInfoCntnrPt = new ArrayList<>(); //存放对讲信息容器的指针。
	public int m_TkbkInfoCurMaxNum; //存放对讲信息的当前最大序号。

	public int m_UseWhatRecvOtptFrm; //存放使用什么接收输出帧，为0表示容器，为1表示自适应抖动缓冲器。

	public class AAjbParm //存放音频自适应抖动缓冲器的参数。
	{
		public int m_MinNeedBufFrmCnt; //存放最小需缓冲帧的数量，单位为个帧，取值区间为[1,2147483647]。
		public int m_MaxNeedBufFrmCnt; //最大需缓冲帧的数量，单位为个帧，取值区间为[1,2147483647]，必须大于等于最小需缓冲帧的数量。
		public int m_MaxCntuLostFrmCnt; //最大连续丢失帧的数量，单位为个帧，取值区间为[1,2147483647]，当连续丢失帧的数量超过最大时，认为是对方中途暂停发送。
		public float m_AdaptSensitivity; //存放自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。
	}
	public AAjbParm m_AAjbParmPt = new AAjbParm();
	public class VAjbParm //存放视频自适应抖动缓冲器的参数。
	{
		public int m_MinNeedBufFrmCnt; //存放最小需缓冲帧的数量，单位为个帧，取值区间为[0,2147483647]。
		public int m_MaxNeedBufFrmCnt; //存放最大需缓冲帧的数量，单位为个帧，取值区间为[1,2147483647]，必须大于等于最小需缓冲帧数量。
		public float m_AdaptSensitivity; //存放自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。
	}
	public VAjbParm m_VAjbParmPt = new VAjbParm();

	public int m_CnctIsInit; //存放连接是否初始化，为0表示未初始化，为非0表示已初始化。
	public int m_IsTcpOrAudpPrtcl; //存放是否是Tcp或Udp协议，为0表示Tcp协议，为1表示高级Udp协议。
	public String m_RmtNodeNameStrPt; //存放远端套接字绑定的远端节点名称字符串的指针，
	public String m_RmtNodeSrvcStrPt; //存放远端套接字绑定的远端节点服务字符串的指针，
	public TcpClntSokt m_TcpClntSoktPt; //存放本端Tcp协议客户端套接字的指针。
	public long m_AudpClntCnctIdx; //存放本端高级Udp协议客户端连接索引。
	public int m_CurCnctSts; //存放当前连接状态，为[-m_MaxCnctTimes,0]表示等待远端接受连接。
	public int m_IsRqstDstoy; //存放是否请求销毁，为0表示未请求，为1表示已请求。

	public int m_MyTkbkIdx; //存放我的对讲索引。
	public int m_LclTkbkMode; //存放本端对讲模式。
	public int m_LastSendAdoInptFrmIsAct; //存放最后发送的音频输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
	public int m_LastSendAdoInptFrmTimeStamp; //存放最后发送音频输入帧的时间戳。
	public int m_LastSendVdoInptFrmTimeStamp; //存放最后发送视频输入帧的时间戳。
	public int m_IsRecvExitPkt; //存放是否接收退出包，为0表示未接收，为1表示已接收。

	//连接信息初始化。
	public int CnctInfoInit( int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			m_CnctIsInit = 1; //设置连接已初始化。
			m_IsTcpOrAudpPrtcl = IsTcpOrAudpPrtcl; //设置协议为Tcp协议或高级Udp协议。
			m_RmtNodeNameStrPt = RmtNodeNameStrPt; //设置远端套接字绑定的远端节点名称字符串的指针。
			m_RmtNodeSrvcStrPt = RmtNodeSrvcStrPt; //设置远端套接字绑定的远端节点服务字符串的指针。
			m_TcpClntSoktPt = null; //设置本端Tcp协议客户端套接字的指针。
			m_AudpClntCnctIdx = -1; //设置本端高级Udp协议客户端连接索引。
			m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Wait; //设置当前连接状态。
			m_IsRqstDstoy = 0; //设置是否请求销毁。

			m_MyTkbkIdx = -1; //设置我的对讲索引。
			m_LastSendAdoInptFrmIsAct = 0; //设置最后发送的音频输入帧为无语音活动。
			m_LastSendAdoInptFrmTimeStamp = 0 - 1; //设置最后发送音频输入帧的时间戳为0的前一个，因为第一次发送音频输入帧时会递增一个步进。
			m_LastSendVdoInptFrmTimeStamp = 0 - 1; //设置最后发送视频输入帧的时间戳为0的前一个，因为第一次发送视频输入帧时会递增一个步进。
			m_IsRecvExitPkt = 0; //设置是否接收退出包。

			String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化与远端节点" + ( ( IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + RmtNodeNameStrPt + ":" + RmtNodeSrvcStrPt + "]的连接成功。";
			if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
			m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );

			m_ClntMediaPocsThrdPt.UserTkbkClntCnctInit( IsTcpOrAudpPrtcl, RmtNodeNameStrPt, RmtNodeSrvcStrPt ); //调用用户定义的对讲客户端连接初始化函数。
			m_ClntMediaPocsThrdPt.UserTkbkClntCnctSts( m_CurCnctSts ); //调用用户定义的对讲客户端连接状态函数。

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			CnctInfoDstoy();
		}
		return p_Rslt;
	}

	//连接信息销毁。
	public void CnctInfoDstoy()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( m_CnctIsInit != 0 )
			{
				//发送退出包。
				if( ( m_IsRecvExitPkt == 0 ) && ( m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct ) ) //如果未接收退出包，且当前连接状态为已连接。
				{
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ClntMediaPocsThrd.PktTyp.Exit; //设置退出包。
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) m_MyTkbkIdx; //设置对讲索引。
					if( ( ( m_IsTcpOrAudpPrtcl == 0 ) && ( m_TcpClntSoktPt.SendApkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 2, ( short ) 0, 1, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
						( ( m_IsTcpOrAudpPrtcl == 1 ) && ( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( m_AudpClntCnctIdx, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 2, 1, 1, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) )
					{
						String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：发送退出包成功。对讲索引：" + m_MyTkbkIdx + "。总长度：2。";
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
						m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
					}
					else
					{
						String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：发送退出包失败。对讲索引：" + m_MyTkbkIdx + "。总长度：2。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
						m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
					}
				}

				TkbkInfoAllDstoy(); //对讲信息全部销毁。

				//销毁本端Tcp协议客户端套接字。
				if( m_TcpClntSoktPt != null )
				{
					m_TcpClntSoktPt.Dstoy( ( short ) -1, null );
					m_TcpClntSoktPt = null;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：销毁本端Tcp协议客户端套接字成功。" );
				}

				//销毁本端高级Udp协议客户端连接。
				if( m_AudpClntCnctIdx != -1 )
				{
					m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.ClosCnct( m_AudpClntCnctIdx, null );
					m_AudpClntCnctIdx = -1;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：销毁本端高级Udp协议客户端连接成功。" );
				}

				if( ( m_IsRecvExitPkt == 0 ) && ( m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Tmot ) ) //如果未接收退出包，且当前连接状态为异常断开，就重连。
				{
					m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Wait; //设置当前连接状态。
					m_ClntMediaPocsThrdPt.UserTkbkClntCnctSts( m_CurCnctSts ); //调用用户定义的对讲客户端连接状态函数。
					m_IsRqstDstoy = 0; //设置未请求销毁。

					m_MyTkbkIdx = -1; //设置我的对讲索引。
					m_LastSendAdoInptFrmIsAct = 0; //设置最后发送的音频输入帧为无语音活动。
					m_LastSendAdoInptFrmTimeStamp = 0 - 1; //设置最后发送音频输入帧的时间戳为0的前一个，因为第一次发送音频输入帧时会递增一个步进。
					m_LastSendVdoInptFrmTimeStamp = 0 - 1; //设置最后发送视频输入帧的时间戳为0的前一个，因为第一次发送视频输入帧时会递增一个步进。
					m_IsRecvExitPkt = 0; //设置未接收退出包。

					String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：连接异常断开，准备重连。";
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
					m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
				}
				else //如果已接收退出包，或当前连接状态不为异常断开，就销毁。
				{
					//设置当前连接状态。
					if( m_CurCnctSts <= ClntMediaPocsThrd.CnctSts.Wait )
					{
						m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Tmot;
						m_ClntMediaPocsThrdPt.UserTkbkClntCnctSts( m_CurCnctSts ); //调用用户定义的对讲客户端连接状态函数。
					}
					else if( m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct )
					{
						m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Dsct;
						m_ClntMediaPocsThrdPt.UserTkbkClntCnctSts( m_CurCnctSts ); //调用用户定义的对讲客户端连接状态函数。
					}

					m_CnctIsInit = 0; //设置连接未初始化。

					String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：销毁与远端节点" + ( ( m_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + m_RmtNodeNameStrPt + ":" + m_RmtNodeSrvcStrPt + "]的连接成功。";
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
					m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );

					m_ClntMediaPocsThrdPt.UserTkbkClntCnctDstoy(); //调用用户定义的对讲客户端连接销毁函数。
				}

				m_ClntMediaPocsThrdPt.SetTkbkMode( 0, 0 ); //设置对讲模式。
			}

			m_ClntMediaPocsThrdPt.IsAutoRqirExit(); //判断是否自动请求退出。
			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//连接发送数据包。
	public int CnctSendPkt( byte PktPt[], long PktLenByt, int Times, int IsRlab, Vstr ErrInfoVstrPt )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( ( m_CnctIsInit != 0 ) && ( m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct ) ) //如果连接已初始化，且当前连接状态为已连接。
			{
				//发送数据包。
				if( ( ( m_IsTcpOrAudpPrtcl == 0 ) && ( m_TcpClntSoktPt.SendApkt( PktPt, PktLenByt, ( short ) 0, Times, 0, ErrInfoVstrPt ) == 0 ) ) ||
					( ( m_IsTcpOrAudpPrtcl == 1 ) && ( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( m_AudpClntCnctIdx, PktPt, PktLenByt, Times, IsRlab, ErrInfoVstrPt ) == 0 ) ) )
				{
					//发送数据包成功。
				}
				else
				{
					break Out; //发送数据包失败。
				}
			}
			else
			{
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) if( ErrInfoVstrPt != null ) ErrInfoVstrPt.Cpy( "连接未初始化，或当前连接状态不为已连接。" );
				break Out;
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return p_Rslt;
	}

	//连接发送对讲模式包。
	public int CnctSendTkbkModePkt( int LclTkbkMode )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte )ClntMediaPocsThrd.PktTyp.TkbkMode; //设置对讲模式包。
			m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte )m_MyTkbkIdx; //设置对讲索引。
			m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] = ( byte )LclTkbkMode; //设置对讲模式。
			if( CnctSendPkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 3, 1, 1, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
			{
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：发送对讲模式包成功。对讲索引：" + m_MyTkbkIdx + "。对讲模式：" + ClntMediaPocsThrd.m_TkbkModeStrArrPt[ LclTkbkMode ] + "。" );
			}
			else
			{
				String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：发送对讲模式包失败。对讲索引：" + m_MyTkbkIdx + "。对讲模式：" + ClntMediaPocsThrd.m_TkbkModeStrArrPt[ LclTkbkMode ] + "。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
				m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
				break Out;
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return p_Rslt;
	}

	//对讲信息初始化。
	public TkbkInfo TkbkInfoInit( int TkbkIdx )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		TkbkInfo p_TkbkInfoTmpPt = null;

		Out:
		{
			//添加空对讲信息到对讲信息容器，直到对讲信息容器的元素总数达到对讲索引。
			while( TkbkIdx >= m_TkbkInfoCntnrPt.size() )
			{
				p_TkbkInfoTmpPt = new TkbkInfo();
				p_TkbkInfoTmpPt.m_TkbkIdx = m_TkbkInfoCntnrPt.size();
				p_TkbkInfoTmpPt.m_IsInit = 0;
				m_TkbkInfoCntnrPt.add( p_TkbkInfoTmpPt );
			}

			p_TkbkInfoTmpPt = m_TkbkInfoCntnrPt.get( TkbkIdx );
			p_TkbkInfoTmpPt.m_IsInit = 1; //设置对讲信息已初始化。
			m_TkbkInfoCurMaxNum++; //递增对讲信息的当前最大序号。
			p_TkbkInfoTmpPt.m_Num = m_TkbkInfoCurMaxNum; //设置序号。

			p_TkbkInfoTmpPt.m_RmtTkbkMode = ClntMediaPocsThrd.TkbkMode.None; //设置远端对讲模式。

			//接收输出帧初始化。
			switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
			{
				case 0: //如果要使用容器。
				{
					//初始化接收音频输出帧容器。
					p_TkbkInfoTmpPt.m_RecvAdoOtptFrmCntnrPt = new ConcurrentLinkedQueue< byte[] >(); //创建接收音频输出帧容器。
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + p_TkbkInfoTmpPt.m_TkbkIdx + "：初始化接收音频输出帧容器成功。" );

					//初始化接收视频输出帧容器。
					p_TkbkInfoTmpPt.m_RecvVdoOtptFrmCntnrPt = new ConcurrentLinkedQueue< byte[] >(); //创建接收视频输出帧容器。
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + p_TkbkInfoTmpPt.m_TkbkIdx + "：初始化接收视频输出帧容器成功。" );
					break;
				}
				case 1: //如果要使用自适应抖动缓冲器。
				{
					//初始化音频自适应抖动缓冲器。
					p_TkbkInfoTmpPt.m_AAjbPt = new HeavenTao.Ado.AAjb();
					if( p_TkbkInfoTmpPt.m_AAjbPt.Init( m_ClntMediaPocsThrdPt.m_LicnCodePt, m_ClntMediaPocsThrdPt.m_AdoOtptPt.m_SmplRate, m_ClntMediaPocsThrdPt.m_AdoOtptPt.m_FrmLenUnit, 1, 1, 0, m_AAjbParmPt.m_MinNeedBufFrmCnt, m_AAjbParmPt.m_MaxNeedBufFrmCnt, m_AAjbParmPt.m_MaxCntuLostFrmCnt, m_AAjbParmPt.m_AdaptSensitivity, ( m_XfrMode == 0 ) ? 0 : 1, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + p_TkbkInfoTmpPt.m_TkbkIdx + "：初始化音频自适应抖动缓冲器成功。" );
					}
					else
					{
						String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化音频自适应抖动缓冲器失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
						m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
						break Out;
					}

					//初始化视频自适应抖动缓冲器。
					p_TkbkInfoTmpPt.m_VAjbPt = new HeavenTao.Vdo.VAjb();
					if( p_TkbkInfoTmpPt.m_VAjbPt.Init( m_ClntMediaPocsThrdPt.m_LicnCodePt, 1, m_VAjbParmPt.m_MinNeedBufFrmCnt, m_VAjbParmPt.m_MaxNeedBufFrmCnt, m_VAjbParmPt.m_AdaptSensitivity, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + p_TkbkInfoTmpPt.m_TkbkIdx + "：初始化视频自适应抖动缓冲器成功。" );
					}
					else
					{
						String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化视频自适应抖动缓冲器失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
						m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
						break Out;
					}
					break;
				}
			}

			m_ClntMediaPocsThrdPt.UserTkbkClntTkbkInfoInit( p_TkbkInfoTmpPt ); //调用用户定义的对讲客户端对讲信息初始化函数。

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			TkbkInfoDstoy( TkbkIdx );
			p_TkbkInfoTmpPt = null;
		}
		return p_TkbkInfoTmpPt;
	}

	//对讲信息销毁。
	public void TkbkInfoDstoy( int TkbkIdx )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		TkbkInfo p_TkbkInfoTmpPt;
		TkbkInfo p_CnctInfoTmp2Pt;

		Out:
		{
			p_TkbkInfoTmpPt = m_TkbkInfoCntnrPt.get( TkbkIdx );
			if( p_TkbkInfoTmpPt.m_IsInit != 0 )
			{
				m_ClntMediaPocsThrdPt.UserTkbkClntTkbkInfoDstoy( p_TkbkInfoTmpPt ); //调用用户定义的对讲客户端对讲信息销毁函数。

				//销毁接收音频输出帧容器。
				if( p_TkbkInfoTmpPt.m_RecvAdoOtptFrmCntnrPt != null )
				{
					p_TkbkInfoTmpPt.m_RecvAdoOtptFrmCntnrPt.clear();
					p_TkbkInfoTmpPt.m_RecvAdoOtptFrmCntnrPt = null;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + p_TkbkInfoTmpPt.m_TkbkIdx + "：销毁接收音频输出帧容器成功。" );
				}

				//销毁接收视频输出帧容器。
				if( p_TkbkInfoTmpPt.m_RecvVdoOtptFrmCntnrPt != null )
				{
					p_TkbkInfoTmpPt.m_RecvVdoOtptFrmCntnrPt.clear();
					p_TkbkInfoTmpPt.m_RecvVdoOtptFrmCntnrPt = null;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + p_TkbkInfoTmpPt.m_TkbkIdx + "：销毁接收视频输出帧容器成功。" );
				}

				//销毁音频自适应抖动缓冲器。
				if( p_TkbkInfoTmpPt.m_AAjbPt != null )
				{
					p_TkbkInfoTmpPt.m_AAjbPt.Dstoy( null );
					p_TkbkInfoTmpPt.m_AAjbPt = null;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + p_TkbkInfoTmpPt.m_TkbkIdx + "：销毁音频自适应抖动缓冲器成功。" );
				}

				//销毁视频自适应抖动缓冲器。
				if( p_TkbkInfoTmpPt.m_VAjbPt != null )
				{
					p_TkbkInfoTmpPt.m_VAjbPt.Dstoy( null );
					p_TkbkInfoTmpPt.m_VAjbPt = null;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + p_TkbkInfoTmpPt.m_TkbkIdx + "：销毁视频自适应抖动缓冲器成功。" );
				}

				//递减后面对讲信息的序号。
				for( int p_TkbkInfoIdx = 0; p_TkbkInfoIdx < m_TkbkInfoCntnrPt.size(); p_TkbkInfoIdx++ )
				{
					p_CnctInfoTmp2Pt = m_TkbkInfoCntnrPt.get( p_TkbkInfoIdx );

					if( ( p_CnctInfoTmp2Pt.m_IsInit != 0 ) && ( p_CnctInfoTmp2Pt.m_Num > p_TkbkInfoTmpPt.m_Num ) )
					{
						p_CnctInfoTmp2Pt.m_Num--; //设置后面对讲信息的序号全部递减1。
					}
				}
				m_TkbkInfoCurMaxNum--; //递减对讲信息的当前最大序号。
				p_TkbkInfoTmpPt.m_IsInit = 0; //设置对讲信息未初始化。

				m_ClntMediaPocsThrdPt.SetTkbkMode( 0, 0 ); //设置对讲模式。
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//对讲信息全部销毁。
	public void TkbkInfoAllDstoy()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		TkbkInfo p_TkbkInfoTmpPt;

		Out:
		{
			for( int p_TkbkInfoIdx = 0; p_TkbkInfoIdx < m_TkbkInfoCntnrPt.size(); p_TkbkInfoIdx++ )
			{
				p_TkbkInfoTmpPt = m_TkbkInfoCntnrPt.get( p_TkbkInfoIdx );

				if( p_TkbkInfoTmpPt.m_IsInit != 0 )
				{
					TkbkInfoDstoy( p_TkbkInfoTmpPt.m_TkbkIdx );
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//连接处理，包括连接服务端、接收数据包。
	public void CnctPocs()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		TkbkInfo p_TkbkInfoTmpPt;
		int p_TmpInt;
		int p_TmpElmTotal;

		Out:
		{
			if( m_CnctIsInit != 0 )
			{
				//用本端客户端套接字连接远端服务端套接字。
				if( m_CurCnctSts <= ClntMediaPocsThrd.CnctSts.Wait ) //如果当前连接状态为等待远端接受连接。
				{
					int p_RmtNodeAddrFamly; //存放远端节点的地址族，为4表示IPv4，为6表示IPv6，为0表示自动选择。

					{ //设置远端节点的地址族。
						try
						{
							InetAddress inetAddress = InetAddress.getByName( m_RmtNodeNameStrPt );
							if( inetAddress.getAddress().length == 4 ) p_RmtNodeAddrFamly = 4;
							else p_RmtNodeAddrFamly = 6;
						}
						catch( UnknownHostException e )
						{
							p_RmtNodeAddrFamly = 0;
						}
					}

					if( m_IsTcpOrAudpPrtcl == 0 ) //用本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字。
					{
						int p_PocsRslt = -1; //存放本处理段的执行结果，为0表示成功，为非0表示失败。

						TcpClntSoktCnctOut:
						{
							if( m_TcpClntSoktPt == null ) //如果未初始化本端Tcp协议客户端套接字。
							{
								if( -m_CurCnctSts >= m_ClntMediaPocsThrdPt.m_MaxCnctTimes ) //如果已达到最大连接次数。
								{
									m_IsRqstDstoy = 1; //设置已请求销毁。这里只设置请求销毁，不设置当前连接状态，因为在连接信息销毁函数里要根据当前连接状态判断是否重连。

									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：已达到最大连接次数，中断连接。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									break TcpClntSoktCnctOut;
								}

								m_CurCnctSts--; //递增当前连接次数。
								m_ClntMediaPocsThrdPt.UserTkbkClntCnctSts( m_CurCnctSts ); //调用用户定义的对讲客户端连接状态函数。

								{
									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：开始第 " + -m_CurCnctSts + " 次连接。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
								}

								m_TcpClntSoktPt = new TcpClntSokt();
								if( m_TcpClntSoktPt.Init( p_RmtNodeAddrFamly, m_RmtNodeNameStrPt, m_RmtNodeSrvcStrPt, null, null, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字失败。
								{
									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + m_RmtNodeNameStrPt + ":" + m_RmtNodeSrvcStrPt + "]失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									break TcpClntSoktCnctOut;
								}
							}

							if( m_TcpClntSoktPt.WaitCnct( ( short ) 0, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //如果等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字成功。
							{
								if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == TcpClntSokt.TcpCnctStsWait ) //如果等待远端接受连接。
								{
									//继续等待本端本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字。
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == TcpClntSokt.TcpCnctStsCnct ) //如果连接成功。
								{
									if( m_TcpClntSoktPt.SetNoDelay( 1, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break TcpClntSoktCnctOut;
									}

									if( m_TcpClntSoktPt.SetSendBufSz( 1024 * 1024, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：设置本端Tcp协议客户端套接字的发送缓冲区大小失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break TcpClntSoktCnctOut;
									}

									if( m_TcpClntSoktPt.SetRecvBufSz( 1024 * 1024 * 3, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：设置本端Tcp协议客户端套接字的接收缓冲区大小失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break TcpClntSoktCnctOut;
									}

									if( m_TcpClntSoktPt.SetKeepAlive( 1, 1, 1, 5, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：设置本端Tcp协议客户端套接字的保活机制失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break TcpClntSoktCnctOut;
									}

									if( m_TcpClntSoktPt.GetLclAddr( null, m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodeAddrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodePortPt, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：获取本端Tcp协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break TcpClntSoktCnctOut;
									}

									if( m_TcpClntSoktPt.GetRmtAddr( null, m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodeAddrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodePortPt, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：获取本端Tcp协议客户端套接字连接的远端Tcp协议客户端套接字绑定的远程节点地址和端口失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break TcpClntSoktCnctOut;
									}

									m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Cnct; //设置当前连接状态为已连接。
									m_TstNtwkDlyPt.m_IsRecvRplyPkt = 1; //设置已接收测试网络延迟应答包，这样可以立即开始发送。
									m_ClntMediaPocsThrdPt.UserTkbkClntCnctSts( m_CurCnctSts ); //调用用户定义的对讲客户端连接状态函数。

									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化本端Tcp协议客户端套接字[" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodePortPt.m_Val + "]，并连接远端Tcp协议服务端套接字[" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodePortPt.m_Val + "]成功。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
								}
								else //如果连接失败。
								{
									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + m_RmtNodeNameStrPt + ":" + m_RmtNodeSrvcStrPt + "]失败。原因：连接失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									break TcpClntSoktCnctOut;
								}
							}
							else //如果等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字失败。
							{
								String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + m_RmtNodeNameStrPt + ":" + m_RmtNodeSrvcStrPt + "]失败。原因：等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
								if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
								m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
								break TcpClntSoktCnctOut;
							}

							p_PocsRslt = 0; //设置本处理段执行成功。
						}

						if( p_PocsRslt != 0 ) //如果本处理段执行失败。
						{
							if( m_TcpClntSoktPt != null )
							{
								m_TcpClntSoktPt.Dstoy( ( short ) -1, null );
								m_TcpClntSoktPt = null;
							}
						}
					}
					else //用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字。
					{
						int p_PocsRslt = -1; //存放本处理段的执行结果，为0表示成功，为非0表示失败。

						AudpClntSoktCnctOut:
						{
							if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt == null ) //如果未初始化本端高级Udp协议客户端套接字。
							{
								m_ClntMediaPocsThrdPt.m_AudpClntSoktPt = new AudpSokt();
								if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.Init( m_ClntMediaPocsThrdPt.m_LicnCodePt, p_RmtNodeAddrFamly, null, null, ( short )0, ( short )5000, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //如果初始化本端高级Udp协议客户端套接字成功。
								{
									if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.SetSendBufSz( 1024 * 1024, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：设置本端高级Udp协议客户端套接字的发送缓冲区大小失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break Out;
									}

									if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.SetRecvBufSz( 1024 * 1024 * 3, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：设置本端高级Udp协议客户端套接字的接收缓冲区大小失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break Out;
									}

									if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.GetLclAddr( null, m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodeAddrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodePortPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果获取本端高级Udp协议套接字绑定的本地节点地址和端口失败。
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：获取本端高级Udp协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break AudpClntSoktCnctOut;
									}

									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化本端高级Udp协议客户端套接字[" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodePortPt.m_Val + "]成功。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
								}
								else //如果初始化本端高级Udp协议客户端套接字失败。
								{
									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化本端高级Udp协议客户端套接字失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									break AudpClntSoktCnctOut;
								}
							}

							if( m_AudpClntCnctIdx == -1 )
							{
								if( -m_CurCnctSts >= m_ClntMediaPocsThrdPt.m_MaxCnctTimes ) //如果已达到最大连接次数。
								{
									m_IsRqstDstoy = 1; //设置已请求销毁。这里只设置请求销毁，不设置当前连接状态，因为在连接信息销毁函数里要根据当前连接状态判断是否重连。

									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：已达到最大连接次数，中断连接。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									break AudpClntSoktCnctOut;
								}

								m_CurCnctSts--; //递增当前连接次数。
								m_ClntMediaPocsThrdPt.UserTkbkClntCnctSts( m_CurCnctSts ); //调用用户定义的对讲客户端连接状态函数。

								{
									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：开始第 " + -m_CurCnctSts + " 次连接。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
								}

								if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.Cnct( p_RmtNodeAddrFamly, m_RmtNodeNameStrPt, m_RmtNodeSrvcStrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字失败。
								{
									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + m_RmtNodeNameStrPt + ":" + m_RmtNodeSrvcStrPt + "]失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									break AudpClntSoktCnctOut;
								}
								m_AudpClntCnctIdx = m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val; //设置本端高级Udp协议客户端连接索引。
							}

							if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.WaitCnct( m_AudpClntCnctIdx, ( short )0, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //循环等待本端高级Udp协议套接字连接远端成功。
							{
								if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsWait ) //如果等待远端接受连接。
								{
									//重新循环，继续等待本端高级Udp协议套接字连接远端。
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsCnct ) //如果连接成功。
								{
									if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.GetRmtAddr( m_AudpClntCnctIdx, null, m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodeAddrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodePortPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：获取本端高级Udp协议客户端套接字连接的远端高级Udp协议服务端套接字绑定的远程节点地址和端口失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break AudpClntSoktCnctOut;
									}

									m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Cnct; //设置当前连接状态为已连接。
									m_TstNtwkDlyPt.m_IsRecvRplyPkt = 1; //设置已接收测试网络延迟应答包，这样可以立即开始发送。
									m_ClntMediaPocsThrdPt.UserTkbkClntCnctSts( m_CurCnctSts ); //调用用户定义的对讲客户端连接状态函数。

									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodePortPt.m_Val + "]成功。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsTmot ) //如果连接超时。
								{
									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + m_RmtNodeNameStrPt + ":" + m_RmtNodeSrvcStrPt + "]失败。原因：连接超时。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									break AudpClntSoktCnctOut;
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsDsct ) //如果连接断开。
								{
									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + m_RmtNodeNameStrPt + ":" + m_RmtNodeSrvcStrPt + "]失败。原因：连接断开。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									break AudpClntSoktCnctOut;
								}
							}

							p_PocsRslt = 0; //设置本处理段执行成功。
						}

						if( p_PocsRslt != 0 ) //如果本处理段执行失败。
						{
							if( ( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt != null ) && ( m_AudpClntCnctIdx != -1 ) )
							{
								m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.ClosCnct( m_AudpClntCnctIdx, null );
								m_AudpClntCnctIdx = -1;
							}
						}
					}
				}

				//用本端客户端套接字接收远端服务端套接字发送的数据包。
				if( ( m_IsRqstDstoy == 0 ) && ( m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct ) ) //如果连接未请求销毁，且当前连接状态为已连接。
				{
					RecvPktOut:
					{
						if( ( ( m_IsTcpOrAudpPrtcl == 0 ) && ( m_TcpClntSoktPt.RecvApkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt.length, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt, ( short ) 0, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
							( ( m_IsTcpOrAudpPrtcl == 1 ) && ( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.RecvApkt( m_AudpClntCnctIdx, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt.length, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt, null, ( short ) 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) )
						{
							if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val != -1 ) //如果用本端套接字接收连接的远端套接字发送的数据包成功。
							{
								if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val == 0 ) //如果数据包的长度为0。
								{
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收数据包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "，表示没有数据，无法继续接收。" );
									break RecvPktOut;
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.TkbkIdx ) //如果是对讲索引包。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果对讲索引包的长度小于1 + 1，表示没有对讲索引。
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收对讲索引包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲索引，无法继续接收。" );
										break RecvPktOut;
									}

									if( m_MyTkbkIdx == -1 ) //如果未设置我的对讲索引。
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收我的对讲索引包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。" );

										m_MyTkbkIdx = m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ]; //设置我的对讲索引。
										m_ClntMediaPocsThrdPt.UserTkbkClntMyTkbkIdx( m_MyTkbkIdx ); //调用用户定义的对讲客户端我的对讲索引函数。
										CnctSendTkbkModePkt( m_LclTkbkMode ); //发送对讲模式包。
										m_ClntMediaPocsThrdPt.SetTkbkMode( 0, 0 ); //设置对讲模式。如果不参考远端对讲模式来设置对讲模式需要这步。
									}
									else
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收其他对讲索引包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。" );

										TkbkInfoInit( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] ); //对讲信息初始化。
									}
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.TkbkMode ) //如果是对讲模式包。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 + 1 ) //如果对讲模式包的长度小于1 + 1 + 1，表示没有对讲索引和对讲模式。
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收对讲模式包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1 + 1，表示没有对讲索引和对讲模式，无法继续接收。" );
										break RecvPktOut;
									}
									if( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] < ( byte ) ClntMediaPocsThrd.TkbkMode.None ) || ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] >= ( byte ) ClntMediaPocsThrd.TkbkMode.NoChg ) )
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收对讲模式包。对讲模式为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] + "不正确，无法继续接收。" );
										break RecvPktOut;
									}

									p_TkbkInfoTmpPt = m_TkbkInfoCntnrPt.get( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] );
									int p_OldRmtTkbkMode = p_TkbkInfoTmpPt.m_RmtTkbkMode; //设置旧远端对讲模式。
									p_TkbkInfoTmpPt.m_RmtTkbkMode = m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ]; //设置远端对讲模式。
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收对讲模式包。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。对讲模式：" + ClntMediaPocsThrd.m_TkbkModeStrArrPt[ p_TkbkInfoTmpPt.m_RmtTkbkMode ] + "。" );
									m_ClntMediaPocsThrdPt.UserTkbkClntTkbkInfoRmtTkbkMode( p_TkbkInfoTmpPt, p_OldRmtTkbkMode, p_TkbkInfoTmpPt.m_RmtTkbkMode ); //调用用户定义的对讲客户端对讲信息远端对讲模式函数。

									m_ClntMediaPocsThrdPt.SetTkbkMode( 0, 0 ); //设置对讲模式。
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.AdoFrm ) //如果是音频输出帧包。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 + 4 ) //如果音频输出帧包的长度小于1 + 1 + 4，表示没有对讲索引和音频输出帧时间戳。
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收音频输出帧包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1 + 4，表示没有对讲索引和音频输出帧时间戳，无法继续接收。" );
										break RecvPktOut;
									}

									//读取音频输出帧时间戳。
									p_TmpInt = ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 8 ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 16 ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] & 0xFF ) << 24 );

									//将音频输出帧放入容器或自适应抖动缓冲器。
									p_TkbkInfoTmpPt = m_TkbkInfoCntnrPt.get( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] );
									if( ( m_LclTkbkMode & ClntMediaPocsThrd.TkbkMode.AdoOtpt ) != 0 ) //如果本端对讲模式有音频输出。
									{
										switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
										{
											case 0: //如果要使用容器。
											{
												if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该音频输出帧为有语音活动。
												{
													p_TmpElmTotal = p_TkbkInfoTmpPt.m_RecvAdoOtptFrmCntnrPt.size(); //获取接收音频输出帧容器的元素总数。
													if( p_TmpElmTotal <= 50 )
													{
														p_TkbkInfoTmpPt.m_RecvAdoOtptFrmCntnrPt.offer( Arrays.copyOfRange( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 1 + 1 + 4, ( int ) ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val ) ) );
														if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收有语音活动的音频输出帧包。放入接收音频输出帧容器成功。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
													}
													else
													{
														if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收有语音活动的音频输出帧包。接收音频输出帧容器中帧总数为" + p_TmpElmTotal + "已经超过上限50，不再放入接收音频输出帧容器。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
													}
												}
												else //如果该音频输出帧为无语音活动。
												{
													if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收无语音活动的音频输出帧包。无需放入接收音频输出帧容器。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
												}
												break;
											}
											case 1: //如果要使用自适应抖动缓冲器。
											{
												if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该音频输出帧为有语音活动。
												{
													p_TkbkInfoTmpPt.m_AAjbPt.PutByteFrm( p_TmpInt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 1 + 1 + 4, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val - 1 - 1 - 4, 1, null );
													if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收有语音活动的音频输出帧包。放入音频自适应抖动缓冲器成功。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
												}
												else //如果该音频输出帧为无语音活动。
												{
													p_TkbkInfoTmpPt.m_AAjbPt.PutByteFrm( p_TmpInt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 1 + 1 + 4, 0, 1, null );
													if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收无语音活动的音频输出帧包。放入音频自适应抖动缓冲器成功。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
												}

												p_TkbkInfoTmpPt.m_AAjbPt.GetBufFrmCnt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurHaveBufActFrmCntPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurHaveBufInactFrmCntPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurHaveBufFrmCntPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_MinNeedBufFrmCntPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_MaxNeedBufFrmCntPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_MaxCntuLostFrmCntPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurNeedBufFrmCntPt, 1, null );
												if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。音频自适应抖动缓冲器：有活动帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurHaveBufActFrmCntPt.m_Val + "，无活动帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurHaveBufInactFrmCntPt.m_Val + "，帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_MaxNeedBufFrmCntPt.m_Val + "，最大丢帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_MaxCntuLostFrmCntPt.m_Val + "，当前需帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurNeedBufFrmCntPt.m_Val + "。" );
												break;
											}
										}
									}
									else //如果本端对讲模式无音频输出。
									{
										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该音频输出帧为有语音活动。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收有语音活动的音频输出帧包。但本端对讲模式无音频。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
										}
										else //如果该音频输出帧为无语音活动。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收无语音活动的音频输出帧包。但本端对讲模式无音频。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
										}
									}
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.VdoFrm ) //如果是视频输出帧包。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 + 4 ) //如果视频输出帧包的长度小于1 + 1 + 4，表示没有对讲索引和视频输出帧时间戳。
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收视频输出帧包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1 + 4，表示没有对讲索引和视频输出帧时间戳，无法继续接收。" );
										break RecvPktOut;
									}

									//读取视频输出帧时间戳。
									p_TmpInt = ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 8 ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 16 ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] & 0xFF ) << 24 );

									//将视频输出帧放入容器或自适应抖动缓冲器。
									p_TkbkInfoTmpPt = m_TkbkInfoCntnrPt.get( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] );
									if( ( m_LclTkbkMode & ClntMediaPocsThrd.TkbkMode.VdoOtpt ) != 0 ) //如果本端对讲模式有视频输出。
									{
										switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
										{
											case 0: //如果要使用容器。
											{
												if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该视频输出帧为有图像活动。
												{
													p_TmpElmTotal = p_TkbkInfoTmpPt.m_RecvVdoOtptFrmCntnrPt.size(); //获取接收视频输出帧容器的元素总数。
													if( p_TmpElmTotal <= 20 )
													{
														p_TkbkInfoTmpPt.m_RecvVdoOtptFrmCntnrPt.offer( Arrays.copyOfRange( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 1 + 1 + 4, ( int ) ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val ) ) );
														if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收有图像活动的视频输出帧包。放入接收视频输出帧容器成功。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。类型：" + ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。" );
													}
													else
													{
														if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收有图像活动的视频输出帧包。接收视频输出帧容器中帧总数为" + p_TmpElmTotal + "已经超过上限20，不再放入接收视频输出帧容器。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。类型：" + ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。" );
													}
												}
												else //如果该视频输出帧为无图像活动。
												{
													if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收无图像活动的视频输出帧包。无需放入接收视频输出帧容器。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
												}
												break;
											}
											case 1: //如果要使用自适应抖动缓冲器。
											{
												if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该视频输出帧为有图像活动。
												{
													p_TkbkInfoTmpPt.m_VAjbPt.PutByteFrm( SystemClock.uptimeMillis(), p_TmpInt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 1 + 1 + 4, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val - 1 - 1 - 4, 1, null );
													if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收有图像活动的视频输出帧包。放入视频自适应抖动缓冲器成功。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。类型：" + ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。" );
												}
												else //如果该视频输出帧为无图像活动。
												{
													if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收无图像活动的视频输出帧包。无需放入视频自适应抖动缓冲器。视对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。类型：" + ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。" );
												}

												p_TkbkInfoTmpPt.m_VAjbPt.GetBufFrmCnt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurHaveBufFrmCntPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_MinNeedBufFrmCntPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_MaxNeedBufFrmCntPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurNeedBufFrmCntPt, 1, null );
												if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。视频自适应抖动缓冲器：帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_MaxNeedBufFrmCntPt.m_Val + "，当前需帧：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_CurNeedBufFrmCntPt.m_Val + "。" );
												break;
											}
										}
									}
									else  //如果本端对讲模式无视频输出。
									{
										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该视频输出帧为有图像活动。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收有图像活动的视频输出帧包。但本端对讲模式无视频。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。类型：" + ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。" );
										}
										else //如果该视频输出帧为无图像活动。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收无图像活动的视频输出帧包。但本端对讲模式无视频。对讲索引：" + p_TkbkInfoTmpPt.m_TkbkIdx + "。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
										}
									}
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.TstNtwkDly ) //如果是测试网络延迟包。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果测试网络延迟包的长度小于1 + 1，表示没有对讲索引。
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收测试网络延迟包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲索引，无法继续接收。" );
										break RecvPktOut;
									}
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] != m_MyTkbkIdx )
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收测试网络延迟包。索引为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "与我的对讲索引" + m_MyTkbkIdx + "不一致，无法继续接收。" );
										break RecvPktOut;
									}

									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收测试网络延迟包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );

									m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) ClntMediaPocsThrd.PktTyp.TstNtwkDlyRply; //设置测试网络延迟应答包。
									if( CnctSendPkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 2, 1, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：发送测试网络延迟应答包成功。对讲索引：" + m_MyTkbkIdx + "。总长度：2。" );
									}
									else
									{
										String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：发送测试网络延迟应答包失败。对讲索引：" + m_MyTkbkIdx + "。总长度：2。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									}
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.TstNtwkDlyRply ) //如果是测试网络延迟应答包。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果退出包的长度小于1 + 1，表示没有对讲索引。
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收测试网络延迟应答包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲索引，无法继续接收。" );
										break RecvPktOut;
									}
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] != m_MyTkbkIdx )
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收测试网络延迟应答包。索引为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "与我的对讲索引" + m_MyTkbkIdx + "不一致，无法继续接收。" );
										break RecvPktOut;
									}

									long p_NtwkDlyMsec = SystemClock.uptimeMillis() - m_TstNtwkDlyPt.m_LastSendTickMsec; //存放网络延迟，单位为毫秒。

									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收测试网络延迟应答包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。延迟：" + p_NtwkDlyMsec + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );

									if( m_TstNtwkDlyPt.m_IsTstNtwkDly != 0 ) //如果要测试网络延迟。
									{
										m_TstNtwkDlyPt.m_IsRecvRplyPkt = 1; //设置已接收测试网络延迟应答包。
										m_ClntMediaPocsThrdPt.UserTkbkClntTstNtwkDly( p_NtwkDlyMsec ); //调用用户定义的对讲客户端测试网络延迟函数。
									}
								}
								else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.Exit ) //如果是退出包。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果退出包的长度小于1 + 1，表示没有对讲索引。
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：接收退出包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲索引，无法继续接收。" );
										break RecvPktOut;
									}

									String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：接收退出包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。";
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									if( m_ClntMediaPocsThrdPt.m_IsShowToast != 0 ) m_ClntMediaPocsThrdPt.UserShowToast( p_InfoStrPt );

									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] == m_MyTkbkIdx ) //如果对讲索引是我的对讲索引。
									{
										m_IsRecvExitPkt = 1; //设置已接收退出包。
										m_IsRqstDstoy = 1; //设置已请求销毁。
									}
									else
									{
										TkbkInfoDstoy( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] ); //对讲信息销毁。
									}
								}
							} //如果用本端套接字接收连接的远端套接字发送的数据包超时，就重新接收。
						}
						else //如果用本端套接字接收连接的远端套接字发送的数据包失败。
						{
							String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：用本端套接字接收连接的远端套接字发送的数据包失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
							m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );

							m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Tmot; //设置当前连接状态为异常断开。
							m_ClntMediaPocsThrdPt.UserTkbkClntCnctSts( m_CurCnctSts ); //调用用户定义的对讲客户端连接状态函数。
							m_IsRqstDstoy = 1; //设置已请求销毁。
							break RecvPktOut;
						}
					}
				}

				//用本端客户端套接字测试网络延迟。
				if( ( m_TstNtwkDlyPt.m_IsTstNtwkDly != 0 ) && ( m_IsRqstDstoy == 0 ) && ( m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct ) && ( m_MyTkbkIdx != -1 ) ) //如果要测试网络延迟，且连接未请求销毁，且当前连接状态为已连接，且已设置我的对讲索引。
				{
					long p_CurTickMsec = SystemClock.uptimeMillis(); //存放当前嘀嗒钟，单位为毫秒。

					if( ( m_TstNtwkDlyPt.m_IsRecvRplyPkt != 0 ) && ( p_CurTickMsec - m_TstNtwkDlyPt.m_LastSendTickMsec >= m_TstNtwkDlyPt.m_SendIntvlMsec ) ) //如果已接收测试网络延迟应答包，且最后发送测试网络延迟包已超过间隔时间。
					{
						m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) ClntMediaPocsThrd.PktTyp.TstNtwkDly; //设置数据包类型为测试网络延迟包。
						m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) m_MyTkbkIdx; //设置对讲索引。
						if( CnctSendPkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 2, 1, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
						{
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：发送测试网络延迟包成功。对讲索引：" + m_MyTkbkIdx + "。总长度：2。" );

							m_TstNtwkDlyPt.m_LastSendTickMsec = p_CurTickMsec; //设置测试网络延迟包最后发送的嘀嗒钟。
							m_TstNtwkDlyPt.m_IsRecvRplyPkt = 0; //设置未接收测试网络延迟应答包。
						}
						else
						{
							String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：发送测试网络延迟包失败。对讲索引：" + m_MyTkbkIdx + "。总长度：2。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
							m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
						}
					}
				}

				//连接销毁。
				if( m_IsRqstDstoy == 1 ) //如果已请求销毁。
				{
					CnctInfoDstoy();
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//用户定义的读取音视频输入帧函数。
	public void UserReadAdoVdoInptFrm( short AdoInptPcmSrcFrmPt[], short AdoInptPcmRsltFrmPt[], long AdoInptPcmFrmLenUnit, int AdoInptPcmRsltFrmVoiceActSts,
									   byte AdoInptEncdRsltFrmPt[], long AdoInptEncdRsltFrmLenByt, int AdoInptEncdRsltFrmIsNeedTrans,
									   byte VdoInptNv21SrcFrmPt[], int VdoInptNv21SrcFrmWidthPt, int VdoInptNv21SrcFrmHeightPt, long VdoInptNv21SrcFrmLenByt,
									   byte VdoInptYu12RsltFrmPt[], int VdoInptYu12RsltFrmWidth, int VdoInptYu12RsltFrmHeight, long VdoInptYu12RsltFrmLenByt,
									   byte VdoInptEncdRsltFrmPt[], long VdoInptEncdRsltFrmLenByt )
	{
		int p_PktLenByt = 0; //存放数据包的长度，单位字节。
		int p_TmpInt32 = 0;

		//发送音频输入帧。
		if( AdoInptPcmSrcFrmPt != null ) //如果有音频输入Pcm格式原始帧。
		{
			if( AdoInptEncdRsltFrmPt == null ) //如果没有音频输入已编码格式结果帧。
			{
				if( AdoInptPcmRsltFrmVoiceActSts != 0 ) //如果音频输入Pcm格式结果帧为有语音活动。
				{
					for( p_TmpInt32 = 0; p_TmpInt32 < AdoInptPcmRsltFrmPt.length; p_TmpInt32++ ) //设置音频输入帧。
					{
						m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 + 1 + 4 + p_TmpInt32 * 2 ] = ( byte ) ( AdoInptPcmRsltFrmPt[ p_TmpInt32 ] & 0xFF );
						m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 + 1 + 4 + p_TmpInt32 * 2 + 1 ] = ( byte ) ( ( AdoInptPcmRsltFrmPt[ p_TmpInt32 ] & 0xFF00 ) >> 8 );
					}
					p_PktLenByt = 1 + 1 + 4 + AdoInptPcmRsltFrmPt.length * 2; //数据包长度 = 数据包类型 + 对讲索引 + 音频输入帧时间戳 + 音频输入Pcm格式结果帧。
				}
				else //如果音频输入Pcm格式结果帧为无语音活动。
				{
					p_PktLenByt = 1 + 1 + 4; //数据包长度 = 数据包类型 + 对讲索引 + 音频输入帧时间戳。
				}
			}
			else //如果有音频输入已编码格式结果帧。
			{
				if( AdoInptPcmRsltFrmVoiceActSts != 0 && AdoInptEncdRsltFrmIsNeedTrans != 0 ) //如果音频输入Pcm格式结果帧为有语音活动，且音频输入已编码格式结果帧需要传输。
				{
					System.arraycopy( AdoInptEncdRsltFrmPt, 0, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 1 + 1 + 4, ( int ) AdoInptEncdRsltFrmLenByt ); //设置音频输入帧。
					p_PktLenByt = 1 + 1 + 4 + ( int ) AdoInptEncdRsltFrmLenByt; //数据包长度 = 数据包类型 + 对讲索引 + 音频输入帧时间戳 + 音频输入已编码格式结果帧。
				}
				else //如果音频输入Pcm格式结果帧为无语音活动，或不需要传输。
				{
					p_PktLenByt = 1 + 1 + 4; //数据包长度 = 数据包类型 + 对讲索引 + 音频输入帧时间戳。
				}
			}

			if( p_PktLenByt != 1 + 1 + 4 ) //如果本次音频输入帧为有语音活动，就发送。
			{
				m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

				//设置数据包类型为音频输入帧包。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) ClntMediaPocsThrd.PktTyp.AdoFrm;
				//设置对讲索引。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) m_MyTkbkIdx;
				//设置音频输入帧时间戳。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

				if( CnctSendPkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, p_PktLenByt, 1, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
				{
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：发送有语音活动的音频输入帧包成功。对讲索引：" + m_MyTkbkIdx + "。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "。总长度：" + p_PktLenByt + "。" );
				}
				else
				{
					String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：发送有语音活动的音频输入帧包失败。对讲索引：" + m_MyTkbkIdx + "，音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "。总长度：" + p_PktLenByt + "。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
				}

				m_LastSendAdoInptFrmIsAct = 1; //设置最后发送的音频输入帧有语音活动。
			}
			else //如果本次音频输入帧为无语音活动。
			{
				if( m_LastSendAdoInptFrmIsAct != 0 ) //如果最后发送的音频输入帧为有语音活动，就发送。
				{
					m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

					//设置数据包类型为音频输入帧包。
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) ClntMediaPocsThrd.PktTyp.AdoFrm;
					//设置对讲索引。
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) m_MyTkbkIdx;
					//设置音频输入帧时间戳。
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

					if( CnctSendPkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, p_PktLenByt, 1, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
					{
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：发送无语音活动的音频输入帧包成功。对讲索引：" + m_MyTkbkIdx + "。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "。总长度：" + p_PktLenByt + "。" );
					}
					else
					{
						String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：发送无语音活动的音频输入帧包失败。对讲索引：" + m_MyTkbkIdx + "，音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "。总长度：" + p_PktLenByt + "。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
						m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
					}

					m_LastSendAdoInptFrmIsAct = 0; //设置最后发送的音频输入帧无语音活动。
				}
				else //如果最后发送的音频输入帧为无语音活动，无需发送。
				{
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：本次音频输入帧为无语音活动，且最后发送的音频输入帧为无语音活动，无需发送。" );
				}
			}
		}

		//发送视频输入帧。
		if( VdoInptYu12RsltFrmPt != null ) //如果有视频输入Yu12格式结果帧。
		{
			if( VdoInptEncdRsltFrmPt == null ) //如果没有视频输入已编码格式结果帧。
			{
				//设置视频输入帧宽度。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 6 ] = ( byte ) ( VdoInptYu12RsltFrmWidth & 0xFF );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 7 ] = ( byte ) ( ( VdoInptYu12RsltFrmWidth & 0xFF00 ) >> 8 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 8 ] = ( byte ) ( ( VdoInptYu12RsltFrmWidth & 0xFF0000 ) >> 16 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 9 ] = ( byte ) ( ( VdoInptYu12RsltFrmWidth & 0xFF000000 ) >> 24 );
				//设置视频输入帧高度。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] = ( byte ) ( VdoInptYu12RsltFrmHeight & 0xFF );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 11 ] = ( byte ) ( ( VdoInptYu12RsltFrmHeight & 0xFF00 ) >> 8 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 12 ] = ( byte ) ( ( VdoInptYu12RsltFrmHeight & 0xFF0000 ) >> 16 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 13 ] = ( byte ) ( ( VdoInptYu12RsltFrmHeight & 0xFF000000 ) >> 24 );

				System.arraycopy( VdoInptYu12RsltFrmPt, 0, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 1 + 1 + 4 + 4 + 4, VdoInptYu12RsltFrmPt.length ); //设置视频输入帧。
				p_PktLenByt = 1 + 1 + 4 + 4 + 4 + VdoInptYu12RsltFrmPt.length; //数据包长度 = 数据包类型 + 对讲索引 + 视频输入帧时间戳 + 视频输入帧宽度 + 视频输入帧高度 + 视频输入Yu12格式结果帧。
			}
			else //如果有视频输入已编码格式结果帧。
			{
				if( VdoInptEncdRsltFrmLenByt != 0 ) //如果本次视频输入帧为有图像活动。
				{
					System.arraycopy( VdoInptEncdRsltFrmPt, 0, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 1 + 1 + 4, ( int ) VdoInptEncdRsltFrmLenByt ); //设置视频输入帧。
					p_PktLenByt = 1 + 1 + 4 + ( int ) VdoInptEncdRsltFrmLenByt; //数据包长度 = 数据包类型 + 对讲索引 + 视频输入帧时间戳 + 视频输入已编码格式结果帧。
				}
				else
				{
					p_PktLenByt = 1 + 1 + 4; //数据包长度 = 数据包类型 + 对讲索引 + 视频输入帧时间戳。
				}
			}

			if( p_PktLenByt != 1 + 1 + 4 ) //如果本次视频输入帧为有图像活动，就发送。
			{
				m_LastSendVdoInptFrmTimeStamp += 1; //视频输入帧的时间戳递增一个步进。

				//设置数据包类型为视频输入帧包。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) ClntMediaPocsThrd.PktTyp.VdoFrm;
				//设置对讲索引。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) m_MyTkbkIdx;
				//设置视频输入帧时间戳。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( m_LastSendVdoInptFrmTimeStamp & 0xFF );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

				if( CnctSendPkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, p_PktLenByt, 1, 1, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
				{
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：发送有图像活动的视频输入帧包成功。对讲索引：" + m_MyTkbkIdx + "。视频输入帧时间戳：" + m_LastSendVdoInptFrmTimeStamp + "。总长度：" + p_PktLenByt + "。类型：" + ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。" );
				}
				else
				{
					String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：发送有图像活动的视频输入帧包失败。对讲索引：" + m_MyTkbkIdx + "，视频输入帧时间戳：" + m_LastSendVdoInptFrmTimeStamp + "。总长度：" + p_PktLenByt + "。类型：" + ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
					m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
				}
			}
			else //如果本次视频输入帧为无图像活动，无需发送。
			{
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：本次视频输入帧为无图像活动，无需发送。" );
			}
		}
	}

	//用户定义的写入音频输出帧函数。
	public void UserWriteAdoOtptFrm( int AdoOtptStrmIdx,
									 short AdoOtptPcmSrcFrmPt[], int AdoOtptPcmFrmLenUnit,
									 byte AdoOtptEncdSrcFrmPt[], long AdoOtptEncdSrcFrmSzByt, HTLong AdoOtptEncdSrcFrmLenBytPt )
	{
		TkbkInfo p_TkbkInfoTmpPt;
		int p_AdoOtptFrmTimeStamp = 0;
		byte p_AdoOtptFrmPt[] = null;
		long p_AdoOtptFrmLen = 0;
		int p_TmpInt32;

		Out:
		{
			p_TkbkInfoTmpPt = m_TkbkInfoCntnrPt.get( AdoOtptStrmIdx ); //这里不用加线程锁，因为媒体处理线程只会递增对讲信息容器的大小，且递增的时候只有一次赋值操作，且音频输出流索引不可能会越界。

			//取出并写入音频输出帧。
			{
				//从容器或自适应抖动缓冲器取出音频输出帧。
				switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
				{
					case 0: //如果要使用容器。
					{
						if( !p_TkbkInfoTmpPt.m_RecvAdoOtptFrmCntnrPt.isEmpty() ) //如果接收音频输出帧容器不为空。
						{
							p_AdoOtptFrmPt = p_TkbkInfoTmpPt.m_RecvAdoOtptFrmCntnrPt.poll(); //从接收音频输出帧容器中取出并删除第一个帧。
							p_AdoOtptFrmLen = p_AdoOtptFrmPt.length;
						}

						if( p_AdoOtptFrmLen != 0 ) //如果音频输出帧为有语音活动。
						{
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + AdoOtptStrmIdx + "：从接收音频输出帧容器取出有语音活动的音频输出帧。长度：" + p_AdoOtptFrmLen + "。" );
						}
						else //如果接收音频输出帧容器为空，或音频输出帧为无语音活动。
						{
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + AdoOtptStrmIdx + "：从接收音频输出帧容器取出无语音活动的音频输出帧。长度：" + p_AdoOtptFrmLen + "。" );
						}

						break;
					}
					case 1: //如果要使用自适应抖动缓冲器。
					{
						p_TkbkInfoTmpPt.m_AAjbPt.GetBufFrmCnt( p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_CurHaveBufActFrmCntPt, p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_CurHaveBufInactFrmCntPt, p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_CurHaveBufFrmCntPt, p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_MinNeedBufFrmCntPt, p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_MaxNeedBufFrmCntPt, p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_MaxCntuLostFrmCntPt, p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_CurNeedBufFrmCntPt, 1, null );
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + AdoOtptStrmIdx + "：音频自适应抖动缓冲器：有活动帧：" + p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_CurHaveBufActFrmCntPt.m_Val + "，无活动帧：" + p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_CurHaveBufInactFrmCntPt.m_Val + "，帧：" + p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_MaxNeedBufFrmCntPt.m_Val + "，最大丢帧：" + p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_MaxCntuLostFrmCntPt.m_Val + "，当前需帧：" + p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_CurNeedBufFrmCntPt.m_Val + "。" );

						//从音频自适应抖动缓冲器取出音频输出帧。
						p_TkbkInfoTmpPt.m_AAjbPt.GetByteFrm( p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_TmpHTIntPt, p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_TmpBytePt, 0, p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_TmpBytePt.length, p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_TmpHTLongPt, 1, null );
						p_AdoOtptFrmTimeStamp = p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_TmpHTIntPt.m_Val;
						p_AdoOtptFrmPt = p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_TmpBytePt;
						p_AdoOtptFrmLen = p_TkbkInfoTmpPt.m_AdoOtptTmpVarPt.m_TmpHTLongPt.m_Val;

						if( p_AdoOtptFrmLen > 0 ) //如果音频输出帧为有语音活动。
						{
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + AdoOtptStrmIdx + "：从音频自适应抖动缓冲器取出有语音活动的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，长度：" + p_AdoOtptFrmLen + "。" );
						}
						else if( p_AdoOtptFrmLen == 0 ) //如果音频输出帧为无语音活动。
						{
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + AdoOtptStrmIdx + "：从音频自适应抖动缓冲器取出无语音活动的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，长度：" + p_AdoOtptFrmLen + "。" );
						}
						else //如果音频输出帧为丢失。
						{
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + AdoOtptStrmIdx + "：从音频自适应抖动缓冲器取出丢失的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，长度：" + p_AdoOtptFrmLen + "。" );
						}
						break;
					}
				}

				//写入音频输出帧。
				if( p_AdoOtptFrmLen > 0 ) //如果音频输出帧为有语音活动。
				{
					if( AdoOtptPcmSrcFrmPt != null ) //如果要使用音频输出Pcm格式原始帧。
					{
						if( p_AdoOtptFrmLen != AdoOtptPcmFrmLenUnit * 2 )
						{
							Arrays.fill( AdoOtptPcmSrcFrmPt, ( short ) 0 );
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + AdoOtptStrmIdx + "：音频输出帧的长度不等于Pcm格式的长度。音频输出帧：" + ( p_AdoOtptFrmLen ) + "。Pcm格式：" + ( AdoOtptPcmSrcFrmPt.length * 2 ) + "。" );
						}
						else
						{
							//写入音频输出Pcm格式原始帧。
							for( p_TmpInt32 = 0; p_TmpInt32 < AdoOtptPcmSrcFrmPt.length; p_TmpInt32++ )
							{
								AdoOtptPcmSrcFrmPt[ p_TmpInt32 ] = ( short ) ( ( p_AdoOtptFrmPt[ p_TmpInt32 * 2 ] & 0xFF ) | ( p_AdoOtptFrmPt[ p_TmpInt32 * 2 + 1 ] << 8 ) );
							}
						}
					}
					else //如果要使用音频输出已编码格式原始帧。
					{
						if( p_AdoOtptFrmLen > AdoOtptEncdSrcFrmPt.length )
						{
							AdoOtptEncdSrcFrmLenBytPt.m_Val = 0;
							if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + AdoOtptStrmIdx + "：音频输出帧的长度已超过已编码格式的长度。音频输出帧：" + ( p_AdoOtptFrmLen ) + "。已编码格式：" + AdoOtptEncdSrcFrmPt.length + "。" );
						}
						else
						{
							//写入音频输出已编码格式原始帧。
							System.arraycopy( p_AdoOtptFrmPt, 0, AdoOtptEncdSrcFrmPt, 0, ( int ) ( p_AdoOtptFrmLen ) );
							AdoOtptEncdSrcFrmLenBytPt.m_Val = p_AdoOtptFrmLen;
						}
					}
				}
				else if( p_AdoOtptFrmLen == 0 ) //如果音频输出帧为无语音活动。
				{
					if( AdoOtptPcmSrcFrmPt != null ) //如果要使用音频输出Pcm格式原始帧。
					{
						Arrays.fill( AdoOtptPcmSrcFrmPt, ( short ) 0 );
					}
					else //如果要使用音频输出已编码格式原始帧。
					{
						AdoOtptEncdSrcFrmLenBytPt.m_Val = 0;
					}
				}
				else //如果音频输出帧为丢失。
				{
					if( AdoOtptPcmSrcFrmPt != null ) //如果要使用音频输出Pcm格式原始帧。
					{
						Arrays.fill( AdoOtptPcmSrcFrmPt, ( short ) 0 );
					}
					else //如果要使用音频输出已编码格式原始帧。
					{
						AdoOtptEncdSrcFrmLenBytPt.m_Val = p_AdoOtptFrmLen;
					}
				}
			}
		}
	}

	//用户定义的写入视频输出帧函数。
	public void UserWriteVdoOtptFrm( int VdoOtptStrmIdx,
									 byte VdoOtptYu12SrcFrmPt[], HTInt VdoOtptYu12SrcFrmWidthPt, HTInt VdoOtptYu12SrcFrmHeightPt,
									 byte VdoOtptEncdSrcFrmPt[], long VdoOtptEncdSrcFrmSzByt, HTLong VdoOtptEncdSrcFrmLenBytPt )
	{
		TkbkInfo p_TkbkInfoTmpPt;
		int p_VdoOtptFrmTimeStamp;
		byte p_VdoOtptFrmPt[] = null;
		long p_VdoOtptFrmLen = 0;

		p_TkbkInfoTmpPt = m_TkbkInfoCntnrPt.get( VdoOtptStrmIdx ); //这里不用加线程锁，因为媒体处理线程只会递增对讲信息容器的大小，且递增的时候只有一次赋值操作，且视频输出流索引不可能会越界。

		//从容器或自适应抖动缓冲器取出视频输出帧。
		switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
		{
			case 0: //如果要使用容器。
			{
				if( !p_TkbkInfoTmpPt.m_RecvVdoOtptFrmCntnrPt.isEmpty() ) //如果接收视频输出帧容器不为空。
				{
					p_VdoOtptFrmPt = p_TkbkInfoTmpPt.m_RecvVdoOtptFrmCntnrPt.poll(); //从接收视频输出帧容器中取出并删除第一个帧。
					p_VdoOtptFrmLen = p_VdoOtptFrmPt.length;

					if( p_VdoOtptFrmLen != 0 ) //如果视频输出帧为有图像活动。
					{
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + VdoOtptStrmIdx + "：从接收视频输出帧容器取出有图像活动的视频输出帧。长度：" + p_VdoOtptFrmLen + "。" );
					}
					else //如果视频输出帧为无图像活动。
					{
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + VdoOtptStrmIdx + "：从接收视频输出帧容器取出无图像活动的视频输出帧。长度：" + p_VdoOtptFrmLen + "。" );
					}
				}
				break;
			}
			case 1: //如果要使用自适应抖动缓冲器。
			{
				p_TkbkInfoTmpPt.m_VAjbPt.GetBufFrmCnt( p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_CurHaveBufFrmCntPt, p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_MinNeedBufFrmCntPt, p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_MaxNeedBufFrmCntPt, p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_CurNeedBufFrmCntPt, 1, null );

				if( p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_CurHaveBufFrmCntPt.m_Val != 0 ) //如果视频自适应抖动缓冲器不为空。
				{
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + VdoOtptStrmIdx + "：视频自适应抖动缓冲器：帧：" + p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_MaxNeedBufFrmCntPt.m_Val + "，当前需帧：" + p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_CurNeedBufFrmCntPt.m_Val + "。" );

					//从视频自适应抖动缓冲器取出视频输出帧。
					p_TkbkInfoTmpPt.m_VAjbPt.GetByteFrm( SystemClock.uptimeMillis(), p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_TmpHTIntPt, p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_TmpBytePt, 0, p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_TmpBytePt.length, p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_TmpHTLongPt, 1, null );
					p_VdoOtptFrmTimeStamp = p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_TmpHTIntPt.m_Val;
					p_VdoOtptFrmPt = p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_TmpBytePt;
					p_VdoOtptFrmLen = p_TkbkInfoTmpPt.m_VdoOtptTmpVarPt.m_TmpHTLongPt.m_Val;

					if( p_VdoOtptFrmLen > 0 ) //如果视频输出帧为有图像活动。
					{
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + VdoOtptStrmIdx + "：从视频自适应抖动缓冲器取出有图像活动的视频输出帧。视频输出帧时间戳：" + p_VdoOtptFrmTimeStamp + "。长度：" + p_VdoOtptFrmLen + "。" );
					}
					else //如果视频输出帧为无图像活动。
					{
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + VdoOtptStrmIdx + "：从视频自适应抖动缓冲器取出无图像活动的视频输出帧。视频输出帧时间戳：" + p_VdoOtptFrmTimeStamp + "。长度：" + p_VdoOtptFrmLen + "。" );
					}
				}

				break;
			}
		}

		//写入视频输出帧。
		if( p_VdoOtptFrmLen > 0 ) //如果视频输出帧为有图像活动。
		{
			if( VdoOtptYu12SrcFrmPt != null ) //如果要使用视频输出Yu12格式原始帧。
			{
				//读取视频输出帧宽度。
				VdoOtptYu12SrcFrmWidthPt.m_Val = ( p_VdoOtptFrmPt[ 0 ] & 0xFF ) + ( ( p_VdoOtptFrmPt[ 1 ] & 0xFF ) << 8 ) + ( ( p_VdoOtptFrmPt[ 2 ] & 0xFF ) << 16 ) + ( ( p_VdoOtptFrmPt[ 3 ] & 0xFF ) << 24 );
				//读取视频输出帧高度。
				VdoOtptYu12SrcFrmHeightPt.m_Val = ( p_VdoOtptFrmPt[ 4 ] & 0xFF ) + ( ( p_VdoOtptFrmPt[ 5 ] & 0xFF ) << 8 ) + ( ( p_VdoOtptFrmPt[ 6 ] & 0xFF ) << 16 ) + ( ( p_VdoOtptFrmPt[ 7 ] & 0xFF ) << 24 );

				if( p_VdoOtptFrmLen - 4 - 4 != ( ( long ) VdoOtptYu12SrcFrmWidthPt.m_Val * VdoOtptYu12SrcFrmHeightPt.m_Val * 3 / 2 ) )
				{
					VdoOtptYu12SrcFrmWidthPt.m_Val = 0;
					VdoOtptYu12SrcFrmHeightPt.m_Val = 0;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + VdoOtptStrmIdx + "：视频输出帧的长度不等于Yu12格式的长度。视频输出帧：" + ( p_VdoOtptFrmLen - 4 - 4 ) + "，Yu12格式：" + ( VdoOtptYu12SrcFrmWidthPt.m_Val * VdoOtptYu12SrcFrmHeightPt.m_Val * 3 / 2 ) + "。" );
					return;
				}

				//写入视频输出Yu12格式原始帧。
				System.arraycopy( p_VdoOtptFrmPt, 4 + 4, VdoOtptYu12SrcFrmPt, 0, ( int )( p_VdoOtptFrmLen - 4 - 4 ) );
			}
			else //如果要使用视频输出已编码格式原始帧。
			{
				if( p_VdoOtptFrmLen > VdoOtptEncdSrcFrmSzByt )
				{
					VdoOtptEncdSrcFrmLenBytPt.m_Val = 0;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "客户端媒体处理线程：对讲客户端：对讲索引" + VdoOtptStrmIdx + "：视频输出帧的长度已超过已编码格式的长度。视频输出帧：" + p_VdoOtptFrmLen + "，已编码格式：" + VdoOtptEncdSrcFrmSzByt + "。" );
					return;
				}

				//写入视频输出已编码格式原始帧。
				System.arraycopy( p_VdoOtptFrmPt, 0, VdoOtptEncdSrcFrmPt, 0, ( int )( p_VdoOtptFrmLen ) );
				VdoOtptEncdSrcFrmLenBytPt.m_Val = p_VdoOtptFrmLen;
			}
		}
		else if( p_VdoOtptFrmLen == 0 ) //如果视频输出帧为无图像活动。
		{
			if( VdoOtptYu12SrcFrmPt != null ) //如果要使用视频输出Yu12格式原始帧。
			{

			}
			else //如果要使用视频输出已编码格式原始帧。
			{

			}
		}
	}
}
