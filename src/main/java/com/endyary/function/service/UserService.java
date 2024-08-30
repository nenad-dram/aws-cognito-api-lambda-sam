package com.endyary.function.service;

import static com.endyary.function.service.Constants.COGNITO_USER_ID;
import static com.endyary.function.service.Constants.EMAIL;
import static com.endyary.function.service.Constants.IS_CONFIRMED;
import static com.endyary.function.service.Constants.IS_SUCCESSFUL;
import static com.endyary.function.service.Constants.NAME;
import static com.endyary.function.service.Constants.PASSWORD;
import static com.endyary.function.service.Constants.STATUS_CODE;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.gson.JsonObject;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

public class UserService {

  private final CognitoIdentityProviderClient cognitoIdentityProviderClient;
  private final String appClientId;
  private final String appClientSecret;

  public UserService() {
    this.cognitoIdentityProviderClient = CognitoProvider.getIdentityClient();
    this.appClientId = CognitoProvider.getClientId();
    this.appClientSecret = CognitoProvider.getClientSecret();
  }

  public JsonObject register(JsonObject user) {
    String email = user.get(EMAIL).getAsString();
    String password = user.get(PASSWORD).getAsString();
    String name = user.get(NAME).getAsString();

    AttributeType emailAttribute = AttributeType.builder()
        .name(EMAIL)
        .value(email)
        .build();

    AttributeType nameAttribute = AttributeType.builder()
        .name(NAME)
        .value(name)
        .build();

    List<AttributeType> attributes = new ArrayList<>();
    attributes.add(emailAttribute);
    attributes.add(nameAttribute);

    String generatedSecretHash = calculateSecretHash(appClientId, appClientSecret, email);

    SignUpRequest signUpRequest = SignUpRequest.builder()
        .username(email)
        .password(password)
        .userAttributes(attributes)
        .clientId(appClientId)
        .secretHash(generatedSecretHash)
        .build();

    SignUpResponse signupResponse = cognitoIdentityProviderClient.signUp(signUpRequest);
    JsonObject registerResult = new JsonObject();
    registerResult.addProperty(IS_SUCCESSFUL, signupResponse.sdkHttpResponse().isSuccessful());
    registerResult.addProperty(STATUS_CODE, signupResponse.sdkHttpResponse().statusCode());
    registerResult.addProperty(COGNITO_USER_ID, signupResponse.userSub());
    registerResult.addProperty(IS_CONFIRMED, signupResponse.userConfirmed());

    return registerResult;
  }

  public JsonObject confirm(String email, String confirmationCode) {

    String generatedSecretHash = calculateSecretHash(appClientId, appClientSecret, email);

    ConfirmSignUpRequest confirmSignUpRequest = ConfirmSignUpRequest.builder()
        .secretHash(generatedSecretHash)
        .username(email)
        .confirmationCode(confirmationCode)
        .clientId(appClientId)
        .build();

    ConfirmSignUpResponse confirmSignUpResponse = cognitoIdentityProviderClient.confirmSignUp(
        confirmSignUpRequest);

    JsonObject confirmUserResponse = new JsonObject();
    confirmUserResponse.addProperty(IS_SUCCESSFUL,
        confirmSignUpResponse.sdkHttpResponse().isSuccessful());
    confirmUserResponse.addProperty(STATUS_CODE,
        confirmSignUpResponse.sdkHttpResponse().statusCode());
    return confirmUserResponse;

  }

  public JsonObject login(JsonObject loginDetails) {

    String email = loginDetails.get(EMAIL).getAsString();
    String password = loginDetails.get(PASSWORD).getAsString();
    String generatedSecretHash = calculateSecretHash(appClientId, appClientSecret, email);

    Map<String, String> authParams = new HashMap<>();
    authParams.put("USERNAME", email);
    authParams.put("PASSWORD", password);
    authParams.put("SECRET_HASH", generatedSecretHash);

    InitiateAuthRequest initiateAuthRequest = InitiateAuthRequest.builder()
        .clientId(appClientId)
        .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
        .authParameters(authParams)
        .build();
    InitiateAuthResponse initiateAuthResponse = cognitoIdentityProviderClient.initiateAuth(
        initiateAuthRequest);
    AuthenticationResultType authenticationResultType = initiateAuthResponse.authenticationResult();

    JsonObject loginUserResult = new JsonObject();
    loginUserResult.addProperty("isSuccessful",
        initiateAuthResponse.sdkHttpResponse().isSuccessful());
    loginUserResult.addProperty("statusCode", initiateAuthResponse.sdkHttpResponse().statusCode());
    loginUserResult.addProperty("idToken", authenticationResultType.idToken());
    loginUserResult.addProperty("accessToken", authenticationResultType.accessToken());
    loginUserResult.addProperty("refreshToken", authenticationResultType.refreshToken());

    return loginUserResult;

  }

  /**
   * @see <a href="https://docs.aws.amazon.com/cognito/latest/developerguide/signing-up-users-in-your-app.html#cognito-user-pools-computing-secret-hash">...</a>
   */
  private String calculateSecretHash(String userPoolClientId, String userPoolClientSecret,
      String userName) {
    final String HMAC_SHA256_ALGORITHM = "HmacSHA256";

    SecretKeySpec signingKey = new SecretKeySpec(
        userPoolClientSecret.getBytes(StandardCharsets.UTF_8),
        HMAC_SHA256_ALGORITHM);
    try {
      Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
      mac.init(signingKey);
      mac.update(userName.getBytes(StandardCharsets.UTF_8));
      byte[] rawHmac = mac.doFinal(userPoolClientId.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(rawHmac);
    } catch (Exception e) {
      throw new RuntimeException("Error while calculating ");
    }
  }

}
