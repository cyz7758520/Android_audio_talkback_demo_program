package com.example.andrd_ado_vdo_tkbk_demo;

import HeavenTao.Media.NtwkMediaPocsThrd;

public class MyNtwkMediaPocsThrd extends NtwkMediaPocsThrd
{
    MainAct m_MainActPt; //存放主界面的指针。

    MyNtwkMediaPocsThrd( MainAct MainActPt )
    {
        super( MainActPt );

        m_MainActPt = MainActPt;
    }

    //用户定义的网络媒体处理线程初始化函数。
    @Override public void UserNtwkMediaPocsThrdInit()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MyNtwkMediaPocsThrdInit );
    }

    //用户定义的网络媒体处理线程销毁函数。
    @Override public void UserNtwkMediaPocsThrdDstoy()
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.MyNtwkMediaPocsThrdDstoy );
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

    //用户定义的连接添加函数。
    @Override public void UserCnctInit( int Idx, String PrtclStrPt, String RmtNodeNameStrPt, String RmtNodeSrvcStrPt, String LclTkbkModeStrPt, String RmtTkbkModeStrPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewAddItem, Idx, PrtclStrPt, RmtNodeNameStrPt, RmtNodeSrvcStrPt, LclTkbkModeStrPt, RmtTkbkModeStrPt );
    }

    //用户定义的连接修改函数。
    @Override public void UserCnctModify( int Idx, String SignStrPt, String LclTkbkModeStrPt, String RmtTkbkModeStrPt )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewModifyItem, Idx, SignStrPt, LclTkbkModeStrPt, RmtTkbkModeStrPt );
    }

    //用户定义的连接销毁函数。
    @Override public void UserCnctDstoy( int Idx, int CnctSts )
    {
        m_MainActPt.SendMainActMsg( MainAct.MainActMsgTyp.CnctLstViewDelItem, Idx );
    }
}
