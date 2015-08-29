package com.orcchg.checkoutapp.adapters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.items.StrippedLineBlock;
import com.orcchg.checkoutapp.views.StrippedLineView;

public class StrippedLineAdapter extends BaseAdapter {
  private final Context mContext;
  private final List<StrippedLineBlock> mBlocks;
  private final StrippedLineView.OnCellClickListener mListener;
  
  private int mCellWidth_id;
  private int mHeight_id;
  private int mExactCellWidth;
  
  /* View holder */
  private static class ViewHolder {
    private StrippedLineView line;
  }
  
  public StrippedLineAdapter(Context context, StrippedLineView.OnCellClickListener listener) {
    super();
    mContext = context;
    mBlocks = new ArrayList<StrippedLineBlock>(100);
    mListener = listener;
    mCellWidth_id = R.dimen.default_stripped_line_cell_width;
    mHeight_id = R.dimen.default_stripped_line_cell_height;
    mExactCellWidth = 0;
  }
  
  @Override
  public int getCount() {
    return mBlocks.size();
  }

  @Override
  public Object getItem(int position) {
    return mBlocks.get(position);
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
      rowView = inflater.inflate(R.layout.stripped_lines_adaptor_row, parent, false);
      holder = new ViewHolder();
      holder.line = (StrippedLineView) rowView.findViewById(R.id.stripped_line_adaptor_view);
      rowView.setTag(holder);
    } else {
      holder = (ViewHolder) rowView.getTag();
    }
    
    holder.line.setLineID(position);
    holder.line.setBlock(mBlocks.get(position));
    if (mExactCellWidth == 0) {
      holder.line.setCellWidth(mCellWidth_id);
    } else {
      holder.line.setExactCellWidth(mExactCellWidth);
    }
    holder.line.setHeight(mHeight_id);
    holder.line.setOnCellClickListener(mListener);
    return rowView;
  }
  
  public void setCellWidth(int res_id) {
    mCellWidth_id = res_id;
    notifyDataSetChanged();
  }
  
  public void setExactCellWidth(int width) {
    mExactCellWidth = width;
    notifyDataSetChanged();
  }
  
  public void setHeight(int res_id) {
    mHeight_id = res_id;
    notifyDataSetChanged();
  }
  
  public void add(final StrippedLineBlock block) { mBlocks.add(block); }
  public void addAll(final List<StrippedLineBlock> blocks) { mBlocks.addAll(blocks); }
  public void addAll(final StrippedLineBlock[] blocks) { mBlocks.addAll(Arrays.asList(blocks)); }
  public void clear() { mBlocks.clear(); }
  public void remove(int position) { mBlocks.remove(position); }
}
