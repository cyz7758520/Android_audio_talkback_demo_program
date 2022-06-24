package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端TCP协议客户端套接字类。
public class TcpClntSokt
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Sokt" ); //加载libSokt.so。
    }

    public long m_TcpClntSoktPt; //存放本端TCP协议客户端套接字的指针。

    //构造函数。
    public TcpClntSokt()
    {
        m_TcpClntSoktPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy( ( short )-1, null );
    }

    //创建并初始化本端TCP协议客户端套接字，并连接已监听的远端TCP协议服务端套接字。
    public int Init( int RmtLclNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, String LclNodeNamePt, String LclNodeSrvcPt, short TimeOutMsec, Vstr ErrInfoVstrPt )
    {
        if( m_TcpClntSoktPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( TcpClntSoktInit( p_WebRtcNsPt, RmtLclNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, LclNodeNamePt, LclNodeSrvcPt, TimeOutMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_TcpClntSoktPt = p_WebRtcNsPt.m_Val;
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

    //获取已连接的本端TCP协议客户端套接字绑定的本端节点地址和端口。
    public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktGetLclAddr( m_TcpClntSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取已连接的本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远端节点地址和端口。
    public int GetRmtAddr( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktGetRmtAddr( m_TcpClntSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public int SetNoDelay( int IsNoDelay, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktSetNoDelay( m_TcpClntSoktPt, IsNoDelay, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public int GetNoDelay( HTInt IsNoDelayPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktGetNoDelay( m_TcpClntSoktPt, IsNoDelayPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端TCP协议客户端套接字的发送缓冲区内存大小。
    public int SetSendBufSz( long SendBufSz, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktSetSendBufSz( m_TcpClntSoktPt, SendBufSz, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的发送缓冲区内存大小。
    public int GetSendBufSz( HTLong SendBufSzPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktGetSendBufSz( m_TcpClntSoktPt, SendBufSzPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端TCP协议客户端套接字的接收缓冲区内存大小。
    public int SetRecvBufSz( long RecvBufSz, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktSetRecvBufSz( m_TcpClntSoktPt, RecvBufSz, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的接收缓冲区内存大小。
    public int GetRecvBufSz( HTLong RecvBufSzPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktGetRecvBufSz( m_TcpClntSoktPt, RecvBufSzPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的接收缓冲区数据长度。
    public int GetRecvBufLen( HTLong RecvBufLenPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktGetRecvBufLen( m_TcpClntSoktPt, RecvBufLenPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用已连接的本端TCP协议客户端套接字发送一个数据包到连接的远端TCP协议客户端套接字。
    public int SendPkt( byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktSendPkt( m_TcpClntSoktPt, DataBufPt, DataBufLen, TimeOutMsec, Times, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //用已连接的本端TCP协议客户端套接字接收一个连接的远端TCP协议客户端套接字发送的数据包。
    public int RecvPkt( byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSoktRecvPkt( m_TcpClntSoktPt, DataBufPt, DataBufSz, DataBufLenPt, TimeOutMsec, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //关闭并销毁已创建的本端TCP协议客户端套接字。
    public int Dstoy( short TimeOutSec, Vstr ErrInfoVstrPt )
    {
        if( m_TcpClntSoktPt != 0 )
        {
            if( TcpClntSoktDstoy( m_TcpClntSoktPt, TimeOutSec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_TcpClntSoktPt = 0;
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

    //创建并初始化本端TCP协议客户端套接字，并连接已监听的远端TCP协议服务端套接字。
    public native int TcpClntSoktInit( HTLong TcpClntSoktPt, int RmtLclNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, String LclNodeNamePt, String LclNodeSrvcPt, short TimeOutMsec, long ErrInfoVstrPt );

    //获取已连接的本端TCP协议客户端套接字绑定的本端节点地址和端口。
    public native int TcpClntSoktGetLclAddr( long TcpClntSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //获取已连接的本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远端节点地址和端口。
    public native int TcpClntSoktGetRmtAddr( long TcpClntSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public native int TcpClntSoktSetNoDelay( long TcpClntSoktPt, int IsNoDelay, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //获取已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public native int TcpClntSoktGetNoDelay( long TcpClntSoktPt, HTInt IsNoDelayPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //设置本端TCP协议客户端套接字的发送缓冲区内存大小。
    public native int TcpClntSoktSetSendBufSz( long TcpClntSoktPt, long SendBufSz, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //获取本端TCP协议客户端套接字的发送缓冲区内存大小。
    public native int TcpClntSoktGetSendBufSz( long TcpClntSoktPt, HTLong SendBufSzPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //设置本端TCP协议客户端套接字的接收缓冲区内存大小。
    public native int TcpClntSoktSetRecvBufSz( long TcpClntSoktPt, long RecvBufSz, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //获取本端TCP协议客户端套接字的接收缓冲区内存大小。
    public native int TcpClntSoktGetRecvBufSz( long TcpClntSoktPt, HTLong RecvBufSzPt, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //获取本端TCP协议客户端套接字的接收缓冲区数据长度。
    public native int TcpClntSoktGetRecvBufLen( long TcpClntSoktPt, HTLong RecvBufLenPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //用已连接的本端TCP协议客户端套接字发送一个数据包到连接的远端TCP协议客户端套接字。
    public native int TcpClntSoktSendPkt( long TcpClntSoktPt, byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //用已连接的本端TCP协议客户端套接字开始接收连接的远端TCP协议客户端套接字发送的一个数据包。
    public native int TcpClntSoktRecvPkt( long TcpClntSoktPt, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //关闭并销毁已创建的本端TCP协议客户端套接字。
    public native int TcpClntSoktDstoy( long TcpClntSoktPt, short TimeOutSec, long ErrInfoVstrPt );
}