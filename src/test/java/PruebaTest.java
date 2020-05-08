import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


@ExtendWith(ServerMockSetupExtension.class)
public class PruebaTest {
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .build();

    @Test
    @ServerMockSetup(stubs = {"stub1.json", "stub2.json"})
    void testing() throws IOException, InterruptedException {

        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:9090/test"))
                .build(), HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
        System.out.println(response.statusCode());


        HttpResponse<String> response3 = httpClient.send(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:9090/test3"))
                .build(), HttpResponse.BodyHandlers.ofString());

        System.out.println(response3.body());
        System.out.println(response3.statusCode());
    }


    @Test
    void testing2() throws IOException, InterruptedException {

        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:9090/test"))
                .build(), HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
        System.out.println(response.statusCode());


        HttpResponse<String> response3 = httpClient.send(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:9090/test3"))
                .build(), HttpResponse.BodyHandlers.ofString());

        System.out.println(response3.body());
        System.out.println(response3.statusCode());
    }


    @Test
    @ServerMockSetup(stubs = {"stub4.json"})
    void testing4() throws IOException, InterruptedException {

        HttpResponse<String> response = httpClient.send(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:9090/test"))
                .build(), HttpResponse.BodyHandlers.ofString());

        System.out.println(response.body());
        System.out.println(response.statusCode());


        HttpResponse<String> response3 = httpClient.send(HttpRequest.newBuilder()
                .GET()
                .uri(URI.create("http://localhost:9090/test3"))
                .build(), HttpResponse.BodyHandlers.ofString());

        System.out.println(response3.body());
        System.out.println(response3.statusCode());
    }
}
