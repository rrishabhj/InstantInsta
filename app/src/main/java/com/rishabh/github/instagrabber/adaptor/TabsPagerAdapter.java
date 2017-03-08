package com.rishabh.github.instagrabber.adaptor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.rishabh.github.instagrabber.tabs.DownloadFragment;
import com.rishabh.github.instagrabber.tabs.HistoryFragment;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:
			// Top Rated fragment activity
			DownloadFragment downloadFragment=new DownloadFragment();
			downloadFragment.setRetainInstance(true);
			return downloadFragment;
		case 1:
			// Games fragment activity
			HistoryFragment gamesFragment=HistoryFragment.newInstance();

			return gamesFragment;
		default:
			return new DownloadFragment();
		}
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 2;
	}


	@Override public CharSequence getPageTitle(int position) {
		if(position==0){
			return new String("Download");
		}else {
			return new String("History");
		}
	}

}
