package com.orcchg.checkoutapp.items;

import java.util.Arrays;

public class StrippedLineBlock {
  public final int[] block;
  public final boolean[] filled_1;
  public final boolean[] filled_2;
  public final boolean[] selected;
  public final boolean[] enabled;

  public StrippedLineBlock(int size) {
    block = new int[size];
    filled_1 = new boolean[size];
    filled_2 = new boolean[size];
    selected = new boolean[size];
    enabled = new boolean[size];
    
    Arrays.fill(block, 0);
    Arrays.fill(filled_1, false);
    Arrays.fill(filled_2, false);
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
