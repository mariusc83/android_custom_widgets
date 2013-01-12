package mc.app.components.calendarwidget;

import mc.app.components.calendarwidget.utils.GingerBreadStrictMode;
import mc.app.components.calendarwidget.utils.HoneyCombStrictMode;
import mc.app.components.calendarwidget.utils.IStrictMode;
import android.app.Application;

public class CalendarWidgetApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		if (CalendarWidgetConstants.DEVELOPER_MODE) {
			IStrictMode strictMode = null;
			if (CalendarWidgetConstants.SUPPORTS_HONEYCOMB)
				strictMode = new HoneyCombStrictMode();
			else if (CalendarWidgetConstants.SUPPORTS_GINGERBREAD)
				strictMode = new GingerBreadStrictMode();
			if (strictMode != null)
				strictMode.enableStrictMode();
		}

	}
}
