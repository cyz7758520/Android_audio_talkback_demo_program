package HeavenTao.Sokt;

import HeavenTao.Data.*;

//本端UDP协议套接字类。
public class UdpSokt
{
    private long m_UdpSoktPt; //存放本端UDP协议套接字的内存指针。

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

    //获取本端UDP协议套接字的内存指针。
    public long GetUdpSoktPt()
    {
        return m_UdpSoktPt;
    }

    //创建并初始化已监听的本端UDP协议套接字。
    public native int Init( int LclNodeAddrFmly, String LclNodeNamePt, String LclNodeSrvcPt, VarStr ErrInfoVarStrPt );

    //用已监听的本端UDP协议套接字连接已监听的远端UDP协议套接字，已连接的本端UDP协议套接字只能接收连接的远端UDP协议套接字发送的数据包。
    public native int Connect( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, VarStr ErrInfoVarStrPt );
    //将已连接的本端UDP协议套接字断开连接的远端UDP协议套接字，已连接的本端UDP协议套接字将变成已监听的本端UDP协议套接字。
    public native int Disconnect( VarStr ErrInfoVarStrPt );

    //获取已监听的本端UDP协议套接字绑定的本地节点地址和端口。
    public native int GetLclAddr( HTInt LclNodeAddrFmlyPt, HTString LclNodeAddrPt, HTString LclNodePortPt, VarStr ErrInfoVarStrPt );
    //获取已连接的本端UDP协议套接字连接的远端UDP协议套接字绑定的远程节点地址和端口。
    public native int GetRmtAddr( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, VarStr ErrInfoVarStrPt );

    //用已监听或已连接的本端UDP协议套接字发送一个数据包到指定的或连接的远端UDP协议套接字。
    public native int SendPkt( int RmtNodeAddrFmly, String RmtNodeNamePt, String RmtNodeSrvcPt, byte DataBufPt[], long DataBufLen, short TimeOutMsec, VarStr ErrInfoVarStrPt );
    //用已监听或已连接的本端UDP协议套接字开始接收远端UDP协议套接字发送的一个数据包。
    public native int RecvPkt( HTInt RmtNodeAddrFmlyPt, HTString RmtNodeAddrPt, HTString RmtNodePortPt, byte DataBufPt[], long DataBufSz, HTLong DataBufLenPt, short TimeOutMsec, VarStr ErrInfoVarStrPt );

    //设置已监听或已连接的本端UDP协议套接字的多线程操作状态。
    public native int SetMultiThread( int IsMultiThread, VarStr ErrInfoVarStrPt );
    //获取已监听或已连接的本端UDP协议套接字的多线程操作状态。
    public native int GetMultiThread( HTInt IsMultiThreadPt, VarStr ErrInfoVarStrPt );

    //设置已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public native int SetSendBufSz( long SendBufSz, VarStr ErrInfoVarStrPt );
    //获取已监听或已连接的本端UDP协议套接字的发送缓冲区内存大小。
    public native int GetSendBufSz( HTLong SendBufSzPt, VarStr ErrInfoVarStrPt );

    //设置已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public native int SetRecvBufSz( long RecvBufSz, VarStr ErrInfoVarStrPt );
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区内存大小。
    public native int GetRecvBufSz( HTLong RecvBufSzPt, VarStr ErrInfoVarStrPt );
    //获取已监听或已连接的本端UDP协议套接字的接收缓冲区数据长度。
    public native int GetRecvBufLen( HTLong RecvBufLenPt, VarStr ErrInfoVarStrPt );

    //关闭并销毁已创建的本端UDP协议套接字。
    public native int Destroy( VarStr ErrInfoVarStrPt );
}