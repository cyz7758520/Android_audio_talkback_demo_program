package HeavenTao.Media;

import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

public class BdctClnt //广播客户端。
{
	public static String m_CurClsNameStrPt = "BdctClnt"; //存放当前类名称字符串。

	ClntMediaPocsThrd m_ClntMediaPocsThrdPt; //存放客户端媒体处理线程的指针。

	public int m_IsInit; //存放是否初始化广播客户端，为0表示未初始化，为非0表示已初始化。
	public int m_CnctNumIsDecr; //存放连接序号是否递减，为0表示不递减，为非0表示要递减。

	public class CnctInfo //存放连接信息。
	{
		public int m_Idx; //存放索引，从0开始，连接信息容器的唯一索引，连接中途不会改变。
		public int m_IsInit; //存放是否初始化，为0表示未初始化，为非0表示已初始化。
		public int m_Num; //存放序号，从0开始，随着前面的连接销毁而递减。

		public int m_IsTcpOrAudpPrtcl; //存放是否是Tcp或Udp协议，为0表示Tcp协议，为1表示高级Udp协议。
		public String m_RmtNodeNameStrPt; //存放远端套接字绑定的远端节点名称字符串的指针，
		public String m_RmtNodeSrvcStrPt; //存放远端套接字绑定的远端节点服务字符串的指针，
		public TcpClntSokt m_TcpClntSoktPt; //存放本端Tcp协议客户端套接字的指针。
		public long m_AudpClntCnctIdx; //存放本端高级Udp协议客户端连接索引。
		public int m_CurCnctSts; //存放当前连接状态，为[-m_MaxCnctTimes,0]表示等待远端接受连接。
		public int m_IsRqstDstoy; //存放是否请求销毁，为0表示未请求，为1表示已请求。

		public int m_MyTkbkIdx; //存放我的对讲索引。
		public int m_IsRecvExitPkt; //存放是否接收退出包，为0表示未接收，为1表示已接收。
	}
	ArrayList< CnctInfo > m_CnctInfoCntnrPt = new ArrayList<>(); //存放连接信息容器的指针。
	public int m_CnctInfoCurMaxNum; //存放连接信息的当前最大序号。

	public int m_LclTkbkMode; //存放本端对讲模式。
	public int m_LastSendAdoInptFrmIsAct; //存放最后发送的音频输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
	public int m_LastSendAdoInptFrmTimeStamp; //存放最后发送音频输入帧的时间戳。
	public int m_LastSendVdoInptFrmTimeStamp; //存放最后发送视频输入帧的时间戳。

	int m_IsVibrate; //存放广播客户端是否已振动，用于提醒用户在第一次连接成功后可以开始广播，为1表示已振动，为0表示未振动。

	//广播客户端初始化。
	public int Init( int CnctNumIsDecr, int LclTkbkMode )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			m_IsInit = 1; //设置已初始化广播客户端。
			m_CnctNumIsDecr = CnctNumIsDecr; //设置连接序号是否递减。
			m_LclTkbkMode = LclTkbkMode | ( ClntMediaPocsThrd.TkbkMode.AdoInpt & ClntMediaPocsThrd.TkbkMode.VdoInpt ); //设置本端对讲模式。
			m_IsVibrate = 0; //设置广播客户端未已振动。

			m_ClntMediaPocsThrdPt.UserBdctClntInit(); //调用用户定义的广播客户端初始化函数。

			m_ClntMediaPocsThrdPt.SetTkbkMode( 1, 0 ); //设置对讲模式。

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			Dstoy();
		}
		return p_Rslt;
	}

	//广播客户端销毁。
	public int Dstoy()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		CnctInfo p_CnctInfoTmpPt;

		Out:
		{
			//连接信息全部销毁。
			for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
			{
				p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

				if( p_CnctInfoTmpPt.m_IsInit != 0 )
				{
					CnctInfoDstoy( p_CnctInfoTmpPt );
				}
			}

			m_IsInit = 0; //设置未初始化广播客户端。

			m_CnctInfoCurMaxNum = -1; //设置连接信息的当前最大序号。

			m_ClntMediaPocsThrdPt.SetTkbkMode( 1, 0 ); //设置对讲模式。

			m_ClntMediaPocsThrdPt.UserBdctClntDstoy(); //调用用户定义的广播客户端销毁函数。

			m_ClntMediaPocsThrdPt.IsAutoRqirExit(); //判断是否自动请求退出。在没有广播连接时需要这一步判断。

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return p_Rslt;
	}

	//连接信息初始化。
	public CnctInfo CnctInfoInit( int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		CnctInfo p_CnctInfoTmpPt = null;

		Out:
		{
			if( m_IsInit == 0 ) //如果广播客户端未初始化。
			{
				String p_InfoStrPt = "客户端媒体处理线程：广播客户端：广播客户端未初始化，无法继续连接信息初始化。";
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
				m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
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
			m_CnctInfoCurMaxNum++; //递增连接信息的当前最大序号。
			p_CnctInfoTmpPt.m_Num = m_CnctInfoCurMaxNum; //设置序号。

			p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl = IsTcpOrAudpPrtcl; //设置协议为Tcp协议或高级Udp协议。
			p_CnctInfoTmpPt.m_RmtNodeNameStrPt = RmtNodeNameStrPt; //设置远端套接字绑定的远端节点名称字符串的指针。
			p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt = RmtNodeSrvcStrPt; //设置远端套接字绑定的远端节点服务字符串的指针。
			p_CnctInfoTmpPt.m_TcpClntSoktPt = null; //设置本端Tcp协议客户端套接字的指针。
			p_CnctInfoTmpPt.m_AudpClntCnctIdx = -1; //设置本端高级Udp协议客户端连接索引。
			p_CnctInfoTmpPt.m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Wait; //设置当前连接状态。
			p_CnctInfoTmpPt.m_IsRqstDstoy = 0; //设置是否请求销毁。

			p_CnctInfoTmpPt.m_MyTkbkIdx = -1; //设置我的对讲索引。
			p_CnctInfoTmpPt.m_IsRecvExitPkt = 0; //存放是否接收退出包。

			String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：初始化与远端节点" + ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]的连接成功。";
			if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
			m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );

			m_ClntMediaPocsThrdPt.UserBdctClntCnctInit( p_CnctInfoTmpPt, IsTcpOrAudpPrtcl, RmtNodeNameStrPt, RmtNodeSrvcStrPt ); //调用用户定义的广播客户端连接初始化函数。
			m_ClntMediaPocsThrdPt.UserBdctClntCnctSts( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_CurCnctSts ); //调用用户定义的广播客户端连接状态函数。

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{
			if( p_CnctInfoTmpPt != null )
			{
				CnctInfoDstoy( p_CnctInfoTmpPt );
				p_CnctInfoTmpPt = null;
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
				//发送退出包。
				if( ( CnctInfoPt.m_IsRecvExitPkt == 0 ) && ( CnctInfoPt.m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct ) ) //如果未接收退出包，且当前连接状态为已连接。
				{
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ClntMediaPocsThrd.PktTyp.Exit; //设置退出包。
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte ) CnctInfoPt.m_MyTkbkIdx; //设置对讲索引。
					if( ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 0 ) && ( CnctInfoPt.m_TcpClntSoktPt.SendApkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 2, ( short ) 0, 1, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
						( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( CnctInfoPt.m_AudpClntCnctIdx, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 2, 1, 1, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) )
					{
						String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + CnctInfoPt.m_Idx + "：发送退出包成功。对讲索引：" + CnctInfoPt.m_MyTkbkIdx + "。总长度：2。";
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
						m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
					}
					else
					{
						String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + CnctInfoPt.m_Idx + "：发送退出包失败。对讲索引：" + CnctInfoPt.m_MyTkbkIdx + "。总长度：2。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
						m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
					}
				}

				//销毁本端Tcp协议客户端套接字。
				if( CnctInfoPt.m_TcpClntSoktPt != null )
				{
					CnctInfoPt.m_TcpClntSoktPt.Dstoy( ( short ) -1, null );
					CnctInfoPt.m_TcpClntSoktPt = null;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + CnctInfoPt.m_Idx + "：销毁本端Tcp协议客户端套接字成功。" );
				}

				//销毁本端高级Udp协议客户端连接。
				if( CnctInfoPt.m_AudpClntCnctIdx != -1 )
				{
					m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.ClosCnct( CnctInfoPt.m_AudpClntCnctIdx, null );
					CnctInfoPt.m_AudpClntCnctIdx = -1;
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + CnctInfoPt.m_Idx + "：销毁本端高级Udp协议客户端连接成功。" );
				}

				if( ( CnctInfoPt.m_IsRecvExitPkt == 0 ) && ( CnctInfoPt.m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Tmot ) ) //如果未接收退出包，且当前连接状态为异常断开，就重连。
				{
					CnctInfoPt.m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Wait; //设置当前连接状态。
					m_ClntMediaPocsThrdPt.UserBdctClntCnctSts( CnctInfoPt, CnctInfoPt.m_CurCnctSts ); //调用用户定义的广播客户端连接状态函数。
					CnctInfoPt.m_IsRqstDstoy = 0; //设置未请求销毁。

					CnctInfoPt.m_MyTkbkIdx = -1; //设置我的对讲索引。
					CnctInfoPt.m_IsRecvExitPkt = 0; //设置未接收退出包。

					String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + CnctInfoPt.m_Idx + "：连接异常断开，准备重连。";
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
					m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
				}
				else //如果已接收退出包，或当前连接状态不为异常断开，就销毁。
				{
					//设置当前连接状态。
					if( CnctInfoPt.m_CurCnctSts <= ClntMediaPocsThrd.CnctSts.Wait )
					{
						CnctInfoPt.m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Tmot;
						m_ClntMediaPocsThrdPt.UserBdctClntCnctSts( CnctInfoPt, CnctInfoPt.m_CurCnctSts ); //调用用户定义的广播客户端连接状态函数。
					}
					else if( CnctInfoPt.m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct )
					{
						CnctInfoPt.m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Dsct;
						m_ClntMediaPocsThrdPt.UserBdctClntCnctSts( CnctInfoPt, CnctInfoPt.m_CurCnctSts ); //调用用户定义的广播客户端连接状态函数。
					}

					CnctInfoPt.m_IsInit = 0; //设置连接信息未初始化。

					//递减后面连接信息的序号。
					if( m_CnctNumIsDecr != 0 )
					{
						for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
						{
							p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

							if( ( p_CnctInfoTmpPt.m_IsInit != 0 ) && ( p_CnctInfoTmpPt.m_Num > CnctInfoPt.m_Num ) )
							{
								p_CnctInfoTmpPt.m_Num--; //设置后面连接信息的序号全部递减1。
							}
						}
						m_CnctInfoCurMaxNum--; //递减连接信息的当前最大序号。
					}

					{
						String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + CnctInfoPt.m_Idx + "：已销毁。";
						if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
						m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
					}

					m_ClntMediaPocsThrdPt.UserBdctClntCnctDstoy( CnctInfoPt ); //调用用户定义的广播客户端连接销毁函数。
				}
			}

			//m_ClntMediaPocsThrdPt.IsAutoRqirExit(); //判断是否自动请求退出。这里不需要判断，因为有多少个广播连接就会请求多少次，没有必要，只需要在连接信息全部销毁时请求一次即可。
			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//连接信息发送数据包。
	public int CnctInfoSendPkt( CnctInfo CnctInfoPt, byte PktPt[], long PktLenByt, int Times, int IsRlab, Vstr ErrInfoVstrPt )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			if( ( CnctInfoPt.m_IsInit != 0 ) && ( CnctInfoPt.m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct ) ) //如果连接信息已初始化，且当前连接状态为已连接。
			{
				//发送数据包。
				if( ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 0 ) && ( CnctInfoPt.m_TcpClntSoktPt.SendApkt( PktPt, PktLenByt, ( short ) 0, Times, 0, ErrInfoVstrPt ) == 0 ) ) ||
					( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( CnctInfoPt.m_AudpClntCnctIdx, PktPt, PktLenByt, Times, IsRlab, ErrInfoVstrPt ) == 0 ) ) )
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
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) if( ErrInfoVstrPt != null ) ErrInfoVstrPt.Cpy( "连接信息的指针为空，或当前连接状态不为已连接。" );
				break Out;
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return p_Rslt;
	}

	//连接信息发送对讲模式包。
	public int CnctInfoSendTkbkModePkt( CnctInfo p_CnctInfoPt, int LclTkbkMode )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte )ClntMediaPocsThrd.PktTyp.TkbkMode;
			m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] = ( byte )p_CnctInfoPt.m_MyTkbkIdx;
			m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] = ( byte )LclTkbkMode;
			if( CnctInfoSendPkt( p_CnctInfoPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, 3, 1, 1, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
			{
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoPt.m_Idx + "：发送对讲模式包成功。对讲索引：" + p_CnctInfoPt.m_MyTkbkIdx + "。对讲模式：" + ClntMediaPocsThrd.m_TkbkModeStrArrPt[ LclTkbkMode ] + "。" );
			}
			else
			{
				String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoPt.m_Idx + "：发送对讲模式包失败。对讲索引：" + p_CnctInfoPt.m_MyTkbkIdx + "。对讲模式：" + ClntMediaPocsThrd.m_TkbkModeStrArrPt[ LclTkbkMode ] + "。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
				m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return p_Rslt;
	}

	//所有连接发送音频视频数据包。
	public void AllCnctSendAdoVdoPkt( byte PktPt[], long PktLenByt, int Times, int IsRlab )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		CnctInfo p_CnctInfoTmpPt;

		Out:
		{
			for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
			{
				p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );
				if( ( p_CnctInfoTmpPt.m_IsInit != 0 ) &&
					( p_CnctInfoTmpPt.m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct ) &&
					( p_CnctInfoTmpPt.m_MyTkbkIdx != -1 ) )
				{
					PktPt[ 1 ] = ( byte ) p_CnctInfoTmpPt.m_MyTkbkIdx; //设置对讲索引。

					//发送数据包。
					if( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) p_CnctInfoTmpPt.m_TcpClntSoktPt.SendApkt( PktPt, PktLenByt, ( short ) 0, Times, 0, null );
					else m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, PktPt, PktLenByt, Times, IsRlab, null );
				}
			}

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return;
	}

	//连接处理，包括连接服务端、接收数据包、删除连接信息。
	public void CnctPocs()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		CnctInfo p_CnctInfoTmpPt;
		int p_TmpInt;

		Out:
		{
			//遍历连接信息容器。
			for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
			{
				p_CnctInfoTmpPt = m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

				if( p_CnctInfoTmpPt.m_IsInit != 0 )
				{
					//用本端客户端套接字连接远端服务端套接字。
					if( ( p_CnctInfoTmpPt.m_IsRqstDstoy == 0 ) && ( p_CnctInfoTmpPt.m_CurCnctSts <= ClntMediaPocsThrd.CnctSts.Wait ) ) //如果该连接未请求销毁，且当前连接状态为等待远端接受连接。
					{
						int p_RmtNodeAddrFamly; //存放远端节点的地址族，为4表示IPv4，为6表示IPv6，为0表示自动选择。

						{ //设置远端节点的地址族。
							try
							{
								InetAddress inetAddress = InetAddress.getByName( p_CnctInfoTmpPt.m_RmtNodeNameStrPt );
								if( inetAddress.getAddress().length == 4 ) p_RmtNodeAddrFamly = 4;
								else p_RmtNodeAddrFamly = 6;
							}
							catch( UnknownHostException e )
							{
								p_RmtNodeAddrFamly = 0;
							}
						}

						if( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) //用本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字。
						{
							int p_PocsRslt = -1; //存放本处理段的执行结果，为0表示成功，为非0表示失败。

							TcpClntSoktCnctOut:
							{
								if( p_CnctInfoTmpPt.m_TcpClntSoktPt == null ) //如果未初始化本端Tcp协议客户端套接字。
								{
									if( -p_CnctInfoTmpPt.m_CurCnctSts >= m_ClntMediaPocsThrdPt.m_MaxCnctTimes ) //如果未达到最大连接次数。
									{
										p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。这里只设置请求销毁，不设置当前连接状态，因为在连接信息销毁函数里要根据当前连接状态判断是否重连。

										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：已达到最大连接次数，中断连接。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break TcpClntSoktCnctOut;
									}

									p_CnctInfoTmpPt.m_CurCnctSts--; //递增当前连接次数。
									m_ClntMediaPocsThrdPt.UserBdctClntCnctSts( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_CurCnctSts ); //调用用户定义的广播客户端连接状态函数。

									{
										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：开始第 " + -p_CnctInfoTmpPt.m_CurCnctSts + " 次连接。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									}

									p_CnctInfoTmpPt.m_TcpClntSoktPt = new TcpClntSokt();
									if( p_CnctInfoTmpPt.m_TcpClntSoktPt.Init( p_RmtNodeAddrFamly, p_CnctInfoTmpPt.m_RmtNodeNameStrPt, p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt, null, null, ( short )5000, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字失败。
									{
										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break TcpClntSoktCnctOut;
									}
								}

								if( p_CnctInfoTmpPt.m_TcpClntSoktPt.WaitCnct( ( short ) 0, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //如果等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字成功。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == TcpClntSokt.TcpCnctStsWait ) //如果等待远端接受连接。
									{
										//继续等待本端本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字。
									}
									else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == TcpClntSokt.TcpCnctStsCnct ) //如果连接成功。
									{
										if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetNoDelay( 1, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。
										{
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break TcpClntSoktCnctOut;
										}

										if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetSendBufSz( 1024 * 1024, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
										{
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：设置本端Tcp协议客户端套接字的发送缓冲区大小失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break TcpClntSoktCnctOut;
										}

										if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetRecvBufSz( 1024 * 1024 * 3, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
										{
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：设置本端Tcp协议客户端套接字的接收缓冲区大小失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break TcpClntSoktCnctOut;
										}

										if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetKeepAlive( 1, 1, 1, 5, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
										{
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：设置本端Tcp协议客户端套接字的保活机制失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break TcpClntSoktCnctOut;
										}

										if( p_CnctInfoTmpPt.m_TcpClntSoktPt.GetLclAddr( null, m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodeAddrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodePortPt, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
										{
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：获取本端Tcp协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break TcpClntSoktCnctOut;
										}

										if( p_CnctInfoTmpPt.m_TcpClntSoktPt.GetRmtAddr( null, m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodeAddrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodePortPt, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
										{
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：获取本端Tcp协议客户端套接字连接的远端Tcp协议客户端套接字绑定的远程节点地址和端口失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break TcpClntSoktCnctOut;
										}

										p_CnctInfoTmpPt.m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Cnct; //设置当前连接状态为已连接。
										m_ClntMediaPocsThrdPt.UserBdctClntCnctSts( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_CurCnctSts ); //调用用户定义的广播客户端连接状态函数。

										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：初始化本端Tcp协议客户端套接字[" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodePortPt.m_Val + "]，并连接远端Tcp协议服务端套接字[" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodePortPt.m_Val + "]成功。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									}
									else //如果连接失败。
									{
										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：连接失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break TcpClntSoktCnctOut;
									}
								}
								else //如果等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字失败。
								{
									String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
									if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
									m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									break TcpClntSoktCnctOut;
								}

								p_PocsRslt = 0; //设置本处理段执行成功。
							}

							if( p_PocsRslt != 0 ) //如果本处理段执行失败。
							{
								if( p_CnctInfoTmpPt.m_TcpClntSoktPt != null )
								{
									p_CnctInfoTmpPt.m_TcpClntSoktPt.Dstoy( ( short ) -1, null );
									p_CnctInfoTmpPt.m_TcpClntSoktPt = null;
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
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：设置本端高级Udp协议客户端套接字的发送缓冲区大小失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break Out;
										}

										if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.SetRecvBufSz( 1024 * 1024 * 3, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
										{
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：设置本端高级Udp协议客户端套接字的接收缓冲区大小失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break Out;
										}

										if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.GetLclAddr( null, m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodeAddrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodePortPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果获取本端高级Udp协议套接字绑定的本地节点地址和端口失败。
										{
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：获取本端高级Udp协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break AudpClntSoktCnctOut;
										}

										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：初始化本端高级Udp协议客户端套接字[" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_LclNodePortPt.m_Val + "]成功。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									}
									else //如果初始化本端高级Udp协议客户端套接字失败。
									{
										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：初始化本端高级Udp协议客户端套接字[" + null + ":" + null + "]失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break AudpClntSoktCnctOut;
									}
								}

								if( p_CnctInfoTmpPt.m_AudpClntCnctIdx == -1 )
								{
									if( -p_CnctInfoTmpPt.m_CurCnctSts >= m_ClntMediaPocsThrdPt.m_MaxCnctTimes ) //如果未达到最大连接次数。
									{
										p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。这里只设置请求销毁，不设置当前连接状态，因为在连接信息销毁函数里要根据当前连接状态判断是否重连。

										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：已达到最大连接次数，中断连接。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break AudpClntSoktCnctOut;
									}

									p_CnctInfoTmpPt.m_CurCnctSts--; //递增当前连接次数。
									m_ClntMediaPocsThrdPt.UserBdctClntCnctSts( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_CurCnctSts ); //调用用户定义的广播客户端连接状态函数。

									{
										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：开始第 " + -p_CnctInfoTmpPt.m_CurCnctSts + " 次连接。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									}

									if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.Cnct( p_RmtNodeAddrFamly, p_CnctInfoTmpPt.m_RmtNodeNameStrPt, p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字失败。
									{
										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break AudpClntSoktCnctOut;
									}
									p_CnctInfoTmpPt.m_AudpClntCnctIdx = m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val; //设置本端高级Udp协议客户端连接索引。
								}

								if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.WaitCnct( p_CnctInfoTmpPt.m_AudpClntCnctIdx, ( short )0, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //循环等待本端高级Udp协议套接字连接远端成功。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsWait ) //如果等待远端接受连接。
									{
										//重新循环，继续等待本端高级Udp协议套接字连接远端。
									}
									else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsCnct ) //如果连接成功。
									{
										if( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.GetRmtAddr( p_CnctInfoTmpPt.m_AudpClntCnctIdx, null, m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodeAddrPt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodePortPt, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
										{
											String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：获取本端高级Udp协议客户端套接字连接的远端高级Udp协议服务端套接字绑定的远程节点地址和端口失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
											m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
											break AudpClntSoktCnctOut;
										}

										p_CnctInfoTmpPt.m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Cnct; //设置当前连接状态为已连接。
										m_ClntMediaPocsThrdPt.UserBdctClntCnctSts( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_CurCnctSts ); //调用用户定义的广播客户端连接状态函数。

										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_RmtNodePortPt.m_Val + "]成功。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
									}
									else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsTmot ) //如果连接超时。
									{
										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：连接超时。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break AudpClntSoktCnctOut;
									}
									else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsDsct ) //如果连接断开。
									{
										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]失败。原因：连接断开。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										break AudpClntSoktCnctOut;
									}
								}

								p_PocsRslt = 0; //设置本处理段执行成功。
							}

							if( p_PocsRslt != 0 ) //如果本处理段执行失败。
							{
								if( ( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt != null ) && ( p_CnctInfoTmpPt.m_AudpClntCnctIdx != -1 ) )
								{
									m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.ClosCnct( p_CnctInfoTmpPt.m_AudpClntCnctIdx, null );
									p_CnctInfoTmpPt.m_AudpClntCnctIdx = -1;
								}
							}
						}
					}

					//用本端客户端套接字接收远端服务端套接字发送的数据包。
					if( ( p_CnctInfoTmpPt.m_IsRqstDstoy == 0 ) && ( p_CnctInfoTmpPt.m_CurCnctSts == ClntMediaPocsThrd.CnctSts.Cnct ) ) //如果该连接未请求销毁，且当前连接状态为已连接。
					{
						RecvPktOut:
						{
							if( ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmpPt.m_TcpClntSoktPt.RecvApkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt.length, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt, ( short ) 0, 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
								( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( m_ClntMediaPocsThrdPt.m_AudpClntSoktPt.RecvApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt.length, m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt, null, ( short ) 0, m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) )
							{
								if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val != -1 ) //如果用本端套接字接收一个连接的远端套接字发送的数据包成功。
								{
									if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val == 0 ) //如果数据包的数据长度为0。
									{
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收数据包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "，表示没有数据，无法继续接收。" );
										break RecvPktOut;
									}
									else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.TkbkIdx ) //如果是对讲索引包。
									{
										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果对讲模式包的长度小于1 + 1，表示没有对讲索引。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收对讲索引包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲索引，无法继续接收。" );
											break RecvPktOut;
										}

										if( p_CnctInfoTmpPt.m_MyTkbkIdx == -1 ) //如果未设置我的对讲索引。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收我的对讲索引包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。" );

											p_CnctInfoTmpPt.m_MyTkbkIdx = m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ]; //设置我的对讲索引。
											CnctInfoSendTkbkModePkt( p_CnctInfoTmpPt, m_LclTkbkMode ); //发送对讲模式包。
											m_ClntMediaPocsThrdPt.SetTkbkMode( 0, 0 ); //设置对讲模式。
										}
										else
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收其他对讲索引包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。" );
										}

										if( m_IsVibrate == 0 ) //如果广播客户端未振动。
										{
											m_ClntMediaPocsThrdPt.UserVibrate(); //如果是广播客户端的第一个连接，就调用用户定义的振动函数。
											m_IsVibrate = 1; //设置广播客户端已振动。
										}
									}
									else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.TkbkMode ) //如果是对讲模式包。
									{
										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 + 1 ) //如果对讲模式包的长度小于1 + 1 + 1，表示没有对讲索引和对讲模式。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收对讲模式包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1 + 1，表示没有对讲索引和对讲模式，无法继续接收。" );
											break RecvPktOut;
										}
										if( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] < ( byte ) ClntMediaPocsThrd.TkbkMode.None ) || ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] >= ( byte ) ClntMediaPocsThrd.TkbkMode.NoChg ) )
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收对讲模式包。对讲模式为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] + "不正确，无法继续接收。" );
											break RecvPktOut;
										}

										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收对讲模式包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。对讲模式：" + ClntMediaPocsThrd.m_TkbkModeStrArrPt[ m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] ] + "。" );
									}
									else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.AdoFrm ) //如果是音频输出帧包。
									{
										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 + 4 ) //如果音频输出帧包的长度小于1 + 1 + 4，表示没有对讲索引和音频输出帧时间戳。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收音频输出帧包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1 + 4，表示没有对讲索引和音频输出帧时间戳，无法继续接收。" );
											break RecvPktOut;
										}

										//读取音频输出帧时间戳。
										p_TmpInt = ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 8 ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 16 ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] & 0xFF ) << 24 );

										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该音频输出帧为有语音活动。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收有语音活动的音频输出帧包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
										}
										else //如果该音频输出帧为无语音活动。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收无语音活动的音频输出帧包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。音频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
										}
									}
									else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.VdoFrm ) //如果是视频输出帧包。
									{
										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 + 4 ) //如果视频输出帧包的长度小于1 + 1 + 4，表示没有对讲索引和视频输出帧时间戳。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收视频输出帧包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1 + 4，表示没有对讲索引和视频输出帧时间戳，无法继续接收。" );
											break RecvPktOut;
										}

										//读取视频输出帧时间戳。
										p_TmpInt = ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 8 ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 16 ) + ( ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] & 0xFF ) << 24 );

										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val > 1 + 1 + 4 ) //如果该视频输出帧为有图像活动。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收有图像活动的视频输出帧包。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。类型：" + ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。" );
										}
										else //如果该视频输出帧为无图像活动。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收无图像活动的视频输出帧包。视频输出帧时间戳：" + p_TmpInt + "。总长度：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "。" );
										}
									}
									else if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] == ( byte ) ClntMediaPocsThrd.PktTyp.Exit ) //如果是退出包。
									{
										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果退出包的长度小于1 + 1，表示没有对讲索引。
										{
											if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收退出包。长度为" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲索引，无法继续接收。" );
											break RecvPktOut;
										}

										String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：接收退出包。对讲索引：" + m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] + "。";
										if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
										m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
										if( m_ClntMediaPocsThrdPt.m_IsShowToast != 0 ) m_ClntMediaPocsThrdPt.UserShowToast( p_InfoStrPt );

										if( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 1 ] == p_CnctInfoTmpPt.m_MyTkbkIdx ) //如果对讲索引是我的对讲索引。
										{
											p_CnctInfoTmpPt.m_IsRecvExitPkt = 1; //设置已接收退出包。
											p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。
										}
									}
								} //如果用本端客户端套接字接收远端服务端套接字发送的数据包超时，就重新接收。
							}
							else //如果用本端客户端套接字接收远端服务端套接字发送的数据包失败。
							{
								String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_CnctInfoTmpPt.m_Idx + "：用本端客户端套接字接收远端服务端套接字发送的数据包失败。原因：" + m_ClntMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
								if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
								m_ClntMediaPocsThrdPt.UserShowLog( p_InfoStrPt );

								p_CnctInfoTmpPt.m_CurCnctSts = ClntMediaPocsThrd.CnctSts.Tmot; //设置当前连接状态为异常断开。
								m_ClntMediaPocsThrdPt.UserBdctClntCnctSts( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_CurCnctSts ); //调用用户定义的广播客户端连接状态函数。
								p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。
								break RecvPktOut;
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
		if( ( ( m_LclTkbkMode & ClntMediaPocsThrd.TkbkMode.AdoInpt ) != 0 ) && ( AdoInptPcmSrcFrmPt != null ) ) //如果本端对讲模式有音频输入，且有音频输入Pcm格式原始帧。
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
				//设置音频输入帧时间戳。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

				AllCnctSendAdoVdoPkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, p_PktLenByt, 1, 0 );
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：发送有语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "。总长度：" + p_PktLenByt + "。" );

				m_LastSendAdoInptFrmIsAct = 1; //设置最后发送的音频输入帧有语音活动。
			}
			else //如果本次音频输入帧为无语音活动。
			{
				if( m_LastSendAdoInptFrmIsAct != 0 ) //如果最后发送的音频输入帧为有语音活动，就发送。
				{
					m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

					//设置数据包类型为音频输入帧包。
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 0 ] = ( byte ) ClntMediaPocsThrd.PktTyp.AdoFrm;
					//设置音频输入帧时间戳。
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
					m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

					AllCnctSendAdoVdoPkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, p_PktLenByt, 1, 0 );
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：发送无语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "。总长度：" + p_PktLenByt + "。" );

					m_LastSendAdoInptFrmIsAct = 0; //设置最后发送的音频输入帧无语音活动。
				}
				else //如果最后发送的音频输入帧为无语音活动，无需发送。
				{
					if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_ClntMediaPocsThrdPt.m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：本次音频输入帧为无语音活动，且最后发送的音频输入帧为无语音活动，无需发送。" );
				}
			}
		}

		//发送视频输入帧。
		if( ( ( m_LclTkbkMode & ClntMediaPocsThrd.TkbkMode.VdoInpt ) != 0 ) && ( VdoInptYu12RsltFrmPt != null ) ) //如果本端对讲模式有视频输入，且有视频输入Yu12格式结果帧。
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
				//设置视频输入帧时间戳。
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( m_LastSendVdoInptFrmTimeStamp & 0xFF );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
				m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 5 ] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

				AllCnctSendAdoVdoPkt( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt, p_PktLenByt, 1, 0 );
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：发送有图像活动的视频输入帧包成功。视频输入帧时间戳：" + m_LastSendVdoInptFrmTimeStamp + "。总长度：" + p_PktLenByt + "。类型：" + ( m_ClntMediaPocsThrdPt.m_ThrdPt.m_TmpBytePt[ 10 ] & 0xff ) + "。" );
			}
			else //如果本次视频输入帧为无图像活动，无需发送。
			{
				if( m_ClntMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "客户端媒体处理线程：广播客户端：本次视频输入帧为无图像活动，无需发送。" );
			}
		}
	}
}
