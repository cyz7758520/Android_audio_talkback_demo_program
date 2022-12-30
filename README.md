# Android下音视频对讲演示程序
# 必读说明

# 简介
&emsp;&emsp;本软件根据《道德经》为核心思想而设计，实现了两个设备之间通过TCP或UDP协议进行实时半双工和实时全双工音视频对讲，并做了以下增强处理：  
&emsp;&emsp;* 支持8000Hz、16000Hz、32000Hz、48000Hz的音频。  
&emsp;&emsp;* 支持声学回音消除，通过本人自己设计的音频输入输出帧同步方法、自适应设置回音延迟方法、三重声学回音消除器，声学回音可以消除到99%以上，还可以消除同一房间回音，且收敛时间很短，无论网络如何抖动都可以消除。  
&emsp;&emsp;* 支持噪音抑制，对常见的底噪音、嘈杂的街道音、风吹音、等都有抑制效果。  
&emsp;&emsp;* 支持语音活动检测，只有在人说话时才发送网络数据，无人说话时不产生网络数据，从而降低噪音、降低网络流量。  
&emsp;&emsp;* 支持自动增益控制，当人说话声音较小时会自动增大音量，当人说话声音较大时会自动减小音量。  
&emsp;&emsp;* 支持音频编解码，对音频数据的压缩率在1~20%之间，且支持动态比特率，从而大幅度降低网络流量，还支持数据包丢失隐藏，当网络丢包率高达30%时，仍然可以进行对讲。  
&emsp;&emsp;* 支持保存音频到文件和绘制音频波形到Surface，可以直观且方便的调试音频。  
&emsp;&emsp;* 支持视频软硬编解码，支持指定比特率，最低到10KB/s仍然可以进行视频对讲，还支持横竖屏切换。  
&emsp;&emsp;* 支持音视频自适应抖动缓冲，当网络存在丢包、乱序、延时等抖动情况时，通过自适应调节缓冲深度来应对这些抖动。  
&emsp;&emsp;* 支持自定义调节各种功能的参数来适应不同的设备，绝大部分情况下都不需要修改。  
&emsp;&emsp;* 支持与Windows下音视频对讲演示程序进行音视频对讲。  

&emsp;&emsp;声学回音消除器效果对比：
![image](https://user-images.githubusercontent.com/16315192/115977799-d0722980-a5ad-11eb-811e-92da4d4f32f9.png)

# 准备
&emsp;&emsp;准备两台安装了Android 2.3及以上系统的设备（已适配到Android 13.0），其中一台设备作为客户端可以连接到另一台作为服务端的设备（可以用Ping工具测试，建议两台设备在同一局域网内），且两台设备都安装相同版本的本软件。

# 开始
&emsp;&emsp;在一台设备上直接点击创建服务端，再在另一台设备上将IP地址改为服务端设备的IP地址，并点击连接服务端，即可开始对讲，在任意一端点击中断，即可中断对讲。  

&emsp;&emsp;设置按钮提供了各项功能的参数设置，绝大部分情况下都不需要修改，当然你也可以根据需要自行修改。  

&emsp;&emsp;**特别注意：如果把两台设备放在同一房间里测试，有可能会出现啸叫、声音不完整、等问题，这是因为现在的麦克风都很灵敏了，一点小小的声音都会被录进去，两台设备会相互录音，导致软件无法正确识别回音，所以建议放在不同的房间里测试。如果实在要测试这种情况，就在设置里，Speex预处理器的其他功能设置里，关闭“使用自动增益控制”后再测试。**  

# 移植
&emsp;&emsp;如果需要在自己的软件中使用本软件的音视频功能，需要以下几个步骤：  
&emsp;&emsp;1、在AndroidManifest.xml文件中添加android.permission.RECORD_AUDIO、android.permission.MODIFY_AUDIO_SETTINGS、android.permission.CAMERA权限。  
&emsp;&emsp;2、将HeavenTao.XXXX包和jniLibs文件夹下各个平台的动态库复制到自己的软件中。  
&emsp;&emsp;3、继承HeavenTao.Media.MediaPocsThrd媒体处理线程类，实现UserInit、UserPocs、UserDstoy、UserMsg、UserReadAdoVdoInptFrm、UserWriteAdoOtptFrm、UserGetPcmAdoOtptFrm、UserWriteVdoOtptFrm、UserGetYU12VdoOtptFrm这九个回调成员函数。如果要在JNI层处理音视频帧，则可以将这些回调成员函数继承为native函数，然后在JNI层实现即可。  
&emsp;&emsp;4、new这个继承的类，然后调用类的相关设置成员函数，最后调用start()成员函数启动媒体处理线程即可。  
&emsp;&emsp;5、当需要媒体处理线程退出时，调用类的RqirExit()成员函数即可。  

&emsp;&emsp;如果用户有不需要的部分功能，则只需要删除该功能对应的库文件即可，还可以进一步删除对应的类文件，并修改HeavenTao.Media.MediaPocsThrd类文件即可。  

&emsp;&emsp;**普通免费功能包括：WebRtc定点版声学回音消除器、Speex预处理器的噪音抑制、WebRtc定点版噪音抑制器、WebRtc浮点版噪音抑制器、Speex预处理器的其他功能、Speex编解码器、Wave文件读取器、Wave文件写入器、音频波形器、本端TCP协议服务端套接字、本端TCP协议客户端套接字、本端UDP协议套接字。**  

&emsp;&emsp;**高级收费功能包括：Speex声学回音消除器、WebRtc浮点版声学回音消除器、SpeexWebRtc三重声学回音消除器、RNNoise噪音抑制器、OpenH264编解码器、系统自带H264编解码器、自己设计的自适应抖动缓冲器、本端高级UDP协议套接字。**  

&emsp;&emsp;各个功能对应的文件如下：  
&emsp;&emsp;* Speex声学回音消除器：libFunc.so、libSpeexDsp.so、SpeexAec.java。  
&emsp;&emsp;* WebRtc定点版声学回音消除器：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcAecm.java。  
&emsp;&emsp;* WebRtc浮点版声学回音消除器：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcAec.java。  
&emsp;&emsp;* SpeexWebRtc三重声学回音消除器：libFunc.so、libSpeexDsp.so、libc++_shared.so、libWebRtc.so、SpeexWebRtcAec.java。  
&emsp;&emsp;* WebRtc定点版噪音抑制器：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcNsx.java。  
&emsp;&emsp;* WebRtc浮点版噪音抑制器：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcNs.java。  
&emsp;&emsp;* RNNoise噪音抑制器：libFunc.so、libc++_shared.so、libWebRtc.so、libRNNoise.so、RNNoise.java。  
&emsp;&emsp;* Speex预处理器：libFunc.so、libSpeexDsp.so、SpeexPrpocs.java。  
&emsp;&emsp;* Speex编解码器：libFunc.so、libSpeex.so、SpeexEncd.java、SpeexDecd.java。  
&emsp;&emsp;* Wave文件写入读取器：libFunc.so、libWaveFile.so、WaveFileReader.java、WaveFileWriter.java。  
&emsp;&emsp;* 音频波形器：libFunc.so、libAdoWavfm.so、AdoWavfm.java。  
&emsp;&emsp;* OpenH264编解码器：libFunc.so、libOpenH264.so、OpenH264Encd.java、OpenH264Decd.java。  
&emsp;&emsp;* 系统自带H264编解码器：libFunc.so、libSystemH264.so、SystemH264Encd.java、SystemH264Decd.java。  
&emsp;&emsp;* 图片处理：libFunc.so、libLibYUV.so、LibYUV.java。  
&emsp;&emsp;* 音视频自适应抖动缓冲器：libFunc.so、libc++_shared.so、libAjb.so、AAjb.java、VAjb.java。  
&emsp;&emsp;* 本端TCP协议UDP协议套接字：libFunc.so、libSokt.so、TcpSrvrSokt.java、TcpClntSokt.java、UdpSokt.java、AudpSokt.java。  

# 注意
&emsp;&emsp;不要在64位操作系统下使用32位动态库，或在32位操作系统下使用64位动态库，否则会导致意想不到的问题。  
&emsp;&emsp;不要对HeavenTao.XXXX包进行代码混淆，否则会导致意想不到的问题。  
&emsp;&emsp;从老版本更新到新版本时，类文件和库文件全部都要更新，不能只更新类文件或库文件，否则会导致意想不到的问题。  
&emsp;&emsp;如果要使用8000Hz采样频率时，最好不要使用RNNoise噪音抑制器，因为它对8000Hz的声音抑制非常强烈。  
&emsp;&emsp;本软件不支持音乐，尤其是系统自带的噪音抑制器和RNNoise噪音抑制器可能对音乐的抑制非常强烈。  
&emsp;&emsp;某些Android设备的软硬件环境可能存在问题，从而可能会导致声学回音消除失败，这种情况必须要先解决这些问题。  
&emsp;&emsp;某些Android设备的系统自带的声学回音消除器、噪音抑制器和自动增益控制器在使用后可能会导致音频输入出现问题，这种情况可以先关闭后再试试。  
&emsp;&emsp;音频波形器占用CPU比较高，建议只在需要调试时临时打开。  
&emsp;&emsp;系统自带H264编解码器需要Android 5.0（API 21）及以上系统，且在某些Android设备上使用可能会花屏，这种情况只能使用OpenH264编解码器。  

# 其他
&emsp;&emsp;本软件采用了Speex的1.2.1版本、SpeexDsp的1.2.1版本、WebRtc的2019年7月份版本、OpenH264的2.3.1版本为基础，并进行了大量优化。  
&emsp;&emsp;讨论QQ群：511046632    欢迎大家参与测试和讨论！  
&emsp;&emsp;本人QQ号：280604597    赤勇玄心行天道  
&emsp;&emsp;本人博客：http://www.cnblogs.com/gaoyaguo  
&emsp;&emsp;Windows版源代码：https://github.com/cyz7758520/Windows_audio_talkback_demo_program  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;https://gitee.com/chen_yi_ze/Windows_audio_talkback_demo_program  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;https://gitcode.net/cyz7758520/Windows_audio_talkback_demo_program  
&emsp;&emsp;Android版源代码：https://github.com/cyz7758520/Android_audio_talkback_demo_program  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;https://gitee.com/chen_yi_ze/Android_audio_talkback_demo_program  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;https://gitcode.net/cyz7758520/Android_audio_talkback_demo_program  

# 版权
&emsp;&emsp;Speex：https://gitlab.xiph.org/xiph/speex/-/blob/master/COPYING  
&emsp;&emsp;WebRtc：https://gitlab.com/webrtc-mirror/webrtc/-/blob/master/LICENSE  
&emsp;&emsp;RNNoise：https://gitlab.xiph.org/xiph/rnnoise/-/blob/master/COPYING  
&emsp;&emsp;OpenH264：https://github.com/cisco/openh264/blob/master/LICENSE  
&emsp;&emsp;LibYUV：https://github.com/lemenkov/libyuv/blob/master/LICENSE  

# 感谢
&emsp;&emsp;感谢 WELEN、善书、陈国福 对 Speex、WebRTC 的指点！  

# 函数
### 九个回调函数
___
函数名称：UserInit  
功能说明：用户定义的初始化函数，在本线程刚启动时回调一次。  
参数说明：无。  
返回说明：0：成功。  
&emsp;&emsp;&emsp;&emsp;&emsp;非0：失败。  
___
函数名称：UserPocs  
功能说明：用户定义的处理函数，在本线程运行时每隔1毫秒就回调一次。  
参数说明：无。  
返回说明：0：成功。  
&emsp;&emsp;&emsp;&emsp;&emsp;非0：失败。  
___
函数名称：UserDstoy  
功能说明：用户定义的销毁函数，在本线程退出时回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserMsg  
功能说明：用户定义的消息函数，在接收到用户消息时回调一次。  
参数说明：MsgArgPt：\[输入\]，存放消息参数的动态参数的指针。如果没有消息参数，则本参数为null。  
返回说明：无。  
___
函数名称：UserReadAdoVdoInptFrm  
功能说明：用户定义的读取音视频输入帧函数，在读取到一个音频输入帧或视频输入帧并处理完后回调一次。如果没有使用音频输入和视频输入，则本函数不会被回调。  
参数说明：PcmAdoInptFrmPt：\[输入\]，存放PCM格式音频输入帧的指针。如果没有使用音频输入，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;PcmAdoRsltFrmPt：\[输入\]，存放PCM格式音频结果帧的指针。如果没有使用音频输入，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VoiceActStsPt：\[输入\]，存放语音活动状态的指针，为非0表示有语音活动，为0表示无语音活动。如果没有使用音频输入，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;EncdAdoInptFrmPt：\[输入\]，存放已编码格式音频输入帧的指针。如果没有使用音频输入，或音频输入编码器使用PCM原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;EncdAdoInptFrmLenPt：\[输入\]，存放已编码格式音频输入帧长度的指针，单位为字节。如果没有使用音频输入，或音频输入编码器使用PCM原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;EncdAdoInptFrmIsNeedTransPt：\[输入\]，存放已编码格式音频输入帧是否需要传输的指针，为非0表示需要传输，为0表示不需要传输。如果没有使用音频输入，或音频输入编码器使用PCM原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;YU12VdoInptFrmPt：\[输入\]，存放YU12格式视频输入帧的指针。如果没有使用视频输入，或本次没有读取到视频输入帧，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;YU12VdoInptFrmWidthPt：\[输入\]，存放YU12格式视频输入帧宽度的指针。如果没有使用视频输入，或本次没有读取到视频输入帧，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;YU12VdoInptFrmHeightPt：\[输入\]，存放YU12格式视频输入帧高度的指针。如果没有使用视频输入，或本次没有读取到视频输入帧，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;EncdVdoInptFrmPt：\[输入\]，存放已编码格式视频输入帧的指针。如果没有使用视频输入，或本次没有读取到视频输入帧，或视频输入编码器使用YU12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;EncdVdoInptFrmLenPt：\[输入\]，存放已编码格式视频输入帧长度的指针，单位为字节。如果没有使用视频输入，或本次没有读取到视频输入帧，或视频输入编码器使用YU12原始数据，则本参数为null。  
返回说明：0：成功。  
&emsp;&emsp;&emsp;&emsp;&emsp;非0：失败。  
___
函数名称：UserWriteAdoOtptFrm  
功能说明：用户定义的写入音频输出帧函数，在需要写入一个音频输出帧时回调一次。如果没有使用音频输出，则本函数不会被回调。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。  
参数说明：PcmAdoOtptFrmPt：\[输出\]，存放PCM格式音频输出帧的指针。如果音频输出解码器没有使用PCM原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;EncdAdoOtptFrmPt：\[输出\]，存放已编码格式音频输出帧的指针。如果音频输出解码器使用PCM原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptFrmLenPt：\[输入&输出\]，输入时，存放音频输出帧最大长度的指针，输出时，存放音频输出帧长度的指针。如果音频输出解码器使用PCM原始数据，则单位为采样。如果音频输出解码器没有使用PCM原始数据，则单位为字节。  
返回说明：无。  
___
函数名称：UserGetPcmAdoOtptFrm  
功能说明：用户定义的获取PCM格式音频输出帧函数，在解码完一个已编码音频输出帧时回调一次。如果没有使用音频输出，则本函数不会被回调。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。  
参数说明：PcmAdoOtptFrmPt：\[输入\]，存放PCM格式音频输出帧的指针。  
&emsp;&emsp;&emsp;&emsp;&emsp;PcmAdoOtptFrmLen：\[输入\]，存放PCM格式音频输出帧的长度，单位为采样。  
返回说明：无。  
___
函数名称：UserWriteVdoOtptFrm  
功能说明：用户定义的写入视频输出帧函数，在可以显示一个视频输出帧时回调一次。如果没有使用视频输出，则本函数不会被回调。注意：本函数不是在媒体处理线程中执行的，而是在视频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音视频输出帧不同步。  
参数说明：YU12VdoOtptFrmPt：\[输出\]，存放YU12格式视频输出帧的指针。如果视频输出解码器没有使用YU12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;YU12VdoInptFrmWidthPt：\[输出\]，存放YU12格式视频输出帧宽度的指针。如果视频输出解码器没有使用YU12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;YU12VdoInptFrmHeightPt：\[输出\]，存放YU12格式视频输出帧高度的指针。如果视频输出解码器没有使用YU12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;EncdVdoOtptFrmPt：\[输出\]，存放已编码格式视频输出帧的指针。如果视频输出解码器使用YU12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptFrmLenPt：\[输入&输出\]，输入时，存放视频输出帧最大长度的指针，输出时，存放视频输出帧长度的指针。单位为字节。   
返回说明：无。  
___
函数名称：UserGetYU12VdoOtptFrm  
功能说明：用户定义的获取YU12格式视频输出帧函数，在解码完一个已编码视频输出帧时回调一次。如果没有使用视频输出，则本函数不会被回调。注意：本函数不是在媒体处理线程中执行的，而是在视频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音视频输出帧不同步。  
参数说明：YU12VdoOtptFrmPt：\[输入\]，存放YU12格式视频输出帧的指针。  
&emsp;&emsp;&emsp;&emsp;&emsp;YU12VdoOtptFrmWidth：\[输入\]，存放YU12格式视频输出帧的宽度。  
&emsp;&emsp;&emsp;&emsp;&emsp;YU12VdoOtptFrmHeight：\[输入\]，存放YU12格式视频输出帧的高度。  
返回说明：无。  
___
