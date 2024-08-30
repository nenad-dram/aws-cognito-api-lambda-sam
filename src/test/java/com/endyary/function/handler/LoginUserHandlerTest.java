package com.endyary.function.handler;

import static com.endyary.function.service.Constants.COGNITO_USER_ID;
import static com.endyary.function.service.Constants.IS_CONFIRMED;
import static com.endyary.function.service.Constants.IS_SUCCESSFUL;
import static com.endyary.function.service.Constants.STATUS_CODE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.endyary.function.service.UserService;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.http.HttpStatusCode;

@ExtendWith(MockitoExtension.class)
class LoginUserHandlerTest {

  @Mock
  private Context context;
  @Mock
  private LambdaLogger lambdaLogger;
  @Mock
  private APIGatewayProxyRequestEvent event;

  @Test
  void handleRequestTest() {
    JsonObject loginResult = new JsonObject();
    loginResult.addProperty(IS_SUCCESSFUL, true);
    loginResult.addProperty(STATUS_CODE, HttpStatusCode.OK);
    loginResult.addProperty("idToken", "idTokenValue");
    loginResult.addProperty("accessToken", "accessTokenValue");
    loginResult.addProperty("refreshToken", "refreshTokenValue");

    try (MockedConstruction<UserService> mockUserService = Mockito.mockConstruction(
        UserService.class,
        (mock, context) -> {
          when(mock.login(any(JsonObject.class))).thenReturn(loginResult);
        })) {

      when(context.getLogger()).thenReturn(lambdaLogger);
      when(event.getBody()).thenReturn("{'email':'user@mail.com', 'password':'userP@ssw0rd'}");

      LoginUserHandler loginUserHandler = new LoginUserHandler();
      APIGatewayProxyResponseEvent response = loginUserHandler.handleRequest(event, context);
      assertEquals(HttpStatusCode.OK, response.getStatusCode());
      assertEquals(new GsonBuilder().create().toJson(loginResult), response.getBody());
    }
  }
}
