package com.example.andrd_ado_vdo_tkbk_demo;

import HeavenTao.Media.MediaPocsThrd;
import HeavenTao.Media.SrvrThrd;

public class MySrvrThrd extends SrvrThrd
{
    MainAct m_MainActPt; //存放主界面的指针。

    MySrvrThrd( MainAct MainActPt )
    {
        super( MainActPt );

        MediaPocsThrd.m_CtxPt = MainActPt;

        m_MainActPt = MainActPt;
    }

    //用户定义的服务端线程初始化函数。
    @Override public void UserSrvrThrdInit()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MySrvrThrdInit );
    }

    //用户定义的服务端线程销毁函数。
    @Override public void UserSrvrThrdDstoy()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MySrvrThrdDstoy );
    }

    //用户定义的显示日志函数。
    @Override public void UserShowLog( String InfoStrPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowLog, InfoStrPt );
    }

    //用户定义的显示Toast函数。
    @Override public void UserShowToast( String InfoStrPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.ShowToast, InfoStrPt );
    }

    //用户定义的振动函数。
    @Override public void UserVibrate()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.Vibrate );
    }

    //用户定义的消息函数。
    @Override public int UserMsg( int MsgTyp, Object[] MsgArgPt )
    {
        return 0;
    }

    //用户定义的服务端初始化函数。
    @Override public void UserSrvrInit()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.SrvrInit );
    }

    //用户定义的服务端销毁函数。
    @Override public void UserSrvrDstoy()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.SrvrDstoy );
    }

    //用户定义的连接初始化函数。
    @Override public void UserCnctInit( CnctInfo CnctInfoPt, int IsTcpOrAudpPrtcl, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstAddItem, CnctInfoPt.m_Num, IsTcpOrAudpPrtcl, RmtNodeNameStrPt, RmtNodeSrvcStrPt );
    }

    //用户定义的连接销毁函数。
    @Override public void UserCnctDstoy( CnctInfo CnctInfoPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstDelItem, CnctInfoPt.m_Num );
    }

    //用户定义的连接状态函数。
    @Override public void UserCnctSts( CnctInfo CnctInfoPt, int CurCnctSts )
    {
        if( CurCnctSts == SrvrThrd.CnctSts.Wait )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "等待远端接受连接", "" );
        }
        else if( CurCnctSts < SrvrThrd.CnctSts.Wait )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "第" + -CurCnctSts + "次连接", "" );
        }
        else if( CurCnctSts == SrvrThrd.CnctSts.Cnct )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "已连接", "" );
        }
        else if( CurCnctSts == SrvrThrd.CnctSts.Tmot )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "异常断开", "" );
        }
        else if( CurCnctSts == SrvrThrd.CnctSts.Dsct )
        {
            m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, "已断开", "" );
        }
    }

    //用户定义的连接远端对讲模式函数。
    @Override public void UserCnctRmtTkbkMode( CnctInfo CnctInfoPt, int RmtTkbkMode )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstModifyItem, CnctInfoPt.m_Num, null, m_TkbkModeStrArrPt[ RmtTkbkMode ] );
    }
}
