// Generated by view binder compiler. Do not edit!
package com.example.capston.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.airbnb.lottie.LottieAnimationView;
import com.example.capston.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class ActivityLoginBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final Button SignInBtn;

  @NonNull
  public final Button SignUpBtn;

  @NonNull
  public final TextView findPWBtn;

  @NonNull
  public final LottieAnimationView imageLoadingView;

  @NonNull
  public final EditText inputID;

  @NonNull
  public final EditText inputPW;

  @NonNull
  public final ImageView logo;

  @NonNull
  public final TextView textView;

  @NonNull
  public final TextView textView2;

  private ActivityLoginBinding(@NonNull ConstraintLayout rootView, @NonNull Button SignInBtn,
      @NonNull Button SignUpBtn, @NonNull TextView findPWBtn,
      @NonNull LottieAnimationView imageLoadingView, @NonNull EditText inputID,
      @NonNull EditText inputPW, @NonNull ImageView logo, @NonNull TextView textView,
      @NonNull TextView textView2) {
    this.rootView = rootView;
    this.SignInBtn = SignInBtn;
    this.SignUpBtn = SignUpBtn;
    this.findPWBtn = findPWBtn;
    this.imageLoadingView = imageLoadingView;
    this.inputID = inputID;
    this.inputPW = inputPW;
    this.logo = logo;
    this.textView = textView;
    this.textView2 = textView2;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static ActivityLoginBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static ActivityLoginBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.activity_login, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static ActivityLoginBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.SignInBtn;
      Button SignInBtn = ViewBindings.findChildViewById(rootView, id);
      if (SignInBtn == null) {
        break missingId;
      }

      id = R.id.SignUpBtn;
      Button SignUpBtn = ViewBindings.findChildViewById(rootView, id);
      if (SignUpBtn == null) {
        break missingId;
      }

      id = R.id.findPWBtn;
      TextView findPWBtn = ViewBindings.findChildViewById(rootView, id);
      if (findPWBtn == null) {
        break missingId;
      }

      id = R.id.imageLoadingView;
      LottieAnimationView imageLoadingView = ViewBindings.findChildViewById(rootView, id);
      if (imageLoadingView == null) {
        break missingId;
      }

      id = R.id.inputID;
      EditText inputID = ViewBindings.findChildViewById(rootView, id);
      if (inputID == null) {
        break missingId;
      }

      id = R.id.inputPW;
      EditText inputPW = ViewBindings.findChildViewById(rootView, id);
      if (inputPW == null) {
        break missingId;
      }

      id = R.id.logo;
      ImageView logo = ViewBindings.findChildViewById(rootView, id);
      if (logo == null) {
        break missingId;
      }

      id = R.id.textView;
      TextView textView = ViewBindings.findChildViewById(rootView, id);
      if (textView == null) {
        break missingId;
      }

      id = R.id.textView2;
      TextView textView2 = ViewBindings.findChildViewById(rootView, id);
      if (textView2 == null) {
        break missingId;
      }

      return new ActivityLoginBinding((ConstraintLayout) rootView, SignInBtn, SignUpBtn, findPWBtn,
          imageLoadingView, inputID, inputPW, logo, textView, textView2);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
