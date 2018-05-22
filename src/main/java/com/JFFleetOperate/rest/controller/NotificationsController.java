package com.JFFleetOperate.rest.controller;

import com.JFFleetOperate.beans.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SequenceWriter;
import com.fasterxml.jackson.databind.SerializationFeature;


@RestController
public class NotificationsController {
	

private static final String welcomemsg = "Welcome Mr. %s!";
    @GetMapping("/welcome/user")
    @ResponseBody
    public Notifications welcomeUser(@RequestParam(name="name", required=false, defaultValue="Java Fan") String name) {
        return new Notifications(String.format(welcomemsg, name));
    }
    
    @RequestMapping(value = "/sendNotification", method = RequestMethod.POST)
    public String sendNotification(@RequestParam(name="driverId", required=false, defaultValue="329") String driverId) {
    	MobileNotificationService objMobileNotificationService = new MobileNotificationService();
    	objMobileNotificationService.sendNotification(driverId);
     // TODO: call persistence layer to update
     return "";
    }
    
  /*  @Value("${server.servlet.context-path}")
    String phyPath;*/
    
    @RequestMapping(value = "/registerNotifications", method = RequestMethod.POST)
    public Notifications registerNotification(@RequestBody DeviceInfo deviceInfo) {
    	SNSConfiguration SNSClient = new SNSConfiguration();
    	System.out.println("calling.....");
    	String endPointArn = SNSClient.registerWithSNS(deviceInfo.deviceToken);
    	System.out.println(endPointArn);
    	//save the data to json
    	
    	 ObjectMapper objectMapper = new ObjectMapper();

    	SNSEndPointInfo objSNSEndPointInfo = new SNSEndPointInfo();

    	objSNSEndPointInfo.setDriverId(deviceInfo.getDriverId());
    	objSNSEndPointInfo.setDeviceId(deviceInfo.deviceId);
    	objSNSEndPointInfo.setEndPointArn(endPointArn);
    	    

    	    objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

    	    try {
    	        
    	           
    	           File file=new File(System.getProperty("user.dir"), "data.json");
    	           
    	           if (file.createNewFile()) {
    	   			System.out.println("File is created!");
    	   			List<SNSEndPointInfo> listOfEndPoints = new ArrayList<>();
        	        listOfEndPoints.add(objSNSEndPointInfo);
    	   			objectMapper.writeValue(file.getAbsoluteFile(), listOfEndPoints);
    	   		} else {
    	   			System.out.println("File is already existed!");
    	   			
    	   	    	List<SNSEndPointInfo> participantJsonList = objectMapper.readValue(file, new TypeReference<List<SNSEndPointInfo>>(){});
    	   	     	
    	   	    	//List<SNSEndPointInfo> filterData= participantJsonList.stream().filter(p -> p.getDriverId() == objSNSEndPointInfo.driverId && p.getDeviceId() == objSNSEndPointInfo.deviceId).collect(Collectors.toList());
    	   	    	
    	   	    	List<Integer> indices = new ArrayList();
    	   	    	
    	   	    	List<SNSEndPointInfo> FilterJsonList = new ArrayList();
    	   	    	
    	   	    	for(int i=0;i<participantJsonList.size();i++) {
    	   	    		if((objSNSEndPointInfo.driverId.equals(participantJsonList.get(i).getDriverId()) && objSNSEndPointInfo.deviceId.equals(participantJsonList.get(i).getDeviceId()))) {
    	   	    			//indices.add(i);
    	   	    			
    	   	    		}
    	   	    		else
    	   	    		{
    	   	    			FilterJsonList.add(participantJsonList.get(i));
    	   	    		}
    	   	    		
    	   	    		//else
    	   	    		//{
    	   	    			//participantJsonList.add(objSNSEndPointInfo);
    	   	    		//objectMapper.writeValue(file.getAbsoluteFile(), participantJsonList);
    	   	    		
    	   	    		//}
    	   	  
    	   	    	}
    	   	    	FilterJsonList.add(objSNSEndPointInfo);
    	   	    	objectMapper.writeValue(file.getAbsoluteFile(), FilterJsonList);
    	   	    	/*if(filterData.size() > 0)
    	   	    	{
    	   	    		for (SNSEndPointInfo snsEndPointInfo : filterData) {
    	   	    			participantJsonList.remove(snsEndPointInfo);
						}
    	   	    		
    	   	    	}*/
    	   	    	
    	   	    	
//    	   	    	participantJsonList.add(objSNSEndPointInfo);
//    	   	    	objectMapper.writeValue(file.getAbsoluteFile(), participantJsonList);
    	   		}
    	           
    	        
    	    } catch (IOException e) {
    	        e.printStackTrace();
    	    }
    	
    	return new Notifications(String.format("endPointArn", endPointArn));
    	//return endPointArn;
    }

}
