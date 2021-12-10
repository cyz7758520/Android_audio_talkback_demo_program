package HeavenTao.Ado;

import android.view.Surface;

import HeavenTao.Data.*;

//音频波形器类。
public class AdoWavfm
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "AdoWavfm" ); //加载libAdoWavfm.so。
    }

    public long m_AdoWavfmPt; //存放音频波形器的指针。

    //构造函数。
    public AdoWavfm()
    {
        m_AdoWavfmPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Dstoy( null );
    }

    //创建并初始化音频波形器。
    public int Init( VarStr ErrInfoVarStrPt )
    {
        if( m_AdoWavfmPt == 0 )
        {
            HTLong p_AdoWavfmPt = new HTLong();
            if( AdoWavfmInit( p_AdoWavfmPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_AdoWavfmPt = p_AdoWavfmPt.m_Val;
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

    //绘制音频波形到Surface。
    public int Draw( short PcmFrmPt[], int FrmLen, Surface DstSurfacePt, VarStr ErrInfoVarStrPt )
    {
        return AdoWavfmDraw( m_AdoWavfmPt, PcmFrmPt, FrmLen, DstSurfacePt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 );
    }

    //销毁音频波形器。
    public int Dstoy( VarStr ErrInfoVarStrPt )
    {
        if( m_AdoWavfmPt != 0 )
        {
            if( AdoWavfmDstoy( m_AdoWavfmPt, ( ErrInfoVarStrPt != null ) ? ErrInfoVarStrPt.m_VarStrPt : 0 ) == 0 )
            {
                m_AdoWavfmPt = 0;
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

    //创建并初始化音频波形器。
    public native int AdoWavfmInit( HTLong AdoWavfmPt, long ErrInfoVarStrPt );

    //绘制音频波形到Surface。
    public native int AdoWavfmDraw( long AdoWavfmPt, short PcmFrmPt[], int FrmLen, Surface DstSurfacePt, long ErrInfoVarStrPt );

    //销毁音频波形器。
    public native int AdoWavfmDstoy( long AdoWavfmPt, long ErrInfoVarStrPt );
}