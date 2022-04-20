/*
 * Copyright 2022 CatenaX
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.catenax.dft.gateways.external;

import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.CreateContractDefinitionRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EDCGatewayTest {

    @Value(value = "{edc.aspect.url}")
    private String edcEndpoint;
    private String API_KEY = "X-Api-Key";
    private String API_VALUE = "123456";
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EDCGateway edcGateway;

    @Test
    @DisplayName("Testing createAsset() method")
    void createAsset() {
        AssetEntryRequest request = new AssetEntryRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.add(API_KEY, API_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AssetEntryRequest> entity = new HttpEntity<>(request, headers);

        when(restTemplate.postForEntity(edcEndpoint + "/assets", entity, String.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        HttpStatus status = edcGateway.createAsset(request);

        assertEquals(status, HttpStatus.NO_CONTENT);
    }

    @Test
    @DisplayName("Testing createContractDefinition method")
    void createContractDefinition() {
        CreateContractDefinitionRequest request = new CreateContractDefinitionRequest();
        HttpHeaders headers = new HttpHeaders();
        headers.add(API_KEY, API_VALUE);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<CreateContractDefinitionRequest> entity = new HttpEntity<>(request, headers);

        when(restTemplate.postForEntity(edcEndpoint + "/contractdefinitions", entity, String.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        HttpStatus status = edcGateway.createContractDefinition(request);

        assertEquals(status, HttpStatus.NO_CONTENT);
    }
}