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

import org.json.simple.JSONObject;

/**
 * Lambda function that simply prints "Hello World" if the input String is not
 * provided, otherwise, print "Hello " with the provided input String.
 * 
 * @param <JSONObject>
 */

public class Shop_now_wish_list_insert implements RequestHandler<JSONObject, JSONObject> {

	
	private String USERNAME;
	private String PASSWORD;
	private String DB_URL;
		


	
	
	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {

		Statement stmt = null;
		Connection con = null;
		
		JSONObject errorPayload = new JSONObject();
	
		if(!input.containsKey("customer_id")){			
			errorPayload.put("errorType", "BadRequest");
			errorPayload.put("httpStatus", 400);
			errorPayload.put("requestId", context.getAwsRequestId());
			errorPayload.put("message", "JSON Input Object request key named 'customer_id' is missing");
			throw new RuntimeException(errorPayload.toJSONString());	
		}
		if(!input.containsKey("product_id")){			
			errorPayload.put("errorType", "BadRequest");
			errorPayload.put("httpStatus", 400);
			errorPayload.put("requestId", context.getAwsRequestId());
			errorPayload.put("message", "JSON Input Object request key named 'product_id' is missing");
			throw new RuntimeException(errorPayload.toJSONString());	
		}
		
		
		
		Properties prop = new Properties();

		try {
			prop.load(getClass().getResourceAsStream("/application.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
				
				DB_URL = prop.getProperty("url");
				USERNAME = prop.getProperty("username");
				PASSWORD = prop.getProperty("password");
				
		
		ResultSet resultSet;
		String strMsg;

		JSONObject jsonObject_InsertWishList_Result = new JSONObject();
		String customerId = null;
		String product_id = null;
		String Str_msg=null;

		LambdaLogger logger = context.getLogger();
		logger.log("Invoked JDBCSample.getCurrentTime");

		if (input.get("customer_id") == null || input.get("product_id") == null || input.get("customer_id") == ""
				|| input.get("product_id") == "") {
			jsonObject_InsertWishList_Result.put("status", "0");
			jsonObject_InsertWishList_Result.put("message", "input invalid");
			return jsonObject_InsertWishList_Result;

		} else {
			customerId = input.get("customer_id").toString();
			product_id = input.get("product_id").toString();

			try {
				con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
				
				
				Statement stmt_customer = con.createStatement();
				ResultSet srs_customer = stmt_customer.executeQuery("SELECT id,status FROM customers where id='" + customerId + "'");
			
				if (srs_customer.next() == false ) {

					Str_msg = "NO User Found !";
					jsonObject_InsertWishList_Result.put("status", "0");
					jsonObject_InsertWishList_Result.put("message", Str_msg);
					return jsonObject_InsertWishList_Result;
				}
				else if(!srs_customer.getString("status").equalsIgnoreCase("1")) {
					
					Str_msg = "User not confirmed !";
					jsonObject_InsertWishList_Result.put("status", "0");
					jsonObject_InsertWishList_Result.put("message", Str_msg);

					return jsonObject_InsertWishList_Result;
					
					
				}
				srs_customer.close();
				stmt_customer.close();
				

				
				Statement stmt1_productid = con.createStatement();
				ResultSet resultSet_productId = stmt1_productid.executeQuery(
						"SELECT * FROM products where id='" + product_id + "'");

				if (resultSet_productId.next() == false) {

					Str_msg = "product id is not valid";
					jsonObject_InsertWishList_Result.put("status", "0");
					jsonObject_InsertWishList_Result.put("message", Str_msg);

					return jsonObject_InsertWishList_Result;

				}
				
				
				
				
			stmt = con.createStatement();
			resultSet = stmt.executeQuery("SELECT customer_id,product_id FROM wish_list where customer_id='"
							+ customerId + "' and product_id='" + product_id + "'");
				
				if (resultSet.next()) {

					strMsg = "customerId and productId Already present in wishlist";
					jsonObject_InsertWishList_Result.put("status", "0");
					jsonObject_InsertWishList_Result.put("message", strMsg);

				} else {

					stmt.executeUpdate("INSERT INTO wish_list (customer_id,product_id)VALUES('" + customerId + "','"+ product_id + "')");

					strMsg = "insert Record sucessfully";
					jsonObject_InsertWishList_Result.put("status", "1");
					jsonObject_InsertWishList_Result.put("message", strMsg);

				}

			}

			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JSONObject jo_catch = new JSONObject();
				jo_catch.put("Exception",e.getMessage());
				return jo_catch;

			}

		}
		return jsonObject_InsertWishList_Result;
	}
}