package com.orcchg.checkoutapp.items;

import java.util.Arrays;

public class StrippedWideLineBlock {
  public final String[] block;
  public final int[] filled_percent_1;
  public final int[] filled_percent_2;
  public final boolean[] selected;
  public final boolean[] enabled;
  
  public StrippedWideLineBlock(int size) {
    block = new String[size];
    filled_percent_1 = new int[size];
    filled_percent_2 = new int[size];
    selected = new boolean[size];
    enabled = new boolean[size];
    
    Arrays.fill(block, "");
    Arrays.fill(filled_percent_1, 0);
    Arrays.fill(filled_percent_2, 0);
    Arrays.fill(selected, false);
    Arrays.fill(enabled, false);
  }
  
  public int getLength() {
    return block.length;
  }
  
  @Override
  public String toString() {
    return Arrays.toString(block);
  }
}
