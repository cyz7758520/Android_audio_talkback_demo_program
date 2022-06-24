package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端UDP协议套接字类。
public class UdpSokt
{
    private long m_UdpSoktPt; //存放本端UDP协议套接字的指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
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

    //创建并初始化已监听的本端UDP协议套接字。
    public int Init( int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, Vstr ErrInfoVstrPt )
    {
        if( m_UdpSoktPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( UdpSoktInit( p_WebRtcNsPt, LclNodeAddrFmly, LclNodeNamePt, LclNodeSrvcPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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

    //用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字，已连接的本端UDP协议套接字只能接收连接的远端UDP协议套接字发送的数据包。
    public int Connect( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktConnect( m_UdpSoktPt, RmtNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。
    public int Disconnect( int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktDisconnect( m_UdpSoktPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //获取已监听的本端UDP协议套接字绑定的本端节点地址和端口。
    public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktGetLclAddr( m_UdpSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取已连接的本端UDP协议套接字连接的远端UDP协议套接字绑定的远端节点地址和端口。
    public int GetRmtAddr( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktGetRmtAddr( m_UdpSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public int SetSendBufSz( long SendBufSz, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktSetSendBufSz( m_UdpSoktPt, SendBufSz, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public int GetSendBufSz( HTLong SendBufSzPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktGetSendBufSz( m_UdpSoktPt, SendBufSzPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public int SetRecvBufSz( long RecvBufSz, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktSetRecvBufSz( m_UdpSoktPt, RecvBufSz, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public int GetRecvBufSz( HTLong RecvBufSzPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktGetRecvBufSz( m_UdpSoktPt, RecvBufSzPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区数据长度。
    public int GetRecvBufLen( HTLong RecvBufLenPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktGetRecvBufLen( m_UdpSoktPt, RecvBufLenPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用已监听或已连接的本端UDP协议套接字发送一个原始数据包到指定的或连接的远端UDP协议套接字。
    public int Send( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktSend( m_UdpSoktPt, RmtNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, DataBufPt, DataBufLen, TimeOutMsec, Times, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //用已监听或已连接的本端UDP协议套接字接收一个原始远端UDP协议套接字发送的数据包。
    public int Recv( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktRecv( m_UdpSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, DataBufPt, DataBufSz, DataBufLenPt, TimeOutMsec, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用已监听或已连接的本端UDP协议套接字发送一个封装数据包到指定的或连接的远端UDP协议套接字。
    public int SendPkt( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktSendPkt( m_UdpSoktPt, RmtNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, DataBufPt, DataBufLen, TimeOutMsec, Times, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //用已监听或已连接的本端UDP协议套接字接收一个远端UDP协议套接字发送的封装数据包。
    public int RecvPkt( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return UdpSoktRecvPkt( m_UdpSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, DataBufPt, DataBufSz, DataBufLenPt, TimeOutMsec, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //关闭并销毁已创建的本端UDP协议套接字。
    public int Dstoy( Vstr ErrInfoVstrPt )
    {
        if( m_UdpSoktPt != 0 )
        {
            if( UdpSoktDstoy( m_UdpSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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

    //创建并初始化已监听的本端UDP协议套接字。
    public native int UdpSoktInit( HTLong UdpSoktPt, int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, long ErrInfoVstrPt );

    //用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字，已连接的本端UDP协议套接字只能接收连接的远端UDP协议套接字发送的数据包。
    public native int UdpSoktConnect( long UdpSoktPt, int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。
    public native int UdpSoktDisconnect( long UdpSoktPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //获取已监听的本端UDP协议套接字绑定的本端节点地址和端口。
    public native int UdpSoktGetLclAddr( long UdpSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //获取已连接的本端UDP协议套接字连接的远端UDP协议套接字绑定的远端节点地址和端口。
    public native int UdpSoktGetRmtAddr( long UdpSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //设置已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public native int UdpSoktSetSendBufSz( long UdpSoktPt, long SendBufSz, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //获取已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public native int UdpSoktGetSendBufSz( long UdpSoktPt, HTLong SendBufSzPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //设置已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public native int UdpSoktSetRecvBufSz( long UdpSoktPt, long RecvBufSz, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public native int UdpSoktGetRecvBufSz( long UdpSoktPt, HTLong RecvBufSzPt, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区数据长度。
    public native int UdpSoktGetRecvBufLen( long UdpSoktPt, HTLong RecvBufLenPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //用已监听或已连接的本端UDP协议套接字发送一个原始数据包到指定的或连接的远端UDP协议套接字。
    public native int UdpSoktSend( long UdpSoktPt, int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //用已监听或已连接的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个原始数据包。
    public native int UdpSoktRecv( long UdpSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //用已监听或已连接的本端UDP协议套接字发送一个封装数据包到指定的或连接的远端UDP协议套接字。
    public native int UdpSoktSendPkt( long UdpSoktPt, int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, long ErrInfoVstrPt );
    //用已监听或已连接的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个封装数据包。
    public native int UdpSoktRecvPkt( long UdpSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //关闭并销毁已创建的本端UDP协议套接字。
    public native int UdpSoktDstoy( long UdpSoktPt, long ErrInfoVstrPt );
}