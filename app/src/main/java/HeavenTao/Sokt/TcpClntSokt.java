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
    public void finalize()
    {
        Dstoy( ( short )-1, null );
    }

    //创建并初始化本端TCP协议客户端套接字，并连接已监听的远端TCP协议服务端套接字。
    public int Init( int RmtLclNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, String LclNodeNamePt, String LclNodeSrvcPt, short TimeOutMsec, VarStr ErrInfoVarStrPt )
    {
        if( m_TcpClntSoktPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( TcpClntSoktInit( p_WebRtcNsPt, RmtLclNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, LclNodeNamePt, LclNodeSrvcPt, TimeOutMsec, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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

    //获取已连接的本端TCP协议客户端套接字绑定的本地节点地址和端口。
    public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktGetLclAddr( m_TcpClntSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //获取已连接的本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远程节点地址和端口。
    public int GetRmtAddr( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktGetRmtAddr( m_TcpClntSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public int SetNoDelay( int IsNoDelay, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktSetNoDelay( m_TcpClntSoktPt, IsNoDelay, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //获取已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public int GetNoDelay( HTInt IsNoDelayPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktGetNoDelay( m_TcpClntSoktPt, IsNoDelayPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //设置本端TCP协议客户端套接字的发送缓冲区内存大小。
    public int SetSendBufSz( long SendBufSz, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktSetSendBufSz( m_TcpClntSoktPt, SendBufSz, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的发送缓冲区内存大小。
    public int GetSendBufSz( HTLong SendBufSzPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktGetSendBufSz( m_TcpClntSoktPt, SendBufSzPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //设置本端TCP协议客户端套接字的接收缓冲区内存大小。
    public int SetRecvBufSz( long RecvBufSz, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktSetRecvBufSz( m_TcpClntSoktPt, RecvBufSz, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的接收缓冲区内存大小。
    public int GetRecvBufSz( HTLong RecvBufSzPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktGetRecvBufSz( m_TcpClntSoktPt, RecvBufSzPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的接收缓冲区数据长度。
    public int GetRecvBufLen( HTLong RecvBufLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktGetRecvBufLen( m_TcpClntSoktPt, RecvBufLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //用已连接的本端TCP协议客户端套接字发送一个数据包到连接的远端TCP协议客户端套接字。
    public int SendPkt( byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktSendPkt( m_TcpClntSoktPt, DataBufPt, DataBufLen, TimeOutMsec, Times, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //用已连接的本端TCP协议客户端套接字接收一个连接的远端TCP协议客户端套接字发送的数据包。
    public int RecvPkt( byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpClntSoktRecvPkt( m_TcpClntSoktPt, DataBufPt, DataBufSz, DataBufLenPt, TimeOutMsec, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //关闭并销毁已创建的本端TCP协议客户端套接字。
    public int Dstoy( short TimeOutSec, VarStr ErrInfoVarStrPt )
    {
        if( m_TcpClntSoktPt != 0 )
        {
            if( TcpClntSoktDstoy( m_TcpClntSoktPt, TimeOutSec, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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
    public native int TcpClntSoktInit( HTLong TcpClntSoktPt, int RmtLclNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, String LclNodeNamePt, String LclNodeSrvcPt, short TimeOutMsec, long ErrInfoVarStrPt );

    //获取已连接的本端TCP协议客户端套接字绑定的本地节点地址和端口。
    public native int TcpClntSoktGetLclAddr( long TcpClntSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //获取已连接的本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远程节点地址和端口。
    public native int TcpClntSoktGetRmtAddr( long TcpClntSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public native int TcpClntSoktSetNoDelay( long TcpClntSoktPt, int IsNoDelay, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //获取已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public native int TcpClntSoktGetNoDelay( long TcpClntSoktPt, HTInt IsNoDelayPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //设置本端TCP协议客户端套接字的发送缓冲区内存大小。
    public native int TcpClntSoktSetSendBufSz( long TcpClntSoktPt, long SendBufSz, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //获取本端TCP协议客户端套接字的发送缓冲区内存大小。
    public native int TcpClntSoktGetSendBufSz( long TcpClntSoktPt, HTLong SendBufSzPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //设置本端TCP协议客户端套接字的接收缓冲区内存大小。
    public native int TcpClntSoktSetRecvBufSz( long TcpClntSoktPt, long RecvBufSz, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //获取本端TCP协议客户端套接字的接收缓冲区内存大小。
    public native int TcpClntSoktGetRecvBufSz( long TcpClntSoktPt, HTLong RecvBufSzPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //获取本端TCP协议客户端套接字的接收缓冲区数据长度。
    public native int TcpClntSoktGetRecvBufLen( long TcpClntSoktPt, HTLong RecvBufLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //用已连接的本端TCP协议客户端套接字发送一个数据包到连接的远端TCP协议客户端套接字。
    public native int TcpClntSoktSendPkt( long TcpClntSoktPt, byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //用已连接的本端TCP协议客户端套接字开始接收连接的远端TCP协议客户端套接字发送的一个数据包。
    public native int TcpClntSoktRecvPkt( long TcpClntSoktPt, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //关闭并销毁已创建的本端TCP协议客户端套接字。
    public native int TcpClntSoktDstoy( long TcpClntSoktPt, short TimeOutSec, long ErrInfoVarStrPt );
}