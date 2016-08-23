package com.dl7.myapp.injector.modules;

import com.dl7.helperlibrary.adapter.BaseQuickAdapter;
import com.dl7.myapp.adapter.NewsListAdapter;
import com.dl7.myapp.injector.PerFragment;
import com.dl7.myapp.module.base.IBasePresenter;
import com.dl7.myapp.module.news.NewsListFragment;
import com.dl7.myapp.module.news.NewsListPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * Created by long on 2016/8/23.
 * 新闻列表 Module
 */
@Module
public class NewsListModule {

    private final NewsListFragment mNewsListView;
    private final int mNewsType;

    public NewsListModule(NewsListFragment view, int newsType) {
        this.mNewsListView = view;
        this.mNewsType = newsType;
    }

    @PerFragment
    @Provides
    public IBasePresenter providePresenter() {
        return new NewsListPresenter(mNewsListView, mNewsType);
    }

    @Provides
    public BaseQuickAdapter provideAdapter() {
        return new NewsListAdapter(mNewsListView.getContext());
    }
}