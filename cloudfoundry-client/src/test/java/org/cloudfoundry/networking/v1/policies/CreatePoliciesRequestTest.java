/*
 * Copyright 2013-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.networking.v1.policies;

import org.junit.Test;

public class CreatePoliciesRequestTest {

    @Test(expected = IllegalStateException.class)
    public void noPolicy() {
        CreatePoliciesRequest.builder()
            .build();
    }

    @Test
    public void valid() {
        CreatePoliciesRequest.builder()
            .policy(Policy.builder()
                .destination(Destination.builder()
                    .id("test-destination-id")
                    .ports(Ports.builder()
                        .end(2)
                        .start(1)
                        .build())
                    .protocol("test-protocol")
                    .build())
                .source(Source.builder()
                    .id("test-source-id")
                    .build())
                .build())
            .build();
    }

}