package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端TCP协议服务端套接字。
public class TcpSrvrSokt
{
	static
	{
		System.loadLibrary( "Func" ); //加载libFunc.so。
		System.loadLibrary( "DataStruct" ); //加载libDataStruct.so。
		System.loadLibrary( "Sokt" ); //加载libSokt.so。
	}

	public long m_TcpSrvrSoktPt; //存放本端TCP协议服务端套接字的指针。

	//构造函数。
	public TcpSrvrSokt()
	{
		m_TcpSrvrSoktPt = 0;
	}

	//析构函数。
	protected void finalize()
	{
		Dstoy( null );
	}

	//创建并初始化本端TCP协议服务端套接字。
	public int Init( int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, int MaxWait, int IsReuseAddr, short TcpClntSoktNtwkTmotMsec, Vstr ErrInfoVstrPt )
	{
		if( m_TcpSrvrSoktPt == 0 )
		{
			HTLong p_TcpSrvrSoktPt = new HTLong();
			if( TcpSrvrInit( p_TcpSrvrSoktPt, LclNodeAddrFmly, LclNodeNamePt, LclNodeSrvcPt, MaxWait, IsReuseAddr, TcpClntSoktNtwkTmotMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_TcpSrvrSoktPt = p_TcpSrvrSoktPt.m_Val;
				return 0;
			}
			else
			{
				return -1;
			}
		}
		else
		{
			return 0;
		}
	}
	//关闭并销毁本端TCP协议服务端套接字。
	public int Dstoy( Vstr ErrInfoVstrPt )
	{
		if( m_TcpSrvrSoktPt != 0 )
		{
			if( TcpSrvrDstoy( m_TcpSrvrSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
			{
				m_TcpSrvrSoktPt = 0;
				return 0;
			}
			else
			{
				return -1;
			}
		}
		else
		{
			return 0;
		}
	}

	//本端TCP协议服务端套接字的互斥锁加锁。
	public int Locked( Vstr ErrInfoVstrPt )
	{
		return TcpSrvrLocked( m_TcpSrvrSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}
	//本端TCP协议服务端套接字的互斥锁解锁。
	public int Unlock( Vstr ErrInfoVstrPt )
	{
		return TcpSrvrUnlock( m_TcpSrvrSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//获取本端TCP协议服务端套接字绑定的本端节点地址和端口。
	public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLock, Vstr ErrInfoVstrPt )
	{
		return TcpSrvrGetLclAddr( m_TcpSrvrSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//用本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接。
	public int Acpt( TcpClntSokt TcpClntSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, short TmotMsec, int IsAutoLock, Vstr ErrInfoVstrPt )
	{
		return TcpSrvrAcpt( m_TcpSrvrSoktPt, TcpClntSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, TmotMsec, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
	}

	//创建并初始化本端TCP协议服务端套接字。
	private native int TcpSrvrInit( HTLong TcpSrvrSoktPt, int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, int MaxWait, int IsReuseAddr, short TcpClntSoktNtwkTmotMsec, long ErrInfoVstrPt );
	//关闭并销毁本端TCP协议服务端套接字。
	private native int TcpSrvrDstoy( long TcpSrvrSoktPt, long ErrInfoVstrPt );

	//本端TCP协议服务端套接字的互斥锁加锁。
	private native int TcpSrvrLocked( long TcpSrvrSoktPt, long ErrInfoVstrPt );
	//本端TCP协议服务端套接字的互斥锁解锁。
	private native int TcpSrvrUnlock( long TcpSrvrSoktPt, long ErrInfoVstrPt );

	//获取本端TCP协议服务端套接字绑定的本端节点地址和端口。
	private native int TcpSrvrGetLclAddr( long TcpSrvrSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLock, long ErrInfoVstrPt );

	//用本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接。
	private native int TcpSrvrAcpt( long TcpSrvrSoktPt, TcpClntSokt TcpClntSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, short TmotMsec, int IsAutoLock, long ErrInfoVstrPt );
}