package HeavenTao.Ado;

import android.view.Surface;

import HeavenTao.Data.*;

//音频波形器。
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
    protected void finalize()
    {
        Dstoy( null );
    }

    //创建并初始化音频波形器。
    public int Init( Vstr ErrInfoVstrPt )
    {
        if( m_AdoWavfmPt == 0 )
        {
            HTLong p_AdoWavfmPt = new HTLong();
            if( AdoWavfmInit( p_AdoWavfmPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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
    public int Draw( short PcmFrmPt[], long FrmLenUnit, Surface DstSurfacePt, Vstr ErrInfoVstrPt )
    {
        return AdoWavfmDraw( m_AdoWavfmPt, PcmFrmPt, FrmLenUnit, DstSurfacePt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 );
    }

    //销毁音频波形器。
    public int Dstoy( Vstr ErrInfoVstrPt )
    {
        if( m_AdoWavfmPt != 0 )
        {
            if( AdoWavfmDstoy( m_AdoWavfmPt, ( ErrInfoVstrPt != null ) ? ErrInfoVstrPt.m_VstrPt : 0 ) == 0 )
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
    private native int AdoWavfmInit( HTLong AdoWavfmPt, long ErrInfoVstrPt );

    //绘制音频波形到Surface。
    private native int AdoWavfmDraw( long AdoWavfmPt, short PcmFrmPt[], long FrmLenUnit, Surface DstSurfacePt, long ErrInfoVstrPt );

    //销毁音频波形器。
    private native int AdoWavfmDstoy( long AdoWavfmPt, long ErrInfoVstrPt );
}