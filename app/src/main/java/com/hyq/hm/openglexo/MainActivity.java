package com.hyq.hm.openglexo;

import android.graphics.SurfaceTexture;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoTimeListener;

public class MainActivity extends AppCompatActivity {

    private View videoPlayerView;//播放器 播放按钮View

    private TextureView textureView;//纹理 播放视频用

    private SimpleExoPlayer player;//播放器

    private Handler mainHandler;

    private boolean isPlayer = false;

    private EGLUtils mEglUtils;//EGL工具类
    private GLFramebuffer mFramebuffer;//滤镜代码，以及绑定和绘制的方法

    private String uri = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoPlayerView = findViewById(R.id.video_player);

        mainHandler = new Handler();

        textureView = findViewById(R.id.texture_view);
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                //初始化SurfaceTexture, 准备就绪
                init(new Surface(surface),uri);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                //SurfaceTexture改变大小时调用
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                //SurfaceTexture摧毁时调用
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                //SurfaceTexture更新时调用
            }
        });


    }
    public void init(Surface surface,String uri){
//        Uri url = Uri.parse(Environment.getExternalStorageDirectory().getAbsolutePath() +"/HMSDK/video/1531383835814.mp4");//本地指定视频
        Uri url = Uri.parse(uri);//网络视频地址
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();


        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);

        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);


        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, "ExoPlayerTime"), bandwidthMeter);


        MediaSource videoSource = new ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(url, mainHandler,null);

        player.addVideoTiemListener(new VideoTimeListener() {
            @Override
            public Surface onSurface(Surface surface,int width,int height) {
                mEglUtils = new EGLUtils();
                mEglUtils.initEGL(surface);
                mFramebuffer = new GLFramebuffer();//滤镜对象
                mFramebuffer.initFramebuffer(textureView.getWidth(),
                        textureView.getHeight(),
                        width,
                        height);

                return new Surface(mFramebuffer.getSurfaceTexture());
            }

            @Override
            public void onVideoTimeChanged(long time) {//每一帧调用一次
                mFramebuffer.drawFrame();

                mEglUtils.swap();
            }

            @Override
            public void onRelease() {
                if(mEglUtils != null){
                    mEglUtils.release();
                }

            }
        });
        player.setVideoSurface(surface);
        player.prepare(videoSource);
    }

    public void playVideo(View view){
        if(player.getContentPosition() >= player.getDuration()){
            player.seekTo(0);
        }
        player.setPlayWhenReady(true);
        videoPlayerView.setVisibility(View.INVISIBLE);
        isPlayEnd();
    }
    private Handler seekBarHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(player.getPlayWhenReady() && player.getContentPosition() < player.getDuration()){
                isPlayEnd();
            }else{
                if(!isPlayer){
                    player.setPlayWhenReady(false);
                    videoPlayerView.setVisibility(View.VISIBLE);
                }
            }
        }
    };
    private void isPlayEnd(){
        seekBarHandler.removeMessages(100);
        Message message = seekBarHandler.obtainMessage();
        message.what = 100;
        seekBarHandler.sendMessageDelayed(message,100);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(player != null){
            if(isPlayer){
                player.setPlayWhenReady(true);
                isPlayer = false;
                isPlayEnd();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(player != null){
            if(player.getPlayWhenReady()){
                player.setPlayWhenReady(false);
                isPlayer = true;
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(player != null){
            player.stop();
            player.release();
            player = null;
        }
    }
}
