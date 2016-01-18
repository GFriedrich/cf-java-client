/*
 * Copyright 2013-2016 the original author or authors.
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

package org.cloudfoundry.client;

import org.cloudfoundry.AbstractIntegrationTest;
import org.cloudfoundry.client.v2.organizations.AssociateOrganizationAuditorByUsernameRequest;
import org.cloudfoundry.client.v2.organizations.AssociateOrganizationAuditorRequest;
import org.cloudfoundry.client.v2.organizations.AssociateOrganizationBillingManagerByUsernameRequest;
import org.cloudfoundry.client.v2.organizations.AssociateOrganizationBillingManagerRequest;
import org.cloudfoundry.client.v2.organizations.CreateOrganizationRequest;
import org.cloudfoundry.client.v2.organizations.ListOrganizationAuditorsRequest;
import org.cloudfoundry.client.v2.organizations.RemoveOrganizationAuditorByUsernameRequest;
import org.cloudfoundry.client.v2.organizations.RemoveOrganizationAuditorRequest;
import org.cloudfoundry.client.v2.organizations.RemoveOrganizationBillingManagerByUsernameRequest;
import org.cloudfoundry.client.v2.organizations.RemoveOrganizationBillingManagerRequest;
import org.cloudfoundry.client.v2.users.ListUsersRequest;
import org.cloudfoundry.operations.util.v2.Paginated;
import org.cloudfoundry.operations.util.v2.Resources;
import org.junit.Test;
import reactor.Mono;

import static org.junit.Assert.assertTrue;

public final class OrganizationsTest extends AbstractIntegrationTest {

    @Test
    public void auditor() {
        getAdminId()
                .and(this.organizationId)
                .then(tuple -> {
                    AssociateOrganizationAuditorRequest request = AssociateOrganizationAuditorRequest.builder()
                            .auditorId(tuple.t1)
                            .organizationId(tuple.t2)
                            .build();

                    return Mono.just(tuple.t1).and(this.cloudFoundryClient.organizations().associateAuditor(request));
                })
                .doOnSuccess(tuple -> {
                    assertTrue(Paginated
                            .requestResources(page -> {
                                ListOrganizationAuditorsRequest request = ListOrganizationAuditorsRequest.builder()
                                        .page(page)
                                        .id(Resources.getId(tuple.t2))
                                        .build();
                                return this.cloudFoundryClient.organizations().listAuditors(request);
                            })
                            .exists(auditor -> Resources.getId(auditor).equals(tuple.t1))
                            .get());
                })
                .then(tuple -> {
                    RemoveOrganizationAuditorRequest request = RemoveOrganizationAuditorRequest.builder()
                            .auditorId(tuple.t1)
                            .id(Resources.getId(tuple.t2))
                            .build();

                    return this.cloudFoundryClient.organizations().removeAuditor(request);
                })
                .subscribe(this.testSubscriber());
    }

    @Test
    public void auditorByUsername() {
        this.organizationId
                .then(orgId -> {
                    AssociateOrganizationAuditorByUsernameRequest request = AssociateOrganizationAuditorByUsernameRequest.builder()
                            .username("admin")
                            .id(orgId)
                            .build();

                    return this.cloudFoundryClient.organizations().associateAuditorByUsername(request);
                })
                .then(response -> {
                    RemoveOrganizationAuditorByUsernameRequest request = RemoveOrganizationAuditorByUsernameRequest.builder()
                            .username("admin")
                            .id(response.getMetadata().getId())
                            .build();

                    return this.cloudFoundryClient.organizations().removeAuditorByUsername(request);
                })
                .subscribe(this.testSubscriber());
    }

    @Test
    public void billingManager() {
        getAdminId()
                .and(this.organizationId)
                .then(tuple -> {
                    AssociateOrganizationBillingManagerRequest request = AssociateOrganizationBillingManagerRequest.builder()
                            .billingManagerId(tuple.t1)
                            .id(tuple.t2)
                            .build();

                    return this.cloudFoundryClient.organizations().associateBillingManager(request)
                            .and(Mono.just(tuple.t1));
                })
                .then(tuple -> {
                    RemoveOrganizationBillingManagerRequest request = RemoveOrganizationBillingManagerRequest.builder()
                            .billingManagerId(tuple.t2)
                            .id(Resources.getId(tuple.t1))
                            .build();

                    return this.cloudFoundryClient.organizations().removeBillingManager(request);
                })
                .subscribe(this.testSubscriber());
    }

    @Test
    public void billingManagerByUsername() {
        this.organizationId
                .then(orgId -> {
                    AssociateOrganizationBillingManagerByUsernameRequest request = AssociateOrganizationBillingManagerByUsernameRequest.builder()
                            .username("admin")
                            .id(orgId)
                            .build();

                    return this.cloudFoundryClient.organizations().associateBillingManagerByUsername(request)
                            .and(Mono.just(orgId));
                })
                .then(tuple -> {
                    RemoveOrganizationBillingManagerByUsernameRequest request = RemoveOrganizationBillingManagerByUsernameRequest.builder()
                            .username("admin")
                            .id(tuple.t2)
                            .build();

                    return this.cloudFoundryClient.organizations().removeBillingManagerByUsername(request);
                })
                .subscribe(this.testSubscriber());
    }

    @Test
    public void create() {
        CreateOrganizationRequest request = CreateOrganizationRequest.builder()
                .name("test-org")
                .build();

        this.cloudFoundryClient.organizations().create(request)
                .map(response -> Resources.getEntity(response).getName())
                .subscribe(this.testSubscriber()
                        .assertEquals("test-org"));
    }

    private Mono<String> getAdminId() {
        return Paginated
                .requestResources(page -> {
                    ListUsersRequest request = ListUsersRequest.builder()
                            .page(page)
                            .build();

                    return this.cloudFoundryClient.users().listUsers(request);
                })
                .filter(userResource -> Resources.getEntity(userResource).getUsername().equals("admin"))
                .single()
                .map(Resources::getId);
    }

}