import com.fasterxml.jackson.databind.JsonNode;

public class MockSetup {

    String path;
    JsonNode body;
    String method;
    int responseCode;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public JsonNode getBody() {
        return body;
    }

    public void setBody(JsonNode body) {
        this.body = body;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    @Override
    public String toString() {
        return "MockSetup{" +
                "path='" + path + '\'' +
                ", body='" + body + '\'' +
                ", method='" + method + '\'' +
                ", responseCode='" + responseCode + '\'' +
                '}';
    }
}
