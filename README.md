# Android下音视频对讲演示程序
# 必读说明

# 简介
#### 本软件根据《道德经》为核心思想而设计，实现了两个设备之间通过TCP或UDP协议进行全双工实时音视频对讲，并做了以下增强处理：
#### 支持声学回音消除，通过本人自己设计的音频输入输出帧同步方法、自适应设置回音延迟方法、三重声学回音消除器，声学回音可以消除到99%以上，还可以消除同一房间回音，且收敛时间很短，无论网络如何抖动都可以消除。
#### 支持噪音抑制，对常见的底噪音、嘈杂的街道音、风吹音、等都有抑制效果。
#### 支持语音活动检测，只有在人说话时才发送网络数据，无人说话时不产生网络数据，从而降低噪音、降低网络流量。
#### 支持自动增益控制，当人说话声音较小时会自动增大音量，当人说话声音较大时会自动减小音量。
#### 支持音频编解码，对音频数据的压缩率在1~20%之间，且支持动态比特率，从而大幅度降低网络流量，还支持数据包丢失隐藏，当网络丢包率高达30%时，仍然可以进行对讲。
#### 支持视频编解码，支持指定比特率，最低到10KB/s仍然可以进行视频对讲，还支持横竖屏切换。
#### 支持音视频自适应抖动缓冲，当网络存在丢包、乱序、延时等抖动情况时，通过自适应调节缓冲深度来应对这些抖动。
#### 声学回音消除器效果对比：
#### ![image](https://user-images.githubusercontent.com/16315192/115977799-d0722980-a5ad-11eb-811e-92da4d4f32f9.png)

# 准备
#### 准备两台安装了Android 2.3及以上系统的设备（已适配到Android 10），其中一台设备作为客户端可以连接到另一台作为服务端的设备（可以用Ping工具测试，建议两台设备在同一局域网内），且两台设备都安装相同版本的本软件。

# 开始
#### 在一台设备上直接点击创建服务端，再在另一台设备上将IP地址改为服务端设备的IP地址，并点击连接服务端，即可开始对讲，在任意一端点击中断，即可中断对讲。
#### 设置按钮提供了各项功能的参数设置，绝大部分情况下都不需要修改，当然你也可以根据需要自行修改。
#### 特别注意：如果把两台设备放在同一房间里测试，有可能会出现啸叫、声音不完整、等问题，这是因为现在的麦克风都很灵敏了，一点小小的声音都会被录进去，两台设备会相互录音，导致软件无法正确识别回音，所以建议放在不同的房间里测试。

# 移植
#### 如果需要在自己的软件中使用本软件的音视频功能，只需要将HeavenTao.XXXX包和jniLibs文件夹下各个平台的动态库复制到自己的软件中，然后继承HeavenTao.Media.MediaProcThread类，实现UserInit、UserProcess、UserDestroy、UserReadAudioVideoInputFrame、UserWriteAudioOutputFrame、UserGetPcmAudioOutputFrame、UserWriteVideoOutputFrame、UserGetYU12VideoOutputFrame这八个函数，再在AndroidManifest.xml文件中添加android.permission.RECORD_AUDIO、android.permission.MODIFY_AUDIO_SETTINGS、android.permission.CAMERA权限即可。
#### 如果用户要在JNI层处理音视频帧，则可以将那八个函数继承为native函数，然后在JNI层实现即可。
#### 如果用户有不需要的部分功能，则只需要删除该功能对应的库文件即可，还可以进一步删除对应的类文件，并修改HeavenTao.Media.MediaProcThread类文件即可。
#### 普通免费功能包括：WebRtc定点版声学回音消除器、Speex预处理器的噪音抑制、WebRtc定点版噪音抑制器、WebRtc浮点版噪音抑制器、Speex预处理器的其他功能、Speex编解码器、本端TCP协议服务端套接字、本端TCP协议客户端套接字、本端UDP协议套接字。
#### 高级收费功能包括：Speex声学回音消除器、WebRtc浮点版声学回音消除器、SpeexWebRtc三重声学回音消除器、RNNoise噪音抑制器、OpenH264编解码器、自己设计的自适应抖动缓冲器。
#### Speex声学回音消除器对应：libFunc.so、libSpeexDsp.so、SpeexAec.java。
#### WebRtc定点版声学回音消除器对应：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcAecm.java。
#### WebRtc浮点版声学回音消除器对应：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcAec.java。
#### SpeexWebRtc三重声学回音消除器对应：libFunc.so、libSpeexDsp.so、libc++_shared.so、libWebRtc.so、SpeexWebRtcAec.java。
#### WebRtc定点版噪音抑制器对应：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcNsx.java。
#### WebRtc浮点版噪音抑制器对应：libFunc.so、libc++_shared.so、libWebRtc.so、WebRtcNs.java。
#### RNNoise噪音抑制器对应：libFunc.so、libc++_shared.so、libWebRtc.so、libRNNoise.so、RNNoise.java。
#### Speex预处理器对应：libFunc.so、libSpeexDsp.so、SpeexPproc.java。
#### Speex编解码器对应：libFunc.so、libSpeex.so、SpeexEncoder.java、SpeexDecoder.java。
#### Wave文件写入读取器对应：libFunc.so、libWaveFile.so、WaveFileReader.java、WaveFileWriter.java。
#### OpenH264编解码器对应：libFunc.so、libOpenH264.so、OpenH264Encoder.java、OpenH264Decoder.java。
#### 图片处理对应：libFunc.so、libLibYUV.so、LibYUV.java。
#### 音视频自适应抖动缓冲器对应：libFunc.so、libc++_shared.so、libAjb.so、AAjb.java、VAjb.java。
#### 本端TCP协议UDP协议套接字对应：libFunc.so、libSokt.so、TcpSrvrSokt.java、TcpClntSokt.java、UdpSokt.java。

# 注意
#### 不要在64位操作系统下使用32位动态库，或在32位操作系统下使用64位动态库，否则会导致意想不到的问题。
#### 不要对HeavenTao.XXXX包进行代码混淆，否则会导致意想不到的问题。
#### 从老版本更新到新版本时，类文件和库文件全部都要更新，不能只更新类文件或库文件，否则会导致意想不到的问题。
#### 如果要使用8000Hz采样频率时，最好不要使用RNNoise噪音抑制，因为它对8000Hz的声音抑制非常强烈。
#### 本软件不支持音乐，尤其是系统自带的噪音抑制器和RNNoise噪音抑制器可能对音乐的抑制非常强烈。
#### 某些Android设备的软硬件环境可能存在问题，从而可能会导致声学回音消除失败，这种情况必须要先解决这些问题。

# 其他
#### 本软件采用了Speex的1.2.0版本、SpeexDsp的1.2.0版本、WebRtc的2019年7月份版本、OpenH264的2021年3月份版本为基础，并进行了大量优化。
#### 讨论QQ群：511046632    欢迎大家参与测试和讨论！
#### 本人QQ号：280604597    赤勇玄心行天道
#### 本人博客：http://www.cnblogs.com/gaoyaguo
#### Windows版源代码：https://github.com/cyz7758520/Windows_audio_talkback_demo_program
#### Android版源代码：https://github.com/cyz7758520/Android_audio_talkback_demo_program

# 感谢
#### 感谢 WELEN、善书、陈国福 对 Speex、WebRTC 的指点！
