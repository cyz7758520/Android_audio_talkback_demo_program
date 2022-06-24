package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端TCP协议服务端套接字类。
public class TcpSrvrSokt
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
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

    //创建并初始化已监听的本端TCP协议服务端套接字。
    public int Init( int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, int MaxWait, int IsReuseAddr, Vstr ErrInfoVstrPt )
    {
        if( m_TcpSrvrSoktPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( TcpSrvrSoktInit( p_WebRtcNsPt, LclNodeAddrFmly, LclNodeNamePt, LclNodeSrvcPt, MaxWait, IsReuseAddr, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_TcpSrvrSoktPt = p_WebRtcNsPt.m_Val;
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

    //获取已监听的本端TCP协议服务端套接字绑定的本端节点地址和端口。
    public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpSrvrSoktGetLclAddr( m_TcpSrvrSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用已监听的本端TCP协议服务端套接字开始接受远端TCP协议客户端套接字的连接。
    public int Accept( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, short TimeOutMsec, TcpClntSokt TcpClntSoktPt, int IsAutoLockUnlock, Vstr ErrInfoVstrPt )
    {
        return TcpSrvrSoktAccept( m_TcpSrvrSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, TimeOutMsec, TcpClntSoktPt, IsAutoLockUnlock, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //关闭并销毁已创建的本端TCP协议服务端套接字。
    public int Dstoy( Vstr ErrInfoVstrPt )
    {
        if( m_TcpSrvrSoktPt != 0 )
        {
            if( TcpSrvrSoktDstoy( m_TcpSrvrSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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

    //创建并初始化已监听的本端TCP协议服务端套接字。
    public native int TcpSrvrSoktInit( HTLong TcpSrvrSoktPt, int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, int MaxWait, int IsReuseAddr, long ErrInfoVstrPt );

    //获取已监听的本端TCP协议服务端套接字绑定的本端节点地址和端口。
    public native int TcpSrvrSoktGetLclAddr( long TcpSrvrSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //用已监听的本端TCP协议服务端套接字开始接受远端TCP协议客户端套接字的连接。
    public native int TcpSrvrSoktAccept( long TcpSrvrSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, short TimeOutMsec, TcpClntSokt TcpClntSoktPt, int IsAutoLockUnlock, long ErrInfoVstrPt );

    //关闭并销毁已创建的本端TCP协议服务端套接字。
    public native int TcpSrvrSoktDstoy( long TcpSrvrSoktPt, long ErrInfoVstrPt );
}