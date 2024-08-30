package com.endyary.function.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.endyary.function.service.UserService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

public class LoginUserHandler implements
    RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

  private final UserService userService;
  private static final Gson GSON = new GsonBuilder().create();

  public LoginUserHandler() {
    this.userService = new UserService();
  }

  @Override
  public APIGatewayProxyResponseEvent handleRequest(
      APIGatewayProxyRequestEvent event, Context context) {
    Map<String, String> headers = new HashMap<>();
    headers.put("Content-Type", "application/json");

    LambdaLogger logger = context.getLogger();

    try (JsonReader reader = new JsonReader(new StringReader(event.getBody()))) {
      JsonObject loginDetails = JsonParser.parseReader(reader).getAsJsonObject();
      JsonObject loginResult = userService.login(loginDetails);
      return new APIGatewayProxyResponseEvent()
          .withHeaders(headers)
          .withBody(GSON.toJson(loginResult, JsonObject.class))
          .withStatusCode(200);
    } catch (AwsServiceException ex) {
      return handleException(logger, ex.awsErrorDetails().errorMessage(),
          ex.awsErrorDetails().sdkHttpResponse().statusCode());
    } catch (Exception ex) {
      return handleException(logger, ex.getMessage(), 500);
    }
  }

  private APIGatewayProxyResponseEvent handleException(LambdaLogger logger, String errorMessage,
      int statusCode) {
    logger.log(errorMessage);
    return new APIGatewayProxyResponseEvent()
        .withBody(errorMessage)
        .withStatusCode(statusCode);
  }
}
