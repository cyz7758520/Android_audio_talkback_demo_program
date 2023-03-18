package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端UDP协议套接字。
public class UdpSokt
{
    private long m_UdpSoktPt; //存放本端UDP协议套接字的指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "DataStruct" ); //加载libDataStruct.so。
        System.loadLibrary( "Sokt" ); //加载libSokt.so。
    }

    //构造函数。
    public UdpSokt()
    {
        m_UdpSoktPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy( null );
    }

    //获取本端UDP协议套接字的指针。
    public long GetUdpSoktPt()
    {
        return m_UdpSoktPt;
    }

    //创建并初始化本端UDP协议套接字。
    public int Init( int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, Vstr ErrInfoVstrPt )
    {
        if( m_UdpSoktPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( UdpInit( p_WebRtcNsPt, LclNodeAddrFmly, LclNodeNamePt, LclNodeSrvcPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_UdpSoktPt = p_WebRtcNsPt.m_Val;
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

    //本端UDP协议套接字的互斥锁加锁。
    public int Locked( Vstr ErrInfoVstrPt )
    {
        return UdpLocked( m_UdpSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //本端UDP协议套接字的互斥锁解锁。
    public int Unlock( Vstr ErrInfoVstrPt )
    {
        return UdpUnlock( m_UdpSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用本端UDP协议套接字连接远端UDP协议套接字，已连接的本端UDP协议套接字只能接收连接的远端UDP协议套接字发送的数据包。
    public int Cnct( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpCnct( m_UdpSoktPt, RmtNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //用本端UDP协议套接字断开连接，本端UDP协议套接字将可以接收任何远端的数据包。
    public int Dsct( int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpDsct( m_UdpSoktPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //获取本端UDP协议套接字绑定的本端节点地址和端口。
    public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpGetLclAddr( m_UdpSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端UDP协议套接字连接的远端UDP协议套接字绑定的远端节点地址和端口。
    public int GetRmtAddr( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpGetRmtAddr( m_UdpSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端UDP协议套接字的发送缓冲区大小。
    public int SetSendBufSz( long SendBufSzByt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpSetSendBufSz( m_UdpSoktPt, SendBufSzByt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端UDP协议套接字的发送缓冲区大小。
    public int GetSendBufSz( HTLong SendBufSzBytPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpGetSendBufSz( m_UdpSoktPt, SendBufSzBytPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端UDP协议套接字的接收缓冲区大小。
    public int SetRecvBufSz( long RecvBufSzByt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpSetRecvBufSz( m_UdpSoktPt, RecvBufSzByt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端UDP协议套接字的接收缓冲区大小。
    public int GetRecvBufSz( HTLong RecvBufSzBytPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpGetRecvBufSz( m_UdpSoktPt, RecvBufSzBytPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端UDP协议套接字的接收缓冲区长度。
    public int GetRecvBufLen( HTLong RecvBufLenBytPt, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpGetRecvBufLen( m_UdpSoktPt, RecvBufLenBytPt, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用本端UDP协议套接字发送原始数据包到指定的或连接的远端UDP协议套接字。
    public int SendPkt( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte PktPt[], long PktLenByt, short TmotMsec, int Times, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpSendPkt( m_UdpSoktPt, RmtNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, PktPt, PktLenByt, TmotMsec, Times, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //用本端UDP协议套接字接收远端UDP协议套接字发送的原始数据包。
    public int RecvPkt( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte PktPt[], long PktSzByt, HTLong PktLenBytPt, short TmotMsec, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpRecvPkt( m_UdpSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, PktPt, PktSzByt, PktLenBytPt, TmotMsec, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用本端UDP协议套接字发送高级数据包到指定的或连接的远端UDP协议套接字。
    public int SendApkt( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte PktPt[], long PktLenByt, short TmotMsec, int Times, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpSendApkt( m_UdpSoktPt, RmtNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, PktPt, PktLenByt, TmotMsec, Times, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //用本端UDP协议套接字接收远端UDP协议套接字发送的高级数据包。
    public int RecvApkt( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte PktPt[], long PktSzByt, HTLong PktLenBytPt, short TmotMsec, int IsAutoLock, Vstr ErrInfoVstrPt )
    {
        return UdpRecvApkt( m_UdpSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, PktPt, PktSzByt, PktLenBytPt, TmotMsec, IsAutoLock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //关闭并销毁本端UDP协议套接字。
    public int Dstoy( Vstr ErrInfoVstrPt )
    {
        if( m_UdpSoktPt != 0 )
        {
            if( UdpDstoy( m_UdpSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_UdpSoktPt = 0;
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

    //创建并初始化本端UDP协议套接字。
    private native int UdpInit( HTLong UdpSoktPt, int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, long ErrInfoVstrPt );

    //本端UDP协议套接字的互斥锁加锁。
    private native int UdpLocked( long UdpSoktPt, long ErrInfoVstrPt );
    //本端UDP协议套接字的互斥锁解锁。
    private native int UdpUnlock( long UdpSoktPt, long ErrInfoVstrPt );

    //用本端UDP协议套接字连接远端UDP协议套接字，已连接的本端UDP协议套接字只能接收连接的远端UDP协议套接字发送的数据包。
    private native int UdpCnct( long UdpSoktPt, int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, int IsAutoLock, long ErrInfoVstrPt );
    //用本端UDP协议套接字断开连接，本端UDP协议套接字将可以接收任何远端的数据包。
    private native int UdpDsct( long UdpSoktPt, int IsAutoLock, long ErrInfoVstrPt );

    //获取本端UDP协议套接字绑定的本端节点地址和端口。
    private native int UdpGetLclAddr( long UdpSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端UDP协议套接字连接的远端UDP协议套接字绑定的远端节点地址和端口。
    private native int UdpGetRmtAddr( long UdpSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLock, long ErrInfoVstrPt );

    //设置本端UDP协议套接字的发送缓冲区大小。
    private native int UdpSetSendBufSz( long UdpSoktPt, long SendBufSzByt, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端UDP协议套接字的发送缓冲区大小。
    private native int UdpGetSendBufSz( long UdpSoktPt, HTLong SendBufSzBytPt, int IsAutoLock, long ErrInfoVstrPt );

    //设置本端UDP协议套接字的接收缓冲区大小。
    private native int UdpSetRecvBufSz( long UdpSoktPt, long RecvBufSzByt, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端UDP协议套接字的接收缓冲区大小。
    private native int UdpGetRecvBufSz( long UdpSoktPt, HTLong RecvBufSzBytPt, int IsAutoLock, long ErrInfoVstrPt );
    //获取本端UDP协议套接字的接收缓冲区长度。
    private native int UdpGetRecvBufLen( long UdpSoktPt, HTLong RecvBufLenBytPt, int IsAutoLock, long ErrInfoVstrPt );

    //用本端UDP协议套接字发送原始数据包到指定的或连接的远端UDP协议套接字。
    private native int UdpSendPkt( long UdpSoktPt, int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte PktPt[], long PktLenByt, short TmotMsec, int Times, int IsAutoLock, long ErrInfoVstrPt );
    //用本端UDP协议套接字接收远端UDP协议套接字发送的原始数据包。
    private native int UdpRecvPkt( long UdpSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte PktPt[], long PktSzByt, HTLong PktLenBytPt, short TmotMsec, int IsAutoLock, long ErrInfoVstrPt );

    //用本端UDP协议套接字发送高级数据包到指定的或连接的远端UDP协议套接字。
    private native int UdpSendApkt( long UdpSoktPt, int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte PktPt[], long PktLenByt, short TmotMsec, int Times, int IsAutoLock, long ErrInfoVstrPt );
    //用本端UDP协议套接字接收远端UDP协议套接字发送的高级数据包。
    private native int UdpRecvApkt( long UdpSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte PktPt[], long PktSzByt, HTLong PktLenBytPt, short TmotMsec, int IsAutoLock, long ErrInfoVstrPt );

    //关闭并销毁本端UDP协议套接字。
    private native int UdpDstoy( long UdpSoktPt, long ErrInfoVstrPt );
}