package com.orcchg.checkoutapp.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;

import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.items.StrippedLineBlock;
import com.orcchg.checkoutapp.utils.DrawUtility;

public class StrippedLineView extends AbstractStrippedLineView {
  private static final String TAG = "StrippedLineView";

  protected StrippedLineBlock mBlock;
  protected String mBlockString = "";
  private final int mTextFactor;
  
  private Bitmap mCheckBitmap;
  private int bmpHalfWidth, bmpHalfHeight;
  
  public StrippedLineView(Context context) {
    this(context, null, 0);
  }

  public StrippedLineView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.StrippedLineViewStyle);
  }
  
  public StrippedLineView(Context context, AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    mCheckBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_done_black_48dp);
    bmpHalfWidth = mCheckBitmap.getWidth() / 2;
    bmpHalfHeight = mCheckBitmap.getHeight() / 2;
    mTextFactor = getResources().getInteger(R.integer.default_text_factor);
  }
  
  /* Virtual API */
  // --------------------------------------------------------------------------
  @Override
  protected void instantiateBlock(int numCells) {
    mBlock = new StrippedLineBlock(numCells);
  }

  @Override
  protected void setBlockRef(Object block) {
    mBlock = (StrippedLineBlock) block;
  }

  @Override
  protected int getBlockLength() {
    return mBlock.getLength();
  }

  @Override
  protected void setBlockValue(int index, Object value) {
    mBlock.block[index] = (Integer) value;
  }

  @Override
  protected void setBlockFilled_1(int index, boolean isFilled) {
    mBlock.filled_1[index] = isFilled;
  }

  @Override
  protected void setBlockFilled_1(int index, int percentage) {
    mBlock.filled_1[index] = percentage == 0 ? false : true;
  }

  @Override
  protected void setBlockFilled_2(int index, boolean isFilled) {
    mBlock.filled_2[index] = isFilled;
  }

  @Override
  protected void setBlockFilled_2(int index, int percentage) {
    mBlock.filled_2[index] = percentage == 0 ? false : true;
  }
  
  @Override
  protected void setBlockSelected(int index, boolean isSelected) {
    mBlock.selected[index] = isSelected;
  }

  @Override
  protected void setBlockEnabled(int index, boolean isEnabled) {
    mBlock.enabled[index] = isEnabled;
  }

  @Override
  protected boolean isBlockEnabled(int index) {
    return mBlock.enabled[index];
  }
  
  @Override
  protected void setTextSize() {
    mTextPaint.setTextSize(DrawUtility.spToPixel(getContext(), half_height / mTextFactor));
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
  private float[] checkLines = new float[8];  // 7 lines
  
  private void calculateCheckLines(float left) {
    float cx = half_cellWidth + left;
    float cy = half_height;
    float LINE_WIDTH = 8.0f;
    float LINE_WIDTH2 = 2 * LINE_WIDTH;
    
    checkLines[0] = cx - mCircleRadius * 1.5f + LINE_WIDTH;
    checkLines[1] = cy - LINE_WIDTH;
    checkLines[2] = cx;
    checkLines[3] = cy + mCircleRadius * 1.5f;
    
    checkLines[4] = cx + mCircleRadius * 1.5f - LINE_WIDTH;
    checkLines[5] = cy - LINE_WIDTH;
    checkLines[6] = cx;
    checkLines[7] = cy + mCircleRadius * 1.5f;
    
//    checkLines[0] = cx - mCircleRadius;
//    checkLines[1] = cy;
//    checkLines[2] = cx - mCircleRadius - LINE_WIDTH;
//    checkLines[3] = checkLines[1];
//    
//    checkLines[4] = cx + mCircleRadius - LINE_WIDTH;
//    checkLines[5] = cy - mCircleRadius * 0.5f - LINE_WIDTH;
//    checkLines[6] = cx + mCircleRadius;
//    checkLines[7] = checkLines[5];
//    
//    checkLines[8] = cx - LINE_WIDTH * 0.5f;
//    checkLines[9] = cy + mCircleRadius;
//    checkLines[10] = cx + LINE_WIDTH * 0.5f;
//    checkLines[11] = checkLines[9];
//    
//    checkLines[12] = checkLines[2];
//    checkLines[13] = checkLines[3];
//    checkLines[14] = checkLines[8];
//    checkLines[15] = checkLines[9];
//    
//    checkLines[16] = checkLines[0];
//    checkLines[17] = checkLines[1];
//    checkLines[18] = checkLines[8] + LINE_WIDTH * 0.5f;
//    checkLines[19] = checkLines[9] - LINE_WIDTH * 0.5f;
//    
//    checkLines[20] = checkLines[18];
//    checkLines[21] = checkLines[19];
//    checkLines[22] = checkLines[4];
//    checkLines[23] = checkLines[5];
//    
//    checkLines[24] = checkLines[10];
//    checkLines[25] = checkLines[11];
//    checkLines[26] = checkLines[6];
//    checkLines[27] = checkLines[7];
  }
  
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    float top = 0;
    float bottom = height;
    for (int b = 0; b < numCells; ++b) {
      float left = b * cellWidth;
      float right = (b + 1) * cellWidth;
      if (mBlock.filled_1[b]) { canvas.drawRect(left, top, right, bottom, mBgExhaustPaint); }
//      if (mBlock.filled_2[b]) { canvas.drawRect(left, top, right, bottom, mBgTillPaint); }
      if (mBlock.selected[b]) { canvas.drawRect(left, top, right, bottom, mBgClickPaint); }
      if (mBlock.enabled[b]) { canvas.drawRect(left + enabledCellStrokeMargin, top + enabledCellStrokeMargin, right - enabledCellStrokeMargin, bottom - enabledCellStrokeMargin, mBgEnabledPaint); }
      mBlockString = mBlock.block[b] > 0 ? Integer.toString(mBlock.block[b]) : "";
      if (!mBlockString.isEmpty()) { canvas.drawText(mBlockString, half_cellWidth + left, half_height + text_half_height, mTextPaint); }
      if (mBlock.block[b] < 0) {  // special case - draw picture
        canvas.drawCircle(left + half_cellWidth, half_height, mCircleRadius, mCirclePaint);
        canvas.drawBitmap(mCheckBitmap, left + half_cellWidth - bmpHalfWidth, half_height - bmpHalfHeight, null);
      }
    }
  }
}
