package mc.app.components.calendarwidget;

import java.util.HashMap;

import mc.app.components.calendarwidget.fragments.CalendarFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentManager.BackStackEntry;

public class MainActivity extends FragmentActivity {

	private CustomFragmentManager customFragmentManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		customFragmentManager = new CustomFragmentManager(this);
		customFragmentManager.switchToFragment(CalendarFragment.newInstance(), null,
				true);

	}

	public static class CustomFragmentManager {
		private FragmentActivity activity;

		private final HashMap<String, FragmentInfo> fragmentsStore = new HashMap<String, FragmentInfo>();

		static final class FragmentInfo {
			private Fragment fragment;

			// private String transaction;

			public FragmentInfo(Fragment f, String transaction) {
				this.fragment = f;
				// this.transaction=transaction;
			}
		}

		public CustomFragmentManager(FragmentActivity a) {
			this.activity = a;
		}

		public void clearBackstack() {
			if (activity.getSupportFragmentManager().getBackStackEntryCount() > 0) {
				BackStackEntry entry = activity.getSupportFragmentManager()
						.getBackStackEntryAt(0);
				if (entry != null) {
					int id = entry.getId();
					// get the first entry id and consume all the entries until
					// the matched one, inclusive the matched one
					activity.getSupportFragmentManager().popBackStackImmediate(
							id, FragmentManager.POP_BACK_STACK_INCLUSIVE);
				}
			}
		}

		/**
		 * navigate to a specific fragment
		 * 
		 * @param f
		 * @param transactionId
		 */
		public void switchToFragment(Fragment f, String transactionId,
				Boolean withAddToBackstack) {

			Fragment toAdd = f;
			if (transactionId != null
					&& fragmentsStore.containsKey(transactionId)) {
				toAdd = fragmentsStore.get(transactionId).fragment;
				toAdd.setArguments(f.getArguments());
			} else if (transactionId != null) {
				fragmentsStore.put(transactionId, new FragmentInfo(toAdd,
						transactionId));
			}
			Fragment currentFragment = activity.getSupportFragmentManager()
					.findFragmentById(R.id.fragmentContainer);
			FragmentTransaction ft = activity.getSupportFragmentManager()
					.beginTransaction();
			// ft.setCustomAnimations(R.anim.fragment_slide_left_enter,
			// R.anim.fragment_slide_left_exit,
			// R.anim.fragment_slide_right_enter,
			// R.anim.fragment_slide_right_exit);
			if (currentFragment != null) {
				ft.replace(R.id.fragmentContainer, toAdd);
			} else {
				ft.add(R.id.fragmentContainer, toAdd);
			}

			if (withAddToBackstack)
				ft.addToBackStack(transactionId);

			ft.commit();
		}

		public void clearCache() {
			if (fragmentsStore != null)
				fragmentsStore.clear();
		}

	}
}
