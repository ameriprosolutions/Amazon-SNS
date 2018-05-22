package com.JFFleetOperate.beans;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

//import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.PublishRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;


public class MobileNotificationService  {
	
	public void sendNotification(String driverId)
	{
		SNSConfiguration pushClient = new SNSConfiguration();
		
		/*Topic based Notification*/
		//CreateTopicResult  createRes = pushClient.ssnClient().createTopic("LogEditSuggestion");
        //System.out.println("TopicArn" + createRes.getTopicArn());
        //pushClient.ssnClient().publish(new PublishRequest(createRes.getTopicArn(), "Hello..!"));
        
		//Get TargetARN
		 ObjectMapper objectMapper = new ObjectMapper();
		 objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
		 File file=new File(System.getProperty("user.dir"), "data.json");
		 
		try {
			List<SNSEndPointInfo> participantJsonList = objectMapper.readValue(file, new TypeReference<List<SNSEndPointInfo>>(){});
			
			List<SNSEndPointInfo> filterData= participantJsonList.stream().filter(p -> p.getDriverId().equals(driverId)).distinct().collect(Collectors.toList());
			
			PublishRequest obj;
			System.out.println("jdd");
			for (SNSEndPointInfo snsEndPointInfo : filterData) {
				obj = new PublishRequest();
				obj.setTargetArn(snsEndPointInfo.endPointArn);
				obj.setMessage("Hello Nilesh");
			    pushClient.ssnClient().publish(obj);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
     
	}
}
