package com.JFFleetOperate.beans;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.CreatePlatformEndpointRequest;
import com.amazonaws.services.sns.model.CreatePlatformEndpointResult;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.GetEndpointAttributesRequest;
import com.amazonaws.services.sns.model.GetEndpointAttributesResult;
import com.amazonaws.services.sns.model.InvalidParameterException;
//import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.NotFoundException;
import com.amazonaws.services.sns.model.SetEndpointAttributesRequest;
//import com.amazonaws.services.sns.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
//import java.util.List;
import java.util.Map;
//import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
@Configuration
public class SNSConfiguration {
 
	public String arnStorage;
    //@Value("${aws.sns.accessKey}")
    private String accessKey ="AKIAJ7SBD3IPTZ762R3A";
    //@Value("${aws.sns.secretKey}")
    private String secretKey="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";
    //@Value("${aws.sns.region}")
    //private String region="US_EAST_1";
    //@Value("${aws.sns.topicArn}")
    private String topicArn= "arn:aws:sns:us-east-1:621926565379:LogEditSuggestion";
   // @Value("${aws.sns.topicName}")
    private String topicName= "LogEditSuggestion";
 
    private static final Logger LOGGER = LoggerFactory.getLogger(SNSConfiguration.class);
 
//    @Bean
    public AmazonSNS ssnClient() {
        // Create Amazon SNS Client
        AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        AmazonSNS snsClient = AmazonSNSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
                .withRegion(Regions.US_EAST_1)
                .build();
 
        // OPTIONAL: Check the topic already created or not
        /*ListTopicsResult listTopicsResult = snsClient.listTopics();
        List<Topic> topics = listTopicsResult.getTopics();
 
        Optional<Topic> result = topics.stream()
             .filter(t -> topicArn.equalsIgnoreCase(t.getTopicArn())).findAny();
 
        // Create a new topic if it doesn't exist
        if(!result.isPresent()) {
            createSNSTopic(snsClient);
        }*/
        
        return snsClient;
    }
 
    private CreateTopicResult createSNSTopic(AmazonSNS snsClient) {
        CreateTopicRequest createTopic = new CreateTopicRequest(topicName);
        CreateTopicResult result = snsClient.createTopic(createTopic);
        LOGGER.info("Created topic request: " +
                snsClient.getCachedResponseMetadata(createTopic));
        return  result;
    }
    
    private String createEndpoint(String token) {
        String endpointArn = null;
        try {
            System.out.println("Creating platform endpoint with token " + token);
            //create endPoint
            CreatePlatformEndpointRequest cpeReq =
                    new CreatePlatformEndpointRequest()
                            .withPlatformApplicationArn("arn:aws:sns:us-east-1:621926565379:app/GCM/FleetOperate")
                            .withCustomUserData("SNS Application")
                            .withToken(token);
            CreatePlatformEndpointResult cpeRes = ssnClient()
                    .createPlatformEndpoint(cpeReq);
            endpointArn = cpeRes.getEndpointArn();

           /* // Subscribe to topic
            SubscribeRequest subscribeRequest=new SubscribeRequest();
            subscribeRequest.setProtocol("application");
            subscribeRequest.setTopicArn("arn:aws:sns:us-east-1:621926565379:createEvent");
            subscribeRequest.setEndpoint(endpointArn);
            ssnClient().subscribe(subscribeRequest);*/
            
        } catch (InvalidParameterException ipe) {
            String message = ipe.getErrorMessage();
            System.out.println("Exception message: " + message);
            Pattern p = Pattern
                    .compile(".*Endpoint (arn:aws:sns[^ ]+) already exists " +
                            "with the same token.*");
            Matcher m = p.matcher(message);
            if (m.matches()) {
                // The platform endpoint already exists for this token, but with
                // additional custom data that
                // createEndpoint doesn't want to overwrite. Just use the
                // existing platform endpoint.
                endpointArn = m.group(1);
            } else {
                // Rethrow the exception, the input is actually bad.
                throw ipe;
            }
        }
        storeEndpointArn(endpointArn);
        return endpointArn;
    }
    private void storeEndpointArn(String endpointArn) {
        // Write the platform endpoint ARN to permanent storage(Database).
        arnStorage = endpointArn;
    }
    private String retrieveEndpointArn(String Token) {
        // Retrieve the platform endpoint ARN from permanent storage(Database) ,
        // or return null if null is stored.
        return arnStorage;
    }
    public String registerWithSNS(String token) {
        String endpointArn = retrieveEndpointArn(token);
        boolean updateNeeded = false;
        boolean createNeeded = (null == endpointArn);
        if (createNeeded) {
            // No platform endpoint ARN is stored; need to call createEndpoint.
            endpointArn = createEndpoint(token);
            createNeeded = false;
        }
        System.out.println("Retrieving platform endpoint data...");
        // Look up the platform endpoint and make sure the data in it is current, even if
        // it was just created.
        try {
            GetEndpointAttributesRequest geaReq =
                    new GetEndpointAttributesRequest()
                            .withEndpointArn(endpointArn);
            GetEndpointAttributesResult geaRes =
                    ssnClient().getEndpointAttributes(geaReq);
            
            updateNeeded = !geaRes.getAttributes().get("Token").equals(token)
                    || !geaRes.getAttributes().get("Enabled").equalsIgnoreCase("true");
        } catch (NotFoundException nfe) {
            // We had a stored ARN, but the platform endpoint associated with it
            // disappeared. Recreate it.
            createNeeded = true;
        }
        if (createNeeded) {
            createEndpoint(token);
        }
        System.out.println("updateNeeded = " + updateNeeded);
        if (updateNeeded) {
            // The platform endpoint is out of sync with the current data;
            // update the token and enable it.
            System.out.println("Updating platform endpoint " + endpointArn);
            Map attribs = new HashMap();
            attribs.put("Token", token);
            attribs.put("Enabled", "true");
            SetEndpointAttributesRequest saeReq =
                    new SetEndpointAttributesRequest()
                            .withEndpointArn(endpointArn)
                            .withAttributes(attribs);
            ssnClient().setEndpointAttributes(saeReq);
        }
        return endpointArn;
    }
}