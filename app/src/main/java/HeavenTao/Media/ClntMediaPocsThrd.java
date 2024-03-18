package HeavenTao.Media;

import android.content.Context;
import android.util.Log;

import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

public abstract class ClntMediaPocsThrd extends MediaPocsThrd //客户端媒体处理线程。
{
    public static String m_CurClsNameStrPt = "ClntMediaPocsThrd"; //存放当前类名称字符串。

    public class PktTyp //数据包类型。
    {
        public static final int TkbkIdx = 0; //对讲索引包。
        public static final int TkbkMode = 1; //对讲模式包。
        public static final int AdoFrm = 2; //音频输入输出帧包。
        public static final int VdoFrm = 3; //视频输入输出帧包。
        public static final int Exit = 4; //退出包。
    }

    public class TkbkMode //对讲模式。
    {
        public static final int None = 0; //挂起。
        public static final int AdoInpt = 1; //音频输入。
        public static final int AdoOtpt = 2; //音频输出。
        public static final int VdoInpt = 4; //视频输入。
        public static final int VdoOtpt = 8; //视频输出。
        public static final int Ado = AdoInpt | AdoOtpt; //音频。
        public static final int Vdo = VdoInpt | VdoOtpt; //视频。
        public static final int AdoVdo = Ado | Vdo; //音视频。
        public static final int NoChg = VdoOtpt << 1; //不变。
    }
    public static String m_TkbkModeStrArrPt[] = {
            "挂起", //0：挂起。
            "音入", //1：音频输入。
            "音出", //2：音频输出。
            "音入出", //3：音频输入、音频输出。
            "视入", //4：视频输入。
            "音入视入", //5：音频输入、视频输入。
            "音出视入", //6：音频输出、视频输入。
            "音入出视入", //7：音频输入、音频输出、视频输入。
            "视出", //8：视频输出。
            "音入视出", //9：音频输入、视频输出。
            "音出视出", //10：音频输出、视频输出。
            "音入出视出", //11：音频输入、音频输出、视频输出。
            "视入出", //12：视频输入、视频输出。
            "音入视入出", //13：音频输入、视频输入、视频输出。
            "音出视入出", //14：音频输出、视频输入、视频输出。
            "音入出视入出", //15：音频输入、音频输出、视频输入、视频输出。
            "不变", //16：不变。
    };

    public class CnctSts //连接状态。
    {
        public static final int Wait = 0; //等待远端接受连接。
        public static final int Cnct = 1; //已连接。
        public static final int Tmot = 2; //超时未接收任何数据包。异常断开。
        public static final int Dsct = 3; //已断开。
    }

    public TkbkClnt m_TkbkClntPt = new TkbkClnt();
    public BdctClnt m_BdctClntPt = new BdctClnt();

    public int m_MaxCnctTimes; //存放最大连接次数，取值区间为[1,2147483647]。
    public int m_IsReferRmtTkbkModeSetTkbkMode; //存放是否参考远端对讲模式来设置对讲模式，为1表示要参考，为0表示不参考。
    public int m_IsAutoRqirExit; //存放是否自动请求退出，为0表示手动，为1表示在对讲客户端的连接销毁且广播客户端销毁时自动请求退出。
    public AudpSokt m_AudpClntSoktPt; //存放本端高级Udp协议客户端套接字的指针。

    class Thrd //线程。
    {
        HTString m_LclNodeAddrPt = new HTString(); //存放本端节点名称字符串的指针。
        HTString m_LclNodePortPt = new HTString(); //存放本端节点端口字符串的指针。
        HTString m_RmtNodeAddrPt = new HTString(); //存放远端节点名称字符串的指针。
        HTString m_RmtNodePortPt = new HTString(); //存放远端节点端口字符串的指针。

        HTInt m_CurHaveBufActFrmCntPt = new HTInt(); //存放当前已缓冲有活动帧的数量。
        HTInt m_CurHaveBufInactFrmCntPt = new HTInt(); //存放当前已缓冲无活动帧的数量。
        HTInt m_CurHaveBufFrmCntPt = new HTInt(); //存放当前已缓冲帧的数量。
        HTInt m_MinNeedBufFrmCntPt = new HTInt(); //存放最小需缓冲帧的数量。
        HTInt m_MaxNeedBufFrmCntPt = new HTInt(); //存放最大需缓冲帧的数量。
        HTInt m_MaxCntuLostFrmCntPt = new HTInt(); //存放最大连续丢失帧的数量。
        HTInt m_CurNeedBufFrmCntPt = new HTInt(); //存放当前需缓冲帧的数量。

        byte m_TmpBytePt[] = new byte[ 1024 * 1024 ]; //存放临时数据。
        HTInt m_TmpHTIntPt = new HTInt(); //存放临时数据。
        HTLong m_TmpHTLongPt = new HTLong(); //存放临时数据。
    }
    Thrd m_ThrdPt = new Thrd(); //存放线程。

    public class UserMsgTyp //用户消息。
    {
        public static final int TkbkClntCnctInit         = 0; //对讲客户端的连接初始化。
        public static final int TkbkClntCnctDstoy        = 1; //对讲客户端的连接销毁。
        public static final int TkbkClntLclTkbkMode      = 2; //对讲客户端的本端对讲模式。
        public static final int TkbkClntPttBtnDown       = 3; //对讲客户端的一键即按即通按钮按下。
        public static final int TkbkClntPttBtnUp         = 4; //对讲客户端的一键即按即通按钮弹起。

        public static final int BdctClntInit             = 5; //广播客户端初始化。
        public static final int BdctClntDstoy            = 6; //广播客户端销毁。
        public static final int BdctClntCnctInit         = 7; //广播客户端的连接初始化。
        public static final int BdctClntCnctDstoy        = 8; //广播客户端的连接销毁。
    }

    //用户定义的相关回调函数。

    //用户定义的显示日志函数。
    public abstract void UserShowLog( String InfoStrPt );

    //用户定义的显示Toast函数。
    public abstract void UserShowToast( String InfoStrPt );

    //用户定义的振动函数。
    public abstract void UserVibrate();

    //用户定义的客户端媒体处理线程初始化函数。
    public abstract void UserClntMediaPocsThrdInit();

    //用户定义的客户端媒体处理线程销毁函数。
    public abstract void UserClntMediaPocsThrdDstoy();

    //用户定义的对讲客户端连接初始化函数。
    public abstract void UserTkbkClntCnctInit( int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt );

    //用户定义的对讲客户端连接销毁函数。
    public abstract void UserTkbkClntCnctDstoy();

    //用户定义的对讲客户端连接状态函数。
    public abstract void UserTkbkClntCnctSts( int CurCnctSts );

    //用户定义的对讲客户端我的对讲索引函数。
    public abstract void UserTkbkClntMyTkbkIdx( int MyTkbkIdx );

    //用户定义的对讲客户端本端对讲模式函数。
    public abstract void UserTkbkClntLclTkbkMode( int OldLclTkbkMode, int NewLclTkbkMode );

    //用户定义的对讲客户端对讲信息初始化函数。
    public abstract void UserTkbkClntTkbkInfoInit( TkbkClnt.TkbkInfo TkbkInfoPt );

    //用户定义的对讲客户端对讲信息销毁函数。
    public abstract void UserTkbkClntTkbkInfoDstoy( TkbkClnt.TkbkInfo TkbkInfoPt );

    //用户定义的对讲客户端对讲信息远端对讲模式函数。
    public abstract void UserTkbkClntTkbkInfoRmtTkbkMode( TkbkClnt.TkbkInfo TkbkInfoPt, int OldRmtTkbkMode, int NewRmtTkbkMode );

    //用户定义的广播客户端初始化函数。
    public abstract void UserBdctClntInit();

    //用户定义的广播客户端销毁函数。
    public abstract void UserBdctClntDstoy();

    //构造函数。
    public ClntMediaPocsThrd( Context CtxPt )
    {
        super( CtxPt );

        //初始化对讲网络。
        m_TkbkClntPt.m_ClntMediaPocsThrdPt = this; //设置网络媒体处理线程的指针。
        m_TkbkClntPt.m_TkbkInfoCurMaxNum = -1; //设置对讲信息的当前最大序号。
        m_TkbkClntPt.m_LclTkbkMode = TkbkMode.None; //设置本端对讲模式为挂起。

        //初始化广播网络。
        m_BdctClntPt.m_ClntMediaPocsThrdPt = this; //设置客户端媒体处理线程的指针。
        m_BdctClntPt.m_CnctInfoCurMaxNum = -1; //设置连接信息的当前最大序号。
        m_BdctClntPt.m_LastSendAdoInptFrmIsAct = 0; //设置最后发送的一个音频输入帧为无语音活动。
        m_BdctClntPt.m_LastSendAdoInptFrmTimeStamp = 0 - 1; //设置最后一个发送音频输入帧的时间戳为0的前一个，因为第一次发送音频输入帧时会递增一个步进。
    }

    //发送对讲客户端的连接初始化消息。
    public int SendTkbkClntCnctInitMsg( int IsBlockWait, int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
    {
        return SendUserMsg( IsBlockWait, UserMsgTyp.TkbkClntCnctInit, IsTcpOrAudpPrtcl, RmtNodeNameStrPt, RmtNodeSrvcStrPt );
    }

    //发送对讲客户端的连接销毁消息。
    public int SendTkbkClntCnctDstoyMsg( int IsBlockWait )
    {
        return SendUserMsg( IsBlockWait, UserMsgTyp.TkbkClntCnctDstoy );
    }

    //发送对讲客户端的本端对讲模式消息。
    public int SendTkbkClntLclTkbkModeMsg( int IsBlockWait, int LclTkbkMode )
    {
        return SendUserMsg( IsBlockWait, UserMsgTyp.TkbkClntLclTkbkMode, LclTkbkMode );
    }

    //发送对讲客户端的一键即按即通按钮按下消息。
    public int SendTkbkClntPttBtnDownMsg( int IsBlockWait )
    {
        return SendUserMsg( IsBlockWait, UserMsgTyp.TkbkClntPttBtnDown );
    }

    //发送对讲客户端的一键即按即通按钮弹起消息。
    public int SendTkbkClntPttBtnUpMsg( int IsBlockWait )
    {
        return SendUserMsg( IsBlockWait, UserMsgTyp.TkbkClntPttBtnUp );
    }

    //发送广播客户端初始化消息。
    public int SendBdctClntInitMsg( int IsBlockWait )
    {
        return SendUserMsg( IsBlockWait, UserMsgTyp.BdctClntInit );
    }

    //发送广播客户端销毁消息。
    public int SendBdctClntDstoyMsg( int IsBlockWait )
    {
        return SendUserMsg( IsBlockWait, UserMsgTyp.BdctClntDstoy );
    }

    //发送广播客户端的连接初始化消息。
    public int SendBdctClntCnctInitMsg( int IsBlockWait, int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
    {
        return SendUserMsg( IsBlockWait, UserMsgTyp.BdctClntCnctInit, IsTcpOrAudpPrtcl, RmtNodeNameStrPt, RmtNodeSrvcStrPt );
    }

    //发送广播客户端的连接销毁消息。
    public int SendBdctClntCnctDstoyMsg( int IsBlockWait, int CnctNum )
    {
        return SendUserMsg( IsBlockWait, UserMsgTyp.BdctClntCnctDstoy, CnctNum );
    }

    //判断是否自动请求退出。
    public void IsAutoRqirExit()
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            if( m_IsAutoRqirExit == 0 )
            {

            }
            else if( m_IsAutoRqirExit == 1 )
            {
                if( ( m_TkbkClntPt.m_CnctIsInit == 0 ) && ( m_BdctClntPt.m_IsInit == 0 ) )
                {
                    RqirExit( 0, 1 );

                    String p_InfoStrPt = "客户端媒体处理线程：所有连接已销毁，自动请求退出。";
                    if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                    UserShowLog( p_InfoStrPt );
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return;
    }

    //设置对讲模式。
    public void SetTkbkMode( int IsBlockWait, int SetMode )
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

        int p_RealTkbkMode; //存放实际对讲模式。
        int p_IsUseAdoInpt = 0;
        int p_IsUseAdoOtpt = 0;
        int p_IsUseVdoInpt = 0;
        int p_IsUseVdoOtpt = 0;

        Out:
        {
            //设置实际对讲模式。
            p_RealTkbkMode = TkbkMode.None; //设置实际对讲模式为挂起。
            if( m_TkbkClntPt.m_CurCnctSts == CnctSts.Cnct ) //如果对讲客户端已连接。
            {
                if( m_IsReferRmtTkbkModeSetTkbkMode != 0 ) //如果要参考远端对讲模式来设置对讲模式。
                {
                    TkbkClnt.TkbkInfo p_TkbkClntTkbkInfoTmpPt;
                    int p_TmpTkbkMode = 0; //存放临时对讲模式。

                    //综合全部的远端对讲模式。
                    for( int p_TkbkInfoIdx = 0; p_TkbkInfoIdx < m_TkbkClntPt.m_TkbkInfoCntnrPt.size(); p_TkbkInfoIdx++ )
                    {
                        p_TkbkClntTkbkInfoTmpPt = m_TkbkClntPt.m_TkbkInfoCntnrPt.get( p_TkbkInfoIdx );

                        if( p_TkbkClntTkbkInfoTmpPt.m_IsInit != 0 )
                        {
                            p_TmpTkbkMode |= p_TkbkClntTkbkInfoTmpPt.m_RmtTkbkMode;
                        }
                    }

                    if( ( ( m_TkbkClntPt.m_LclTkbkMode & TkbkMode.AdoInpt ) != 0 ) && ( ( p_TmpTkbkMode & TkbkMode.AdoOtpt ) != 0 ) )
                    {
                        p_RealTkbkMode |= TkbkMode.AdoInpt;
                    }
                    if( ( ( m_TkbkClntPt.m_LclTkbkMode & TkbkMode.AdoOtpt ) != 0 ) && ( ( p_TmpTkbkMode & TkbkMode.AdoInpt ) != 0 ) )
                    {
                        p_RealTkbkMode |= TkbkMode.AdoOtpt;
                    }
                    if( ( ( m_TkbkClntPt.m_LclTkbkMode & TkbkMode.VdoInpt ) != 0 ) && ( ( p_TmpTkbkMode & TkbkMode.VdoOtpt ) != 0 ) )
                    {
                        p_RealTkbkMode |= TkbkMode.VdoInpt;
                    }
                    if( ( ( m_TkbkClntPt.m_LclTkbkMode & TkbkMode.VdoOtpt ) != 0 ) && ( ( p_TmpTkbkMode & TkbkMode.VdoInpt ) != 0 ) )
                    {
                        p_RealTkbkMode |= TkbkMode.VdoOtpt;
                    }
                }
                else //如果不参考远端对讲模式来设置对讲模式。
                {
                    p_RealTkbkMode = m_TkbkClntPt.m_LclTkbkMode;
                }
            }

            if( m_TkbkClntPt.m_XfrMode == 0 ) //如果传输模式为实时半双工（一键通）。
            {
                if( m_TkbkClntPt.m_PttBtnIsDown != 0 ) //如果一键即按即通按钮为按下。
                {
                    if( ( p_RealTkbkMode & TkbkMode.AdoInpt ) != 0 ) p_IsUseAdoInpt = 1;
                    if( ( p_RealTkbkMode & TkbkMode.VdoInpt ) != 0 ) p_IsUseVdoInpt = 1;
                }
                else //如果一键即按即通按钮为弹起。
                {
                    if( ( p_RealTkbkMode & TkbkMode.AdoOtpt ) != 0 ) p_IsUseAdoOtpt = 1;
                    if( ( p_RealTkbkMode & TkbkMode.VdoOtpt ) != 0 ) p_IsUseVdoOtpt = 1;
                }
            }
            else //如果传输模式为实时全双工。
            {
                if( ( p_RealTkbkMode & TkbkMode.AdoInpt ) != 0 ) p_IsUseAdoInpt = 1;
                if( ( p_RealTkbkMode & TkbkMode.AdoOtpt ) != 0 ) p_IsUseAdoOtpt = 1;
                if( ( p_RealTkbkMode & TkbkMode.VdoInpt ) != 0 ) p_IsUseVdoInpt = 1;
                if( ( p_RealTkbkMode & TkbkMode.VdoOtpt ) != 0 ) p_IsUseVdoOtpt = 1;
            }

            if( m_BdctClntPt.m_IsInit != 0 ) p_IsUseAdoInpt = 1; //如果广播客户端已初始化，就要使用音频输入。

            //设置是否使用音视频输入输出。
            if( SetMode == 0 ) //如果同时设置不使用和要使用。
            {
                SetIsUseAdoVdoInptOtpt( IsBlockWait, p_IsUseAdoInpt, p_IsUseAdoOtpt, p_IsUseVdoInpt, p_IsUseVdoOtpt );
            }
            else if( SetMode == 1 ) //如果只设置不使用。
            {
                SetIsUseAdoVdoInptOtpt( IsBlockWait, ( p_IsUseAdoInpt == 0 ) ? 0 : -1, ( p_IsUseAdoOtpt == 0 ) ? 0 : -1, ( p_IsUseVdoInpt == 0 ) ? 0 : -1, ( p_IsUseVdoOtpt == 0 ) ? 0 : -1 );
            }
            else if( SetMode == 2 ) //如果只设置要使用。
            {
                SetIsUseAdoVdoInptOtpt( IsBlockWait, ( p_IsUseAdoInpt != 0 ) ? 1 : -1, ( p_IsUseAdoOtpt != 0 ) ? 1 : -1, ( p_IsUseVdoInpt != 0 ) ? 1 : -1, ( p_IsUseVdoOtpt != 0 ) ? 1 : -1 );
            }

            //if( m_TkbkClntPt.m_XfrMode == 0 ) m_TkbkClntPt.RecvOtptFrmReset(); //接收输出帧重置。防止在实时半双工（一键通）模式下每次按下PTT时还有点点播放上次按下的声音。

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return;
    }

    //用户定义的初始化函数。
    @Override public int UserInit()
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            UserClntMediaPocsThrdInit(); //客户端媒体处理线程初始化。

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return p_Rslt;
    }

    //用户定义的处理函数。
    @Override public int UserPocs()
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            m_TkbkClntPt.CnctPocs();
            m_BdctClntPt.CnctPocs();

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return p_Rslt;
    }

    //用户定义的销毁函数。
    @Override public void UserDstoy()
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            m_TkbkClntPt.CnctInfoDstoy(); //对讲客户端的连接销毁。
            m_BdctClntPt.Dstoy(); //广播客户端销毁。

            //销毁本端高级Udp协议客户端套接字。
            if( m_AudpClntSoktPt != null )
            {
                m_AudpClntSoktPt.Dstoy( null ); //关闭并销毁本端高级Udp协议客户端套接字。
                m_AudpClntSoktPt = null;

                String p_InfoStrPt = "客户端媒体处理线程：关闭并销毁本端高级Udp协议客户端套接字成功。";
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                UserShowLog( p_InfoStrPt );
            }

            UserClntMediaPocsThrdDstoy(); //调用用户定义的客户端媒体处理线程销毁函数。
            UserVibrate(); //调用用户定义的振动函数。

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

        }
        return;
    }

    //用户定义的消息函数。
    @Override public int UserMsg( int MsgTyp, Object MsgArgPt[] )
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            switch( MsgTyp )
            {
                case UserMsgTyp.TkbkClntCnctInit:
                {
                    int p_IsTcpOrAudpPrtcl = ( int ) MsgArgPt[ 0 ];
                    String p_RmtNodeNameStrPt = ( String ) MsgArgPt[ 1 ];
                    String p_RmtNodeSrvcStrPt = ( String ) MsgArgPt[ 2 ];

                    OutTkbkClntCnctInit:
                    {
                        if( m_TkbkClntPt.m_CnctIsInit != 0 )
                        {
                            String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：已初始化与远端节点" + ( ( m_TkbkClntPt.m_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + m_TkbkClntPt.m_RmtNodeNameStrPt + ":" + m_TkbkClntPt.m_RmtNodeSrvcStrPt + "]的服务端连接，请先销毁再初始化。";
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            UserShowLog( p_InfoStrPt );
                            break OutTkbkClntCnctInit;
                        }

                        //Ping一下远程节点名称，这样可以快速获取ARP条目。
                        try
                        {
                            Runtime.getRuntime().exec( "ping -c 1 -w 1 " + p_RmtNodeNameStrPt );
                        }
                        catch( Exception ignored )
                        {
                        }

                        if( m_TkbkClntPt.CnctInfoInit( p_IsTcpOrAudpPrtcl, p_RmtNodeNameStrPt, p_RmtNodeSrvcStrPt, null, -1, CnctSts.Wait ) != 0 ) break Out;

                        String p_InfoStrPt = "客户端媒体处理线程：对讲客户端：初始化与远端节点" + ( ( p_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + p_RmtNodeNameStrPt + ":" + p_RmtNodeSrvcStrPt + "]的连接成功。";
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        UserShowLog( p_InfoStrPt );

                        UserTkbkClntCnctInit( p_IsTcpOrAudpPrtcl, p_RmtNodeNameStrPt, p_RmtNodeSrvcStrPt ); //用户定义的对讲客户端连接初始化函数。
                    }
                    break;
                }
                case UserMsgTyp.TkbkClntCnctDstoy:
                {
                    m_TkbkClntPt.CnctInfoDstoy();
                    break;
                }
                case UserMsgTyp.TkbkClntLclTkbkMode:
                {
                    int p_OldLclTkbkMode = m_TkbkClntPt.m_LclTkbkMode; //设置旧本端对讲模式。
                    if( ( Integer ) MsgArgPt[ 0 ] != TkbkMode.NoChg ) m_TkbkClntPt.m_LclTkbkMode = ( Integer ) MsgArgPt[ 0 ]; //设置本端对讲模式。
                    SetTkbkMode( 1, 1 ); //只设置不使用的对讲模式。
                    UserTkbkClntLclTkbkMode( p_OldLclTkbkMode, m_TkbkClntPt.m_LclTkbkMode ); //调用用户定义的对讲客户端本端对讲模式函数。
                    if( m_TkbkClntPt.m_CurCnctSts == CnctSts.Cnct ) m_TkbkClntPt.CnctSendTkbkModePkt( m_TkbkClntPt.m_LclTkbkMode ); //发送对讲模式包。
                    SetTkbkMode( 1, 2 ); //只设置要使用的对讲模式。
                    break;
                }
                case UserMsgTyp.TkbkClntPttBtnDown:
                {
                    m_TkbkClntPt.m_PttBtnIsDown = 1; //设置一键即按即通按钮为按下。
                    SetTkbkMode( 1, 0 ); //设置对讲模式。
                    UserVibrate(); //调用用户定义的振动函数。
                    break;
                }
                case UserMsgTyp.TkbkClntPttBtnUp:
                {
                    m_TkbkClntPt.m_PttBtnIsDown = 0; //设置一键即按即通按钮为弹起。
                    SetTkbkMode( 1, 0 ); //设置对讲模式。
                    break;
                }
                case UserMsgTyp.BdctClntInit:
                {
                    m_BdctClntPt.Init();
                    SetTkbkMode( 1, 0 ); //设置对讲模式。
                    break;
                }
                case UserMsgTyp.BdctClntDstoy:
                {
                    m_BdctClntPt.Dstoy();
                    SetTkbkMode( 1, 0 ); //设置对讲模式。
                    break;
                }
                case UserMsgTyp.BdctClntCnctInit:
                {
                    int p_IsTcpOrAudpPrtcl = ( int ) MsgArgPt[ 0 ];
                    String p_RmtNodeNameStrPt = ( String ) MsgArgPt[ 1 ];
                    String p_RmtNodeSrvcStrPt = ( String ) MsgArgPt[ 2 ];
                    BdctClnt.CnctInfo p_BdctClntCnctInfoTmpPt;

                    OutBdctClntCnctInit:
                    {
                        for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_BdctClntPt.m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
                        {
                            p_BdctClntCnctInfoTmpPt = m_BdctClntPt.m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );
                            if( ( p_BdctClntCnctInfoTmpPt.m_IsInit != 0 ) &&
                                    ( p_BdctClntCnctInfoTmpPt.m_IsTcpOrAudpPrtcl == p_IsTcpOrAudpPrtcl ) &&
                                    ( p_BdctClntCnctInfoTmpPt.m_RmtNodeNameStrPt.equals( p_RmtNodeNameStrPt ) ) &&
                                    ( p_BdctClntCnctInfoTmpPt.m_RmtNodeSrvcStrPt.equals( p_RmtNodeSrvcStrPt ) ) )
                            {
                                String p_InfoStrPt = "客户端媒体处理线程：广播客户端：已存在与远端节点" + ( ( p_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + p_RmtNodeNameStrPt + ":" + p_RmtNodeSrvcStrPt + "]的连接，无需重复连接。";
                                if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                                UserShowLog( p_InfoStrPt );
                                break OutBdctClntCnctInit;
                            }
                        }

                        //Ping一下远程节点名称，这样可以快速获取ARP条目。
                        try
                        {
                            Runtime.getRuntime().exec( "ping -c 1 -w 1 " + p_RmtNodeNameStrPt );
                        }
                        catch( Exception ignored )
                        {
                        }

                        if( ( p_BdctClntCnctInfoTmpPt = m_BdctClntPt.CnctInfoInit( p_IsTcpOrAudpPrtcl, p_RmtNodeNameStrPt, p_RmtNodeSrvcStrPt, null, -1, CnctSts.Wait ) ) == null ) break Out;

                        String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_BdctClntCnctInfoTmpPt.m_Idx + "：初始化与远端节点" + ( ( p_BdctClntCnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + p_BdctClntCnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_BdctClntCnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]的连接成功。";
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        UserShowLog( p_InfoStrPt );
                    }
                    break;
                }
                case UserMsgTyp.BdctClntCnctDstoy:
                {
                    int p_CnctNum = ( int ) MsgArgPt[ 0 ];
                    BdctClnt.CnctInfo p_BdctClntCnctInfoTmpPt = null;

                    OutBdctClntCnctDstoy:
                    {
                        if( ( p_CnctNum > m_BdctClntPt.m_CnctInfoCurMaxNum ) || ( p_CnctNum < 0 ) )
                        {
                            String p_InfoStrPt = "客户端媒体处理线程：广播客户端：没有序号为" + p_CnctNum + "]的连接，无法删除。";
                            if( m_IsPrintLogcat != 0 ) Log.e( m_CurClsNameStrPt, p_InfoStrPt );
                            UserShowLog( p_InfoStrPt );
                            break OutBdctClntCnctDstoy;
                        }

                        for( int p_CnctInfoLstIdx = 0; p_CnctInfoLstIdx < m_BdctClntPt.m_CnctInfoCntnrPt.size(); p_CnctInfoLstIdx++ )
                        {
                            p_BdctClntCnctInfoTmpPt = m_BdctClntPt.m_CnctInfoCntnrPt.get( p_CnctInfoLstIdx );

                            if( ( p_BdctClntCnctInfoTmpPt.m_IsInit != 0 ) && ( p_BdctClntCnctInfoTmpPt.m_Num == p_CnctNum ) )
                            {
                                p_BdctClntCnctInfoTmpPt.m_IsRqstDstoy = 1; //设置已请求销毁。
                                break;
                            }
                        }

                        String p_InfoStrPt = "客户端媒体处理线程：广播客户端：连接" + p_BdctClntCnctInfoTmpPt.m_Idx + "：请求销毁远端节点" + ( ( p_BdctClntCnctInfoTmpPt.m_IsTcpOrAudpPrtcl == 0 ) ? "Tcp协议" : "高级Udp协议" ) + "[" + p_BdctClntCnctInfoTmpPt.m_RmtNodeNameStrPt + ":" + p_BdctClntCnctInfoTmpPt.m_RmtNodeSrvcStrPt + "]的连接。";
                        if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                        UserShowLog( p_InfoStrPt );

                        p_Rslt = 0; //设置本函数执行成功。
                    }

                    if( p_Rslt != 0 ) //如果本函数执行失败。
                    {

                    }
                    break;
                }
            }

            p_Rslt = 0; //设置本函数执行成功。
        }

        if( p_Rslt != 0 ) //如果本函数执行失败。
        {

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
        if( m_TkbkClntPt.m_CnctIsInit != 0 )
        {
            m_TkbkClntPt.UserReadAdoVdoInptFrm( AdoInptPcmSrcFrmPt, AdoInptPcmRsltFrmPt, AdoInptPcmFrmLenUnit, AdoInptPcmRsltFrmVoiceActSts,
                                                AdoInptEncdRsltFrmPt, AdoInptEncdRsltFrmLenByt, AdoInptEncdRsltFrmIsNeedTrans,
                                                VdoInptNv21SrcFrmPt, VdoInptNv21SrcFrmWidthPt, VdoInptNv21SrcFrmHeightPt, VdoInptNv21SrcFrmLenByt,
                                                VdoInptYu12RsltFrmPt, VdoInptYu12RsltFrmWidth, VdoInptYu12RsltFrmHeight, VdoInptYu12RsltFrmLenByt,
                                                VdoInptEncdRsltFrmPt, VdoInptEncdRsltFrmLenByt );
        }

        if( ( AdoInptPcmSrcFrmPt != null ) && ( m_BdctClntPt.m_CnctInfoCurMaxNum > -1 ) )
        {
            m_BdctClntPt.UserReadAdoVdoInptFrm( AdoInptPcmSrcFrmPt, AdoInptPcmRsltFrmPt, AdoInptPcmFrmLenUnit, AdoInptPcmRsltFrmVoiceActSts,
                                                AdoInptEncdRsltFrmPt, AdoInptEncdRsltFrmLenByt, AdoInptEncdRsltFrmIsNeedTrans,
                                                VdoInptNv21SrcFrmPt, VdoInptNv21SrcFrmWidthPt, VdoInptNv21SrcFrmHeightPt, VdoInptNv21SrcFrmLenByt,
                                                VdoInptYu12RsltFrmPt, VdoInptYu12RsltFrmWidth, VdoInptYu12RsltFrmHeight, VdoInptYu12RsltFrmLenByt,
                                                VdoInptEncdRsltFrmPt, VdoInptEncdRsltFrmLenByt );
        }
    }

    //用户定义的写入音频输出帧函数。
    @Override public void UserWriteAdoOtptFrm( int AdoOtptStrmIdx,
                                               short AdoOtptPcmSrcFrmPt[], int AdoOtptPcmFrmLenUnit,
                                               byte AdoOtptEncdSrcFrmPt[], long AdoOtptEncdSrcFrmSzByt, HTLong AdoOtptEncdSrcFrmLenBytPt )
    {
        m_TkbkClntPt.UserWriteAdoOtptFrm( AdoOtptStrmIdx,
                                          AdoOtptPcmSrcFrmPt, AdoOtptPcmFrmLenUnit,
                                          AdoOtptEncdSrcFrmPt, AdoOtptEncdSrcFrmSzByt, AdoOtptEncdSrcFrmLenBytPt );
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
        m_TkbkClntPt.UserWriteVdoOtptFrm( VdoOtptStrmIdx,
                                          VdoOtptYu12SrcFrmPt, VdoOtptYu12SrcFrmWidthPt, VdoOtptYu12SrcFrmHeightPt,
                                          VdoOtptEncdSrcFrmPt, VdoOtptEncdSrcFrmSzByt, VdoOtptEncdSrcFrmLenBytPt );
    }

    //用户定义的获取视频输出帧函数。
    @Override public void UserGetVdoOtptFrm( int VdoOtptStrmIdx,
                                             byte VdoOtptYu12SrcFrmPt[], int VdoOtptYu12SrcFrmWidth, int VdoOtptYu12SrcFrmHeight,
                                             byte VdoOtptEncdSrcFrmPt[], long VdoOtptEncdSrcFrmLenByt )
    {

    }
}
