package com.betbox.app.activity;

import com.betbox.app.data.Bet;
import com.betbox.app.data.BetPool;
import com.betbox.app.gcm.CommonUtilities;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPreapproval;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.annotation.SuppressLint;
import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

import com.loopj.android.http.*;


@SuppressLint("HandlerLeak")
public class BetPayment extends Activity implements OnClickListener {
	public static final String RESPONSE_SUCCESS = "SUCCESS";
	public static final String RESPONSE_FAILURE = "FAILURE";
	
	// The PayPal server to be used - can also be ENV_SANDBOX and ENV_LIVE
	private static final int server = PayPal.ENV_SANDBOX;
	private static final String appID = "APP-80W284485P519543T";
	private static final int request = 1;

	public static String resultTitle;
	public static String resultInfo;
	public static String resultExtra;
	public static final int INIT_SUCCESS = 0;
	public static final int INIT_FAILURE = 1;

	private String betContent;
	private String betResult;
	private String betID;
	private Bet bet;

	private LinearLayout content;
	private TextView text1;
	private TextView text2;
	private EditText preapprovalKey;
	private CheckoutButton launchPayment;
	private TextView title;
	private TextView info;
	private TextView extra;

    /* Handler instance to handle he paypal library initialization thread */
	Handler checkInit = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case INIT_SUCCESS:
				getCheckoutButton();
				break;
			case INIT_FAILURE:
				showFailure();
				break;
			}
		}
	};

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		/* New thread created to initialize the paypal library */
		Thread libraryInitializationThread = new Thread() {
			@Override
			public void run() {
				initLibrary();

				if (PayPal.getInstance().isLibraryInitialized()) {
					checkInit.sendEmptyMessage(INIT_SUCCESS);
				} else {
					checkInit.sendEmptyMessage(INIT_FAILURE);
				}
			}
		};
		libraryInitializationThread.start();

		// Get the message from the intent
		Intent intent = getIntent();
		betContent = intent.getStringExtra(BetDetailFragment.BET_CONTENT);
		betResult = intent.getStringExtra(BetDetailFragment.BET_RESULT);
		betID = intent.getStringExtra(BetDetailFragment.BET_ID);

		bet = BetPool.ITEM_MAP.get(betID);

		content = new LinearLayout(this);
		content.setGravity(Gravity.CENTER_HORIZONTAL);
		content.setOrientation(LinearLayout.VERTICAL);
		content.setPadding(10, 10, 10, 10);
		content.setBackgroundColor(Color.TRANSPARENT);

		text1 = new TextView(this);
		text1.setTextSize(40);
		text1.setGravity(Gravity.CENTER_HORIZONTAL);
		text1.setText(betContent);
		content.addView(text1);

		text2 = new TextView(this);
		text2.setGravity(Gravity.CENTER_HORIZONTAL);
		text2.setTextSize(40);
		text2.setText("Bet: " + betResult);
		content.addView(text2);

		preapprovalKey = new EditText(this);
		preapprovalKey.setLayoutParams(new LayoutParams(300, 45));
		preapprovalKey.setGravity(Gravity.CENTER_HORIZONTAL);
		preapprovalKey.setHint("Preapproval key");
		content.addView(preapprovalKey);

		title = new TextView(this);
		title.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		title.setPadding(0, 5, 0, 5);
		title.setGravity(Gravity.CENTER_HORIZONTAL);
		title.setTextSize(30.0f);
		title.setVisibility(TextView.GONE);
		content.addView(title);

		info = new TextView(this);
		info.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		info.setPadding(0, 5, 0, 5);
		info.setGravity(Gravity.CENTER_HORIZONTAL);
		info.setTextSize(20.0f);
		info.setVisibility(TextView.GONE);
		content.addView(info);

		extra = new TextView(this);
		extra.setLayoutParams(new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		extra.setPadding(0, 5, 0, 5);
		extra.setGravity(Gravity.CENTER_HORIZONTAL);
		extra.setTextSize(12.0f);
		extra.setVisibility(TextView.GONE);
		content.addView(extra);

		setContentView(content);
		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	public void initLibrary() {

		PayPal pp = PayPal.getInstance();

		if (pp == null) { // Test to see if the library is already initialized

			// This main initialization call takes your Context, AppID, and
			// target server
			pp = PayPal.initWithAppID(this, appID, server);

			// Required settings:

			// Set the language for the library
			pp.setLanguage("en_US");
			pp.setFeesPayer(PayPal.FEEPAYER_EACHRECEIVER);
			pp.setShippingEnabled(true);
			pp.setDynamicAmountCalculationEnabled(false);
		}
	}

	public void getCheckoutButton() {
		PayPal pp = PayPal.getInstance();

		launchPayment = pp.getCheckoutButton(this, PayPal.BUTTON_194x37,
				CheckoutButton.TEXT_PAY);
		launchPayment.setLayoutParams(new LayoutParams(300, 45));
		launchPayment.setOnClickListener(this);
		content.addView(launchPayment);
	}

	public void showFailure() {
		title.setText("FAILURE");
		info.setText("Could not intialize the Library");
		title.setVisibility(View.VISIBLE);
		info.setVisibility(View.VISIBLE);
	}

	private PayPalPreapproval CreatePayment() {

		// Create the PayPalPreapproval
		PayPalPreapproval preapproval = new PayPalPreapproval();
		// Sets the currency type for this payment.
		preapproval.setCurrencyType("USD");
		// Sets the memo. This memo will be part of the notification sent by
		// PayPal to the necessary parties.
		preapproval.setMemo("Preapproval payment");
		// Sets the merchant name. This is the name of your Application or
		// Company.
		preapproval.setMerchantName("Betbox");

		return preapproval;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode != request) {
			return;
		}
		switch (resultCode) {
		case Activity.RESULT_OK:
			resultTitle = "SUCCESS";
			resultInfo = "You have successfully completed this preapproval.";
			resultExtra = "Transaction ID: "
					+ data.getStringExtra(PayPalActivity.EXTRA_PAY_KEY);
			/* Update the server if the paypal transaction is successful */
			updateServer();
			break;
		case Activity.RESULT_CANCELED:
			resultTitle = "CANCELED";
			resultInfo = "The transaction has been cancelled.";
			resultExtra = "";
			bet.pool.release();
			break;
		case PayPalActivity.RESULT_FAILURE:
			resultTitle = "FAILURE";
			resultInfo = data
					.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE);
			resultExtra = "Error ID: "
					+ data.getStringExtra(PayPalActivity.EXTRA_ERROR_ID);
			bet.pool.release();
			break;
		}

		launchPayment.updateButton();

		title.setText(resultTitle);
		title.setVisibility(View.VISIBLE);
		info.setText(resultInfo);
		info.setVisibility(View.VISIBLE);
		extra.setText(resultExtra);
		extra.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		// Create a Paypal preapproval payment
		PayPalPreapproval payment = CreatePayment();
		PayPal.getInstance().setPreapprovalKey(
				preapprovalKey.getText().toString());
		// Use checkout to create the intent
		Intent checkoutIntent = PayPal.getInstance().preapprove(payment, this,
				new ResultDelegate());
		startActivityForResult(checkoutIntent, request);
	}
	
	public void updateServer() {
        AsyncHttpClient client = new AsyncHttpClient();
        
        RequestParams params = new RequestParams();
        params.put("bet", betContent);
        params.put("member", "none");
        params.put("stand", betResult);

        client.post(CommonUtilities.SERVER_URL + "/updateBet", params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String response) {
            	
                if (response.equals(BetPayment.RESPONSE_SUCCESS)) {
                	bet.pool.finish(betResult);
                } else {
            		title.setText("FAILURE");
            		info.setText("Cannot sync with the server");
            		extra.setText("");

        			bet.pool.release();
                }
            }
        });
	}
}
