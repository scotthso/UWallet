package ca.uwallet.main;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.renderer.DefaultRenderer;
import org.achartengine.renderer.SimpleSeriesRenderer;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import ca.uwallet.main.provider.WatcardContract;
import ca.uwallet.main.util.ProviderUtils;


public class StatsFragment extends Fragment implements LoaderCallbacks<Cursor>{
	
	private static final int LOADER_BALANCES_ID = 17;
	private static final int BALANCE_CHART_ID = 123456;

	public StatsFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_stats, container,
				false);

		return v;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState){
		super.onActivityCreated(savedInstanceState);
		getLoaderManager().initLoader(LOADER_BALANCES_ID, null, this);
	}

	private DefaultRenderer buildRenderer(int[] colors){
		DefaultRenderer renderer = new DefaultRenderer();
		for (int color : colors) {
	        SimpleSeriesRenderer r = new SimpleSeriesRenderer();
	        r.setColor(color);
	        renderer.addSeriesRenderer(r);
	    }
		renderer.setBackgroundColor(0x00000000);
		renderer.setPanEnabled(false);
		renderer.setZoomEnabled(false);
		renderer.setLabelsTextSize(30);
		renderer.setShowLegend(false);
	    return renderer;
	}
	
	private GraphicalView getBalanceChart(int[] amounts){
		Context context = getActivity();
		CategorySeries series = new CategorySeries("Balance");
		
		series.add("Meal Plan", ProviderUtils.getMealBalance(amounts));
		series.add("Flex Dollars", ProviderUtils.getFlexBalance(amounts));
		
		int[] colors = {0xFF00FF00, 0xFFFFFF00};
		DefaultRenderer renderer = buildRenderer(colors);
		return ChartFactory.getPieChartView(context, series, renderer);
	}
	
	private void appendView(View v, int id){
		ViewGroup parent = (ViewGroup)getView();
		if (parent != null){
			View old = parent.findViewById(id);
			if (old != null)
				parent.removeView(old);
			if (v != null)
				parent.addView(v);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		if (id != LOADER_BALANCES_ID)
			return null;
		return new CursorLoader(getActivity(), WatcardContract.Balance.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		int[] amounts = ProviderUtils.getBalanceAmounts(data);
		GraphicalView pieChart = getBalanceChart(amounts);
		appendView(pieChart, BALANCE_CHART_ID);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		appendView(null, BALANCE_CHART_ID);
	}	
}