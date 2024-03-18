package com.example.andrd_ado_vdo_tkbk_demo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

//前台服务。
public class FrgndSrvc extends Service
{
    MainAct m_MainActPt; //存放主界面的指针。

    public class FrgndSrvcBinder extends Binder
    {
        public void SetForeground( MainAct MainActPt )
        {
            m_MainActPt = MainActPt;

            NotificationManager p_NotificationManagerPt = (NotificationManager) getSystemService( NOTIFICATION_SERVICE ); //存放通知管理器对象的指针。

            if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ) //如果当前系统为Andoird 8.0及以上。
            {
                //创建状态通知的通知渠道，并设置为静音。
                NotificationChannel p_NotificationChannel = new NotificationChannel( "前台服务通知", "前台服务通知", NotificationManager.IMPORTANCE_HIGH );
                p_NotificationChannel.setSound( null, null );
                p_NotificationManagerPt.createNotificationChannel( p_NotificationChannel );
            }

            //创建通知。
            PendingIntent pendingIntent = PendingIntent.getActivity( m_MainActPt, 0, new Intent( m_MainActPt, MainAct.class ), PendingIntent.FLAG_IMMUTABLE );
            Notification notification =
                    new NotificationCompat
                            .Builder( m_MainActPt, "前台服务通知" ) //Android API 14及以上版本使用。
                            //new NotificationCompat.Builder( m_MainActPt ) //Android API 9~25版本使用。
                            .setSmallIcon( R.mipmap.tkbk_icon )
                            .setContentTitle( m_MainActPt.getString( R.string.app_name ) )
                            .setContentText( "前台服务" )
                            .setOngoing( true )
                            .setSound( null )
                            .setContentIntent( pendingIntent )
                            .build();

            //发送通知，并变成前台服务。
            startForeground( 1, notification );
        }
    }

    @Nullable @Override public IBinder onBind( Intent intent ) //本服务被绑定。
    {
        return new FrgndSrvcBinder();
    }

    @Override public boolean onUnbind( Intent intent ) //本服务被解除绑定。
    {
        stopForeground( true ); //退出前台服务，并变成普通服务。

        return super.onUnbind( intent );
    }

    @Override public void onDestroy() //本服务被销毁。
    {
        super.onDestroy();
    }
}