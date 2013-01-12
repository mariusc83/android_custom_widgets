package mc.app.components.calendarwidget.fragments;

import java.util.ArrayList;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

public abstract class AbstractAsyncLoader<D> extends
		AsyncTaskLoader<ArrayList<D>> {
	private ArrayList<D> data;
	
	public AbstractAsyncLoader(Context c) {
		super(c);
	}

	@Override
	public ArrayList<D> loadInBackground() {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deliverResult(ArrayList<D> data) {

		// check to see if we are in the middle of reset event
		if (isReset()) {
			onReleaseResource(data);
		}

		// caching the data for future use
		this.data = (ArrayList<D>)data.clone();

		if (isStarted()) {
			super.deliverResult(data);
		}

	}
	

	@Override
	protected void onStartLoading() {
		// checking to see if we already have some data available
		if (data != null)
			deliverResult(data);
		else
		forceLoad();
	}

	@Override
	public void onReset() {
		super.onReset();
		// ensure the loading is canceled
		onStopLoading();

		// release the current cached data
		if (isReset())
			onReleaseResource(data);
	}

	@Override
	public void onCanceled(ArrayList<D> data) {
		super.onCanceled(data);
		// release the cached data
		onReleaseResource(data);
	}

	public void onReleaseResource(ArrayList<D> data) {
		if (data != null) {
			data.clear();
			data = null;
		}
	}

}