package api;

import java.util.ArrayList;

import org.jboss.resteasy.plugins.server.netty.NettyJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;

public class MyNettyJaxrsServer extends NettyJaxrsServer {
	public MyNettyJaxrsServer(int port, String path) {
		super();
		// TODO Auto-generated constructor stub
		
		ArrayList <String> myServiceName = new ArrayList <String> ();
		myServiceName.add(DeviceService.class.getName());
		myServiceName.add(DataService.class.getName());
		
		ResteasyDeployment deployment = new ResteasyDeployment();
		deployment.setResourceClasses(myServiceName);
		
		this.setDeployment(deployment);
		this.setPort(port);
		this.setRootResourcePath(path);
		this.setSecurityDomain(null);
	
	}
	
}
