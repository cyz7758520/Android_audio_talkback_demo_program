package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端TCP协议服务端套接字类。
public class TcpSrvrSokt
{
    private long m_TcpSrvrSoktPt; //存放本端TCP协议服务端套接字的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Sokt" ); //加载libSokt.so。
    }

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

    //获取本端TCP协议服务端套接字的内存指针。
    public long GetTcpSrvrSoktPt()
    {
        return m_TcpSrvrSoktPt;
    }

    //创建并初始化已监听的本端TCP协议服务端套接字。
    public native int Init( String LclNodeNamePt, String LclNodeSrvcPt, int MaxWait, int IsReuseAddr, VarStr ErrInfoVarStrPt );

    //获取已监听的本端TCP协议服务端套接字绑定的本地节点地址和端口。
    public native int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, VarStr ErrInfoVarStrPt );

    //用已监听的本端TCP协议服务端套接字开始接受远端TCP协议客户端套接字的连接。
    public native int Accept( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, short TimeOut, TcpClntSokt TcpClntSoktPt, VarStr ErrInfoVarStrPt );

    //关闭并销毁已创建的本端TCP协议服务端套接字。
    public native int Destroy( VarStr ErrInfoVarStrPt );
}