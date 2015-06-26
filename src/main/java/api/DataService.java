package api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.ws.rs.Consumes;  
import javax.ws.rs.GET;  
import javax.ws.rs.POST;  
import javax.ws.rs.Path;  
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;  
import javax.ws.rs.core.Response;  


@Path("/data")  
public class DataService {

	static int selected = 0;
	
	@GET
	@Path("/{name}/{id}")  
    @Produces("application/json")  
	public Response getSingleData(@PathParam("name") String name, @PathParam("id") int id) {  
    	ArrayList <HashMap <String, Object>> rsp; // = new HashMap <String, Object> ();
    	rsp = Database.getData(name);

		return Response.status(200).entity(rsp).build();   
    }

	@GET
	@Path("/{name}")  
    @Produces("application/json")  
	public Response getMultipleData(@PathParam("name") String name) {  
    	ArrayList <HashMap <String, Object>> rsp; // = new HashMap <String, Object> ();
    	rsp = Database.getData(name);

		return Response.status(200).entity(rsp).build();   
    }
   
	@POST
    @Path("/{name}")
    @Consumes("application/json")  
    public Response createData(@PathParam("name") String name, Object obj) {  	    
		System.out.println("obj class=["+ obj.getClass().getName() +"]");      
		System.out.println("obj=["+ obj.toString() +"]");      
    	if (Database.insertData2(name, obj)) {
    		return Response.status(200).build();  
    	}
    	else {
    		return Response.status(403).build();  
    	}
    }     
}  

