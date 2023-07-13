package com.example.andrd_ado_vdo_tkbk_demo;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import HeavenTao.Ado.*;
import HeavenTao.Vdo.*;
import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

//我的媒体处理线程。
public class MyMediaPocsThrd extends MediaPocsThrd
{
    MainActivity m_MainActivityPt; //存放主界面的指针。
    Handler m_MainActivityHandlerPt; //存放主界面消息处理的指针。
    public int m_IsInterrupt; //存放是否中断，为0表示未中断，为1表示已中断。

    class Ntwk //存放网络。
    {
        String m_IPAddrStrPt; //存放IP地址字符串的指针。
        String m_PortStrPt; //存放端口字符串的指针。
        int m_XfrMode; //存放传输模式，为0表示实时半双工（一键通），为1表示实时全双工。
        int m_PttBtnIsDown; //存放一键即按即通按钮是否按下，为0表示弹起，为非0表示按下。
        int m_MaxCnctTimes; //存放最大连接次数，取值区间为[1,2147483647]。
        int m_UseWhatXfrPrtcl; //存放使用什么传输协议，为0表示TCP协议，为1表示UDP协议。
        int m_IsCreateSrvrOrClnt; //存放创建服务端或者客户端标记，为1表示创建服务端，为0表示创建客户端。
        TcpSrvrSokt m_TcpSrvrSoktPt; //存放本端TCP协议服务端套接字的指针。
        TcpClntSokt m_TcpClntSoktPt; //存放本端TCP协议客户端套接字的指针。
        AudpSokt m_AudpSoktPt; //存放本端高级UDP协议套接字的指针。
        HTLong m_AudpCnctIdxPt; //存放本端高级UDP协议连接索引。
    }
    Ntwk m_NtwkPt = new Ntwk();
    enum PktTyp //数据包类型。
    {
        AllowCnct, //允许连接包。
        RefuseCnct, //拒绝连接包。
        TkbkMode, //对讲模式包。
        AdoFrm, //音频输入输出帧包。
        VdoFrm, //视频输入输出帧包。
        Exit, //退出包。
    }

    int m_IsAutoAllowCnct; //存放是否自动允许连接，为0表示手动，为1表示自动。
    int m_RqstCnctRslt; //存放请求连接的结果，为0表示没有选择，为1表示允许，为2表示拒绝。

    public enum TkbkMode //对讲模式。
    {
        None, //空。
        Ado, //音频。
        Vdo, //视频。
        AdoVdo, //音视频。
        NoChg, //不变。
    }
    TkbkMode m_LclTkbkMode; //存放本端对讲模式。
    TkbkMode m_RmtTkbkMode; //存放远端对讲模式。

    int m_LastSendAdoInptFrmIsAct; //存放最后一个发送的音频输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
    int m_LastSendAdoInptFrmTimeStamp; //存放最后一个发送音频输入帧的时间戳。
    int m_LastSendVdoInptFrmTimeStamp; //存放最后一个发送视频输入帧的时间戳。
    int m_IsRecvExitPkt; //存放是否接收到退出包，为0表示否，为1表示是。

    int m_UseWhatRecvOtptFrm; //存放使用什么接收输出帧，为0表示容器，为1表示自适应抖动缓冲器。

    ConcurrentLinkedQueue< byte[] > m_RecvAdoOtptFrmCntnrPt; //存放接收音频输出帧容器的指针。
    ConcurrentLinkedQueue< byte[] > m_RecvVdoOtptFrmCntnrPt; //存放接收视频输出帧容器的指针。

    class AAjb //存放音频自适应抖动缓冲器。
    {
        HeavenTao.Ado.AAjb m_Pt; //存放指针。
        int m_MinNeedBufFrmCnt; //存放最小需缓冲帧的数量，单位为个帧，取值区间为[1,2147483647]。
        int m_MaxNeedBufFrmCnt; //最大需缓冲帧的数量，单位为个帧，取值区间为[1,2147483647]，必须大于等于最小需缓冲帧的数量。
        int m_MaxCntuLostFrmCnt; //最大连续丢失帧的数量，单位为个帧，取值区间为[1,2147483647]，当连续丢失帧的数量超过最大时，认为是对方中途暂停发送。
        float m_AdaptSensitivity; //存放自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。
    }
    AAjb m_AAjbPt = new AAjb();
    class VAjb //存放视频自适应抖动缓冲器。
    {
        HeavenTao.Vdo.VAjb m_Pt; //存放指针。
        int m_MinNeedBufFrmCnt; //存放最小需缓冲帧数量，单位为个帧，必须大于0。
        int m_MaxNeedBufFrmCnt; //存放最大需缓冲帧数量，单位为个帧，必须大于最小需缓冲数据帧的数量。
        float m_AdaptSensitivity; //存放自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。
    }
    VAjb m_VAjbPt = new VAjb();

    byte m_TmpBytePt[]; //存放临时数据。
    byte m_TmpByte2Pt[]; //存放临时数据。
    byte m_TmpByte3Pt[]; //存放临时数据。
    HTInt m_TmpHTIntPt; //存放临时数据。
    HTInt m_TmpHTInt2Pt; //存放临时数据。
    HTInt m_TmpHTInt3Pt; //存放临时数据。
    HTLong m_TmpHTLongPt; //存放临时数据。
    HTLong m_TmpHTLong2Pt; //存放临时数据。
    HTLong m_TmpHTLong3Pt; //存放临时数据。

    public enum UserMsgTyp //我的媒体处理线程消息。
    {
        LclTkbkMode, //本端对讲模式。
        RmtTkbkMode, //远端对讲模式。
        PttBtnDown, //一键即按即通按钮按下。
        PttBtnUp, //一键即按即通按钮弹起。
    }

    MyMediaPocsThrd( MainActivity MainActivityPt, Handler MainActivityHandlerPt )
    {
        super( MainActivityPt );

        m_MainActivityPt = MainActivityPt; //设置主界面的指针。
        m_MainActivityHandlerPt = MainActivityHandlerPt; //设置主界面消息处理的指针。
        m_IsInterrupt = 0; //设置未中断。

        m_LclTkbkMode = TkbkMode.None;
        m_RmtTkbkMode = TkbkMode.None;
    }

    //用户定义的初始化函数。
    @Override public int UserInit()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
        int p_RmtNodeAddrFamly; //存放远端节点的地址族，为4表示IPv4，为6表示IPv6，为0表示自动选择。
        HTString p_LclNodeAddrPt = new HTString();
        HTString p_LclNodePortPt = new HTString();
        HTString p_RmtNodeAddrPt = new HTString();
        HTString p_RmtNodePortPt = new HTString();

        Out:
        {
            { Message p_MessagePt = new Message(); p_MessagePt.what = MainActivityHandler.Msg.MediaPocsThrdInit.ordinal(); m_MainActivityHandlerPt.sendMessage( p_MessagePt ); } //向主界面发送初始化媒体处理线程的消息。

            m_RqstCnctRslt = 0; //设置请求连接的结果为没有选择。
            m_IsRecvExitPkt = 0; //设置没有接收到退出包。
            { //设置远端节点的地址族。
                try
                {
                    InetAddress inetAddress = InetAddress.getByName( m_NtwkPt.m_IPAddrStrPt );
                    if( inetAddress.getAddress().length == 4 ) p_RmtNodeAddrFamly = 4;
                    else p_RmtNodeAddrFamly = 6;
                }
                catch( UnknownHostException e )
                {
                    p_RmtNodeAddrFamly = 0;
                }
            }
            if( m_TmpBytePt == null ) m_TmpBytePt = new byte[ 1024 * 1024 ]; //初始化临时数据。
            if( m_TmpByte2Pt == null ) m_TmpByte2Pt = new byte[ 1024 * 1024 ]; //初始化临时数据。
            if( m_TmpByte3Pt == null ) m_TmpByte3Pt = new byte[ 1024 * 1024 ]; //初始化临时数据。
            if( m_TmpHTIntPt == null ) m_TmpHTIntPt = new HTInt(); //初始化临时数据。
            if( m_TmpHTInt2Pt == null ) m_TmpHTInt2Pt = new HTInt(); //初始化临时数据。
            if( m_TmpHTInt3Pt == null ) m_TmpHTInt3Pt = new HTInt(); //初始化临时数据。
            if( m_TmpHTLongPt == null ) m_TmpHTLongPt = new HTLong(); //初始化临时数据。
            if( m_TmpHTLong2Pt == null ) m_TmpHTLong2Pt = new HTLong(); //初始化临时数据。
            if( m_TmpHTLong3Pt == null ) m_TmpHTLong3Pt = new HTLong(); //初始化临时数据。

            if( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) //如果要使用TCP协议。
            {
                if( m_NtwkPt.m_IsCreateSrvrOrClnt == 1 ) //如果是创建本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接。
                {
                    m_NtwkPt.m_TcpSrvrSoktPt = new TcpSrvrSokt();

                    if( m_NtwkPt.m_TcpSrvrSoktPt.Init( p_RmtNodeAddrFamly, m_NtwkPt.m_IPAddrStrPt, m_NtwkPt.m_PortStrPt, 1, 1, m_ErrInfoVstrPt ) == 0 ) //如果初始化本端TCP协议服务端套接字成功。
                    {
                        if( m_NtwkPt.m_TcpSrvrSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVstrPt ) != 0 ) //如果获取本端TCP协议服务端套接字绑定的本地节点地址和端口失败。
                        {
                            String p_InfoStrPt = "获取本端TCP协议服务端套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }

                        String p_InfoStrPt = "初始化本端TCP协议服务端套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]成功。";
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }
                    else //如果初始化本端TCP协议服务端套接字失败。
                    {
                        String p_InfoStrPt = "初始化本端TCP协议服务端套接字[" + m_NtwkPt.m_IPAddrStrPt + ":" + m_NtwkPt.m_PortStrPt + "]失败。原因：" + m_ErrInfoVstrPt.GetStr();
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break Out;
                    }

                    m_NtwkPt.m_TcpClntSoktPt = new TcpClntSokt();

                    while( true ) //循环接受远端TCP协议客户端套接字的连接。
                    {
                        if( m_NtwkPt.m_TcpSrvrSoktPt.Acpt( m_NtwkPt.m_TcpClntSoktPt, null, p_RmtNodeAddrPt, p_RmtNodePortPt, ( short ) 1, 0, m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_NtwkPt.m_TcpClntSoktPt.m_TcpClntSoktPt != 0 ) //如果用本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接成功。
                            {
                                m_NtwkPt.m_TcpSrvrSoktPt.Dstoy( null ); //关闭并销毁本端TCP协议服务端套接字，防止还有其他远端TCP协议客户端套接字继续连接。
                                m_NtwkPt.m_TcpSrvrSoktPt = null;

                                String p_InfoStrPt = "用本端TCP协议服务端套接字接受远端TCP协议客户端套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]的连接成功。";
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break;
                            } //如果用本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接超时，就重新接受。
                        }
                        else
                        {
                            m_NtwkPt.m_TcpClntSoktPt = null;

                            String p_InfoStrPt = "用本端TCP协议服务端套接字接受远端TCP协议客户端套接字的连接失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }

                        if( m_ReadyExitCnt != 0 ) //如果本线程接收到退出请求。
                        {
                            m_NtwkPt.m_TcpClntSoktPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
                            break Out;
                        }
                    }
                }
                else if( m_NtwkPt.m_IsCreateSrvrOrClnt == 0 ) //如果是创建本端TCP协议客户端套接字连接远端TCP协议服务端套接字。
                {
                    //Ping一下远程节点地址，这样可以快速获取局域网的ARP条目，从而避免连接失败。
                    try
                    {
                        Runtime.getRuntime().exec( "ping -c 1 -w 1 " + m_NtwkPt.m_IPAddrStrPt );
                    }
                    catch( Exception ignored )
                    {
                    }

                    m_NtwkPt.m_TcpClntSoktPt = new TcpClntSokt();

                    LoopTcpCnct:
                    for( int p_CurCnctTimes = 1; ; ) //循环连接远端TCP协议服务端套接字。
                    {
                        {
                            String p_InfoStrPt = "开始第 " + p_CurCnctTimes + " 次连接。";
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        }

                        if( m_NtwkPt.m_TcpClntSoktPt.Init( p_RmtNodeAddrFamly, m_NtwkPt.m_IPAddrStrPt, m_NtwkPt.m_PortStrPt, null, null, m_ErrInfoVstrPt ) == 0 ) //如果初始化本端TCP协议客户端套接字，并连接远端TCP协议服务端套接字成功。
                        {
                            HTInt p_TcpCnctSts = new HTInt();

                            while( m_NtwkPt.m_TcpClntSoktPt.WaitCnct( ( short ) 1, p_TcpCnctSts, m_ErrInfoVstrPt ) == 0 ) //循环等待本端TCP协议客户端套接字连接远端TCP协议服务端套接字成功。
                            {
                                if( p_TcpCnctSts.m_Val == TcpClntSokt.TcpCnctStsWait ) //如果等待远端接受连接。
                                {
                                    //重新循环，继续等待本端本端TCP协议客户端套接字连接远端TCP协议服务端套接字。
                                }
                                else if( p_TcpCnctSts.m_Val == TcpClntSokt.TcpCnctStsCnct ) //如果连接成功。
                                {
                                    if( m_NtwkPt.m_TcpClntSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, 0, m_ErrInfoVstrPt ) != 0 )
                                    {
                                        String p_InfoStrPt = "获取本端TCP协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVstrPt.GetStr();
                                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                        break Out;
                                    }
                                    if( m_NtwkPt.m_TcpClntSoktPt.GetRmtAddr( null, p_RmtNodeAddrPt, p_RmtNodePortPt, 0, m_ErrInfoVstrPt ) != 0 )
                                    {
                                        String p_InfoStrPt = "获取本端TCP协议客户端套接字连接的远端TCP协议客户端套接字绑定的远程节点地址和端口失败。原因：" + m_ErrInfoVstrPt.GetStr();
                                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                        break Out;
                                    }

                                    String p_InfoStrPt = "初始化本端TCP协议客户端套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]，并连接远端TCP协议服务端套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]成功。";
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                    break LoopTcpCnct; //跳出重连。
                                }
                                else //如果连接失败。
                                {
                                    String p_InfoStrPt = "初始化本端TCP协议客户端套接字，并连接远端TCP协议服务端套接字[" + m_NtwkPt.m_IPAddrStrPt + ":" + m_NtwkPt.m_PortStrPt + "]失败。原因：连接失败。原因：" + m_ErrInfoVstrPt.GetStr();
                                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                    break;
                                }

                                if( m_ReadyExitCnt != 0 ) //如果本线程接收到退出请求。
                                {
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
                                    break Out;
                                }
                            }

                            m_NtwkPt.m_TcpClntSoktPt.Dstoy( ( short ) -1, m_ErrInfoVstrPt ); //销毁本端TCP协议客户端套接字，等待重连。
                        }
                        else
                        {
                            String p_InfoStrPt = "初始化本端TCP协议客户端套接字，并连接远端TCP协议服务端套接字[" + m_NtwkPt.m_IPAddrStrPt + ":" + m_NtwkPt.m_PortStrPt + "]失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        }

                        p_CurCnctTimes++; //递增当前连接次数。
                        if( p_CurCnctTimes > m_NtwkPt.m_MaxCnctTimes )
                        {
                            m_NtwkPt.m_TcpClntSoktPt = null;

                            String p_InfoStrPt = "达到最大连接次数，中断连接。";
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }

                        if( m_ReadyExitCnt != 0 ) //如果本线程接收到退出请求。
                        {
                            m_NtwkPt.m_TcpClntSoktPt = null;
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
                            break Out;
                        }
                    }
                }

                if( m_NtwkPt.m_TcpClntSoktPt.SetNoDelay( 1, 0, m_ErrInfoVstrPt ) != 0 ) //如果设置本端TCP协议客户端套接字的Nagle延迟算法状态为禁用失败。
                {
                    String p_InfoStrPt = "设置本端TCP协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_ErrInfoVstrPt.GetStr();
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break Out;
                }

                if( m_NtwkPt.m_TcpClntSoktPt.SetSendBufSz( 1024 * 1024, 0, m_ErrInfoVstrPt ) != 0 )
                {
                    String p_InfoStrPt = "设置本端TCP协议客户端套接字的发送缓冲区大小失败。原因：" + m_ErrInfoVstrPt.GetStr();
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break Out;
                }

                if( m_NtwkPt.m_TcpClntSoktPt.SetRecvBufSz( 1024 * 1024, 0, m_ErrInfoVstrPt ) != 0 )
                {
                    String p_InfoStrPt = "设置本端TCP协议客户端套接字的接收缓冲区大小失败。原因：" + m_ErrInfoVstrPt.GetStr();
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break Out;
                }

                if( m_NtwkPt.m_TcpClntSoktPt.SetKeepAlive( 1, 1, 1, 5, 0, m_ErrInfoVstrPt ) != 0 )
                {
                    String p_InfoStrPt = "设置本端TCP协议客户端套接字的保活机制失败。原因：" + m_ErrInfoVstrPt.GetStr();
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break Out;
                }
            }
            else //如果要使用UDP协议。
            {
                m_NtwkPt.m_AudpSoktPt = new AudpSokt();
                m_NtwkPt.m_AudpCnctIdxPt = new HTLong();

                if( m_NtwkPt.m_IsCreateSrvrOrClnt == 1 ) //如果是创建本端高级UDP协议套接字接受远端高级UDP协议套接字的连接。
                {
                    if( m_NtwkPt.m_AudpSoktPt.Init( p_RmtNodeAddrFamly, m_NtwkPt.m_IPAddrStrPt, m_NtwkPt.m_PortStrPt, ( short )1, ( short )5000, m_ErrInfoVstrPt ) == 0 ) //如果初始化本端高级UDP协议套接字成功。
                    {
                        if( m_NtwkPt.m_AudpSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, m_ErrInfoVstrPt ) != 0 ) //如果获取本端高级UDP协议套接字绑定的本地节点地址和端口失败。
                        {
                            String p_InfoStrPt = "获取本端高级UDP协议套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }

                        String p_InfoStrPt = "初始化本端高级UDP协议套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]成功。";
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }
                    else //如果初始化本端高级UDP协议套接字失败。
                    {
                        String p_InfoStrPt = "初始化本端高级UDP协议套接字[" + m_NtwkPt.m_IPAddrStrPt + ":" + m_NtwkPt.m_PortStrPt + "]失败。原因：" + m_ErrInfoVstrPt.GetStr();
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break Out;
                    }

                    while( true ) //循环接受远端高级UDP协议套接字的连接。
                    {
                        if( m_NtwkPt.m_AudpSoktPt.Acpt( m_NtwkPt.m_AudpCnctIdxPt, null, p_RmtNodeAddrPt, p_RmtNodePortPt, ( short )1, m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_NtwkPt.m_AudpCnctIdxPt.m_Val != -1 ) //如果用本端高级UDP协议套接字接受远端高级UDP协议套接字的连接成功。
                            {
                                String p_InfoStrPt = "用本端高级UDP协议套接字接受远端高级UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]的连接成功。";
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break;
                            } //如果用本端高级UDP协议套接字接受远端高级UDP协议套接字的连接超时，就重新接受。
                        }
                        else
                        {
                            String p_InfoStrPt = "用本端高级UDP协议套接字接受远端高级UDP协议套接字的连接失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }

                        if( m_ReadyExitCnt != 0 ) //如果本线程接收到退出请求。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
                            break Out;
                        }
                    }
                }
                else if( m_NtwkPt.m_IsCreateSrvrOrClnt == 0 ) //如果是创建本端高级UDP协议套接字连接远端高级UDP协议套接字。
                {
                    //Ping一下远程节点地址，这样可以快速获取ARP条目。
                    try
                    {
                        Runtime.getRuntime().exec( "ping -c 1 -w 1 " + m_NtwkPt.m_IPAddrStrPt );
                    }
                    catch( Exception ignored )
                    {
                    }

                    if( m_NtwkPt.m_AudpSoktPt.Init( p_RmtNodeAddrFamly, null, null, ( short )0, ( short )5000, m_ErrInfoVstrPt ) == 0 ) //如果初始化本端高级UDP协议套接字成功。
                    {
                        if( m_NtwkPt.m_AudpSoktPt.GetLclAddr( null, p_LclNodeAddrPt, p_LclNodePortPt, m_ErrInfoVstrPt ) != 0 ) //如果获取本端高级UDP协议套接字绑定的本地节点地址和端口失败。
                        {
                            String p_InfoStrPt = "获取本端高级UDP协议套接字绑定的本地节点地址和端口失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }

                        String p_InfoStrPt = "初始化本端高级UDP协议套接字[" + p_LclNodeAddrPt.m_Val + ":" + p_LclNodePortPt.m_Val + "]成功。";
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }
                    else //如果初始化本端高级UDP协议套接字失败。
                    {
                        String p_InfoStrPt = "初始化本端高级UDP协议套接字[" + m_NtwkPt.m_IPAddrStrPt + ":" + m_NtwkPt.m_PortStrPt + "]失败。原因：" + m_ErrInfoVstrPt.GetStr();
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break Out;
                    }

                    LoopUdpCnct:
                    for( int p_CurCnctTimes = 1; ; ) //循环连接远端高级UDP协议服务端套接字。
                    {
                        //连接远端。
                        {
                            {
                                String p_InfoStrPt = "开始第 " + p_CurCnctTimes + " 次连接。";
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            }

                            if( m_NtwkPt.m_AudpSoktPt.Cnct( p_RmtNodeAddrFamly, m_NtwkPt.m_IPAddrStrPt, m_NtwkPt.m_PortStrPt, m_NtwkPt.m_AudpCnctIdxPt, m_ErrInfoVstrPt ) == 0 ) //如果连接远端高级UDP协议套接字成功。
                            {
                                HTInt p_AudpCnctSts = new HTInt();

                                while( m_NtwkPt.m_AudpSoktPt.WaitCnct( m_NtwkPt.m_AudpCnctIdxPt.m_Val, ( short )1, p_AudpCnctSts, m_ErrInfoVstrPt ) == 0 ) //循环等待本端高级UDP协议套接字连接远端成功。
                                {
                                    if( p_AudpCnctSts.m_Val == AudpSokt.AudpCnctStsWait ) //如果等待远端接受连接。
                                    {
                                        //重新循环，继续等待本端高级UDP协议套接字连接远端。
                                    }
                                    else if( p_AudpCnctSts.m_Val == AudpSokt.AudpCnctStsCnct ) //如果连接成功。
                                    {
                                        if( m_NtwkPt.m_AudpSoktPt.GetRmtAddr( m_NtwkPt.m_AudpCnctIdxPt.m_Val, null, p_RmtNodeAddrPt, p_RmtNodePortPt, m_ErrInfoVstrPt ) != 0 )
                                        {
                                            String p_InfoStrPt = "获取本端高级UDP协议客户端套接字连接的远端高级UDP协议客户端套接字绑定的远程节点地址和端口失败。原因：" + m_ErrInfoVstrPt.GetStr();
                                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                            break Out;
                                        }

                                        String p_InfoStrPt = "用本端高级UDP协议套接字连接远端高级UDP协议套接字[" + p_RmtNodeAddrPt.m_Val + ":" + p_RmtNodePortPt.m_Val + "]成功。";
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                        break LoopUdpCnct; //跳出重连。
                                    }
                                    else if( p_AudpCnctSts.m_Val == AudpSokt.AudpCnctStsTmot ) //如果连接超时。
                                    {
                                        String p_InfoStrPt = "用本端高级UDP协议套接字连接远端高级UDP协议套接字[" + m_NtwkPt.m_IPAddrStrPt + ":" + m_NtwkPt.m_PortStrPt + "]失败。原因：连接超时。";
                                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                        break;
                                    }
                                    else if( p_AudpCnctSts.m_Val == AudpSokt.AudpCnctStsDsct ) //如果连接断开。
                                    {
                                        String p_InfoStrPt = "用本端高级UDP协议套接字连接远端高级UDP协议套接字[" + m_NtwkPt.m_IPAddrStrPt + ":" + m_NtwkPt.m_PortStrPt + "]失败。原因：连接断开。";
                                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                        break;
                                    }

                                    if( m_ReadyExitCnt != 0 ) //如果本线程接收到退出请求。
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
                                        break Out;
                                    }
                                }

                                m_NtwkPt.m_AudpSoktPt.ClosCnct( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_ErrInfoVstrPt ); //关闭连接，等待重连。
                            }
                            else
                            {
                                String p_InfoStrPt = "用本端高级UDP协议套接字连接远端高级UDP协议套接字[" + m_NtwkPt.m_IPAddrStrPt + ":" + m_NtwkPt.m_PortStrPt + "]失败。原因：" + m_ErrInfoVstrPt.GetStr();
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            }
                        }

                        p_CurCnctTimes++;
                        if( p_CurCnctTimes > m_NtwkPt.m_MaxCnctTimes )
                        {
                            String p_InfoStrPt = "达到最大连接次数，中断连接。";
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }

                        if( m_ReadyExitCnt != 0 ) //如果本线程接收到退出请求。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本线程接收到退出请求，开始准备退出。" );
                            break Out;
                        }
                    }
                }

                if( m_NtwkPt.m_AudpSoktPt.SetSendBufSz( 1024 * 1024, m_ErrInfoVstrPt ) != 0 )
                {
                    String p_InfoStrPt = "设置本端高级UDP协议套接字的发送缓冲区大小失败。原因：" + m_ErrInfoVstrPt.GetStr();
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break Out;
                }

                if( m_NtwkPt.m_AudpSoktPt.SetRecvBufSz( 1024 * 1024, m_ErrInfoVstrPt ) != 0 )
                {
                    String p_InfoStrPt = "设置本端高级UDP协议套接字的接收缓冲区大小失败。原因：" + m_ErrInfoVstrPt.GetStr();
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break Out;
                }
            } //协议连接结束。

            //等待允许连接。
            if( ( m_NtwkPt.m_IsCreateSrvrOrClnt == 1 ) && ( m_IsAutoAllowCnct != 0 ) ) m_RqstCnctRslt = 1;
            else m_RqstCnctRslt = 0;
            {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgInit.ordinal();p_MessagePt.obj = p_RmtNodeAddrPt.m_Val;m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送显示请求连接对话框的消息。
            WaitAllowCnct:
            while( true )
            {
                if( m_NtwkPt.m_IsCreateSrvrOrClnt == 1 ) //如果是服务端。
                {
                    if( m_RqstCnctRslt == 1 ) //如果允许连接。
                    {
                        m_TmpBytePt[ 0 ] = ( byte ) PktTyp.AllowCnct.ordinal(); //设置允许连接包。
                        if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.SendApkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
                            ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.SendApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, 1, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
                        {
                            {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

                            String p_InfoStrPt = "发送一个允许连接包成功。";
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                            break WaitAllowCnct;
                        }
                        else
                        {
                            {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

                            String p_InfoStrPt = "发送一个允许连接包失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }
                    }
                    else if( m_RqstCnctRslt == 2 ) //如果拒绝连接。
                    {
                        m_TmpBytePt[ 0 ] = ( byte ) PktTyp.RefuseCnct.ordinal(); //设置拒绝连接包。
                        if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.SendApkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
                            ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.SendApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, 1, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
                        {
                            {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

                            String p_InfoStrPt = "发送一个拒绝连接包成功。";
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                            break Out;
                        }
                        else
                        {
                            {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

                            String p_InfoStrPt = "发送一个拒绝连接包失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }
                    }
                }
                else //如果是客户端。
                {
                    if( m_RqstCnctRslt == 2 ) //如果中断等待。
                    {
                        m_TmpBytePt[ 0 ] = ( byte ) PktTyp.RefuseCnct.ordinal(); //设置拒绝连接包。
                        if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.SendApkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
                            ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.SendApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, 1, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
                        {
                            {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

                            String p_InfoStrPt = "发送一个拒绝连接包成功。";
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                            break Out;
                        }
                        else
                        {
                            {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

                            String p_InfoStrPt = "发送一个拒绝连接包失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }
                    }
                }

                //接收一个远端发送的数据包。
                if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.RecvApkt( m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
                    ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.RecvApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, null, ( short ) 1, m_ErrInfoVstrPt ) == 0 ) ) )
                {
                    if( m_TmpHTLongPt.m_Val != -1 ) //如果用本端套接字接收一个连接的远端套接字发送的数据包成功。
                    {
                        if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[ 0 ] == ( byte ) PktTyp.AllowCnct.ordinal() ) ) //如果是允许连接包。
                        {
                            if( m_NtwkPt.m_IsCreateSrvrOrClnt == 0 ) //如果是客户端。
                            {
                                m_RqstCnctRslt = 1;

                                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

                                String p_InfoStrPt = "接收到一个允许连接包。";
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                                break WaitAllowCnct;
                            } //如果是服务端，就重新接收。
                        }
                        else if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[ 0 ] == ( byte ) PktTyp.RefuseCnct.ordinal() ) ) //如果是拒绝连接包。
                        {
                            m_RqstCnctRslt = 2;

                            {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送毁请求连接对话框的消息。

                            String p_InfoStrPt = "接收到一个拒绝连接包。";
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                            break Out;
                        } //如果是其他包，就重新接收。
                    } //如果用本端套接字接收一个连接的远端套接字发送的数据包超时，就重新接收。
                }
                else //如果用本端套接字接收一个连接的远端套接字发送的数据包失败。
                {
                    {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.RqstCnctDlgDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁请求连接对话框的消息。

                    String p_InfoStrPt = "用本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_ErrInfoVstrPt.GetStr();
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break Out;
                }
            } //等待允许连接结束。

            m_LastSendAdoInptFrmIsAct = 0; //设置最后发送的一个音频输入帧为无语音活动。
            m_LastSendAdoInptFrmTimeStamp = 0 - 1; //设置最后一个发送音频输入帧的时间戳为0的前一个，因为第一次发送音频输入帧时会递增一个步进。
            m_LastSendVdoInptFrmTimeStamp = 0 - 1; //设置最后一个发送视频输入帧的时间戳为0的前一个，因为第一次发送视频输入帧时会递增一个步进。

            switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
            {
                case 0: //如果要使用容器。
                {
                    //初始化接收音频输出帧容器。
                    m_RecvAdoOtptFrmCntnrPt = new ConcurrentLinkedQueue< byte[] >(); //创建接收音频输出帧容器。
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "初始化接收音频输出帧容器对象成功。" );

                    //初始化接收视频输出帧容器。
                    m_RecvVdoOtptFrmCntnrPt = new ConcurrentLinkedQueue< byte[] >(); //创建接收视频输出帧容器。
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "初始化接收视频输出帧容器对象成功。" );
                    break;
                }
                case 1: //如果要使用自适应抖动缓冲器。
                {
                    //初始化音频自适应抖动缓冲器。
                    m_AAjbPt.m_Pt = new HeavenTao.Ado.AAjb();
                    if( m_AAjbPt.m_Pt.Init( m_AdoOtptPt.m_SmplRate, m_AdoOtptPt.m_FrmLenUnit, 1, 1, 0, m_AAjbPt.m_MinNeedBufFrmCnt, m_AAjbPt.m_MaxNeedBufFrmCnt, m_AAjbPt.m_MaxCntuLostFrmCnt, m_AAjbPt.m_AdaptSensitivity, ( m_NtwkPt.m_XfrMode == 0 ) ? 0 : 1, m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "初始化音频自适应抖动缓冲器成功。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "初始化音频自适应抖动缓冲器失败。原因：" + m_ErrInfoVstrPt.GetStr();
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break Out;
                    }

                    //初始化视频自适应抖动缓冲器。
                    m_VAjbPt.m_Pt = new HeavenTao.Vdo.VAjb();
                    if( m_VAjbPt.m_Pt.Init( 1, m_VAjbPt.m_MinNeedBufFrmCnt, m_VAjbPt.m_MaxNeedBufFrmCnt, m_VAjbPt.m_AdaptSensitivity, m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "初始化视频自适应抖动缓冲器成功。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "初始化视频自适应抖动缓冲器失败。原因：" + m_ErrInfoVstrPt.GetStr();
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        break Out;
                    }
                    break;
                }
            }

            {
                String p_InfoStrPt = "开始对讲。";
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
            }

            {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.Vibrate.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
            if( m_NtwkPt.m_XfrMode == 0 ) {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.PttBtnInit.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送初始化一键即按即通按钮的消息。
            SendUserMsg( UserMsgTyp.LclTkbkMode, TkbkMode.NoChg ); //发送对讲模式包。

            p_Rslt = 0; //设置本函数执行成功。
        }

        return p_Rslt;
    }

    //用户定义的处理函数。
    @Override public int UserPocs()
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。
        int p_TmpInt;
        int p_TmpElmTotal;

        Out:
        {
            //接收远端发送过来的一个数据包。
            if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.RecvApkt( m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 0, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
                ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.RecvApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, null, ( short ) 0, m_ErrInfoVstrPt ) == 0 ) ) )
            {
                if( m_TmpHTLongPt.m_Val != -1 ) //如果用本端套接字接收一个连接的远端套接字发送的数据包成功。
                {
                    if( m_TmpHTLongPt.m_Val == 0 ) //如果数据包的数据长度为0。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "接收到一个数据包的数据长度为" + m_TmpHTLongPt.m_Val + "，表示没有数据，无法继续接收。" );
                        break Out;
                    }
                    else if( m_TmpBytePt[ 0 ] == ( byte ) PktTyp.TkbkMode.ordinal() ) //如果是对讲模式包。
                    {
                        if( m_TmpHTLongPt.m_Val < 1 + 1 ) //如果音频输出帧包的数据长度小于1 + 1，表示没有对讲模式。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "接收到一个对讲模式包的数据长度为" + m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲模式，无法继续接收。" );
                            break Out;
                        }
                        if( m_TmpBytePt[ 1 ] >= TkbkMode.NoChg.ordinal() )
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "接收到一个对讲模式包的对讲模式为" + m_TmpBytePt[ 1 ] + "不正确，无法继续接收。" );
                            break Out;
                        }

                        m_RmtTkbkMode = TkbkMode.values()[ m_TmpBytePt[ 1 ] ]; //设置远端对讲模式。
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个对讲模式包。对讲模式：" + m_RmtTkbkMode );
                        SetTkbkMode(); //设置对讲模式。
                    }
                    else if( m_TmpBytePt[ 0 ] == ( byte ) PktTyp.AdoFrm.ordinal() ) //如果是音频输出帧包。
                    {
                        if( m_TmpHTLongPt.m_Val < 1 + 4 ) //如果音频输出帧包的数据长度小于1 + 4，表示没有音频输出帧时间戳。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "接收到一个音频输出帧包的数据长度为" + m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有音频输出帧时间戳，无法继续接收。" );
                            break Out;
                        }

                        //读取音频输出帧时间戳。
                        p_TmpInt = ( m_TmpBytePt[ 1 ] & 0xFF ) + ( ( m_TmpBytePt[ 2 ] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[ 3 ] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[ 4 ] & 0xFF ) << 24 );

                        if( ( m_AdoOtptPt.m_IsInit != 0 ) || //如果已初始化音频输出。
                            ( ( m_NtwkPt.m_XfrMode == 0 ) && ( ( m_LclTkbkMode == TkbkMode.Ado ) || ( m_LclTkbkMode == TkbkMode.AdoVdo ) ) ) ) //如果传输模式为实时半双工（一键通），且本端对讲模式为音频或音视频。
                        {
                            //将音频输出帧放入容器或自适应抖动缓冲器。
                            switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
                            {
                                case 0: //如果要使用容器。
                                {
                                    if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                                    {
                                        p_TmpElmTotal = m_RecvAdoOtptFrmCntnrPt.size(); //获取接收音频输出帧容器的元素总数。
                                        if( p_TmpElmTotal <= 50 )
                                        {
                                            m_RecvAdoOtptFrmCntnrPt.offer( Arrays.copyOfRange( m_TmpBytePt, 1 + 4, ( int ) ( m_TmpHTLongPt.m_Val ) ) );
                                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个有语音活动的音频输出帧包，并放入接收音频输出帧容器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                        }
                                        else
                                        {
                                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "接收到一个有语音活动的音频输出帧包，但接收音频输出帧容器中帧总数为" + p_TmpElmTotal + "已经超过上限50，不再放入接收音频输出帧容器。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                        }
                                    }
                                    else //如果该音频输出帧为无语音活动。
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个无语音活动的音频输出帧包，无需放入接收音频输出帧容器。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    break;
                                }
                                case 1: //如果要使用自适应抖动缓冲器。
                                {
                                    if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                                    {
                                        m_AAjbPt.m_Pt.PutByteFrm( p_TmpInt, m_TmpBytePt, 1 + 4, m_TmpHTLongPt.m_Val - 1 - 4, 1, null );
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个有语音活动的音频输出帧包，并放入音频自适应抖动缓冲器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    else //如果该音频输出帧为无语音活动。
                                    {
                                        m_AAjbPt.m_Pt.PutByteFrm( p_TmpInt, m_TmpBytePt, 1 + 4, 0, 1, null );
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个无语音活动的音频输出帧包，并放入音频自适应抖动缓冲器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }

                                    HTInt p_CurHaveBufActFrmCntPt = new HTInt(); //存放当前已缓冲有活动帧的数量。
                                    HTInt p_CurHaveBufInactFrmCntPt = new HTInt(); //存放当前已缓冲无活动帧的数量。
                                    HTInt p_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
                                    HTInt p_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
                                    HTInt p_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
                                    HTInt p_MaxCntuLostFrmCntPt = new HTInt(); //存放最大连续丢失帧的数量。
                                    HTInt p_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。
                                    m_AAjbPt.m_Pt.GetBufFrmCnt( p_CurHaveBufActFrmCntPt, p_CurHaveBufInactFrmCntPt, p_CurHaveBufFrmCntPt, p_MinNeedBufFrmCntPt, p_MaxNeedBufFrmCntPt, p_MaxCntuLostFrmCntPt, p_CurNeedBufFrmCntPt, 1, null );
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频自适应抖动缓冲器：有活动帧：" + p_CurHaveBufActFrmCntPt.m_Val + "，无活动帧：" + p_CurHaveBufInactFrmCntPt.m_Val + "，帧：" + p_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrmCntPt.m_Val + "，最大丢帧：" + p_MaxCntuLostFrmCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrmCntPt.m_Val + "。" );
                                    break;
                                }
                            }
                        }
                        else //如果未初始化音频输出。
                        {
                            if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个有语音活动的音频输出帧包成功，但未初始化音频输出。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                            }
                            else //如果该音频输出帧为无语音活动。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个无语音活动的音频输出帧包成功，但未初始化音频输出。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                            }
                        }
                    }
                    else if( m_TmpBytePt[ 0 ] == ( byte ) PktTyp.VdoFrm.ordinal() ) //如果是视频输出帧包。
                    {
                        if( m_TmpHTLongPt.m_Val < 1 + 4 ) //如果视频输出帧包的数据长度小于1 + 4，表示没有视频输出帧时间戳。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "接收到一个视频输出帧包的数据长度为" + m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有视频输出帧时间戳，无法继续接收。" );
                            break Out;
                        }

                        //读取视频输出帧时间戳。
                        p_TmpInt = ( m_TmpBytePt[ 1 ] & 0xFF ) + ( ( m_TmpBytePt[ 2 ] & 0xFF ) << 8 ) + ( ( m_TmpBytePt[ 3 ] & 0xFF ) << 16 ) + ( ( m_TmpBytePt[ 4 ] & 0xFF ) << 24 );

                        if( ( m_VdoOtptPt.m_IsInit != 0 ) || //如果已初始化视频输出。
                            ( ( m_NtwkPt.m_XfrMode == 0 ) && ( ( m_LclTkbkMode == TkbkMode.Vdo ) || ( m_LclTkbkMode == TkbkMode.AdoVdo ) ) ) ) //如果传输模式为实时半双工（一键通），且本端对讲模式为视频或音视频。
                        {
                            //将视频输出帧放入容器或自适应抖动缓冲器。
                            switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
                            {
                                case 0: //如果要使用容器。
                                {
                                    if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                                    {
                                        p_TmpElmTotal = m_RecvVdoOtptFrmCntnrPt.size(); //获取接收视频输出帧容器的元素总数。
                                        if( p_TmpElmTotal <= 20 )
                                        {
                                            m_RecvVdoOtptFrmCntnrPt.offer( Arrays.copyOfRange( m_TmpBytePt, 1 + 4, ( int ) ( m_TmpHTLongPt.m_Val ) ) );
                                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个有图像活动的视频输出帧包，并放入接收视频输出帧容器成功。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                        }
                                        else
                                        {
                                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "接收到一个有图像活动的视频输出帧包，但接收视频输出帧容器中帧总数为" + p_TmpElmTotal + "已经超过上限20，不再放入接收视频输出帧容器。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                        }
                                    }
                                    else //如果该视频输出帧为无图像活动。
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个无图像活动的视频输出帧包，无需放入接收视频输出帧容器。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    break;
                                }
                                case 1: //如果要使用自适应抖动缓冲器。
                                {
                                    if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                                    {
                                        m_VAjbPt.m_Pt.PutByteFrm( SystemClock.uptimeMillis(), p_TmpInt, m_TmpBytePt, 1 + 4, m_TmpHTLongPt.m_Val - 1 - 4, 1, null );
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个有图像活动的视频输出帧包，并放入视频自适应抖动缓冲器成功。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "，类型：" + ( m_TmpBytePt[ 9 ] & 0xff ) + "。" );
                                    }
                                    else //如果该视频输出帧为无图像活动。
                                    {
                                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个无图像活动的视频输出帧包，无需放入视频自适应抖动缓冲器。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                                    }

                                    HTInt p_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
                                    HTInt p_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
                                    HTInt p_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
                                    HTInt p_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。
                                    m_VAjbPt.m_Pt.GetBufFrmCnt( p_CurHaveBufFrmCntPt, p_MinNeedBufFrmCntPt, p_MaxNeedBufFrmCntPt, p_CurNeedBufFrmCntPt, 1, null );
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频自适应抖动缓冲器：帧：" + p_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrmCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrmCntPt.m_Val + "。" );
                                    break;
                                }
                            }
                        }
                        else //如果未初始化视频输出。
                        {
                            if( m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个有图像活动的视频输出帧包成功，但未初始化视频输出。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                            }
                            else //如果该视频输出帧为无图像活动。
                            {
                                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "接收到一个无图像活动的视频输出帧包成功，但未初始化视频输出。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_TmpHTLongPt.m_Val + "。" );
                            }
                        }
                    }
                    else if( m_TmpBytePt[ 0 ] == ( byte ) PktTyp.Exit.ordinal() ) //如果是退出包。
                    {
                        if( m_TmpHTLongPt.m_Val > 1 ) //如果退出包的数据长度大于1。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "接收到一个退出包的数据长度为" + m_TmpHTLongPt.m_Val + "大于1，表示还有其他数据，无法继续接收。" );
                            break Out;
                        }

                        m_IsRecvExitPkt = 1; //设置已经接收到退出包。
                        RqirExit( 1, 0 ); //请求退出。

                        String p_InfoStrPt = "接收到一个退出包。";
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                        if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
                    }
                } //如果用本端套接字接收一个连接的远端套接字发送的数据包超时，就重新接收。
            }
            else //如果用本端套接字接收一个连接的远端套接字发送的数据包失败。
            {
                String p_InfoStrPt = "用本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_ErrInfoVstrPt.GetStr();
                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                break Out;
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        return p_Rslt;
    }

    //用户定义的销毁函数。
    @Override public void UserDstoy()
    {
        if( ( m_NtwkPt.m_TcpClntSoktPt != null ) || ( ( m_NtwkPt.m_AudpSoktPt != null ) && ( m_NtwkPt.m_AudpSoktPt.GetRmtAddr( m_NtwkPt.m_AudpCnctIdxPt.m_Val, null, null, null, null ) == 0 ) ) ) //如果本端TCP协议客户端套接字不为空或本端高级UDP协议套接字不为空且已连接远端。
        {
            OutExitPkt:
            {
                //发送退出包。
                m_TmpBytePt[ 0 ] = ( byte ) PktTyp.Exit.ordinal(); //设置退出包。
                if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.SendApkt( m_TmpBytePt, 1, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) != 0 ) ) ||
                    ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.SendApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, 1, 1, 1, m_ErrInfoVstrPt ) != 0 ) ) )
                {
                    String p_InfoStrPt = "发送一个退出包失败。原因：" + m_ErrInfoVstrPt.GetStr();
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    break OutExitPkt;
                }

                {
                    String p_InfoStrPt = "发送一个退出包成功。";
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                }

                //接收退出包。
                if( m_IsRecvExitPkt == 0 ) //如果没有接收到退出包。
                {
                    while( true ) //循环接收退出包。
                    {
                        if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.RecvApkt( m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, ( short ) 5000, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
                            ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.RecvApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, m_TmpBytePt.length, m_TmpHTLongPt, null, ( short ) 5000, m_ErrInfoVstrPt ) == 0 ) ) )
                        {
                            if( m_TmpHTLongPt.m_Val != -1 ) //如果用本端套接字接收一个连接的远端套接字发送的数据包成功。
                            {
                                if( ( m_TmpHTLongPt.m_Val == 1 ) && ( m_TmpBytePt[ 0 ] == ( byte ) PktTyp.Exit.ordinal() ) ) //如果是退出包。
                                {
                                    String p_InfoStrPt = "接收到一个退出包。";
                                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                    break OutExitPkt;
                                } //如果是其他包，就继续接收。
                            }
                            else //如果用本端套接字接收一个连接的远端套接字发送的数据包超时。
                            {
                                String p_InfoStrPt = "用本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_ErrInfoVstrPt.GetStr();
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                                break OutExitPkt;
                            }
                        }
                        else //如果用本端套接字接收一个连接的远端套接字发送的数据包失败。
                        {
                            String p_InfoStrPt = "用本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break OutExitPkt;
                        }
                    }
                }
            }

            String p_InfoStrPt = "中断对讲。";
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
            if( m_IsShowToast != 0 ) m_ShowToastActivityPt.runOnUiThread( new Runnable() { public void run() { Toast.makeText( m_ShowToastActivityPt, p_InfoStrPt, Toast.LENGTH_LONG ).show(); } } );
        }

        //销毁本端TCP协议服务端套接字。
        if( m_NtwkPt.m_TcpSrvrSoktPt != null )
        {
            m_NtwkPt.m_TcpSrvrSoktPt.Dstoy( null ); //关闭并销毁本端TCP协议服务端套接字。
            m_NtwkPt.m_TcpSrvrSoktPt = null;

            String p_InfoStrPt = "关闭并销毁本端TCP协议服务端套接字成功。";
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
        }

        //销毁本端TCP协议客户端套接字。
        if( m_NtwkPt.m_TcpClntSoktPt != null )
        {
            m_NtwkPt.m_TcpClntSoktPt.Dstoy( ( short ) -1, null ); //关闭并销毁本端TCP协议客户端套接字。
            m_NtwkPt.m_TcpClntSoktPt = null;

            String p_InfoStrPt = "关闭并销毁本端TCP协议客户端套接字成功。";
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
        }

        //销毁本端高级UDP协议套接字。
        if( m_NtwkPt.m_AudpSoktPt != null )
        {
            m_NtwkPt.m_AudpSoktPt.Dstoy( null ); //关闭并销毁本端高级UDP协议套接字。
            m_NtwkPt.m_AudpSoktPt = null;
            m_NtwkPt.m_AudpCnctIdxPt = null;

            String p_InfoStrPt = "关闭并销毁本端高级UDP协议套接字成功。";
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
        }

        //销毁接收音频输出帧容器。
        if( m_RecvAdoOtptFrmCntnrPt != null )
        {
            m_RecvAdoOtptFrmCntnrPt.clear();
            m_RecvAdoOtptFrmCntnrPt = null;
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "销毁接收音频输出帧容器成功。" );
        }

        //销毁接收视频输出帧容器。
        if( m_RecvVdoOtptFrmCntnrPt != null )
        {
            m_RecvVdoOtptFrmCntnrPt.clear();
            m_RecvVdoOtptFrmCntnrPt = null;
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "销毁接收视频输出帧容器成功。" );
        }

        //销毁音频自适应抖动缓冲器。
        if( m_AAjbPt.m_Pt != null )
        {
            m_AAjbPt.m_Pt.Dstoy( null );
            m_AAjbPt.m_Pt = null;
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "销毁音频自适应抖动缓冲器成功。" );
        }

        //销毁视频自适应抖动缓冲器。
        if( m_VAjbPt.m_Pt != null )
        {
            m_VAjbPt.m_Pt.Dstoy( null );
            m_VAjbPt.m_Pt = null;
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "销毁视频自适应抖动缓冲器成功。" );
        }

        if( m_NtwkPt.m_IsCreateSrvrOrClnt == 1 ) //如果是创建服务端。
        {
            if( m_IsRecvExitPkt == 1 )
            {
                String p_InfoStrPt = "由于是创建服务端，且接收到了退出包，表示是远端套接字主动退出，本线程重新初始化来继续保持监听。";
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

                RqirExit( 2, 0 ); //请求重启。
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.Vibrate.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
                if( m_NtwkPt.m_XfrMode == 0 ) {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.PttBtnDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁一键即按即通按钮的消息。
            }
            else if( ( m_IsInterrupt == 0 ) && ( m_ExitCode == ExitCode.UserInit ) && ( m_RqstCnctRslt == 2 ) )
            {
                String p_InfoStrPt = "由于是创建服务端，且未中断，且退出码为调用用户定义的初始化函数失败，且请求连接的结果为拒绝，表示是拒绝本次连接，本线程重新初始化来继续保持监听。";
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

                RqirExit( 2, 0 ); //请求重启。
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.Vibrate.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
                if( m_NtwkPt.m_XfrMode == 0 ) {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.PttBtnDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁一键即按即通按钮的消息。
            }
            else if( ( m_IsInterrupt == 0 ) && ( ( m_ExitCode == ExitCode.MediaMsgPocs ) || ( m_ExitCode == ExitCode.AdoVdoInptOtptPocs ) ) )
            {
                String p_InfoStrPt = "由于是创建服务端，且未中断，且退出码为媒体消息处理失败或音视频输入输出处理失败，表示是媒体消息处理失败或连接异常断开，本线程重新初始化来继续保持监听。";
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

                RqirExit( 2, 0 ); //请求重启。
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.Vibrate.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
                if( m_NtwkPt.m_XfrMode == 0 ) {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.PttBtnDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁一键即按即通按钮的消息。
            }
            else //其他情况，本线程直接退出。
            {
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.MediaPocsThrdDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁媒体处理线程的消息。
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.Vibrate.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
                if( m_NtwkPt.m_XfrMode == 0 ) {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.PttBtnDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁一键即按即通按钮的消息。
            }
        }
        else if( m_NtwkPt.m_IsCreateSrvrOrClnt == 0 ) //如果是创建客户端。
        {
            if( ( m_IsInterrupt == 0 ) && ( ( m_ExitCode == ExitCode.MediaMsgPocs ) || ( m_ExitCode == ExitCode.AdoVdoInptOtptPocs ) ) )
            {
                String p_InfoStrPt = "由于是创建客户端，且未中断，且退出码为媒体消息处理失败或音视频输入输出处理失败，表示是媒体消息处理失败或连接异常断开，本线程重新初始化来重连服务端。";
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );}

                RqirExit( 2, 0 ); //请求重启。
                if( m_NtwkPt.m_XfrMode == 0 ) {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.PttBtnDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁一键即按即通按钮的消息。
            }
            else //其他情况，本线程直接退出。
            {
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.MediaPocsThrdDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁媒体处理线程的消息。
                {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.Vibrate.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
                if( m_NtwkPt.m_XfrMode == 0 ) {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.PttBtnDstoy.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送销毁一键即按即通按钮的消息。
            }
        }
    }

    //设置对讲模式。
    public void SetTkbkMode()
    {
        if( m_NtwkPt.m_XfrMode == 0 ) //如果传输模式为实时半双工（一键通）。
        {
            if( m_NtwkPt.m_PttBtnIsDown == 0 ) //如果一键即按即通按钮为弹起。
            {
                switch( m_LclTkbkMode )
                {
                    case None: //如果本端对讲模式为空。
                    {
                        SetIsUseAdoVdoInptOtpt( 0, 0, 0, 0 ); //设置是否使用音视频输入输出。
                        break;
                    }
                    case Ado: //如果本端对讲模式为音频。
                    {
                        SetIsUseAdoVdoInptOtpt( 0, 1, 0, 0 ); //设置是否使用音视频输入输出。
                        break;
                    }
                    case Vdo: //如果本端对讲模式为视频。
                    {
                        SetIsUseAdoVdoInptOtpt( 0, 0, 0, 1 ); //设置是否使用音视频输入输出。
                        break;
                    }
                    case AdoVdo: //如果本端对讲模式为音视频。
                    {
                        SetIsUseAdoVdoInptOtpt( 0, 1, 0, 1 ); //设置是否使用音视频输入输出。
                        break;
                    }
                }
            }
            else //如果一键即按即通按钮为按下。
            {
                switch( m_LclTkbkMode )
                {
                    case None: //如果本端对讲模式为空。
                    {
                        SetIsUseAdoVdoInptOtpt( 0, 0, 0, 0 ); //设置是否使用音视频输入输出。
                        break;
                    }
                    case Ado: //如果本端对讲模式为音频。
                    {
                        SetIsUseAdoVdoInptOtpt( 1, 0, 0, 0 ); //设置是否使用音视频输入输出。
                        break;
                    }
                    case Vdo: //如果本端对讲模式为视频。
                    {
                        SetIsUseAdoVdoInptOtpt( 0, 0, 1, 0 ); //设置是否使用音视频输入输出。
                        break;
                    }
                    case AdoVdo: //如果本端对讲模式为音视频。
                    {
                        SetIsUseAdoVdoInptOtpt( 1, 0, 1, 0 ); //设置是否使用音视频输入输出。
                        break;
                    }
                }
            }
        }
        else //如果传输模式为实时全双工。
        {
            switch( m_LclTkbkMode )
            {
                case None: //如果本端对讲模式为空。
                {
                    SetIsUseAdoVdoInptOtpt( 0, 0, 0, 0 ); //设置是否使用音视频输入输出。
                    break;
                }
                case Ado: //如果本端对讲模式为音频。
                {
                    switch( m_RmtTkbkMode )
                    {
                        case None: //如果远端对讲模式为空。
                        {
                            SetIsUseAdoVdoInptOtpt( 1, 0, 0, 0 ); //设置是否使用音视频输入输出。
                            break;
                        }
                        case Ado: //如果远端对讲模式为音频。
                        {
                            SetIsUseAdoVdoInptOtpt( 1, 1, 0, 0 ); //设置是否使用音视频输入输出。
                            break;
                        }
                        case Vdo: //如果远端对讲模式为视频。
                        {
                            SetIsUseAdoVdoInptOtpt( 1, 0, 0, 0 ); //设置是否使用音视频输入输出。
                            break;
                        }
                        case AdoVdo: //如果远端对讲模式为音视频。
                        {
                            SetIsUseAdoVdoInptOtpt( 1, 1, 0, 0 ); //设置是否使用音视频输入输出。
                            break;
                        }
                    }
                    break;
                }
                case Vdo: //如果本端对讲模式为视频。
                {
                    switch( m_RmtTkbkMode )
                    {
                        case None: //如果远端对讲模式为空。
                        {
                            SetIsUseAdoVdoInptOtpt( 0, 0, 1, 0 ); //设置是否使用音视频输入输出。
                            break;
                        }
                        case Ado: //如果远端对讲模式为音频。
                        {
                            SetIsUseAdoVdoInptOtpt( 0, 0, 1, 0 ); //设置是否使用音视频输入输出。
                            break;
                        }
                        case Vdo: //如果远端对讲模式为视频。
                        {
                            SetIsUseAdoVdoInptOtpt( 0, 0, 1, 1 ); //设置是否使用音视频输入输出。
                            break;
                        }
                        case AdoVdo: //如果远端对讲模式为音视频。
                        {
                            SetIsUseAdoVdoInptOtpt( 0, 0, 1, 1 ); //设置是否使用音视频输入输出。
                            break;
                        }
                    }
                    break;
                }
                case AdoVdo: //如果本端对讲模式为音视频。
                {
                    switch( m_RmtTkbkMode )
                    {
                        case None: //如果远端对讲模式为空。
                        {
                            SetIsUseAdoVdoInptOtpt( 1, 0, 1, 0 ); //设置是否使用音视频输入输出。
                            break;
                        }
                        case Ado: //如果远端对讲模式为音频。
                        {
                            SetIsUseAdoVdoInptOtpt( 1, 1, 1, 0 ); //设置是否使用音视频输入输出。
                            break;
                        }
                        case Vdo: //如果远端对讲模式为视频。
                        {
                            SetIsUseAdoVdoInptOtpt( 1, 0, 1, 1 ); //设置是否使用音视频输入输出。
                            break;
                        }
                        case AdoVdo: //如果远端对讲模式为音视频。
                        {
                            SetIsUseAdoVdoInptOtpt( 1, 1, 1, 1 ); //设置是否使用音视频输入输出。
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    //用户定义的消息函数。
    public int UserMsg( Object MsgArgPt[] )
    {
        int p_Rslt = -1; //存放本函数执行结果的值，为0表示成功，为非0表示失败。

        Out:
        {
            switch( ( UserMsgTyp ) MsgArgPt[ 0 ] )
            {
                case LclTkbkMode:
                {
                    if( ( TkbkMode )MsgArgPt[ 1 ] != TkbkMode.NoChg ) m_LclTkbkMode = ( TkbkMode )MsgArgPt[ 1 ]; //设置本端对讲模式。
                    SetTkbkMode(); //设置对讲模式。
                    if( ( m_NtwkPt.m_TcpClntSoktPt != null ) || ( ( m_NtwkPt.m_AudpSoktPt != null ) && ( m_NtwkPt.m_AudpSoktPt.GetRmtAddr( m_NtwkPt.m_AudpCnctIdxPt.m_Val, null, null, null, null ) == 0 ) ) ) //如果本端TCP协议客户端套接字不为空或本端高级UDP协议套接字不为空且已连接远端。
                    {
                        //发送对讲模式包。
                        m_TmpBytePt[ 0 ] = ( byte ) PktTyp.TkbkMode.ordinal(); //设置对讲模式包。
                        m_TmpBytePt[ 1 ] = ( byte ) m_LclTkbkMode.ordinal(); //设置对讲模式。
                        if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.SendApkt( m_TmpBytePt, 2, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) != 0 ) ) ||
                            ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.SendApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, 2, 1, 1, m_ErrInfoVstrPt ) != 0 ) ) )
                        {
                            String p_InfoStrPt = "发送一个对讲模式包失败。原因：" + m_ErrInfoVstrPt.GetStr();
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                            break Out;
                        }
                        else
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "发送一个对讲模式包成功。对讲模式：" + m_LclTkbkMode );
                        }
                    }
                    break;
                }
                case RmtTkbkMode:
                {
                    if( ( TkbkMode )MsgArgPt[ 1 ] != TkbkMode.NoChg ) m_RmtTkbkMode = ( TkbkMode )MsgArgPt[ 1 ]; //设置远端对讲模式。
                    SetTkbkMode(); //设置对讲模式。
                    break;
                }
                case PttBtnDown:
                {
                    m_NtwkPt.m_PttBtnIsDown = 1; //设置一键即按即通按钮为按下。
                    SetTkbkMode(); //设置对讲模式。
                    {Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.Vibrate.ordinal();m_MainActivityHandlerPt.sendMessage( p_MessagePt );} //向主界面发送振动的消息。
                    break;
                }
                case PttBtnUp:
                {
                    m_NtwkPt.m_PttBtnIsDown = 0; //设置一键即按即通按钮为弹起。
                    SetTkbkMode(); //设置对讲模式。
                    break;
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        return p_Rslt;
    }

    //用户定义的读取音视频输入帧函数。
    @Override public void UserReadAdoVdoInptFrm( short AdoInptPcmSrcFrmPt[], short AdoInptPcmRsltFrmPt[], long AdoInptPcmFrmLenUnit, int AdoInptPcmRsltFrmVoiceActSts,
                                                 byte AdoInptEncdRsltFrmPt[], long AdoInptEncdRsltFrmLenByt, int AdoInptEncdRsltFrmIsNeedTrans,
                                                 byte VdoInptNv21SrcFrmPt[], int VdoInptNv21SrcFrmWidthPt, int VdoInptNv21SrcFrmHeightPt, long VdoInptNv21SrcFrmLenByt,
                                                 byte VdoInptYu12RsltFrmPt[], int VdoInptYu12RsltFrmWidth, int VdoInptYu12RsltFrmHeight, long VdoInptYu12RsltFrmLenByt,
                                                 byte VdoInptEncdRsltFrmPt[], long VdoInptEncdRsltFrmLenByt )
    {
        int p_FrmPktLen = 0; //存放输入输出帧数据包的数据长度，单位字节。
        int p_TmpInt32 = 0;

        //发送音频输入帧。
        if( AdoInptPcmSrcFrmPt != null ) //如果有音频输入Pcm格式原始帧。
        {
            if( AdoInptEncdRsltFrmPt == null ) //如果没有音频输入已编码格式结果帧。
            {
                if( AdoInptPcmRsltFrmVoiceActSts != 0 ) //如果本次音频输入帧为有语音活动。
                {
                    for( p_TmpInt32 = 0; p_TmpInt32 < AdoInptPcmRsltFrmPt.length; p_TmpInt32++ ) //设置音频输入帧。
                    {
                        m_TmpBytePt[ 1 + 4 + p_TmpInt32 * 2 ] = ( byte ) ( AdoInptPcmRsltFrmPt[ p_TmpInt32 ] & 0xFF );
                        m_TmpBytePt[ 1 + 4 + p_TmpInt32 * 2 + 1 ] = ( byte ) ( ( AdoInptPcmRsltFrmPt[ p_TmpInt32 ] & 0xFF00 ) >> 8 );
                    }
                    p_FrmPktLen = 1 + 4 + AdoInptPcmRsltFrmPt.length * 2; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 音频输入Pcm格式结果帧。
                }
                else //如果本次音频输入帧为无语音活动，或不需要传输。
                {
                    p_FrmPktLen = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
                }
            }
            else //如果有音频输入已编码格式结果帧。
            {
                if( AdoInptPcmRsltFrmVoiceActSts != 0 && AdoInptEncdRsltFrmIsNeedTrans != 0 ) //如果本次音频输入帧为有语音活动，且需要传输。
                {
                    System.arraycopy( AdoInptEncdRsltFrmPt, 0, m_TmpBytePt, 1 + 4, ( int ) AdoInptEncdRsltFrmLenByt ); //设置音频输入帧。
                    p_FrmPktLen = 1 + 4 + ( int ) AdoInptEncdRsltFrmLenByt; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 音频输入已编码格式结果帧。
                }
                else //如果本次音频输入帧为无语音活动，或不需要传输。
                {
                    p_FrmPktLen = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
                }
            }

            if( p_FrmPktLen != 1 + 4 ) //如果本音频输入帧为有语音活动，就发送。
            {
                m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

                //设置数据包类型为音频输入帧包。
                m_TmpBytePt[ 0 ] = ( byte ) PktTyp.AdoFrm.ordinal();
                //设置音频输入帧时间戳。
                m_TmpBytePt[ 1 ] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
                m_TmpBytePt[ 2 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
                m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
                m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

                if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.SendApkt( m_TmpBytePt, p_FrmPktLen, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
                    ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.SendApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, p_FrmPktLen, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) )
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "发送一个有语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。" );
                }
                else
                {
                    String p_InfoStrPt = "发送一个有语音活动的音频输入帧包失败。原因：" + m_ErrInfoVstrPt.GetStr() + "音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。";
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                }

                m_LastSendAdoInptFrmIsAct = 1; //设置最后一个发送的音频输入帧有语音活动。
            }
            else //如果本音频输入帧为无语音活动。
            {
                if( m_LastSendAdoInptFrmIsAct != 0 ) //如果最后一个发送的音频输入帧为有语音活动，就发送。
                {
                    m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

                    //设置数据包类型为音频输入帧包。
                    m_TmpBytePt[ 0 ] = ( byte ) PktTyp.AdoFrm.ordinal();
                    //设置音频输入帧时间戳。
                    m_TmpBytePt[ 1 ] = ( byte ) ( m_LastSendAdoInptFrmTimeStamp & 0xFF );
                    m_TmpBytePt[ 2 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
                    m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
                    m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

                    if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.SendApkt( m_TmpBytePt, p_FrmPktLen, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
                        ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.SendApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, p_FrmPktLen, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "发送一个无语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "发送一个无语音活动的音频输入帧包失败。原因：" + m_ErrInfoVstrPt.GetStr() + "音频输入帧时间戳：" + m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。";
                        if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                    }

                    m_LastSendAdoInptFrmIsAct = 0; //设置最后一个发送的音频输入帧无语音活动。
                }
                else //如果最后一个发送的音频输入帧为无语音活动，无需发送。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本音频输入帧为无语音活动，且最后一个发送的音频输入帧为无语音活动，无需发送。" );
                }
            }
        }

        //发送视频输入帧。
        if( VdoInptYu12RsltFrmPt != null ) //如果有视频输入Yu12格式结果帧。
        {
            if( VdoInptEncdRsltFrmPt == null ) //如果没有视频输入已编码格式结果帧。
            {
                //设置视频输入帧宽度。
                m_TmpBytePt[ 5 ] = ( byte ) ( VdoInptYu12RsltFrmWidth & 0xFF );
                m_TmpBytePt[ 6 ] = ( byte ) ( ( VdoInptYu12RsltFrmWidth & 0xFF00 ) >> 8 );
                m_TmpBytePt[ 7 ] = ( byte ) ( ( VdoInptYu12RsltFrmWidth & 0xFF0000 ) >> 16 );
                m_TmpBytePt[ 8 ] = ( byte ) ( ( VdoInptYu12RsltFrmWidth & 0xFF000000 ) >> 24 );
                //设置视频输入帧高度。
                m_TmpBytePt[ 9 ] = ( byte ) ( VdoInptYu12RsltFrmHeight & 0xFF );
                m_TmpBytePt[ 10 ] = ( byte ) ( ( VdoInptYu12RsltFrmHeight & 0xFF00 ) >> 8 );
                m_TmpBytePt[ 11 ] = ( byte ) ( ( VdoInptYu12RsltFrmHeight & 0xFF0000 ) >> 16 );
                m_TmpBytePt[ 12 ] = ( byte ) ( ( VdoInptYu12RsltFrmHeight & 0xFF000000 ) >> 24 );

                System.arraycopy( VdoInptYu12RsltFrmPt, 0, m_TmpBytePt, 1 + 4 + 4 + 4, VdoInptYu12RsltFrmPt.length ); //设置视频输入帧。
                p_FrmPktLen = 1 + 4 + 4 + 4 + VdoInptYu12RsltFrmPt.length; //数据包长度 = 数据包类型 + 视频输入帧时间戳 + 视频输入帧宽度 + 视频输入帧高度 + 视频输入Yu12格式结果帧。
            }
            else //如果有视频输入已编码格式结果帧。
            {
                if( VdoInptEncdRsltFrmLenByt != 0 ) //如果本次视频输入帧为有图像活动。
                {
                    System.arraycopy( VdoInptEncdRsltFrmPt, 0, m_TmpBytePt, 1 + 4, ( int ) VdoInptEncdRsltFrmLenByt ); //设置视频输入帧。
                    p_FrmPktLen = 1 + 4 + ( int ) VdoInptEncdRsltFrmLenByt; //数据包长度 = 数据包类型 + 视频输入帧时间戳 + 视频输入已编码格式结果帧。
                }
                else
                {
                    p_FrmPktLen = 1 + 4; //数据包长度 = 数据包类型 + 视频输入帧时间戳。
                }
            }

            if( p_FrmPktLen != 1 + 4 ) //如果本次视频输入帧为有图像活动，就发送。
            {
                m_LastSendVdoInptFrmTimeStamp += 1; //视频输入帧的时间戳递增一个步进。

                //设置数据包类型为视频输入帧包。
                m_TmpBytePt[ 0 ] = ( byte ) PktTyp.VdoFrm.ordinal();
                //设置视频输入帧时间戳。
                m_TmpBytePt[ 1 ] = ( byte ) ( m_LastSendVdoInptFrmTimeStamp & 0xFF );
                m_TmpBytePt[ 2 ] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
                m_TmpBytePt[ 3 ] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
                m_TmpBytePt[ 4 ] = ( byte ) ( ( m_LastSendVdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

                if( ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 0 ) && ( m_NtwkPt.m_TcpClntSoktPt.SendApkt( m_TmpBytePt, p_FrmPktLen, ( short ) 0, 1, 0, m_ErrInfoVstrPt ) == 0 ) ) ||
                      ( ( m_NtwkPt.m_UseWhatXfrPrtcl == 1 ) && ( m_NtwkPt.m_AudpSoktPt.SendApkt( m_NtwkPt.m_AudpCnctIdxPt.m_Val, m_TmpBytePt, p_FrmPktLen, 1, 1, m_ErrInfoVstrPt ) == 0 ) ) )
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "发送一个有图像活动的视频输入帧包成功。视频输入帧时间戳：" + m_LastSendVdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "，类型：" + ( m_TmpBytePt[ 9 ] & 0xff ) + "。" );
                }
                else
                {
                    String p_InfoStrPt = "发送一个有图像活动的视频输入帧包失败。视频输入帧时间戳：" + m_LastSendVdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "，类型：" + ( m_TmpBytePt[ 9 ] & 0xff ) + "。原因：" + m_ErrInfoVstrPt.GetStr();
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    Message p_MessagePt = new Message();p_MessagePt.what = MainActivityHandler.Msg.ShowLog.ordinal();p_MessagePt.obj = p_InfoStrPt;m_MainActivityHandlerPt.sendMessage( p_MessagePt );
                }
            }
            else //如果本次视频输入帧为无图像活动，无需发送。
            {
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本次视频输入帧为无图像活动，无需发送。" );
            }
        }
    }

    //用户定义的写入音频输出帧函数。
    @Override public void UserWriteAdoOtptFrm( int AdoOtptStrmIdx,
                                               short AdoOtptPcmSrcFrmPt[], int AdoOtptPcmFrmLenUnit,
                                               byte AdoOtptEncdSrcFrmPt[], long AdoOtptEncdSrcFrmSzByt, HTLong AdoOtptEncdSrcFrmLenBytPt )
    {
        int p_AdoOtptFrmTimeStamp = 0;
        byte p_AdoOtptFrmPt[] = null;
        long p_AdoOtptFrmLen = 0;
        int p_TmpInt32;

        Out:
        {
            //取出并写入音频输出帧。
            {
                //从容器或自适应抖动缓冲器取出一个音频输出帧。
                switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
                {
                    case 0: //如果要使用容器。
                    {
                        if( m_RecvAdoOtptFrmCntnrPt.size() != 0 ) //如果接收音频输出帧容器不为空。
                        {
                            p_AdoOtptFrmPt = m_RecvAdoOtptFrmCntnrPt.poll(); //从接收音频输出帧容器中取出并删除第一个帧。
                            p_AdoOtptFrmLen = p_AdoOtptFrmPt.length;
                        }

                        if( p_AdoOtptFrmLen != 0 ) //如果音频输出帧为有语音活动。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从接收音频输出帧容器取出一个有语音活动的音频输出帧。数据长度：" + p_AdoOtptFrmLen + "。" );
                        }
                        else //如果音频输出帧为无语音活动。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从接收音频输出帧容器取出一个无语音活动的音频输出帧。数据长度：" + p_AdoOtptFrmLen + "。" );
                        }

                        break;
                    }
                    case 1: //如果要使用自适应抖动缓冲器。
                    {
                        HTInt p_CurHaveBufActFrmCntPt = new HTInt(); //存放当前已缓冲有活动帧的数量。
                        HTInt p_CurHaveBufInactFrmCntPt = new HTInt(); //存放当前已缓冲无活动帧的数量。
                        HTInt p_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
                        HTInt p_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
                        HTInt p_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
                        HTInt p_MaxCntuLostFrmCntPt = new HTInt(); //存放最大连续丢失帧的数量。
                        HTInt p_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。
                        m_AAjbPt.m_Pt.GetBufFrmCnt( p_CurHaveBufActFrmCntPt, p_CurHaveBufInactFrmCntPt, p_CurHaveBufFrmCntPt, p_MinNeedBufFrmCntPt, p_MaxNeedBufFrmCntPt, p_MaxCntuLostFrmCntPt, p_CurNeedBufFrmCntPt, 1, null );
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频自适应抖动缓冲器：有活动帧：" + p_CurHaveBufActFrmCntPt.m_Val + "，无活动帧：" + p_CurHaveBufInactFrmCntPt.m_Val + "，帧：" + p_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrmCntPt.m_Val + "，最大丢帧：" + p_MaxCntuLostFrmCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrmCntPt.m_Val + "。" );

                        //从音频自适应抖动缓冲器取出音频输出帧。
                        m_AAjbPt.m_Pt.GetByteFrm( m_TmpHTInt2Pt, m_TmpByte2Pt, 0, m_TmpByte2Pt.length, m_TmpHTLong2Pt, 1, null );
                        p_AdoOtptFrmTimeStamp = m_TmpHTInt2Pt.m_Val;
                        p_AdoOtptFrmPt = m_TmpByte2Pt;
                        p_AdoOtptFrmLen = m_TmpHTLong2Pt.m_Val;

                        if( p_AdoOtptFrmLen > 0 ) //如果音频输出帧为有语音活动。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个有语音活动的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，长度：" + p_AdoOtptFrmLen + "。" );
                        }
                        else if( p_AdoOtptFrmLen == 0 ) //如果音频输出帧为无语音活动。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个无语音活动的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，长度：" + p_AdoOtptFrmLen + "。" );
                        }
                        else //如果音频输出帧为丢失。
                        {
                            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个丢失的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，长度：" + p_AdoOtptFrmLen + "。" );
                        }
                        break;
                    }
                }

                //写入音频输出帧。
                if( p_AdoOtptFrmLen > 0 ) //如果音频输出帧为有语音活动。
                {
                    if( AdoOtptPcmSrcFrmPt != null ) //如果要使用音频输出Pcm格式原始帧。
                    {
                        if( p_AdoOtptFrmLen != AdoOtptPcmFrmLenUnit * 2 )
                        {
                            Arrays.fill( AdoOtptPcmSrcFrmPt, ( short ) 0 );
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出帧的数据长度不等于Pcm格式的数据长度。音频输出帧：" + ( p_AdoOtptFrmLen ) + "，Pcm格式：" + ( AdoOtptPcmSrcFrmPt.length * 2 ) + "。" );
                            break Out;
                        }

                        //写入音频输出Pcm格式原始帧。
                        for( p_TmpInt32 = 0; p_TmpInt32 < AdoOtptPcmSrcFrmPt.length; p_TmpInt32++ )
                        {
                            AdoOtptPcmSrcFrmPt[ p_TmpInt32 ] = ( short ) ( ( p_AdoOtptFrmPt[ p_TmpInt32 * 2 ] & 0xFF ) | ( p_AdoOtptFrmPt[ p_TmpInt32 * 2 + 1 ] << 8 ) );
                        }
                    }
                    else //如果要使用音频输出已编码格式原始帧。
                    {
                        if( p_AdoOtptFrmLen > AdoOtptEncdSrcFrmPt.length )
                        {
                            AdoOtptEncdSrcFrmLenBytPt.m_Val = 0;
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出帧的数据长度已超过已编码格式的数据长度。音频输出帧：" + ( p_AdoOtptFrmLen ) + "，已编码格式：" + AdoOtptEncdSrcFrmPt.length + "。" );
                            break Out;
                        }

                        //写入音频输出已编码格式原始帧。
                        System.arraycopy( p_AdoOtptFrmPt, 0, AdoOtptEncdSrcFrmPt, 0, ( int ) ( p_AdoOtptFrmLen ) );
                        AdoOtptEncdSrcFrmLenBytPt.m_Val = p_AdoOtptFrmLen;
                    }
                }
                else if( p_AdoOtptFrmLen == 0 ) //如果音频输出帧为无语音活动。
                {
                    if( AdoOtptPcmSrcFrmPt != null ) //如果要使用音频输出Pcm格式原始帧。
                    {
                        Arrays.fill( AdoOtptPcmSrcFrmPt, ( short ) 0 );
                    }
                    else //如果要使用音频输出已编码格式原始帧。
                    {
                        AdoOtptEncdSrcFrmLenBytPt.m_Val = 0;
                    }
                }
                else //如果音频输出帧为丢失。
                {
                    if( AdoOtptPcmSrcFrmPt != null ) //如果要使用音频输出Pcm格式原始帧。
                    {
                        Arrays.fill( AdoOtptPcmSrcFrmPt, ( short ) 0 );
                    }
                    else //如果要使用音频输出已编码格式原始帧。
                    {
                        AdoOtptEncdSrcFrmLenBytPt.m_Val = p_AdoOtptFrmLen;
                    }
                }
            }
        }
    }

    //用户定义的获取音频输出帧函数。
    @Override public void UserGetAdoOtptFrm( int AdoOtptStrmIdx,
                                             short AdoOtptPcmSrcFrmPt[], long AdoOtptPcmFrmLenUnit,
                                             byte AdoOtptEncdSrcFrmPt[], long AdoOtptEncdSrcFrmLenByt )
    {

    }

    //用户定义的写入视频输出帧函数。
    @Override public void UserWriteVdoOtptFrm( int VdoOtptStrmIdx,
                                               byte VdoOtptYu12SrcFrmPt[], HTInt VdoOtptYu12SrcFrmWidthPt, HTInt VdoOtptYu12SrcFrmHeightPt,
                                               byte VdoOtptEncdSrcFrmPt[], long VdoOtptEncdSrcFrmSzByt, HTLong VdoOtptEncdSrcFrmLenBytPt )
    {
        byte p_VdoOtptFrmPt[] = null;
        long p_VdoOtptFrmLen = 0;

        //从容器或自适应抖动缓冲器取出一个视频输出帧。
        switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
        {
            case 0: //如果要使用容器。
            {
                if( m_RecvVdoOtptFrmCntnrPt.size() != 0 ) //如果接收视频输出帧容器不为空。
                {
                    p_VdoOtptFrmPt = m_RecvVdoOtptFrmCntnrPt.poll(); //从接收视频输出帧容器中取出并删除第一个帧。
                    p_VdoOtptFrmLen = p_VdoOtptFrmPt.length;
                }

                if( p_VdoOtptFrmLen != 0 ) //如果视频输出帧为有图像活动。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从接收视频输出帧容器取出一个有图像活动的视频输出帧。数据长度：" + p_VdoOtptFrmLen + "。" );
                }
                else //如果视频输出帧为无图像活动。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从接收视频输出帧容器取出一个无图像活动的视频输出帧。数据长度：" + p_VdoOtptFrmLen + "。" );
                }
                break;
            }
            case 1: //如果要使用自适应抖动缓冲器。
            {
                HTInt p_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
                HTInt p_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
                HTInt p_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
                HTInt p_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。
                m_VAjbPt.m_Pt.GetBufFrmCnt( p_CurHaveBufFrmCntPt, p_MinNeedBufFrmCntPt, p_MaxNeedBufFrmCntPt, p_CurNeedBufFrmCntPt, 1, null );

                if( p_CurHaveBufFrmCntPt.m_Val != 0 ) //如果视频自适应抖动缓冲器不为空。
                {
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频自适应抖动缓冲器：帧：" + p_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + p_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + p_MaxNeedBufFrmCntPt.m_Val + "，当前需帧：" + p_CurNeedBufFrmCntPt.m_Val + "。" );

                    int p_VdoOtptFrmTimeStamp;

                    //从视频自适应抖动缓冲器取出视频输出帧。
                    m_VAjbPt.m_Pt.GetByteFrm( SystemClock.uptimeMillis(), m_TmpHTInt3Pt, m_TmpByte3Pt, 0, m_TmpByte3Pt.length, m_TmpHTLong3Pt, 1, null );
                    p_VdoOtptFrmTimeStamp = m_TmpHTInt3Pt.m_Val;
                    p_VdoOtptFrmPt = m_TmpByte3Pt;
                    p_VdoOtptFrmLen = m_TmpHTLong3Pt.m_Val;

                    if( p_VdoOtptFrmLen > 0 ) //如果视频输出帧为有图像活动。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从视频自适应抖动缓冲器取出一个有图像活动的视频输出帧。时间戳：" + p_VdoOtptFrmTimeStamp + "，数据长度：" + p_VdoOtptFrmLen + "。" );
                    }
                    else //如果视频输出帧为无图像活动。
                    {
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从视频自适应抖动缓冲器取出一个无图像活动的视频输出帧。时间戳：" + p_VdoOtptFrmTimeStamp + "，数据长度：" + p_VdoOtptFrmLen + "。" );
                    }
                }

                break;
            }
        }

        //写入视频输出帧。
        if( p_VdoOtptFrmLen > 0 ) //如果视频输出帧为有图像活动。
        {
            if( VdoOtptYu12SrcFrmPt != null ) //如果要使用视频输出Yu12格式原始帧。
            {
                //读取视频输出帧宽度。
                VdoOtptYu12SrcFrmWidthPt.m_Val = ( p_VdoOtptFrmPt[ 0 ] & 0xFF ) + ( ( p_VdoOtptFrmPt[ 1 ] & 0xFF ) << 8 ) + ( ( p_VdoOtptFrmPt[ 2 ] & 0xFF ) << 16 ) + ( ( p_VdoOtptFrmPt[ 3 ] & 0xFF ) << 24 );
                //读取视频输出帧高度。
                VdoOtptYu12SrcFrmHeightPt.m_Val = ( p_VdoOtptFrmPt[ 4 ] & 0xFF ) + ( ( p_VdoOtptFrmPt[ 5 ] & 0xFF ) << 8 ) + ( ( p_VdoOtptFrmPt[ 6 ] & 0xFF ) << 16 ) + ( ( p_VdoOtptFrmPt[ 7 ] & 0xFF ) << 24 );

                if( p_VdoOtptFrmLen - 4 - 4 != ( ( long ) VdoOtptYu12SrcFrmWidthPt.m_Val * VdoOtptYu12SrcFrmHeightPt.m_Val * 3 / 2 ) )
                {
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度不等于Yu12格式的数据长度。视频输出帧：" + ( p_VdoOtptFrmLen - 4 - 4 ) + "，Yu12格式：" + ( VdoOtptYu12SrcFrmWidthPt.m_Val * VdoOtptYu12SrcFrmHeightPt.m_Val * 3 / 2 ) + "。" );
                    VdoOtptYu12SrcFrmWidthPt.m_Val = 0;
                    VdoOtptYu12SrcFrmHeightPt.m_Val = 0;
                    return;
                }

                //写入视频输出Yu12格式原始帧。
                System.arraycopy( p_VdoOtptFrmPt, 4 + 4, VdoOtptYu12SrcFrmPt, 0, ( int )( p_VdoOtptFrmLen - 4 - 4 ) );
            }
            else //如果要使用视频输出已编码格式原始帧。
            {
                if( p_VdoOtptFrmLen > VdoOtptEncdSrcFrmSzByt )
                {
                    VdoOtptEncdSrcFrmLenBytPt.m_Val = 0;
                    if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度已超过已编码格式的数据长度。视频输出帧：" + p_VdoOtptFrmLen + "，已编码格式：" + VdoOtptEncdSrcFrmSzByt + "。" );
                    return;
                }

                //写入视频输出已编码格式原始帧。
                System.arraycopy( p_VdoOtptFrmPt, 0, VdoOtptEncdSrcFrmPt, 0, ( int )( p_VdoOtptFrmLen ) );
                VdoOtptEncdSrcFrmLenBytPt.m_Val = p_VdoOtptFrmLen;
            }
        }
        else if( p_VdoOtptFrmLen == 0 ) //如果视频输出帧为无图像活动。
        {
            if( VdoOtptYu12SrcFrmPt != null ) //如果要使用视频输出Yu12格式原始帧。
            {

            }
            else //如果要使用视频输出已编码格式原始帧。
            {

            }
        }
    }

    //用户定义的获取视频输出帧函数。
    @Override public void UserGetVdoOtptFrm( int VdoOtptStrmIdx,
                                             byte VdoOtptYu12SrcFrmPt[], int VdoOtptYu12SrcFrmWidth, int VdoOtptYu12SrcFrmHeight,
                                             byte VdoOtptEncdSrcFrmPt[], long VdoOtptEncdSrcFrmLenByt )
    {

    }
}