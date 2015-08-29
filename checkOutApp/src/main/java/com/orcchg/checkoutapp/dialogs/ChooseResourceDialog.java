package com.orcchg.checkoutapp.dialogs;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;
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

import com.orcchg.checkoutapp.CheckOutPage;
import com.orcchg.checkoutapp.EntryPage;
import com.orcchg.checkoutapp.R;
import com.orcchg.checkoutapp.utils.OnFileSelectedListener;
import com.orcchg.checkoutapp.utils.OpenFileDialog;

class ChooseResourceDialog extends DialogFragment {
  private static final String TAG = "CheckOut_ChooseResourceDialog";
  private static final String bundleKey_callerPage = "bundleKey_callerPage";
  static final String out_bundleKey_resourceURL = "out_bundleKey_resourceURL";
  
  private @NamedItemDialog.CallerPage int mCallerPage;
  private String mErrorNameString;
  private EditText mResourceURLEditText;
  
  public static ChooseResourceDialog newInstance(final @NamedItemDialog.CallerPage int caller_page) {
    ChooseResourceDialog dialog = new ChooseResourceDialog();
    Bundle args = new Bundle();
    args.putInt(bundleKey_callerPage, caller_page);
    dialog.setArguments(args);
    return dialog;
  }
  
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setStyle(DialogFragment.STYLE_NO_FRAME, R.style.NamedItemDialogStyle);
    Bundle args = getArguments();
    mCallerPage = args.getInt(bundleKey_callerPage);
    mErrorNameString = getResources().getString(R.string.util_add_new_item_error_name);
  }
  
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    super.onCreateView(inflater, container, savedInstanceState);
    Log.d(TAG, "Create view of ChooseResourceDialog");
    View rootView = inflater.inflate(R.layout.choose_resource_dialog, container, false);
    
    mResourceURLEditText = (EditText) rootView.findViewById(R.id.choose_resource_dialog_resource_url_edittext);
    setPrefix("http://");
    ImageButton mWebButton = (ImageButton) rootView.findViewById(R.id.choose_resource_dialog_web_imagebutton);
    mWebButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setPrefix("http://");
      }
    });
    ImageButton mFileButton = (ImageButton) rootView.findViewById(R.id.choose_resource_dialog_file_imagebutton);
    mFileButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setPrefix("");
        showOpenFileDialog();
      }
    });
    ImageButton mMapButton = (ImageButton) rootView.findViewById(R.id.choose_resource_dialog_map_imagebutton);
    mMapButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setPrefix("geo:");
        // TODO: open Map
      }
    });
    
    Button mCancelButton = (Button) rootView.findViewById(R.id.choose_resource_dialog_CANCEL_button);
    mCancelButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        disposeDialog();
      }
    });
    Button mOKButton = (Button) rootView.findViewById(R.id.choose_resource_dialog_OK_button);
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
    String url = mResourceURLEditText.getText().toString();
    if (url.isEmpty()) {
      Log.d(TAG, "Resource URL is empty which is wrong");
      mResourceURLEditText.setError(mErrorNameString);
      return false;
    }

    Bundle bundle = new Bundle();
    bundle.putString(out_bundleKey_resourceURL, url);
    FragmentManager fm = getActivity().getSupportFragmentManager();
    String fromTag = "";
    switch (mCallerPage) {
      default:
      case NamedItemDialog.CHECKOUT_PAGE:
        fromTag = CheckOutPage.DIALOG_TAG;
        break;
      case NamedItemDialog.ENTRY_PAGE:
        fromTag = EntryPage.DIALOG_TAG;
        break;
    }
    NamedItemDialog dialog = (NamedItemDialog) (fm.findFragmentByTag(fromTag));
    dialog.receiveBundle(bundle);
    return true;
  }
  
  private void disposeDialog() {
    dismiss();
  }
  
  private void showOpenFileDialog() {
    OpenFileDialog dialogf =
        new OpenFileDialog(
            getActivity(),
            Environment.getExternalStorageDirectory().getPath(),
            null,
            new OnFileSelectedListener() {
              @Override
              public void onFileSelected(File f) {
                mResourceURLEditText.setText(f.getAbsolutePath());
              }
            });
    dialogf.show();
  }
  
  private void setPrefix(final String prefix) {
    mResourceURLEditText.setText(prefix);
    mResourceURLEditText.setSelection(prefix.length());
  }
}
