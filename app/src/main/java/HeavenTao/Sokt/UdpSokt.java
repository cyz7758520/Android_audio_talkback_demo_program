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
    public void finalize()
    {
        Destroy( null );
    }

    //获取本端UDP协议套接字的指针。
    public long GetUdpSoktPt()
    {
        return m_UdpSoktPt;
    }

    //创建并初始化已监听的本端UDP协议套接字。
    public int Init( int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, VarStr ErrInfoVarStrPt )
    {
        if( m_UdpSoktPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( UdpSoktInit( p_WebRtcNsPt, LclNodeAddrFmly, LclNodeNamePt, LclNodeSrvcPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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
    public int Connect( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktConnect( m_UdpSoktPt, RmtNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。
    public int Disconnect( int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktDisconnect( m_UdpSoktPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //获取已监听的本端UDP协议套接字绑定的本地节点地址和端口。
    public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktGetLclAddr( m_UdpSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //获取已连接的本端UDP协议套接字连接的远端UDP协议套接字绑定的远程节点地址和端口。
    public int GetRmtAddr( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktGetRmtAddr( m_UdpSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //设置已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public int SetSendBufSz( long SendBufSz, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktSetSendBufSz( m_UdpSoktPt, SendBufSz, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //获取已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public int GetSendBufSz( HTLong SendBufSzPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktGetSendBufSz( m_UdpSoktPt, SendBufSzPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //设置已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public int SetRecvBufSz( long RecvBufSz, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktSetRecvBufSz( m_UdpSoktPt, RecvBufSz, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public int GetRecvBufSz( HTLong RecvBufSzPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktGetRecvBufSz( m_UdpSoktPt, RecvBufSzPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区数据长度。
    public int GetRecvBufLen( HTLong RecvBufLenPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktGetRecvBufLen( m_UdpSoktPt, RecvBufLenPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //用已监听或已连接的本端UDP协议套接字发送一个数据包到指定的或连接的远端UDP协议套接字。
    public int SendPkt( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktSendPkt( m_UdpSoktPt, RmtNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, DataBufPt, DataBufLen, TimeOutMsec, Times, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }
    //用已监听或已连接的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包。
    public int RecvPkt( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return UdpSoktRecvPkt( m_UdpSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, DataBufPt, DataBufSz, DataBufLenPt, TimeOutMsec, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //关闭并销毁已创建的本端UDP协议套接字。
    public int Destroy( VarStr ErrInfoVarStrPt )
    {
        if( m_UdpSoktPt != 0 )
        {
            if( UdpSoktDestroy( m_UdpSoktPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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
    public native int UdpSoktInit( HTLong UdpSoktPt, int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, long ErrInfoVarStrPt );

    //用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字，已连接的本端UDP协议套接字只能接收连接的远端UDP协议套接字发送的数据包。
    public native int UdpSoktConnect( long UdpSoktPt, int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。
    public native int UdpSoktDisconnect( long UdpSoktPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //获取已监听的本端UDP协议套接字绑定的本地节点地址和端口。
    public native int UdpSoktGetLclAddr( long UdpSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //获取已连接的本端UDP协议套接字连接的远端UDP协议套接字绑定的远程节点地址和端口。
    public native int UdpSoktGetRmtAddr( long UdpSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //设置已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public native int UdpSoktSetSendBufSz( long UdpSoktPt, long SendBufSz, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //获取已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public native int UdpSoktGetSendBufSz( long UdpSoktPt, HTLong SendBufSzPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //设置已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public native int UdpSoktSetRecvBufSz( long UdpSoktPt, long RecvBufSz, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public native int UdpSoktGetRecvBufSz( long UdpSoktPt, HTLong RecvBufSzPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区数据长度。
    public native int UdpSoktGetRecvBufLen( long UdpSoktPt, HTLong RecvBufLenPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //用已监听或已连接的本端UDP协议套接字发送一个数据包到指定的或连接的远端UDP协议套接字。
    public native int UdpSoktSendPkt( long UdpSoktPt, int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte DataBufPt[], long DataBufLen, short TimeOutMsec, int Times, int IsAutoLockUnlock, long ErrInfoVarStrPt );
    //用已监听或已连接的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包。
    public native int UdpSoktRecvPkt( long UdpSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //关闭并销毁已创建的本端UDP协议套接字。
    public native int UdpSoktDestroy( long UdpSoktPt, long ErrInfoVarStrPt );
}