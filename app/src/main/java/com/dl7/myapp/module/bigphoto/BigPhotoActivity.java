package com.dl7.myapp.module.bigphoto;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.dl7.drag.DragSlopLayout;
import com.dl7.myapp.R;
import com.dl7.myapp.adapter.PhotoPagerAdapter;
import com.dl7.myapp.injector.components.DaggerBigPhotoComponent;
import com.dl7.myapp.injector.modules.BigPhotoModule;
import com.dl7.myapp.local.table.BeautyPhotoBean;
import com.dl7.myapp.module.base.BaseActivity;
import com.dl7.myapp.module.base.ILoadDataView;
import com.dl7.myapp.module.base.ILocalPresenter;
import com.dl7.myapp.utils.AnimateHelper;
import com.dl7.myapp.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class BigPhotoActivity extends BaseActivity<ILocalPresenter> implements ILoadDataView<List<BeautyPhotoBean>> {

    private static final String BIG_PHOTO_KEY = "BigPhotoKey";
    private static final String PHOTO_INDEX_KEY = "PhotoIndexKey";

    @BindView(R.id.vp_photo)
    ViewPager mVpPhoto;
    @BindView(R.id.iv_favorite)
    ImageView mIvFavorite;
    @BindView(R.id.iv_download)
    ImageView mIvDownload;
    @BindView(R.id.iv_praise)
    ImageView mIvPraise;
    @BindView(R.id.iv_share)
    ImageView mIvShare;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.drag_layout)
    DragSlopLayout mDragLayout;

    @Inject
    PhotoPagerAdapter mAdapter;
    private List<BeautyPhotoBean> mPhotoList;
    private int mIndex; // 初始索引
    private boolean mIsHideToolbar = false; // 是否隐藏 Toolbar
    private boolean mIsInteract = false;    // 是否和 ViewPager 联动
    private int mCurPosition;   // Adapter 当前位置

    public static void launch(Context context, ArrayList<BeautyPhotoBean> datas, int index) {
        Intent intent = new Intent(context, BigPhotoActivity.class);
        intent.putParcelableArrayListExtra(BIG_PHOTO_KEY, datas);
        intent.putExtra(PHOTO_INDEX_KEY, index);
        context.startActivity(intent);
    }

    @Override
    protected boolean isSystemBarTranslucent() {
        return true;
    }

    @Override
    protected int attachLayoutRes() {
        return R.layout.activity_big_photo;
    }

    @Override
    protected void initInjector() {
        mPhotoList = getIntent().getParcelableArrayListExtra(BIG_PHOTO_KEY);
        mIndex = getIntent().getIntExtra(PHOTO_INDEX_KEY, 0);
        DaggerBigPhotoComponent.builder()
                .applicationComponent(getAppComponent())
                .bigPhotoModule(new BigPhotoModule(this, mPhotoList))
                .build()
                .inject(this);
    }

    @Override
    protected void initViews() {
        initToolBar(mToolbar, true, "");
        mAdapter = new PhotoPagerAdapter(this);
        mVpPhoto.setAdapter(mAdapter);
        mDragLayout.interactWithViewPager(mIsInteract);
        mAdapter.setTapListener(new PhotoPagerAdapter.OnTapListener() {
            @Override
            public void onPhotoClick() {
                mIsHideToolbar = !mIsHideToolbar;
                if (mIsHideToolbar) {
                    mDragLayout.startOutAnim();
                    mToolbar.animate().translationY(-mToolbar.getBottom()).setDuration(300);
                } else {
                    mDragLayout.startInAnim();
                    mToolbar.animate().translationY(0).setDuration(300);
                }
            }
        });
        mAdapter.setLoadMoreListener(new PhotoPagerAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                mPresenter.getMoreData();
            }
        });
        mVpPhoto.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                mCurPosition = position;
                // 设置图标状态
                mIvFavorite.setSelected(mAdapter.isLoved(position));
                mIvDownload.setSelected(mAdapter.isDownload(position));
                mIvPraise.setSelected(mAdapter.isPraise(position));
            }
        });
    }

    @Override
    protected void updateViews() {
        mPresenter.getData();
    }

    @Override
    public void loadData(List<BeautyPhotoBean> data) {
        mAdapter.updateData(data);
        mVpPhoto.setCurrentItem(mIndex);
    }

    @Override
    public void loadMoreData(List<BeautyPhotoBean> data) {
        mAdapter.addData(data);
        mAdapter.startUpdate(mVpPhoto);
    }

    @Override
    public void loadNoData() {
    }

    @OnClick({R.id.iv_favorite, R.id.iv_download, R.id.iv_praise, R.id.iv_share})
    public void onClick(View view) {
        final boolean isSelected = !view.isSelected();
        view.setSelected(isSelected);
        switch (view.getId()) {
            case R.id.iv_favorite:
                mAdapter.getData(mCurPosition).setLove(isSelected);
                break;
            case R.id.iv_download:
                mAdapter.getData(mCurPosition).setDownload(isSelected);
                break;
            case R.id.iv_praise:
                mAdapter.getData(mCurPosition).setPraise(isSelected);
                break;
            case R.id.iv_share:
                ToastUtils.showToast("分享:功能没加(╯-╰)");
                break;
        }
        // 除分享外都做动画和数据库处理
        if (view.getId() != R.id.iv_share) {
            AnimateHelper.doHeartBeat(view, 500);
            if (isSelected) {
                mPresenter.insert(mAdapter.getData(mCurPosition));
            } else {
                mPresenter.delete(mAdapter.getData(mCurPosition));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_animate, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        item.setChecked(true);
        switch (item.getItemId()) {
            case R.id.slide_bottom:
                mDragLayout.setAnimatorMode(DragSlopLayout.SLIDE_BOTTOM);
                return true;
            case R.id.slide_left:
                mDragLayout.setAnimatorMode(DragSlopLayout.SLIDE_LEFT);
                return true;
            case R.id.slide_right:
                mDragLayout.setAnimatorMode(DragSlopLayout.SLIDE_RIGHT);
                return true;
            case R.id.slide_fade:
                mDragLayout.setAnimatorMode(DragSlopLayout.FADE);
                return true;
            case R.id.slide_flip_x:
                mDragLayout.setAnimatorMode(DragSlopLayout.FLIP_X);
                return true;
            case R.id.slide_flip_y:
                mDragLayout.setAnimatorMode(DragSlopLayout.FLIP_Y);
                return true;
            case R.id.slide_zoom:
                mDragLayout.setAnimatorMode(DragSlopLayout.ZOOM);
                return true;
            case R.id.slide_zoom_left:
                mDragLayout.setAnimatorMode(DragSlopLayout.ZOOM_LEFT);
                return true;
            case R.id.slide_zoom_right:
                mDragLayout.setAnimatorMode(DragSlopLayout.ZOOM_RIGHT);
                return true;

            case R.id.item_interact:
                mIsInteract = !mIsInteract;
                item.setChecked(mIsInteract);
                mDragLayout.interactWithViewPager(mIsInteract);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}