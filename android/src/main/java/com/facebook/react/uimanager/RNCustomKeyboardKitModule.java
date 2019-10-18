
package com.facebook.react.uimanager;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.facebook.react.ReactApplication;
import com.facebook.react.ReactRootView;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.uimanager.RootView;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.UIViewOperationQueue;
import com.facebook.react.views.textinput.ReactEditText;

public class RNCustomKeyboardKitModule extends ReactContextBaseJavaModule {

  private final ReactApplicationContext reactContext;

  private final int TAG_ID = 0xdeadbeaf;

  public RNCustomKeyboardKitModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  Handler handle = new Handler(Looper.getMainLooper());

  @Override
  public String getName() {
    return "CustomKeyboardKit";
  }

  private ReactEditText getEditById(int id) {
    // UIViewOperationQueue uii = this.getReactApplicationContext().getNativeModule(UIManagerModule.class).getUIImplementation().getUIViewOperationQueue();
    // return (ReactEditText) uii.getNativeViewHierarchyManager().resolveView(id);

    UIViewOperationQueue uii = null;
    ReactEditText edit = null;

    while (edit == null) {
      uii = this.getReactApplicationContext().getNativeModule(UIManagerModule.class).getUIImplementation().getUIViewOperationQueue();

      try {
        edit = (ReactEditText) uii.getNativeViewHierarchyManager().resolveView(id);
      } catch (IllegalViewOperationException e) {
        Log.e("CustomKeyboard", e.toString());
      }
    }

    return edit;
  }

  @ReactMethod
  public void install(final int tag, final String type) {
    UiThreadUtil.runOnUiThread(new Runnable() {
        @Override
        public void run() {
          final Activity activity = getCurrentActivity();
          final ReactEditText edit = getEditById(tag);

          if (edit == null) {
            return;
          }

          edit.setTag(TAG_ID, createCustomKeyboardKit(activity, tag, type));
          
          final View.OnFocusChangeListener prevListener = edit.getOnFocusChangeListener();

          edit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(final View v, boolean hasFocus) {
              // Keep the original onFocus prop of TextInput
              if (prevListener != null) {
                prevListener.onFocusChange(v, hasFocus);
              }
              if (hasFocus) {
                View keyboard = (View)edit.getTag(TAG_ID);
                if (keyboard.getParent() == null) {
                   activity.addContentView(keyboard, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                }
                UiThreadUtil.runOnUiThread(new Runnable() {
                  @Override
                  public void run() {
                    ((InputMethodManager) getReactApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
                  }
                });
              } else {
                View keyboard = (View)edit.getTag(TAG_ID);
                if (keyboard.getParent() != null) {
                  ((ViewGroup) keyboard.getParent()).removeView(keyboard);
                }
              }
            }
          });

          edit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View v) {
              View keyboard = (View)edit.getTag(TAG_ID);
              if (keyboard.getParent() == null) {
                activity.addContentView(keyboard, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
              }
              UiThreadUtil.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                  ((InputMethodManager) getReactApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
              });
            }
          });
        }
    });
  }

  ReactRootView rootView = null;

  private View createCustomKeyboardKit(Activity activity, int tag, String type) {
    RelativeLayout layout = new RelativeLayout(activity);
    rootView = new ReactRootView(this.getReactApplicationContext());
    rootView.setBackgroundColor(Color.WHITE);

    Bundle bundle = new Bundle();
    bundle.putInt("tag", tag);
    bundle.putString("type", type);
    rootView.startReactApplication(
            ((ReactApplication) activity.getApplication()).getReactNativeHost().getReactInstanceManager(),
            "CustomKeyboardKit",
            bundle);

    final float scale = activity.getResources().getDisplayMetrics().density;
    RelativeLayout.LayoutParams lParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.round(216*scale));
    lParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
    layout.addView(rootView, lParams);
    // activity.addContentView(layout, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    return layout;
  }

  @ReactMethod
  public void uninstall(final int tag) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }

        edit.setTag(TAG_ID, null);
      }
    });
  }

  @ReactMethod
  public void insertText(final int tag, final String text) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        edit.getText().replace(Math.min(start, end), Math.max(start, end),
                text, 0, text.length());

      }
    });
  }

  @ReactMethod
  public void setText(final int tag, final String text) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }

        edit.setText(text);

        // Set Cursor to the end
        edit.setSelection(text.length());
      }
    });
  }

  @ReactMethod
  public void getText(final int tag, final Promise promise) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }

        promise.resolve(edit.getText().toString());
      }
    });
  }

  @ReactMethod
  public void backSpace(final int tag) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        if (start != end) {
          edit.getText().delete(start, end);
        } else if (start > 0){
          edit.getText().delete(start - 1, end);
        }
      }
    });
  }

  @ReactMethod
  public void doDelete(final int tag) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        if (start != end) {
          edit.getText().delete(start, end);
        } else if (start > 0){
          edit.getText().delete(start, end+1);
        }
      }
    });
  }

  @ReactMethod
  public void moveLeft(final int tag) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        if (start != end) {
          edit.setSelection(start, start);
        } else {
          edit.setSelection(start - 1, start - 1);
        }
      }
    });
  }

  @ReactMethod
  public void moveRight(final int tag) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }

        int start = Math.max(edit.getSelectionStart(), 0);
        int end = Math.max(edit.getSelectionEnd(), 0);
        if (start != end) {
          edit.setSelection(end, end);
        } else if (start > 0){
          edit.setSelection(end + 1, end + 1);
        }
      }
    });
  }

  @ReactMethod
  public void switchSystemKeyboard(final int tag) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final Activity activity = getCurrentActivity();
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }

        View keyboard = (View)edit.getTag(TAG_ID);
        if (keyboard.getParent() != null) {
          ((ViewGroup) keyboard.getParent()).removeView(keyboard);
        }
        UiThreadUtil.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            ((InputMethodManager) getReactApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).showSoftInput(edit, InputMethodManager.SHOW_IMPLICIT);
          }
        });
      }
    });
  }

  @ReactMethod
  public void hideKeyboard(final int tag) {
    UiThreadUtil.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        final ReactEditText edit = getEditById(tag);
        if (edit == null) {
          return;
        }
        
        View keyboard = (View)edit.getTag(TAG_ID);
        if (keyboard.getParent() != null) {
          ((ViewGroup) keyboard.getParent()).removeView(keyboard);
        }
      }
    });
  }

  @ReactMethod
  public  void hideStandardKeyboard(final int tag) {
    final ReactEditText edit = getEditById(tag);
    ((InputMethodManager) getReactApplicationContext().getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(edit.getWindowToken(), 0);
  }
}
