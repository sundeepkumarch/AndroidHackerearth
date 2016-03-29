package com.hackerearth.mrsun.cube26;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.DataHolder>{

    Context context;



    public static List<PGObject> dataList = new ArrayList<>();
    private static String LOG_TAG = "MainRecyclerViewAdapter";

    MainRecyclerViewAdapter(){}
    public MainRecyclerViewAdapter(List<PGObject> dataList, Context context) {
        Log.d(LOG_TAG,"Adapter Constructor - "+ dataList.size());
        this.dataList = dataList;
        this.context = context;
    }

    public static class DataHolder extends RecyclerView.ViewHolder{

        ImageView image;
        TextView title;
        TextView rating;
        TextView setupFee;
        RelativeLayout rl;

        public DataHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.image);
            title = (TextView) itemView.findViewById(R.id.title);
            rating = (TextView) itemView.findViewById(R.id.rating);
            setupFee = (TextView) itemView.findViewById(R.id.setupfee);
            rl = (RelativeLayout) itemView.findViewById(R.id.rl);
            Log.d(LOG_TAG,"Views Initialized");
        }


    }

    @Override
    public MainRecyclerViewAdapter.DataHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.gateway_item,parent,false);

        return new DataHolder(view);
    }

    @Override
    public void onBindViewHolder(MainRecyclerViewAdapter.DataHolder holder, final int position) {
        holder.image.setImageResource(R.mipmap.ic_launcher);
        Log.d(LOG_TAG, "Calling Image downloading task");
        new DownloadImageTask(holder.image).execute(dataList.get(position).getImage());
        holder.title.setText(dataList.get(position).getName());
        holder.rating.setText("Rating: " + dataList.get(position).getRating());
        holder.setupFee.setText("Setup Fee: "+dataList.get(position).getSetup_fee());
        holder.rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("name",dataList.get(position).getName());
                intent.putExtra("description",dataList.get(position).getDescription());
                intent.putExtra("branding",dataList.get(position).getBranding());
                intent.putExtra("currencies",dataList.get(position).getCurrencies());
                intent.putExtra("setupfee",dataList.get(position).getSetup_fee());
                intent.putExtra("transactionfee",dataList.get(position).getTransaction_fees());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    public void animateTo(List<PGObject> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<PGObject> newModels) {
        for (int i = dataList.size() - 1; i >= 0; i--) {
            final PGObject model = dataList.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<PGObject> newModels) {
        for (int i = 0, count = newModels.size(); i < count; i++) {
            final PGObject model = newModels.get(i);
            if (!dataList.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<PGObject> newModels) {
        for (int toPosition = newModels.size() - 1; toPosition >= 0; toPosition--) {
            final PGObject model = newModels.get(toPosition);
            final int fromPosition = dataList.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    public PGObject removeItem(int position) {
        final PGObject model = dataList.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    public void addItem(int position, PGObject model) {
        dataList.add(position, model);
        notifyItemInserted(position);
    }

    public void moveItem(int fromPosition, int toPosition) {
        final PGObject model = dataList.remove(fromPosition);
        dataList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap>{

        ImageView mImageView;

        public DownloadImageTask(ImageView mImageView) {
            this.mImageView = mImageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {

            String urldisplay = urls[0];
            Bitmap mIcon = null;
            try {
                Log.d(LOG_TAG,"Downloading image...");
                InputStream in = new URL(urldisplay).openStream();
                mIcon = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon;
        }
        protected void onPostExecute(Bitmap result) {
            Log.d(LOG_TAG, "Setting image to view");
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            Log.d(LOG_TAG, "X:"+size.x+" Y:"+size.y);
            Bitmap b = Bitmap.createScaledBitmap(result,size.x/8, 50, true);
            mImageView.setImageBitmap(b);
        }
    }
}
