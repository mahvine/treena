package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.data.DynamicForm;
import play.data.Form;
import play.http.DetailsResponse;
import play.http.Tree;
import play.libs.Json;
import play.models.Devices;
import play.models.GoldenTrees;
import play.models.PlantedTrees;
import play.mvc.Controller;
import play.mvc.Result;

public class Application extends Controller {

    public static Result index() {
        return ok("Your new application is ready.");
    }
    
    public Result details(){
    	String deviceId = request().getQueryString("did");
    	if(deviceId!=null){
    		if(!deviceId.isEmpty()){
    			Devices device = Devices.find.where().ieq("deviceIdentifier", deviceId).findUnique();
	    		if(device==null){
	    			device = new Devices();
	    			device.deviceIdentifier = deviceId;
	    			device.save();
	    			device.refresh();
	    		}
	    		DetailsResponse response = new DetailsResponse();
	    		response.lat = device.latitude;
	    		response.lon = device.longitude;
	    		response.treesOfLife = device.discoveredTrees.size();
	    		response.treesPlanted = device.plantedTrees.size();
	    		//TODO getTitle
	    		return ok(Json.toJson(response));
    		}
    	}
    	return badRequest("invalid parameters");
    }
    
    
    public Result plant(){
    	String deviceId = request().getQueryString("did");
    	if(deviceId!=null){
    		if(!deviceId.isEmpty()){
    			Devices device = Devices.find.where().ieq("deviceIdentifier", deviceId).findUnique();
	    		if(device==null){
	    			device = new Devices();
	    			device.deviceIdentifier = deviceId;
	    			device.save();
	    			device.refresh();
	    		}
	    		
	    		
	    		
	    		Map<String,Object> mapResponse = new HashMap<String,Object>();
	    		long goldenTreeId = isGoldenTreeSite(device);
	    		if(goldenTreeId > -1){
	    			GoldenTrees goldenTree = GoldenTrees.find.byId(goldenTreeId);
	    			if(device.discoveredTrees.contains(goldenTree)){
		    			mapResponse.put("message", "Golden tree site :"+goldenTree.areaname);
	    			}else{
	    				device.discoveredTrees.add(goldenTree);
	    				device.save();
	    				mapResponse.put("message", "You discovered a tree planting site called "+goldenTree.areaname);
	    			}
	    		}else{
		    		if(isValidPoint(device)){
			    		PlantedTrees plantedTrees = new PlantedTrees();
			    		plantedTrees.devices = device;
			    		plantedTrees.latitude = device.latitude;
			    		plantedTrees.longitude = device.longitude;
			    		plantedTrees.save();
			    		mapResponse.put("message", "Successfully planted a tree :)");
		    		}else{
		    			mapResponse.put("message", "Cannot plant a tree near other trees :(");
		    		}
	    		}
	    		return ok(Json.toJson(mapResponse));
    		}
    	}
    	return badRequest("Invalid parameters");
    }
    
    
    public Result trees(){
    	String deviceId = request().getQueryString("did");
    	if(deviceId!=null){
    		if(!deviceId.isEmpty()){
    			Devices device = Devices.find.where().ieq("deviceIdentifier", deviceId).findUnique();
	    		if(device==null){
	    			device = new Devices();
	    			device.deviceIdentifier = deviceId;
	    			device.save();
	    			device.refresh();
	    		}
	    		
	    		List<Tree> trees =  new ArrayList<Tree>();
	    		for(PlantedTrees plantedTree :device.plantedTrees){
	    			Tree tree = new Tree();
	    			tree.lat = plantedTree.latitude;
	    			tree.lon = plantedTree.longitude;
	    			tree.type = 0;
	    			trees.add(tree);
	    		}
	    		for(GoldenTrees goldenTree :device.discoveredTrees){
	    			Tree tree = new Tree();
	    			tree.lat = goldenTree.latitude;
	    			tree.lon = goldenTree.longitude;
	    			tree.type = 1;
	    			trees.add(tree);
	    		}
	    		return ok(Json.toJson(trees));
    		}
    	}
    	return badRequest("invalid parameters");
    }
    
    
    public Result createGoldenTree(){
    	DynamicForm formData = DynamicForm.form().bindFromRequest();
    	String lon = formData.get("lon");
    	String lat = formData.get("lat");
    	String areaname = formData.get("areaname");
    	double longitude = Double.parseDouble(lon);
    	double latitude = Double.parseDouble(lat);
    	GoldenTrees goldenTree = new GoldenTrees();
    	goldenTree.areaname = areaname;
    	goldenTree.latitude = latitude;
    	goldenTree.longitude = longitude;
    	goldenTree.save();
    	
    	return ok(Json.toJson(goldenTree));
    }
    

    public Result changeLocation(){
    	DynamicForm formData = DynamicForm.form().bindFromRequest();
    	String lon = formData.get("lon");
    	String lat = formData.get("lat");
    	String deviceIdentifier = formData.get("device");
    	double longitude = Double.parseDouble(lon);
    	double latitude = Double.parseDouble(lat);
    	Devices device = Devices.find.where().eq("deviceIdentifier", deviceIdentifier).findUnique();
    	if(device == null){
    		device = new Devices();
    		device.deviceIdentifier = deviceIdentifier;
    	}
    	device.latitude = latitude;
    	device.longitude = longitude;
    	Map<String,Object> responseMap = new HashMap<String,Object>();
    	responseMap.put("message", "Changed location!");
    	return ok(Json.toJson(responseMap));
    }
    
    
    

    public static long isGoldenTreeSite(Devices device){
    	double allowance = 0.000585;
    	for(GoldenTrees tree : GoldenTrees.find.all()){
    		double distance = getDistance(device.longitude, device.latitude, tree.longitude, tree.latitude);
    		if(distance <= allowance){
    			return tree.id;
    		}
    	}
    	return -1;
    }
    
    
    public static boolean isValidPoint(Devices device){
    	double allowance = 0.000485;
    	for(PlantedTrees tree :device.plantedTrees){
    		double distance = getDistance(device.longitude, device.latitude, tree.longitude, tree.latitude);
    		if(distance <= allowance){
    			return false;
    		}
    	}
    	return true;
    }
    
    
    /**
	 * distance formula between to point1 and point2
	 * @param x1 120.995361 longitude
	 * @param y1 14.563687 lat
	 * @param x2 120.995629 longitude
	 * @param y2 14.563282 lat
	 * @return 0.000485
	 */
	public static double getDistance(double x1,double y1,double x2,double y2){
		double x = Math.abs(x2-x1);
		double y = Math.abs(y2-y1);
		double distance= Math.abs(Math.sqrt(x*x+y*y));
		return distance;
	}
    
    public static void main(String[] args){
    	System.out.println(getDistance(120.995361,14.563687,120.995629,14.563282));
    }

	
}
