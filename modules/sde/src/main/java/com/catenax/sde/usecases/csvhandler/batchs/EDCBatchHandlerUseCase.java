/********************************************************************************
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

package com.catenax.sde.usecases.csvhandler.batchs;


import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.catenax.sde.edc.entities.request.asset.AssetEntryRequest;
import com.catenax.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import com.catenax.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import com.catenax.sde.edc.entities.request.contractdefinition.ContractDefinitionRequestFactory;
import com.catenax.sde.edc.entities.request.policies.ConstraintRequest;
import com.catenax.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import com.catenax.sde.edc.entities.request.policies.PolicyDefinitionRequest;
import com.catenax.sde.edc.entities.request.policies.PolicyRequestFactory;
import com.catenax.sde.edc.enums.UsagePolicyEnum;
import com.catenax.sde.edc.gateways.external.EDCGateway;
import com.catenax.sde.entities.usecases.Batch;
import com.catenax.sde.usecases.csvhandler.AbstractCsvHandlerUseCase;
import com.catenax.sde.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;
import com.catenax.sde.edc.model.response.EdcCommonResponse;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class EDCBatchHandlerUseCase extends AbstractCsvHandlerUseCase<Batch, Batch> {

    @Value("#{new Boolean('${edc.enabled:false}')}")
    private boolean isEdcEnable;
    private final AssetEntryRequestFactory assetFactory;
    private final EDCGateway edcGateway;
    private final PolicyRequestFactory policyFactory;
    private final ContractDefinitionRequestFactory contractFactory;
    private final PolicyConstraintBuilderService policyConstraintBuilderService;


    public EDCBatchHandlerUseCase(StoreBatchCsvHandlerUseCase nextUseCase,
                                  EDCGateway edcGateway,
                                  AssetEntryRequestFactory assetFactory,
                                  PolicyRequestFactory policyFactory,
                                  ContractDefinitionRequestFactory contractFactory, PolicyConstraintBuilderService policyConstraintBuilderService) {
        super(nextUseCase);
        this.assetFactory = assetFactory;
        this.edcGateway = edcGateway;
        this.policyFactory = policyFactory;
        this.contractFactory = contractFactory;
        this.policyConstraintBuilderService = policyConstraintBuilderService;
    }

    @SneakyThrows
    @Override
    protected Batch executeUseCase(Batch input, String processId) {
        if (!isEdcEnable){
            return input;
        }
        HashMap<String, String> extensibleProperties = new HashMap<>();
        String shellId = input.getShellId();
        String subModelId = input.getSubModelId();

        try {

            AssetEntryRequest assetEntryRequest = assetFactory.getBatchAssetRequest(shellId, subModelId, input.getUuid());
            if (!edcGateway.assetExistsLookup(assetEntryRequest.getAsset().getProperties().get("asset:prop:id"))) {
            	ResponseEntity<EdcCommonResponse> edcAssetId=edcGateway.createAsset(assetEntryRequest);

                List<ConstraintRequest> usageConstraints =  policyConstraintBuilderService.getUsagePolicyConstraints(input.getUsagePolicies());
                List<ConstraintRequest> accessConstraints =  policyConstraintBuilderService.getAccessConstraints(input.getBpnNumbers());

                String customValue = getCustomValue(input);
                if(StringUtils.isNotBlank(customValue))
                {
                    extensibleProperties.put(UsagePolicyEnum.CUSTOM.name(), customValue);
                }

                PolicyDefinitionRequest accessPolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId, accessConstraints, new HashMap<>());
                PolicyDefinitionRequest usagePolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId, usageConstraints, extensibleProperties);

                ResponseEntity<EdcCommonResponse> edcPolicyAccess=edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);
                ResponseEntity<EdcCommonResponse> edcPolicyUsage=edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);

                ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(
                        assetEntryRequest.getAsset().getProperties().get("asset:prop:id"),
                        accessPolicyDefinitionRequest.getId(), usagePolicyDefinitionRequest.getId());
                ResponseEntity<EdcCommonResponse> edcContractDefinationId=edcGateway.createContractDefinition(contractDefinitionRequest);
                input.setAssetId(edcAssetId.getBody().getId());
                input.setAccessPolicyId(edcPolicyAccess.getBody().getId());
                input.setUsagePolicyId(edcPolicyUsage.getBody().getId());
                input.setContractDefinationId(edcContractDefinationId.getBody().getId());
            }

            return input;
        } catch (Exception e) {
            throw new CsvHandlerUseCaseException(input.getRowNumber(), "EDC: " + e.getMessage());
        }
    }

    private String getCustomValue(Batch input) {
        if(!CollectionUtils.isEmpty(input.getUsagePolicies()))
        {
            return input.getUsagePolicies().stream().filter(policy -> policy.getType()
                    .equals(UsagePolicyEnum.CUSTOM)).map(value -> value.getValue()).findFirst().orElse(null);
        }
        return null;
    }
}
