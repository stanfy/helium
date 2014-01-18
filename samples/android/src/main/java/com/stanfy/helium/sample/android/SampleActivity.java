package com.stanfy.helium.sample.android;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.TextView;

import com.stanfy.helium.sample.api.Tweet;
import com.stanfy.helium.sample.api.User;
import com.stanfy.helium.sample.constants.UserConstants;

/**
 * Sample activity.
 */
public class SampleActivity extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TextView textView = new TextView(this);
    setContentView(textView);
    textView.setText(
        "Constant example:\n"
        + "UserConstants.COLUMN_CREATED_AT = " + UserConstants.COLUMN_CREATED_AT + "\n\n"
        + "Some of generated classes :\n"
        + User.class + "\n"
        + Tweet.class + "\n\n"
        + "(Tweet implements Parcelable) == " + (Parcelable.class.isAssignableFrom(Tweet.class))
    );
  }
}
