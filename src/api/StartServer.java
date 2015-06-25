package api;

//import org.jboss.resteasy.test.TestPortProvider;

public class StartServer {
	public static void main(String[] args) {
		 //int port = TestPortProvider.getPort();
		int port = 8083; 
		System.out.println("port = " + port);
		try {
			Database.initClass();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      MyNettyJaxrsServer netty = new MyNettyJaxrsServer(port, "");
	      netty.start();
	 }	
	
	
	
}



