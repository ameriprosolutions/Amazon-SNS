package com.JFFleetOperate.beans;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("aws.sns")
public class AWSProperties {

	private String accessKey;
	public String getAccessKey() {
		return accessKey;
	}
	public void setAccessKey(String accessKey) {
		this.accessKey = accessKey;
	}
	public String getSecretKey() {
		return secretKey;
	}
	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
	public String getRegion() {
		return region;
	}
	public void setRegion(String region) {
		this.region = region;
	}
	public String getTopicArn() {
		return topicArn;
	}
	public void setTopicArn(String topicArn) {
		this.topicArn = topicArn;
	}
	public String getTopicName() {
		return topicName;
	}
	public void setTopicName(String topicName) {
		this.topicName = topicName;
	}
	public String getEndPointArn() {
		return endPointArn;
	}
	public void setEndPointArn(String endPointArn) {
		this.endPointArn = endPointArn;
	}
	private String secretKey;
	private String region;
	private String topicArn;
	private String topicName;
	private String endPointArn;

}