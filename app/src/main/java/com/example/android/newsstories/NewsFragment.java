package com.example.android.newsstories;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Greta Grigutė on 2018-05-09.
 */
public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<NewsStory>> {
    private static final int NEWS_LOADER_ID = 1;
    boolean isWifiConn;
    boolean isMobileConn;
    //please add the API key in the gradle.properties like this:
    //NewsStories_GuardianApiKey="your-key"
    private String GUARDIAN_REQUEST_URL;
    private RecyclerView recyclerView;
    private StoryAdapter adapter;
    private RelativeLayout empty;
    private RelativeLayout noInternet;
    private ProgressBar loadingSpinner;

    public NewsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Check the device network connection
        ConnectivityManager connManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        isWifiConn = networkInfo.isConnected();
        networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        isMobileConn = networkInfo.isConnected();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment, container, false);

        GUARDIAN_REQUEST_URL = getArguments().getString(Constants.URL_KEY);
        recyclerView = rootView.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Find a reference to the loading spinner{@link ProgressBar} in the layout
        loadingSpinner = rootView.findViewById(R.id.loading_spinner);

        // Create a new {@link ArrayAdapter} of stories
        adapter = new StoryAdapter(getActivity(), new ArrayList<NewsStory>());

        // Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        recyclerView.setAdapter(adapter);
        // Find a reference to the no stories layout {@link RelativeLayout} in the layout
        empty = rootView.findViewById(R.id.no_stories_layout);
        noInternet = rootView.findViewById(R.id.no_internet_layout);
        recyclerView.setVisibility(View.GONE);
        empty.setVisibility(View.VISIBLE);

        if (!isWifiConn && !isMobileConn) {
            loadingSpinner.setVisibility(View.GONE);
            noInternet.setVisibility(View.VISIBLE);
        } else {
            getLoaderManager().initLoader(NEWS_LOADER_ID, null, this);
            noInternet.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getContext(), recyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                String url = adapter.getItem(position).getUrl();
                if (url != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    startActivity(intent);
                }
            }
        }));

        return rootView;
    }

    @Override
    public Loader<List<NewsStory>> onCreateLoader(int id, Bundle args) {
        return new StoryLoader(getActivity(), GUARDIAN_REQUEST_URL);

    }

    @Override
    public void onLoadFinished(Loader<List<NewsStory>> loader, List<NewsStory> newsStories) {
        // Clear the adapter of previous news data
        adapter = new StoryAdapter(getActivity(), new ArrayList<NewsStory>());
        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.
        if (newsStories != null && !newsStories.isEmpty()) {
            adapter = new StoryAdapter(getActivity(), newsStories);
            loadingSpinner.setVisibility(View.GONE);
            empty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(adapter);
        } else {
            empty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            loadingSpinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<NewsStory>> loader) {
        adapter.setNews(new ArrayList<NewsStory>());
    }
}
