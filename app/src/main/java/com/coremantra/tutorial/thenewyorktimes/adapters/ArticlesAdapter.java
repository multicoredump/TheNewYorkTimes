package com.coremantra.tutorial.thenewyorktimes.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.coremantra.tutorial.thenewyorktimes.R;
import com.coremantra.tutorial.thenewyorktimes.models.Doc;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by radhikak on 3/13/17.
 */

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ArticleViewHolder> {

    List<Doc> articles;

    public static class ArticleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.tvTitle) TextView tvTitle;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public ArticlesAdapter(List<Doc> articles) {
        this.articles = articles;
    }

    public void updateDate(List<Doc> articles) {
        this.articles = articles;
        notifyDataSetChanged();
    }

    @Override
    public ArticleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_article, null);
        return new ArticleViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(ArticleViewHolder holder, int position) {
        holder.tvTitle.setText(articles.get(position).getSnippet());
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }
}
