package com.betbox.app.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.util.Log;

import com.betbox.app.data.Bet;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class BetPool {
	public static String lastUpdated = "00/00/00 00:00:00";
	/**
	 * An array of sample (dummy) items.
	 */
	public static List<Bet> ITEMS = new ArrayList<Bet>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, Bet> ITEM_MAP = new HashMap<String, Bet>();

	/*
	 * Generate id for a new bet input, return null if this bet is already
	 * stored
	 */
	private static String generateID(Bet bet) {
		String id = bet.content;
		if (ITEM_MAP.keySet().contains(id)) {
			Log.i("BetPool", "This key already exists");
			return null;
		}
		return id;
	}

	// static {
	// // Add 3 sample items.
	// addItem(new Bet("Will Lakes win?"));
	// addItem(new Bet("Will Nasa lanches its rockets?"));
	// addItem(new Bet("Will it rain tomorrow?"));
	// }

	public static void addItem(Bet item) {
		String id = generateID(item);
		if (id != null) {
			ITEMS.add(item);
			ITEM_MAP.put(id, item);
			try {
				if (isLater(item.creationTime, lastUpdated)) {
					lastUpdated = item.creationTime;
				}
			} catch (ParseException e) {
				e.fillInStackTrace();
			}
		}
	}

	/* return true if time1 is later than time2 */
	private static boolean isLater(String time1, String time2)
			throws ParseException {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss");
		Date date1 = df.parse(time1);
		Date date2 = df.parse(time2);
		return date1.after(date2);
	}
}
