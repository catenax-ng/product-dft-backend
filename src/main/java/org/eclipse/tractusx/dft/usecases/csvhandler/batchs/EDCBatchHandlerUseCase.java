/********************************************************************************
 * Copyright (c) 2022 T-Systems International GmbH
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.eclipse.tractusx.dft.usecases.csvhandler.batchs;


import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.dft.entities.edc.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.dft.entities.edc.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.dft.entities.edc.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.dft.entities.edc.request.contractdefinition.ContractDefinitionRequestFactory;
import org.eclipse.tractusx.dft.entities.edc.request.policies.ConstraintRequest;
import org.eclipse.tractusx.dft.entities.edc.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.dft.entities.edc.request.policies.PolicyDefinitionRequest;
import org.eclipse.tractusx.dft.entities.edc.request.policies.PolicyRequestFactory;
import org.eclipse.tractusx.dft.entities.usecases.Batch;
import org.eclipse.tractusx.dft.enums.UsagePolicyEnum;
import org.eclipse.tractusx.dft.gateways.external.EDCGateway;
import org.eclipse.tractusx.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import org.eclipse.tractusx.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

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
                edcGateway.createAsset(assetEntryRequest);

                List<ConstraintRequest> usageConstraints =  policyConstraintBuilderService.getUsagePolicyConstraints(input.getUsagePolicies());
                List<ConstraintRequest> accessConstraints =  policyConstraintBuilderService.getAccessConstraints(input.getBpnNumbers());

                String customValue = getCustomValue(input);
                if(StringUtils.isNotBlank(customValue))
                {
                    extensibleProperties.put(UsagePolicyEnum.CUSTOM.name(), customValue);
                }

                PolicyDefinitionRequest accessPolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId, accessConstraints, new HashMap<>());
                PolicyDefinitionRequest usagePolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId, usageConstraints, extensibleProperties);

                edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);
                edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);

                ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(
                        assetEntryRequest.getAsset().getProperties().get("asset:prop:id"),
                        accessPolicyDefinitionRequest.getId(), usagePolicyDefinitionRequest.getId());
                edcGateway.createContractDefinition(contractDefinitionRequest);
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
