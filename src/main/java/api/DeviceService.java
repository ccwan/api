package api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("/device")
public class DeviceService {

	@GET
	@Path("/namelist")
	@Produces("application/json")
	public ArrayList <String> getDeviceNameList() {
		return Database.getDataPatternName();
	}

	@GET
	@Path("/name/{name}")
	@Produces("application/json")
	public ArrayList <ClassField> getDeviceField(@PathParam("name") String name) {
		return Database.getDataPatternField(name);
	}

	@POST
	@Path("/name/{name}")
	@Consumes("application/json")
//	public Response creatDevice(@PathParam("name") String name, ClassFieldParam req) {
	public Response creatDevice(@PathParam("name") String name, ArrayList <ClassField> req) {
		System.out.println("obj class: " + req.getClass().getName()); 
		System.out.println("obj: " + req.toString()); 
		
		Database.insertDevice(name, req);
		
		return Response.status(200).build();
	}

}