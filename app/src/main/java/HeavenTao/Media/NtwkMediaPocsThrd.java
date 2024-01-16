package HeavenTao.Media;

import android.content.Context;
import android.util.Log;

import HeavenTao.Media.*;
import HeavenTao.Data.*;
import HeavenTao.Sokt.*;

public abstract class NtwkMediaPocsThrd extends MediaPocsThrd //网络媒体处理线程。
{
    public static String m_CurClsNameStrPt = "NtwkMediaPocsThrd"; //存放当前类名称字符串。

    public int m_IsInterrupt; //存放是否中断，为0表示未中断，为1表示已中断。

    public class PktTyp //数据包类型。
    {
        public static final int TkbkMode = 0; //对讲模式包。
        public static final int AdoFrm = 1; //音频输入输出帧包。
        public static final int VdoFrm = 2; //视频输入输出帧包。
        public static final int Exit = 3; //退出包。
    }

    public class TkbkMode //对讲模式。
    {
        public static final int None = 0; //挂起。
        public static final int Ado = 1; //音频。
        public static final int Vdo = 2; //视频。
        public static final int AdoVdo = Ado | Vdo; //音视频。
        public static final int NoChg = 4; //不变。
    }
    public static String m_TkbkModeStrArrPt[] = { "挂起", "音频", "视频", "音视频", "不变" };

    public class CnctSts //连接状态。
    {
        public static final int Wait = 0; //等待远端接受连接。
        public static final int Cnct = 1; //已连接。
        public static final int Tmot = 2; //超时未接收到任何数据包。异常断开。
        public static final int Dsct = 3; //已断开。
    }

    public TkbkNtwk m_TkbkNtwkPt = new TkbkNtwk();
    public BdctNtwk m_BdctNtwkPt = new BdctNtwk();

    public int m_LclTkbkMode; //存放本端对讲模式。
    public int m_MaxCnctTimes; //存放最大连接次数，取值区间为[1,2147483647]。
    public int m_IsAutoRqirExit; //存放是否自动请求退出，为0表示手动，为1表示在所有连接销毁时自动请求退出，为2表示在所有连接和服务端都销毁时自动请求退出。
    public AudpSokt m_AudpClntSoktPt; //存放本端高级Udp协议客户端套接字的指针。

    //临时变量。
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
    byte m_TmpByte2Pt[] = new byte[ 1024 * 1024 ]; //存放临时数据。
    byte m_TmpByte3Pt[] = new byte[ 1024 * 1024 ]; //存放临时数据。
    HTInt m_TmpHTIntPt = new HTInt(); //存放临时数据。
    HTInt m_TmpHTInt2Pt = new HTInt(); //存放临时数据。
    HTInt m_TmpHTInt3Pt = new HTInt(); //存放临时数据。
    HTLong m_TmpHTLongPt = new HTLong(); //存放临时数据。
    HTLong m_TmpHTLong2Pt = new HTLong(); //存放临时数据。
    HTLong m_TmpHTLong3Pt = new HTLong(); //存放临时数据。

    public class UserMsgTyp //用户消息。
    {
        public static final int SrvrInitOrDstoy = 0; //服务端初始化或销毁。
        public static final int CnctInit = 1; //连接初始化。
        public static final int CnctAct = 2; //连接激活。
        public static final int CnctDstoy = 3; //连接销毁。
        public static final int LclTkbkMode = 4; //本端对讲模式。
        public static final int PttBtnDown = 5; //一键即按即通按钮按下。
        public static final int PttBtnUp = 6; //一键即按即通按钮弹起。
        public static final int BdctCnctInit = 7; //广播连接初始化。
        public static final int BdctCnctAllDstoy = 8; //广播连接全部销毁。
    }

    //用户定义的相关回调函数。

    //用户定义的网络媒体处理线程初始化函数。
    public abstract void UserNtwkMediaPocsThrdInit();

    //用户定义的网络媒体处理线程销毁函数。
    public abstract void UserNtwkMediaPocsThrdDstoy();

    //用户定义的服务端初始化函数。
    public abstract void UserSrvrInit();

    //用户定义的服务端销毁函数。
    public abstract void UserSrvrDstoy();

    //用户定义的显示日志函数。
    public abstract void UserShowLog( String InfoStrPt );

    //用户定义的显示Toast函数。
    public abstract void UserShowToast( String InfoStrPt );

    //用户定义的振动函数。
    public abstract void UserVibrate();

    //用户定义的连接添加函数。
    public abstract void UserCnctInit( TkbkNtwk.CnctInfo CnctInfoPt, String PrtclStrPt, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt );

    //用户定义的连接状态函数。
    public abstract void UserCnctSts( TkbkNtwk.CnctInfo CnctInfoPt, int CurCnctSts );

    //用户定义的连接激活函数。
    public abstract void UserCnctAct( TkbkNtwk.CnctInfo CnctInfoPt, int IsAct );

    //用户定义的连接本端对讲模式函数。
    public abstract void UserCnctLclTkbkMode( TkbkNtwk.CnctInfo CnctInfoPt, int IsRqirAct, int LclTkbkMode );

    //用户定义的连接远端对讲模式函数。
    public abstract void UserCnctRmtTkbkMode( TkbkNtwk.CnctInfo CnctInfoPt, int IsRqirAct, int RmtTkbkMode );

    //用户定义的连接销毁函数。
    public abstract void UserCnctDstoy( TkbkNtwk.CnctInfo CnctInfoPt );

    //构造函数。
    public NtwkMediaPocsThrd( Context CtxPt )
    {
        super( CtxPt );

        m_IsInterrupt = 0; //设置未中断。

        m_TkbkNtwkPt.m_NtwkMediaPocsThrdPt = this; //设置网络媒体处理线程的指针。
        m_BdctNtwkPt.m_NtwkMediaPocsThrdPt = this; //设置网络媒体处理线程的指针。

        m_LclTkbkMode = TkbkMode.None;
    }

    //用户定义的初始化函数。
    @Override public int UserInit()
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            UserNtwkMediaPocsThrdInit(); //网络媒体处理线程初始化。

            m_TkbkNtwkPt.RecvOtptFrmInit(); //接收输出帧初始化。

            p_Rslt = 0; //设置本函数执行成功。
        }

        return p_Rslt;
    }

    //用户定义的处理函数。
    @Override public int UserPocs()
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            m_TkbkNtwkPt.CnctPocs();

            m_BdctNtwkPt.CnctPocs();

            p_Rslt = 0; //设置本函数执行成功。
        }

        return p_Rslt;
    }

    //用户定义的销毁函数。
    @Override public void UserDstoy()
    {
        m_BdctNtwkPt.CnctInfoAllDstoy(); //连接信息全部销毁。

        m_TkbkNtwkPt.CnctInfoAllDstoy(); //连接信息全部销毁。
        m_TkbkNtwkPt.SrvrDstoy(); //服务端销毁。
        m_TkbkNtwkPt.RecvOtptFrmDstoy(); //接收输出帧销毁。

        //销毁本端高级Udp协议客户端套接字。
        if( m_AudpClntSoktPt != null )
        {
            m_AudpClntSoktPt.Dstoy( null ); //关闭并销毁本端高级Udp协议客户端套接字。
            m_AudpClntSoktPt = null;

            String p_InfoStrPt = "网络媒体处理线程：关闭并销毁本端高级Udp协议客户端套接字成功。";
            if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
            UserShowLog( p_InfoStrPt );
        }

        UserNtwkMediaPocsThrdDstoy(); //调用用户定义的网络媒体处理线程销毁函数。
        UserVibrate(); //调用用户定义的振动函数。
    }

    //判断是否自动请求退出。
    public void IsAutoRqirExit()
    {
        if( m_IsAutoRqirExit == 0 )
        {

        }
        else if( m_IsAutoRqirExit == 1 )
        {
            if( ( m_TkbkNtwkPt.m_CnctInfoCntnrPt.isEmpty() ) && ( m_BdctNtwkPt.m_CnctInfoCntnrPt.isEmpty() ) )
            {
                RqirExit( 1, 0 );

                String p_InfoStrPt = "网络媒体处理线程：对讲网络：所有连接已销毁，自动请求退出。";
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                UserShowLog( p_InfoStrPt );
            }
        }
        else if( m_IsAutoRqirExit == 2 )
        {
            if( ( m_TkbkNtwkPt.m_CnctInfoCntnrPt.isEmpty() ) && ( m_BdctNtwkPt.m_CnctInfoCntnrPt.isEmpty() ) && ( m_TkbkNtwkPt.m_SrvrIsInit == 0 ) )
            {
                RqirExit( 1, 0 );

                String p_InfoStrPt = "网络媒体处理线程：对讲网络：所有连接和服务端已销毁，自动请求退出。";
                if( m_IsPrintLogcat != 0 ) Log.i( m_CurClsNameStrPt, p_InfoStrPt );
                UserShowLog( p_InfoStrPt );
            }
        }
    }

    //设置对讲模式。
    public void SetTkbkMode( int IsBlockWait )
    {
        int p_RmtTkbkMode; //存放远端对讲模式。
        int p_RealTkbkMode; //存放实际对讲模式。
        int p_IsUseAdoInpt = 0;
        int p_IsUseAdoOtpt = 0;
        int p_IsUseVdoInpt = 0;
        int p_IsUseVdoOtpt = 0;

        //设置远端对讲模式。
        if( m_TkbkNtwkPt.m_CurActCnctInfoPt != null ) p_RmtTkbkMode = m_TkbkNtwkPt.m_CurActCnctInfoPt.m_RmtTkbkMode;
        else p_RmtTkbkMode = TkbkMode.None;

        //设置实际对讲模式。
        p_RealTkbkMode = m_LclTkbkMode & p_RmtTkbkMode;

        if( m_TkbkNtwkPt.m_XfrMode == 0 ) //如果传输模式为实时半双工（一键通）。
        {
            if( m_TkbkNtwkPt.m_PttBtnIsDown != 0 ) //如果一键即按即通按钮为按下。
            {
                if( ( m_LclTkbkMode & TkbkMode.Ado ) != 0 )
                {
                    p_IsUseAdoInpt = 1;
                }
                if( ( m_LclTkbkMode & TkbkMode.Vdo ) != 0 )
                {
                    p_IsUseVdoInpt = 1;
                }
            }
            else //如果一键即按即通按钮为弹起。
            {
                if( ( p_RealTkbkMode & TkbkMode.Ado ) != 0 )
                {
                    p_IsUseAdoOtpt = 1;
                }
                if( ( p_RealTkbkMode & TkbkMode.Vdo ) != 0 )
                {
                    p_IsUseVdoOtpt = 1;
                }
            }
        }
        else //如果传输模式为实时全双工。
        {
            if( ( p_RealTkbkMode & TkbkMode.Ado ) != 0 )
            {
                p_IsUseAdoInpt = 1;
                p_IsUseAdoOtpt = 1;
            }
            if( ( p_RealTkbkMode & TkbkMode.Vdo ) != 0 )
            {
                p_IsUseVdoInpt = 1;
                p_IsUseVdoOtpt = 1;
            }
        }

        if( !m_BdctNtwkPt.m_CnctInfoCntnrPt.isEmpty() ) //如果有广播连接。
        {
            p_IsUseAdoInpt = 1;
        }

        SetIsUseAdoVdoInptOtpt( IsBlockWait, p_IsUseAdoInpt, p_IsUseAdoOtpt, p_IsUseVdoInpt, p_IsUseVdoOtpt ); //设置是否使用音视频输入输出。

        if( m_TkbkNtwkPt.m_XfrMode == 0 ) m_TkbkNtwkPt.RecvOtptFrmReset(); //接收输出帧重置。防止在实时半双工（一键通）模式下每次按下PTT时还有点点播放上次按下的声音。
    }

    //用户定义的消息函数。
    @Override public int UserMsg( Object MsgArgPt[] )
    {
        int p_Rslt = -1; //存放本函数执行结果，为0表示成功，为非0表示失败。

        Out:
        {
            switch( ( Integer ) MsgArgPt[ 0 ] )
            {
                case UserMsgTyp.SrvrInitOrDstoy:
                {
                    if( m_TkbkNtwkPt.m_SrvrIsInit == 0 ) //如果服务端未初始化。
                    {
                        m_TkbkNtwkPt.SrvrInit( ( String ) MsgArgPt[ 1 ] );
                    }
                    else //如果服务端已初始化。
                    {
                        m_TkbkNtwkPt.SrvrDstoy();
                    }
                    break;
                }
                case UserMsgTyp.CnctInit:
                {
                    m_TkbkNtwkPt.CnctInit( ( int ) MsgArgPt[ 1 ], ( String ) MsgArgPt[ 2 ], ( String ) MsgArgPt[ 3 ] );
                    break;
                }
                case UserMsgTyp.CnctAct:
                {
                    m_TkbkNtwkPt.CnctAct( ( int ) MsgArgPt[ 1 ] );
                    break;
                }
                case UserMsgTyp.CnctDstoy:
                {
                    m_TkbkNtwkPt.CnctDstoy( ( int ) MsgArgPt[ 1 ] );
                    break;
                }
                case UserMsgTyp.LclTkbkMode:
                {
                    if( ( Integer ) MsgArgPt[ 1 ] != TkbkMode.NoChg ) m_LclTkbkMode = ( Integer ) MsgArgPt[ 1 ]; //设置本端对讲模式。
                    if( m_TkbkNtwkPt.m_CurActCnctInfoPt != null ) //如果当前激活的连接信息的指针不为空。
                    {
                        m_TkbkNtwkPt.CnctInfoUpdtLclTkbkMode( m_TkbkNtwkPt.m_CurActCnctInfoPt, m_LclTkbkMode ); //当前激活的连接信息更新本端对讲模式。
                    }
                    else
                    {
                        m_TkbkNtwkPt.CnctInfoAllUpdtLclTkbkMode( m_LclTkbkMode ); //连接信息全部更新本端对讲模式。
                    }
                    SetTkbkMode( 1 ); //设置对讲模式。
                    break;
                }
                case UserMsgTyp.PttBtnDown:
                {
                    m_TkbkNtwkPt.m_PttBtnIsDown = 1; //设置一键即按即通按钮为按下。
                    if( m_TkbkNtwkPt.m_CurActCnctInfoPt != null ) //如果当前激活的连接信息的指针不为空。
                    {
                        m_TkbkNtwkPt.CnctInfoSendTkbkModePkt( m_TkbkNtwkPt.m_CurActCnctInfoPt, m_LclTkbkMode ); //发送对讲模式包。
                        SetTkbkMode( 1 ); //设置对讲模式。
                        UserVibrate(); //调用用户定义的振动函数。
                    }
                    break;
                }
                case UserMsgTyp.PttBtnUp:
                {
                    m_TkbkNtwkPt.m_PttBtnIsDown = 0; //设置一键即按即通按钮为弹起。
                    if( m_TkbkNtwkPt.m_CurActCnctInfoPt != null ) //如果当前激活的连接信息的指针不为空。
                    {
                        m_TkbkNtwkPt.CnctInfoSendTkbkModePkt( m_TkbkNtwkPt.m_CurActCnctInfoPt, TkbkMode.None ); //发送对讲模式包。
                        SetTkbkMode( 1 ); //设置对讲模式。
                    }
                    break;
                }
                case UserMsgTyp.BdctCnctInit:
                {
                    m_BdctNtwkPt.CnctInit( ( int ) MsgArgPt[ 1 ], ( String ) MsgArgPt[ 2 ], ( String ) MsgArgPt[ 3 ] );
                    break;
                }
                case UserMsgTyp.BdctCnctAllDstoy:
                {
                    m_BdctNtwkPt.CnctInfoAllDstoy();
                    SetTkbkMode( 1 ); //设置对讲模式。
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
        if( m_TkbkNtwkPt.m_CurActCnctInfoPt != null )
        {
            m_TkbkNtwkPt.UserReadAdoVdoInptFrm( AdoInptPcmSrcFrmPt, AdoInptPcmRsltFrmPt, AdoInptPcmFrmLenUnit, AdoInptPcmRsltFrmVoiceActSts,
                                                AdoInptEncdRsltFrmPt, AdoInptEncdRsltFrmLenByt, AdoInptEncdRsltFrmIsNeedTrans,
                                                VdoInptNv21SrcFrmPt, VdoInptNv21SrcFrmWidthPt, VdoInptNv21SrcFrmHeightPt, VdoInptNv21SrcFrmLenByt,
                                                VdoInptYu12RsltFrmPt, VdoInptYu12RsltFrmWidth, VdoInptYu12RsltFrmHeight, VdoInptYu12RsltFrmLenByt,
                                                VdoInptEncdRsltFrmPt, VdoInptEncdRsltFrmLenByt );
        }

        if( ( AdoInptPcmSrcFrmPt != null ) && ( !m_BdctNtwkPt.m_CnctInfoCntnrPt.isEmpty() ) )
        {
            m_BdctNtwkPt.UserReadAdoVdoInptFrm( AdoInptPcmSrcFrmPt, AdoInptPcmRsltFrmPt, AdoInptPcmFrmLenUnit, AdoInptPcmRsltFrmVoiceActSts,
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
        m_TkbkNtwkPt.UserWriteAdoOtptFrm( AdoOtptStrmIdx,
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
        m_TkbkNtwkPt.UserWriteVdoOtptFrm( VdoOtptStrmIdx,
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