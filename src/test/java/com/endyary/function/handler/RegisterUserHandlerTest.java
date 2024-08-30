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
class RegisterUserHandlerTest {
  @Mock
  private Context context;
  @Mock
  private LambdaLogger lambdaLogger;
  @Mock
  private APIGatewayProxyRequestEvent event;

  @Test
  void handleRequestTest() {
    JsonObject registerResult = new JsonObject();
    registerResult.addProperty(IS_SUCCESSFUL, true);
    registerResult.addProperty(STATUS_CODE, HttpStatusCode.OK);
    registerResult.addProperty(COGNITO_USER_ID, "userId");
    registerResult.addProperty(IS_CONFIRMED, false);

    try (MockedConstruction<UserService> mockUserService = Mockito.mockConstruction(
        UserService.class,
        (mock, context) -> {
          when(mock.register(any(JsonObject.class))).thenReturn(registerResult);
        })) {

      when(context.getLogger()).thenReturn(lambdaLogger);
      when(event.getBody()).thenReturn("{'email':'user@mail.com', "
          + "'password':'userP@ssw0rd', "
          + "'name':'John Doe'}");

      RegisterUserHandler registerUserHandler = new RegisterUserHandler();
      APIGatewayProxyResponseEvent response = registerUserHandler.handleRequest(event, context);
      assertEquals(HttpStatusCode.OK, response.getStatusCode());
      assertEquals(new GsonBuilder().create().toJson(registerResult), response.getBody());
    }
  }

}
