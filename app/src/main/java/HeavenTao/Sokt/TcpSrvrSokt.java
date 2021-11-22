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
    public void finalize()
    {
        Destroy( null );
    }

    //创建并初始化已监听的本端TCP协议服务端套接字。
    public int Init( int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, int MaxWait, int IsReuseAddr, VarStr ErrInfoVarStrPt )
    {
        if( m_TcpSrvrSoktPt == 0 )
        {
            HTLong p_WebRtcNsPt = new HTLong();
            if( TcpSrvrSoktInit( p_WebRtcNsPt, LclNodeAddrFmly, LclNodeNamePt, LclNodeSrvcPt, MaxWait, IsReuseAddr, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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

    //获取已监听的本端TCP协议服务端套接字绑定的本地节点地址和端口。
    public int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpSrvrSoktGetLclAddr( m_TcpSrvrSoktPt, LclNodeAddrFmlyPt, LclNodeAddrPt, LclNodePortPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //用已监听的本端TCP协议服务端套接字开始接受远端TCP协议客户端套接字的连接。
    public int Accept( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, short TimeOutMsec, TcpClntSokt TcpClntSoktPt, int IsAutoLockUnlock, VarStr ErrInfoVarStrPt )
    {
        return TcpSrvrSoktAccept( m_TcpSrvrSoktPt, RmtNodeAddrFmlyPt, RmtNodeAddrPt, RmtNodePortPt, TimeOutMsec, TcpClntSoktPt, IsAutoLockUnlock, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //关闭并销毁已创建的本端TCP协议服务端套接字。
    public int Destroy( VarStr ErrInfoVarStrPt )
    {
        if( m_TcpSrvrSoktPt != 0 )
        {
            if( TcpSrvrSoktDestroy( m_TcpSrvrSoktPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
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
    public native int TcpSrvrSoktInit( HTLong TcpSrvrSoktPt, int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, int MaxWait, int IsReuseAddr, long ErrInfoVarStrPt );

    //获取已监听的本端TCP协议服务端套接字绑定的本地节点地址和端口。
    public native int TcpSrvrSoktGetLclAddr( long TcpSrvrSoktPt, HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //用已监听的本端TCP协议服务端套接字开始接受远端TCP协议客户端套接字的连接。
    public native int TcpSrvrSoktAccept( long TcpSrvrSoktPt, HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, short TimeOutMsec, TcpClntSokt TcpClntSoktPt, int IsAutoLockUnlock, long ErrInfoVarStrPt );

    //关闭并销毁已创建的本端TCP协议服务端套接字。
    public native int TcpSrvrSoktDestroy( long TcpSrvrSoktPt, long ErrInfoVarStrPt );
}