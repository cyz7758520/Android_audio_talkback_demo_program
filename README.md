# Android下音视频对讲演示程序
# 必读说明

# 简介
&emsp;&emsp;本软件根据《道德经》为核心思想而设计，实现了两至多个设备之间进行音视频对讲，实现了一个设备对多个设备进行广播对讲，一般可用于楼宇对讲、智能门铃对讲、企业员工对讲、智能对讲机、视频会议、以及类似于微信QQ这种需要两至多人进行音视频对讲的场景。本软件支持以下增强处理：  
&emsp;&emsp;* 支持同时与多个设备建立连接，并同时与多个设备进行音视频对讲。  
&emsp;&emsp;* 支持IPv4和IPv6的TCP和UDP协议传输，UDP协议支持可靠传输、支持连接中途更换IP不中断。  
&emsp;&emsp;* 支持实时半双工（一键通）和实时全双工的音频或视频或音视频对讲。  
&emsp;&emsp;* 支持8000Hz、16000Hz、32000Hz、48000Hz的音频。  
&emsp;&emsp;* 支持声学回音消除，通过本人自己设计的音频输入输出帧同步方法、自适应设置回音延迟方法、三重声学回音消除器，声学回音可以消除到99%以上，还可以消除同一房间回音，且收敛时间很短，无论网络如何抖动都可以消除。  
&emsp;&emsp;* 支持噪音抑制，对常见的底噪音、嘈杂的街道音、风吹音、等都有抑制效果。  
&emsp;&emsp;* 支持语音活动检测，只有在人说话时才发送网络数据，无人说话时不产生网络数据，从而降低噪音、降低网络流量。  
&emsp;&emsp;* 支持自动增益控制，当人说话声音较小时会自动增大音量，当人说话声音较大时会自动减小音量。  
&emsp;&emsp;* 支持音频编解码，对音频数据的压缩率在1~20%之间，且支持动态比特率，从而大幅度降低网络流量，声音依然清晰，还支持数据包丢失隐藏，当网络丢包率高达30%时，仍然可以进行对讲。  
&emsp;&emsp;* 支持保存音频到文件和绘制音频波形到Surface，可以直观且方便的调试音频。  
&emsp;&emsp;* 支持视频软硬编解码，支持指定比特率，最低到10KB/s仍然可以进行视频对讲，还支持横竖屏切换。  
&emsp;&emsp;* 支持音视频自适应抖动缓冲，当网络存在丢包、乱序、延时等抖动情况时，通过自适应调节缓冲深度来应对这些抖动。  
&emsp;&emsp;* 支持自定义调节各种功能的参数来适应不同的设备，绝大部分情况下使用默认设置即可。  
&emsp;&emsp;* 支持将对讲时全部设备的音视频输入输出保存到一个Avi文件。  
&emsp;&emsp;* 支持与Windows下音视频对讲演示程序进行音视频对讲。  

&emsp;&emsp;声学回音消除器效果对比：
|名称|收敛时间|回音延迟不稳定|残余回音|远近端同时说话|同一房间对讲|运算量|
|:---|:---|:---|:---|:---|:---|:---|
|Speex声学回音消除器|有语音活动1~3秒|0~3秒自适应调节|回音延迟稳定时1%<br>回音延迟不稳定时50%|近端语音被消除20%|会产生一定回音|一般|
|WebRtc定点版声学回音消除器|0秒|延迟400ms以内0秒自适应调节<br>延后超过400ms将无法消除|回音延迟稳定时1%<br>回音延迟不稳定时1%|近端语音被消除99%|会产生较大回音|一般|
|WebRtc浮点版声学回音消除器|有语音活动1秒|0秒自适应调节|回音延迟稳定时1%<br>回音延迟不稳定时5%|近端语音被消除50%|会产生较小回音|较大|
|WebRtc第三版声学回音消除器|有语音活动1秒|0秒自适应调节|回音延迟稳定时5%<br>回音延迟不稳定时10%|近端语音被消除30%|会产生较小回音|很大|
|Speex声学回音消除器<br>+WebRtc定点版声学回音消除器|0秒|0~3秒自适应调节|回音延迟稳定时1%<br>回音延迟不稳定时50%|近端语音被消除20%|会产生一定回音|一般|
|WebRtc定点版声学回音消除器<br>+WebRtc浮点版声学回音消除器|0秒|0秒自适应调节|回音延迟稳定时1%<br>回音延迟不稳定时5%|近端语音被消除50%|会产生较小回音|较大|
|Speex声学回音消除器<br>+WebRtc定点版声学回音消除器<br>+WebRtc浮点版声学回音消除器|0秒|0秒自适应调节|回音延迟稳定时1%<br>回音延迟不稳定时1%~5%|近端语音被消除50%|会产生很小回音|很大|
|WebRtc定点版声学回音消除器<br>+WebRtc第三版声学回音消除器|0秒|0秒自适应调节|回音延迟稳定时1%<br>回音延迟不稳定时10%|近端语音被消除30%|会产生较小回音|很大|
|Speex声学回音消除器<br>+WebRtc定点版声学回音消除器<br>+WebRtc第三版声学回音消除器|0秒|0秒自适应调节|回音延迟稳定时1%<br>回音延迟不稳定时1%~10%|近端语音被消除30%|会产生很小回音|巨大|

&emsp;&emsp;特别注意：以上是在不使用系统自带的声学回音消除器的效果，且不同设备或不同环境或不同时间效果都会不同，所以需要自己亲自测试。  

# 准备
&emsp;&emsp;准备两至多个安装了Android 2.3及以上系统的设备（已适配到Android 15.0），设备之间可以相互通信（可以用Ping工具测试，建议设备都在同一局域网内，外网设备访问内网设备可能需要做内网穿透），所有设备都安装相同版本的本软件。  

# 开始
&emsp;&emsp;1、在一个设备上直接点击服务端的创建按钮。  
&emsp;&emsp;2、在全部需要参与对讲的设备上将客户端的IP地址改为服务端设备的IP地址，并点击添加按钮。  
&emsp;&emsp;3、在客户端列表里点击刚刚添加的IP地址，再点击连接按钮，即可与连接上该服务端的全部设备进行对讲。  
&emsp;&emsp;**4、如果服务端所在设备也需要参与对讲，则也需要进行添加和连接操作。**  

&emsp;&emsp;一键即按即广播按钮：批量连接客户端列表里全部服务端，并广播音频输入到全部服务端，连接到这些服务端的客户端都能听见该音频。  

&emsp;&emsp;设置按钮：提供了各项功能的参数设置，大部分情况下都不需要修改，如果发现不适合某些设备，则需要根据设备情况修改。  
&emsp;&emsp;保存设置按钮：将各项功能的参数保存到Stng.xml中，每次运行本软件时会自动读取设置。  
&emsp;&emsp;读取设置按钮：将Stng.xml中各项功能的参数读取到本软件中。  
&emsp;&emsp;删除设置按钮：将Stng.xml文件删除。  
&emsp;&emsp;重置设置按钮：将本软件的设置重置为默认。  

&emsp;&emsp;**特别注意：如果把两至多个设备放在同一房间里测试，有可能会出现啸叫、声音不完整、等问题，这是因为现在的麦克风都很灵敏了，一点小小的声音都会被录进去，设备之间会相互录音，导致软件无法正确识别回音，所以建议放在不同的房间里测试。如果实在要测试这种情况，就在设置里，Speex预处理器的设置里，关闭“使用自动增益控制”后再测试。**  

# 移植
&emsp;&emsp;如果需要在自己的软件中使用本软件的音视频功能，需要以下几个步骤：  
&emsp;&emsp;1、在AndroidManifest.xml文件中添加android.permission.RECORD_AUDIO、android.permission.MODIFY_AUDIO_SETTINGS、android.permission.CAMERA权限。  
&emsp;&emsp;2、将HeavenTao.XXXX包和jniLibs文件夹下各个平台的动态库复制到自己的软件中。  
&emsp;&emsp;3、如果自己的软件已有传输协议：继承HeavenTao.Media.MediaPocsThrd媒体处理线程类，实现UserInit、UserDstoy、UserPocs、UserMsg、UserDvcChg、UserReadAdoVdoInptFrm、UserWriteAdoOtptFrm、UserGetAdoOtptFrm、UserWriteVdoOtptFrm、UserGetVdoOtptFrm这些回调成员函数。  
&emsp;&emsp;&emsp;&emsp;如果自己的软件没有传输协议：继承HeavenTao.Media.SrvrThrd服务端线程类，实现UserInit、UserDstoy、UserPocs、UserMsg、UserShowLog、UserShowToast、UserVibrate、UserSrvrInit、UserSrvrDstoy、UserCnctInit、UserCnctDstoy、UserCnctSts、UserCnctRmtTkbkMode、UserCnctTstNtwkDly这些回调成员函数。  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;继承HeavenTao.Media.ClntMediaPocsThrd客户端媒体处理线程类，实现UserInit、UserDstoy、UserPocs、UserMsg、UserDvcChg、UserShowLog、UserShowToast、UserVibrate、UserTkbkClntCnctInit、UserTkbkClntCnctDstoy、UserTkbkClntCnctSts、UserTkbkClntMyTkbkIdx、UserTkbkClntLclTkbkMode、UserTkbkClntTkbkInfoInit、UserTkbkClntTkbkInfoDstoy、UserTkbkClntTkbkInfoRmtTkbkMode、UserTkbkClntTstNtwkDly、UserBdctClntInit、UserBdctClntDstoy、UserBdctClntCnctInit、UserBdctClntCnctDstoy、UserBdctClntCnctSts这些回调成员函数。  
&emsp;&emsp;&emsp;&emsp;如果要在JNI层处理音视频帧，则可以将这些回调成员函数继承为native函数，然后在JNI层实现即可。  
&emsp;&emsp;4、new这个继承的类，然后调用类的相关设置成员函数，最后调用start()成员函数启动该线程即可。  
&emsp;&emsp;5、当需要线程退出时，调用线程类的RqirExit()成员函数即可。  

&emsp;&emsp;如果用户有不需要的部分功能，则只需要删除该功能对应的库文件即可，还可以进一步删除对应的类文件，并修改HeavenTao.Media.MediaPocsThrd类文件即可。  
&emsp;&emsp;如果只移植部分功能，没有移植MediaPocsThrd媒体处理线程类，则效果可能不好，因为MediaPocsThrd类做了很多优化。  

&emsp;&emsp;**普通免费功能包括：WebRtc定点版声学回音消除器、Speex预处理器的噪音抑制、WebRtc定点版噪音抑制器、WebRtc浮点版噪音抑制器、Speex预处理器、Speex编解码器、Wave文件读取器、Wave文件写入器、音频波形器、本端TCP协议服务端套接字、本端TCP协议客户端套接字、本端UDP协议套接字。**  

&emsp;&emsp;**高级收费功能包括：Speex声学回音消除器、WebRtc浮点版声学回音消除器、WebRtc第三版声学回音消除器、SpeexWebRtc三重声学回音消除器、RNNoise噪音抑制器、OpenH264编解码器、系统自带H264编解码器、自己设计的自适应抖动缓冲器、Avi文件写入器、本端高级UDP协议套接字。**  

&emsp;&emsp;各个功能对应的文件如下：  
&emsp;&emsp;* Speex声学回音消除器：libFunc.so、libSpeexDsp.so、SpeexAec.java。  
&emsp;&emsp;* WebRtc定点版声学回音消除器：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcAecm.java。  
&emsp;&emsp;* WebRtc浮点版声学回音消除器：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcAec.java。  
&emsp;&emsp;* WebRtc第三版声学回音消除器：libFunc.so、libc++_shared.so、libWebRtc3.so、WebRtcAec3.java。  
&emsp;&emsp;* SpeexWebRtc三重声学回音消除器：libFunc.so、libSpeexDsp.so、libc++_shared.so、libWebRtc.so、libWebRtc3.so、libSpeexWebRtcAec.so、SpeexWebRtcAec.java。  
&emsp;&emsp;* WebRtc定点版噪音抑制器：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcNsx.java。  
&emsp;&emsp;* WebRtc浮点版噪音抑制器：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcNs.java。  
&emsp;&emsp;* RNNoise噪音抑制器：libFunc.so、libc++_shared.so、libWebRtc.so、libRNNoise.so、RNNoise.java。  
&emsp;&emsp;* Speex预处理器：libFunc.so、libSpeexDsp.so、SpeexPrpocs.java。  
&emsp;&emsp;* Speex编解码器：libFunc.so、libSpeex.so、SpeexEncd.java、SpeexDecd.java。  
&emsp;&emsp;* 音频波形器：libFunc.so、libAdoWavfm.so、AdoWavfm.java。  
&emsp;&emsp;* OpenH264编解码器：libFunc.so、libOpenH264.so、OpenH264Encd.java、OpenH264Decd.java。  
&emsp;&emsp;* 系统自带H264编解码器：libFunc.so、libSystemH264.so、SystemH264Encd.java、SystemH264Decd.java。  
&emsp;&emsp;* 图片处理：libFunc.so、libLibYUV.so、LibYUV.java。  
&emsp;&emsp;* 音视频自适应抖动缓冲器：libFunc.so、libc++_shared.so、libAjb.so、AAjb.java、VAjb.java。  
&emsp;&emsp;* 本端TCP协议UDP协议套接字：libFunc.so、libSokt.so、TcpSrvrSokt.java、TcpClntSokt.java、UdpSokt.java、AudpSokt.java。  
&emsp;&emsp;* Wave文件Avi文件写入读取器：libFunc.so、libMediaFile.so、WaveFileReader.java、WaveFileWriter.java、AviFileWriter.java。  

# 注意
&emsp;&emsp;不要在64位操作系统下使用32位动态库，或在32位操作系统下使用64位动态库，否则会导致意想不到的问题。  
&emsp;&emsp;不要对HeavenTao.XXXX包进行代码混淆，否则会导致意想不到的问题。  
&emsp;&emsp;从老版本更新到新版本时，类文件和库文件全部都要更新，不能只更新类文件或库文件，否则会导致意想不到的问题。  
&emsp;&emsp;本软件测试的网络延迟包括两端来回的网络延迟，还包括两端对数据包的处理延迟，所以未对讲时延迟低一点，对讲开始后延迟会高一点。  
&emsp;&emsp;如果要使用8000Hz采样频率时，最好不要使用RNNoise噪音抑制器，因为它对8000Hz的声音抑制非常强烈。  
&emsp;&emsp;本软件不支持音乐，尤其是系统自带噪音抑制器和RNNoise噪音抑制器可能对音乐的抑制非常强烈。  
&emsp;&emsp;某些Android设备的软硬件环境可能存在问题，从而可能会导致声学回音消除失败，这种情况必须要先解决这些问题。  
&emsp;&emsp;某些Android设备的系统自带声学回音消除器、噪音抑制器和自动增益控制器在使用后可能会导致音频输入出现问题，这种情况可以先关闭后再试试。  
&emsp;&emsp;音频波形器占用CPU比较高，建议只在需要调试时临时打开。  
&emsp;&emsp;系统自带H264编解码器需要Android 5.0（API 21）及以上系统，且在某些Android设备上使用可能会花屏，这种情况只能使用OpenH264编解码器。  
&emsp;&emsp;保存音视频输入输出的AdoVdoInptOtpt.avi文件不能直接播放，需要使用FFmpeg命令转码后才能播放，建议用VLC播放器，转码命令为：ffmpeg -i AdoVdoInptOtpt.avi -filter_complex "[0:<zero-width space>a:1][0:<zero-width space>a:2]amix=inputs=2:duration=max[aout]" -map [aout] -map 0:v -acodec pcm_s16le -vcodec copy AdoVdoInptOtpt_Mix.avi -y。

# 其他
&emsp;&emsp;本软件采用了Speex的1.2.1版本、SpeexDsp的1.2.1版本、WebRtc的2019年7月份版本、WebRtc的2024年8月份版本、OpenH264的2.5.0版本为基础，并进行了大量优化。  
&emsp;&emsp;讨论QQ群：511046632    欢迎大家参与测试和讨论！  
&emsp;&emsp;本人QQ号：280604597    赤勇玄心行天道  
&emsp;&emsp;本人微信：qq280604597    赤勇玄心行天道  
&emsp;&emsp;本人博客：http://www.cnblogs.com/gaoyaguo  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;https://blog.csdn.net/cyz7758520?type=blog  
&emsp;&emsp;Windows版源代码：https://github.com/cyz7758520/Windows_audio_talkback_demo_program  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;https://gitee.com/chen_yi_ze/Windows_audio_talkback_demo_program  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;https://gitcode.com/cyz77585201/Windows_audio_talkback_demo_program  
&emsp;&emsp;Android版源代码：https://github.com/cyz7758520/Android_audio_talkback_demo_program  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;https://gitee.com/chen_yi_ze/Android_audio_talkback_demo_program  
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;https://gitcode.com/cyz77585201/Android_audio_talkback_demo_program  

# 版权
&emsp;&emsp;Speex：https://gitlab.xiph.org/xiph/speex/-/blob/master/COPYING  
&emsp;&emsp;WebRtc：https://gitlab.com/webrtc-mirror/webrtc/-/blob/master/LICENSE  
&emsp;&emsp;RNNoise：https://gitlab.xiph.org/xiph/rnnoise/-/blob/master/COPYING  
&emsp;&emsp;OpenH264：https://github.com/cisco/openh264/blob/master/LICENSE  
&emsp;&emsp;LibYUV：https://github.com/lemenkov/libyuv/blob/master/LICENSE  
&emsp;&emsp;TinyXml2：https://github.com/leethomason/tinyxml2/blob/master/LICENSE.txt  

# 感谢
&emsp;&emsp;感谢 WELEN、善书、陈国福 对 Speex、WebRTC 的指点！  

# 函数
### MediaPocsThrd媒体处理线程类的回调成员函数
___
函数名称：UserInit  
功能说明：用户定义的初始化函数，在本线程刚启动时回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserDstoy  
功能说明：用户定义的销毁函数，在本线程退出时回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserPocs  
功能说明：用户定义的处理函数，在本线程运行时每隔1毫秒就回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserMsg  
功能说明：用户定义的消息函数，在接收用户消息时回调一次。  
参数说明：MsgTyp：\[输入\]，存放消息类型。  
&emsp;&emsp;&emsp;&emsp;&emsp;MsgArgPt：\[输入\]，存放消息参数的动态参数的指针。如果没有消息参数，则本参数为null。  
返回说明：本次消息处理结果。  
___
函数名称：UserDvcChg  
功能说明：用户定义的设备改变函数，在音频输入输出设备、视频输入设备改变时回调一次。  
参数说明：AdoInptOtptDvcInfoPt：\[输入\]，存放音频输入输出设备信息指针。如果音频输入输出设备没有改变，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptDvcInfoPt：\[输入\]，存放视频输入设备信息的指针。如果视频输入设备没有改变，则本参数为null。  
返回说明：无。  
___
函数名称：UserReadAdoVdoInptFrm  
功能说明：用户定义的读取音视频输入帧函数，在读取到一个音频输入帧或视频输入帧并处理完后回调一次。如果没有使用音频输入和视频输入，则本函数不会被回调。  
参数说明：AdoInptPcmSrcFrmPt：\[输入\]，存放音频输入Pcm格式原始帧的指针，就是未经过声学回音消除、噪音抑制、自动增益控制的音频帧。如果不使用音频输入，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoInptPcmRsltFrmPt：\[输入\]，存放音频输入Pcm格式结果帧的指针，就是已经过声学回音消除、噪音抑制、自动增益控制的音频帧。如果不使用音频输入，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoInptPcmFrmLenUnit：\[输入\]，存放音频输入Pcm格式帧的长度，单位为采样单元。如果不使用音频输入，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoInptPcmRsltFrmVoiceActSts：\[输入\]，存放音频输入Pcm格式结果帧的语音活动状态，为非0表示有语音活动，为0表示无语音活动。如果不使用音频输入，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoInptEncdRsltFrmPt：\[输入\]，存放音频输入已编码格式结果帧的指针。如果不使用音频输入，或音频输入编码器要使用Pcm原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoInptEncdRsltFrmLenByt：\[输入\]，存放音频输入已编码格式结果帧的长度，单位为字节。如果不使用音频输入，或音频输入编码器要使用Pcm原始数据，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoInptEncdRsltFrmIsNeedTrans：\[输入\]，存放音频输入已编码格式结果帧是否需要传输，为非0表示需要传输，为0表示不需要传输。如果不使用音频输入，或音频输入编码器要使用Pcm原始数据，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptNv21SrcFrmPt：\[输入\]，存放视频输入Nv21格式原始帧的指针。如果不使用视频输入，或本次没有读取到视频输入帧，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptNv21SrcFrmWidth：\[输入\]，存放视频输入Nv21格式原始帧的宽度，单位为像素。如果不使用视频输入，或本次没有读取到视频输入帧，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptNv21SrcFrmHeight：\[输入\]，存放视频输入Nv21格式原始帧的高度，单位为像素。如果不使用视频输入，或本次没有读取到视频输入帧，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptNv21SrcFrmLenByt：\[输入\]，存放视频输入Nv21格式原始帧的长度，单位为字节。如果不使用视频输入，或本次没有读取到视频输入帧，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptYu12RsltFrmPt：\[输入\]，存放视频输入Yu12格式结果帧的指针。如果不使用视频输入，或本次没有读取到视频输入帧，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptYu12RsltFrmWidth：\[输入\]，存放视频输入Yu12格式结果帧的宽度，单位为像素。如果不使用视频输入，或本次没有读取到视频输入帧，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptYu12RsltFrmHeight：\[输入\]，存放视频输入Yu12格式结果帧的高度，单位为像素。如果不使用视频输入，或本次没有读取到视频输入帧，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptYu12RsltFrmLenByt：\[输入\]，存放视频输入Yu12格式结果帧的长度，单位为字节。如果不使用视频输入，或本次没有读取到视频输入帧，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptEncdRsltFrmPt：\[输入\]，存放视频输入已编码格式结果帧的指针。如果不使用视频输入，或本次没有读取到视频输入帧，或视频输入编码器要使用Yu12原始数据，则本参数为为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptEncdRsltFrmLenByt：\[输入\]，存放视频输入已编码格式结果帧的长度，单位为字节。如果不使用视频输入，或本次没有读取到视频输入帧，或视频输入编码器要使用Yu12原始数据，则本参数无意义。  
返回说明：无。  
___
函数名称：UserWriteAdoOtptFrm  
功能说明：用户定义的写入音频输出帧函数，在需要写入一个音频输出帧时回调一次。如果不使用音频输出，则本函数不会被回调。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。  
参数说明：AdoOtptStrmIdx：\[输入\]，存放音频输出流索引。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptPcmSrcFrmPt：\[输出\]，存放音频输出Pcm格式原始帧的指针。如果音频输出解码器不使用Pcm原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptPcmFrmLenUnit：\[输入\]，存放音频输出Pcm格式帧的长度，单位为采样单元。如果音频输出解码器不使用Pcm原始数据，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptEncdSrcFrmPt：\[输出\]，存放音频输出已编码格式原始帧的指针。如果音频输出解码器要使用Pcm原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptEncdSrcFrmSzByt：\[输入\]，存放音频输出已编码格式原始帧的大小，单位为字节。如果音频输出解码器要使用Pcm原始数据，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptEncdSrcFrmLenBytPt：\[输出\]，存放音频输出已编码格式原始帧的长度的指针，单位为字节。如果音频输出解码器要使用Pcm原始数据，则本参数为null。  
返回说明：无。  
___
函数名称：UserGetAdoOtptFrm  
功能说明：用户定义的获取音频输出帧函数，在解码完一个已编码音频输出帧时回调一次。如果不使用音频输出，则本函数不会被回调。注意：本函数不是在媒体处理线程中执行的，而是在音频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音频输入输出帧不同步，从而导致声学回音消除失败。  
参数说明：AdoOtptStrmIdx：\[输入\]，存放音频输出流索引。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptPcmSrcFrmPt：\[输入\]，存放音频输出Pcm格式原始帧的指针。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptPcmFrmLenUnit：\[输入\]，存放音频输出Pcm格式帧的长度，单位为采样单元。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptEncdSrcFrmPt：\[输入\]，存放音频输出已编码格式原始帧的指针。如果音频输出解码器要使用Pcm原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;AdoOtptEncdSrcFrmLenByt：\[输入\]，存放音频输出已编码格式原始帧的长度，单位为字节。如果音频输出解码器要使用Pcm原始数据，则本参数无意义。  
返回说明：无。  
___
函数名称：UserWriteVdoOtptFrm  
功能说明：用户定义的写入视频输出帧函数，在可以显示一个视频输出帧时回调一次。如果不使用视频输出，则本函数不会被回调。注意：本函数不是在媒体处理线程中执行的，而是在视频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音视频输出帧不同步。  
参数说明：VdoOtptStrmIdx：\[输入\]，存放视频输出流索引。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptYu12SrcFrmPt：\[输出\]，存放视频输出Yu12格式原始帧的指针。如果视频输出解码器不使用Yu12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptYu12SrcFrmWidthPt：\[输出\]，存放视频输出Yu12格式原始帧宽度的指针，单位为像素。如果视频输出解码器不使用Yu12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptYu12SrcFrmHeightPt：\[输出\]，存放视频输出Yu12格式原始帧高度的指针，单位为像素。如果视频输出解码器不使用Yu12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptEncdSrcFrmPt：\[输出\]，存放视频输出已编码格式原始帧的指针。如果视频输出解码器要使用Yu12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptEncdSrcFrmSzByt：\[输入\]，存放视频输出已编码格式原始帧的大小，单位为字节。如果视频输出解码器要使用Yu12原始数据，则本参数无意义。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptEncdSrcFrmLenBytPt：\[输出\]，输入时，存放视频输出已编码格式原始帧的长度的指针，单位为字节。如果视频输出解码器要使用Yu12原始数据，则本参数为null。  
返回说明：无。  
___
函数名称：UserGetVdoOtptFrm  
功能说明：用户定义的获取视频输出帧函数，在解码完一个已编码视频输出帧时回调一次。如果不使用视频输出，则本函数不会被回调。注意：本函数不是在媒体处理线程中执行的，而是在视频输出线程中执行的，所以本函数应尽量在一瞬间完成执行，否则会导致音视频输出帧不同步。  
参数说明：VdoOtptStrmIdx：\[输入\]，存放视频输出流索引。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptYu12SrcFrmPt：\[输入\]，存放视频输出Yu12格式原始帧的指针。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptYu12SrcFrmWidth：\[输入\]，存放视频输出Yu12格式原始帧的宽度，单位为像素。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptYu12SrcFrmHeight：\[输入\]，存放视频输出Yu12格式原始帧的高度，单位为像素。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptEncdSrcFrmPt：\[输入\]，存放视频输出已编码格式原始帧的指针。如果视频输出解码器要使用Yu12原始数据，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoOtptEncdSrcFrmLenByt：\[输入\]，存放视频输出已编码格式原始帧的长度，单位为字节。如果视频输出解码器要使用Yu12原始数据，则本参数无意义。  
返回说明：无。  
___
### SrvrThrd服务端线程类的回调成员函数
___
函数名称：UserInit  
功能说明：用户定义的初始化函数，在本线程刚启动时回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserDstoy  
功能说明：用户定义的销毁函数，在本线程退出时回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserPocs  
功能说明：用户定义的处理函数，在本线程运行时每隔1毫秒就回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserMsg  
功能说明：用户定义的消息函数，在接收用户消息时回调一次。  
参数说明：MsgTyp：\[输入\]，存放消息类型。  
&emsp;&emsp;&emsp;&emsp;&emsp;MsgArgPt：\[输入\]，存放消息参数数组的指针。如果没有消息参数，则本参数为null。  
返回说明：本次消息处理结果。  
___
函数名称：UserShowLog  
功能说明：用户定义的显示日志函数。  
___
函数名称：UserShowToast  
功能说明：用户定义的显示Toast函数。  
___
函数名称：UserVibrate  
功能说明：用户定义的振动函数。  
___
函数名称：UserSrvrInit  
功能说明：用户定义的服务端初始化函数。  
___
函数名称：UserSrvrDstoy  
功能说明：用户定义的服务端销毁函数。  
___
函数名称：UserCnctInit  
功能说明：用户定义的连接初始化函数。  
___
函数名称：UserCnctDstoy  
功能说明：用户定义的连接销毁函数。  
___
函数名称：UserCnctSts  
功能说明：用户定义的连接状态函数。  
___
函数名称：UserCnctRmtTkbkMode  
功能说明：用户定义的连接远端对讲模式函数。  
___
函数名称：UserCnctTstNtwkDly  
功能说明：用户定义的连接测试网络延迟函数。  
___
### ClntMediaPocsThrd客户端媒体处理线程类的回调成员函数
___
函数名称：UserInit  
功能说明：用户定义的初始化函数，在本线程刚启动时回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserDstoy  
功能说明：用户定义的销毁函数，在本线程退出时回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserPocs  
功能说明：用户定义的处理函数，在本线程运行时每隔1毫秒就回调一次。  
参数说明：无。  
返回说明：无。  
___
函数名称：UserMsg  
功能说明：用户定义的消息函数，在接收用户消息时回调一次。  
参数说明：MsgTyp：\[输入\]，存放消息类型。  
&emsp;&emsp;&emsp;&emsp;&emsp;MsgArgPt：\[输入\]，存放消息参数数组的指针。如果没有消息参数，则本参数为null。  
返回说明：本次消息处理结果。  
___
函数名称：UserDvcChg  
功能说明：用户定义的设备改变函数，在音频输入输出设备、视频输入设备改变时回调一次。  
参数说明：AdoInptOtptDvcInfoPt：\[输入\]，存放音频输入输出设备信息指针。如果音频输入输出设备没有改变，则本参数为null。  
&emsp;&emsp;&emsp;&emsp;&emsp;VdoInptDvcInfoPt：\[输入\]，存放视频输入设备信息的指针。如果视频输入设备没有改变，则本参数为null。  
返回说明：无。  
___
函数名称：UserShowLog  
功能说明：用户定义的显示日志函数。  
___
函数名称：UserShowToast  
功能说明：用户定义的显示Toast函数。  
___
函数名称：UserVibrate  
功能说明：用户定义的振动函数。  
___
函数名称：UserTkbkClntCnctInit  
功能说明：用户定义的对讲客户端连接初始化函数。  
___
函数名称：UserTkbkClntCnctDstoy  
功能说明：用户定义的对讲客户端连接销毁函数。  
___
函数名称：UserTkbkClntCnctSts  
功能说明：用户定义的对讲客户端连接状态函数。  
___
函数名称：UserTkbkClntMyTkbkIdx  
功能说明：用户定义的对讲客户端我的对讲索引函数。  
___
函数名称：UserTkbkClntLclTkbkMode  
功能说明：用户定义的对讲客户端本端对讲模式函数。  
___
函数名称：UserTkbkClntTkbkInfoInit  
功能说明：用户定义的对讲客户端对讲信息初始化函数。  
___
函数名称：UserTkbkClntTkbkInfoDstoy  
功能说明：用户定义的对讲客户端对讲信息销毁函数。  
___
函数名称：UserTkbkClntTkbkInfoRmtTkbkMode  
功能说明：用户定义的对讲客户端对讲信息远端对讲模式函数。  
___
函数名称：UserTkbkClntTstNtwkDly  
功能说明：用户定义的对讲客户端测试网络延迟函数。  
___
函数名称：UserBdctClntInit  
功能说明：用户定义的广播客户端初始化函数。  
___
函数名称：UserBdctClntDstoy  
功能说明：用户定义的广播客户端销毁函数。  
___
函数名称：UserBdctClntCnctInit  
功能说明：用户定义的广播客户端连接初始化函数。  
___
函数名称：UserBdctClntCnctDstoy  
功能说明：用户定义的广播客户端连接销毁函数。  
___
函数名称：UserBdctClntCnctSts  
功能说明：用户定义的广播客户端连接状态函数。  
___
