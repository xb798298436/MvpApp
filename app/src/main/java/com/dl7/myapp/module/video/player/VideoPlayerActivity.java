package com.dl7.myapp.module.video.player;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.dl7.downloaderlib.model.DownloadStatus;
import com.dl7.myapp.R;
import com.dl7.myapp.injector.components.DaggerVideoPlayerComponent;
import com.dl7.myapp.injector.modules.VideoPlayerModule;
import com.dl7.myapp.local.table.VideoBean;
import com.dl7.myapp.module.base.BaseActivity;
import com.dl7.myapp.module.base.ILoadDataView;
import com.dl7.myapp.module.base.ILocalPresenter;
import com.dl7.player.media.IjkPlayerView;
import com.sackcentury.shinebuttonlib.ShineButton;

import butterknife.BindView;
import butterknife.OnClick;

public class VideoPlayerActivity extends BaseActivity<ILocalPresenter> implements ILoadDataView<VideoBean> {

    private static final String VIDEO_DATA_KEY = "VideoPlayerKey";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.video_player)
    IjkPlayerView mPlayerView;
    @BindView(R.id.iv_video_share)
    ImageView mIvVideoShare;
    @BindView(R.id.iv_video_collect)
    ShineButton mIvVideoCollect;
    @BindView(R.id.iv_video_download)
    ImageView mIvVideoDownload;

    private VideoBean mVideoData;

    public static void launch(Context context, VideoBean data) {
        Intent intent = new Intent(context, VideoPlayerActivity.class);
        intent.putExtra(VIDEO_DATA_KEY, data);
        context.startActivity(intent);
    }

    @Override
    protected boolean isSystemBarTranslucent() {
        return true;
    }

    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_video_player;
    }

    @Override
    protected void initInjector() {
        mVideoData = getIntent().getParcelableExtra(VIDEO_DATA_KEY);
        DaggerVideoPlayerComponent.builder()
                .applicationComponent(getAppComponent())
                .videoPlayerModule(new VideoPlayerModule(this, mVideoData))
                .build()
                .inject(this);
    }

    @Override
    protected void initViews() {
        initToolBar(mToolbar, true, mVideoData.getTitle());
        mPlayerView.init()
                .setTitle(mVideoData.getTitle())
                .enableDanmaku()
                .setVideoSource(null, mVideoData.getM3u8_url(), mVideoData.getM3u8Hd_url(), null, null);
        mIvVideoCollect.init(this);
        mIvVideoCollect.setShapeResource(R.mipmap.video_collect);
        mIvVideoCollect.setOnCheckStateChangeListener(new ShineButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(View view, boolean checked) {
                // 这里直接点击就处理，通常是需要和服务器交互返回成功才处理的，但是这个库内部直接受理了点击事件，没法方便地
                // 来控制它，需要改代码
                mVideoData.setCollect(checked);
                if (mVideoData.isCollect()) {
                    mPresenter.insert(mVideoData);
                } else {
                    mPresenter.delete(mVideoData);
                }
            }
        });
    }

    @Override
    protected void updateViews() {
        Glide.with(this).load(mVideoData.getCover()).fitCenter().into(mPlayerView.mPlayerThumb);
        mPresenter.getData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPlayerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlayerView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayerView.onDestroy();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPlayerView.configurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mPlayerView.handleVolumeKey(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mPlayerView.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void loadData(VideoBean data) {
        mVideoData = data;
        mIvVideoCollect.setChecked(data.isCollect());
        if (data.getDownloadStatus() != DownloadStatus.NORMAL) {
            mIvVideoDownload.setSelected(true);
        }
    }

    @Override
    public void loadMoreData(VideoBean data) {
    }

    @Override
    public void loadNoData() {
    }

    @OnClick({R.id.iv_video_share, R.id.iv_video_download})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_video_share:
                break;
            case R.id.iv_video_download:
                break;
        }
    }
}