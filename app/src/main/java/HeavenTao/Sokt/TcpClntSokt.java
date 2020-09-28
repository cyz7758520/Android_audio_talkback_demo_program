package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端TCP协议客户端套接字类。
public class TcpClntSokt
{
    private long m_TcpClntSoktPt; //存放本端TCP协议客户端套接字的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "Sokt" ); //加载libSokt.so。
    }

    //构造函数。
    public TcpClntSokt()
    {
        m_TcpClntSoktPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy( ( short )-1, null );
    }

    //获取本端TCP协议客户端套接字的内存指针。
    public long GetTcpClntSoktPt()
    {
        return m_TcpClntSoktPt;
    }

    //创建并初始化本端TCP协议客户端套接字，并连接已监听的远端TCP协议服务端套接字。
    public native int Init( int RmtLclNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, String LclNodeNamePt, String LclNodeSrvcPt, short TimeOutMsec, VarStr ErrInfoVarStrPt );

    //获取已连接的本端TCP协议客户端套接字绑定的本地节点地址和端口。
    public native int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, VarStr ErrInfoVarStrPt );
    //获取已连接的本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远程节点地址和端口。
    public native int GetRmtAddr( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, VarStr ErrInfoVarStrPt );

    //用已连接的本端TCP协议客户端套接字发送一个数据包到连接的远端TCP协议客户端套接字。
    public native int SendPkt( byte DataBufPt[], long DataBufLen, short TimeOutMsec, VarStr ErrInfoVarStrPt );
    //用已连接的本端TCP协议客户端套接字开始接收连接的远端TCP协议客户端套接字发送的一个数据包。
    public native int RecvPkt( byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, VarStr ErrInfoVarStrPt );

    //设置已连接的本端TCP协议客户端套接字的多线程操作状态。
    public native int SetMultiThread( int IsMultiThread, VarStr ErrInfoVarStrPt );
    //获取已连接的本端TCP协议客户端套接字的多线程操作状态。
    public native int GetMultiThread( HTInt IsMultiThreadPt, VarStr ErrInfoVarStrPt );

    //设置已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public native int SetNoDelay( int IsNoDelay, VarStr ErrInfoVarStrPt );
    //获取已连接的本端TCP协议客户端套接字的Nagle延迟算法状态。
    public native int GetNoDelay( HTInt IsNoDelayPt, VarStr ErrInfoVarStrPt );

    //设置本端TCP协议客户端套接字的发送缓冲区内存大小。
    public native int SetSendBufSz( long SendBufSz, VarStr ErrInfoVarStrPt );
    //获取本端TCP协议客户端套接字的发送缓冲区内存大小。
    public native int GetSendBufSz( HTLong SendBufSzPt, VarStr ErrInfoVarStrPt );

    //设置本端TCP协议客户端套接字的接收缓冲区内存大小。
    public native int SetRecvBufSz( long RecvBufSz, VarStr ErrInfoVarStrPt );
    //获取本端TCP协议客户端套接字的接收缓冲区内存大小。
    public native int GetRecvBufSz( HTLong RecvBufSzPt, VarStr ErrInfoVarStrPt );
    //获取本端TCP协议客户端套接字的接收缓冲区数据长度。
    public native int GetRecvBufLen( HTLong RecvBufLenPt, VarStr ErrInfoVarStrPt );

    //关闭并销毁已创建的本端TCP协议客户端套接字。
    public native int Destroy( short TimeOutSec, VarStr ErrInfoVarStrPt );
}