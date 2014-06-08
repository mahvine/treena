package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import play.Logger;
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
	        	List<PlantedTrees> plantedTrees = PlantedTrees.find.where().eq("devices.id",device.id).findList();
	    		response.treesPlanted = plantedTrees.size();
	    		response.title = title(plantedTrees.size());
	    		response.progress = progress(device.discoveredTrees.size(), plantedTrees.size());
	    		return ok(Json.toJson(response));
    		}
    	}
    	return badRequest("invalid parameters");
    }
    
    private String title(int plantedTrees){
    	if(plantedTrees ==0){
    		return "Wanderer";
    	}else if(plantedTrees >=3 && plantedTrees <6){
    		return "Tree Observer";
    	}else if(plantedTrees >=7 && plantedTrees <9){
    		return "Tree Surveyor";
    	}else if(plantedTrees ==10){
    		return "Treant Protector";
    	}else if(plantedTrees >=11 && plantedTrees <15){
    		return "Tree Hugger";
    	}else {
    		return "Guardian";
    	}
    }
    
    private int progress(int treesOfLife,int plantedTrees){
    	int score = 0;
    	if(treesOfLife >=1 && treesOfLife <=3){
    		score+=12;
    	}else if(treesOfLife >=4 && treesOfLife <=6){
    		score+=18;
    	}else if(treesOfLife >=7 && treesOfLife <=8){
    		score+=24;
    	}else if(treesOfLife ==9 ){
    		score+=35;
    	}else if(treesOfLife ==10){
    		score+=42;
    	}
    	
    	if(treesOfLife >=1 && treesOfLife <=3){
    		score+=12;
    	}else if(treesOfLife >=4 && treesOfLife <=6){
    		score+=18;
    	}else if(treesOfLife >=7 && treesOfLife <=8){
    		score+=24;
    	}else if(treesOfLife ==9 ){
    		score+=35;
    	}else if(treesOfLife ==10){
    		score+=68;
    	}
    	return score;
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
				List<Tree> coveredTrees = coveredTrees(); 
	        	List<PlantedTrees> plantedTrees = PlantedTrees.find.where().eq("devices.id",device.id).findList();
	    		for(PlantedTrees plantedTree :plantedTrees){
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
	    		
	    		double allowance = 0.00044;
	    		List<Tree> newList = new ArrayList<Tree>();
	    		for(Tree coveredTree:coveredTrees){
	    			for(Tree someTree : trees){
	    		    		double distance = getDistance(someTree.lon, someTree.lat, coveredTree.lon, coveredTree.lat);
	    		    		if(distance < allowance){
	    		    			newList.add(coveredTree);
	    		    		}
	    			}
	    		}
	    		coveredTrees.removeAll(newList);
	    		coveredTrees.addAll(trees);
	    		
	    		Logger.info("New list size:"+coveredTrees.size());
	    		return ok(Json.toJson(coveredTrees));
    		}
    	}
    	return badRequest("invalid parameters");
    }
    
    public List<Tree> coveredTrees(){
    	double allowance = 0.00038;
    	List<Tree> trees = new ArrayList<Tree>();
    	for(double lat = 14.561536;lat<=14.565596;lat+=allowance ){
    		for(double lon = 120.991047;lon<=120.999277;lon+=allowance ){
    	    	Tree tree = new Tree();
    	    	tree.lat = lat;
    	    	tree.lon = lon;
    	    	tree.type = 2;
    	    	trees.add(tree);
    		}
    	}

		Logger.info("covered tree size:"+trees.size());
    	return trees;
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
		device.save();
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
    	double allowance = 0.000385;
    	List<PlantedTrees> plantedTrees = PlantedTrees.find.where().eq("devices.id",device.id).findList();
    	for(PlantedTrees tree :plantedTrees){
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
