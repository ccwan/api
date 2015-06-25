package api;

import java.util.ArrayList;
import java.util.HashMap;

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
	@Consumes("application/json")
	@Produces("application/json")
	public ArrayList <ClassField> getDeviceField(@PathParam("name") String name) {
		return Database.getDataPatternField(name);
	}

	@POST
	@Path("/name/{name}")
	@Consumes("application/json")
	@Produces("application/json")
	public Response creatDevice(ArrayList <ClassField> getDeviceField) {
		return Response.status(200).build();
	}

}