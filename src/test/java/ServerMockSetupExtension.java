import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.matching.UrlPattern;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;

public class ServerMockSetupExtension implements BeforeEachCallback, AfterAllCallback, BeforeAllCallback, AfterEachCallback {
    private ObjectMapper objectMapper;
    private WireMockServer wireMockServer = new WireMockServer(9090);
    private List<StubMapping> testInstanceIds = new ArrayList<>();

    public ServerMockSetupExtension() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        ServerMockSetup[] serverMockSetups = extensionContext.getElement().get().getDeclaredAnnotationsByType(ServerMockSetup.class);
        for (ServerMockSetup serverMockSetup : serverMockSetups) {
            testInstanceIds.addAll(createStubs(extensionContext, serverMockSetup.stubs()));
        }
    }

    private List<StubMapping> createStubs(ExtensionContext extensionContext, String[] stubs) throws IOException {
        List<StubMapping> testInstanceIds = new ArrayList<>();
        for (String stubFileName : stubs) {
            File stub = new File(Objects.requireNonNull(extensionContext.getRequiredTestClass().getClassLoader().getResource(stubFileName)).getFile());
            byte[] stubAsBytes = Files.readAllBytes(stub.toPath());
            MockSetup mockSetup = objectMapper.readValue(stubAsBytes, MockSetup.class);
            UrlPattern urlMatcher = urlEqualTo(mockSetup.path);
            MappingBuilder mappingBuilder = getHttpMethodFunction(mockSetup.getMethod()).apply(urlMatcher);
            ResponseDefinitionBuilder mockResp = WireMock.aResponse().withBody(mockSetup.getBody().toString())
                    .withStatus(mockSetup.getResponseCode());
            StubMapping stubMapping = wireMockServer.stubFor(mappingBuilder.willReturn(mockResp));
            testInstanceIds.add(stubMapping);
        }
        return testInstanceIds;
    }

    private Function<UrlPattern, MappingBuilder> getHttpMethodFunction(String httpMethod) {
        switch (httpMethod) {
            case "GET":
                return WireMock::get;
            case "POST":
                return WireMock::post;
            case "PATCH":
                return WireMock::patch;
            case "PUT":
                return WireMock::put;
            case "DELETE":
                return WireMock::delete;
            case "HEAD":
                return WireMock::head;
            case "OPTIONS":
                return WireMock::options;
            default:
                return WireMock::any;
        }
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        wireMockServer.stop();
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        DefaultServerMockSetup defaultServerMockSetup = extensionContext.getRequiredTestClass().getDeclaredAnnotationsByType(DefaultServerMockSetup.class)[0];
        createStubs(extensionContext, defaultServerMockSetup.stubs());
        wireMockServer.start();

    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        testInstanceIds.forEach(wireMockServer::removeStub);
        testInstanceIds = new ArrayList<>();
    }
}