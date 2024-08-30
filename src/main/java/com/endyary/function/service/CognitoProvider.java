package com.endyary.function.service;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.core.SdkSystemSetting;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

public class CognitoProvider {

  public static final String ENV_CLIENT_ID = "COGNITO_POOL_APP_CLIENT_ID";
  public static final String ENV_CLIENT_SECRET = "COGNITO_POOL_APP_CLIENT_SECRET";

  private CognitoProvider(){}

  public static CognitoIdentityProviderClient getIdentityClient() {
    return CognitoIdentityProviderClient.builder()
        .region(Region.of(System.getenv(SdkSystemSetting.AWS_REGION.environmentVariable())))
        .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
        .build();
  }

  public static String getClientId() {
    return System.getenv(ENV_CLIENT_ID);
  }

  public static String getClientSecret() {
    return System.getenv(ENV_CLIENT_SECRET);
  }

}
