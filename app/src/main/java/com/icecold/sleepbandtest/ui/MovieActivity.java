package com.icecold.sleepbandtest.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.icecold.sleepbandtest.R;
import com.icecold.sleepbandtest.adapter.MovieAdapter;
import com.icecold.sleepbandtest.adapter.decoration.MovieDecoration;
import com.icecold.sleepbandtest.entity.Movie;
import com.icecold.sleepbandtest.network.DouBanLoader;
import com.vise.log.ViseLog;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class MovieActivity extends AppCompatActivity implements BaseQuickAdapter.RequestLoadMoreListener {

    private static final int MAX_PAGE = 24;
    @BindView(R.id.rv_list)
    RecyclerView mRecyclerView;
    @BindView(R.id.movie_toolbar)
    Toolbar movieToolbar;
    private MovieAdapter movieAdapter;
    private int page;
    private DouBanLoader douBanLoader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        ButterKnife.bind(this);
        initView();
    }

    private void initView() {
        douBanLoader = new DouBanLoader();
        movieToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //设置横向布局
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //加入分割线
        mRecyclerView.addItemDecoration(new MovieDecoration());
        movieAdapter = new MovieAdapter(this, R.layout.moive_item, null);
        //设置下拉加载更多监听
        movieAdapter.setOnLoadMoreListener(this, mRecyclerView);
        //打开加入删除动画
        movieAdapter.openLoadAnimation(BaseQuickAdapter.SLIDEIN_LEFT);
        mRecyclerView.setAdapter(movieAdapter);
        getMovieList();
    }

    private void getMovieList() {
        AndroidSchedulers.mainThread();
        douBanLoader.requestTop250Movies(0, 10).subscribe(new Consumer<List<Movie>>() {
            @Override
            public void accept(List<Movie> movies) throws Exception {
                page = 1;
                movieAdapter.addData(movies);
                movieAdapter.notifyDataSetChanged();
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {

                ViseLog.d("请求出错 errorMessage = " + throwable.getMessage());
            }
        });
    }

    @Override
    public void onLoadMoreRequested() {
        douBanLoader.requestTop250Movies(page, (page + 1) * 10).subscribe(new Consumer<List<Movie>>() {
            @Override
            public void accept(List<Movie> movies) throws Exception {
                page++;
                if (page <= MAX_PAGE) {
                    movieAdapter.addData(movies);
//                    movieAdapter.notifyDataSetChanged();
                    movieAdapter.loadMoreComplete();
                } else {
                    movieAdapter.loadMoreEnd();
                }
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                movieAdapter.loadMoreFail();
                ViseLog.d("请求出错 errorMessage = " + throwable.getMessage());
            }
        });

    }
}
