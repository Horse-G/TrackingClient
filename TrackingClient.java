 
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
 
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
 
public class TrackingClient {
 
	private static BufferedReader writier;
	private final String USER_AGENT = "Mozilla/5.0";
	
	private static String fedExURL;
	private static String fedExKey;
	private static String fedExPassword;
	private static String fedExAccountNumber;
	private static String fedExMeterNumber;
	
	private static String upsURL; 
	private static String upsAccessLicenseNumber;
	private static String upsUserId;
	private static String upsPassword;

 
	public static void main(String[] args) throws Exception {
		
		
 
		TrackingClient http = new TrackingClient();
		http.init("C:\\Users\\redxma1\\developments\\POC Client\\config");
		String response;
		
		//==========================Testing 1==========================
		System.out.println("\nTesting 1 - Send Http POST request to UPS");
		String trackingNumber= "1Z04519F0372416325";
		String carrier = "UPS";
		response = http.sendPost(carrier,trackingNumber);
		
		parseResponse(carrier,response);
	
		//==========================Testing 2==========================
		System.out.println("\nTesting 2 - Send Http POST request to FedEx");
		String trackingNumber2= "641632368893";
		String carrier2 = "FedEx";
		response=http.sendPost(carrier2,trackingNumber2);
		
		parseResponse(carrier2,response);
	}
	
	public TrackingClient(){
	}
	public TrackingClient(String config){
		init(config);
	}
	public void track(String carrier,String trackingNumber){
		parseResponse(carrier,sendPost(carrier,trackingNumber));
	}
	
	private static void init(String config){
		try{
			Scanner reader = new Scanner(new File(config));
			String buffer;
			while(reader.hasNextLine()){
				buffer =reader.nextLine();
				fedExURL = buffer.substring(buffer.indexOf("=")+1);
				buffer =reader.nextLine();
				fedExKey = buffer.substring(buffer.indexOf("=")+1);
				buffer =reader.nextLine();
				fedExPassword = buffer.substring(buffer.indexOf("=")+1);
				buffer =reader.nextLine();
				fedExAccountNumber = buffer.substring(buffer.indexOf("=")+1);
				buffer =reader.nextLine();
				fedExMeterNumber = buffer.substring(buffer.indexOf("=")+1);
				buffer =reader.nextLine();
				upsURL = buffer.substring(buffer.indexOf("=")+1);
				buffer =reader.nextLine();
				upsAccessLicenseNumber = buffer.substring(buffer.indexOf("=")+1);
				buffer =reader.nextLine();
				upsUserId = buffer.substring(buffer.indexOf("=")+1);
				buffer =reader.nextLine();
				upsPassword = buffer.substring(buffer.indexOf("=")+1);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	private String sendPost(String carrier, String trackingNumber)  {
		StringBuffer response = new StringBuffer();
		
		try{
		String url = null;
		String urlParameters =null;
		
		if(carrier.equals("FedEx")){
			url=fedExURL;
			urlParameters=composeFedExRequestMessage(trackingNumber);
		}else{
			url=upsURL;
			urlParameters=composeUPSRequestMessage(trackingNumber);
		}
		
		URL obj = new URL(url);
		HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
 
		//add request header
		con.setRequestMethod("POST");
		con.setRequestProperty("User-Agent", USER_AGENT);
		con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
 
 
		// Send post request
		con.setDoOutput(true);
		DataOutputStream wr = new DataOutputStream(con.getOutputStream());
		wr.writeBytes(urlParameters);
		wr.flush();
		wr.close();
 
		int responseCode = con.getResponseCode();
		System.out.println("\nSending 'POST' request to URL : " + url);
		System.out.println("Post parameters : " + urlParameters);
		System.out.println("Response Code : " + responseCode);
 
		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		
		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		}catch(Exception e){
			e.printStackTrace();
		}
		//print result
		System.out.println(response.toString());
		return response.toString();
		
	}
	private String composeFedExRequestMessage(String trackingNumber){
		String message = "<TrackRequest xmlns='http://fedex.com/ws/track/v3'><WebAuthenticationDetail><UserCredential><Key>"+fedExKey
				+"</Key><Password>"+fedExPassword+"</Password></UserCredential></WebAuthenticationDetail><ClientDetail>"
				+ "<AccountNumber>"+fedExAccountNumber+"</AccountNumber><MeterNumber>"+fedExMeterNumber+"</MeterNumber></ClientDetail>"
				+ "<TransactionDetail><CustomerTransactionId>ActiveShipping</CustomerTransactionId></TransactionDetail>"
				+ "<Version><ServiceId>trck</ServiceId><Major>3</Major><Intermediate>0</Intermediate><Minor>0</Minor></Version>"
				+ "<PackageIdentifier><Value>" +trackingNumber +"</Value><Type>TRACKING_NUMBER_OR_DOORTAG</Type></PackageIdentifier>"
				+ "<IncludeDetailedScans>1</IncludeDetailedScans></TrackRequest>";
		return message;
	}
	
	private String composeUPSRequestMessage(String trackingNumber){
		String message="<?xml version=\"1.0\"?><AccessRequest xml:lang=\"en-US\"><AccessLicenseNumber>"+upsAccessLicenseNumber+"</AccessLicenseNumber>"
				+ "<UserId>"+upsUserId+"</UserId><Password>"+upsPassword+"</Password></AccessRequest>"
				+ "<?xml version=\"1.0\"?><TrackRequest xml:lang=\"en-US\"><Request><TransactionReference>"
						+ "<CustomerContext>QAST Track</CustomerContext><XpciVersion>1.0</XpciVersion></TransactionReference>"
						+ "<RequestAction>Track</RequestAction><RequestOption>activity</RequestOption></Request>"
						+ "<TrackingNumber>"+trackingNumber+"</TrackingNumber></TrackRequest>";
		return message;
	}
	
	private static void parseResponse(String carrier, String response){
		try{
			//create temp file to store response
			File responseFile = new File("response.xml");
			PrintWriter writer = new PrintWriter(responseFile);
			writer.print(response);
			writer.flush();
			writer.close();
			
			System.out.println("\n===Response Parsed Info:===");
			
			//parse the respose to query
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(responseFile);
			doc.getDocumentElement().normalize();
			
			if(carrier.equals("FedEx")){
				System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
				System.out.println("LocalizedMessage: "+ doc.getElementsByTagName("LocalizedMessage").item(0).getTextContent());
				System.out.println("StatusDescription: "+ doc.getElementsByTagName("StatusDescription").item(0).getTextContent());
				System.out.println("ShipTimestamp: "+ doc.getElementsByTagName("ShipTimestamp").item(0).getTextContent());
				System.out.println("ActualDeliveryTimestamp: "+ doc.getElementsByTagName("ActualDeliveryTimestamp").item(0).getTextContent());
			}else{
				System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
				System.out.println("ResponseStatusDescription: "+ doc.getElementsByTagName("ResponseStatusDescription").item(0).getTextContent());
				System.out.println("PickupDate: "+ doc.getElementsByTagName("PickupDate").item(0).getTextContent());
			}
			responseFile.delete();
		}catch(Exception e){
			e.printStackTrace();
		}
	
	}
 
}