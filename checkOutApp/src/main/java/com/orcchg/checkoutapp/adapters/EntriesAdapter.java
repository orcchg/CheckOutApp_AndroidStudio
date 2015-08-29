package com.orcchg.checkoutapp.adapters;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable.IShapeBuilder;
import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.items.Entry;
import com.orcchg.checkoutapp.utils.DrawUtility;

public class EntriesAdapter extends BaseAdapter {
  private static final String TAG = "CheckOut_EntriesAdapter";
  
  private final Context mContext;
  private final List<Entry> mEntries;
  private IShapeBuilder mDrawableConfig;
  private Drawable mCompoundDrawable;
  
  public EntriesAdapter(final Context context) {
    mContext = context;
    mEntries = new ArrayList<Entry>(50);
    initResources();
  }
  
  private static class ViewHolder {
    View mMainView;
    TextView mEntryTextView;
    ImageView mCheckOutsImageView;
  }
  
  @Override
  public int getCount() {
    return mEntries.size();
  }

  @Override
  public Object getItem(int position) {
    return mEntries.get(position);
  }

  @Override
  public long getItemId(int position) {
    return mEntries.get(position).getID();
  }
  
  public long[] getItemIds() {
    long[] ids = new long[mEntries.size()];
    for (int i = 0; i < ids.length; ++i) {
      ids[i] = mEntries.get(i).getID();
    }
    return ids;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder holder = null;
    View rowView = convertView;
    LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    
    if (rowView == null) {
      rowView = inflater.inflate(R.layout.entries_adapter_row, parent, false);
      holder = new ViewHolder();
      holder.mMainView = rowView.findViewById(R.id.entries_adapter_main_layout);
      holder.mEntryTextView = (TextView) rowView.findViewById(R.id.entries_adapter_entry_textview);
      holder.mCheckOutsImageView = (ImageView) rowView.findViewById(R.id.entries_adapter_checkouts_imageview);
      rowView.setTag(holder);
    } else {
      holder = (ViewHolder) rowView.getTag();
    }
    
    Entry entry = mEntries.get(position);
    holder.mEntryTextView.setText(entry.getName());
    if (!entry.getResourceURL().isEmpty()) {
      holder.mEntryTextView.setCompoundDrawablesWithIntrinsicBounds(mCompoundDrawable, null, null, null);
    }
    DrawUtility.setImageViewDesign(mDrawableConfig, holder.mCheckOutsImageView, Integer.toString(entry.getSupplementaryValue()), Color.WHITE, DrawUtility.ImageViewShape.ROUND);
    if (entry.isArchived()) {
      holder.mMainView.setBackgroundColor(Color.LTGRAY);
    }
    return rowView;
  }

  public void add(final Entry entry) { mEntries.add(entry); }
  public void addAll(final List<Entry> entries) { mEntries.addAll(entries); }
  public void remove(int position) { mEntries.remove(position); }
  
  private void initResources() {
    mDrawableConfig = DrawUtility.initDrawableConfig(Color.BLACK, mContext.getResources().getInteger(R.integer.default_checkouts_imageview_text_drawable_size));
    Bitmap bmp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_link_black_18dp);
    mCompoundDrawable = new BitmapDrawable(mContext.getResources(), bmp);
  }
}
