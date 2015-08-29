package com.orcchg.checkoutapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.items.StrippedWideLineBlock;
import com.orcchg.checkoutapp.utils.DrawUtility;

public class StrippedWideLineView extends AbstractStrippedLineView {
  private static final String TAG = "StrippedWideLineView";

  private StrippedWideLineBlock mWideBlock;
  private String mBlockString = "";
  private final int mTextFactor;

  public StrippedWideLineView(Context context) {
    this(context, null, 0);
  }
  
  public StrippedWideLineView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.StrippedLineViewStyle);
  }
  
  public StrippedWideLineView(Context context, AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    mTextFactor = getResources().getInteger(R.integer.default_text_factor);
  }
  
  /* Virtual API */
  // --------------------------------------------------------------------------
  @Override
  protected void instantiateBlock(int numCells) {
    mWideBlock = new StrippedWideLineBlock(numCells);
  }

  @Override
  protected void setBlockRef(Object block) {
    mWideBlock = (StrippedWideLineBlock) block;
  }

  @Override
  protected int getBlockLength() {
    return mWideBlock.getLength();
  }

  @Override
  protected void setBlockValue(int index, Object value) {
    mWideBlock.block[index] = (String) value;
  }

  @Override
  protected void setBlockFilled_1(int index, boolean isFilled) {
    mWideBlock.filled_percent_1[index] = isFilled ? 100 : 0;
  }

  @Override
  protected void setBlockFilled_1(int index, int percentage) {
    mWideBlock.filled_percent_1[index] = percentage;
  }

  @Override
  protected void setBlockFilled_2(int index, boolean isFilled) {
    mWideBlock.filled_percent_2[index] = isFilled ? 100 : 0;
  }

  @Override
  protected void setBlockFilled_2(int index, int percentage) {
    mWideBlock.filled_percent_2[index] = percentage;
  }
  
  @Override
  protected void setBlockSelected(int index, boolean isSelected) {
    mWideBlock.selected[index] = isSelected;
  }

  @Override
  protected void setBlockEnabled(int index, boolean isEnabled) {
    mWideBlock.enabled[index] = isEnabled;
  }

  @Override
  protected boolean isBlockEnabled(int index) {
    return mWideBlock.enabled[index];
  }
  
  @Override
  protected void setTextSize() {
    mTextPaint.setTextSize(DrawUtility.dipToPixel(getContext(), half_height / mTextFactor));
    text_height = mTextPaint.getTextSize();
    text_half_height = text_height / mTextFactor;
  }
  
  @Override
  protected void setMediumTextSize() {
    mMediumTextPaint.setTextSize(DrawUtility.spToPixel(getContext(), half_height / 1.5f));
    medium_text_height = mMediumTextPaint.getTextSize();
    medium_text_half_height = medium_text_height / mTextFactor;
  }
  
  @Override
  protected void setLargeTextSize() {
    mLargeTextPaint.setTextSize(DrawUtility.spToPixel(getContext(), half_height));
    large_text_height = mLargeTextPaint.getTextSize();
    large_text_half_height = large_text_height / mTextFactor;
  }

  /* Private methods */
  // --------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    float top = 0;
    float bottom = height;
    for (int b = 0; b < numCells; ++b) {
      float left = b * cellWidth;
      float right = (b + 1) * cellWidth;
      if (mWideBlock.filled_percent_1[b] > 0) {
        float percent_right_1 = left + (float) cellWidth / 100 * mWideBlock.filled_percent_1[b];
        canvas.drawRect(left, top, percent_right_1, bottom, mBgExhaustPaint);
      }
      if (mWideBlock.filled_percent_2[b] > 0) {
        float percent_right_2 = left + (float) cellWidth / 100 * mWideBlock.filled_percent_2[b];
        canvas.drawRect(left, top, percent_right_2, bottom, mBgTillPaint);
      }
      if (mWideBlock.selected[b]) { canvas.drawRect(left, top, right, bottom, mBgClickPaint); }
      if (mWideBlock.enabled[b]) { canvas.drawRect(left + enabledCellStrokeMargin, top + enabledCellStrokeMargin, right - enabledCellStrokeMargin, bottom - enabledCellStrokeMargin, mBgEnabledPaint); }
      mBlockString = (mWideBlock.block[b].isEmpty() || mWideBlock.block[b].charAt(0) == '0') ? "" : mWideBlock.block[b];
      canvas.drawText(mBlockString, half_cellWidth + left, half_height + text_half_height, mTextPaint);
    }
  }
}
