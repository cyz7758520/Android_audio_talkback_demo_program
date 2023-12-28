package HeavenTao.Media;

import android.os.SystemClock;
import android.util.Log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

public class TkbkNtwk //对讲网络。
{
    public static String m_CurClsNameStrPt = "TkbkNtwk"; //存放当前类名称字符串。

    public NtwkMediaPocsThrd m_NtwkMediaPocsThrdPt; //存放网络媒体处理线程的指针。

    public int m_SrvrIsInit; //存放服务端是否初始化，为0表示未初始化，为1表示已初始化。
    public TcpSrvrSokt m_TcpSrvrSoktPt; //存放本端Tcp协议服务端套接字的指针。
    public AudpSokt m_AudpSrvrSoktPt; //存放本端高级Udp协议服务端套接字的指针。
    public int m_IsAutoActCnct; //存放是否自动激活连接，为0表示手动，为1表示自动。

    public int m_XfrMode; //存放传输模式，为0表示实时半双工（一键通），为1表示实时全双工。
    public int m_PttBtnIsDown; //存放一键即按即通按钮是否按下，为0表示弹起，为非0表示按下。

    class CnctInfo //存放连接信息。
    {
        int m_Idx; //存放索引，从0开始。

        int m_IsSrvrOrClntCnct; //存放是否是服务端或客户端的连接，为0表示服务端，为1表示客户端。
        int m_IsTcpOrAudpPrtcl; //存放是否是Tcp或Udp协议，为0表示Tcp协议，为1表示高级Udp协议。
        String m_RmtNodeNamePt; //存放远端套接字绑定的远端节点名称字符串的指针，
        String m_RmtNodeSrvcPt; //存放远端套接字绑定的远端节点服务字符串的指针，
        TcpClntSokt m_TcpClntSoktPt; //存放本端Tcp协议客户端套接字的指针。
        long m_AudpClntCnctIdx; //存放本端高级Udp协议客户端连接索引。
        int m_IsRqstDstoy; //存放是否请求销毁，为0表示未请求，为1表示已请求。

        int m_CurCnctSts; //存放当前连接状态，为[-m_MaxCnctTimes,0]表示等待远端接受连接。
        int m_LclTkbkMode; //存放本端对讲模式。
        int m_RmtTkbkMode; //存放远端对讲模式。

        int m_LastSendAdoInptFrmIsAct; //存放最后一个发送的音频输入帧有无语音活动，为1表示有语音活动，为0表示无语音活动。
        int m_LastSendAdoInptFrmTimeStamp; //存放最后一个发送音频输入帧的时间戳。
        int m_LastSendVdoInptFrmTimeStamp; //存放最后一个发送视频输入帧的时间戳。
        int m_IsRecvExitPkt; //存放是否接收到退出包，为0表示否，为1表示是。
    }
    public ArrayList< CnctInfo > m_CnctInfoLstPt = new ArrayList<>(); //存放连接信息列表的指针。
    public CnctInfo m_CurActCnctInfoPt; //存放当前激活的连接信息的指针。
    public int m_MaxCnctNum; //存放最大连接数。

    public int m_UseWhatRecvOtptFrm; //存放使用什么接收输出帧，为0表示容器，为1表示自适应抖动缓冲器。
    public int m_RecvOtptFrmIsInit; //存放接收输出帧是否初始化，为0表示未初始化，为1表示已初始化。

    ConcurrentLinkedQueue< byte[] > m_RecvAdoOtptFrmCntnrPt; //存放接收音频输出帧容器的指针。
    ConcurrentLinkedQueue< byte[] > m_RecvVdoOtptFrmCntnrPt; //存放接收视频输出帧容器的指针。

    public class AAjb //存放音频自适应抖动缓冲器。
    {
        public HeavenTao.Ado.AAjb m_Pt; //存放指针。
        public int m_MinNeedBufFrmCnt; //存放最小需缓冲帧的数量，单位为个帧，取值区间为[1,2147483647]。
        public int m_MaxNeedBufFrmCnt; //最大需缓冲帧的数量，单位为个帧，取值区间为[1,2147483647]，必须大于等于最小需缓冲帧的数量。
        public int m_MaxCntuLostFrmCnt; //最大连续丢失帧的数量，单位为个帧，取值区间为[1,2147483647]，当连续丢失帧的数量超过最大时，认为是对方中途暂停发送。
        public float m_AdaptSensitivity; //存放自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。
    }
    public AAjb m_AAjbPt = new AAjb();
    public class VAjb //存放视频自适应抖动缓冲器。
    {
        public HeavenTao.Vdo.VAjb m_Pt; //存放指针。
        public int m_MinNeedBufFrmCnt; //存放最小需缓冲帧数量，单位为个帧，必须大于0。
        public int m_MaxNeedBufFrmCnt; //存放最大需缓冲帧数量，单位为个帧，必须大于最小需缓冲数据帧的数量。
        public float m_AdaptSensitivity; //存放自适应灵敏度，灵敏度越大自适应计算当前需缓冲帧的数量越多，取值区间为[0.0,127.0]。
    }
    public VAjb m_VAjbPt = new VAjb();

    //连接信息初始化。
    public CnctInfo CnctInfoInit( int IsSrvrOrClntCnct, int IsTcpOrAudpPrtcl, String RmtNodeNamePt, String RmtNodeSrvcPt, TcpClntSokt TcpClntSoktPt, long AudpClntCnctIdx, int CurCnctSts, String CnctLstLclTkbkModeStrPt, String CnctLstRmtTkbkModeStrPt )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
        CnctInfo p_CnctInfoTmpPt = null;

        Out:
        {
            if( m_CnctInfoLstPt.size() < m_MaxCnctNum ) //如果未达到最大连接数。
            {
                p_CnctInfoTmpPt = new CnctInfo();

                p_CnctInfoTmpPt.m_Idx = m_CnctInfoLstPt.size(); //设置索引。

                p_CnctInfoTmpPt.m_IsSrvrOrClntCnct = IsSrvrOrClntCnct; //设置为服务端的连接。
                p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl = IsTcpOrAudpPrtcl; //设置协议为Tcp协议或高级Udp协议。
                p_CnctInfoTmpPt.m_RmtNodeNamePt = RmtNodeNamePt; //设置远端套接字绑定的远端节点名称字符串的指针。
                p_CnctInfoTmpPt.m_RmtNodeSrvcPt = RmtNodeSrvcPt; //设置远端套接字绑定的远端节点服务字符串的指针。
                p_CnctInfoTmpPt.m_TcpClntSoktPt = TcpClntSoktPt; //设置本端Tcp协议客户端套接字的指针。
                p_CnctInfoTmpPt.m_AudpClntCnctIdx = AudpClntCnctIdx; //设置本端高级Udp协议客户端连接索引。

                p_CnctInfoTmpPt.m_CurCnctSts = CurCnctSts; //设置当前连接状态。
                p_CnctInfoTmpPt.m_LclTkbkMode = NtwkMediaPocsThrd.TkbkMode.None; //设置本端对讲模式。
                p_CnctInfoTmpPt.m_RmtTkbkMode = NtwkMediaPocsThrd.TkbkMode.None; //设置远端对讲模式。

                p_CnctInfoTmpPt.m_LastSendAdoInptFrmIsAct = 0; //设置最后发送的一个音频输入帧为无语音活动。
                p_CnctInfoTmpPt.m_LastSendAdoInptFrmTimeStamp = 0 - 1; //设置最后一个发送音频输入帧的时间戳为0的前一个，因为第一次发送音频输入帧时会递增一个步进。
                p_CnctInfoTmpPt.m_LastSendVdoInptFrmTimeStamp = 0 - 1; //设置最后一个发送视频输入帧的时间戳为0的前一个，因为第一次发送视频输入帧时会递增一个步进。

                m_NtwkMediaPocsThrdPt.UserCnctInit( p_CnctInfoTmpPt.m_Idx, ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) ? "Tcp" : "Audp", p_CnctInfoTmpPt.m_RmtNodeNamePt, p_CnctInfoTmpPt.m_RmtNodeSrvcPt, CnctLstLclTkbkModeStrPt, CnctLstRmtTkbkModeStrPt ); //调用用户定义的连接添加函数。
                m_CnctInfoLstPt.add( p_CnctInfoTmpPt ); //添加到连接信息列表。
            }
            else //如果已达到最大连接数。
            {
                String p_InfoStrPt = "网络媒体处理线程：对讲网络：已达到最大连接数，无法添加连接信息。";
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                if( m_NtwkMediaPocsThrdPt.m_IsShowToast != 0 ) m_NtwkMediaPocsThrdPt.UserShowToast( p_InfoStrPt );
                break Out;
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_CnctInfoTmpPt;
    }

    //连接信息更新本端对讲模式。
    public void CnctInfoUpdtLclTkbkMode( CnctInfo CnctInfoPt, int LclTkbkMode )
    {
        CnctInfoPt.m_LclTkbkMode = LclTkbkMode;
        if( m_CurActCnctInfoPt == CnctInfoPt )
        {
            m_NtwkMediaPocsThrdPt.UserCnctModify( CnctInfoPt.m_Idx, null, NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ CnctInfoPt.m_LclTkbkMode ], null );
        }
        else
        {
            if( CnctInfoPt.m_LclTkbkMode != NtwkMediaPocsThrd.TkbkMode.None )
            {
                m_NtwkMediaPocsThrdPt.UserCnctModify( CnctInfoPt.m_Idx, null, "请求激活\n" + NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ CnctInfoPt.m_LclTkbkMode ], null );
            }
            else
            {
                m_NtwkMediaPocsThrdPt.UserCnctModify( CnctInfoPt.m_Idx, null, NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ CnctInfoPt.m_LclTkbkMode ], null );
            }
        }
        CnctSendTkbkModePkt( CnctInfoPt, CnctInfoPt.m_LclTkbkMode ); //发送对讲模式包。
    }

    //连接信息全部更新本端对讲模式。
    public void CnctInfoAllUpdtLclTkbkMode( int LclTkbkMode )
    {
        for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoLstPt.size(); p_CnctInfoLstIdx++ )
        {
            CnctInfo p_CnctInfoTmpPt = m_CnctInfoLstPt.get( p_CnctInfoLstIdx );

            CnctInfoUpdtLclTkbkMode( p_CnctInfoTmpPt, LclTkbkMode );
        }
    }

    //连接信息更新远端对讲模式。
    public void CnctInfoUpdtRmtTkbkMode( CnctInfo CnctInfoPt )
    {
        if( m_CurActCnctInfoPt == CnctInfoPt )
        {
            m_NtwkMediaPocsThrdPt.UserCnctModify( CnctInfoPt.m_Idx, null, null, NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ CnctInfoPt.m_RmtTkbkMode ] );
        }
        else
        {
            if( CnctInfoPt.m_RmtTkbkMode != NtwkMediaPocsThrd.TkbkMode.None )
            {
                m_NtwkMediaPocsThrdPt.UserCnctModify( CnctInfoPt.m_Idx, null, null, "请求激活\n" + NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ CnctInfoPt.m_RmtTkbkMode ] );
            }
            else
            {
                m_NtwkMediaPocsThrdPt.UserCnctModify( CnctInfoPt.m_Idx, null, null, NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ CnctInfoPt.m_RmtTkbkMode ] );
            }
        }
    }

    //连接信息全部更新远端对讲模式。
    public void CnctInfoAllUpdtRmtTkbkMode()
    {
        for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoLstPt.size(); p_CnctInfoLstIdx++ )
        {
            CnctInfo p_CnctInfoTmpPt = m_CnctInfoLstPt.get( p_CnctInfoLstIdx );

            CnctInfoUpdtRmtTkbkMode( p_CnctInfoTmpPt );
        }
    }

    //连接信息激活。
    public void CnctInfoAct( CnctInfo CnctInfoPt )
    {
        if( m_CurActCnctInfoPt != CnctInfoPt ) //如果当前激活的连接信息与要激活的连接信息不一致。
        {
            if( m_CurActCnctInfoPt != null ) //如果当前激活的连接信息不为空。
            {
                CnctInfo p_TmpCnctInfoPt = m_CurActCnctInfoPt;

                m_CurActCnctInfoPt = null; //设置当前激活的连接信息。
                if( p_TmpCnctInfoPt.m_Idx != -1 )
                {
                    m_NtwkMediaPocsThrdPt.UserCnctModify( p_TmpCnctInfoPt.m_Idx, "", null, null );
                    CnctInfoUpdtRmtTkbkMode( p_TmpCnctInfoPt );
                }
                if( p_TmpCnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) CnctSendTkbkModePkt( p_TmpCnctInfoPt, NtwkMediaPocsThrd.TkbkMode.None ); //发送对讲模式包。
            }

            if( CnctInfoPt != null ) //如果要激活的连接信息不为空。
            {
                m_CurActCnctInfoPt = CnctInfoPt;
                m_CurActCnctInfoPt.m_LclTkbkMode = m_NtwkMediaPocsThrdPt.m_LclTkbkMode;
                m_NtwkMediaPocsThrdPt.UserCnctModify( m_CurActCnctInfoPt.m_Idx, "⇶", NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ m_CurActCnctInfoPt.m_LclTkbkMode ], null );
                if( m_CurActCnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) CnctSendTkbkModePkt( m_CurActCnctInfoPt, m_NtwkMediaPocsThrdPt.m_LclTkbkMode ); //如果传输模式为实时全双工，就发送对讲模式包。实时半双工（一键通）在一键即按即通按钮按下和弹起时才发送。
                CnctInfoUpdtRmtTkbkMode( m_CurActCnctInfoPt );
                m_NtwkMediaPocsThrdPt.SetTkbkMode( 1 ); //设置对讲模式。

                RecvOtptFrmReset(); //接收输出帧重置。

                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + CnctInfoPt.hashCode() + "：连接激活。";
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );

                //挂起全部非激活连接信息。
                for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoLstPt.size(); p_CnctInfoLstIdx++ )
                {
                    CnctInfo p_CnctInfoTmpPt = m_CnctInfoLstPt.get( p_CnctInfoLstIdx );

                    if( p_CnctInfoTmpPt != m_CurActCnctInfoPt )
                    {
                        CnctInfoPend( p_CnctInfoTmpPt );
                    }
                }
            }
            else //如果要激活的连接信息为空。
            {
                m_NtwkMediaPocsThrdPt.SetTkbkMode( 1 ); //设置对讲模式。

                String p_InfoStrPt = "网络媒体处理线程：对讲网络：无连接激活。";
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
            }
        }
    }

    //连接信息挂起。
    public void CnctInfoPend( CnctInfo CnctInfoPt )
    {
        if( CnctInfoPt.m_LclTkbkMode != NtwkMediaPocsThrd.TkbkMode.None ) //如果连接信息当前不为挂起。
        {
            CnctInfoPt.m_LclTkbkMode = NtwkMediaPocsThrd.TkbkMode.None;
            m_NtwkMediaPocsThrdPt.UserCnctModify( CnctInfoPt.m_Idx, "", NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ CnctInfoPt.m_LclTkbkMode ], null );
            CnctSendTkbkModePkt( CnctInfoPt, CnctInfoPt.m_LclTkbkMode ); //发送对讲模式包。
        }
    }

    //连接信息销毁。
    public void CnctInfoDstoy( CnctInfo CnctInfoPt )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            if( CnctInfoPt != null )
            {
                //发送退出包。
                if( ( CnctInfoPt.m_IsRecvExitPkt == 0 ) && ( CnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) ) //如果未接收到退出包，且当前连接状态为已连接。
                {
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = NtwkMediaPocsThrd.PktTyp.Exit; //设置退出包。
                    if( ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 0 ) && ( CnctInfoPt.m_TcpClntSoktPt.SendApkt( m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1, ( short ) 0, 1, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
                        ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 1 ) && ( CnctInfoPt.m_IsSrvrOrClntCnct == 0 ) && ( m_AudpSrvrSoktPt.SendApkt( CnctInfoPt.m_AudpClntCnctIdx, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1, 1, 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
                        ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 1 ) && ( CnctInfoPt.m_IsSrvrOrClntCnct == 1 ) && ( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( CnctInfoPt.m_AudpClntCnctIdx, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1, 1, 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) )
                    {
                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + CnctInfoPt.hashCode() + "：发送一个退出包成功。";
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    }
                    else
                    {
                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + CnctInfoPt.hashCode() + "：发送一个退出包失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    }
                }

                //销毁本端Tcp协议客户端套接字。
                if( CnctInfoPt.m_TcpClntSoktPt != null )
                {
                    CnctInfoPt.m_TcpClntSoktPt.Dstoy( ( short ) -1, null );
                    CnctInfoPt.m_TcpClntSoktPt = null;
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + CnctInfoPt.hashCode() + "：销毁本端Tcp协议客户端套接字成功。" );
                }

                //销毁本端高级Udp协议客户端连接。
                if( CnctInfoPt.m_AudpClntCnctIdx != -1 )
                {
                    if( CnctInfoPt.m_IsSrvrOrClntCnct == 0 ) m_AudpSrvrSoktPt.ClosCnct( CnctInfoPt.m_AudpClntCnctIdx, null );
                    else m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.ClosCnct( CnctInfoPt.m_AudpClntCnctIdx, null );
                    CnctInfoPt.m_AudpClntCnctIdx = -1;
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + CnctInfoPt.hashCode() + "：销毁本端高级Udp协议客户端连接成功。" );
                }

                if( ( CnctInfoPt.m_IsSrvrOrClntCnct == 1 ) && ( CnctInfoPt.m_IsRecvExitPkt == 0 ) && ( CnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Tmot ) ) //如果为客户端的连接，且未接收到退出包，且当前连接状态为异常断开，就重连。
                {
                    CnctInfoPt.m_IsRqstDstoy = 0; //设置未请求销毁。

                    CnctInfoPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Wait; //设置当前连接状态。
                    CnctInfoPt.m_RmtTkbkMode = NtwkMediaPocsThrd.TkbkMode.None; //设置远端对讲模式。

                    CnctInfoPt.m_LastSendAdoInptFrmIsAct = 0; //设置最后发送的一个音频输入帧为无语音活动。
                    CnctInfoPt.m_LastSendAdoInptFrmTimeStamp = 0 - 1; //设置最后一个发送音频输入帧的时间戳为0的前一个，因为第一次发送音频输入帧时会递增一个步进。
                    CnctInfoPt.m_LastSendVdoInptFrmTimeStamp = 0 - 1; //设置最后一个发送视频输入帧的时间戳为0的前一个，因为第一次发送视频输入帧时会递增一个步进。
                    CnctInfoPt.m_IsRecvExitPkt = 0; //设置未接收到退出包。

                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + CnctInfoPt.hashCode() + "：连接异常断开，准备重连。";
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                }
                else //如果不为客户端的连接，或已接收到退出包，或当前连接状态不为异常断开，就销毁。
                {
                    int p_IsCurActCnctInfo = ( CnctInfoPt == m_CurActCnctInfoPt ) ? 1 : 0; //存放是否是当前激活的连接信息。

                    //设置当前连接状态。
                    if( CnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Wait ) CnctInfoPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Tmot;
                    else if( CnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) CnctInfoPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Dsct;

                    if( p_IsCurActCnctInfo != 0 ) CnctInfoAct( null ); //如果是当前激活的连接信息，就激活空连接信息。

                    m_NtwkMediaPocsThrdPt.UserCnctDstoy( CnctInfoPt.m_Idx, CnctInfoPt.m_CurCnctSts ); //调用用户定义的连接销毁函数。

                    //从连接信息列表删除。
                    for( int p_CnctInfoLstIdx = CnctInfoPt.m_Idx + 1; p_CnctInfoLstIdx < m_CnctInfoLstPt.size(); p_CnctInfoLstIdx++ )
                    {
                        CnctInfo p_CnctInfoTmpPt = m_CnctInfoLstPt.get( p_CnctInfoLstIdx );

                        p_CnctInfoTmpPt.m_Idx--; //设置后面的连接信息的索引全部递减1。
                    }
                    m_CnctInfoLstPt.remove( CnctInfoPt.m_Idx );

                    {
                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + CnctInfoPt.hashCode() + "：已销毁。";
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    }

                    //连接信息激活。
                    if( ( p_IsCurActCnctInfo != 0 ) && //如果是当前激活的连接信息。
                        ( ( m_IsAutoActCnct == 1 ) && ( !m_CnctInfoLstPt.isEmpty() ) ) ) //如果是自动激活连接，且还有其他连接信息。
                    {
                        ActRqirActCnctInfoOut:
                        {
                            //激活请求激活的连接信息。
                            for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoLstPt.size(); p_CnctInfoLstIdx++ )
                            {
                                CnctInfo p_CnctInfoTmpPt = m_CnctInfoLstPt.get( p_CnctInfoLstIdx );

                                if( ( p_CnctInfoTmpPt.m_RmtTkbkMode != NtwkMediaPocsThrd.TkbkMode.None ) )
                                {
                                    CnctInfoAct( p_CnctInfoTmpPt );
                                    break ActRqirActCnctInfoOut;
                                }
                            }

                            CnctInfoAllUpdtLclTkbkMode( m_NtwkMediaPocsThrdPt.m_LclTkbkMode ); //如果没有请求激活的连接信息，就连接信息全部更新本端对讲模式。
                        }
                    }

                    m_NtwkMediaPocsThrdPt.IsAutoRqirExit(); //判断是否自动请求退出。
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
    }

    //连接信息全部销毁。
    public void CnctInfoAllDstoy()
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            while( !m_CnctInfoLstPt.isEmpty() ) CnctInfoDstoy( m_CnctInfoLstPt.get( 0 ) );

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
    }

    //服务端初始化。
    public int SrvrInit( String SrvrUrlStrPt )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
        Vstr p_SrvrUrlVstrPt = null;

        int p_RmtNodeAddrFamly; //存放远端节点的地址族，为4表示IPv4，为6表示IPv6，为0表示自动选择。

        Out:
        {
            m_NtwkMediaPocsThrdPt.UserSrvrInit(); //调用用户定义的服务端初始化函数。

            if( m_SrvrIsInit == 0 ) //如果服务端未初始化。
            {
                HTString p_SrvrPrtclStrPt = new HTString();
                HTString p_SrvrNodeNameStrPt = new HTString();
                HTString p_SrvrNodeSrvcStrPt = new HTString();

                m_SrvrIsInit = 1; //设置服务端已初始化。

                p_SrvrUrlVstrPt = new Vstr();
                if( p_SrvrUrlVstrPt.Init( SrvrUrlStrPt ) != 0 )
                {
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：初始化服务端Url动态字符串失败。" );
                    break Out;
                }

                //解析服务端Url字符串。
                if( p_SrvrUrlVstrPt.UrlParse( p_SrvrPrtclStrPt, null, null, p_SrvrNodeNameStrPt, p_SrvrNodeSrvcStrPt, null, null, null, null, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                {
                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：解析服务端Url字符串失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    break Out;
                }
                if( ( p_SrvrPrtclStrPt.m_Val.equals( "Tcp" ) == false ) && ( p_SrvrPrtclStrPt.m_Val.equals( "Audp" ) == false ) )
                {
                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：服务端Url字符串的协议不正确。";
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    break Out;
                }
                if( p_SrvrNodeSrvcStrPt.m_Val.equals( "" ) )
                {
                    p_SrvrNodeSrvcStrPt.m_Val = "12345";
                }

                { //设置远端节点的地址族。
                    try
                    {
                        InetAddress inetAddress = InetAddress.getByName( p_SrvrNodeNameStrPt.m_Val );
                        if( inetAddress.getAddress().length == 4 ) p_RmtNodeAddrFamly = 4;
                        else p_RmtNodeAddrFamly = 6;
                    }
                    catch( UnknownHostException e )
                    {
                        p_RmtNodeAddrFamly = 0;
                    }
                }

                if( p_SrvrPrtclStrPt.m_Val.equals( "Tcp" ) ) //如果要使用Tcp协议。
                {
                    m_TcpSrvrSoktPt = new TcpSrvrSokt();

                    if( m_TcpSrvrSoktPt.Init( p_RmtNodeAddrFamly, p_SrvrNodeNameStrPt.m_Val, p_SrvrNodeSrvcStrPt.m_Val, 1, 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //如果初始化本端Tcp协议服务端套接字成功。
                    {
                        if( m_TcpSrvrSoktPt.GetLclAddr( null, m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt, m_NtwkMediaPocsThrdPt.m_LclNodePortPt, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果获取本端Tcp协议服务端套接字绑定的本地节点地址和端口失败。
                        {
                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：获取本端Tcp协议服务端套接字绑定的本地节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break Out;
                        }

                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：初始化本端Tcp协议服务端套接字[" + m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_LclNodePortPt.m_Val + "]成功。";
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    }
                    else //如果初始化本端Tcp协议服务端套接字失败。
                    {
                        m_TcpSrvrSoktPt = null;

                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：初始化本端Tcp协议服务端套接字[" + p_SrvrNodeNameStrPt.m_Val + ":" + p_SrvrNodeSrvcStrPt.m_Val + "]失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                        break Out;
                    }
                }
                else //如果要使用高级Udp协议。
                {
                    m_AudpSrvrSoktPt = new AudpSokt();

                    if( m_AudpSrvrSoktPt.Init( p_RmtNodeAddrFamly, p_SrvrNodeNameStrPt.m_Val, p_SrvrNodeSrvcStrPt.m_Val, ( short )1, ( short )5000, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //如果初始化本端高级Udp协议服务端套接字成功。
                    {
                        if( m_AudpSrvrSoktPt.SetSendBufSz( 1024 * 1024, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                        {
                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：设置本端高级Udp协议服务端套接字的发送缓冲区大小失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break Out;
                        }

                        if( m_AudpSrvrSoktPt.SetRecvBufSz( 1024 * 1024, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                        {
                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：设置本端高级Udp协议服务端套接字的接收缓冲区大小失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break Out;
                        }

                        if( m_AudpSrvrSoktPt.GetLclAddr( null, m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt, m_NtwkMediaPocsThrdPt.m_LclNodePortPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果获取本端高级Udp协议套接字绑定的本地节点地址和端口失败。
                        {
                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：获取本端高级Udp协议服务端套接字绑定的本地节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break Out;
                        }

                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：初始化本端高级Udp协议服务端套接字[" + m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_LclNodePortPt.m_Val + "]成功。";
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    }
                    else //如果初始化本端高级Udp协议服务端套接字失败。
                    {
                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：初始化本端高级Udp协议服务端套接字[" + p_SrvrNodeNameStrPt.m_Val + ":" + p_SrvrNodeSrvcStrPt.m_Val + "]失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                        break Out;
                    }
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_SrvrUrlVstrPt != null ) p_SrvrUrlVstrPt.Dstoy();
        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
            SrvrDstoy();
        }
        return p_Rslt;
    }

    //服务端销毁。
    public void SrvrDstoy()
    {
        Out:
        {
            m_NtwkMediaPocsThrdPt.UserSrvrDstoy(); //调用用户定义的服务端销毁函数。

            if( m_SrvrIsInit != 0 ) //如果服务端已初始化。
            {
                //删除所有的服务端连接。
                for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoLstPt.size(); p_CnctInfoLstIdx++ )
                {
                    CnctInfo p_CnctInfoTmpPt = m_CnctInfoLstPt.get( p_CnctInfoLstIdx );

                    if( p_CnctInfoTmpPt.m_IsSrvrOrClntCnct == 0 )
                    {
                        CnctInfoDstoy( p_CnctInfoTmpPt );
                        p_CnctInfoLstIdx--;
                    }
                }

                //销毁本端Tcp协议服务端套接字。
                if( m_TcpSrvrSoktPt != null )
                {
                    m_TcpSrvrSoktPt.Dstoy( null ); //关闭并销毁本端Tcp协议服务端套接字。
                    m_TcpSrvrSoktPt = null;

                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：关闭并销毁本端Tcp协议服务端套接字成功。";
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                }

                //销毁本端高级Udp协议服务端套接字。
                if( m_AudpSrvrSoktPt != null )
                {
                    m_AudpSrvrSoktPt.Dstoy( null ); //关闭并销毁本端高级Udp协议服务端套接字。
                    m_AudpSrvrSoktPt = null;

                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：关闭并销毁本端高级Udp协议服务端套接字成功。";
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                }

                m_SrvrIsInit = 0; //设置服务端未初始化。
            }

            m_NtwkMediaPocsThrdPt.IsAutoRqirExit(); //判断是否自动请求退出。
        }
    }

    //连接初始化。
    public void CnctInit( int IsTcpOrAudpPrtcl, String RmtNodeNamePt, String RmtNodeSrvcPt )
    {
        Out:
        {
            for( CnctInfo p_CnctInfoTmpPt : m_CnctInfoLstPt )
            {
                if( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == IsTcpOrAudpPrtcl ) &&
                    ( p_CnctInfoTmpPt.m_RmtNodeNamePt.equals( RmtNodeNamePt ) ) &&
                    ( p_CnctInfoTmpPt.m_RmtNodeSrvcPt.equals( RmtNodeSrvcPt ) ) )
                {
                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：已存在与远端节点" + ( ( IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + RmtNodeNamePt + ":" + RmtNodeSrvcPt + "]的连接，无需重复连接。";
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    break Out;
                }
            }

            CnctInfo p_CnctInfoTmpPt;
            if( ( p_CnctInfoTmpPt = CnctInfoInit( 1, IsTcpOrAudpPrtcl, RmtNodeNamePt, RmtNodeSrvcPt, null, -1, NtwkMediaPocsThrd.CnctSts.Wait, "初始化", null ) ) == null ) break Out; //如果连接信息初始化失败。

            //Ping一下远程节点名称，这样可以快速获取ARP条目。
            try
            {
                Runtime.getRuntime().exec( "ping -c 1 -w 1 " + RmtNodeNamePt );
            }
            catch( Exception ignored )
            {
            }

            String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化与远端节点" + ( ( IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + RmtNodeNamePt + ":" + RmtNodeSrvcPt + "]的连接成功。";
            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
        }
    }

    //连接激活。
    public void CnctAct( int CnctIdx )
    {
        Out:
        {
            if( ( CnctIdx >= m_CnctInfoLstPt.size() ) || ( CnctIdx < 0 ) )
            {
                String p_InfoStrPt = "网络媒体处理线程：对讲网络：没有索引为" + CnctIdx + "]的连接，无法激活。";
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                break Out;
            }

            CnctInfo p_CnctInfoTmpPt = m_CnctInfoLstPt.get( CnctIdx );
            CnctInfoAct( p_CnctInfoTmpPt ); //连接信息激活。
        }
    }

    //连接发送数据包。
    public int CnctSendPkt( CnctInfo CnctInfoPt, byte PktPt[], long PktLenByt, int Times, int IsRlab, Vstr ErrInfoVstrPt )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            if( ( CnctInfoPt != null ) && ( CnctInfoPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) ) //如果当前激活的连接信息的指针不为空，且当前连接状态为已连接。
            {
                //发送数据包。
                if( ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 0 ) && ( CnctInfoPt.m_TcpClntSoktPt.SendApkt( PktPt, PktLenByt, ( short ) 0, Times, 0, ErrInfoVstrPt ) == 0 ) ) ||
                    ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 1 ) && ( CnctInfoPt.m_IsSrvrOrClntCnct == 0 ) && ( m_AudpSrvrSoktPt.SendApkt( CnctInfoPt.m_AudpClntCnctIdx, PktPt, PktLenByt, Times, IsRlab, ErrInfoVstrPt ) == 0 ) ) ||
                    ( ( CnctInfoPt.m_IsTcpOrAudpPrtcl == 1 ) && ( CnctInfoPt.m_IsSrvrOrClntCnct == 1 ) && ( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.SendApkt( CnctInfoPt.m_AudpClntCnctIdx, PktPt, PktLenByt, Times, IsRlab, ErrInfoVstrPt ) == 0 ) ) )
                {
                    //发送数据包成功。
                }
                else
                {
                    break Out; //发送数据包失败。
                }
            }
            else
            {
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) if( ErrInfoVstrPt != null ) ErrInfoVstrPt.Cpy( "当前激活的连接信息的指针为空，或当前连接状态不为已连接。" );
                break Out;
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //连接发送对讲模式包。
    public int CnctSendTkbkModePkt( CnctInfo CnctInfoPt, int LclTkbkMode )
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte )NtwkMediaPocsThrd.PktTyp.TkbkMode;
            m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] = ( byte )LclTkbkMode;
            if( CnctSendPkt( CnctInfoPt, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 2, 1, 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
            {
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + CnctInfoPt.hashCode() + "：发送一个对讲模式包成功。对讲模式：" + LclTkbkMode );
            }
            else
            {
                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + CnctInfoPt.hashCode() + "：发送一个对讲模式包失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //连接销毁。
    public void CnctDstoy( int CnctIdx )
    {
        Out:
        {
            if( ( CnctIdx >= m_CnctInfoLstPt.size() ) || ( CnctIdx < 0 ) )
            {
                String p_InfoStrPt = "网络媒体处理线程：对讲网络：没有索引为" + CnctIdx + "]的连接，无法删除。";
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                break Out;
            }

            CnctInfo p_CnctInfoTmpPt = m_CnctInfoLstPt.get( CnctIdx );
            p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。

            String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：请求销毁远端节点" + ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + p_CnctInfoTmpPt.m_RmtNodeNamePt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcPt + "]的连接。";
            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
        }
    }

    //接收输出帧初始化。
    public int RecvOtptFrmInit()
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            if( m_RecvOtptFrmIsInit == 0 )
            {
                switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
                {
                    case 0: //如果要使用容器。
                    {
                        //初始化接收音频输出帧容器。
                        m_RecvAdoOtptFrmCntnrPt = new ConcurrentLinkedQueue< byte[] >(); //创建接收音频输出帧容器。
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：初始化接收音频输出帧容器成功。" );

                        //初始化接收视频输出帧容器。
                        m_RecvVdoOtptFrmCntnrPt = new ConcurrentLinkedQueue< byte[] >(); //创建接收视频输出帧容器。
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：初始化接收视频输出帧容器成功。" );
                        break;
                    }
                    case 1: //如果要使用自适应抖动缓冲器。
                    {
                        //初始化音频自适应抖动缓冲器。
                        m_AAjbPt.m_Pt = new HeavenTao.Ado.AAjb();
                        if( m_AAjbPt.m_Pt.Init( m_NtwkMediaPocsThrdPt.m_AdoOtptPt.m_SmplRate, m_NtwkMediaPocsThrdPt.m_AdoOtptPt.m_FrmLenUnit, 1, 1, 0, m_AAjbPt.m_MinNeedBufFrmCnt, m_AAjbPt.m_MaxNeedBufFrmCnt, m_AAjbPt.m_MaxCntuLostFrmCnt, m_AAjbPt.m_AdaptSensitivity, ( m_XfrMode == 0 ) ? 0 : 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：初始化音频自适应抖动缓冲器成功。" );
                        }
                        else
                        {
                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：初始化音频自适应抖动缓冲器失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break Out;
                        }

                        //初始化视频自适应抖动缓冲器。
                        m_VAjbPt.m_Pt = new HeavenTao.Vdo.VAjb();
                        if( m_VAjbPt.m_Pt.Init( 1, m_VAjbPt.m_MinNeedBufFrmCnt, m_VAjbPt.m_MaxNeedBufFrmCnt, m_VAjbPt.m_AdaptSensitivity, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：初始化视频自适应抖动缓冲器成功。" );
                        }
                        else
                        {
                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：初始化视频自适应抖动缓冲器失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break Out;
                        }
                        break;
                    }
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
        return p_Rslt;
    }

    //接收输出帧重置。
    public void RecvOtptFrmReset()
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            if( m_RecvOtptFrmIsInit != 0 )
            {
                switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
                {
                    case 0: //如果要使用容器。
                    {
                        //清空接收音频输出帧容器。
                        m_RecvAdoOtptFrmCntnrPt.clear();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：清空接收音频输出帧容器成功。" );

                        //清空接收视频输出帧容器。
                        m_RecvVdoOtptFrmCntnrPt.clear();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：清空接收视频输出帧容器成功。" );
                        break;
                    }
                    case 1: //如果要使用自适应抖动缓冲器。
                    {
                        //清空并重置音频自适应抖动缓冲器。
                        if( m_AAjbPt.m_Pt.Reset( 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：清空并重置音频自适应抖动缓冲器成功。" );
                        }
                        else
                        {
                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：清空并重置音频自适应抖动缓冲器失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break Out;
                        }

                        //清空并重置视频自适应抖动缓冲器。
                        if( m_VAjbPt.m_Pt.Reset( 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                        {
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：清空并重置视频自适应抖动缓冲器成功。" );
                        }
                        else
                        {
                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：清空并重置视频自适应抖动缓冲器失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break Out;
                        }
                        break;
                    }
                }
            }
            else
            {
                RecvOtptFrmInit();
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
    }

    //接收输出帧销毁。
    public void RecvOtptFrmDstoy()
    {
        int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            if( m_RecvOtptFrmIsInit != 0 )
            {
                //销毁接收音频输出帧容器。
                if( m_RecvAdoOtptFrmCntnrPt != null )
                {
                    m_RecvAdoOtptFrmCntnrPt.clear();
                    m_RecvAdoOtptFrmCntnrPt = null;
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：销毁接收音频输出帧容器成功。" );
                }

                //销毁接收视频输出帧容器。
                if( m_RecvVdoOtptFrmCntnrPt != null )
                {
                    m_RecvVdoOtptFrmCntnrPt.clear();
                    m_RecvVdoOtptFrmCntnrPt = null;
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：销毁接收视频输出帧容器成功。" );
                }

                //销毁音频自适应抖动缓冲器。
                if( m_AAjbPt.m_Pt != null )
                {
                    m_AAjbPt.m_Pt.Dstoy( null );
                    m_AAjbPt.m_Pt = null;
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：销毁音频自适应抖动缓冲器成功。" );
                }

                //销毁视频自适应抖动缓冲器。
                if( m_VAjbPt.m_Pt != null )
                {
                    m_VAjbPt.m_Pt.Dstoy( null );
                    m_VAjbPt.m_Pt = null;
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：销毁视频自适应抖动缓冲器成功。" );
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {
        }
    }

    //连接处理，包括接受连接、连接服务端、接收数据包、删除连接。
    public void CnctPocs()
    {
        int p_TmpInt;
        int p_TmpElmTotal;

        //用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字的连接。
        if( m_TcpSrvrSoktPt != null )
        {
            int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
            TcpClntSokt p_TcpClntSoktTmpPt = null;

            TcpSrvrSoktAcptOut:
            {
                while( true )
                {
                    p_TcpClntSoktTmpPt = new TcpClntSokt();
                    if( m_TcpSrvrSoktPt.Acpt( p_TcpClntSoktTmpPt, null, m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt, m_NtwkMediaPocsThrdPt.m_RmtNodePortPt, ( short ) 0, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                    {
                        if( p_TcpClntSoktTmpPt.m_TcpClntSoktPt != 0 ) //如果用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字的连接成功。
                        {
                            if( p_TcpClntSoktTmpPt.SetNoDelay( 1, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpSrvrSoktAcptOut;
                            }

                            if( p_TcpClntSoktTmpPt.SetSendBufSz( 1024 * 1024, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：设置本端Tcp协议客户端套接字的发送缓冲区大小失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpSrvrSoktAcptOut;
                            }

                            if( p_TcpClntSoktTmpPt.SetRecvBufSz( 1024 * 1024, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：设置本端Tcp协议客户端套接字的接收缓冲区大小失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpSrvrSoktAcptOut;
                            }

                            if( p_TcpClntSoktTmpPt.SetKeepAlive( 1, 1, 1, 5, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：设置本端Tcp协议客户端套接字的保活机制失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpSrvrSoktAcptOut;
                            }

                            CnctInfo p_CnctInfoTmpPt;
                            if( ( p_CnctInfoTmpPt = CnctInfoInit( 0, 0, m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt.m_Val, m_NtwkMediaPocsThrdPt.m_RmtNodePortPt.m_Val, p_TcpClntSoktTmpPt, -1, NtwkMediaPocsThrd.CnctSts.Cnct, NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ NtwkMediaPocsThrd.TkbkMode.None ], NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ NtwkMediaPocsThrd.TkbkMode.None ] ) ) == null ) break TcpSrvrSoktAcptOut; //如果连接信息初始化失败。
                            if( ( m_IsAutoActCnct != 0 ) && ( m_CurActCnctInfoPt == null ) ) CnctInfoUpdtLclTkbkMode( p_CnctInfoTmpPt, m_NtwkMediaPocsThrdPt.m_LclTkbkMode );

                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字[" + m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_RmtNodePortPt.m_Val + "]的连接成功。";
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break;
                        }
                        else //如果用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字的连接超时，就跳出接受。
                        {
                            break TcpSrvrSoktAcptOut;
                        }
                    }
                    else
                    {
                        p_TcpClntSoktTmpPt = null;

                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：用本端Tcp协议服务端套接字接受远端Tcp协议客户端套接字的连接失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                        break TcpSrvrSoktAcptOut;
                    }
                }

                p_Rslt = 0; //设置本函数执行成功。
            }

            if( p_Rslt != 0 ) //如果本函数执行失败。
            {
                if( p_TcpClntSoktTmpPt != null )
                {
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte ) NtwkMediaPocsThrd.PktTyp.Exit; //设置退出包。
                    p_TcpClntSoktTmpPt.SendApkt( m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1, ( short ) 0, 1, 0, null ); //发送退出包。
                    p_TcpClntSoktTmpPt.Dstoy( ( short ) -1, null );
                }
            }
        }

        //用本端高级Udp协议服务端套接字接受远端高级Udp协议客户端套接字的连接。
        if( m_AudpSrvrSoktPt != null )
        {
            int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
            m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val = -1;

            AudpSrvrSoktAcptOut:
            {
                while( true ) //循环接受远端高级Udp协议套接字的连接。
                {
                    if( m_AudpSrvrSoktPt.Acpt( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt, null, m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt, m_NtwkMediaPocsThrdPt.m_RmtNodePortPt, ( short ) 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val != -1 ) //如果用本端高级Udp协议套接字接受远端高级Udp协议套接字的连接成功。
                        {
                            CnctInfo p_CnctInfoTmpPt;
                            if( ( p_CnctInfoTmpPt = CnctInfoInit( 0, 1,  m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt.m_Val, m_NtwkMediaPocsThrdPt.m_RmtNodePortPt.m_Val, null, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val, NtwkMediaPocsThrd.CnctSts.Cnct, NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ NtwkMediaPocsThrd.TkbkMode.None ], NtwkMediaPocsThrd.m_TkbkModeStrArrPt[ NtwkMediaPocsThrd.TkbkMode.None ] ) ) == null ) break AudpSrvrSoktAcptOut; //如果连接信息初始化失败。
                            if( ( m_IsAutoActCnct != 0 ) && ( m_CurActCnctInfoPt == null ) ) CnctInfoUpdtLclTkbkMode( p_CnctInfoTmpPt, m_NtwkMediaPocsThrdPt.m_LclTkbkMode );

                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端高级Udp协议服务端套接字接受远端高级Udp协议客户端套接字[" + m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_RmtNodePortPt.m_Val + "]的连接成功。";
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break;
                        }
                        else //如果用本端高级Udp协议服务端套接字接受远端高级Udp协议客户端套接字的连接超时，就跳出接受。
                        {
                            break AudpSrvrSoktAcptOut;
                        }
                    }
                    else
                    {
                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：用本端高级Udp协议服务端套接字接受远端高级Udp协议客户端套接字的连接失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                        break AudpSrvrSoktAcptOut;
                    }
                }

                p_Rslt = 0; //设置本函数执行成功。
            }

            if( p_Rslt != 0 ) //如果本函数执行失败。
            {
                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val != -1 )
                {
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte ) NtwkMediaPocsThrd.PktTyp.Exit; //设置退出包。
                    m_AudpSrvrSoktPt.SendApkt( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1, 1, 1, null ); //发送退出包。
                    m_AudpSrvrSoktPt.ClosCnct( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val, null );
                }
            }
        }

        //遍历连接列表。
        for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_CnctInfoLstPt.size(); p_CnctInfoLstIdx++ )
        {
            CnctInfo p_CnctInfoTmpPt = m_CnctInfoLstPt.get( p_CnctInfoLstIdx );

            //用本端客户端套接字连接远端服务端套接字。
            if( ( p_CnctInfoTmpPt.m_IsRqstDstoy == 0 ) && ( p_CnctInfoTmpPt.m_IsSrvrOrClntCnct == 1 ) && ( p_CnctInfoTmpPt.m_CurCnctSts <= NtwkMediaPocsThrd.CnctSts.Wait ) ) //如果该连接未请求销毁，且为客户端的连接，且当前连接状态为等待远端接受连接。
            {
                int p_RmtNodeAddrFamly; //存放远端节点的地址族，为4表示IPv4，为6表示IPv6，为0表示自动选择。

                { //设置远端节点的地址族。
                    try
                    {
                        InetAddress inetAddress = InetAddress.getByName( p_CnctInfoTmpPt.m_RmtNodeNamePt );
                        if( inetAddress.getAddress().length == 4 ) p_RmtNodeAddrFamly = 4;
                        else p_RmtNodeAddrFamly = 6;
                    }
                    catch( UnknownHostException e )
                    {
                        p_RmtNodeAddrFamly = 0;
                    }
                }

                if( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) //用本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字。
                {
                    int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

                    TcpClntSoktCnctOut:
                    {
                        if( p_CnctInfoTmpPt.m_TcpClntSoktPt == null ) //如果未初始化本端Tcp协议客户端套接字。
                        {
                            if( -p_CnctInfoTmpPt.m_CurCnctSts >= m_NtwkMediaPocsThrdPt.m_MaxCnctTimes ) //如果未达到最大连接次数。
                            {
                                p_CnctInfoTmpPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Dsct; //设置当前连接状态为已断开。
                                p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。
                                m_NtwkMediaPocsThrdPt.UserCnctModify( p_CnctInfoTmpPt.m_Idx, null, "最大连接次数", "" );

                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：已达到最大连接次数，中断连接。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpClntSoktCnctOut;
                            }

                            p_CnctInfoTmpPt.m_CurCnctSts--; //递增当前连接次数。
                            m_NtwkMediaPocsThrdPt.UserCnctModify( p_CnctInfoTmpPt.m_Idx, null, "第" + -p_CnctInfoTmpPt.m_CurCnctSts + "次连接", "" );

                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：开始第 " + -p_CnctInfoTmpPt.m_CurCnctSts + " 次连接。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }

                            p_CnctInfoTmpPt.m_TcpClntSoktPt = new TcpClntSokt();
                            if( p_CnctInfoTmpPt.m_TcpClntSoktPt.Init( p_RmtNodeAddrFamly, p_CnctInfoTmpPt.m_RmtNodeNamePt, p_CnctInfoTmpPt.m_RmtNodeSrvcPt, null, null, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字失败。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNamePt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcPt + "]失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpClntSoktCnctOut;
                            }
                        }

                        if( p_CnctInfoTmpPt.m_TcpClntSoktPt.WaitCnct( ( short ) 0, m_NtwkMediaPocsThrdPt.m_TmpHTIntPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //如果等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字成功。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == TcpClntSokt.TcpCnctStsWait ) //如果等待远端接受连接。
                            {
                                //继续等待本端本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字。
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == TcpClntSokt.TcpCnctStsCnct ) //如果连接成功。
                            {
                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetNoDelay( 1, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：设置本端Tcp协议客户端套接字的Nagle延迟算法状态为禁用失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetSendBufSz( 1024 * 1024, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：设置本端Tcp协议客户端套接字的发送缓冲区大小失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetRecvBufSz( 1024 * 1024, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：设置本端Tcp协议客户端套接字的接收缓冲区大小失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.SetKeepAlive( 1, 1, 1, 5, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：设置本端Tcp协议客户端套接字的保活机制失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.GetLclAddr( null, m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt, m_NtwkMediaPocsThrdPt.m_LclNodePortPt, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：获取本端Tcp协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                if( p_CnctInfoTmpPt.m_TcpClntSoktPt.GetRmtAddr( null, m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt, m_NtwkMediaPocsThrdPt.m_RmtNodePortPt, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：获取本端Tcp协议客户端套接字连接的远端Tcp协议客户端套接字绑定的远程节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break TcpClntSoktCnctOut;
                                }

                                p_CnctInfoTmpPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Cnct; //设置当前连接状态为已连接。
                                if( ( m_IsAutoActCnct != 0 ) && ( m_CurActCnctInfoPt == null ) ) CnctInfoUpdtLclTkbkMode( p_CnctInfoTmpPt, m_NtwkMediaPocsThrdPt.m_LclTkbkMode );
                                else CnctInfoUpdtLclTkbkMode( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_LclTkbkMode );
                                CnctInfoUpdtRmtTkbkMode( p_CnctInfoTmpPt );

                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化本端Tcp协议客户端套接字[" + m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_LclNodePortPt.m_Val + "]，并连接远端Tcp协议服务端套接字[" + m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_RmtNodePortPt.m_Val + "]成功。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }
                            else //如果连接失败。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNamePt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcPt + "]失败。原因：连接失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break TcpClntSoktCnctOut;
                            }
                        }
                        else //如果等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字失败。
                        {
                            String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：初始化本端Tcp协议客户端套接字，并连接远端Tcp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNamePt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcPt + "]失败。原因：等待本端Tcp协议客户端套接字连接远端Tcp协议服务端套接字失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                            m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            break TcpClntSoktCnctOut;
                        }

                        p_Rslt = 0; //设置本函数执行成功。
                    }

                    if( p_Rslt != 0 ) //如果本函数执行失败。
                    {
                        if( p_CnctInfoTmpPt.m_TcpClntSoktPt != null )
                        {
                            p_CnctInfoTmpPt.m_TcpClntSoktPt.Dstoy( ( short ) -1, null );
                            p_CnctInfoTmpPt.m_TcpClntSoktPt = null;
                        }
                    }
                }
                else //用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字。
                {
                    int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

                    AudpClntSoktCnctOut:
                    {
                        if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt == null ) //如果未初始化本端高级Udp协议客户端套接字。
                        {
                            m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt = new AudpSokt();
                            if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.Init( p_RmtNodeAddrFamly, null, null, ( short )0, ( short )5000, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //如果初始化本端高级Udp协议客户端套接字成功。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.GetLclAddr( null, m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt, m_NtwkMediaPocsThrdPt.m_LclNodePortPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果获取本端高级Udp协议套接字绑定的本地节点地址和端口失败。
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：获取本端高级Udp协议客户端套接字绑定的本地节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break AudpClntSoktCnctOut;
                                }

                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：初始化本端高级Udp协议客户端套接字[" + m_NtwkMediaPocsThrdPt.m_LclNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_LclNodePortPt.m_Val + "]成功。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }
                            else //如果初始化本端高级Udp协议客户端套接字失败。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：初始化本端高级Udp协议客户端套接字[" + null + ":" + null + "]失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }
                        }

                        if( p_CnctInfoTmpPt.m_AudpClntCnctIdx == -1 )
                        {
                            if( -p_CnctInfoTmpPt.m_CurCnctSts >= m_NtwkMediaPocsThrdPt.m_MaxCnctTimes ) //如果未达到最大连接次数。
                            {
                                p_CnctInfoTmpPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Dsct; //设置当前连接状态为已断开。
                                p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。
                                m_NtwkMediaPocsThrdPt.UserCnctModify( p_CnctInfoTmpPt.m_Idx, null, "最大连接次数", "" );

                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：已达到最大连接次数，中断连接。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }

                            p_CnctInfoTmpPt.m_CurCnctSts--; //递增当前连接次数。
                            m_NtwkMediaPocsThrdPt.UserCnctModify( p_CnctInfoTmpPt.m_Idx, null, "第" + -p_CnctInfoTmpPt.m_CurCnctSts + "次连接", "" );

                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：开始第 " + -p_CnctInfoTmpPt.m_CurCnctSts + " 次连接。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }

                            if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.Cnct( p_RmtNodeAddrFamly, p_CnctInfoTmpPt.m_RmtNodeNamePt, p_CnctInfoTmpPt.m_RmtNodeSrvcPt, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 ) //如果用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字失败。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNamePt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcPt + "]失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }
                            p_CnctInfoTmpPt.m_AudpClntCnctIdx = m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val; //设置本端高级Udp协议客户端连接索引。
                        }

                        if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.WaitCnct( p_CnctInfoTmpPt.m_AudpClntCnctIdx, ( short )0, m_NtwkMediaPocsThrdPt.m_TmpHTIntPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) //循环等待本端高级Udp协议套接字连接远端成功。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsWait ) //如果等待远端接受连接。
                            {
                                //重新循环，继续等待本端高级Udp协议套接字连接远端。
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsCnct ) //如果连接成功。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.GetRmtAddr( p_CnctInfoTmpPt.m_AudpClntCnctIdx, null, m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt, m_NtwkMediaPocsThrdPt.m_RmtNodePortPt, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) != 0 )
                                {
                                    String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：获取本端高级Udp协议客户端套接字连接的远端高级Udp协议服务端套接字绑定的远程节点地址和端口失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                    break AudpClntSoktCnctOut;
                                }

                                p_CnctInfoTmpPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Cnct; //设置当前连接状态为已连接。
                                if( ( m_IsAutoActCnct != 0 ) && ( m_CurActCnctInfoPt == null ) ) CnctInfoUpdtLclTkbkMode( p_CnctInfoTmpPt, m_NtwkMediaPocsThrdPt.m_LclTkbkMode );
                                else CnctInfoUpdtLclTkbkMode( p_CnctInfoTmpPt, p_CnctInfoTmpPt.m_LclTkbkMode );
                                CnctInfoUpdtRmtTkbkMode( p_CnctInfoTmpPt );

                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + m_NtwkMediaPocsThrdPt.m_RmtNodeAddrPt.m_Val + ":" + m_NtwkMediaPocsThrdPt.m_RmtNodePortPt.m_Val + "]成功。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsTmot ) //如果连接超时。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNamePt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcPt + "]失败。原因：连接超时。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpHTIntPt.m_Val == AudpSokt.AudpCnctStsDsct ) //如果连接断开。
                            {
                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端高级Udp协议客户端套接字连接远端高级Udp协议服务端套接字[" + p_CnctInfoTmpPt.m_RmtNodeNamePt + ":" + p_CnctInfoTmpPt.m_RmtNodeSrvcPt + "]失败。原因：连接断开。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                break AudpClntSoktCnctOut;
                            }
                        }

                        p_Rslt = 0; //设置本函数执行成功。
                    }

                    if( p_Rslt != 0 ) //如果本函数执行失败。
                    {
                        if( ( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt != null ) && ( p_CnctInfoTmpPt.m_AudpClntCnctIdx != -1 ) )
                        {
                            m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.ClosCnct( p_CnctInfoTmpPt.m_AudpClntCnctIdx, null );
                            p_CnctInfoTmpPt.m_AudpClntCnctIdx = -1;
                        }
                    }
                }
            }

            //用本端客户端套接字接收远端服务端套接字发送过来的一个数据包。
            if( ( p_CnctInfoTmpPt.m_IsRqstDstoy == 0 ) && ( p_CnctInfoTmpPt.m_CurCnctSts == NtwkMediaPocsThrd.CnctSts.Cnct ) ) //如果该连接未请求销毁，且当前连接状态为已连接。
            {
                RecvPktOut:
                {
                    if( ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) && ( p_CnctInfoTmpPt.m_TcpClntSoktPt.RecvApkt( m_NtwkMediaPocsThrdPt.m_TmpBytePt, m_NtwkMediaPocsThrdPt.m_TmpBytePt.length, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt, ( short ) 0, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
                        ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( p_CnctInfoTmpPt.m_IsSrvrOrClntCnct == 0 ) && ( m_AudpSrvrSoktPt.RecvApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_NtwkMediaPocsThrdPt.m_TmpBytePt, m_NtwkMediaPocsThrdPt.m_TmpBytePt.length, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt, null, ( short ) 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) ||
                        ( ( p_CnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 1 ) && ( p_CnctInfoTmpPt.m_IsSrvrOrClntCnct == 1 ) && ( m_NtwkMediaPocsThrdPt.m_AudpClntSoktPt.RecvApkt( p_CnctInfoTmpPt.m_AudpClntCnctIdx, m_NtwkMediaPocsThrdPt.m_TmpBytePt, m_NtwkMediaPocsThrdPt.m_TmpBytePt.length, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt, null, ( short ) 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 ) ) )
                    {
                        if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val != -1 ) //如果用本端套接字接收一个连接的远端套接字发送的数据包成功。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val == 0 ) //如果数据包的数据长度为0。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个数据包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "，表示没有数据，无法继续接收。" );
                                break RecvPktOut;
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] == ( byte ) NtwkMediaPocsThrd.PktTyp.TkbkMode ) //如果是对讲模式包。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val < 1 + 1 ) //如果音频输出帧包的数据长度小于1 + 1，表示没有对讲模式。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个对讲模式包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 1，表示没有对讲模式，无法继续接收。" );
                                    break RecvPktOut;
                                }
                                if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] >= ( byte ) NtwkMediaPocsThrd.TkbkMode.NoChg )
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个对讲模式包的对讲模式为" + m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] + "不正确，无法继续接收。" );
                                    break RecvPktOut;
                                }

                                p_CnctInfoTmpPt.m_RmtTkbkMode = m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ]; //设置远端对讲模式。
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个对讲模式包。对讲模式：" + p_CnctInfoTmpPt.m_RmtTkbkMode );

                                if( ( m_IsAutoActCnct != 0 ) && ( m_CurActCnctInfoPt == null ) && ( p_CnctInfoTmpPt.m_LclTkbkMode != NtwkMediaPocsThrd.TkbkMode.None ) && ( p_CnctInfoTmpPt.m_RmtTkbkMode != NtwkMediaPocsThrd.TkbkMode.None ) ) CnctInfoAct( p_CnctInfoTmpPt ); //连接信息激活。
                                CnctInfoUpdtRmtTkbkMode( p_CnctInfoTmpPt );
                                if( m_CurActCnctInfoPt == p_CnctInfoTmpPt ) m_NtwkMediaPocsThrdPt.SetTkbkMode( 1 ); //设置对讲模式。
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] == ( byte ) NtwkMediaPocsThrd.PktTyp.AdoFrm ) //如果是音频输出帧包。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val < 1 + 4 ) //如果音频输出帧包的数据长度小于1 + 4，表示没有音频输出帧时间戳。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个音频输出帧包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有音频输出帧时间戳，无法继续接收。" );
                                    break RecvPktOut;
                                }

                                //读取音频输出帧时间戳。
                                p_TmpInt = ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] & 0xFF ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) << 8 ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 16 ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 24 );

                                if( ( m_CurActCnctInfoPt == p_CnctInfoTmpPt ) && //如果该连接为当前激活连接。
                                    ( ( m_NtwkMediaPocsThrdPt.m_AdoOtptPt.m_IsInit != 0 ) || //如果已初始化音频输出。
                                      ( ( m_XfrMode == 0 ) && ( ( m_NtwkMediaPocsThrdPt.m_LclTkbkMode == NtwkMediaPocsThrd.TkbkMode.Ado ) || ( m_NtwkMediaPocsThrdPt.m_LclTkbkMode == NtwkMediaPocsThrd.TkbkMode.AdoVdo ) ) ) ) ) //如果传输模式为实时半双工（一键通），且本端对讲模式为音频或音视频。
                                {
                                    //将音频输出帧放入容器或自适应抖动缓冲器。
                                    switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
                                    {
                                        case 0: //如果要使用容器。
                                        {
                                            if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                                            {
                                                p_TmpElmTotal = m_RecvAdoOtptFrmCntnrPt.size(); //获取接收音频输出帧容器的元素总数。
                                                if( p_TmpElmTotal <= 50 )
                                                {
                                                    m_RecvAdoOtptFrmCntnrPt.offer( Arrays.copyOfRange( m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1 + 4, ( int ) ( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val ) ) );
                                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有语音活动的音频输出帧包，并放入接收音频输出帧容器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                                }
                                                else
                                                {
                                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有语音活动的音频输出帧包，但接收音频输出帧容器中帧总数为" + p_TmpElmTotal + "已经超过上限50，不再放入接收音频输出帧容器。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                                }
                                            }
                                            else //如果该音频输出帧为无语音活动。
                                            {
                                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个无语音活动的音频输出帧包，无需放入接收音频输出帧容器。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                            }
                                            break;
                                        }
                                        case 1: //如果要使用自适应抖动缓冲器。
                                        {
                                            if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                                            {
                                                m_AAjbPt.m_Pt.PutByteFrm( p_TmpInt, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1 + 4, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val - 1 - 4, 1, null );
                                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有语音活动的音频输出帧包，并放入音频自适应抖动缓冲器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                            }
                                            else //如果该音频输出帧为无语音活动。
                                            {
                                                m_AAjbPt.m_Pt.PutByteFrm( p_TmpInt, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1 + 4, 0, 1, null );
                                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个无语音活动的音频输出帧包，并放入音频自适应抖动缓冲器成功。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                            }

                                            m_AAjbPt.m_Pt.GetBufFrmCnt( m_NtwkMediaPocsThrdPt.m_CurHaveBufActFrmCntPt, m_NtwkMediaPocsThrdPt.m_CurHaveBufInactFrmCntPt, m_NtwkMediaPocsThrdPt.m_CurHaveBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MinNeedBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MaxNeedBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MaxCntuLostFrmCntPt, m_NtwkMediaPocsThrdPt.m_CurNeedBufFrmCntPt, 1, null );
                                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：音频自适应抖动缓冲器：有活动帧：" + m_NtwkMediaPocsThrdPt.m_CurHaveBufActFrmCntPt.m_Val + "，无活动帧：" + m_NtwkMediaPocsThrdPt.m_CurHaveBufInactFrmCntPt.m_Val + "，帧：" + m_NtwkMediaPocsThrdPt.m_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + m_NtwkMediaPocsThrdPt.m_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + m_NtwkMediaPocsThrdPt.m_MaxNeedBufFrmCntPt.m_Val + "，最大丢帧：" + m_NtwkMediaPocsThrdPt.m_MaxCntuLostFrmCntPt.m_Val + "，当前需帧：" + m_NtwkMediaPocsThrdPt.m_CurNeedBufFrmCntPt.m_Val + "。" );
                                            break;
                                        }
                                    }
                                }
                                else //如果未初始化音频输出。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该音频输出帧为有语音活动。
                                    {
                                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有语音活动的音频输出帧包成功，但不是当前激活连接或未初始化音频输出。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    else //如果该音频输出帧为无语音活动。
                                    {
                                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个无语音活动的音频输出帧包成功，但不是当前激活连接或未初始化音频输出。音频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                    }
                                }
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] == ( byte ) NtwkMediaPocsThrd.PktTyp.VdoFrm ) //如果是视频输出帧包。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val < 1 + 4 ) //如果视频输出帧包的数据长度小于1 + 4，表示没有视频输出帧时间戳。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个视频输出帧包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "小于1 + 4，表示没有视频输出帧时间戳，无法继续接收。" );
                                    break RecvPktOut;
                                }

                                //读取视频输出帧时间戳。
                                p_TmpInt = ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] & 0xFF ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 2 ] & 0xFF ) << 8 ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 3 ] & 0xFF ) << 16 ) + ( ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 4 ] & 0xFF ) << 24 );

                                if( ( m_CurActCnctInfoPt == p_CnctInfoTmpPt ) && //如果该连接为当前激活连接。
                                    ( ( m_NtwkMediaPocsThrdPt.m_VdoOtptPt.m_IsInit != 0 ) || //如果已初始化视频输出。
                                      ( ( m_XfrMode == 0 ) && ( ( m_NtwkMediaPocsThrdPt.m_LclTkbkMode == NtwkMediaPocsThrd.TkbkMode.Vdo ) || ( m_NtwkMediaPocsThrdPt.m_LclTkbkMode == NtwkMediaPocsThrd.TkbkMode.AdoVdo ) ) ) ) ) //如果传输模式为实时半双工（一键通），且本端对讲模式为视频或音视频。
                                {
                                    //将视频输出帧放入容器或自适应抖动缓冲器。
                                    switch( m_UseWhatRecvOtptFrm ) //使用什么接收输出帧。
                                    {
                                        case 0: //如果要使用容器。
                                        {
                                            if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                                            {
                                                p_TmpElmTotal = m_RecvVdoOtptFrmCntnrPt.size(); //获取接收视频输出帧容器的元素总数。
                                                if( p_TmpElmTotal <= 20 )
                                                {
                                                    m_RecvVdoOtptFrmCntnrPt.offer( Arrays.copyOfRange( m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1 + 4, ( int ) ( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val ) ) );
                                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有图像活动的视频输出帧包，并放入接收视频输出帧容器成功。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                                }
                                                else
                                                {
                                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有图像活动的视频输出帧包，但接收视频输出帧容器中帧总数为" + p_TmpElmTotal + "已经超过上限20，不再放入接收视频输出帧容器。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                                }
                                            }
                                            else //如果该视频输出帧为无图像活动。
                                            {
                                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个无图像活动的视频输出帧包，无需放入接收视频输出帧容器。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                            }
                                            break;
                                        }
                                        case 1: //如果要使用自适应抖动缓冲器。
                                        {
                                            if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                                            {
                                                m_VAjbPt.m_Pt.PutByteFrm( SystemClock.uptimeMillis(), p_TmpInt, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1 + 4, m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val - 1 - 4, 1, null );
                                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有图像活动的视频输出帧包，并放入视频自适应抖动缓冲器成功。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "，类型：" + ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 9 ] & 0xff ) + "。" );
                                            }
                                            else //如果该视频输出帧为无图像活动。
                                            {
                                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个无图像活动的视频输出帧包，无需放入视频自适应抖动缓冲器。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                            }

                                            m_VAjbPt.m_Pt.GetBufFrmCnt( m_NtwkMediaPocsThrdPt.m_CurHaveBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MinNeedBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MaxNeedBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_CurNeedBufFrmCntPt, 1, null );
                                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：视频自适应抖动缓冲器：帧：" + m_NtwkMediaPocsThrdPt.m_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + m_NtwkMediaPocsThrdPt.m_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + m_NtwkMediaPocsThrdPt.m_MaxNeedBufFrmCntPt.m_Val + "，当前需帧：" + m_NtwkMediaPocsThrdPt.m_CurNeedBufFrmCntPt.m_Val + "。" );
                                            break;
                                        }
                                    }
                                }
                                else //如果未初始化视频输出。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 + 4 ) //如果该视频输出帧为有图像活动。
                                    {
                                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个有图像活动的视频输出帧包成功，但不是当前激活连接或未初始化视频输出。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                    }
                                    else //如果该视频输出帧为无图像活动。
                                    {
                                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个无图像活动的视频输出帧包成功，但不是当前激活连接或未初始化视频输出。视频输出帧时间戳：" + p_TmpInt + "，总长度：" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "。" );
                                    }
                                }
                            }
                            else if( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] == ( byte ) NtwkMediaPocsThrd.PktTyp.Exit ) //如果是退出包。
                            {
                                if( m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val > 1 ) //如果退出包的数据长度大于1。
                                {
                                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个退出包的数据长度为" + m_NtwkMediaPocsThrdPt.m_TmpHTLongPt.m_Val + "大于1，表示还有其他数据，无法继续接收。" );
                                    break RecvPktOut;
                                }

                                p_CnctInfoTmpPt.m_IsRecvExitPkt = 1; //设置已经接收到退出包。
                                p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。

                                String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：接收到一个退出包。";
                                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                                m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                                if( m_NtwkMediaPocsThrdPt.m_IsShowToast != 0 ) m_NtwkMediaPocsThrdPt.UserShowToast( p_InfoStrPt );
                            }
                        } //如果用本端套接字接收一个连接的远端套接字发送的数据包超时，就重新接收。
                    }
                    else //如果用本端套接字接收一个连接的远端套接字发送的数据包失败。
                    {
                        p_CnctInfoTmpPt.m_CurCnctSts = NtwkMediaPocsThrd.CnctSts.Tmot; //设置当前连接状态为异常断开。
                        p_CnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。

                        String p_InfoStrPt = "网络媒体处理线程：对讲网络：连接" + p_CnctInfoTmpPt.hashCode() + "：用本端套接字接收一个连接的远端套接字发送的数据包失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr();
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_NtwkMediaPocsThrdPt.m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                        break RecvPktOut;
                    }
                }
            }

            //销毁连接信息。
            if( p_CnctInfoTmpPt.m_IsRqstDstoy == 1 ) //如果该连接已请求销毁。
            {
                CnctInfoDstoy( p_CnctInfoTmpPt );
                p_CnctInfoLstIdx--;
            }
        }
    }

    //用户定义的读取音视频输入帧函数。
    public void UserReadAdoVdoInptFrm( short AdoInptPcmSrcFrmPt[], short AdoInptPcmRsltFrmPt[], long AdoInptPcmFrmLenUnit, int AdoInptPcmRsltFrmVoiceActSts,
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
                if( AdoInptPcmRsltFrmVoiceActSts != 0 ) //如果音频输入Pcm格式结果帧为有语音活动。
                {
                    for( p_TmpInt32 = 0; p_TmpInt32 < AdoInptPcmRsltFrmPt.length; p_TmpInt32++ ) //设置音频输入帧。
                    {
                        m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 + 4 + p_TmpInt32 * 2 ] = ( byte ) ( AdoInptPcmRsltFrmPt[ p_TmpInt32 ] & 0xFF );
                        m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 + 4 + p_TmpInt32 * 2 + 1 ] = ( byte ) ( ( AdoInptPcmRsltFrmPt[ p_TmpInt32 ] & 0xFF00 ) >> 8 );
                    }
                    p_FrmPktLen = 1 + 4 + AdoInptPcmRsltFrmPt.length * 2; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 音频输入Pcm格式结果帧。
                }
                else //如果音频输入Pcm格式结果帧为无语音活动。
                {
                    p_FrmPktLen = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
                }
            }
            else //如果有音频输入已编码格式结果帧。
            {
                if( AdoInptPcmRsltFrmVoiceActSts != 0 && AdoInptEncdRsltFrmIsNeedTrans != 0 ) //如果音频输入Pcm格式结果帧为有语音活动，且音频输入已编码格式结果帧需要传输。
                {
                    System.arraycopy( AdoInptEncdRsltFrmPt, 0, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1 + 4, ( int ) AdoInptEncdRsltFrmLenByt ); //设置音频输入帧。
                    p_FrmPktLen = 1 + 4 + ( int ) AdoInptEncdRsltFrmLenByt; //数据包长度 = 数据包类型 + 音频输入帧时间戳 + 音频输入已编码格式结果帧。
                }
                else //如果音频输入Pcm格式结果帧为无语音活动，或不需要传输。
                {
                    p_FrmPktLen = 1 + 4; //数据包长度 = 数据包类型 + 音频输入帧时间戳。
                }
            }

            if( p_FrmPktLen != 1 + 4 ) //如果本次音频输入帧为有语音活动，就发送。
            {
                m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

                //设置数据包类型为音频输入帧包。
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte ) NtwkMediaPocsThrd.PktTyp.AdoFrm;
                //设置音频输入帧时间戳。
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] = ( byte ) ( m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp & 0xFF );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( ( m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

                if( CnctSendPkt( m_CurActCnctInfoPt, m_NtwkMediaPocsThrdPt.m_TmpBytePt, p_FrmPktLen, 1, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                {
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "发送一个有语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。" );
                }
                else
                {
                    String p_InfoStrPt = "发送一个有语音活动的音频输入帧包失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() + "。音频输入帧时间戳：" + m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。";
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                }

                m_CurActCnctInfoPt.m_LastSendAdoInptFrmIsAct = 1; //设置最后一个发送的音频输入帧有语音活动。
            }
            else //如果本次音频输入帧为无语音活动。
            {
                if( m_CurActCnctInfoPt.m_LastSendAdoInptFrmIsAct != 0 ) //如果最后一个发送的音频输入帧为有语音活动，就发送。
                {
                    m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp += 1; //音频输入帧的时间戳递增一个步进。

                    //设置数据包类型为音频输入帧包。
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte ) NtwkMediaPocsThrd.PktTyp.AdoFrm;
                    //设置音频输入帧时间戳。
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] = ( byte ) ( m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp & 0xFF );
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( ( m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
                    m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

                    if( CnctSendPkt( m_CurActCnctInfoPt, m_NtwkMediaPocsThrdPt.m_TmpBytePt, p_FrmPktLen, 1, 0, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                    {
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "发送一个无语音活动的音频输入帧包成功。音频输入帧时间戳：" + m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。" );
                    }
                    else
                    {
                        String p_InfoStrPt = "发送一个无语音活动的音频输入帧包失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() + "。音频输入帧时间戳：" + m_CurActCnctInfoPt.m_LastSendAdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "。";
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                        m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                    }

                    m_CurActCnctInfoPt.m_LastSendAdoInptFrmIsAct = 0; //设置最后一个发送的音频输入帧无语音活动。
                }
                else //如果最后一个发送的音频输入帧为无语音活动，无需发送。
                {
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本次音频输入帧为无语音活动，且最后一个发送的音频输入帧为无语音活动，无需发送。" );
                }
            }
        }

        //发送视频输入帧。
        if( VdoInptYu12RsltFrmPt != null ) //如果有视频输入Yu12格式结果帧。
        {
            if( VdoInptEncdRsltFrmPt == null ) //如果没有视频输入已编码格式结果帧。
            {
                //设置视频输入帧宽度。
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 5 ] = ( byte ) ( VdoInptYu12RsltFrmWidth & 0xFF );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 6 ] = ( byte ) ( ( VdoInptYu12RsltFrmWidth & 0xFF00 ) >> 8 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 7 ] = ( byte ) ( ( VdoInptYu12RsltFrmWidth & 0xFF0000 ) >> 16 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 8 ] = ( byte ) ( ( VdoInptYu12RsltFrmWidth & 0xFF000000 ) >> 24 );
                //设置视频输入帧高度。
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 9 ] = ( byte ) ( VdoInptYu12RsltFrmHeight & 0xFF );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 10 ] = ( byte ) ( ( VdoInptYu12RsltFrmHeight & 0xFF00 ) >> 8 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 11 ] = ( byte ) ( ( VdoInptYu12RsltFrmHeight & 0xFF0000 ) >> 16 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 12 ] = ( byte ) ( ( VdoInptYu12RsltFrmHeight & 0xFF000000 ) >> 24 );

                System.arraycopy( VdoInptYu12RsltFrmPt, 0, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1 + 4 + 4 + 4, VdoInptYu12RsltFrmPt.length ); //设置视频输入帧。
                p_FrmPktLen = 1 + 4 + 4 + 4 + VdoInptYu12RsltFrmPt.length; //数据包长度 = 数据包类型 + 视频输入帧时间戳 + 视频输入帧宽度 + 视频输入帧高度 + 视频输入Yu12格式结果帧。
            }
            else //如果有视频输入已编码格式结果帧。
            {
                if( VdoInptEncdRsltFrmLenByt != 0 ) //如果本次视频输入帧为有图像活动。
                {
                    System.arraycopy( VdoInptEncdRsltFrmPt, 0, m_NtwkMediaPocsThrdPt.m_TmpBytePt, 1 + 4, ( int ) VdoInptEncdRsltFrmLenByt ); //设置视频输入帧。
                    p_FrmPktLen = 1 + 4 + ( int ) VdoInptEncdRsltFrmLenByt; //数据包长度 = 数据包类型 + 视频输入帧时间戳 + 视频输入已编码格式结果帧。
                }
                else
                {
                    p_FrmPktLen = 1 + 4; //数据包长度 = 数据包类型 + 视频输入帧时间戳。
                }
            }

            if( p_FrmPktLen != 1 + 4 ) //如果本次视频输入帧为有图像活动，就发送。
            {
                m_CurActCnctInfoPt.m_LastSendVdoInptFrmTimeStamp += 1; //视频输入帧的时间戳递增一个步进。

                //设置数据包类型为视频输入帧包。
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 0 ] = ( byte ) NtwkMediaPocsThrd.PktTyp.VdoFrm;
                //设置视频输入帧时间戳。
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 1 ] = ( byte ) ( m_CurActCnctInfoPt.m_LastSendVdoInptFrmTimeStamp & 0xFF );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 2 ] = ( byte ) ( ( m_CurActCnctInfoPt.m_LastSendVdoInptFrmTimeStamp & 0xFF00 ) >> 8 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 3 ] = ( byte ) ( ( m_CurActCnctInfoPt.m_LastSendVdoInptFrmTimeStamp & 0xFF0000 ) >> 16 );
                m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 4 ] = ( byte ) ( ( m_CurActCnctInfoPt.m_LastSendVdoInptFrmTimeStamp & 0xFF000000 ) >> 24 );

                if( CnctSendPkt( m_CurActCnctInfoPt, m_NtwkMediaPocsThrdPt.m_TmpBytePt, p_FrmPktLen, 1, 1, m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt ) == 0 )
                {
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "发送一个有图像活动的视频输入帧包成功。视频输入帧时间戳：" + m_CurActCnctInfoPt.m_LastSendVdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "，类型：" + ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 9 ] & 0xff ) + "。" );
                }
                else
                {
                    String p_InfoStrPt = "发送一个有图像活动的视频输入帧包失败。原因：" + m_NtwkMediaPocsThrdPt.m_ErrInfoVstrPt.GetStr() + "。视频输入帧时间戳：" + m_CurActCnctInfoPt.m_LastSendVdoInptFrmTimeStamp + "，总长度：" + p_FrmPktLen + "，类型：" + ( m_NtwkMediaPocsThrdPt.m_TmpBytePt[ 9 ] & 0xff ) + "。";
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                    m_NtwkMediaPocsThrdPt.UserShowLog( p_InfoStrPt );
                }
            }
            else //如果本次视频输入帧为无图像活动，无需发送。
            {
                if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "本次视频输入帧为无图像活动，无需发送。" );
            }
        }
    }

    //用户定义的写入音频输出帧函数。
    public void UserWriteAdoOtptFrm( int AdoOtptStrmIdx,
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
                        if( !m_RecvAdoOtptFrmCntnrPt.isEmpty() ) //如果接收音频输出帧容器不为空。
                        {
                            p_AdoOtptFrmPt = m_RecvAdoOtptFrmCntnrPt.poll(); //从接收音频输出帧容器中取出并删除第一个帧。
                            p_AdoOtptFrmLen = p_AdoOtptFrmPt.length;
                        }

                        if( p_AdoOtptFrmLen != 0 ) //如果音频输出帧为有语音活动。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从接收音频输出帧容器取出一个有语音活动的音频输出帧。数据长度：" + p_AdoOtptFrmLen + "。" );
                        }
                        else //如果音频输出帧为无语音活动。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从接收音频输出帧容器取出一个无语音活动的音频输出帧。数据长度：" + p_AdoOtptFrmLen + "。" );
                        }

                        break;
                    }
                    case 1: //如果要使用自适应抖动缓冲器。
                    {
                        m_AAjbPt.m_Pt.GetBufFrmCnt( m_NtwkMediaPocsThrdPt.m_CurHaveBufActFrmCntPt, m_NtwkMediaPocsThrdPt.m_CurHaveBufInactFrmCntPt, m_NtwkMediaPocsThrdPt.m_CurHaveBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MinNeedBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MaxNeedBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MaxCntuLostFrmCntPt, m_NtwkMediaPocsThrdPt.m_CurNeedBufFrmCntPt, 1, null );
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "音频自适应抖动缓冲器：有活动帧：" + m_NtwkMediaPocsThrdPt.m_CurHaveBufActFrmCntPt.m_Val + "，无活动帧：" + m_NtwkMediaPocsThrdPt.m_CurHaveBufInactFrmCntPt.m_Val + "，帧：" + m_NtwkMediaPocsThrdPt.m_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + m_NtwkMediaPocsThrdPt.m_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + m_NtwkMediaPocsThrdPt.m_MaxNeedBufFrmCntPt.m_Val + "，最大丢帧：" + m_NtwkMediaPocsThrdPt.m_MaxCntuLostFrmCntPt.m_Val + "，当前需帧：" + m_NtwkMediaPocsThrdPt.m_CurNeedBufFrmCntPt.m_Val + "。" );

                        //从音频自适应抖动缓冲器取出音频输出帧。
                        m_AAjbPt.m_Pt.GetByteFrm( m_NtwkMediaPocsThrdPt.m_TmpHTInt2Pt, m_NtwkMediaPocsThrdPt.m_TmpByte2Pt, 0, m_NtwkMediaPocsThrdPt.m_TmpByte2Pt.length, m_NtwkMediaPocsThrdPt.m_TmpHTLong2Pt, 1, null );
                        p_AdoOtptFrmTimeStamp = m_NtwkMediaPocsThrdPt.m_TmpHTInt2Pt.m_Val;
                        p_AdoOtptFrmPt = m_NtwkMediaPocsThrdPt.m_TmpByte2Pt;
                        p_AdoOtptFrmLen = m_NtwkMediaPocsThrdPt.m_TmpHTLong2Pt.m_Val;

                        if( p_AdoOtptFrmLen > 0 ) //如果音频输出帧为有语音活动。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个有语音活动的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，长度：" + p_AdoOtptFrmLen + "。" );
                        }
                        else if( p_AdoOtptFrmLen == 0 ) //如果音频输出帧为无语音活动。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个无语音活动的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，长度：" + p_AdoOtptFrmLen + "。" );
                        }
                        else //如果音频输出帧为丢失。
                        {
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从音频自适应抖动缓冲器取出一个丢失的音频输出帧。音频输出帧时间戳：" + p_AdoOtptFrmTimeStamp + "，长度：" + p_AdoOtptFrmLen + "。" );
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
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出帧的数据长度不等于Pcm格式的数据长度。音频输出帧：" + ( p_AdoOtptFrmLen ) + "，Pcm格式：" + ( AdoOtptPcmSrcFrmPt.length * 2 ) + "。" );
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
                            if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "音频输出帧的数据长度已超过已编码格式的数据长度。音频输出帧：" + ( p_AdoOtptFrmLen ) + "，已编码格式：" + AdoOtptEncdSrcFrmPt.length + "。" );
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

    //用户定义的写入视频输出帧函数。
    public void UserWriteVdoOtptFrm( int VdoOtptStrmIdx,
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
                if( !m_RecvVdoOtptFrmCntnrPt.isEmpty() ) //如果接收视频输出帧容器不为空。
                {
                    p_VdoOtptFrmPt = m_RecvVdoOtptFrmCntnrPt.poll(); //从接收视频输出帧容器中取出并删除第一个帧。
                    p_VdoOtptFrmLen = p_VdoOtptFrmPt.length;
                }

                if( p_VdoOtptFrmLen != 0 ) //如果视频输出帧为有图像活动。
                {
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从接收视频输出帧容器取出一个有图像活动的视频输出帧。数据长度：" + p_VdoOtptFrmLen + "。" );
                }
                else //如果视频输出帧为无图像活动。
                {
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从接收视频输出帧容器取出一个无图像活动的视频输出帧。数据长度：" + p_VdoOtptFrmLen + "。" );
                }
                break;
            }
            case 1: //如果要使用自适应抖动缓冲器。
            {
                m_VAjbPt.m_Pt.GetBufFrmCnt( m_NtwkMediaPocsThrdPt.m_CurHaveBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MinNeedBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_MaxNeedBufFrmCntPt, m_NtwkMediaPocsThrdPt.m_CurNeedBufFrmCntPt, 1, null );

                if( m_NtwkMediaPocsThrdPt.m_CurHaveBufFrmCntPt.m_Val != 0 ) //如果视频自适应抖动缓冲器不为空。
                {
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "视频自适应抖动缓冲器：帧：" + m_NtwkMediaPocsThrdPt.m_CurHaveBufFrmCntPt.m_Val + "，最小需帧：" + m_NtwkMediaPocsThrdPt.m_MinNeedBufFrmCntPt.m_Val + "，最大需帧：" + m_NtwkMediaPocsThrdPt.m_MaxNeedBufFrmCntPt.m_Val + "，当前需帧：" + m_NtwkMediaPocsThrdPt.m_CurNeedBufFrmCntPt.m_Val + "。" );

                    int p_VdoOtptFrmTimeStamp;

                    //从视频自适应抖动缓冲器取出视频输出帧。
                    m_VAjbPt.m_Pt.GetByteFrm( SystemClock.uptimeMillis(), m_NtwkMediaPocsThrdPt.m_TmpHTInt3Pt, m_NtwkMediaPocsThrdPt.m_TmpByte3Pt, 0, m_NtwkMediaPocsThrdPt.m_TmpByte3Pt.length, m_NtwkMediaPocsThrdPt.m_TmpHTLong3Pt, 1, null );
                    p_VdoOtptFrmTimeStamp = m_NtwkMediaPocsThrdPt.m_TmpHTInt3Pt.m_Val;
                    p_VdoOtptFrmPt = m_NtwkMediaPocsThrdPt.m_TmpByte3Pt;
                    p_VdoOtptFrmLen = m_NtwkMediaPocsThrdPt.m_TmpHTLong3Pt.m_Val;

                    if( p_VdoOtptFrmLen > 0 ) //如果视频输出帧为有图像活动。
                    {
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从视频自适应抖动缓冲器取出一个有图像活动的视频输出帧。时间戳：" + p_VdoOtptFrmTimeStamp + "，数据长度：" + p_VdoOtptFrmLen + "。" );
                    }
                    else //如果视频输出帧为无图像活动。
                    {
                        if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, "从视频自适应抖动缓冲器取出一个无图像活动的视频输出帧。时间戳：" + p_VdoOtptFrmTimeStamp + "，数据长度：" + p_VdoOtptFrmLen + "。" );
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
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度不等于Yu12格式的数据长度。视频输出帧：" + ( p_VdoOtptFrmLen - 4 - 4 ) + "，Yu12格式：" + ( VdoOtptYu12SrcFrmWidthPt.m_Val * VdoOtptYu12SrcFrmHeightPt.m_Val * 3 / 2 ) + "。" );
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
                    if( m_NtwkMediaPocsThrdPt.m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, "视频输出帧的数据长度已超过已编码格式的数据长度。视频输出帧：" + p_VdoOtptFrmLen + "，已编码格式：" + VdoOtptEncdSrcFrmSzByt + "。" );
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
}
