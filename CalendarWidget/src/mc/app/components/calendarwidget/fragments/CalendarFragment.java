package mc.app.components.calendarwidget.fragments;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import com.actionbarsherlock.app.SherlockFragment;

import mc.app.components.calendarwidget.R;
import mc.app.components.calendarwidget.fragments.CalendarFragment.CalendarAdapter.CalendarCell;
import mc.app.components.calendarwidget.fragments.CalendarFragment.CalendarAdapter.CalendarHeaderCell;
import mc.app.components.calendarwidget.fragments.CalendarFragment.CalendarAdapter.Cell;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.MonthDisplayHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Customizable widget Fragment
 * 
 * @author Marius Constantin
 * 
 */
public class CalendarFragment extends SherlockFragment implements
		LoaderCallbacks<ArrayList<Cell>> {

	// cel types
	public static final int HEADER_CELL = 0;
	public static final int SELECTABLE_CELL = 1;

	private static final String CURRENT_YEAR = "current_year";
	private static final String CURRENT_MONTH = "current_month";
	private static final String CURRENT_DAY = "current_day";

	public final int CALENDAR_LOADER = 0;

	protected Button currentDateButton;
	protected ImageButton prevMonthButton;
	protected ImageButton nextMonthButton;
	protected ImageView calendarHeader;
	protected GridView calendar;
	protected CalendarAdapter calendarAdapter;
	public MonthDisplayHelper helper;
	public CalendarListener dispatcher;

	// private TextView currentSelectedCell;

	public CalendarFragment() {
	}

	public static CalendarFragment newInstance() {
		CalendarFragment f = new CalendarFragment();
		Bundle b = new Bundle();
		f.setArguments(b);
		return f;
	}

	// life - cycle methods
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.calendar_layout, container, false);
		currentDateButton = (Button) v.findViewById(R.id.selectedDayMonthYear);
		prevMonthButton = (ImageButton) v.findViewById(R.id.prevMonth);
		nextMonthButton = (ImageButton) v.findViewById(R.id.nextMonth);
		calendarHeader = (ImageView) v.findViewById(R.id.calendarHeader);
		calendar = (GridView) v.findViewById(R.id.calendar);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Calendar c = Calendar.getInstance();
		calendarAdapter = new CalendarAdapter(this);
		calendar.setAdapter(calendarAdapter);
		if (getArguments().containsKey(CURRENT_MONTH)
				&& getArguments().containsKey(CURRENT_YEAR))
			helper = new MonthDisplayHelper(
					getArguments().getInt(CURRENT_YEAR), getArguments().getInt(
							CURRENT_MONTH), Calendar.MONDAY);

		else
			helper = new MonthDisplayHelper(c.get(Calendar.YEAR),
					c.get(Calendar.MONTH), Calendar.MONDAY);

		initListeners();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		currentDateButton = null;
		prevMonthButton = null;
		nextMonthButton = null;
		calendarHeader = null;
		calendar = null;
	}

	public Loader<ArrayList<Cell>> onCreateLoader(int arg0, Bundle arg1) {
		return new CalendarLoader(getActivity(), Calendar.getInstance(), this);
	}

	public void onLoaderReset(Loader<ArrayList<Cell>> arg0) {
		if (calendarAdapter != null)
			calendarAdapter.setData(null);
	}

	public void onLoadFinished(Loader<ArrayList<Cell>> arg0,
			ArrayList<Cell> arg1) {

		if (arg1 != null) {
			calendar.removeAllViewsInLayout();
			calendarAdapter.setData(arg1);
			// refresh calendar header
			refreshDateHeader(getCurrentYear(), getCurrentMonth(),
					getCurrentDay());
			if (dispatcher != null) {
				dispatcher.onFinishedLoading();
			}
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getArguments().putInt(CURRENT_YEAR, getCurrentYear());
		getArguments().putInt(CURRENT_MONTH, getCurrentMonth());

	}

	// initializers
	private void initListeners() {
		nextMonthButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				nextMonth();
			}
		});

		prevMonthButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				prevMonth();
			}
		});

		calendar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				ArrayList<Cell> cells = calendarAdapter.getData();
				CalendarCell cell = (CalendarCell) cells.get(arg2);
				if (cell.isSelectable) {
					// unselect the prev selectedCell
					// if (currentSelectedCell != null)
					// currentSelectedCell
					// .setBackgroundResource(R.drawable.calendar_button_selector);

					// currentSelectedCell = (TextView) arg1;
					// currentSelectedCell
					// .setBackgroundResource(R.drawable.calendar_selected_cell_button_selector);

					// unselect all calendar cells
					int len = cells.size();
					while (--len > 6) {
						((CalendarCell) cells.get(len)).selected = false;
					}

					// mark the current cell as selected

					cell.selected = true;
					refreshDateHeader(getCurrentYear(), getCurrentMonth(),
							cell.dayNumber);
					// calendar.removeAllViewsInLayout();
					// calendarAdapter.notifyDataSetChanged();
					// propagate the event to the parent container
					if (dispatcher != null) {
						dispatcher.onCellSelected(cell);
					}
					// update the current selected day into the arguments
					getArguments().putInt(CURRENT_DAY, cell.dayNumber);
				}
			}
		});
	}

	/**
	 * sets the current events dispatcher
	 */
	public void setDispatcher(CalendarListener dispatcher) {
		this.dispatcher = dispatcher;
	}

	/**
	 * dismiss the current events dispatcher
	 */
	public void dismissDispatcher() {
		this.dispatcher = null;
	}

	// ===================================================================
	// actions
	/**
	 * refresh the current date into calendar header
	 * 
	 * @param year
	 * @param month
	 * @param day
	 */
	private void refreshDateHeader(int year, int month, int day) {
		Calendar c = Calendar.getInstance();
		c.set(year, month, day);
		SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");
		currentDateButton.setText(formatter.format(c.getTime()));
	}

	/**
	 * go to the next month
	 */
	public void nextMonth() {
		// Loader<ArrayList<CalendarCell>> l = getLoaderManager().getLoader(
		// CALENDAR_LOADER);
		if (!getLoaderManager().hasRunningLoaders()) {
			if (helper != null) {
				helper.nextMonth();
				// update the current month into the arguments
				getArguments().putInt(CURRENT_MONTH, helper.getMonth());
				// update the current year into the arguments
				getArguments().putInt(CURRENT_YEAR, helper.getYear());
			}
			getLoaderManager().restartLoader(CALENDAR_LOADER, null, this);
		}
	}

	/**
	 * go the the previous month
	 */
	public void prevMonth() {

		// Loader<ArrayList<CalendarCell>> l = getLoaderManager().getLoader(
		// CALENDAR_LOADER);
		if (!getLoaderManager().hasRunningLoaders()) {
			if (helper != null) {
				// update the current month into the arguments
				getArguments().putInt(CURRENT_MONTH, helper.getMonth());
				// update the current year into the arguments
				getArguments().putInt(CURRENT_YEAR, helper.getYear());

				helper.previousMonth();
			}
			getLoaderManager().restartLoader(CALENDAR_LOADER, null, this);
		}
	}

	// getters
	/**
	 * 
	 * @return selected calendar month
	 */
	public int getCurrentMonth() {
		if (helper != null)
			return helper.getMonth();
		else
			return 0;
	}

	/**
	 * 
	 * @return selected calendar year
	 */
	public int getCurrentYear() {
		if (helper != null)
			return helper.getYear();
		else
			return 0;
	}

	/**
	 * 
	 * @return selected calendar day number (Month number) as integer
	 */
	public int getCurrentDay() {
		if (calendarAdapter != null) {
			ArrayList<Cell> data = calendarAdapter.getData();
			if (data != null) {
				for (Cell c : data) {
					if (c instanceof CalendarCell
							&& ((CalendarCell) c).selected) {
						return ((CalendarCell) c).dayNumber;
					}

				}
			}
		}

		return 1;
	}

	// ===========================================================================
	// adapters
	public static class CalendarAdapter extends BaseAdapter {
		private ArrayList<Cell> data;
		protected WeakReference<CalendarFragment> fragmentReference;
		private Drawable currentDayCellDrawable;
		private Drawable dataCellDrawable;
		private int selectableCellColor;
		private int weekendCellColor;
		private int unselectableCellColor;
		private int headerCellColor;

		public CalendarAdapter(CalendarFragment f) {
			this.fragmentReference = new WeakReference<CalendarFragment>(f);
			TypedArray a = f.getActivity().obtainStyledAttributes(
					R.styleable.calendarStyles);

			currentDayCellDrawable = a
					.getDrawable(R.styleable.calendarStyles_calendarCurrentDayMarker);
			dataCellDrawable = a
					.getDrawable(R.styleable.calendarStyles_calendarHasDataMarker);
			selectableCellColor = a.getColor(
					R.styleable.calendarStyles_calendarSelectableCellColor,
					0xFF000000);
			unselectableCellColor = a.getColor(
					R.styleable.calendarStyles_calendarUnselectableCellColor,
					0xFFCCCCCC);
			weekendCellColor = a.getColor(
					R.styleable.calendarStyles_calendarWeekendCellColor,
					0xFFFF0000);
			headerCellColor = a.getColor(
					R.styleable.calendarStyles_calendarHeaderCellColor,
					0xFFFFFFFF);
			a.recycle();
		}

		// setters
		public void setData(ArrayList<Cell> data) {
			this.data = data;
			notifyDataSetChanged();
		}

		// getters
		public int getCount() {
			return data != null ? data.size() : 0;
		}

		public Object getItem(int position) {
			return data != null ? data.get(position) : null;
		}

		public long getItemId(int position) {
			return position;
		}

		public ArrayList<Cell> getData() {
			return data;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return data.get(position) instanceof CalendarCell ? SELECTABLE_CELL
					: HEADER_CELL;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			if (getItemViewType(position) == SELECTABLE_CELL) {
				return true;
			}
			return false;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;
			CellHolder holder;

			if (fragmentReference.get() == null)
				return v;

			if (v == null) {
				LayoutInflater inflater = fragmentReference.get()
						.getLayoutInflater(null);
				v = inflater.inflate(R.layout.calendar_day_gridcell, parent,
						false);
				holder = new CellHolder();
				holder.cellTextView = (TextView) v.findViewById(R.id.dayCell);
				v.setTag(holder);
			}

			holder = (CellHolder) v.getTag();
			if (getItemViewType(position) == SELECTABLE_CELL) {
				CalendarCell cell = (CalendarCell) getItem(position);
				if (!cell.isSelectable)
					holder.cellTextView.setTextColor(unselectableCellColor);
				else if (cell.isWeekend)
					holder.cellTextView.setTextColor(weekendCellColor);
				else
					holder.cellTextView.setTextColor(selectableCellColor);

				holder.cellTextView.setText(String.valueOf(cell.dayNumber));

				if (cell.isCurrentDay && cell.selected) {
					// holder.cellTextView
					// .setBackgroundResource(R.drawable.calendar_selected_cell_button_selector);

					holder.cellTextView
							.setCompoundDrawablesWithIntrinsicBounds(null,
									currentDayCellDrawable, null,
									cell.data != null ? dataCellDrawable : null);
				} else if (cell.isCurrentDay) {
					holder.cellTextView
							.setCompoundDrawablesWithIntrinsicBounds(null,
									currentDayCellDrawable, null,
									cell.data != null ? dataCellDrawable : null);

				} else if (cell.selected) {
					// holder.cellTextView
					// .setBackgroundResource(R.drawable.calendar_selected_cell_button_selector);

					holder.cellTextView
							.setCompoundDrawablesWithIntrinsicBounds(null,

							null, null, cell.data != null ? dataCellDrawable
									: null);
				} else if (cell.data != null) {
					holder.cellTextView
							.setCompoundDrawablesWithIntrinsicBounds(null,
									null, null,
									cell.data != null ? dataCellDrawable : null);
				}
			} else {
				CalendarHeaderCell headerCell = (CalendarHeaderCell) getItem(position);
				holder.cellTextView.setTextColor(headerCellColor);
				holder.cellTextView.setText(headerCell.dayName);
			}
			return v;

		}

		// cell holder class
		public static final class CellHolder {
			public TextView cellTextView;
		}

		// cell class
		public static class CalendarCell implements Cell {
			public Date cellDate;
			public boolean isSelectable = true;
			public boolean selected = false;
			public int dayNumber;
			public int markerType = 0;
			public boolean isCurrentDay = false;
			public boolean isWeekend = false;
			public ArrayList<Parcelable> data;
		}

		public static class CalendarHeaderCell implements Cell {
			public String dayName;
			// public int cellColor = 0xFF000000;

		}

		public static interface Cell {
		}
	}

	// loaders
	public static class CalendarLoader extends AbstractAsyncLoader<Cell> {

		// private Date currentDay;
		// will be used to store the current date calendar data
		protected Calendar currentDayCalendar;
		protected WeakReference<CalendarFragment> fragmentReference;
		private final int prevSelectedDayNumber;

		public CalendarLoader(Context c, Calendar calendar, CalendarFragment f) {
			super(c);
			this.currentDayCalendar = calendar;
			prevSelectedDayNumber = f.getArguments().getInt(CURRENT_DAY, -1);
			fragmentReference = new WeakReference<CalendarFragment>(f);
			// initializeMonthDisplayHelper(currentDayCalendar);
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();
			if (fragmentReference.get() != null) {
				if (fragmentReference.get().dispatcher != null) {
					fragmentReference.get().dispatcher.onStartLoading();
				}
			}
		}

		/**
		 * used to re - initialize the month display helper when performing the
		 * nextMonth/prevMonth actions
		 * 
		 * @param c
		 */
		// public void initializeMonthDisplayHelper(Calendar c) {
		// helper = new MonthDisplayHelper(c.get(Calendar.YEAR),
		// c.get(Calendar.MONTH), Calendar.MONDAY);
		// }

		/**
		 * 
		 * used to re - initialize the month display helper when performing the
		 * nextMonth/prevMonth actions
		 * 
		 * @param c
		 */
		// public void initializeMonthDisplayHelper(int year, int month) {
		// helper = new MonthDisplayHelper(year,
		// month, Calendar.MONDAY);
		// }

		@Override
		public ArrayList<Cell> loadInBackground() {

			ArrayList<Cell> data = new ArrayList<Cell>();
			// we add first the day names cells
			CalendarHeaderCell monday = new CalendarHeaderCell();
			monday.dayName = "Mon";
			CalendarHeaderCell tuesday = new CalendarHeaderCell();
			tuesday.dayName = "Tue";
			CalendarHeaderCell wednesday = new CalendarHeaderCell();
			wednesday.dayName = "Wed";
			CalendarHeaderCell thursday = new CalendarHeaderCell();
			thursday.dayName = "Thu";
			CalendarHeaderCell friday = new CalendarHeaderCell();
			friday.dayName = "Fri";
			CalendarHeaderCell saturday = new CalendarHeaderCell();
			saturday.dayName = "Sat";
			CalendarHeaderCell sunday = new CalendarHeaderCell();
			sunday.dayName = "Sun";

			data.add(monday);
			data.add(tuesday);
			data.add(wednesday);
			data.add(thursday);
			data.add(friday);
			data.add(saturday);
			data.add(sunday);
			if (fragmentReference.get() == null
					|| fragmentReference.get().isDetached()
					|| fragmentReference.get().helper == null)
				return data;

			MonthDisplayHelper helper = fragmentReference.get().helper;

			Calendar c = Calendar.getInstance();
			c.set(helper.getYear(), helper.getMonth(), 0);
			int calendarCurrentDayOfMonth = currentDayCalendar
					.get(Calendar.DAY_OF_MONTH);
			// first day of the month flag
			boolean foundFirstDay = false;

			int[][] tmp = new int[6][7];
			for (int i = 0; i < tmp.length; i++) {
				int rowDaysNumber[] = helper.getDigitsForRow(i);
				ArrayList<CalendarCell> tmpCells = new ArrayList<CalendarCell>();
				for (int k = 0; k < rowDaysNumber.length; k++) {
					CalendarCell cell = new CalendarCell();
					cell.dayNumber = rowDaysNumber[k];
					if (k == 5 || k == 6)
						cell.isWeekend = true;
					tmpCells.add(cell);
				}

				CalendarCell tmpCell = null;
				for (int d = 0; d < rowDaysNumber.length; d++) {
					tmpCell = tmpCells.get(d);
					if (helper.isWithinCurrentMonth(i, d)) {
						// make the first day in month selected as default

						// tmpCell.cellColor = 0xFF000000;
						tmpCell.isSelectable = true;
						c.set(Calendar.DATE, tmpCell.dayNumber);
						// set the cell date
						tmpCell.cellDate = c.getTime();
						if (prevSelectedDayNumber != -1
								&& tmpCell.dayNumber == prevSelectedDayNumber)
							tmpCell.selected = true;

						if (!foundFirstDay) {
							foundFirstDay = true;
							fragmentReference.get().getArguments()
									.putInt(CURRENT_DAY, tmpCell.dayNumber);

						}
						// check if this cell date matches the current date
						if (c.get(Calendar.YEAR) == currentDayCalendar
								.get(Calendar.YEAR)
								&& c.get(Calendar.MONTH) == currentDayCalendar
										.get(Calendar.MONTH)
								&& calendarCurrentDayOfMonth == tmpCell.dayNumber)
							tmpCell.isCurrentDay = true;

					} else {
						// tmpCell.cellColor = 0xFFCCCCCC;
						tmpCell.isSelectable = false;
						// set the cell date
					}
				}

				data.addAll(tmpCells);

			}

			return data;
		}

	}

	// interfaces
	public interface CalendarListener {
		public void onCellSelected(CalendarCell cell);

		public void onStartLoading();

		public void onFinishedLoading();
	}
}
