package HeavenTao.Data;

import android.os.SystemClock;

import java.util.concurrent.ConcurrentLinkedDeque;

public abstract class MsgQueue //消息队列。
{
	class Msg //消息。
	{
		int m_MsgPocsRslt; //消息处理结果。
		int m_MsgTyp; //消息类型。
		Object[] m_MsgArgCntnrPt; //消息参数容器的指针。
	}
	public final ConcurrentLinkedDeque< Msg > m_MsgCntnrPt = new ConcurrentLinkedDeque<>(); //存放消息容器的指针。这里忽略报错“Call requires API level 21 (current min is 14): new java.util.concurrent.ConcurrentLinkedDeque”。
	public final Thread m_MsgPocsThrdPt; //消息处理线程的指针。

	//用户定义的消息处理回调函数。
	public abstract int UserMsgPocs( int MsgTyp, Object[] MsgArgPt );

	//构造函数。
	public MsgQueue( Thread MsgPocsThrdPt )
	{
		m_MsgPocsThrdPt = MsgPocsThrdPt;
	}

	//消息处理线程进行消息处理。
	public int MsgPocsThrdMsgPocs()
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。
		Msg p_MsgPt;

		Out:
		{
			if( m_MsgCntnrPt.isEmpty() ) break Out; //如果没有消息需要处理。

			p_MsgPt = m_MsgCntnrPt.pollFirst(); //从消息容器中取出并删除第一个消息。这里忽略报错“Call requires API level 21 (current min is 14): java.util.concurrent.ConcurrentLinkedDeque#pollFirst”。
			p_MsgPt.m_MsgPocsRslt = UserMsgPocs( p_MsgPt.m_MsgTyp, p_MsgPt.m_MsgArgCntnrPt ); //调用用户定义的消息处理回调函数。

			p_Rslt = 0; //设置本函数执行成功。
		}

		if( p_Rslt != 0 ) //如果本函数执行失败。
		{

		}
		return p_Rslt;
	}

	//发送消息。
	public int SendMsg( int IsBlockWait, int AddFirstOrLast, int MsgTyp, Object... MsgArgPt )
	{
		int p_Rslt = -1; //存放本函数的执行结果，为0表示成功，为非0表示失败。

		Out:
		{
			Msg p_MsgPt = new Msg();

			//放入消息到消息容器。
			p_MsgPt.m_MsgPocsRslt = -99999;
			p_MsgPt.m_MsgTyp = MsgTyp;
			p_MsgPt.m_MsgArgCntnrPt = MsgArgPt;
			if( AddFirstOrLast == 0 ) m_MsgCntnrPt.addFirst( p_MsgPt ); //添加消息到消息容器第一个。这里忽略报错“Call requires API level 21 (current min is 14): java.util.concurrent.ConcurrentLinkedDeque#addFirst”。
			else m_MsgCntnrPt.addLast( p_MsgPt ); //添加消息到消息容器最后一个。这里忽略报错“Call requires API level 21 (current min is 14): java.util.concurrent.ConcurrentLinkedDeque#addLast”。

			if( IsBlockWait != 0 ) //如果要阻塞等待。
			{
				if( Thread.currentThread().getId() != m_MsgPocsThrdPt.getId() ) //如果发送消息线程不是消息处理线程。
				{
					do
					{
						SystemClock.sleep( 1 ); //暂停一下，避免CPU使用率过高。
					} while( ( m_MsgPocsThrdPt.isAlive() ) && ( p_MsgPt.m_MsgPocsRslt == -99999 ) );
				}
				else //如果发送消息线程就是消息处理线程。
				{
					do
					{
						MsgPocsThrdMsgPocs();
					} while( p_MsgPt.m_MsgPocsRslt == -99999 );
				}
				p_Rslt = p_MsgPt.m_MsgPocsRslt; //返回消息处理结果。
			}
			else //如果不阻塞等待。
			{
				p_Rslt = 0; //返回消息处理结果为成功。因为要让设置函数返回成功。
			}
		}

		return p_Rslt;
	}
}
