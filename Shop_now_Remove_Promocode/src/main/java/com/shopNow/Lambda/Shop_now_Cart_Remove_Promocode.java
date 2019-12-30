package com.shopNow.Lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Shop_now_Cart_Remove_Promocode implements RequestHandler<JSONObject, JSONObject> {

	private String USERNAME;
	private String PASSWORD;
	private String DB_URL;
	
	
	@SuppressWarnings({ "unchecked", "unused" })
	public JSONObject handleRequest(JSONObject input, Context context) {

		LambdaLogger logger = context.getLogger();
		logger.log("Invoked JDBCSample.getCurrentTime");

		JSONObject errorPayload = new JSONObject();
	
		if(!input.containsKey("userid")){			
			errorPayload.put("errorType", "BadRequest");
			errorPayload.put("httpStatus", 400);
			errorPayload.put("requestId", context.getAwsRequestId());
			errorPayload.put("message", "JSON Input Object request key named 'userid' is missing");
			throw new RuntimeException(errorPayload.toJSONString());	
		}
		if(!input.containsKey("promocode")){
			errorPayload.put("errorType", "BadRequest");
			errorPayload.put("httpStatus", 400);
			errorPayload.put("requestId", context.getAwsRequestId());
			errorPayload.put("message", "JSON Input Object request key named 'promocode' is missing");
			throw new RuntimeException(errorPayload.toJSONString());	
		}
		if(!input.containsKey("device_id")){
			errorPayload.put("errorType", "BadRequest");
			errorPayload.put("httpStatus", 400);
			errorPayload.put("requestId", context.getAwsRequestId());
			errorPayload.put("message", "JSON Input Object request key named 'device_id' is missing");
			throw new RuntimeException(errorPayload.toJSONString());	
		}	
		
		
		Object userid1 = input.get("userid");
		Object promocode1 = input.get("promocode");

		String promocode = promocode1.toString();
		Object device_id1 = input.get("device_id");
		String device_id = device_id1.toString();

		long userid;
		int promocode_id;
		long cart_id;

		String Str_msg;
		JSONArray promocodes_array = new JSONArray();
		JSONObject jo_cartInsert = new JSONObject();

		if (userid1 == null || userid1 == "") {
			userid = 0;
		} else {

			userid = Long.parseLong(userid1.toString());
		}

		// Get time from DB server

		if (promocode == null || promocode == "") {

			Str_msg = "No Promocode Entered";
			jo_cartInsert.put("status", "0");
			jo_cartInsert.put("message", Str_msg);

			return jo_cartInsert;
		}

		if ((device_id1 == null || device_id1 == "") && userid == 0) {

			Str_msg = "Please Enter either UserId or Device_id";
			jo_cartInsert.put("status", "0");
			jo_cartInsert.put("message", Str_msg);
			return jo_cartInsert;
		}

		
		
		Properties prop = new Properties();

		try {
			prop.load(getClass().getResourceAsStream("/application.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
				
		try {
			
			DB_URL = prop.getProperty("url");
			USERNAME = prop.getProperty("username");
			PASSWORD = prop.getProperty("password");
	
			
			Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
		
			String promo_sql1 = "SELECT * FROM promocodes where promocode='" + promocode + "'";

			Statement stmt1 = conn.createStatement();
			ResultSet resultSet1 = stmt1.executeQuery(promo_sql1);

			if (resultSet1.next()) {
				promocode_id = resultSet1.getInt("id");
				if (userid == 0) {
					String delete_promo_sql = "DELETE FROM cart_promocode where device_id='"
							+ device_id + "'AND user_id=0 AND promocode_id='"+promocode_id+"'";
					int i = stmt1.executeUpdate(delete_promo_sql);
					if (i > 0) {
						String promo_sql = "SELECT * FROM cart_promocode where device_id='" + device_id
								+ "'AND user_id=0";

						ResultSet resultSet2 = stmt1.executeQuery(promo_sql);
						while (resultSet2.next()) {
							JSONObject promocodes = new JSONObject();
							
							promocodes.put("promocode_id", resultSet2.getInt("promocode_id"));
							promocodes_array.add(promocodes);

						}
						Str_msg = "Promocode Removed Successfully";
						jo_cartInsert.put("status", "1");
						jo_cartInsert.put("message", Str_msg);
						jo_cartInsert.put("Promocodes", promocodes_array);
					} else {
						String promo_sql = "SELECT * FROM cart_promocode where device_id='" + device_id
								+ "'AND user_id=0";

						ResultSet resultSet2 = stmt1.executeQuery(promo_sql);
						while (resultSet2.next()) {
							JSONObject promocodes = new JSONObject();
							
							promocodes.put("promocode_id", resultSet2.getInt("promocode_id"));
							promocodes_array.add(promocodes);

						}
						Str_msg = "Promocode Not Removed Successfully";
						jo_cartInsert.put("status", "1");
						jo_cartInsert.put("message", Str_msg);
						jo_cartInsert.put("Promocodes", promocodes_array);
					}
				}
				else {
					String delete_promo_sql = "DELETE FROM cart_promocode where user_id='" + userid + "'AND promocode_id='"+promocode_id+"'";
					int i = stmt1.executeUpdate(delete_promo_sql);
					if (i > 0) {
						String promo_sql = "SELECT id, promocode, description FROM promocodes where id IN(SELECT promocode_id FROM cart_promocode where user_id='" + userid + "')";

						ResultSet resultSet2 = stmt1.executeQuery(promo_sql);
						while (resultSet2.next()) {
							JSONObject promocodes = new JSONObject();
							
							promocodes.put("promocode_id", resultSet2.getInt("id"));
							promocodes.put("promoname", resultSet2.getString("description"));
							//promocodes.put("promocode", resultSet2.getString("promocode"));
							
							promocodes_array.add(promocodes);

						}
						Str_msg = "Promocode Removed Successfully";
						jo_cartInsert.put("status", "1");
						jo_cartInsert.put("message", Str_msg);
						jo_cartInsert.put("Promocodes", promocodes_array);
					} else {
						String promo_sql = "SELECT id, promocode, description FROM promocodes where id IN(SELECT promocode_id FROM cart_promocode where user_id='" + userid + "')";

						ResultSet resultSet2 = stmt1.executeQuery(promo_sql);
						while (resultSet2.next()) {
							JSONObject promocodes = new JSONObject();
							
							promocodes.put("promocode_id", resultSet2.getInt("id"));
							promocodes.put("promoname", resultSet2.getString("description"));
							//promocodes.put("promocode", resultSet2.getString("promocode"));
							
							promocodes_array.add(promocodes);

						}
						Str_msg = "Promocode Not Removed Successfully";
						jo_cartInsert.put("status", "1");
						jo_cartInsert.put("message", Str_msg);
						jo_cartInsert.put("Promocodes", promocodes_array);
					}

				}
			} else {
				Str_msg = "Invalid Promocode entered";
				jo_cartInsert.put("status", "0");
				jo_cartInsert.put("message", Str_msg);
			}

		} catch (Exception e) {
			logger.log("Exception " + e);
			jo_cartInsert.put("status", "0");
			jo_cartInsert.put("message", e);
		}

		return jo_cartInsert;
	}
}
