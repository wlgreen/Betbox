package com.betbox.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.FrameLayout.LayoutParams;

import com.betbox.app.R;
import com.betbox.app.data.*;

/**
 * A fragment representing a single Bet detail screen. This fragment is either
 * contained in a {@link BetListActivity} in two-pane mode (on tablets) or a
 * {@link BetDetailActivity} on handsets.
 */
public class BetDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	private Bet mItem;

	public final static String BET_CONTENT = "com.Betbox.BetDisplayActivity.BetContent";
	public final static String BET_RESULT = "com.Betbox.BetDisplayActivity.BetResult";
	public final static String BET_ID = "com.Betbox.BetDisplayActivity.BetID";

	private RadioGroup radioBetGroup;
	private RadioButton radioBetButton;
	private Button payButton;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public BetDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mItem = BetPool.ITEM_MAP.get(getArguments().getString(
					ARG_ITEM_ID));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LinearLayout content = (LinearLayout) inflater.inflate(
				R.layout.fragment_bet_detail, container, false);

		// Show the bet content as text in a TextView.
		if (mItem != null) {
			((TextView) content.findViewById(R.id.bet_detail))
					.setText(mItem.content);
		}

		radioBetGroup = (RadioGroup) content.findViewById(R.id.radioBet);

		payButton = new Button(getActivity());
		payButton.setLayoutParams(new LayoutParams(200,
				LayoutParams.WRAP_CONTENT)); // Semi mimic PP button sizes
		payButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// get selected radio button from radioGroup
				int selectedId = radioBetGroup.getCheckedRadioButtonId();

				// find the radiobutton by returned id
				radioBetButton = (RadioButton) getActivity().findViewById(
						selectedId);

				if (radioBetButton != null && mItem.pool.avaiable()) {
					String message = radioBetButton.getText().toString();
					/* Hold a position in the bet pool when the pay transaction is started.
					 * If the transaction fails, release this position.
					 * TODO: Hold should also be sync up with the server.
					 */
					mItem.pool.hold();
					Intent intent = new Intent(getActivity(), BetPayment.class);					
					intent.putExtra(BET_CONTENT, mItem.content);
					intent.putExtra(BET_RESULT, message);
					intent.putExtra(BET_ID, mItem.content);
					startActivity(intent);
				}
			}
		});
		payButton.setText("Pay");
		content.addView(payButton);

		return content;
	}
}
