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

public class Shop_now_wish_list_delete implements RequestHandler<JSONObject, JSONObject> {

	private String USERNAME;
	private String PASSWORD;
	private String DB_URL;
			
	
	@SuppressWarnings("unchecked")
	public JSONObject handleRequest(JSONObject input, Context context) {
   
		
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
				Connection con = null;
				Statement stmt = null;;
		
		
		ResultSet resultSet;
		int i = 0;
		String str_Msg = null;
		JSONObject jsonObject_wishListDelete_Result = new JSONObject();
        String customer_id= input.get("customer_id").toString();
	    String product_id=input.get("product_id").toString();
	   
		if (customer_id == null || product_id == null ||customer_id == ""
				|| product_id == "") {

			str_Msg = "enter valid customerId and productId";
			jsonObject_wishListDelete_Result.put("status", "0");
			jsonObject_wishListDelete_Result.put("message", str_Msg);

			return jsonObject_wishListDelete_Result;

		} else {
			final int customerId = Integer.parseInt(input.get("customer_id").toString());
			final int productId = Integer.parseInt(input.get("product_id").toString());
			final String sql = "delete from wish_list where customer_id=" + customerId + " and product_id=" + productId
					+ "";

			String sql1 = ("SELECT id FROM wish_list where customer_id='" + customerId + "' and product_id='"
					+ productId + "'");
			try {

				con = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
				stmt = con.createStatement();
				resultSet = stmt.executeQuery(sql1);

				if (resultSet.next()) {

					try {

						i = stmt.executeUpdate(sql);
					} catch (SQLException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

					str_Msg = " number of rows =" + i + " delete product in wish_list successfully";
					jsonObject_wishListDelete_Result.put("status", "1");
					jsonObject_wishListDelete_Result.put("message", str_Msg);
				} else {

					str_Msg = "enter valid customerId and productId";
					jsonObject_wishListDelete_Result.put("status", "0");
					jsonObject_wishListDelete_Result.put("message", str_Msg);

				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				JSONObject jo_catch = new JSONObject();
				jo_catch.put("Exception",e.getMessage());
				return jo_catch;
			}

			return jsonObject_wishListDelete_Result;
		}
	}
}