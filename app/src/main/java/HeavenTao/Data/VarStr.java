package HeavenTao.Data;

//动态字符串类。
public class VarStr
{
    private long m_VarStrPt; //存放动态字符串的内存指针。

    static
    {
        System.loadLibrary( "Func" ); //加载libFunc.so。
    }

    //构造函数。
    public VarStr()
    {
        m_VarStrPt = 0;
    }

    //析构函数。
    public void finalize()
    {
        Destroy();
    }

    //获取动态字符串的内存指针。
    public long GetVarStrPt()
    {
        return m_VarStrPt;
    }

    //创建并初始化动态字符串。
    public native int Init();

    //复制字符串到动态字符串。
    public native int Cpy( String StrPt );

    //插入字符串到动态字符串的指定位置。
    public native int Ins( long Pos, String StrPt );

    //追加字符串到动态字符串的末尾。
    public native int Cat( String StrPt );

    //清空动态字符串的字符串。
    public native int SetEmpty();

    //设置动态字符串的字符串内存大小。
    public native int SetSz( long StrSz );

    //获取动态字符串的字符串内存大小。
    public native int GetSz( HTLong StrSzPt );

    //获取动态字符串的字符串。
    public native String GetStr();

    //销毁动态字符串。
    public native int Destroy();
}