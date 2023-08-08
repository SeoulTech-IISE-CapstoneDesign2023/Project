// Generated by view binder compiler. Do not edit!
package com.example.capston.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.capston.R;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentEditTodoBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextInputEditText memoEditTextView;

  @NonNull
  public final EditText placeEditTextView;

  @NonNull
  public final TextView placeTextView;

  @NonNull
  public final TextInputLayout textTextInputLayout;

  private FragmentEditTodoBinding(@NonNull ConstraintLayout rootView,
      @NonNull TextInputEditText memoEditTextView, @NonNull EditText placeEditTextView,
      @NonNull TextView placeTextView, @NonNull TextInputLayout textTextInputLayout) {
    this.rootView = rootView;
    this.memoEditTextView = memoEditTextView;
    this.placeEditTextView = placeEditTextView;
    this.placeTextView = placeTextView;
    this.textTextInputLayout = textTextInputLayout;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentEditTodoBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentEditTodoBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_edit_todo, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentEditTodoBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.memoEditTextView;
      TextInputEditText memoEditTextView = ViewBindings.findChildViewById(rootView, id);
      if (memoEditTextView == null) {
        break missingId;
      }

      id = R.id.placeEditTextView;
      EditText placeEditTextView = ViewBindings.findChildViewById(rootView, id);
      if (placeEditTextView == null) {
        break missingId;
      }

      id = R.id.placeTextView;
      TextView placeTextView = ViewBindings.findChildViewById(rootView, id);
      if (placeTextView == null) {
        break missingId;
      }

      id = R.id.textTextInputLayout;
      TextInputLayout textTextInputLayout = ViewBindings.findChildViewById(rootView, id);
      if (textTextInputLayout == null) {
        break missingId;
      }

      return new FragmentEditTodoBinding((ConstraintLayout) rootView, memoEditTextView,
          placeEditTextView, placeTextView, textTextInputLayout);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
