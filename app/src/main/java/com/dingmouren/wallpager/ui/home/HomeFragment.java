package com.dingmouren.wallpager.ui.home;

import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.dingmouren.wallpager.MyApplication;
import com.dingmouren.wallpager.R;
import com.dingmouren.wallpager.base.BaseFragment;
import com.dingmouren.wallpager.model.bean.UnsplashResult;
import com.dingmouren.wallpager.ui.home.dagger.DaggerHomeComponent;
import com.dingmouren.wallpager.ui.home.dagger.HomeModule;

import java.lang.ref.WeakReference;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;

/**
 * Created by dingmouren on 2017/5/2.
 */

public class HomeFragment extends BaseFragment implements HomeContract.View{

    private static final int DELAY_TIME_OUT = 1500;

    @BindView(R.id.recycler) RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh)SwipeRefreshLayout mSwipeRefreshLayout;

    private Handler mHandler;
    private DelayRunnable mDelayRunnable;
    @Inject
    HomeAdapter mHomeAdapter;
    @Inject
    HomePresenter mHomePresenter;


    @Override
    public void init() {
        mHandler = new Handler();
        mDelayRunnable = new DelayRunnable(this);
    }

    @Override
    public int requestLayout() {
        return R.layout.fragment_home;
    }

    @Override
    public void initView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MyApplication.sContext));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(mHomeAdapter);
    }

    @Override
    public void initInjector() {
        DaggerHomeComponent.builder()
                .homeModule(new HomeModule(this))
                .applicationComponent(((MyApplication)this.getActivity().getApplication()).getApplicationComponent())//这里依赖的是ApplicationComponent
                .build().inject(this);
    }

    @Override
    public void initListener() {
        mSwipeRefreshLayout.setOnRefreshListener(()->{
            HomeFragment.this.setRefresh(true);
            mHomePresenter.requestData();
        });
    }

    @Override
    public void initData() {
        setRefresh(true);
        mHomePresenter.requestData();
    }


    @Override
    public void setRefresh(boolean refresh) {
        if (refresh)
            mSwipeRefreshLayout.setRefreshing(true);
        else
            mHandler.postDelayed(mDelayRunnable,DELAY_TIME_OUT);
    }


    @Override
    public void setData(List<UnsplashResult> data) {
        mHomeAdapter.setList(data);
        mHomeAdapter.notifyDataSetChanged();
        setRefresh(false);
    }


    /**
     * 延时任务
     */
    public static class DelayRunnable implements Runnable{
       private WeakReference<HomeFragment> mHomeFragmentWeakReference;
       public DelayRunnable(HomeFragment fragment){
           this.mHomeFragmentWeakReference = new WeakReference<HomeFragment>(fragment);
       }
       @Override
       public void run() {
            HomeFragment homeFragment = mHomeFragmentWeakReference.get();
           if (homeFragment != null){
               homeFragment.mSwipeRefreshLayout.setRefreshing(false);
           }
       }
   }
}