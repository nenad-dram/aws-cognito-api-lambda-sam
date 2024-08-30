package com.endyary.function.service;

import static com.endyary.function.service.Constants.COGNITO_USER_ID;
import static com.endyary.function.service.Constants.EMAIL;
import static com.endyary.function.service.Constants.IS_CONFIRMED;
import static com.endyary.function.service.Constants.IS_SUCCESSFUL;
import static com.endyary.function.service.Constants.NAME;
import static com.endyary.function.service.Constants.PASSWORD;
import static com.endyary.function.service.Constants.STATUS_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.google.gson.JsonObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.http.SdkHttpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.ConfirmSignUpResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.SignUpResponse;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static CognitoIdentityProviderClient cognitoIdentityProviderClient;
  private static final String CLIENT_ID = "cognitoClientId";
  private static final String CLIENT_SECRET = "cognitoClientSecret";

  @BeforeAll
  static void init() {
    cognitoIdentityProviderClient = mock(CognitoIdentityProviderClient.class);
  }

  @Test
  void registerTest() {
    try (MockedStatic<CognitoProvider> cognitoProviderMocked = mockStatic(CognitoProvider.class)) {
      cognitoProviderMocked.when(CognitoProvider::getIdentityClient)
          .thenReturn(cognitoIdentityProviderClient);
      cognitoProviderMocked.when(CognitoProvider::getClientId).thenReturn(CLIENT_ID);
      cognitoProviderMocked.when(CognitoProvider::getClientSecret).thenReturn(CLIENT_SECRET);

      SdkHttpResponse sdkHttpResponse = mock(SdkHttpResponse.class);
      SignUpResponse signUpResponse = mock(SignUpResponse.class);
      when(sdkHttpResponse.isSuccessful()).thenReturn(true);
      when(sdkHttpResponse.statusCode()).thenReturn(HttpStatusCode.OK);
      when(signUpResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);
      when(signUpResponse.userConfirmed()).thenReturn(false);
      when(signUpResponse.userSub()).thenReturn("userId");

      when(cognitoIdentityProviderClient.signUp(any(SignUpRequest.class)))
          .thenReturn(signUpResponse);

      UserService userService = new UserService();
      JsonObject jsonUser = new JsonObject();
      jsonUser.addProperty(EMAIL, "user@mail.com");
      jsonUser.addProperty(PASSWORD, "userP@ssw0rd");
      jsonUser.addProperty(NAME, "John Doe");

      JsonObject response = userService.register(jsonUser);
      assertTrue(response.get(IS_SUCCESSFUL).getAsBoolean());
      assertEquals(HttpStatusCode.OK, response.get(STATUS_CODE).getAsInt());
      assertEquals("userId", response.get(COGNITO_USER_ID).getAsString());
      assertFalse(response.get(IS_CONFIRMED).getAsBoolean());
    }
  }

  @Test
  void confirmTest() {
    try(MockedStatic<CognitoProvider> cognitoProviderMocked = mockStatic(CognitoProvider.class)) {
      cognitoProviderMocked.when(CognitoProvider::getIdentityClient)
          .thenReturn(cognitoIdentityProviderClient);
      cognitoProviderMocked.when(CognitoProvider::getClientId).thenReturn(CLIENT_ID);
      cognitoProviderMocked.when(CognitoProvider::getClientSecret).thenReturn(CLIENT_SECRET);

      SdkHttpResponse sdkHttpResponse = mock(SdkHttpResponse.class);
      ConfirmSignUpResponse confirmSignUpResponse = mock(ConfirmSignUpResponse.class);
      when(sdkHttpResponse.isSuccessful()).thenReturn(true);
      when(sdkHttpResponse.statusCode()).thenReturn(HttpStatusCode.OK);
      when(confirmSignUpResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);

      when(cognitoIdentityProviderClient.confirmSignUp(any(ConfirmSignUpRequest.class)))
          .thenReturn(confirmSignUpResponse);

      UserService userService = new UserService();

      JsonObject response = userService.confirm("user@mail.com", "confirmCode");
      assertTrue(response.get(IS_SUCCESSFUL).getAsBoolean());
      assertEquals(HttpStatusCode.OK, response.get(STATUS_CODE).getAsInt());
    }
  }

  @Test
  void loginTest() {
    try(MockedStatic<CognitoProvider> cognitoProviderMocked = mockStatic(CognitoProvider.class)) {
      cognitoProviderMocked.when(CognitoProvider::getIdentityClient)
          .thenReturn(cognitoIdentityProviderClient);
      cognitoProviderMocked.when(CognitoProvider::getClientId).thenReturn(CLIENT_ID);
      cognitoProviderMocked.when(CognitoProvider::getClientSecret).thenReturn(CLIENT_SECRET);

      SdkHttpResponse sdkHttpResponse = mock(SdkHttpResponse.class);
      InitiateAuthResponse initiateAuthResponse = mock(InitiateAuthResponse.class);
      AuthenticationResultType authenticationResult = mock(AuthenticationResultType.class);
      when(sdkHttpResponse.isSuccessful()).thenReturn(true);
      when(sdkHttpResponse.statusCode()).thenReturn(HttpStatusCode.OK);
      when(authenticationResult.idToken()).thenReturn("idTokenValue");
      when(authenticationResult.accessToken()).thenReturn("accessTokenValue");
      when(authenticationResult.refreshToken()).thenReturn("refreshTokenValue");
      when(initiateAuthResponse.sdkHttpResponse()).thenReturn(sdkHttpResponse);
      when(initiateAuthResponse.authenticationResult()).thenReturn(authenticationResult);

      when(cognitoIdentityProviderClient.initiateAuth(any(InitiateAuthRequest.class)))
          .thenReturn(initiateAuthResponse);

      UserService userService = new UserService();
      JsonObject jsonUser = new JsonObject();
      jsonUser.addProperty(EMAIL, "user@mail.com");
      jsonUser.addProperty(PASSWORD, "userP@ssw0rd");

      JsonObject response = userService.login(jsonUser);
      assertTrue(response.get(IS_SUCCESSFUL).getAsBoolean());
      assertEquals(HttpStatusCode.OK, response.get(STATUS_CODE).getAsInt());
      assertEquals("idTokenValue", response.get("idToken").getAsString());
      assertEquals("accessTokenValue", response.get("accessToken").getAsString());
      assertEquals("refreshTokenValue", response.get("refreshToken").getAsString());
    }
  }

}
