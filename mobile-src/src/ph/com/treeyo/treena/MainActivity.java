package ph.com.treeyo.treena;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;

import com.androidquery.callback.*;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.os.Build;

public class MainActivity extends ActionBarActivity {
	 private static LatLng myPosition = new LatLng(14.563615, 120.994996);
	 private static String server = "http://treena-smartdev1337.rhcloud.com";
    private GoogleMap mMap;
    private String deviceId;

    private AQuery aq;
    Marker myMarker;
    List<Marker> trees = new ArrayList<Marker>();
	@Override
	protected void onResume() {
		super.onResume();
		if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition,18));
            myMarker = mMap.addMarker(new MarkerOptions().position(myPosition).title("Me"));
            mMap.setOnMapClickListener(listener);
//                                      .icon(BitmapDescriptorFactory.fromResource(R.drawable.greentree)));
        }
        aq = new AQuery(getApplicationContext());
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        deviceId = telephonyManager.getDeviceId();
        System.out.println("deviceId|"+deviceId);
        renderDetails();
    	renderTrees();
	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        
    }
    
    public void renderDetails(){
		String url = server+"/details?did="+deviceId;
		        
		        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {
		                @Override
		                public void callback(String url, JSONObject json, AjaxStatus status) {
		                        if(json != null){
		                                //successful ajax call, show status code and json content
//		                                Toast.makeText(aq.getContext(), status.getCode() + ":" + json.toString(), Toast.LENGTH_LONG).show();
		                                try {
		                                	double latitude = json.getDouble("lat");
											double longitude = json.getDouble("lon");
											String title  = json.getString("title");
											int progress  = json.getInt("progress");
											myPosition = new LatLng(latitude, longitude);
											mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myPosition,18));
								            myMarker.setPosition(myPosition);
								            EditText rankEditText = (EditText) findViewById(R.id.rankEditText);
								            rankEditText.setText(title);
								            ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar1);;
								            progressBar.setProgress(progress);
										} catch (JSONException e) {
											System.out.println(e.toString());
										}
		                        }else{
		                                //ajax error, show error code
		                                Toast.makeText(aq.getContext(), "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
		                        }
		                }
		        });
    }
    
    

    public void renderTrees(){
		String url = server+"/trees?did="+deviceId;
		        
		        aq.ajax(url, JSONArray.class, new AjaxCallback<JSONArray>() {
		                @Override
		                public void callback(String url, JSONArray json, AjaxStatus status) {
		                        if(json != null){
		                        	for(Marker marker:trees){
		                        		marker.remove();
		                        	}
		                        	trees = new ArrayList<Marker>();
		                                //successful ajax call, show status code and json content
//		                                Toast.makeText(aq.getContext(), status.getCode() + ":" + json.toString(), Toast.LENGTH_LONG).show();
		                        		try {
		                                	for(int i =0;i<json.length() ;i++){
		                                		JSONObject jObject = json.getJSONObject(i);
		                                		double latitude = jObject.getDouble("lat");
		                                		double longitude = jObject.getDouble("lon");
		                                		int type = jObject.getInt("type");
		                                		LatLng treePosition =  new LatLng(latitude, longitude);
		                                		BitmapDescriptor icon =null;
		                                		if(type==0){
		                                			icon=BitmapDescriptorFactory.fromResource(R.drawable.greentree);
		                                			Marker treeMarker = mMap.addMarker(new MarkerOptions().position(treePosition).icon(icon));
		                                			trees.add(treeMarker);
		                                		}else if(type == 1){
		                                			icon=BitmapDescriptorFactory.fromResource(R.drawable.goldentree);
		                                			Marker treeMarker = mMap.addMarker(new MarkerOptions().position(treePosition).icon(icon));
		                                			trees.add(treeMarker);
		                                		}else{

		                                			icon=BitmapDescriptorFactory.fromResource(R.drawable.dark4);
		                                			Marker treeMarker = mMap.addMarker(new MarkerOptions().position(treePosition).icon(icon));
		                                			trees.add(treeMarker);
		                                		}
		                                	}
										} catch (JSONException e) {
											System.out.println(e.toString());
										}
		                        }else{
		                                //ajax error, show error code
		                                Toast.makeText(aq.getContext(), "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
		                        }
		                }
		        });
    }
    
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.refresh_map_view) {
        	renderDetails();
        	renderTrees();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    

    public void plantedClicked(View button){
    	String url = server+"/plant?did="+deviceId;
        
        aq.ajax(url, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject json, AjaxStatus status) {
                        if(json != null){
                                //successful ajax call, show status code and json content
                                try {
									String message = json.getString("message");
						            Toast.makeText(aq.getContext(), message, Toast.LENGTH_LONG).show();
						            renderTrees();
						            renderDetails();
								} catch (JSONException e) {
									System.out.println(e.toString());
								}
                        }else{
                                //ajax error, show error code
                                Toast.makeText(aq.getContext(), "Error:" + status.getCode(), Toast.LENGTH_LONG).show();
                        }
                }
        });
    }
    
    
    

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
    
    
    /****
     * onClickListeners
     */

    public OnMapClickListener listener = new OnMapClickListener() {
		@Override
		public void onMapClick(LatLng point) {
			String url = server+"/changelocation";
	        
		    Map<String, Object> params = new HashMap<String, Object>();
		    params.put("lon", point.longitude);
		    params.put("lat", point.latitude);
		    params.put("device", deviceId);
		    aq.ajax(url, params, JSONObject.class, new AjaxCallback<JSONObject>() {

		        @Override
		        public void callback(String url, JSONObject json, AjaxStatus status) {
		            renderTrees();
		            renderDetails();
		        }
		    });
		}
	};
    
    
}
