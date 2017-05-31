package com.pisces.basequickadapter;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.pisces.basequickadapter.example.MovieEntity;
import com.pisces.basequickadapter.example.MovieQuickAdapter;
import com.pisces.basequickadapter.quickadapter.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements Callback<MovieEntity> {
    SwipeRefreshLayout srl;
    RecyclerView rcv;
    List<MovieEntity.SubjectsBean> mBeanList;
    static final int START_PAGE = 1;//起始页第一页
    static final int PAGE_COUNT = 10;//每页有10条数据

    MovieQuickAdapter mQuickAdapter;
    LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        //init RecyclerView
        rcv = (RecyclerView) findViewById(R.id.rcv);
        mLinearLayoutManager = new LinearLayoutManager(this);
        rcv.setLayoutManager(mLinearLayoutManager);
        rcv.addOnScrollListener(new EndlessOnScrollListener(mLinearLayoutManager) {
            @Override
            protected void loadMore(int currentPage) {
                //getMovies(currentPage);
                if (currentPage == -1000)
                    Toast.makeText(MainActivity.this, "无更多数据", Toast.LENGTH_SHORT).show();
                else getMovies(currentPage);
            }
        });

        srl = (SwipeRefreshLayout) findViewById(R.id.srl);
        srl.setColorSchemeColors(Color.RED, Color.GREEN, Color.YELLOW);
        srl.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mBeanList.clear();
                        getMovies(START_PAGE);
                    }
                }, 2000);
            }
        });
        //init Adapter
        mBeanList = new ArrayList<>();
        mQuickAdapter = new MovieQuickAdapter(this, mBeanList);

        //set EmptyView
        mQuickAdapter.setEmptyView(R.layout.rcv_empty);

        //set OnItemClickListener
        mQuickAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Toasty.info(MainActivity.this, mBeanList.get(position).getTitle()).show();
            }
        });
        getMovies(START_PAGE);
    }

    private void getMovies(int page) {
        //1,11,21 (page-1)*PAGE_COUNT+1
        srl.setRefreshing(true);
        RetrofitManager.getService().getTopMovie((page - 1) * PAGE_COUNT + 1, PAGE_COUNT).enqueue(this);
    }

    @Override
    public void onResponse(Call<MovieEntity> call, Response<MovieEntity> response) {
        mBeanList.addAll(response.body().getSubjects());
        if (rcv.getAdapter() == null) {
            rcv.setAdapter(mQuickAdapter);
        } else {
            mQuickAdapter.notifyDataSetChanged();
        }
        srl.setRefreshing(false);
    }

    @Override
    public void onFailure(Call<MovieEntity> call, Throwable t) {
        srl.setRefreshing(false);
    }
}
