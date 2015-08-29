package com.orcchg.checkoutapp.dialogs;

import com.orcchg.checkoutapp.CheckOutPage;
import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.core.Cache;
import com.orcchg.checkoutapp.items.Entry;
import com.orcchg.checkoutapp.utils.RequestCode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChangeEntryDialog extends DialogFragment {
  private static final String TAG = "CheckOut_ChangeEntryDialog";
  public static final String out_bundleKey_name = "out_bundleKey_name";
  public static final String out_bundleKey_toDelete = "out_bundleKey_toDelete";
  public static final String out_bundleKey_entryIndex = "out_bundleKey_entryIndex";
  public static final int RESULT_CODE = RequestCode.CHANGE_ENTRY_DIALOG;
  
  private String mInitText;
  private int mEntryIndex;
  private String mErrorNameString;
  
  private EditText mNameEditText;
  private ImageButton mDeleteButton;
  
  public static ChangeEntryDialog newInstance() {
    ChangeEntryDialog dialog = new ChangeEntryDialog();
    return dialog;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_FRAME, R.style.NamedItemDialogStyle);
    mErrorNameString = getResources().getString(R.string.util_add_new_item_error_name);
    Bundle bundle = getArguments();
    if (bundle != null) {
      mEntryIndex = bundle.getInt(CheckOutPage.out_bundleKey_entryIndex);
      Entry entry = Cache.getInstance().getEntries().get(mEntryIndex);
      mInitText = entry.getName();
    }
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of ChangeEntryDialog");
    View rootView = inflater.inflate(R.layout.change_entry_dialog, container, false);
    
    TextView mTitleTextView = (TextView) rootView.findViewById(R.id.change_entry_dialog_title_textview);
    mTitleTextView.setText(getResources().getString(R.string.util_change_entry_dialog_title));
    mNameEditText = (EditText) rootView.findViewById(R.id.change_entry_dialog_item_name_edittext);
    mNameEditText.setText(mInitText);
    mNameEditText.selectAll();
    mDeleteButton = (ImageButton) rootView.findViewById(R.id.change_entry_dialog_delete_imagebutton);
    mDeleteButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        sendDelete();
        disposeDialog();
      }
    });
    
    Button mCancelButton = (Button) rootView.findViewById(R.id.change_entry_dialog_CANCEL_button);
    mCancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        disposeDialog();
      }
    });
    Button mOKButton = (Button) rootView.findViewById(R.id.change_entry_dialog_OK_button);
    mOKButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if (sendResult()) {
          disposeDialog();
        }
      }
    });
    
    return rootView;
  }
  
  /* Private methods */
  // --------------------------------------------------------------------------
  private boolean sendResult() {
    String name = mNameEditText.getText().toString();
    if (name.isEmpty()) {
      Log.d(TAG, "Name is empty which is wrong");
      mNameEditText.setError(mErrorNameString);
      return false;
    }

    Intent intent = new Intent();
    intent.putExtra(out_bundleKey_name, name);
    intent.putExtra(out_bundleKey_toDelete, false);
    intent.putExtra(out_bundleKey_entryIndex, mEntryIndex);
    getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CODE, intent);
    return true;
  }
  
  private void sendDelete() {
    Intent intent = new Intent();
    intent.putExtra(out_bundleKey_name, "");
    intent.putExtra(out_bundleKey_toDelete, true);
    intent.putExtra(out_bundleKey_entryIndex, mEntryIndex);
    getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CODE, intent);
  }
  
  private void disposeDialog() {
    dismiss();
  }
}
