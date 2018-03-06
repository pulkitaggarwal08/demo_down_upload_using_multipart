package com.demo_down_upload_image.pulkit.adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.demo_down_upload_image.pulkit.R;
import com.demo_down_upload_image.pulkit.models.Images;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class ShowImageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Images> imagesList;
    private Context context;
    private Typeface fontAwesomeFont;
    private onClickListener onClickListener;

    public interface onClickListener {
        void onClickButton(int position, int view, Images images);
    }

    public ShowImageAdapter(Context context,List<Images> imagesList, onClickListener onClickListener) {
        this.imagesList = imagesList;
        this.context = context;
        this.onClickListener = onClickListener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new GalleryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_show_image, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final GalleryViewHolder holder = (GalleryViewHolder) viewHolder;

        String imageUrl = imagesList.get(position).getImagePath().toString();

        Glide.with(context)
                .load(imageUrl)
                .into(holder.iv_imageView);

//        Picasso.with(context)
//                .load(imageUrl)
//                .placeholder(R.drawable.back_image)
//                .error(R.drawable.back_image)
////                .resize(300,250)
//                .into(holder.iv_imageView);

        fontAwesomeFont = Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf");
        holder.tv_fa_cancel.setTypeface(fontAwesomeFont);

    }

    @Override
    public int getItemCount() {
        return imagesList.size();
    }

    class GalleryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView iv_imageView;
        private TextView tv_fa_cancel;

        public GalleryViewHolder(View itemView) {
            super(itemView);

            iv_imageView = (ImageView) itemView.findViewById(R.id.iv_imageView);
            tv_fa_cancel = (TextView) itemView.findViewById(R.id.tv_fa_cancel);

            tv_fa_cancel.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onClickListener.onClickButton(getLayoutPosition(), view.getId(), imagesList.get(getLayoutPosition()));
        }

    }
}
