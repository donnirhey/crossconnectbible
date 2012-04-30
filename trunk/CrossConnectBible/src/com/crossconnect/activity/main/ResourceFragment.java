package com.crossconnect.activity.main;

/*
 * Copyright 2011 Gary Lo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.util.List;

import musicplayer.MusicService;
import net.londatiga.android.QuickActionVertical;
import net.londatiga.qahorizontal.ActionItem;
import net.londatiga.qahorizontal.QuickActionHorizontal;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.crossconnect.activity.ArticleActivity;
import com.crossconnect.model.BibleText;
import com.crossconnect.model.OnlineAudioResource;
import com.crossconnect.service.CrossConnectServerService;
import com.crossconnect.service.ResourceService;
import com.crossconnect.views.BibleTextViewImpl;
import com.crossconnect.views.NotesEditText;
import com.crossconnect.views.R;

public class ResourceFragment extends ListFragment implements LoaderManager.LoaderCallbacks<List<OnlineAudioResource>> {
	
	private static final String TAG = "ResourceFragment";
	
    private ResourceService resourceService;        
    
    private OnClickListener downloadListener;
    
    private NotesEditText notesEditText;

    private ImageButton notesLock;
    
    ResourceLoader resourceListLoader;


    public void updateResources(BibleText bibleText) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("BibleText", bibleText);
        getLoaderManager().restartLoader(0, bundle, this);
}


    
    
    private void loadNotes() {
        BibleTextViewImpl bibleTextView = (BibleTextViewImpl) getActivity().findViewById(R.id.bible_text);
        
        if (bibleTextView != null && bibleTextView.getBibleText() != null) {
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            Bundle bundle = new Bundle();
            bundle.putParcelable("BibleText", bibleTextView.getBibleText());
            getLoaderManager().initLoader(0, bundle, this);
        }
    }
    
    @Override public void onListItemClick(ListView l, View v, int pos, long id) {
    	
    	final int position = pos;
    	
        //Get the corresponding resource
        final OnlineAudioResource resource = mAdapter.getItem(position);

    	
        final ActionItem playAction = new ActionItem();
        
        playAction.setTitle("Play");
        playAction.setIcon(getResources().getDrawable(R.drawable.kontak));

        final ActionItem downloadAction = new ActionItem();
        
        downloadAction.setTitle("Download");
        downloadAction.setIcon(getResources().getDrawable(R.drawable.kontak));
        
        final ActionItem readAction = new ActionItem();
        
        readAction.setTitle("Read");
        readAction.setIcon(getResources().getDrawable(R.drawable.kontak));

    	
        final QuickActionHorizontal mQuickAction  = new QuickActionHorizontal(v);
        
        final String text               = "blah";
        
        
        playAction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Streaming Audio File " + text, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Streaming Audio" + mAdapter.getItem(position).getResourceName() + "from " + mAdapter.getItem(position).getAudioURL());
                try {
                    // the download happens in another thread
                    Intent i = new Intent(MusicService.ACTION_URL);
                    Uri uri = Uri.parse(mAdapter.getItem(position).getAudioURL());
                    i.setData(uri);
                    getActivity().startService(i);

                } catch (Exception e) {
                    Log.e("BookManagerActivity", "Error on attempt to download", e);
                }

                mQuickAction.dismiss();
            }
        });

        downloadAction.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(), "Downloading Audio File " + text, Toast.LENGTH_SHORT).show();
        		Log.d(TAG, "Downloading Audio" + mAdapter.getItem(position).getResourceName() + "from " + mAdapter.getItem(position).getAudioURL());

        		Request request = new Request(Uri.parse(resource.getAudioURL()));
        	    ((DownloadManager) getActivity().getSystemService("download")).enqueue(request);

                mQuickAction.dismiss();
            }
        });
        
        //Check the links are actually there
        mQuickAction.addActionItem(playAction);
        mQuickAction.addActionItem(downloadAction);
        if (resource.getReadURL() != null) {
            readAction.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getActivity(), "Read " + text, Toast.LENGTH_SHORT).show();
            		Intent intent = new Intent(getActivity(), ArticleActivity.class);
            		if(resource.getReadURL() != null){
            			intent.putExtra("url",resource.getReadURL());
            			startActivity(intent);			
            		}
                    
                    mQuickAction.dismiss();
                }
            });
        	mQuickAction.addActionItem(readAction);
        }
        
        mQuickAction.setAnimStyle(QuickActionVertical.ANIM_AUTO);
        
        mQuickAction.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
            }
        });
        
        mQuickAction.show();
    }

    	
    
    
    /**
     * A custom Loader that loads all of the installed applications.
     */
    public static class ResourceLoader extends AsyncTaskLoader<List<OnlineAudioResource>> {

        List<OnlineAudioResource> onlineResources;
        
        BibleText bibleText;

        public ResourceLoader(Context context, BibleText bibleText) {
            super(context);
            this.bibleText = bibleText;
        }

        /**
         * This is where the bulk of our work is done.  This function is
         * called in a background thread and should generate a new set of
         * data to be published by the loader.
         */
        @Override public List<OnlineAudioResource> loadInBackground() {
			Log.e(TAG, "Load in Background");

            //resourceService.doSomething();
            return new CrossConnectServerService().getOnlineResource(bibleText.getBook());
        }

        /**
         * Called when there is new data to deliver to the client.  The
         * super class will take care of delivering it; the implementation
         * here just adds a little more logic.
         */
        @Override public void deliverResult(List<OnlineAudioResource> books) {
            if (isReset()) {
                // An async query came in while the loader is stopped.  We
                // don't need the result.
                if (books != null) {
                    onReleaseResources(books);
                }
            }
            List<OnlineAudioResource> oldApps = books;
            onlineResources = books;

            if (isStarted()) {
                // If the Loader is currently started, we can immediately
                // deliver its results.
                super.deliverResult(books);
            }

            // At this point we can release the resources associated with
            // 'oldApps' if needed; now that the new result is delivered we
            // know that it is no longer in use.
            if (oldApps != null) {
                onReleaseResources(oldApps);
            }
        }

        /**
         * Handles a request to start the Loader.
         */
        @Override protected void onStartLoading() {
            if (onlineResources != null) {
                // If we currently have a result available, deliver it
                // immediately.
                deliverResult(onlineResources);
            } else {
            	forceLoad();
            }
        }

        /**
         * Handles a request to stop the Loader.
         */
        @Override protected void onStopLoading() {
            // Attempt to cancel the current load task if possible.
            cancelLoad();
        }

        /**
         * Handles a request to cancel a load.
         */
        @Override public void onCanceled(List<OnlineAudioResource> apps) {
            super.onCanceled(apps);

            // At this point we can release the resources associated with 'apps'
            // if needed.
            onReleaseResources(apps);
        }

        /**
         * Handles a request to completely reset the Loader.
         */
        @Override protected void onReset() {
            super.onReset();

            // Ensure the loader is stopped
            onStopLoading();

            // At this point we can release the resources associated with 'apps'
            // if needed.
            if (onlineResources != null) {
                onReleaseResources(onlineResources);
                onlineResources = null;
            }
        }

        /**
         * Helper function to take care of releasing resources associated
         * with an actively loaded data set.
         */
        protected void onReleaseResources(List<OnlineAudioResource> apps) {
            // For a simple List<> there is nothing to do.  For something
            // like a Cursor, we would close it here.
        }
    }



    public static class ResourceListAdapter extends ArrayAdapter<OnlineAudioResource> {
        private final LayoutInflater mInflater;

        public ResourceListAdapter(Context context) {
            super(context, R.layout.list_item_text);
            mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public void setData(List<OnlineAudioResource> data) {
            clear();
            //AddAll is not supported till 3.x
            if(data != null) {
                for (OnlineAudioResource book : data) {
                    if (book != null) {
                        add(book);
                    }
                }
            }
        }

        /**
         * Populate new items in the list.
         */
        @Override public View getView(int position, View convertView, ViewGroup parent) {
            View view;

            //Convertview will not be null if already drawn so no need to redraw
            if (convertView == null) {
                view = mInflater.inflate(R.layout.list_item_icon_text, parent, false);
            } else {
                view = convertView;
            }

            OnlineAudioResource item = getItem(position);
//            ((ImageView)view.findViewById(R.id.icon)).setImageDrawable(item.getIcon());
            ((TextView)view.findViewById(R.id.text)).setText(item.getResourceName() + " " + item.getResourceVerse());

            return view;
        }
    }

    

	
    // This is the Adapter being used to display the list's data.
    ResourceListAdapter mAdapter;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        //Start with progress indicator
        setListShown(false);
        
        BibleTextViewImpl bibleTextView = (BibleTextViewImpl) getActivity().findViewById(R.id.bible_text);
        
        if (bibleTextView != null && bibleTextView.getBibleText() != null) {
            // Prepare the loader.  Either re-connect with an existing one,
            // or start a new one.
            Bundle bundle = new Bundle();
            bundle.putParcelable("BibleText", bibleTextView.getBibleText());
            getLoaderManager().initLoader(0, bundle, this);
        }

        
        // Give some text to display if there is no data.  In a real
        // application this would come from a resource.
        setEmptyText("Sorry no Online Resources were received");


        // Create an empty adapter we will use to display the loaded data.
        mAdapter = new ResourceListAdapter(getActivity());
        setListAdapter(mAdapter);
        
        notesEditText = ((NotesEditText) getActivity().findViewById(R.id.notes_edit_text));
        notesLock = ((ImageButton) getActivity().findViewById(R.id.menu_button_notes_lock));
        notesEditText.initialise((notesLock), bibleTextView);
        bibleTextView.post(new Runnable(){

            @Override
            public void run() {
                loadNotes();                
            }});
        
        
    }


    @Override public Loader<List<OnlineAudioResource>> onCreateLoader(int id, Bundle args) {
        // This is called when a new Loader needs to be created.  
    	Log.i("LoaderCustom", "onCreateLoader()");
    	resourceListLoader = new ResourceLoader(getActivity(),(BibleText) args.getParcelable("BibleText"));
        return resourceListLoader;
    }


	@Override
	public void onLoadFinished(Loader<List<OnlineAudioResource>> loader,
			List<OnlineAudioResource> data) {
    	Log.i(TAG, "onLoadFinished");
        // Set the new data in the adapter.
        mAdapter.setData(data);
        
        setListShown(true);

		
	}


	@Override
	public void onLoaderReset(Loader<List<OnlineAudioResource>> arg0) {
        // Clear the data in the adapter.
        mAdapter.setData(null);
	}
	    
}