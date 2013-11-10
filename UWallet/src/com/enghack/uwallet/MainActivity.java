package com.enghack.uwallet;

import org.jsoup.nodes.Element;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.Menu;
import android.widget.EditText;

import com.enghack.uwallet.login.HTMLParser;
import com.enghack.uwallet.login.LoginTask;
import com.enghack.uwallet.login.LoginTask.ResponseListener;
import com.enghack.watcard.WatcardInfo;

public class MainActivity extends Activity implements ResponseListener,
		BalanceFragment.Listener, TransactionFragment.Listener,
		AboutFragment.Listener, LoginFragment.Listener, MenuFragment.Listener {

	BalanceFragment mBalanceFragment = null;
	TransactionFragment mTransactionFragment = null;
	AboutFragment mAboutFragment = null;
	LoginFragment mLoginFragment = null;
	MenuFragment mMenuFragment = null;

	private final String URL = "https://account.watcard.uwaterloo.ca/watgopher661.asp";
	private HTMLParser parser;
	private EditText viewID = null;
	private EditText viewPIN = null;
	private int studentID = 0;
	private int studentPIN = 0;
	
	private WatcardInfo person;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mBalanceFragment = new BalanceFragment();
		mTransactionFragment = new TransactionFragment();
		mAboutFragment = new AboutFragment();
		mLoginFragment = new LoginFragment();
		mMenuFragment = new MenuFragment();

		if(viewID == null){
			switchToFragment(mLoginFragment);
		}else{
			switchToFragment(mMenuFragment);
		}
	}

	void switchToFragment(Fragment newFrag){
		switchToFragment(newFrag, true);
	}
	
	void switchToFragment(Fragment newFrag, boolean addToBackStack){
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.setCustomAnimations(
                R.anim.card_flip_right_in, R.anim.card_flip_right_out,
                R.anim.card_flip_left_in, R.anim.card_flip_left_out)
                .replace(R.id.fragment_container, newFrag);
		if (addToBackStack)
			transaction.addToBackStack(null);
		transaction.commit();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onBalanceButtonClicked() {
		switchToFragment(mBalanceFragment);
	}

	@Override
	public void onTransactionsButtonClicked() {
		switchToFragment(mTransactionFragment);
	}

	@Override
	public void onAboutButtonClicked() {
		switchToFragment(mAboutFragment);
	}

	@Override
	public void onLogOutButtonClicked() {
		// TODO: Use cleardata base method
		viewID = null;
		viewPIN = null;
		studentID = 0;
		studentPIN = 0;
		switchToFragment(mLoginFragment);
	}
	
	@Override
	public void onLogInButtonClicked() {
		//DatabaseHandler db = new DatabaseHandler(this);
		parser = new HTMLParser();
		viewID = (EditText) (this.findViewById(R.id.username_input));
		viewPIN = (EditText) (this.findViewById(R.id.password_input));
		if (!authenticate(viewID.getText().toString(), viewPIN.getText().toString()))
		{
			
		}
		studentID = Integer.parseInt(viewID.getText().toString());
		studentPIN = Integer.parseInt(viewPIN.getText().toString());
		executeLogin(URL, viewID.getText().toString(), viewPIN.getText()
				.toString());
		switchToFragment(mMenuFragment);
	}

	private void executeLogin(String URL, String ID, String PIN) {
		try {
			LoginTask login = new LoginTask();
			login.mListener = this;
			login.execute(URL, ID, PIN);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResponseFinish(Element histDoc, Element statusDoc) {
		person = new WatcardInfo(parser.parseHist(histDoc),
		// Indexes of each type of balance based on the website
				parser.parseBalance(statusDoc, 2, 5), parser.parseBalance(
						statusDoc, 5, 8),
				parser.parseBalance(statusDoc, 8, 14), studentID, studentPIN);
		person.printData(); // for testing purposes
		return;
	}

	private boolean authenticate(String a, String b)
	{
	    try { 
	        Integer.parseInt(a);
	        Integer.parseInt(b);
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    if (!this.isNetworkAvailable()) {
	    	return false;
	    }
		return true;
	}
	
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager 
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}
}
