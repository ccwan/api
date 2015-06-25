package api;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.ws.rs.PathParam;

import javassist.*;


public class Database {
	ArrayList<Integer> numList = new ArrayList<Integer>();
	ArrayList<String> stringList = new ArrayList<String>();
	ArrayList<Boolean> booleanList = new ArrayList<Boolean>();
	
	static HashMap <String, Class<?>> classList = new HashMap <String, Class<?>> ();

	final String fileName = "d:/tmp/sycloud1.db";
	static Connection conn = null;

	private static void connect() {
		try {
			Class.forName("org.sqlite.JDBC");
			conn = DriverManager.getConnection("jdbc:sqlite:sycloud1.db");
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			System.exit(0);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		return;
	}

	private static void disconnect() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		conn = null;
		return;
	}

	public static ArrayList <String> getDataPatternName() {
		ArrayList <String> nameList = new ArrayList <String> ();
		Statement stmt;
		ResultSet rs;
		if (conn == null) connect();
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM tbl_device_pattern group by name;");
			while (rs.next()) {
				String name = rs.getString("name");
				System.out.println("name: " + name);
				nameList.add(name);				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return nameList;
	}
	
	
	public static ArrayList <ClassField> getDataPatternField(String name) {
		ArrayList <ClassField> params = new ArrayList <ClassField> ();
		Statement stmt;
		ResultSet rs;
		if (conn == null) connect();
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery("SELECT * FROM tbl_device_pattern where name = '" + name + "';");
			while (rs.next()) {
				String paramName = rs.getString("paramName");
				String paramType = rs.getString("paramType");
				System.out.println("name: " + paramName + " type: " + paramType);
				params.add(new ClassField(paramName, paramType));				
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return params;
	}
	
	private static void setField(ClassField f, ClassPool classPool, CtClass ctClass) {		
	    CtField field = null;
		try {
			switch (f.getParamType()) {
			case "string":
    			field = new CtField(classPool.get(String.class.getName()), f.getParamName(), ctClass);
    			break;
			case "number":
				field = new CtField(classPool.get(Integer.class.getName()), f.getParamName(), ctClass);
    			break;
			case "boolean":
				field = new CtField(classPool.get(Boolean.class.getName()), f.getParamName(), ctClass);
    			break;
			default:
				break;			
			}
		    field.setModifiers(Modifier.PUBLIC);  
		    
		    // 添加getter和setter方法  
		    ctClass.addMethod(CtNewMethod.setter("set" + f.getParamName(), field));  
		    ctClass.addMethod(CtNewMethod.getter("get" + f.getParamName(), field));  
		    ctClass.addField(field);  
		} catch (CannotCompileException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  				
		return ;		
	}
	
	public static void initClass() throws Exception {  
		
		ArrayList <String> classNameList = Database.getDataPatternName();
		//ArrayList <DataPattern> classList = new ArrayList <DataPattern> ();
		
		ClassPool classPool = ClassPool.getDefault();  
		  
		for (String name : classNameList) {
			System.out.println("class name = " + name);
	    	ArrayList <ClassField> fieldList = Database.getDataPatternField(name);
		    // 创建一个类  
	    	CtClass ctClass = classPool.makeClass("api.rfid");  
		    // 为类型设置接口  
		    //ctClass.setInterfaces(new CtClass[] {classPool.get(Runnable.class.getName())});  
		  
		    // 为类型设置字段  , getter and setter method
	    	for (ClassField f : fieldList) {
	    		setField(f, classPool, ctClass);
	    	}
		    
		    // 参数构造器  
	    	System.out.println("field size = " + fieldList.size());
	    	CtClass[] constructorParams = new CtClass[fieldList.size()];
	    	int i = 0;
	    	for (ClassField f : fieldList) {
	    		String s;
				switch (f.getParamType()) {
				case "string":
	    			s = String.class.getName();
	    			break;
				case "number":
					s = Integer.class.getName();
	    			break;
				case "boolean":
					s = Boolean.class.getName();
	    			break;
				default:
					s = "";
					break;			
				}
				System.out.println("s = " + s);
				constructorParams[i] = classPool.get(s);
	    		System.out.println("---" + constructorParams[i].getName()
				         + "---" + constructorParams[i].getClass().toString() );
                i++;
	    	}
	    	
	    	
	    	CtConstructor constructor = new CtConstructor(constructorParams, ctClass);  
		    constructor.setModifiers(Modifier.PUBLIC);  
		    String cmd = "{";
		    for (ClassField f : fieldList) {
		    	cmd += "this." + f.getParamName();
		    	cmd += "=$"+ fieldList.indexOf(f) + ";";
		    }
		    cmd += "}";
		    System.out.println("setter body = [" + cmd + "]");
		    constructor.setBody(cmd);  
		    ctClass.addConstructor(constructor);  
		  
		    // 为类设置方法  
//		    CtMethod method = new CtMethod(CtClass.voidType, "run", null, ctClass);  
//		    method.setModifiers(Modifier.PUBLIC);  
//		    method.setBody("{System.out.println(\"执行结果\" + this.value);}");  
//		    ctClass.addMethod(method);  
		  
		    // 加载和执行生成的类  
		    Class<?> clazz = ctClass.toClass();  
		    classList.put(name, clazz);
		}
		
		Iterator<String> itr = classList.keySet().iterator();
		while (itr.hasNext()) {
			String className = itr.next();
			System.out.println("class name:" + className);
			System.out.println("class:" + classList.get(className).getClass().toString());
		}
		return;
	}  	
	
	
	public static ArrayList <HashMap <String, Object>> getData(String name) {
		ArrayList <HashMap <String, Object>> rsp = new ArrayList <HashMap <String, Object>> ();
		
		ArrayList <ClassField> fieldList = getDataPatternField(name);
    	String ste = "select * from tbl_data_" + name + ";";
    	ResultSet rs;

		if (conn == null) connect();
		try {
			Statement stmt = conn.createStatement();
			rs = stmt.executeQuery(ste);
			System.out.println("read from db:");
			while (rs.next()) {
				System.out.println("item:");
				HashMap <String, Object> item = new HashMap <String, Object> ();
				for (ClassField f : fieldList) {
					String stringValue;
					switch(f.getParamType()) {
					case "string":
						stringValue = rs.getString(f.getParamName());
						System.out.println("name:" + f.getParamName() + ",value:" + stringValue);
						item.put(f.getParamName(), stringValue);
						break;
					default:
						System.out.println("not found");
					    break;
					}
				}
				rsp.add(item);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		return rsp;
	}
	
	
	public static boolean insertData(@PathParam("name") String name, Object req) {	
		
		if (classList.containsKey(name)) {
			Class<?> myclass = classList.get(name);
			System.out.println("---myclass:" + myclass.getName());
			System.out.println("req:" + req.getClass().getName());
			if (myclass.isInstance(req)) {
				System.out.println("----match!!!");
			}
		}
		
		HashMap <String, Object> map = (HashMap <String, Object>) req;
		ArrayList <ClassField> fieldList = getDataPatternField(name);
    	String ste = "insert into tbl_data_" + name + " (";
    	String keyStr = "";
    	String valueStr = "";
    	boolean firstDone = false;
    	for (ClassField f : fieldList) {
    		if (firstDone) {
    			keyStr += ",";
    			valueStr += ",";
    		}
    		else {
    			firstDone = true;
    		}
    		if (map.containsKey(f.getParamName())) {
    			keyStr += f.getParamName();
    			Object obj = map.get(f.getParamName());
    			System.out.println("name is ["+ f.getParamName() + "], obj is ["+ obj.getClass().getName() +"]");
        		switch(f.getParamType()) {
        		case "string":
        			valueStr += "\"" + (String) obj + "\"";
        			break;
        		default:
        			System.out.println("not found");
        		    break;
        		}    			
    		}    		
    	}
    	ste += keyStr + ") values (" + valueStr + ");";
    	System.out.println("ste=["+ ste +"]");        
    	
//		if (conn == null) connect();
//		try {
//			Statement stmt = conn.createStatement();
//			if (stmt.execute(ste)) {
//				System.out.println("succeed");
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    	
    	return true;
	}

}
