package com.cowin.finder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TimerTask;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Scheduler extends TimerTask {

	//all fields configured in configuration.json File
	private String email_sender = "";
	private String email_sender_password = "";
	private Long age = (long) 0;
	private String[] email_recipients = {};
	private Long[] pincodeArr = {};

	@Override
	public void run() {
		//call vaccine tracker method
		vaccineTracker();
	}


	/**
	 * This method reads details configured by user in configurtion.json
	 * 
	 */
	public void getConfigurationFileDetails() {
		//Read configuration.json File

		System.out.println("Reading Configured Details");
		JSONParser parser = new JSONParser();
		try {
			String folerPath= new File("..\\").getCanonicalPath();
			FileReader fr=new FileReader(folerPath+"\\configuration.json");    
			int i;    
			String confData= "";
			while((i=fr.read())!=-1)    
				confData+=(char)i;    
			fr.close();    

			Object obj = parser.parse(confData);
			JSONObject jsonObject = (JSONObject)obj;
			email_sender = (String)jsonObject.get("email_sender");
			email_sender_password = (String)jsonObject.get("email_sender_password");
			age = (Long)jsonObject.get("age");

			JSONArray recipientsJArr = (JSONArray)jsonObject.get("email_recipients");
			int j =0;
			email_recipients = new String[recipientsJArr.size()];
			for (Object recipientsJObj : recipientsJArr) {
				email_recipients[j] = (String) recipientsJObj;
				j++;
			}

			int k=0;
			JSONArray pincodeJArr = (JSONArray)jsonObject.get("pincode");
			pincodeArr = new Long[pincodeJArr.size()];
			for (Object pincodeObject : pincodeJArr) {
				pincodeArr[k]=(Long) pincodeObject;
				k++;
			}

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method reads details configured by user in configurtion.json
	 * 
	 */
	private void vaccineTracker() {
		Date dNow = new Date( );
		SimpleDateFormat ft = new SimpleDateFormat ("dd-MM-yyyy");
		String requestPincode="";
		String requestDate = ft.format(dNow);
		for (Long pincode : pincodeArr) {
			requestPincode =pincode.toString();
			checkAvailability(requestPincode, requestDate );
		}
	}

	/**
	 * This method used to check Availability of slots using belo param as input
	 * @param requestPincode : pincode number whose slot availability you want to check 
	 * @param requestDate : date on which slot availability you want to check - currently it is checking for present day. 
	 */
	private void checkAvailability(String requestPincode , String requestDate) {	
		URL urlForGetRequest;
		try {

			urlForGetRequest = new URL("https://cdn-api.co-vin.in/api/v2/appointment/sessions/public/calendarByPin?pincode="+requestPincode+"&date="+requestDate);
			String readLine = null;
			HttpURLConnection conection = (HttpURLConnection) urlForGetRequest.openConnection();
			conection.setRequestMethod("GET");
			conection.setRequestProperty("User-Agent", "Mozilla/5.0");
			conection.setRequestProperty("Accept-Language", "hi-IN"); // set userId its a sample here
			int responseCode = conection.getResponseCode();

			boolean isSlotAvailabilityChecked = false;
			if (responseCode == HttpURLConnection.HTTP_OK) {
				BufferedReader in = new BufferedReader(
						new InputStreamReader(conection.getInputStream()));
				StringBuffer response = new StringBuffer();
				while ((readLine = in .readLine()) != null) {
					response.append(readLine);
				} in .close();
				// print result

				JSONParser parser = new JSONParser(); 
				JSONObject json = (JSONObject) parser.parse(response.toString());
				JSONArray responseArr = (JSONArray) json.get("centers");

				for (Object object : responseArr) {
					JSONObject jsonObj = (JSONObject) object;

					Long pincode = (Long) jsonObj.get("pincode");
					String name = (String) jsonObj.get("name");
					String address = (String)jsonObj.get("address")+", "+(String)jsonObj.get("district_name") +
							", "+(String)jsonObj.get("state_name") + " "+pincode ;
					String fee_type = (String)jsonObj.get("fee_type");
					JSONArray sessionsArr = (JSONArray) jsonObj.get("sessions");
					for (Object sessionArrObj : sessionsArr) {
						JSONObject sesionObj = (JSONObject) sessionArrObj;
						Long slotAvailability = (Long) sesionObj.get("available_capacity");
						Long minAge = (Long) sesionObj.get("min_age_limit");
						String vaccine = (String) sesionObj.get("vaccine");

						if(slotAvailability >0 && minAge < age ) {
							System.out.println("Slot is available for pincode : "+pincode + " from Time :"+ new Date() );

							String emailSubject = "Vaccine Available for "+pincode;
							String emailMessage = "Dear User, \n\nSlot is available for pincode : "+pincode + " from Time : "+ new Date()+
									"\nCovid Center : "+name +"\nAddress : "+address+"\n Vaccine : "+vaccine+"\n Fee Type : "+fee_type+"\nTo book slot visit: https://www.cowin.gov.in/home";
							sendNotification(email_sender,
									email_sender_password,email_recipients,
									emailSubject,
									emailMessage);
							isSlotAvailabilityChecked = true;
						}else if(slotAvailability ==0 && minAge < age ){
							System.out.println("Sorry !!! slot is not available for pincode : "+pincode + " at Time :"+ new Date() );
							isSlotAvailabilityChecked = true;
						}
					}
				}

			} else {
				System.out.println("GET NOT WORKED" + responseCode);
			}


			if(isSlotAvailabilityChecked == false)
				System.out.println("Sorry !!! slot is not available for pincode : "+requestPincode + " at Time :"+ new Date() );
		} catch (IOException | ParseException e) {
			e.printStackTrace();
		}

	}
	
	
	/**
	 * This method is used for sending notification to user though mail using google mail id
	 * @param from : Sender email id
	 * @param password : password of senders email id 
	 * @param toList : array of senders email ids
	 * @param sub : subject of email
	 * @param msg : email content body
	 */
	private void sendNotification(String from,String password,String[] toList,String sub,String msg){  
		//Get properties object    
		Properties props = new Properties();    
		props.put("mail.smtp.host", "smtp.gmail.com");    
		props.put("mail.smtp.socketFactory.port", "465");    
		props.put("mail.smtp.socketFactory.class",    
				"javax.net.ssl.SSLSocketFactory");    
		props.put("mail.smtp.auth", "true");    
		props.put("mail.smtp.port", "465");    
		//get Session   
		Session session = Session.getDefaultInstance(props,    
				new javax.mail.Authenticator() {    
			protected PasswordAuthentication getPasswordAuthentication() {    
				return new PasswordAuthentication(from,password);  
			}    
		});    
		//compose message    
		try {    
			MimeMessage message = new MimeMessage(session);    
			for (String to : toList) {
				message.addRecipient(Message.RecipientType.TO,new InternetAddress(to)); 
			}

			message.setSubject(sub);    
			message.setText(msg);    
			//send message  
			Transport.send(message);    
			System.out.println("message sent successfully");    
		} catch (MessagingException e) {throw new RuntimeException(e);}    

	}

}
