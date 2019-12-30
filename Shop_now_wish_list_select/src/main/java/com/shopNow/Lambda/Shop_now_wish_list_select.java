package com.shopNow.Lambda;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Lambda function that simply prints "Hello World" if the input String is not
 * provided, otherwise, print "Hello " with the provided input String.
 * 
 * @param <JSONObject>
 */

public class Shop_now_wish_list_select implements RequestHandler<JSONObject, JSONObject> {
	private String USERNAME;
	private String PASSWORD;
	private String DB_URL;
		


	
	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject j, Context context) {

		Statement stmt = null,stmt_customer=null;
		Connection con = null;
		Properties prop = new Properties();

		JSONObject errorPayload = new JSONObject();
	
		if(!j.containsKey("customer_id")){			
			errorPayload.put("errorType", "BadRequest");
			errorPayload.put("httpStatus", 400);
			errorPayload.put("requestId", context.getAwsRequestId());
			errorPayload.put("message", "JSON Input Object request key named 'customer_id' is missing");
			throw new RuntimeException(errorPayload.toJSONString());	
		}
		
		
		
		
		try {
			prop.load(getClass().getResourceAsStream("/application.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
				DB_URL = prop.getProperty("url");
				USERNAME = prop.getProperty("username");
				PASSWORD = prop.getProperty("password");
				
		
		ResultSet resultSet,resultSet_customer;
		int customer_id = 0;
		LambdaLogger logger = context.getLogger();

		if (j.get("customer_id") != null && j.get("customer_id") != "") {
			customer_id = Integer.parseInt(j.get("customer_id").toString());
		} else {
			customer_id = 0;
		}
		String strMsg;
		JSONObject jsonObject_wishList_result = new JSONObject();
		JSONArray json_array_wishlist = new JSONArray();

		try {
			con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			
			Statement stmt_customer1 = con.createStatement();
			ResultSet srs_customer = stmt_customer1.executeQuery("SELECT id, status FROM customers where id='" + customer_id + "'");
		
			if (srs_customer.next() == false ) {

				strMsg = "No User Found !";
				jsonObject_wishList_result.put("status", "0");
				jsonObject_wishList_result.put("message", strMsg);
				return jsonObject_wishList_result;
			}
			else if(!srs_customer.getString("status").equalsIgnoreCase("1")) {
				
				strMsg = "User not confirmed !";
				jsonObject_wishList_result.put("status", "0");
				jsonObject_wishList_result.put("message", strMsg);

				return jsonObject_wishList_result;
				
				
			}
			
			srs_customer.close();
			stmt_customer1.close();
					
			
			stmt_customer = con.createStatement();
			resultSet_customer = stmt_customer.executeQuery("SELECT customer_id,product_id FROM wish_list where customer_id='"+ customer_id + "'");
				
				if (resultSet_customer.next()==false) {

					strMsg = "wish_list for customerId Not present in wishlist";
					jsonObject_wishList_result.put("status", "0");
					jsonObject_wishList_result.put("message", strMsg);
					return jsonObject_wishList_result;
				}
			
				resultSet_customer.close();
				stmt_customer.close();
			
			
			
			stmt = con.createStatement();
			resultSet = stmt.executeQuery(
					"SELECT pa.id,pa.name,pa.regular_price,pa.sale_price,pa.image,wish_list.customer_id FROM products AS pa INNER JOIN wish_list ON wish_list.product_id=pa.id WHERE wish_list.customer_id="+ customer_id);
			
				while (resultSet.next()) {
					JSONObject jsonObject_wishList = new JSONObject();

					jsonObject_wishList.put("product_id", resultSet.getString("id"));
					jsonObject_wishList.put("product_name", resultSet.getString("name"));
					jsonObject_wishList.put("regular_price", resultSet.getFloat("regular_price"));
					jsonObject_wishList.put("sale_price", resultSet.getFloat("sale_price"));
					// jo.put("customer_id",rs.getInt("customer_id"));
					jsonObject_wishList.put("image", resultSet.getString("image"));
					jsonObject_wishList.put("Currency","INR");
					json_array_wishlist.add(jsonObject_wishList);
				}

				jsonObject_wishList_result.put("wishlist", json_array_wishlist);

		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			JSONObject jo_catch = new JSONObject();
			jo_catch.put("Exception",e.getMessage());
			return jo_catch;
		}

		return jsonObject_wishList_result;
	}
}