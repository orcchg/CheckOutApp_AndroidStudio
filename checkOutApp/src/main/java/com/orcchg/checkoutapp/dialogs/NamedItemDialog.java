package com.orcchg.checkoutapp.dialogs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.utils.RequestCode;

public class NamedItemDialog extends DialogFragment {
  private static final String TAG = "CheckOut_NamedItemDialog";
  private static final String DIALOG_TAG = "NamedItemDialog_dialog";
  private static final String bundleKey_initText = "bundleKey_initText";
  private static final String bundleKey_initResourceURL = "bundleKey_initResourceURL";
  public static final String out_bundleKey_name = "out_bundleKey_name";
  public static final String out_bundleKey_resourceURL = "out_bundleKey_resourceURL";
  public static final int RESULT_CODE = RequestCode.NAMED_ITEM_DIALOG;
  
  public static final int CHECKOUT_PAGE = 0;
  public static final int ENTRY_PAGE = 1;
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({ CHECKOUT_PAGE, ENTRY_PAGE })
  public @interface CallerPage {}
//  public static enum CallerPage {
//    CHECKOUT_PAGE(0), ENTRY_PAGE(1);
//    
//    private int value;
//    
//    CallerPage(int value) {
//      this.value = value;
//    }
//    
//    int getValue() { return value; }
//    
//    static CallerPage fromInt(int value) {
//      switch (value) {
//        default:
//        case 0: return CHECKOUT_PAGE;
//        case 1: return ENTRY_PAGE;
//      }
//    }
//  }
  
  private String mInitText;
  private String mErrorNameString;
  private String mResourceURL;
  private @CallerPage int mCallerPage;
  
  private TextView mResourceURLTextView;
  private EditText mNameEditText;
  
  public static NamedItemDialog newInstance() {
    return newInstance("", "");
  }
  
  public static NamedItemDialog newInstance(
      final String initText,
      final String initResourceURL) {
    NamedItemDialog dialog = new NamedItemDialog();
    Bundle args = new Bundle();
    args.putString(bundleKey_initText, initText);
    args.putString(bundleKey_initResourceURL, initResourceURL);
    dialog.setArguments(args);
    return dialog;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_FRAME, R.style.NamedItemDialogStyle);
    mErrorNameString = getResources().getString(R.string.util_add_new_item_error_name);
    Bundle args = getArguments();
    mInitText = args.getString(bundleKey_initText);
    mResourceURL = args.getString(bundleKey_initResourceURL);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of NamedItemDialog");
    View rootView = inflater.inflate(R.layout.named_item_dialog, container, false);
    
    TextView mTitleTextView = (TextView) rootView.findViewById(R.id.named_item_dialog_title_textview);
    mTitleTextView.setText(getResources().getString(R.string.util_new_entry_dialog_title));
    mNameEditText = (EditText) rootView.findViewById(R.id.named_item_dialog_item_name_edittext);
    mNameEditText.setText(mInitText);
    mNameEditText.selectAll();
    mResourceURLTextView = (TextView) rootView.findViewById(R.id.named_item_dialog_resource_url_textview);
    mResourceURLTextView.setText(mResourceURL);
    ImageButton mResourceButton = (ImageButton) rootView.findViewById(R.id.named_item_dialog_resource_imagebutton);
    mResourceButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        showResourceDialog();
      }
    });
    
    // Disabled resource button
//    mResourceButton.setVisibility(View.GONE);
//    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//    mNameEditText.setLayoutParams(params);
    
    Button mCancelButton = (Button) rootView.findViewById(R.id.named_item_dialog_CANCEL_button);
    mCancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        disposeDialog();
      }
    });
    Button mOKButton = (Button) rootView.findViewById(R.id.named_item_dialog_OK_button);
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
  
  public void setCallerPage(final @CallerPage int caller_page) {
    mCallerPage = caller_page;
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
    intent.putExtra(out_bundleKey_resourceURL, mResourceURL);
    getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CODE, intent);
    return true;
  }
  
  private void disposeDialog() {
    dismiss();
  }
  
  private void showResourceDialog() {
    FragmentManager fm = getActivity().getSupportFragmentManager();
    DialogFragment dialog = ChooseResourceDialog.newInstance(mCallerPage);
    dialog.show(fm, DIALOG_TAG);
  }
  
  void receiveBundle(final Bundle bundle) {
    mResourceURL = bundle.getString(ChooseResourceDialog.out_bundleKey_resourceURL);
    mResourceURLTextView.setText(mResourceURL);
    Log.d(TAG, "Received resource URL: " + mResourceURL);
  }
}
