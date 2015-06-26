package api;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.ws.rs.PathParam;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

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
		    field.setModifiers(Modifier.PRIVATE);  
		    
		    // ���getter��setter����  
		    String name = f.getParamName();
	        char[] cs=name.toCharArray();
	        cs[0]-=32;
	        name = String.valueOf(cs);
	        System.out.println("class name upper= " + name);
		    
		    ctClass.addMethod(CtNewMethod.setter("set" + name, field));  
		    ctClass.addMethod(CtNewMethod.getter("get" + name, field));  
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
		    // ����һ����  
	    	CtClass ctClass = classPool.makeClass("api." + name);  
		    // Ϊ�������ýӿ�  
		    //ctClass.setInterfaces(new CtClass[] {classPool.get(Runnable.class.getName())});  	    	
	    	
		    // Ϊ���������ֶ�  , getter and setter method
	    	for (ClassField f : fieldList) {
	    		setField(f, classPool, ctClass);
	    	}
	    	
	    	CtConstructor constructor = new CtConstructor(null, ctClass);  
		    constructor.setModifiers(Modifier.PUBLIC);  
		    constructor.setBody("{}");  
		    ctClass.addConstructor(constructor);  
		  
		    // ����������  
	    	System.out.println("field size = " + fieldList.size());
	    	CtClass[] constructorParams = new CtClass[fieldList.size()];
	    	int i = 0;
	    	for (ClassField f : fieldList) {
	    		String s;
				switch (f.getParamType()) {
				case ClassField.DATA_TYPE_STRING:
	    			s = String.class.getName();
	    			break;
				case ClassField.DATA_TYPE_NUMBER:
					s = Integer.class.getName();
	    			break;
				case ClassField.DATA_TYPE_BOOLEAN:
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
	    	
	    	constructor = new CtConstructor(constructorParams, ctClass);  
		    constructor.setModifiers(Modifier.PUBLIC);  
		    String cmd = "{";
		    for (ClassField f : fieldList) {
		    	cmd += "this." + f.getParamName();
		    	cmd += "=$"+ (fieldList.indexOf(f) + 1) + ";";
		    }
		    cmd += "}";
		    System.out.println("setter body = [" + cmd + "]");
		    constructor.setBody(cmd);  
		    ctClass.addConstructor(constructor);  
		  
//		    CtMethod method = new CtMethod(CtClass.voidType, "run", null, ctClass);  
//		    method.setModifiers(Modifier.PUBLIC);  
//		    method.setBody("{System.out.println(\"ִ�н��\" + this.value);}");  
//		    ctClass.addMethod(method);  
		  
		    Class<?> clazz = ctClass.toClass();  
		    classList.put(name, clazz);
		    
		    System.out.println("---Created:-------------------------------");
		    System.out.println(clazz.toString() );
		    System.out.println(clazz.getConstructors().toString() );
		    System.out.println(clazz.getDeclaredFields().toString());
		    System.out.println(clazz.getDeclaredMethods().toString() );
		    System.out.println(clazz.getFields().toString());
		    System.out.println("------------------------------------------");
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
					switch(f.getParamType()) {
					case Constant.DATA_TYPE_STRING:
						String stringValue = rs.getString(f.getParamName());
						System.out.println("name:" + f.getParamName() + ",value:" + stringValue);
						item.put(f.getParamName(), stringValue);
						break;
					case Constant.DATA_TYPE_NUMBER:
						int intValue = rs.getInt(f.getParamName());
						System.out.println("name:" + f.getParamName() + ",value:" + intValue);
						item.put(f.getParamName(), intValue);
						break;
					case Constant.DATA_TYPE_BOOLEAN:
						boolean boolValue = rs.getBoolean(f.getParamName());
						System.out.println("name:" + f.getParamName() + ",value:" + boolValue);
						item.put(f.getParamName(), boolValue);
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
	
	public static boolean insertDevice(String name, ArrayList <ClassField> deviceField) {			
		if (classList.containsKey(name)) {
			return false;
		}
		
		if (conn == null) connect();
		
		for (ClassField f : deviceField) {
			String ste = "insert into tbl_device_pattern values ( \"" + name
					+ "\", \"" + f.paramName + "\", \"" + f.paramType + "\");";

			try {
				Statement stmt = conn.createStatement();
				if (stmt.execute(ste)) {
					System.out.println("succeed");
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	    	System.out.println("ste=[" + ste + "]");
		}

		String ste2 = "create table tbl_data_" + name +" (";
		
		boolean needComma = false;
		for (ClassField f : deviceField) {
			if (needComma) {
				ste2 += ",";
			}
			else {
				needComma = true;
			}
			
			ste2 += f.paramName;
			
			switch (f.paramType) {
			case ClassField.DATA_TYPE_STRING :
				ste2 += " varchar(255)";
				break;
			case ClassField.DATA_TYPE_NUMBER :
				ste2 += " int";
				break;
			case ClassField.DATA_TYPE_BOOLEAN :
				ste2 += " int";
				break;
			}
		}
		
		ste2 += ");";
    	System.out.println("ste=[" + ste2 + "]");

		try {
			Statement stmt = conn.createStatement();
			if (stmt.execute(ste2)) {
				System.out.println("succeed");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		    	
		return true;
	}
	
	
	public static boolean insertData(String name, Object req) {	
		
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
        		case Constant.DATA_TYPE_STRING:
        			valueStr += "\"" + (String) obj + "\"";
        			break;
        		case Constant.DATA_TYPE_NUMBER:
        			valueStr += (int) obj;
        			break;
        		case Constant.DATA_TYPE_BOOLEAN:
        			valueStr += ((boolean) obj)? "1": "0";
        			break;
        		default:
        			System.out.println("not found");
        		    break;
        		}    			
    		}    		
    	}
    	ste += keyStr + ") values (" + valueStr + ");";
    	System.out.println("ste=["+ ste +"]");        
    	
		if (conn == null) connect();
		try {
			Statement stmt = conn.createStatement();
			if (stmt.execute(ste)) {
				System.out.println("succeed");
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return true;
	}

	
	public static boolean insertData2(String name, Object req) {	
		
		if (classList.containsKey(name)) {
			Class<?> myclass = classList.get(name);
			System.out.println("---myclass:" + myclass.getName());
			System.out.println("req:" + req.getClass().getName());
			if (myclass.isInstance(req)) {
				System.out.println("----match!!!");
			}

			ObjectMapper objectMapper = new ObjectMapper ();
			JsonGenerator jsonGenerator = null;
	        try {

	        	ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	        	
	        	jsonGenerator = objectMapper.getJsonFactory().createJsonGenerator(baos, JsonEncoding.UTF8);
	        	jsonGenerator.writeObject(req);

	        	String jsonstr = baos.toString();   
	        	System.out.println("--jsonstr=" + jsonstr);
				Object newobj = (Object) objectMapper.readValue(jsonstr, myclass);
				System.out.println("aaa---:" + newobj.getClass().toString());
			} catch (JsonGenerationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JsonMappingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
//		HashMap <String, Object> map = (HashMap <String, Object>) req;
//		ArrayList <ClassField> fieldList = getDataPatternField(name);
//    	String ste = "insert into tbl_data_" + name + " (";
//    	String keyStr = "";
//    	String valueStr = "";
//    	boolean firstDone = false;
//    	for (ClassField f : fieldList) {
//    		if (firstDone) {
//    			keyStr += ",";
//    			valueStr += ",";
//    		}
//    		else {
//    			firstDone = true;
//    		}
//    		if (map.containsKey(f.getParamName())) {
//    			keyStr += f.getParamName();
//    			Object obj = map.get(f.getParamName());
//    			System.out.println("name is ["+ f.getParamName() + "], obj is ["+ obj.getClass().getName() +"]");
//        		switch(f.getParamType()) {
//        		case Constant.DATA_TYPE_STRING:
//        			valueStr += "\"" + (String) obj + "\"";
//        			break;
//        		case Constant.DATA_TYPE_NUMBER:
//        			valueStr += (int) obj;
//        			break;
//        		case Constant.DATA_TYPE_BOOLEAN:
//        			valueStr += ((boolean) obj)? "1": "0";
//        			break;
//        		default:
//        			System.out.println("not found");
//        		    break;
//        		}    			
//    		}    		
//    	}
//    	ste += keyStr + ") values (" + valueStr + ");";
//    	System.out.println("ste=["+ ste +"]");        
//    	
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
