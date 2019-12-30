
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

public class Shop_now_pincode implements RequestHandler<JSONObject, JSONObject> {

	private String USERNAME;
	private String PASSWORD;
	private String DB_URL;


	@SuppressWarnings({ "unchecked" })
	public JSONObject handleRequest(JSONObject input, Context context) {
		LambdaLogger logger = context.getLogger();
		logger.log("Invoked JDBCSample.getCurrentTime");

		JSONArray pincode_array = new JSONArray();
		JSONObject jsonObject_category_result = new JSONObject();
		String Str_msg = null;

		Properties prop = new Properties();
		try {
			prop.load(getClass().getResourceAsStream("/application.properties"));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Get time from DB server
		try {
			DB_URL = prop.getProperty("url");
			USERNAME = prop.getProperty("username");
			PASSWORD = prop.getProperty("password");
		
			Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
			
			
			
			Statement stmt = conn.createStatement();
			Statement stmt1 = conn.createStatement();
			Statement stmt2 = conn.createStatement();

	
			
			String pincode1 = input.get("pincode").toString();	
			if (input.get("pincode")==null ||input.get("pincode")=="") {

				Str_msg = "Pincode cannot be empty !! ";
				jsonObject_category_result.put("status", "0");
				jsonObject_category_result.put("message", Str_msg);
				return jsonObject_category_result;

			}
			int pincode = Integer.parseInt(pincode1);
		
			
			
			String product_id1 = input.get("product_id").toString();
			if (input.get("product_id")==null || input.get("product_id")=="") {

				Str_msg = "Product_id cannot be empty !! ";
				jsonObject_category_result.put("status", "0");
				jsonObject_category_result.put("message", Str_msg);
				return jsonObject_category_result;

			}

			int product_id = Integer.parseInt(product_id1);	
			int vendor_id = 0;

			ResultSet resultSet_product = stmt.executeQuery("select * from products where id=" + product_id+ " AND stock = 'true'");
			if (resultSet_product.next() == false) {

				Str_msg = "Either product is not present or product is out of stock !! ";
				jsonObject_category_result.put("status", "0");
				jsonObject_category_result.put("message", Str_msg);
				return jsonObject_category_result;

			}
			vendor_id = Integer.parseInt(resultSet_product.getString("vendor_id"));
			resultSet_product.close();
			
			
			
			
			
			
			
			
			
			String courier_name = input.get("courier_name").toString();	
			if (courier_name.equalsIgnoreCase("null")) {

				Str_msg = "courier_name cannot be null !! ";
				jsonObject_category_result.put("status", "0");
				jsonObject_category_result.put("message", Str_msg);
				return jsonObject_category_result;

			}
			
			ResultSet resultSet_couriers = stmt.executeQuery("SELECT * FROM couriers WHERE courier_name='" + courier_name+"' AND couriers.status='1'");
			if (resultSet_couriers.next() == false) {

				Str_msg = "Either courier_name is not present or courier service is not available !! ";
				jsonObject_category_result.put("status", "0");
				jsonObject_category_result.put("message", Str_msg);
				return jsonObject_category_result;

			}
			int couriers_id=resultSet_couriers.getInt("id");
			resultSet_couriers.close();
			

			
			
			
			
			
			String category_name = input.get("category_name").toString();
			if (category_name.equalsIgnoreCase("null") || input.get("category_name") =="") {
				category_name = "Standard";
			}
			ResultSet resultSet_category_name = stmt.executeQuery("SELECT * FROM shipping_categories WHERE category_name='" + category_name+"' AND courier_id="+couriers_id);
			if (resultSet_category_name.next() == false) {

				Str_msg = "Either category_name is not present or this courier does not provide this service !! ";
				jsonObject_category_result.put("status", "0");
				jsonObject_category_result.put("message", Str_msg);
				return jsonObject_category_result;

			}
			resultSet_category_name.close();
			
			
			

			ResultSet resultSet_pin = stmt.executeQuery("SELECT * FROM pincodes WHERE pincode=" + pincode);
			if (resultSet_pin.next() == false) {

				Str_msg = "Either pincode is not valid or products are not available for this pincode !! ";
				jsonObject_category_result.put("status", "0");
				jsonObject_category_result.put("message", Str_msg);
				return jsonObject_category_result;

			}
			
			resultSet_pin.close();

			String city_seller = null;
			String zone_seller = null;
			String state_seller = null;
			String country_seller = null;

			int is_metro_seller = 0, is_special_destination_seller = 0, is_RoI_A_seller = 0, is_RoI_B_seller = 0;

			
			String pincode_Sql_seller = "SELECT * FROM (SELECT * FROM vendor_address WHERE vendor_id =" + vendor_id
					+ " )AS table1 INNER JOIN pincodes ON pincodes.pincode=table1.pincode";

			logger.log("\n pincode_Sql_seller \n" + pincode_Sql_seller);

			ResultSet resultSet_seller = stmt.executeQuery(pincode_Sql_seller);
			JSONObject jsonObject_pincode_seller = new JSONObject();
			while (resultSet_seller.next()) {

				jsonObject_pincode_seller.put("id", resultSet_seller.getInt("id"));

				// vendor_id=resultSet_seller.getInt("vendor_id");

				city_seller = resultSet_seller.getString("city");
				zone_seller = resultSet_seller.getString("zone");
				
				
				state_seller = resultSet_seller.getString("state");
				country_seller = resultSet_seller.getString("country");
				is_metro_seller = resultSet_seller.getInt("is_metro");

				
				is_special_destination_seller = resultSet_seller.getInt("is_special_destination");
				is_RoI_A_seller = resultSet_seller.getInt("is_RoI_A");
				is_RoI_B_seller = resultSet_seller.getInt("is_RoI_A");

				jsonObject_pincode_seller.put("city", city_seller);
				jsonObject_pincode_seller.put("state", state_seller);
				jsonObject_pincode_seller.put("country", country_seller);
				jsonObject_pincode_seller.put("Zone", zone_seller);
				jsonObject_pincode_seller.put("is_metro", is_metro_seller);

			}

			resultSet_seller.close();

			String city = null;
			String state = null;
			String country = null;
			int is_metro = 0, is_special_destination = 0, is_RoI_A = 0, is_RoI_B = 0;
			String zone = null;

			String pincode_Sql = "SELECT * FROM pincodes where pincode =" + pincode;

			logger.log("\n pincode_Sql \n" + pincode_Sql);

			ResultSet resultSet = stmt1.executeQuery(pincode_Sql);
			JSONObject jsonObject_pincode = new JSONObject();
			while (resultSet.next()) {

				jsonObject_pincode.put("id", resultSet.getInt("id"));
				city = resultSet.getString("city");
				zone = resultSet.getString("zone");

				state = resultSet.getString("state");
				country = resultSet.getString("country");

				is_metro = resultSet.getInt("is_metro");
				is_special_destination = resultSet.getInt("is_special_destination");
				is_RoI_A = resultSet.getInt("is_RoI_A");
				is_RoI_B = resultSet.getInt("is_RoI_B");

				jsonObject_pincode.put("city", city);
				jsonObject_pincode.put("state", state);
				jsonObject_pincode.put("country", country);
				jsonObject_pincode.put("Zone", zone);
				jsonObject_pincode.put("is_metro", is_metro);
				jsonObject_pincode.put("is_RoI_B", is_RoI_B);
				jsonObject_pincode.put("is_RoI_A", is_RoI_A);
				jsonObject_pincode.put("is_special_destination", is_special_destination);

			}

			resultSet.close();
			String city_type = null;

			if (city.equalsIgnoreCase(city_seller)) {

				city_type = "intra_city";

			} else if (zone.equalsIgnoreCase(zone_seller)) {
				city_type = "intra_zone";

				/*
				 * if ((is_metro & is_metro_seller) == 1) {
				 * 
				 * city_type = "metro_to_metro";
				 * 
				 * 
				 * } else if (is_RoI_A == 1) {
				 * 
				 * city_type = "roi_A";
				 * 
				 * } else if (is_RoI_B == 1) {
				 * 
				 * city_type = "roi_B";
				 * 
				 * 
				 * } else if (is_special_destination == 1) {
				 * 
				 * city_type = "special_destination";
				 * 
				 * } else { city_type = "intra_zone";
				 * 
				 * }
				 */

			} else {

				logger.log("\n we are in else \n" + city_type);

				if ((is_metro & is_metro_seller) == 1) {

					city_type = "metro_to_metro";

				} else if (is_RoI_A == 1) {

					city_type = "roi_A";

				} else if (is_RoI_B == 1) {

					city_type = "roi_B";

				} else if (is_special_destination == 1) {

					city_type = "special_destination";

				} else {
					city_type = "null";

				}

			}

			logger.log("\n city_type \n" + city_type);

			String sql_shipping = "SELECT shipping_categories.*,couriers.courier_name FROM couriers  INNER JOIN shipping_categories ON shipping_categories.courier_id=couriers.id WHERE courier_name='"+courier_name+"' AND category_name='"
					+ category_name + "' AND weight='0-500 gms' AND vendor_id='"+vendor_id+"'";

			logger.log("\n sql_shipping \n" + sql_shipping);

			ResultSet sql_shipping1 = stmt2.executeQuery(sql_shipping);
			float shipping_charge = 0;

			if (sql_shipping1.next()) {

				shipping_charge = sql_shipping1.getFloat(city_type);

			}
			else{
				
				Str_msg = "Sorry ! Currently this courier doesn't provide service to the said vendor !! ";
				jsonObject_category_result.put("status", "0");
				jsonObject_category_result.put("message", Str_msg);
				return jsonObject_category_result;		
				
			}
			sql_shipping1.close();

			logger.log("\n shipping_charge \n" + shipping_charge);

			jsonObject_pincode.put("shipping_charge", shipping_charge);
			pincode_array.add(jsonObject_pincode);
			jsonObject_category_result.put("pincode", pincode_array);

		} catch (Exception e) {
			e.printStackTrace();
			logger.log("Caught exception: " + e.getMessage());
		}

		if (jsonObject_category_result.isEmpty()) {

			Str_msg = "Request parameter is not valid !! ";
			jsonObject_category_result.put("status", "0");
			jsonObject_category_result.put("message", Str_msg);
			return jsonObject_category_result;
		}

		return jsonObject_category_result;

	}
}
