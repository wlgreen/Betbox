package com.betbox.app.activity;

import java.io.Serializable;

import com.paypal.android.MEP.PayPalResultDelegate;

public class ResultDelegate implements PayPalResultDelegate, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 10001L;

	public void onPaymentSucceeded(String payKey, String paymentStatus) {

		BetPayment.resultTitle = "SUCCESS";
		BetPayment.resultInfo = "You have successfully completed your transaction.";
		BetPayment.resultExtra = "Key: " + payKey;
	}

	public void onPaymentFailed(String paymentStatus, String correlationID,
			String payKey, String errorID, String errorMessage) {
		BetPayment.resultTitle = "FAILURE";
		BetPayment.resultInfo = errorMessage;
		BetPayment.resultExtra = "Error ID: " + errorID + "\nCorrelation ID: "
				+ correlationID + "\nPay Key: " + payKey;
	}

	public void onPaymentCanceled(String paymentStatus) {
		BetPayment.resultTitle = "CANCELED";
		BetPayment.resultInfo = "The transaction has been cancelled.";
		BetPayment.resultExtra = "";
	}

}
