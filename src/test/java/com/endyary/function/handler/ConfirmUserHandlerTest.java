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
class ConfirmUserHandlerTest {

  @Mock
  private Context context;
  @Mock
  private LambdaLogger lambdaLogger;
  @Mock
  private APIGatewayProxyRequestEvent event;

  @Test
  void handleRequestTest() {
    JsonObject confirmResult = new JsonObject();
    confirmResult.addProperty(IS_SUCCESSFUL, true);
    confirmResult.addProperty(STATUS_CODE, HttpStatusCode.OK);

    try (MockedConstruction<UserService> mockUserService = Mockito.mockConstruction(
        UserService.class,
        (mock, context) -> {
          when(mock.confirm("user@mail.com", "confirmationCode"))
              .thenReturn(confirmResult);
        })) {

      when(context.getLogger()).thenReturn(lambdaLogger);
      when(event.getBody()).thenReturn("{'email':'user@mail.com', 'code':'confirmationCode'}");

      ConfirmUserHandler confirmUserHandler = new ConfirmUserHandler();
      APIGatewayProxyResponseEvent response = confirmUserHandler.handleRequest(event, context);
      assertEquals(HttpStatusCode.OK, response.getStatusCode());
      assertEquals(new GsonBuilder().create().toJson(confirmResult), response.getBody());
    }

  }
}
