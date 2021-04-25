package HeavenTao.Video;

import android.graphics.Bitmap;
import android.view.Surface;

import HeavenTao.Data.*;

//图片处理类。
public class LibYUV
{
    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
        System.loadLibrary( "LibYUV" ); //加载libLibYUV.so。
    }

    //构造函数。
    public LibYUV()
    {

    }

    //析构函数。
    public void finalize()
    {

    }

    //图片格式。
    public static final int PICTR_FMT_NV21 = 0x3132564E;
    public static final int PICTR_FMT_YV12 = 0x32315659;
    public static final int PICTR_FMT_YU12 = 0x32315559;
    public static final int PICTR_FMT_I420 = 0x30323449;
    public static final int PICTR_FMT_ARGB8888 = 0x42475241;
    public static final int PICTR_FMT_ABGR8888 = 0x52474241;
    public static final int PICTR_FMT_RGB565 = 0x50424752;

    //旋转角度。
    public static final int ROTATE_DEGREE_0 = 0;
    public static final int ROTATE_DEGREE_90 = 90;
    public static final int ROTATE_DEGREE_180 = 180;
    public static final int ROTATE_DEGREE_270 = 270;

    public native static int PictrRotate( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight, int RotateDegree, byte DstPictrPt[], long DstPictrSz, HTInt DstPictrWidthPt, HTInt DstPictrHeightPt, VarStr ErrInfoVarStrPt );

    public native static int PictrScale( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight, byte DstPictrPt[], long DstPictrSz, int DstPictrWidth, int DstPictrHeight, int Quality, HTLong DstPictrLenPt, VarStr ErrInfoVarStrPt );

    public native static int PictrFmtCnvrt( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight, byte DstPictrPt[], long DstPictrSz, int DstPictrFmt, HTLong DstPictrLenPt, VarStr ErrInfoVarStrPt );

    public native static int PictrDrawToBitmap( byte SrcPictrPt[], int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight, Bitmap DstBitmapPt, VarStr ErrInfoVarStrPt );

    public native static int PictrDrawToSurface( byte SrcPictrPt[], long SrcPictrStart, int SrcPictrFmt, int SrcPictrWidth, int SrcPictrHeight, Surface DstSurfacePt, VarStr ErrInfoVarStrPt );
}