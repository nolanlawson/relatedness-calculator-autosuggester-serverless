package autosuggester;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import java.util.HashMap;

public class AppTest {
  @Test
  public void successfulResponse() {
    App app = new App();
    APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
    input.setQueryStringParameters(new HashMap<String, String>(){{
      put("q", "grandfather");
    }});
    APIGatewayProxyResponseEvent result = app.handleRequest(input, null);
    assertEquals(result.getStatusCode().intValue(), 200);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json;charset=utf-8");
    String content = result.getBody();
    assertNotNull(content);
    assertEquals(content, "{\"results\":[\"grandfather\",\"grandfather's aunt\",\"grandfather's cousin\",\"grandfather's father\",\"grandfather's grandma\",\"grandfather's grandpa\",\"grandfather's grandparent\",\"grandfather's great-aunt\",\"grandfather's great-grandma\",\"grandfather's great-grandpa\"]}");
  }

  @Test
  public void emptyRequest() {
    App app = new App();
    APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
    APIGatewayProxyResponseEvent result = app.handleRequest(input, null);
    assertEquals(result.getStatusCode().intValue(), 500);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json;charset=utf-8");
    String content = result.getBody();
    assertNotNull(content);
    assertEquals(content, "{\"error\":true}");
  }

  @Test
  public void nullRequest() {
    App app = new App();
    APIGatewayProxyResponseEvent result = app.handleRequest(null, null);
    assertEquals(result.getStatusCode().intValue(), 500);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json;charset=utf-8");
    String content = result.getBody();
    assertNotNull(content);
    assertEquals(content, "{\"error\":true}");
  }

  @Test
  public void requestTooLong() {
    App app = new App();
    APIGatewayProxyRequestEvent input = new APIGatewayProxyRequestEvent();
    input.setQueryStringParameters(new HashMap<String, String>(){{
      String q = "";
      for (int i = 0; i < 1000; i++) {
        q += "grandfather";
      }
      put("q", q);
    }});
    APIGatewayProxyResponseEvent result = app.handleRequest(input, null);
    assertEquals(result.getStatusCode().intValue(), 500);
    assertEquals(result.getHeaders().get("Content-Type"), "application/json;charset=utf-8");
    String content = result.getBody();
    assertNotNull(content);
    assertEquals(content, "{\"error\":true}");
  }
}
