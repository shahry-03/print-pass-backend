package com.printpass.job.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    // application.yml se keys nikal rahe hain
    @Value("${cloud.digitalocean.spaces.access-key}")
    private String accessKey;

    @Value("${cloud.digitalocean.spaces.secret-key}")
    private String secretKey;

    @Value("${cloud.digitalocean.spaces.endpoint}")
    private String endpoint;

    @Value("${cloud.digitalocean.spaces.region}")
    private String region;

    // Yeh jadooi bean humein poori app mein cloud access dega
    @Bean
    public AmazonS3 s3Client() {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
                .build();
    }
}