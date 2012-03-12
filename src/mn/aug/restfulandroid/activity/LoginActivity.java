package mn.aug.restfulandroid.activity;

import mn.aug.restfulandroid.R;
import mn.aug.restfulandroid.rest.method.PostLoginRestMethod;
import mn.aug.restfulandroid.rest.method.RestMethod;
import mn.aug.restfulandroid.rest.method.RestMethodResult;
import mn.aug.restfulandroid.rest.resource.Login;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class LoginActivity extends Activity {

	public static final String PREFS_KEY_COOKIE = null;
	
	private EditText usernameField;
	private EditText passwordField;
	protected ProgressBar mProgressSpinner;

	private OnLoginTaskCompleteListener mOnLoginTaskCompleteListener = new OnLoginTaskCompleteListener() {

		@Override
		public void onSuccess(Login login) {
			Toast.makeText(LoginActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
			mProgressSpinner.setVisibility(View.GONE);
			save(login);
			setResult(Activity.RESULT_OK);
			finish();
		}

		@Override
		public void onError(String message, Exception e) {
			Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
			mProgressSpinner.setVisibility(View.GONE);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		usernameField = (EditText) findViewById(R.id.username);
		passwordField = (EditText) findViewById(R.id.password);

		mProgressSpinner = (ProgressBar) findViewById(R.id.progress);

		Button loginButton = (Button) findViewById(R.id.btnLogin);
		loginButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mProgressSpinner.setVisibility(View.VISIBLE);
				hideKeybaord();
				String username = usernameField.getText().toString();
				String password = passwordField.getText().toString();
				new LoginTask(mOnLoginTaskCompleteListener).execute(username, password);
			}
		});
	}

	protected void hideKeybaord() {
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(passwordField.getWindowToken(), 0);
	}

	protected void save(Login login) {
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
		editor.putString(PREFS_KEY_COOKIE, login.getCookie());
		editor.commit();
	}

	public class LoginTask extends AsyncTask<String, Void, RestMethodResult<Login>> {

		private OnLoginTaskCompleteListener mListener;

		public LoginTask(OnLoginTaskCompleteListener listener) {
			mListener = listener;
		}

		@Override
		protected RestMethodResult<Login> doInBackground(String... params) {

			Login login = new Login(params[0], params[1]);
			RestMethod<Login> postLoginRestMethod = new PostLoginRestMethod(
					getApplicationContext(), login);
			return postLoginRestMethod.execute();

		}

		@Override
		protected void onPostExecute(RestMethodResult<Login> result) {
			super.onPostExecute(result);
			
			Login login = result.getResource();
			
			if (login != null && login.getCookie() != null) {
				mListener.onSuccess(login);
			} else {
				mListener.onError("Login error", null);
			}
			
		}

	}

	public interface OnLoginTaskCompleteListener {

		public void onSuccess(Login login);

		public void onError(String message, Exception e);
	}

}