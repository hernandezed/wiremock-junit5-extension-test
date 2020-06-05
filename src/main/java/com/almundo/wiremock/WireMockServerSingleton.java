package com.almundo.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class WireMockServerSingleton {

    private static WireMockServer wireMockServer;

    public static WireMockServer getInstance() {
        if (wireMockServer == null) {
            wireMockServer = new WireMockServer(options().dynamicHttpsPort().dynamicPort());
            wireMockServer.start();
        }
        return wireMockServer;
    }

}
