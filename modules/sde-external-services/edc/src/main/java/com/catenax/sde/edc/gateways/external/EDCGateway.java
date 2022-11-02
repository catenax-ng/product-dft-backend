/********************************************************************************
   * Copyright (c) 2022 BMW GmbH
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the CatenaX (ng) GitHub Organisation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ********************************************************************************/

package com.catenax.sde.edc.gateways.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.catenax.sde.edc.model.response.EdcCommonResponse;
import com.catenax.sde.edc.entities.request.asset.AssetEntryRequest;
import com.catenax.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import com.catenax.sde.edc.entities.request.policies.PolicyDefinitionRequest;
import com.catenax.sde.edc.exceptions.EDCGatewayException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EDCGateway {

    @Value(value = "${edc.hostname}")
    private String edcHostname;
    @Value(value = "${edc.apiKeyHeader}")
    private String apiKeyHeader;
    @Value(value = "${edc.apiKey}")
    private String apiKey;

    public boolean assetExistsLookup(String id) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(apiKeyHeader, apiKey);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(headers);

        try {
            String url = edcHostname + "/assets/"+ id;
            restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    Object.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return false;
            }
            throw e;
        }
        return true;
    }

    public ResponseEntity<EdcCommonResponse> createAsset(AssetEntryRequest request) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(apiKeyHeader, apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AssetEntryRequest> entity = new HttpEntity<>(request, headers);
        try {
            return restTemplate.postForEntity(edcHostname + "/assets", entity, EdcCommonResponse.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new EDCGatewayException("Asset already exists");
            }
            throw new EDCGatewayException(e.getStatusCode().toString());
        }
    }

    public ResponseEntity<EdcCommonResponse> createPolicyDefinition(PolicyDefinitionRequest request) {
        final String policyResource = "/policydefinitions";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(apiKeyHeader, apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<PolicyDefinitionRequest> entity = new HttpEntity<>(request, headers);
        try {
        	 return restTemplate.postForEntity(edcHostname + policyResource, entity, EdcCommonResponse.class);
        } catch (HttpClientErrorException e) {
            throw new EDCGatewayException(e.getStatusCode().toString());
        }
    }

    public ResponseEntity<EdcCommonResponse> createContractDefinition(ContractDefinitionRequest request) {
        final String contractDefinitionResource = "/contractdefinitions";
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.add(apiKeyHeader, apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ContractDefinitionRequest> entity = new HttpEntity<>(request, headers);
        try {
        	 return restTemplate.postForEntity(edcHostname + contractDefinitionResource, entity, EdcCommonResponse.class);
        } catch (HttpClientErrorException e) {
            throw new EDCGatewayException(e.getStatusCode().toString());
        }
    }
}