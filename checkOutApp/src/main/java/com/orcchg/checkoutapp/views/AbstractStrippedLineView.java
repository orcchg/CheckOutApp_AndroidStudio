package com.orcchg.checkoutapp.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.utils.DrawUtility;

public abstract class AbstractStrippedLineView extends View {
  private static final String TAG = "AbstractStrippedLineView";
  
  protected int mID = -1;
  
  protected static final int defaultNumCells = 31;
  protected int defaultCellWidth = 80;  // in dp  (has no effect, see default dimension)
  protected int desiredWidth = defaultCellWidth * defaultNumCells;
  protected int desiredHeight = 80;  // in dp  (has no effect, see default dimension)
  protected static final int defaultCircleRadius = 16;
  
  /* Pixel values */
  protected static final int enabledCellStrokeMargin = 4;
  protected int cellWidth;
  protected int cellWidthClamp;
  protected int width;
  protected int half_cellWidth, quarter_cellWidth;
  protected int height;
  protected int half_height, quarter_height;
  protected float text_height, text_half_height;
  protected float medium_text_height, medium_text_half_height;
  protected float large_text_height, large_text_half_height;
  protected int mCircleRadius = defaultCircleRadius;
  
  protected int numCells = defaultNumCells;
  protected int touchCellNumber = 0, prevTouchCellNumber = 0;
  protected float[] verticalLines;
  
  protected Paint mLinesPaint;
  protected Paint mTextPaint;
  protected Paint mMediumTextPaint;
  protected Paint mLargeTextPaint;
  protected Paint mBgExhaustPaint;
  protected Paint mBgTillPaint;
  protected Paint mBgClickPaint;
  protected Paint mBgEnabledPaint;
  protected Paint mCirclePaint;
  
  public interface OnCellClickListener {
    public void onCellClick(int cellNumber, int lineID);
    public void onCellDoubleClick(int cellNumber, int lineID);
    public void onCellLongClick(int cellNumber, int lineID);
  }
  
  protected OnCellClickListener mListener;
  protected OnLongClickListener mLongClickListener;
  protected static final int SINGLE_TAP_TIMEOUT = 90;  // ms
  protected static final int DOUBLE_TAP_TIMEOUT = 200;  // ms
  protected long mUpTime, mDownTime;
  protected boolean mDownTouch = false;
  protected boolean isDoubleClick = false;
  protected boolean isLongClick = false;
  
  public void setOnCellClickListener(OnCellClickListener listener) {
    mListener = listener;
  }
  
  public AbstractStrippedLineView(Context context) {
    this(context, null, 0);
  }

  public AbstractStrippedLineView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.StrippedLineViewStyle);
  }
  
  public AbstractStrippedLineView(Context context, AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    
    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.StrippedLineView, defStyle, R.style.DefaultStrippedLineViewStyle);
    
    // Init paints
    mLinesPaint = new Paint();
    mLinesPaint.setColor(ta.getColor(R.styleable.StrippedLineView_color_line, R.attr.color_line));
    
    mTextPaint = new Paint();
    mTextPaint.setColor(ta.getColor(R.styleable.StrippedLineView_color_text, R.attr.color_text));
    mTextPaint.setStyle(Style.FILL);
    mTextPaint.setTextAlign(Paint.Align.CENTER);
    
    mMediumTextPaint = new Paint();
    mMediumTextPaint.setColor(ta.getColor(R.styleable.StrippedLineView_color_text, R.attr.color_text));
    mMediumTextPaint.setStyle(Style.FILL);
    mMediumTextPaint.setTextAlign(Paint.Align.CENTER);
    
    mLargeTextPaint = new Paint();
    mLargeTextPaint.setColor(ta.getColor(R.styleable.StrippedLineView_color_text, R.attr.color_text));
    mLargeTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
    mLargeTextPaint.setStyle(Style.FILL);
    mLargeTextPaint.setTextAlign(Paint.Align.CENTER);
    
    mBgExhaustPaint = new Paint();
    mBgExhaustPaint.setColor(ta.getColor(R.styleable.StrippedLineView_color_exhaust, R.attr.color_exhaust));
    mBgExhaustPaint.setStyle(Style.FILL);
    
    mBgTillPaint = new Paint();
    mBgTillPaint.setColor(ta.getColor(R.styleable.StrippedLineView_color_till, R.attr.color_till));
    mBgTillPaint.setStyle(Style.FILL);
    
    mBgClickPaint = new Paint();
    mBgClickPaint.setColor(ta.getColor(R.styleable.StrippedLineView_color_click, R.attr.color_click));
    mBgClickPaint.setStyle(Style.FILL);
    
    mBgEnabledPaint = new Paint();
    mBgEnabledPaint.setColor(ta.getColor(R.styleable.StrippedLineView_color_enabled, R.attr.color_enabled));
    mBgEnabledPaint.setStyle(Style.STROKE);
    mBgEnabledPaint.setStrokeWidth(getResources().getInteger(R.integer.default_enabled_stroke_width));
    
    mCirclePaint = new Paint();
    mCirclePaint.setColor(context.getResources().getColor(R.color.android_blue_5));
    mCirclePaint.setStyle(Style.FILL);
    mCirclePaint.setStrokeWidth(getResources().getInteger(R.integer.default_check_stroke_width));
    
    /* Initial dimensions */
    float displayDensity = getResources().getDisplayMetrics().density;
    float defaultCellWidth_default = getResources().getDimension(R.dimen.default_stripped_line_cell_width);
    defaultCellWidth = (int) (ta.getDimension(R.styleable.StrippedLineView_cell_width, defaultCellWidth_default) / displayDensity);
    
    float desiredHeight_default = getResources().getDimension(R.dimen.default_stripped_line_cell_height);
    desiredHeight = (int) (ta.getDimension(R.styleable.StrippedLineView_cell_height, desiredHeight_default) / displayDensity);
    
    numCells = ta.getInteger(R.styleable.StrippedLineView_num_cells, defaultNumCells);
    desiredWidth = defaultCellWidth * numCells;
    
    verticalLines = new float[numCells << 2];
    instantiateBlock(numCells);
    
    ta.recycle();
    
    mLongClickListener = new OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        isLongClick = true;
        if (mListener != null) {
          mListener.onCellLongClick(touchCellNumber, mID);
        }
        return true;
      }};
  }
  
  /* Virtual API */
  // --------------------------------------------------------------------------
  protected abstract void instantiateBlock(int numCells);
  protected abstract void setBlockRef(Object block);
  protected abstract int getBlockLength();
  protected abstract void setBlockValue(int index, Object value);
  protected abstract void setBlockFilled_1(int index, boolean isFilled);
  protected abstract void setBlockFilled_1(int index, int percentage);
  protected abstract void setBlockFilled_2(int index, boolean isFilled);
  protected abstract void setBlockFilled_2(int index, int percentage);
  protected abstract void setBlockSelected(int index, boolean isSelected);
  protected abstract void setBlockEnabled(int index, boolean isEnabled);
  protected abstract boolean isBlockEnabled(int index);
  protected abstract void setTextSize();
  protected abstract void setMediumTextSize();
  protected abstract void setLargeTextSize();
  
  /* Public API */
  // --------------------------------------------------------------------------
  public void setLineID(int id) {
    mID = id;
  }
  
  public void setBlock(Object block) {
    setBlockRef(block);
    numCells = getBlockLength();
    measure(cellWidth * numCells, height);
    invalidate();
  }
  
  public void setCellWidth(int res_id) {
    float displayDensity = getResources().getDisplayMetrics().density;
    defaultCellWidth = (int) (getResources().getDimension(res_id) / displayDensity);
    measure(defaultCellWidth * numCells, height);
    invalidate();
  }
  
  public void setExactCellWidth(int width) {
    float displayDensity = getResources().getDisplayMetrics().density;
    defaultCellWidth = (int) (width / displayDensity);
    measure(defaultCellWidth * numCells, height);
    invalidate();
  }
  
  public void setHeight(int res_id) {
    float displayDensity = getResources().getDisplayMetrics().density;
    desiredHeight = (int) (getResources().getDimension(res_id) / displayDensity);
    measure(width, desiredHeight);
    invalidate();
  }
  
  public void setCellValue(int index, Object value) {
    setBlockValue(index, value);
    invalidate(index * cellWidth, 0, (index + 1) * cellWidth, height);
  }
  
  public void setCellFilled_1(int index, boolean isFilled) {
    setBlockFilled_1(index, isFilled);
    invalidate(index * cellWidth, 0, (index + 1) * cellWidth, height);
  }
  
  public void setCellFilled_2(int index, boolean isFilled) {
    setBlockFilled_2(index, isFilled);
    invalidate(index * cellWidth, 0, (index + 1) * cellWidth, height);
  }
  
  public void setCellEnabled(int index, boolean isEnabled) {
    setBlockEnabled(index, isEnabled);
    invalidate(index * cellWidth, 0, (index + 1) * cellWidth, height);
  }
  
  public boolean isCellEnabled(int index) {
    return isBlockEnabled(index);
  }
  
  public void setTypeface(int style) {
    mTextPaint.setTypeface(Typeface.create(Typeface.DEFAULT, style));
  }
  
  public int getMeasuredCellWidth() { return cellWidth; }
  
  /* User gestures */
  // --------------------------------------------------------------------------
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
      // ----------------------------------------
      case MotionEvent.ACTION_UP:
        mUpTime = System.currentTimeMillis();
        if (mDownTouch) {
          performClick();
        }
        if (mListener != null) {
          if (!isDoubleClick && Math.abs(mUpTime - mDownTime) > SINGLE_TAP_TIMEOUT) {
//            if (!isLongClick) {
              mListener.onCellClick(touchCellNumber, mID);
//            }
          }
        }
        // intentional no break
      case MotionEvent.ACTION_CANCEL:  // i.e. scroll parent view
        mDownTime = 0;
        mDownTouch = false;
        isDoubleClick = false;
        setBlockSelected(touchCellNumber, false);
        invalidate(touchCellNumber * cellWidth, 0, (touchCellNumber + 1) * cellWidth, height);
        prevTouchCellNumber = touchCellNumber;
        touchCellNumber = 0;
        return true;
      
      // ----------------------------------------
      case MotionEvent.ACTION_DOWN:
        mDownTime = System.currentTimeMillis();
        mDownTouch = true;
        isLongClick = false;
        touchCellNumber = (int) Math.floor(event.getXPrecision() * event.getX() / cellWidth);
        setBlockSelected(touchCellNumber, true);
        invalidate(touchCellNumber * cellWidth, 0, (touchCellNumber + 1) * cellWidth, height);
        if (mListener != null) {
          if (touchCellNumber == prevTouchCellNumber && Math.abs(mUpTime - mDownTime) <= DOUBLE_TAP_TIMEOUT) {
            isDoubleClick = true;
            mListener.onCellDoubleClick(touchCellNumber, mID);
          }
        }
        prevTouchCellNumber = 0;
        return true;
        
      // ----------------------------------------
    }
    return true;
  };
  
  @Override
  public boolean performClick() {
    return super.performClick();
  }

  /* Private methods */
  // --------------------------------------------------------------------------
  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    canvas.drawLines(verticalLines, mLinesPaint);
    // must be implemented in subclasses
  }
  
  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);

    cellWidth = DrawUtility.dipToPixel(getContext(), defaultCellWidth);
    cellWidthClamp = cellWidth - 1;
    width = cellWidth * numCells;
    half_cellWidth = cellWidth / 2;
    quarter_cellWidth = cellWidth / 4;

    int desiredHeight_dp = DrawUtility.dipToPixel(getContext(), desiredHeight);
    if (heightMode == MeasureSpec.EXACTLY) {
      height = heightSize;
    } else if (heightMode == MeasureSpec.AT_MOST) {
      height = Math.min(desiredHeight_dp, heightSize);
    } else {
      height = desiredHeight_dp;
    }  
    half_height = height / 2;
    quarter_height = height / 4;
    
    setMeasuredDimension(width, height);
    
    // post work
    setTextSize();
    setMediumTextSize();
    setLargeTextSize();
    mCircleRadius = quarter_height;
    
    for (int c = 0; c + 1 < numCells; ++c) {
      verticalLines[c * 4 + 0] = cellWidth * (c + 1);
      verticalLines[c * 4 + 1] = 0;
      verticalLines[c * 4 + 2] = cellWidth * (c + 1);
      verticalLines[c * 4 + 3] = height;
    }
  }
}
