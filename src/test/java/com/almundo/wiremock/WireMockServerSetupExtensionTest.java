package com.almundo.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.Mockito;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WireMockServerSetupExtensionTest {

    private final WireMockServerSetupExtension wireMockServerSetupExtension = new WireMockServerSetupExtension();

    @AfterEach
    void tearDown() {
        WireMockServerSingleton.getInstance().getStubMappings().forEach(WireMockServerSingleton.getInstance()::removeStub);
    }

    @Test
    void beforeAll_withoutAnnotations_doNothing() {
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        Mockito.doReturn(BeforeAllWithoutAnnotationsDoNothing.class).when(extensionContext).getRequiredTestClass();
        wireMockServerSetupExtension.beforeAll(extensionContext);
        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();
        assertThat(wireMockServer.getStubMappings()).isEmpty();
    }

    @Test
    void beforeAll_withOneServerStubAnnotation_withOneStub_addOneStubMapping() {
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        Mockito.doReturn(BeforeAllWithOneServerStubAnnotationWithOneStubAddOneStubMapping.class).when(extensionContext).getRequiredTestClass();
        wireMockServerSetupExtension.beforeAll(extensionContext);
        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();
        assertThat(wireMockServer.getStubMappings()).hasSize(1);
    }

    @Test
    void beforeAll_withOneServerStubAnnotation_withTwoStubs_addTwoStubMapping() {
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        Mockito.doReturn(BeforeAllWithOneServerStubAnnotationWithTwoStubsAddTwoStubMapping.class).when(extensionContext).getRequiredTestClass();
        wireMockServerSetupExtension.beforeAll(extensionContext);
        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();
        assertThat(wireMockServer.getStubMappings()).hasSize(2);
    }

    @Test
    void beforeAll_withoutStubMapping_withInheritingFromClassWithStubMapping_addStubMapping() {
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        Mockito.doReturn(BeforeAllWithoutStubMappingWithInheritingFromClassWithStubMappingAddStubMapping.class).when(extensionContext).getRequiredTestClass();
        wireMockServerSetupExtension.beforeAll(extensionContext);
        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();
        assertThat(wireMockServer.getStubMappings()).hasSize(1);
    }

    @Test
    void beforeAll_withStubMapping_withInheritingFromClassWithStubMapping_addStubMapping() {
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        Mockito.doReturn(BeforeAllWithStubMappingWithInheritingFromClassWithStubMappingAddStubMapping.class).when(extensionContext).getRequiredTestClass();
        wireMockServerSetupExtension.beforeAll(extensionContext);
        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();
        assertThat(wireMockServer.getStubMappings()).hasSize(3);
    }

    @Test
    void afterEach_withoutStubsInServer_doNothing() {
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        Mockito.doReturn(BeforeAllWithStubMappingWithInheritingFromClassWithStubMappingAddStubMapping.class).when(extensionContext).getRequiredTestClass();
        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();
        List<StubMapping> stubsBefore = wireMockServer.getStubMappings();
        wireMockServerSetupExtension.afterEach(extensionContext);
        List<StubMapping> stubsAfter = wireMockServer.getStubMappings();
        assertThat(stubsAfter).isEqualTo(stubsBefore);
    }

    @Test
    void afterEach_onlyPersistentStubs_doNothing() {
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        Mockito.doReturn(BeforeAllWithStubMappingWithInheritingFromClassWithStubMappingAddStubMapping.class).when(extensionContext).getRequiredTestClass();
        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();
        wireMockServer.stubFor(get("").willReturn(aResponse().withStatus(200)));
        List<StubMapping> stubsBefore = wireMockServer.getStubMappings();
        wireMockServerSetupExtension.afterEach(extensionContext);
        List<StubMapping> stubsAfter = wireMockServer.getStubMappings();
        assertThat(stubsAfter).isEqualTo(stubsBefore);
    }

    @Test
    void afterEach_withNonPersistentStubs_removeNonPersistent() throws NoSuchFieldException, IllegalAccessException {
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        Mockito.doReturn(BeforeAllWithStubMappingWithInheritingFromClassWithStubMappingAddStubMapping.class).when(extensionContext).getRequiredTestClass();
        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();
        wireMockServer.stubFor(get("").willReturn(aResponse().withStatus(200)));
        StubMapping nonPersistent = wireMockServer.stubFor(get("/a").willReturn(aResponse().withStatus(200)));

        Field field = WireMockServerSetupExtension.class.getDeclaredField("nonPersistentStubMappings");
        field.setAccessible(true);
        List<StubMapping> stubMappings = (List<StubMapping>) field.get(wireMockServerSetupExtension);
        stubMappings.add(nonPersistent);
        field.setAccessible(false);

        List<StubMapping> stubsBefore = wireMockServer.getStubMappings();
        wireMockServerSetupExtension.afterEach(extensionContext);
        List<StubMapping> stubsAfter = wireMockServer.getStubMappings();
        assertThat(stubsAfter).hasSize(1);
        assertThat(stubsBefore).hasSize(2);
        assertThat(stubsAfter.get(0).getRequest().getUrl()).isEqualTo("");
    }

    @Test
    void beforeEach_withTestWithoutStubMapping_doNothing() {
        AnnotatedElement annotatedElement = mock(AnnotatedElement.class);
        when(annotatedElement.getDeclaredAnnotationsByType(any())).thenReturn(new ServerMockSetup[]{
        });

        ExtensionContext extensionContext = mock(ExtensionContext.class);
        when(extensionContext.getElement()).thenReturn(Optional.of(annotatedElement));
        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();

        List<StubMapping> beforeStubMappings = wireMockServer.getStubMappings();

        wireMockServerSetupExtension.beforeEach(extensionContext);

        List<StubMapping> afterStubMappings = wireMockServer.getStubMappings();

        assertThat(afterStubMappings).isEqualTo(beforeStubMappings);
    }

    @Test
    void beforeEach_withTestWithSingleStubMapping_addOneStub() {
        AnnotatedElement annotatedElement = mock(AnnotatedElement.class);
        ServerMockSetup serverMockSetup = mock(ServerMockSetup.class);

        when(serverMockSetup.stubs()).thenReturn(new String[]{"test_files/GetSomeThing.json"});
        when(annotatedElement.getDeclaredAnnotationsByType(any())).thenReturn(new ServerMockSetup[]{
                serverMockSetup
        });
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        when(extensionContext.getElement()).thenReturn(Optional.of(annotatedElement));
        Mockito.doReturn(BeforeAllWithOneServerStubAnnotationWithOneStubAddOneStubMapping.class).when(extensionContext).getRequiredTestClass();

        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();

        List<StubMapping> beforeStubMappings = wireMockServer.getStubMappings();

        wireMockServerSetupExtension.beforeEach(extensionContext);

        List<StubMapping> afterStubMappings = wireMockServer.getStubMappings();

        assertThat(afterStubMappings).isNotEqualTo(beforeStubMappings).hasSize(beforeStubMappings.size() + 1);
    }

    @Test
    void beforeEach_withTestWithSingleServerMockSetup_WithMultipleStubs_addStubs() {
        AnnotatedElement annotatedElement = mock(AnnotatedElement.class);
        ServerMockSetup serverMockSetup = mock(ServerMockSetup.class);

        when(serverMockSetup.stubs()).thenReturn(new String[]{"test_files/GetSomeThing.json", "test_files/GetSomeThing2.json"});
        when(annotatedElement.getDeclaredAnnotationsByType(any())).thenReturn(new ServerMockSetup[]{
                serverMockSetup
        });
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        when(extensionContext.getElement()).thenReturn(Optional.of(annotatedElement));
        Mockito.doReturn(BeforeAllWithOneServerStubAnnotationWithOneStubAddOneStubMapping.class).when(extensionContext).getRequiredTestClass();

        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();

        List<StubMapping> beforeStubMappings = wireMockServer.getStubMappings();

        wireMockServerSetupExtension.beforeEach(extensionContext);

        List<StubMapping> afterStubMappings = wireMockServer.getStubMappings();

        assertThat(afterStubMappings).isNotEqualTo(beforeStubMappings).hasSize(beforeStubMappings.size() + 2);
    }

    @Test
    void beforeEach_withTestWithMultipleServerMockSetup_addStubs() {
        AnnotatedElement annotatedElement = mock(AnnotatedElement.class);
        ServerMockSetup serverMockSetup1 = mock(ServerMockSetup.class);
        ServerMockSetup serverMockSetup2 = mock(ServerMockSetup.class);

        when(serverMockSetup1.stubs()).thenReturn(new String[]{"test_files/GetSomeThing.json"});
        when(serverMockSetup2.stubs()).thenReturn(new String[]{"test_files/GetSomeThing2.json"});
        when(annotatedElement.getDeclaredAnnotationsByType(any())).thenReturn(new ServerMockSetup[]{
                serverMockSetup1,
                serverMockSetup2
        });
        ExtensionContext extensionContext = mock(ExtensionContext.class);
        when(extensionContext.getElement()).thenReturn(Optional.of(annotatedElement));
        Mockito.doReturn(BeforeAllWithOneServerStubAnnotationWithOneStubAddOneStubMapping.class).when(extensionContext).getRequiredTestClass();

        WireMockServer wireMockServer = WireMockServerSingleton.getInstance();

        List<StubMapping> beforeStubMappings = wireMockServer.getStubMappings();

        wireMockServerSetupExtension.beforeEach(extensionContext);

        List<StubMapping> afterStubMappings = wireMockServer.getStubMappings();

        assertThat(afterStubMappings).isNotEqualTo(beforeStubMappings).hasSize(beforeStubMappings.size() + 2);
    }

    class BeforeAllWithoutAnnotationsDoNothing {
    }

    @ServerMockSetup(stubs = "test_files/GetSomeThing.json")
    class BeforeAllWithOneServerStubAnnotationWithOneStubAddOneStubMapping {
    }

    @ServerMockSetup(stubs = {"test_files/GetSomeThing.json", "test_files/GetSomeThing2.json"})
    class BeforeAllWithOneServerStubAnnotationWithTwoStubsAddTwoStubMapping {
    }

    class BeforeAllWithoutStubMappingWithInheritingFromClassWithStubMappingAddStubMapping extends BeforeAllWithOneServerStubAnnotationWithOneStubAddOneStubMapping {
    }

    @ServerMockSetup(stubs = "test_files/GetSomeThing.json")
    class BeforeAllWithStubMappingWithInheritingFromClassWithStubMappingAddStubMapping extends BeforeAllWithOneServerStubAnnotationWithTwoStubsAddTwoStubMapping {

    }
}

