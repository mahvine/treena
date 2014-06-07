package controllers;

import java.util.List;

import play.models.Devices;
import play.models.GoldenTrees;
import play.mvc.Controller;
import play.mvc.Result;

public class ViewController extends Controller{

	public Result mainPage(){
		String deviceId = request().getQueryString("did");
		if(deviceId!=null){
			Devices device = Devices.find.where().eq("deviceIdentifier", deviceId).findUnique();
			if(device!=null){
				return ok(views.html.changedevicelocation.render("change location", device.deviceIdentifier, device.latitude, device.longitude));
			}
		}
		List<GoldenTrees> goldenTrees = GoldenTrees.find.orderBy("id DESC").findList();
		return ok(views.html.main.render("Admin page",goldenTrees, null));
	}
	
	
}
