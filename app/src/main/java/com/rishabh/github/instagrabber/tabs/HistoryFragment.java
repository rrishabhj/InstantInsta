package com.rishabh.github.instagrabber.tabs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import com.rishabh.github.instagrabber.R;
import com.rishabh.github.instagrabber.adaptor.ImageRecyclerAdaptor;
import com.rishabh.github.instagrabber.database.DBController;

public class HistoryFragment extends Fragment {

	ImageView ivSettings;
	private FragmentActivity mContext;
	private RecyclerView rvInsta;

	//DB
	private DBController dbcon;
	private ImageRecyclerAdaptor imageRecyclerAdaptor;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("Tag1","MoviesFrag");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View rootView = inflater.inflate(R.layout.fragment_history, container, false);
		mContext =getActivity();
		//DB
		dbcon = new DBController(mContext);

		rvInsta= (RecyclerView) rootView.findViewById(R.id.rvInstaImages);
		imageRecyclerAdaptor = new ImageRecyclerAdaptor(dbcon.getAllInstaImages(),mContext);

		rvInsta.setAdapter(imageRecyclerAdaptor);
		rvInsta.setLayoutManager(new LinearLayoutManager(mContext));


			return rootView;
	}

	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
}
