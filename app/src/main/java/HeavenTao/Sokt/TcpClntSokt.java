package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端TCP协议客户端套接字。
public class TcpClntSokt
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "DataStruct" ); //加载libDataStruct.so。
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

    public static final int TcpCnctStsWait = 0; //连接状态：等待远端接受连接。
    public static final int TcpCnctStsCnct = 1; //连接状态：已连接。
    public static final int TcpCnctStsFail = 2; //连接状态：连接失败。

    //创建并初始化本端TCP协议客户端套接字，并连接远端TCP协议服务端套接字。
    public int Init( int RmtLclNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, String LclNodeNamePt, String LclNodeSrvcPt, Vstr ErrInfoVstrPt )
    {
        if( m_TcpClntSoktPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( TcpClntInit( p_WebRtcNsPt, RmtLclNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, LclNodeNamePt, LclNodeSrvcPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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

    public int WaitCnct( short TmotMsec, HTInt CnctStsPt, Vstr ErrInfoVstrPt )
    {
        return TcpClntWaitCnct( m_TcpClntSoktPt, TmotMsec, CnctStsPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //本端TCP协议客户端套接字的互斥锁加锁。
    public int Locked( Vstr ErrInfoVstrPt )
    {
        return TcpClntLocked( m_TcpClntSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //本端TCP协议客户端套接字的互斥锁解锁。
    public int Unlock( Vstr ErrInfoVstrPt )
    {
        return TcpClntUnlock( m_TcpClntSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //获取本端TCP协议客户端套接字绑定的本端节点地址和端口。
    public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntGetLclAddr( m_TcpClntSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远端节点地址和端口。
    public int GetRmtAddr( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntGetRmtAddr( m_TcpClntSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端TCP协议客户端套接字的Nagle延迟算法状态。
    public int SetNoDelay( int IsNoDelay, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSetNoDelay( m_TcpClntSoktPt, IsNoDelay, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的Nagle延迟算法状态。
    public int GetNoDelay( HTInt IsNoDelayPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntGetNoDelay( m_TcpClntSoktPt, IsNoDelayPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端TCP协议客户端套接字的发送缓冲区大小。
    public int SetSendBufSz( long SendBufSz, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSetSendBufSz( m_TcpClntSoktPt, SendBufSz, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的发送缓冲区大小。
    public int GetSendBufSz( HTLong SendBufSzPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntGetSendBufSz( m_TcpClntSoktPt, SendBufSzPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端TCP协议客户端套接字的接收缓冲区大小。
    public int SetRecvBufSz( long RecvBufSz, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSetRecvBufSz( m_TcpClntSoktPt, RecvBufSz, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的接收缓冲区大小。
    public int GetRecvBufSz( HTLong RecvBufSzPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntGetRecvBufSz( m_TcpClntSoktPt, RecvBufSzPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的接收缓冲区长度。
    public int GetRecvBufLen( HTLong RecvBufLenPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntGetRecvBufLen( m_TcpClntSoktPt, RecvBufLenPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端TCP协议客户端套接字的保活机制。
    public int SetKeepAlive( int IsUseKeepAlive, int KeepIdle, int KeepIntvl, int KeepCnt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSetKeepAlive( m_TcpClntSoktPt, IsUseKeepAlive, KeepIdle, KeepIntvl, KeepCnt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端TCP协议客户端套接字的保活机制。
    public int GetKeepAlive( HTInt IsUseKeepAlivePt, HTInt KeepIdlePt, HTInt KeepIntvlPt, HTInt KeepCntPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntGetKeepAlive( m_TcpClntSoktPt, IsUseKeepAlivePt, KeepIdlePt, KeepIntvlPt, KeepCntPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用本端TCP协议客户端套接字发送原始数据包到连接的远端TCP协议客户端套接字。
    public int SendPkt( byte PktPt[], long PktLenByt, short TmotMsec, int Times, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSendPkt( m_TcpClntSoktPt, PktPt, PktLenByt, TmotMsec, Times, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //用本端TCP协议客户端套接字接收连接的远端TCP协议客户端套接字发送的原始数据包。
    public int RecvPkt( byte PktPt[], long PktSzByt, HTLong PktLenBytPt, short TmotMsec, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntRecvPkt( m_TcpClntSoktPt, PktPt, PktSzByt, PktLenBytPt, TmotMsec, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用本端TCP协议客户端套接字发送高级数据包到连接的远端TCP协议客户端套接字。
    public int SendApkt( byte PktPt[], long PktLenByt, short TmotMsec, int Times, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntSendApkt( m_TcpClntSoktPt, PktPt, PktLenByt, TmotMsec, Times, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //用本端TCP协议客户端套接字接收连接的远端TCP协议客户端套接字发送的高级数据包。
    public int RecvApkt( byte PktPt[], long PktSzByt, HTLong PktLenBytPt, short TmotMsec, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return TcpClntRecvApkt( m_TcpClntSoktPt, PktPt, PktSzByt, PktLenBytPt, TmotMsec, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //关闭并销毁本端TCP协议客户端套接字。
    public int Dstoy( short TmotSec, Vstr ErrInfoVstrPt )
    {
        if( m_TcpClntSoktPt != 0 )
        {
            if( TcpClntDstoy( m_TcpClntSoktPt, TmotSec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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

    //创建并初始化本端TCP协议客户端套接字，并连接远端TCP协议服务端套接字。
    private native int TcpClntInit( HTLong TcpClntSoktPt, int RmtLclNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, String LclNodeNamePt, String LclNodeSrvcPt, long ErrInfoVstrPt );

    //等待本端TCP协议客户端套接字连接远端TCP协议服务端套接字是否成功。
    private native int TcpClntWaitCnct( long TcpClntSoktPt, short TmotMsec, HTInt CnctStsPt, long ErrInfoVstrPt );

    //本端TCP协议客户端套接字的互斥锁加锁。
    private native int TcpClntLocked( long TcpClntSoktPt, long ErrInfoVstrPt );
    //本端TCP协议客户端套接字的互斥锁解锁。
    private native int TcpClntUnlock( long TcpClntSoktPt, long ErrInfoVstrPt );

    //获取本端TCP协议客户端套接字绑定的本端节点地址和端口。
    private native int TcpClntGetLclAddr( long TcpClntSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远端节点地址和端口。
    private native int TcpClntGetRmtAddr( long TcpClntSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLock, long ErrInfoVstrPt );

    //设置本端TCP协议客户端套接字的Nagle延迟算法状态。
    private native int TcpClntSetNoDelay( long TcpClntSoktPt, int IsNoDelay, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端TCP协议客户端套接字的Nagle延迟算法状态。
    private native int TcpClntGetNoDelay( long TcpClntSoktPt, HTInt IsNoDelayPt, int IsAutoLock, long ErrInfoVstrPt );

    //设置本端TCP协议客户端套接字的发送缓冲区大小。
    private native int TcpClntSetSendBufSz( long TcpClntSoktPt, long SendBufSzByt, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端TCP协议客户端套接字的发送缓冲区大小。
    private native int TcpClntGetSendBufSz( long TcpClntSoktPt, HTLong SendBufSzBytPt, int IsAutoLock, long ErrInfoVstrPt );

    //设置本端TCP协议客户端套接字的接收缓冲区大小。
    private native int TcpClntSetRecvBufSz( long TcpClntSoktPt, long RecvBufSzByt, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端TCP协议客户端套接字的接收缓冲区大小。
    private native int TcpClntGetRecvBufSz( long TcpClntSoktPt, HTLong RecvBufSzBytPt, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端TCP协议客户端套接字的接收缓冲区长度。
    private native int TcpClntGetRecvBufLen( long TcpClntSoktPt, HTLong RecvBufLenBytPt, int IsAutoLock, long ErrInfoVstrPt );

    //设置本端TCP协议客户端套接字的保活机制。
    private native int TcpClntSetKeepAlive( long TcpClntSoktPt, int IsUseKeepAlive, int KeepIdle, int KeepIntvl, int KeepCnt, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端TCP协议客户端套接字的保活机制。
    private native int TcpClntGetKeepAlive( long TcpClntSoktPt, HTInt IsUseKeepAlivePt, HTInt KeepIdlePt, HTInt KeepIntvlPt, HTInt KeepCntPt, int IsAutoLock, long ErrInfoVstrPt );

    //用本端TCP协议客户端套接字发送原始数据包到连接的远端TCP协议客户端套接字。
    private native int TcpClntSendPkt( long TcpClntSoktPt, byte PktPt[], long PktLenByt, short TmotMsec, int Times, int IsAutoLock, long ErrInfoVstrPt );
    //用本端TCP协议客户端套接字接收连接的远端TCP协议客户端套接字发送的原始数据包。
    private native int TcpClntRecvPkt( long TcpClntSoktPt, byte PktPt[], long PktSzByt, HTLong PktLenBytPt, short TmotMsec, int IsAutoLock, long ErrInfoVstrPt );

    //用本端TCP协议客户端套接字发送高级数据包到连接的远端TCP协议客户端套接字。
    private native int TcpClntSendApkt( long TcpClntSoktPt, byte PktPt[], long PktLenByt, short TmotMsec, int Times, int IsAutoLock, long ErrInfoVstrPt );
    //用本端TCP协议客户端套接字接收连接的远端TCP协议客户端套接字发送的高级数据包。
    private native int TcpClntRecvApkt( long TcpClntSoktPt, byte PktPt[], long PktSzByt, HTLong PktLenBytPt, short TmotMsec, int IsAutoLock, long ErrInfoVstrPt );

    //关闭并销毁本端TCP协议客户端套接字。
    private native int TcpClntDstoy( long TcpClntSoktPt, short TmotSec, long ErrInfoVstrPt );
}