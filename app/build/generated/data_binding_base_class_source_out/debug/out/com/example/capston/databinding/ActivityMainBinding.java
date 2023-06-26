// Generated by view binder compiler. Do not edit!
package com.example.capston.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.capston.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityMainBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final ImageView calendarButton;

  @NonNull
  public final ImageView friendButton;

  @NonNull
  public final ImageView mainButton;

  @NonNull
  public final FrameLayout mainFragment;

  @NonNull
  public final ImageView settingButton;

  @NonNull
  public final ImageView timeTableButton;

  @NonNull
  public final Toolbar toolbar2;

  private ActivityMainBinding(@NonNull ConstraintLayout rootView, @NonNull ImageView calendarButton,
      @NonNull ImageView friendButton, @NonNull ImageView mainButton,
      @NonNull FrameLayout mainFragment, @NonNull ImageView settingButton,
      @NonNull ImageView timeTableButton, @NonNull Toolbar toolbar2) {
    this.rootView = rootView;
    this.calendarButton = calendarButton;
    this.friendButton = friendButton;
    this.mainButton = mainButton;
    this.mainFragment = mainFragment;
    this.settingButton = settingButton;
    this.timeTableButton = timeTableButton;
    this.toolbar2 = toolbar2;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityMainBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_main, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityMainBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.calendar_button;
      ImageView calendarButton = ViewBindings.findChildViewById(rootView, id);
      if (calendarButton == null) {
        break missingId;
      }

      id = R.id.friend_button;
      ImageView friendButton = ViewBindings.findChildViewById(rootView, id);
      if (friendButton == null) {
        break missingId;
      }

      id = R.id.main_button;
      ImageView mainButton = ViewBindings.findChildViewById(rootView, id);
      if (mainButton == null) {
        break missingId;
      }

      id = R.id.mainFragment;
      FrameLayout mainFragment = ViewBindings.findChildViewById(rootView, id);
      if (mainFragment == null) {
        break missingId;
      }

      id = R.id.setting_button;
      ImageView settingButton = ViewBindings.findChildViewById(rootView, id);
      if (settingButton == null) {
        break missingId;
      }

      id = R.id.timeTable_button;
      ImageView timeTableButton = ViewBindings.findChildViewById(rootView, id);
      if (timeTableButton == null) {
        break missingId;
      }

      id = R.id.toolbar2;
      Toolbar toolbar2 = ViewBindings.findChildViewById(rootView, id);
      if (toolbar2 == null) {
        break missingId;
      }

      return new ActivityMainBinding((ConstraintLayout) rootView, calendarButton, friendButton,
          mainButton, mainFragment, settingButton, timeTableButton, toolbar2);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
