package com.orcchg.checkoutapp.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.widget.ImageView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.amulyakhare.textdrawable.TextDrawable.IShapeBuilder;

public class DrawUtility {
  private static final String TAG = "CheckOut_DrawUtility";
  
  // Convert the dps to pixels, based on density scale
  public static int dipToPixel(Context context, float dip_value) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) (dip_value * scale + 0.5f);
  }
  
  // Convert the sp to pixels, based on density scale
  public static int spToPixel(Context context, float sp_value) {
    final float scale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) (sp_value * scale + 0.5f);
  }
  
  // Convert the pixels to dps, based on density scale
  public static int pixelToDip(Context context, float pixel_value) {
    final float scale = context.getResources().getDisplayMetrics().density;
    return (int) ((pixel_value - 0.5f) / scale);
  }
  
  // Convert the pixels to sp, based on density scale
  public static int pixelToSp(Context context, float pixel_value) {
    final float scale = context.getResources().getDisplayMetrics().scaledDensity;
    return (int) ((pixel_value - 0.5f) / scale);
  }
  
  public static boolean isTablet(Context context) {
    return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
  }
  
  public static enum ImageViewShape { RECT, ROUND, ROUND_RECT }
  
  public static IShapeBuilder initDrawableConfig(int color, int textSize) {
    IShapeBuilder drawable_config = TextDrawable.builder()
        .beginConfig()
          .textColor(color)
          .useFont(Typeface.DEFAULT)
          .fontSize(textSize) /* size in px */
        .endConfig();
    return drawable_config;
  }
  
  public static void setImageViewDesign(
      final IShapeBuilder drawable_config,
      final ImageView view,
      final String label,
      int color,
      ImageViewShape shape) {
    
    TextDrawable drawable = null;
    switch (shape) {
      case RECT:
        drawable = drawable_config.buildRect(label, color);
        break;
      case ROUND:
        drawable = drawable_config.buildRound(label, color);
        break;
      default:
      case ROUND_RECT:
        drawable = drawable_config.buildRoundRect(label, color, 12);
        break;
    }
    view.setImageDrawable(drawable);
  }
}
