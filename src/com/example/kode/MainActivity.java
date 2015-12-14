package com.example.kode;

import google.com.android.cloudprint.PrintDialogActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import utils.net.HTTPGetPost;
import utils.net.NetworkUtil;
import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	static Context mContext;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
    }


    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

    	private static final String JSON_DATA = "data";
    	private static final String FILENAME = "collage.png";
    	private static final String JSON_USERLIST_DATA_USERNAME = "username";
    	private static final String JSON_USERLIST_DATA_FULLNAME = "full_name";
    	private static final String JSON_ID = "id";
    	
    	//private static final String ACCESS_TOKEN = "1419229302.32cf788.30e8bd13dd1d4da19c68d6f93974e4a8ё";
    	private static final String ACCESS_TOKEN = "1419229302.32cf788.b6f8c641ba364f1d9d8512347c884ce7";
    	
    	
    	private static final String JSON_RECENT_PAGINATION = "pagination";
    	private static final String JSON_RECENT_PAGINATION_NEXTURL = "next_url";
    	private static final String JSON_RECENT_DATA_TYPE = "type";
    	private static final String JSON_RECENT_DATA_TYPEIMAGE = "image";
    	private static final String JSON_RECENT_DATA_LIKES = "likes";
    	private static final String JSON_RECENT_DATA_IMAGES = "images";
    	
    	private static final String JSON_RECENT_DATA_IMAGES_THUMBNAIL = "thumbnail";
    	private static final String JSON_RECENT_DATA_IMAGES_STANDARD = "standard_resolution";
    	private static final String JSON_RECENT_DATA_IMAGES_URL = "url";
    	private static final String JSON_RECENT_DATA_IMAGES_WIDTH = "width";
    	private static final String JSON_RECENT_DATA_IMAGES_HEIGHT = "height";
    	private static final String JSON_RECENT_DATA_LIKES_COUNT = "count";
    	
    	private static final String JSON_NEXT_URL_END = "END";
    	
    	private static final int STATUS_USERLIST_RECIEVED = 100;
    	private static final int STATUS_POPULAR_RECIEVING = 110;
    	private static final int STATUS_POPULAR_RECIEVED = 120;
    	private static final int STATUS_IMAGES_RECIEVED = 130;
    	private static final int STATUS_EMPTY = 200;
    	
    	EditText iNameET;
    	List<String> usersList, idList;
    	List<InstaPopular> popularList;
    	List<SelectedPopular> selectedList;
    	
    	String next_url;
    	Boolean isPrint;
    	
    	Dialog mDialog;
    	int btnHeight;
    	
    	ProgressBar waitPB;
    	TextView waitTV;
    	ListView searchLV;
    	Button collageBtn;
    	ImageView collageIV;
    	WebView browser;
    	
    	private class InstaPopular {
    		String thumbnailURL;
    		String standardResolutionURL;
    		Integer likeCount, width, height;
    		
    		InstaPopular (String thumbnailURL, String standardResolutionURL, int likeCount, int width, int height) {
    			this.thumbnailURL = thumbnailURL;
    			this.standardResolutionURL = standardResolutionURL;
    			this.likeCount = likeCount;
    			this.width = width;
    			this.height = height;
    		}
    	}
    	
    	private class SelectedPopular {
    		String likes;
    		Drawable icon, standard;
    		int width, height;
    		boolean selected;
    		
    		SelectedPopular (String likes, Drawable icon, Drawable standard, boolean selected, int width, int height) {
    			this.likes = likes;
    			this.icon = icon;
    			this.standard = standard;
    			this.selected = selected;
    			this.width = width;
    			this.height = height;
    		}
    	}
    	
    	private class CollageBMP {
    		Bitmap bmp;
    		int width, height;
    		
    		CollageBMP (Bitmap bmp, int width, int height) {
    			this.bmp = bmp;
    			this.width = width;
    			this.height = height;
    		}
    	}
    	
    	private Handler mHandler = new Handler () {

            @Override
            public void handleMessage(Message msg) {
            	
            	switch (msg.what) {
    	        case STATUS_USERLIST_RECIEVED:
    	        	ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, usersList);
    	        	if (usersList.size() == 0) {
    	        		Toast.makeText(mContext, "Пользователь не найден!", Toast.LENGTH_SHORT).show();
    	        		mDialog.dismiss();
    	        		return;
    	        	}
    	        	mDialog.setTitle("Выберите пользователя");
    	        	searchLV.setAdapter(adapter);
    	        	waitPB.setVisibility(View.GONE);
    	        	waitTV.setVisibility(View.GONE);
    	        	searchLV.setVisibility(View.VISIBLE);
    	        	if (usersList.size() == 1) {
    	        		getPopular(idList.get(0));
    	        	}
    	        	
    	        	break;
    	        case STATUS_POPULAR_RECIEVING:
    	        	waitTV.setText("НАЙДЕНО " + msg.arg1 + "...");
    	        	break;
    	        case STATUS_POPULAR_RECIEVED:
    	        	waitTV.setText("ЗАГРУЖАЕМ ЛУЧШИЕ из " + popularList.size() + "...");
    	        	break;
    	        case STATUS_EMPTY:
    	        	Toast.makeText(mContext, "У пользователя нет изображений!", Toast.LENGTH_SHORT).show();
					mDialog.dismiss();
    	        	break;
    	        case STATUS_IMAGES_RECIEVED:
	        		MyArrayAdapter myAdapter = new MyArrayAdapter(selectedList);
	        		mDialog.setTitle("Выберите фото для коллажа");
    	        	searchLV.setAdapter(myAdapter);
    	        	waitPB.setVisibility(View.GONE);
    	        	waitTV.setVisibility(View.GONE);
    	        	searchLV.setVisibility(View.VISIBLE);
    	        	collageBtn.setVisibility(View.VISIBLE);
    	        	collageBtn.setText("Превью");
    	        	break;
    	        }
            }
        };
        
        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            iNameET = (EditText) rootView.findViewById(R.id.iNameET);
            Button getBtn = (Button) rootView.findViewById(R.id.getBtn);
            getBtn.setOnClickListener(getListener);
            
            return rootView;
        }
        
        View.OnClickListener getListener = new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if (NetworkUtil.getConnectivityStatus(mContext) == NetworkUtil.TYPE_NOT_CONNECTED) {
					Toast.makeText(mContext, "Необходимо подключение к сети интернет!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				final String name = iNameET.getText().toString();
				if (name.isEmpty()) {
					Toast.makeText(mContext, "Укажите пользователя", Toast.LENGTH_SHORT).show();
					return;
				}
				
				showDialog();
				usersList = new ArrayList<String>();
    			idList = new ArrayList<String>();
				
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
		    				String url = "https://api.instagram.com/v1/users/search?q=" + name + 
		    						"&access_token=" + ACCESS_TOKEN;
		    				String result = HTTPGetPost.getStringByUrl(url);
		    				if (!result.isEmpty()) {
		    					parseSearchResult(result);
		    				}
		    				
							mHandler.sendEmptyMessage(STATUS_USERLIST_RECIEVED);
		        		} catch (Exception e) {
		        			e.printStackTrace();
		        		}
					}
				});
				t.start();
			}
		};
		
		private void parseSearchResult(String result) {
			try {
				JSONArray dataArray = (new JSONObject(result)).getJSONArray(JSON_DATA);
				for (int i = 0; i < dataArray.length(); i++) {
        			JSONObject row = (JSONObject) dataArray.get(i);
        			String userName = row.getString(JSON_USERLIST_DATA_USERNAME);
        			String fullName = row.getString(JSON_USERLIST_DATA_FULLNAME);
        			String id = row.getString(JSON_ID);
        			
        			idList.add(id);
        			
        			if (userName.isEmpty())
        				usersList.add(fullName);
        			else if (fullName.isEmpty())
        				usersList.add(userName);
        			else
        				usersList.add(userName + " - " + fullName);
        		}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private String parsePopularResult(String result) {
			String nextURL = JSON_NEXT_URL_END;
			try {
				JSONObject paginationObject = (JSONObject) (new JSONObject(result)).get(JSON_RECENT_PAGINATION);
				if (paginationObject.has(JSON_RECENT_PAGINATION_NEXTURL)) {
					nextURL  = paginationObject.getString(JSON_RECENT_PAGINATION_NEXTURL);
				} 
				
				JSONArray dataArray = (new JSONObject(result)).getJSONArray(JSON_DATA);
				//Log.d("KYM", "parsePopularResult dataArray.length() = " + dataArray.length());
				for (int i = 0; i < dataArray.length(); i++) {
        			JSONObject row = ((JSONObject) dataArray.get(i));
        			
        			String type = row.getString(JSON_RECENT_DATA_TYPE);
        			//Log.d("KYM", "parsePopularResult type = " + type);
        			
        			if (!type.equalsIgnoreCase(JSON_RECENT_DATA_TYPEIMAGE))
        				continue;
        			
        			JSONObject likesObject = (JSONObject) row.get(JSON_RECENT_DATA_LIKES);
        			int likeCount = likesObject.getInt(JSON_RECENT_DATA_LIKES_COUNT);
        			
        			//Log.d("KYM", "parsePopularResult likeCount = " + likeCount);
        			
        			JSONObject imagesObject = (JSONObject) row.get(JSON_RECENT_DATA_IMAGES);
        			String tURL = ((JSONObject) imagesObject.get(JSON_RECENT_DATA_IMAGES_THUMBNAIL)).getString(JSON_RECENT_DATA_IMAGES_URL);
        			String sURL = ((JSONObject) imagesObject.get(JSON_RECENT_DATA_IMAGES_STANDARD)).getString(JSON_RECENT_DATA_IMAGES_URL);
        			int width = ((JSONObject) imagesObject.get(JSON_RECENT_DATA_IMAGES_STANDARD)).getInt(JSON_RECENT_DATA_IMAGES_WIDTH);
        			int height = ((JSONObject) imagesObject.get(JSON_RECENT_DATA_IMAGES_STANDARD)).getInt(JSON_RECENT_DATA_IMAGES_HEIGHT);
        			
        			popularList.add(new InstaPopular(tURL, sURL, likeCount, width, height));
        		}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return nextURL;
		}
		
		private void showDialog () {
			mDialog = new Dialog(mContext);
			mDialog.setTitle("Поиск пользователя...");
			mDialog.setContentView(R.layout.search_dialog);
			
			isPrint = false;
			waitPB = (ProgressBar) mDialog.findViewById(R.id.waitPB);
			waitTV = (TextView) mDialog.findViewById(R.id.waitTV);
			searchLV = (ListView) mDialog.findViewById(R.id.searchLV);
			collageIV = (ImageView) mDialog.findViewById(R.id.collageIV);
			collageBtn = (Button) mDialog.findViewById(R.id.collageBtn);
			btnHeight = collageBtn.getLayoutParams().height;
			collageBtn.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if (!isPrint) {
						collageBtn.setText("Печатать коллаж!");
						int collW, collH, out, count;
						collW = collH = 0;
						out = 6;
						List<CollageBMP> mbpList = new ArrayList<CollageBMP>();
						
						for (SelectedPopular item: selectedList) {
							if (item.selected) {
								mbpList.add(new CollageBMP(((BitmapDrawable)item.standard).getBitmap(), item.width, item.height));	
							}
						}
						
						count = mbpList.size();
						
						if (count == 1) {
							collW = mbpList.get(0).width;
							collH = mbpList.get(0).height;
						} else if (count == 2) {
							collW = out + mbpList.get(0).width + out + mbpList.get(1).width + out;
							collH = getMax(mbpList, false) + (out * 2);
						} else if (count == 3) {
							collW = out + mbpList.get(0).width + out + (mbpList.get(0).width / 2) + out;
							collH = out + mbpList.get(0).height + out;
						} else if (count == 4) {
							collW = out + mbpList.get(0).width + out + mbpList.get(1).width + out;
							collH = out + mbpList.get(2).height + out + mbpList.get(3).height + out;
						} else if (count == 5) {
							collW = out + mbpList.get(0).width + out + ((mbpList.get(0).height + out + mbpList.get(1).height) / 3) + out;
							collH = out + mbpList.get(0).height + out + mbpList.get(1).height + out;
						}
						
						Bitmap collageBMP = Bitmap.createBitmap(collW, collH, Bitmap.Config.ARGB_8888);
						Canvas refCanvas = new Canvas(collageBMP);
						
						int newSize;
						
						if (count == 1) {
							refCanvas.drawBitmap(mbpList.get(0).bmp, out, out, null);
						} else if (count == 2) {
							refCanvas.drawBitmap(mbpList.get(0).bmp, out, out, null);
							refCanvas.drawBitmap(mbpList.get(1).bmp, out + mbpList.get(0).width + out, out, null);
						} else if (count == 3) {
							refCanvas.drawBitmap(mbpList.get(0).bmp, out, out, null);
							newSize = (mbpList.get(0).height / 2) - (out / 2);
							Bitmap resized1 = Bitmap.createScaledBitmap(mbpList.get(1).bmp, newSize, newSize, false);
							Bitmap resized2 = Bitmap.createScaledBitmap(mbpList.get(2).bmp, newSize, newSize, false);
							refCanvas.drawBitmap(resized1, out + mbpList.get(0).width + out, out, null);
							refCanvas.drawBitmap(resized2, out + mbpList.get(0).width + out, out + newSize + out, null);
						} else if (count == 4) {
							refCanvas.drawBitmap(mbpList.get(0).bmp, out, out, null);
							refCanvas.drawBitmap(mbpList.get(1).bmp, out + mbpList.get(0).width + out, out, null);
							refCanvas.drawBitmap(mbpList.get(2).bmp, out, out + mbpList.get(0).height + out, null);
							refCanvas.drawBitmap(mbpList.get(3).bmp, out + mbpList.get(0).width + out, out + mbpList.get(1).height + out, null);
						} else if (count == 5) {
							refCanvas.drawBitmap(mbpList.get(0).bmp, out, out, null);
							refCanvas.drawBitmap(mbpList.get(1).bmp, out, out + mbpList.get(0).height + out, null);
							
							newSize = (mbpList.get(0).height + mbpList.get(1).height) / 3 - (out / 3);
							Bitmap resized2 = Bitmap.createScaledBitmap(mbpList.get(2).bmp, newSize, newSize, false);
							Bitmap resized3 = Bitmap.createScaledBitmap(mbpList.get(3).bmp, newSize, newSize, false);
							Bitmap resized4 = Bitmap.createScaledBitmap(mbpList.get(4).bmp, newSize, newSize, false);
							
							refCanvas.drawBitmap(resized2, out + getMax(mbpList, true) + out, out, null);
							refCanvas.drawBitmap(resized3, out + getMax(mbpList, true) + out, out + newSize + out, null);
							refCanvas.drawBitmap(resized4, out + getMax(mbpList, true) + out, out + newSize + out +	newSize + out, null);
						}
						
						collageIV.setImageBitmap(collageBMP);
						
						File mFile = new File(mContext.getFilesDir() + "/", FILENAME);
						try {
							mFile.createNewFile();
							FileOutputStream fos = new FileOutputStream(mFile);
							collageBMP.compress(Bitmap.CompressFormat.PNG, 90, fos);
							fos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						searchLV.setVisibility(View.GONE);
						collageIV.setVisibility(View.VISIBLE);
						
						LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) collageIV.getLayoutParams();
				        lp.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
				        collageIV.setLayoutParams(lp);
						isPrint = true;
						mDialog.setTitle("Превью коллажа");
					} else {
						Intent printIntent = new Intent(mContext, PrintDialogActivity.class);
						File file = new File(mContext.getFilesDir() + "/", FILENAME);
						Uri uri = Uri.fromFile(file);
						printIntent.setDataAndType(uri, "image/png");
						printIntent.putExtra("Collage", "Collage");
						startActivity(printIntent);
					}
					
					
				}
			});
			
			waitPB.setVisibility(View.VISIBLE);
			waitTV.setVisibility(View.INVISIBLE);
			searchLV.setVisibility(View.GONE);
			collageBtn.setVisibility(View.GONE);
			collageIV.setVisibility(View.GONE);
			
			searchLV.setOnItemClickListener(new AdapterView.OnItemClickListener(){
		        @Override
		        public void onItemClick(AdapterView<?> a, View v, int position, long l) {
		            // TODO Auto-generated method stub
		        	getPopular(idList.get(position));
		        }
		     });
			
			mDialog.show();
		}
		
		private int getMax(List<CollageBMP> cBMP, boolean width) {
			int max = 0;
			if (width) {
				
				if (cBMP.size() == 3) {
					max = cBMP.get(1).width;	
					for (int i = 2; i < cBMP.size(); i++) {
						if (cBMP.get(i).width > max)
							max = cBMP.get(i).width; 
					}
				} else {
					max = cBMP.get(2).width;	
					for (int i = 3; i < cBMP.size(); i++) {
						if (cBMP.get(i).width > max)
							max = cBMP.get(i).width; 
					}
				}
			} else {
				max = cBMP.get(0).height;
				for (int i = 1; i < cBMP.size(); i++) {
					if (cBMP.get(i).height > max)
						max = cBMP.get(i).height; 
				}
			}
			
			
			return max;
		}
		
		private void getPopular(final String userID) {
			mDialog.setTitle("Поиск изображений...");
			waitPB.setVisibility(View.VISIBLE);
        	waitTV.setVisibility(View.VISIBLE);
			searchLV.setVisibility(View.GONE);
			
        	popularList = new ArrayList<InstaPopular>();
        	next_url = "";
        	
        	Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					while (!next_url.equalsIgnoreCase(JSON_NEXT_URL_END)) {
						try {
							String url = "";
							if (next_url.isEmpty())
								url = "https://api.instagram.com/v1/users/" + userID + 
		    						"/media/recent?access_token=" + ACCESS_TOKEN;
							else
								url = next_url;
		    				String result = HTTPGetPost.getStringByUrl(url);
		    				if (!result.isEmpty()) {
		    					String nextURL = parsePopularResult(result);
		    					next_url = nextURL;
		    				} else {
		    					next_url = JSON_NEXT_URL_END;
		    				}
		    				
		    				Message message = Message.obtain(mHandler, STATUS_POPULAR_RECIEVING, popularList.size(), 0);
		    	    		message.sendToTarget();
		    				
		    				//Log.d("KYM", "Thread popularList.size() = " + popularList.size());
		        		} catch (Exception e) {
		        			e.printStackTrace();
		        		}
					}
					
					if (popularList.size() == 0) {
						mHandler.sendEmptyMessage(STATUS_EMPTY);
						return;
					}
					
					mHandler.sendEmptyMessage(STATUS_POPULAR_RECIEVED);
					
					Collections.sort(popularList, new PopularComparator());
					
					selectedList = new ArrayList<SelectedPopular>();
    	        	Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							for (int i = 0; i < popularList.size(); i++) {
		    	        		if (i >= 5)
		    	        			break;

		    	        		Drawable icon = HTTPGetPost.getDrawableByUrl(popularList.get(i).thumbnailURL);
								Drawable standard = HTTPGetPost.getDrawableByUrl(popularList.get(i).standardResolutionURL);
								selectedList.add(new SelectedPopular(String.valueOf(popularList.get(i).likeCount), icon, standard, true, 
										popularList.get(i).width, popularList.get(i).height));
		    	        		
		    	        	}
							
							mHandler.sendEmptyMessage(STATUS_IMAGES_RECIEVED);
						}
					});
					t.start();
				}
			});
			t.start();
		}
		
		private class PopularComparator implements Comparator<InstaPopular> {
		    @Override
		    public int compare(InstaPopular o1, InstaPopular o2) {
		        return o2.likeCount.compareTo(o1.likeCount);
		    }
		}
		
		private class MyArrayAdapter extends ArrayAdapter<SelectedPopular> {

			private class VHolder {
				private CheckBox selectedCB;
				private TextView itemTV;
				private ImageView iconIV;
			}

			public MyArrayAdapter(List<SelectedPopular> list) {
				super(mContext, R.layout.search_dialog_items, R.id.itemTV, list);
			}

			public View getView(final int position, final View convertView,	final ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				if (v != convertView && v != null) {
					final VHolder holder = new VHolder();
					holder.itemTV = (TextView) v.findViewById(R.id.itemTV);
					holder.selectedCB = (CheckBox) v.findViewById(R.id.selectedCB);
					holder.selectedCB.setTag(position);
					holder.selectedCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
						
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							// TODO Auto-generated method stub
							
							int count = 0;
							for (SelectedPopular item: selectedList) {
								if (item.selected)
									count++;
							}
							
							if (count == 1 && !isChecked) {
								buttonView.setChecked(true);
								return;
							}
							
							int index = (Integer)buttonView.getTag();
							getItem(index).selected = isChecked;
						}
					});
					holder.iconIV = (ImageView) v.findViewById(R.id.imageView1);
					
					v.setTag(holder);
				} else {
				      v = convertView;
				      ((VHolder) v.getTag()).selectedCB.setTag(position);
				}
				
		        VHolder holder = (VHolder) v.getTag();
		        holder.iconIV.setImageDrawable(getItem(position).icon);
		        
		        holder.itemTV.setText("Likes = " + getItem(position).likes);
		        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) holder.itemTV.getLayoutParams();
		        lp.gravity = Gravity.CENTER_VERTICAL;
		        holder.itemTV.setLayoutParams(lp);
		        
				holder.selectedCB.setChecked(getItem(position).selected);
				
				return v;
			}
		}
    }

}
