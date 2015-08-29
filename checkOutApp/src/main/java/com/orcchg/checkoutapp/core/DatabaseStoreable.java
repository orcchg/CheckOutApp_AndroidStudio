package com.orcchg.checkoutapp.core;

public interface DatabaseStoreable {
  public long store();
  public boolean delete();
  public ModifiedLabel getModifiedLabel();
  public void setModifiedLabel(final ModifiedLabel label);
}
