/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.file.remote.integration;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Unit test to test keepLastModified option.
 */
public class FromFtpKeepLastModifiedIT extends FtpServerTestSupport {

    protected String getFtpUrl() {
        return "ftp://admin@localhost:{{ftp.server.port}}/keep?password=admin&binary=false&noop=true";
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        template.sendBodyAndHeader(getFtpUrl(), "Hello World", "CamelFileName", "hello.txt");
    }

    @Test
    public void testKeepLastModified() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(getFtpUrl()).delay(3000).to(fileUri("?keepLastModified=true"), "mock:result");
            }
        });
        context.start();

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedFileExists(testFile("hello.txt"));
        mock.message(0).header(Exchange.FILE_LAST_MODIFIED).isNotNull();

        assertMockEndpointsSatisfied();

        long t1 = mock.getReceivedExchanges().get(0).getIn().getHeader(Exchange.FILE_LAST_MODIFIED, long.class);
        long t2 = testFile("hello.txt").toFile().lastModified();

        assertEquals(t1, t2, "Timestamp should have been kept");
    }

    @Test
    public void testDoNotKeepLastModified() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(getFtpUrl()).delay(3000).to(fileUri("?keepLastModified=false"), "mock:result");
            }
        });
        context.start();

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedFileExists(testFile("hello.txt"));
        mock.message(0).header(Exchange.FILE_LAST_MODIFIED).isNotNull();

        assertMockEndpointsSatisfied();

        long t1 = mock.getReceivedExchanges().get(0).getIn().getHeader(Exchange.FILE_LAST_MODIFIED, long.class);
        long t2 = testFile("hello.txt").toFile().lastModified();

        assertNotSame(t1, t2, "Timestamp should NOT have been kept");
    }

    @Test
    public void testDoNotKeepLastModifiedIsDefault() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from(getFtpUrl()).delay(3000).to(fileUri(), "mock:result");
            }
        });
        context.start();

        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.expectedFileExists(testFile("hello.txt"));
        mock.message(0).header(Exchange.FILE_LAST_MODIFIED).isNotNull();

        assertMockEndpointsSatisfied();

        long t1 = mock.getReceivedExchanges().get(0).getIn().getHeader(Exchange.FILE_LAST_MODIFIED, long.class);
        long t2 = testFile("hello.txt").toFile().lastModified();

        assertNotSame(t1, t2, "Timestamp should NOT have been kept");
    }
}
