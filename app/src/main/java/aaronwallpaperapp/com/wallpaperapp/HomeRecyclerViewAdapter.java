package aaronwallpaperapp.com.wallpaperapp;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by aaronowen on 14/03/2018.
 */

public class HomeRecyclerViewAdapter extends  RecyclerView.Adapter<HomeRecyclerViewAdapter.HomeViewHolder>{
    //values
    private ArrayList<String>  mNames = new ArrayList<>();
    private ArrayList<String>  mTitle = new ArrayList<>();
    private ArrayList<String>  mImages = new ArrayList<>();
    private ArrayList<String>  mImageIds = new ArrayList<>();
    private ArrayList<String> mImageUser = new ArrayList<>();
    private ArrayList<String> mImageDescription = new ArrayList<>();
    private ArrayList<String> mImageUserId = new ArrayList<>();

    private Context mContext;


    public HomeRecyclerViewAdapter(ArrayList<String> mNames, ArrayList<String> mTitle,ArrayList<String> mImages,ArrayList<String> mImageIds,ArrayList<String> mImageUser,ArrayList<String> mImageDescription,ArrayList<String> mImageUserId, Context mContext) {
        //take passed values and set them into the array
        this.mNames = mNames;
        this.mTitle = mTitle;
        this.mImages = mImages;
        this.mImageIds = mImageIds;
        this.mImageUser = mImageUser;
        this.mImageDescription =mImageDescription;
        this.mImageUserId =mImageUserId;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_image_view,parent,false);
        HomeViewHolder holder = new HomeViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, final int position) {
        Glide.with(mContext)
                .asBitmap()
                .load(mImages.get(position))
                .into(holder.image);
        //pass values back and add on click with put extra data
        holder.homeParentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, PostActivity.class);
                intent.putExtra("image_url",mImages.get(position));
                intent.putExtra("image_name",mNames.get(position));
                intent.putExtra("image_Title",mTitle.get(position));
                intent.putExtra("image_Ids",mImageIds.get(position));
                intent.putExtra("image_User",mImageUser.get(position));
                intent.putExtra("image_Description",mImageDescription.get(position));
                intent.putExtra("image_UserId",mImageUserId.get(position));
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImages.size();
    }

    public class HomeViewHolder extends RecyclerView.ViewHolder{
        ImageView image;
        TextView text;
        RelativeLayout homeParentLayout;
        public HomeViewHolder(View itemView) {

            super(itemView);
            image = itemView.findViewById(R.id.homeImage);
            homeParentLayout = itemView.findViewById(R.id.homeParentLayout);
        }
    }
}
