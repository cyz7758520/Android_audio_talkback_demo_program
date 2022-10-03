package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端高级UDP协议套接字类。
public class AudpSokt
{
    private long m_AudpSoktPt; //存放本端高级UDP协议套接字的指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Sokt" ); //加载libSokt.so。
    }

    //构造函数。
    public AudpSokt()
    {
        m_AudpSoktPt = 0;
    }

    //析构函数。
    protected void finalize()
    {
        Dstoy( null );
    }

    public static final int AudpCnctStsWait = 0; //连接状态：等待远端接受连接。
    public static final int AudpCnctStsCnct = 1; //连接状态：已连接。
    public static final int AudpCnctStsTmot = 2; //连接状态：超时未接收到任何数据包。异常断开。
    public static final int AudpCnctStsDsct = 3; //连接状态：已断开。本端或远端正常断开。

    //获取本端高级UDP协议套接字的指针。
    public long GetAudpSoktPt()
    {
        return m_AudpSoktPt;
    }

    //创建并初始化本端高级UDP协议套接字。
    public int Init( int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, short NewCnctMaxWaitCnt, short TmotMsec, Vstr ErrInfoVstrPt )
    {
        if( m_AudpSoktPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( AudpInit( p_WebRtcNsPt, LclNodeAddrFmly, LclNodeNamePt, LclNodeSrvcPt, NewCnctMaxWaitCnt, TmotMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_AudpSoktPt = p_WebRtcNsPt.m_Val;
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

    //用本端高级UDP协议套接字接受远端高级UDP协议套接字的连接。
    public int Acpt( HTLong CnctIdxPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, short TmotMsec, Vstr ErrInfoVstrPt )
    {
        return AudpAcpt( m_AudpSoktPt, CnctIdxPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, TmotMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用本端高级UDP协议套接字连接远端高级UDP协议套接字。本函数立即返回，随后调用WaitCnct函数等待连接是否成功。
    public int Cnct( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, HTLong CnctIdxPt, Vstr ErrInfoVstrPt )
    {
        return AudpCnct( m_AudpSoktPt, RmtNodeAddrFmly, RmtNodeNamePt, RmtNodeSrvcPt, CnctIdxPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //等待本端高级UDP协议套接字连接远端是否成功。
    public int WaitCnct( long CnctIdx, short TmotMsec, HTInt CnctStsPt, Vstr ErrInfoVstrPt )
    {
        return AudpWaitCnct( m_AudpSoktPt, CnctIdx, TmotMsec, CnctStsPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //关闭本端高级UDP协议套接字的连接。
    public int ClosCnct( long CnctIdx, Vstr ErrInfoVstrPt )
    {
        return AudpClosCnct( m_AudpSoktPt, CnctIdx, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //获取本端高级UDP协议套接字绑定的本端节点地址和端口。
    public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, Vstr ErrInfoVstrPt )
    {
        return AudpGetLclAddr( m_AudpSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端高级UDP协议套接字连接的远端高级UDP协议套接字绑定的远端节点地址和端口。
    public int GetRmtAddr( long CnctIdx, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, Vstr ErrInfoVstrPt )
    {
        return AudpGetRmtAddr( m_AudpSoktPt, CnctIdx, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端高级UDP协议套接字的发送缓冲区大小。
    public int SetSendBufSz( long SendBufSz, Vstr ErrInfoVstrPt )
    {
        return AudpSetSendBufSz( m_AudpSoktPt, SendBufSz, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端高级UDP协议套接字的发送缓冲区大小。
    public int GetSendBufSz( HTLong SendBufSzPt, Vstr ErrInfoVstrPt )
    {
        return AudpGetSendBufSz( m_AudpSoktPt, SendBufSzPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //设置本端高级UDP协议套接字的接收缓冲区大小。
    public int SetRecvBufSz( long RecvBufSz, Vstr ErrInfoVstrPt )
    {
        return AudpSetRecvBufSz( m_AudpSoktPt, RecvBufSz, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端高级UDP协议套接字的接收缓冲区大小。
    public int GetRecvBufSz( HTLong RecvBufSzPt, Vstr ErrInfoVstrPt )
    {
        return AudpGetRecvBufSz( m_AudpSoktPt, RecvBufSzPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //获取本端高级UDP协议套接字的接收缓冲区长度。
    public int GetRecvBufLen( HTLong RecvBufLenPt, Vstr ErrInfoVstrPt )
    {
        return AudpGetRecvBufLen( m_AudpSoktPt, RecvBufLenPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //用本端高级UDP协议套接字发送数据包到连接的远端高级UDP协议套接字。
    public int SendPkt( long CnctIdx, byte DataBufPt[], long DataBufLen, int Times, Vstr ErrInfoVstrPt )
    {
        return AudpSendPkt( m_AudpSoktPt, CnctIdx, DataBufPt, DataBufLen, Times, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }
    //用本端高级UDP协议套接字接收连接的远端高级UDP协议套接字发送的数据包。
    public int RecvPkt( long CnctIdx, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TmotMsec, Vstr ErrInfoVstrPt )
    {
        return AudpRecvPkt( m_AudpSoktPt, CnctIdx, DataBufPt, DataBufSz, DataBufLenPt, TmotMsec, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //关闭并销毁本端高级UDP协议套接字。
    public int Dstoy( Vstr ErrInfoVstrPt )
    {
        if( m_AudpSoktPt != 0 )
        {
            if( AudpDstoy( m_AudpSoktPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
            {
                m_AudpSoktPt = 0;
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

    //创建并初始化本端高级UDP协议套接字。
    private native int AudpInit( HTLong AudpSoktPt, int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, short NewCnctMaxWaitCnt, short TmotMsec, long ErrInfoVstrPt );

    //用本端高级UDP协议套接字接受远端高级UDP协议套接字的连接。
    private native int AudpAcpt( long AudpSoktPt, HTLong CnctIdxPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, short TmotMsec, long ErrInfoVstrPt );

    //用本端高级UDP协议套接字连接远端高级UDP协议套接字。本函数立即返回，随后调用AudpWaitCnct函数等待连接是否成功。
    private native int AudpCnct( long AudpSoktPt, int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, HTLong CnctIdxPt, long ErrInfoVstrPt );
    //等待本端高级UDP协议套接字连接远端是否成功。
    private native int AudpWaitCnct( long AudpSoktPt, long CnctIdx, short TmotMsec, HTInt CnctStsPt, long ErrInfoVstrPt );
    //关闭本端高级UDP协议套接字的连接。
    private native int AudpClosCnct( long AudpSoktPt, long CnctIdx, long ErrInfoVstrPt );

    //获取本端高级UDP协议套接字绑定的本端节点地址和端口。
    private native int AudpGetLclAddr( long AudpSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, long ErrInfoVstrPt );
    //获取本端高级UDP协议套接字连接的远端高级UDP协议套接字绑定的远端节点地址和端口。
    private native int AudpGetRmtAddr( long AudpSoktPt, long CnctIdx, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, long ErrInfoVstrPt );

    //设置本端高级UDP协议套接字的发送缓冲区大小。
    private native int AudpSetSendBufSz( long AudpSoktPt, long SendBufSz, long ErrInfoVstrPt );
    //获取本端高级UDP协议套接字的发送缓冲区大小。
    private native int AudpGetSendBufSz( long AudpSoktPt, HTLong SendBufSzPt, long ErrInfoVstrPt );

    //设置本端高级UDP协议套接字的接收缓冲区大小。
    private native int AudpSetRecvBufSz( long AudpSoktPt, long RecvBufSz, long ErrInfoVstrPt );
    //获取本端高级UDP协议套接字的接收缓冲区大小。
    private native int AudpGetRecvBufSz( long AudpSoktPt, HTLong RecvBufSzPt, long ErrInfoVstrPt );
    //获取本端高级UDP协议套接字的接收缓冲区长度。
    private native int AudpGetRecvBufLen( long AudpSoktPt, HTLong RecvBufLenPt, long ErrInfoVstrPt );

    //用本端高级UDP协议套接字发送数据包到连接的远端高级UDP协议套接字。
    private native int AudpSendPkt( long AudpSoktPt, long CnctIdx, byte DataBufPt[], long DataBufLen, int Times, long ErrInfoVstrPt );
    //用本端高级UDP协议套接字接收连接的远端高级UDP协议套接字发送的数据包。
    private native int AudpRecvPkt( long AudpSoktPt, long CnctIdx, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TmotMsec, long ErrInfoVstrPt );

    //关闭并销毁本端高级UDP协议套接字。
    private native int AudpDstoy( long AudpSoktPt, long ErrInfoVstrPt );
}