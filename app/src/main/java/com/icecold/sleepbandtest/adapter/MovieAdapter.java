package com.icecold.sleepbandtest.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.icecold.sleepbandtest.R;
import com.icecold.sleepbandtest.entity.Movie;

import java.util.List;

/**
 *
 * Created by icecold_laptop_2 on 2018/8/9.
 */

public class MovieAdapter extends BaseQuickAdapter<Movie,BaseViewHolder> {

    private final RequestManager requestManager;

    public MovieAdapter(Context context, int layoutResId, @Nullable List<Movie> data) {
        super(layoutResId, data);
        requestManager = Glide.with(context);
    }

    @Override
    protected void convert(BaseViewHolder helper, Movie item) {
        helper.setText(R.id.movie_title,item.getTitle());
        helper.setText(R.id.movie_sub_title,item.getOriginal_title());
        helper.setText(R.id.movie_time,"上映时间:"+item.getYear()+"年");
        requestManager.load(item.getImages().getSmall())
                .into((ImageView) helper.getView(R.id.movie_image));
    }
}
