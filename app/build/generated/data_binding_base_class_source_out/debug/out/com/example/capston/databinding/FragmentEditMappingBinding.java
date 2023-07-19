// Generated by view binder compiler. Do not edit!
package com.example.capston.databinding;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.helper.widget.Layer;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;
import androidx.viewbinding.ViewBindings;
import com.example.capston.R;
import java.lang.NullPointerException;
import java.lang.Override;
import java.lang.String;

public final class FragmentEditMappingBinding implements ViewBinding {
  @NonNull
  private final ConstraintLayout rootView;

  @NonNull
  public final TextView arrivalTextView;

  @NonNull
  public final TextView arrivalValueTextView;

  @NonNull
  public final ImageView carWayButton;

  @NonNull
  public final ImageView publicTransportButton;

  @NonNull
  public final RecyclerView routeSearchResultRecyclerView;

  @NonNull
  public final Button searchButton;

  @NonNull
  public final Layer searchlayer;

  @NonNull
  public final TextView startTextView;

  @NonNull
  public final TextView startValueTextView;

  @NonNull
  public final TextView totalTimeTextView;

  @NonNull
  public final Spinner trackingTimeSpinner;

  @NonNull
  public final TextView trackingTimeTextView;

  @NonNull
  public final ImageView walkingButton;

  private FragmentEditMappingBinding(@NonNull ConstraintLayout rootView,
      @NonNull TextView arrivalTextView, @NonNull TextView arrivalValueTextView,
      @NonNull ImageView carWayButton, @NonNull ImageView publicTransportButton,
      @NonNull RecyclerView routeSearchResultRecyclerView, @NonNull Button searchButton,
      @NonNull Layer searchlayer, @NonNull TextView startTextView,
      @NonNull TextView startValueTextView, @NonNull TextView totalTimeTextView,
      @NonNull Spinner trackingTimeSpinner, @NonNull TextView trackingTimeTextView,
      @NonNull ImageView walkingButton) {
    this.rootView = rootView;
    this.arrivalTextView = arrivalTextView;
    this.arrivalValueTextView = arrivalValueTextView;
    this.carWayButton = carWayButton;
    this.publicTransportButton = publicTransportButton;
    this.routeSearchResultRecyclerView = routeSearchResultRecyclerView;
    this.searchButton = searchButton;
    this.searchlayer = searchlayer;
    this.startTextView = startTextView;
    this.startValueTextView = startValueTextView;
    this.totalTimeTextView = totalTimeTextView;
    this.trackingTimeSpinner = trackingTimeSpinner;
    this.trackingTimeTextView = trackingTimeTextView;
    this.walkingButton = walkingButton;
  }

  @Override
  @NonNull
  public ConstraintLayout getRoot() {
    return rootView;
  }

  @NonNull
  public static FragmentEditMappingBinding inflate(@NonNull LayoutInflater inflater) {
    return inflate(inflater, null, false);
  }

  @NonNull
  public static FragmentEditMappingBinding inflate(@NonNull LayoutInflater inflater,
      @Nullable ViewGroup parent, boolean attachToParent) {
    View root = inflater.inflate(R.layout.fragment_edit_mapping, parent, false);
    if (attachToParent) {
      parent.addView(root);
    }
    return bind(root);
  }

  @NonNull
  public static FragmentEditMappingBinding bind(@NonNull View rootView) {
    // The body of this method is generated in a way you would not otherwise write.
    // This is done to optimize the compiled bytecode for size and performance.
    int id;
    missingId: {
      id = R.id.arrivalTextView;
      TextView arrivalTextView = ViewBindings.findChildViewById(rootView, id);
      if (arrivalTextView == null) {
        break missingId;
      }

      id = R.id.arrivalValueTextView;
      TextView arrivalValueTextView = ViewBindings.findChildViewById(rootView, id);
      if (arrivalValueTextView == null) {
        break missingId;
      }

      id = R.id.carWayButton;
      ImageView carWayButton = ViewBindings.findChildViewById(rootView, id);
      if (carWayButton == null) {
        break missingId;
      }

      id = R.id.publicTransportButton;
      ImageView publicTransportButton = ViewBindings.findChildViewById(rootView, id);
      if (publicTransportButton == null) {
        break missingId;
      }

      id = R.id.routeSearchResultRecyclerView;
      RecyclerView routeSearchResultRecyclerView = ViewBindings.findChildViewById(rootView, id);
      if (routeSearchResultRecyclerView == null) {
        break missingId;
      }

      id = R.id.search_button;
      Button searchButton = ViewBindings.findChildViewById(rootView, id);
      if (searchButton == null) {
        break missingId;
      }

      id = R.id.searchlayer;
      Layer searchlayer = ViewBindings.findChildViewById(rootView, id);
      if (searchlayer == null) {
        break missingId;
      }

      id = R.id.startTextView;
      TextView startTextView = ViewBindings.findChildViewById(rootView, id);
      if (startTextView == null) {
        break missingId;
      }

      id = R.id.startValueTextView;
      TextView startValueTextView = ViewBindings.findChildViewById(rootView, id);
      if (startValueTextView == null) {
        break missingId;
      }

      id = R.id.totalTimeTextView;
      TextView totalTimeTextView = ViewBindings.findChildViewById(rootView, id);
      if (totalTimeTextView == null) {
        break missingId;
      }

      id = R.id.trackingTimeSpinner;
      Spinner trackingTimeSpinner = ViewBindings.findChildViewById(rootView, id);
      if (trackingTimeSpinner == null) {
        break missingId;
      }

      id = R.id.trackingTimeTextView;
      TextView trackingTimeTextView = ViewBindings.findChildViewById(rootView, id);
      if (trackingTimeTextView == null) {
        break missingId;
      }

      id = R.id.walkingButton;
      ImageView walkingButton = ViewBindings.findChildViewById(rootView, id);
      if (walkingButton == null) {
        break missingId;
      }

      return new FragmentEditMappingBinding((ConstraintLayout) rootView, arrivalTextView,
          arrivalValueTextView, carWayButton, publicTransportButton, routeSearchResultRecyclerView,
          searchButton, searchlayer, startTextView, startValueTextView, totalTimeTextView,
          trackingTimeSpinner, trackingTimeTextView, walkingButton);
    }
    String missingId = rootView.getResources().getResourceName(id);
    throw new NullPointerException("Missing required view with ID: ".concat(missingId));
  }
}
