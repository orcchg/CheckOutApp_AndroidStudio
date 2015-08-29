package com.orcchg.checkoutapp.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable.IShapeBuilder;
import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.items.Summary;
import com.orcchg.checkoutapp.utils.DrawUtility;

public class SummariesAdapter extends BaseAdapter {
  private static final String TAG = "CheckOut_SummariesAdapter";
  
  private final Context mContext;
  private final List<Summary> mSummaries;
  private IShapeBuilder mDrawableConfig;
  
  public SummariesAdapter(final Context context) {
    mContext = context;
    mSummaries = new ArrayList<Summary>(50);
    initResources();
  }
  
  private static class ViewHolder {
    View mMainView;
    TextView mSummaryTextView;
    TextView mDateTextView;
    ImageView mLabelImageView;
  }
  
  @Override
  public boolean isEnabled(int position) {
    return false; // disable item clicks
  }
  
  @Override
  public int getCount() {
    return mSummaries.size();
  }
  @Override
  public Object getItem(int position) {
    return mSummaries.get(position);
  }
  
  @Override
  public long getItemId(int position) {
    return position;
  }
  
  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder = null;
    View rowView = convertView;
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    
    if (rowView == null) {
      rowView = inflater.inflate(R.layout.summaries_adapter_row, parent, false);
      holder = new ViewHolder();
      holder.mMainView = rowView.findViewById(R.id.summaries_adapter_main_layout);
      holder.mSummaryTextView = (TextView) rowView.findViewById(R.id.summaries_adapter_summary_textview);
      holder.mDateTextView = (TextView) rowView.findViewById(R.id.summaries_adapter_date_textview);
      holder.mLabelImageView = (ImageView) rowView.findViewById(R.id.summaries_adapter_label_imageview);
      rowView.setTag(holder);
    } else {
      holder = (ViewHolder) rowView.getTag();
    }
    
    Summary summary = mSummaries.get(position);
    holder.mSummaryTextView.setText(summary.getSummary(mContext));
    holder.mDateTextView.setText(summary.getFormattedDate());
    DrawUtility.setImageViewDesign(mDrawableConfig, holder.mLabelImageView, "?", Color.GREEN, DrawUtility.ImageViewShape.ROUND);
    if (summary.isArchived()) {
      holder.mMainView.setBackgroundColor(Color.LTGRAY);
    }
    return rowView;
  }
  
  public void add(final Summary summary) { mSummaries.add(summary); }
  public void addAll(final List<Summary> summaries) { mSummaries.addAll(summaries); }
  public void clear() { mSummaries.clear(); }
  public void remove(int position) { mSummaries.remove(position); }
  
  private void initResources() {
    mDrawableConfig = DrawUtility.initDrawableConfig(Color.BLACK, mContext.getResources().getInteger(R.integer.default_summaries_imageview_text_drawable_size));
  }
}
