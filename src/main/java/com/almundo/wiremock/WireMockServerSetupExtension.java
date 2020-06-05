package com.almundo.wiremock;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class WireMockServerSetupExtension implements BeforeEachCallback, BeforeAllCallback, AfterEachCallback {

    private ObjectMapper objectMapper;
    private static WireMockServer wireMockServer;
    private List<StubMapping> nonPersistentStubMappings;

    public WireMockServerSetupExtension() {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        wireMockServer = WireMockServerSingleton.getInstance();
        nonPersistentStubMappings = new ArrayList<>();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        nonPersistentStubMappings.forEach(wireMockServer::removeStub);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) {
        Class<?> clazz = extensionContext.getRequiredTestClass();
        List<ServerMockSetup> serverMockSetups = new ArrayList();
        do {
            ServerMockSetup[] defaultServerMockSetups = clazz.getDeclaredAnnotationsByType(ServerMockSetup.class);
            Collections.addAll(serverMockSetups, defaultServerMockSetups);
            clazz = clazz.getSuperclass();
        } while (!clazz.equals(Object.class));
        addStubFromServerMockSetups(extensionContext, serverMockSetups.toArray(ServerMockSetup[]::new), true);
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        ServerMockSetup[] serverMockSetups = extensionContext.getElement().get()
                .getDeclaredAnnotationsByType(ServerMockSetup.class);
        addStubFromServerMockSetups(extensionContext, serverMockSetups, false);
    }

    private StubMapping createStubMapping(byte[] file) throws IOException {
        return objectMapper.readValue(file, StubMapping.class);
    }

    private byte[] getFile(ExtensionContext extensionContext, String filename) throws IOException {
        File stub = new File(Objects.requireNonNull(
                extensionContext.getRequiredTestClass().getClassLoader().getResource(filename)).getFile());
        return Files.readAllBytes(stub.toPath());
    }

    private void addStubFromServerMockSetups(ExtensionContext extensionContext, ServerMockSetup[] serverMockSetups, boolean persistent) {
        for (ServerMockSetup serverMockSetup : serverMockSetups) {
            addStub(extensionContext, serverMockSetup.stubs(), persistent);
        }
    }

    private void addStub(ExtensionContext extensionContext, String[] filenames, boolean persistent) {
        Arrays.stream(filenames).forEach(filename -> {
            byte[] file;
            try {
                file = getFile(extensionContext, filename);
                StubMapping stubMapping = createStubMapping(file);
                wireMockServer.addStubMapping(stubMapping);
                if (!persistent) {
                    nonPersistentStubMappings.add(stubMapping);
                }
            } catch (IOException e) {
                throw new RuntimeException();
            }
        });
    }
}
