/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
 * Copyright (c) 2022 BMW GmbH
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

package org.eclipse.tractusx.sde.submodels.apr.steps;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.enums.UsagePolicyEnum;
import org.eclipse.tractusx.sde.common.exception.CsvHandlerUseCaseException;
import org.eclipse.tractusx.sde.common.submodel.executor.Step;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequest;
import org.eclipse.tractusx.sde.edc.entities.request.asset.AssetEntryRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.contractdefinition.ContractDefinitionRequestFactory;
import org.eclipse.tractusx.sde.edc.entities.request.policies.ConstraintRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyConstraintBuilderService;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyDefinitionRequest;
import org.eclipse.tractusx.sde.edc.entities.request.policies.PolicyRequestFactory;
import org.eclipse.tractusx.sde.edc.facilitator.DeleteEDCFacilitator;
import org.eclipse.tractusx.sde.edc.gateways.external.EDCGateway;
import org.eclipse.tractusx.sde.submodels.apr.entity.AspectRelationshipEntity;
import org.eclipse.tractusx.sde.submodels.apr.mapper.AspectRelationshipMapper;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationship;
import org.eclipse.tractusx.sde.submodels.apr.model.AspectRelationshipResponse;
import org.eclipse.tractusx.sde.submodels.apr.model.ChildPart;
import org.eclipse.tractusx.sde.submodels.apr.service.AspectRelationshipService;
import org.eclipse.tractusx.sde.submodels.spt.entity.AspectEntity;
import org.eclipse.tractusx.sde.submodels.spt.model.Aspect;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.gson.JsonObject;

import lombok.SneakyThrows;

@Service
public class EDCAspectRelationshipHandlerUseCase extends Step {

	private static final String ASSET_PROP_NAME_ASPECT_RELATIONSHIP = "Serialized Part - Submodel AssemblyPartRelationship";
	private static final String UPDATED_Y = "Y";
	
	private final AssetEntryRequestFactory assetFactory;
	private final EDCGateway edcGateway;
	private final PolicyRequestFactory policyFactory;
	private final ContractDefinitionRequestFactory contractFactory;
	private final PolicyConstraintBuilderService policyConstraintBuilderService;
	private final AspectRelationshipService aspectRelationshipService;
	private final DeleteEDCFacilitator deleteEDCFacilitator;
	private final AspectRelationshipMapper aspectRelationshipMapper;
	
	public EDCAspectRelationshipHandlerUseCase(EDCGateway edcGateway, AssetEntryRequestFactory assetFactory,
			PolicyRequestFactory policyFactory, ContractDefinitionRequestFactory contractFactory,
			PolicyConstraintBuilderService policyConstraintBuilderService,
			AspectRelationshipService aspectRelationshipService, DeleteEDCFacilitator deleteEDCFacilitator,
			AspectRelationshipMapper aspectRelationshipMapper) {
		this.assetFactory = assetFactory;
		this.edcGateway = edcGateway;
		this.policyFactory = policyFactory;
		this.contractFactory = contractFactory;
		this.policyConstraintBuilderService = policyConstraintBuilderService;
		this.aspectRelationshipService = aspectRelationshipService;
		this.deleteEDCFacilitator = deleteEDCFacilitator;
		this.aspectRelationshipMapper = aspectRelationshipMapper;
	}
	
	@SneakyThrows
	public AspectRelationship run(String submodel, AspectRelationship input, String processId) {
		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();

		try {

			AssetEntryRequest assetEntryRequest = assetFactory.getAssetRequest(submodel, ASSET_PROP_NAME_ASPECT_RELATIONSHIP,
					shellId, subModelId, input.getParentUuid());
			if (!edcGateway.assetExistsLookup(assetEntryRequest.getAsset().getProperties().get("asset:prop:id"))) {

				edcProcessingforAspectRelationship(assetEntryRequest, input);

			} else {

				deleteEDCFirstForUpdate(submodel, input, processId);
				edcProcessingforAspectRelationship(assetEntryRequest, input);
				input.setUpdated(UPDATED_Y);
			}

			return input;
		} catch (Exception e) {
			throw new CsvHandlerUseCaseException(input.getRowNumber(), "EDC: " + e.getMessage());
		}
	}
	
	@SneakyThrows
	private void deleteEDCFirstForUpdate(String submodel, AspectRelationship input, String processId) {
		AspectRelationshipResponse aspectRelationshipResponse=aspectRelationshipMapper.mapforResponse(aspectRelationshipService.readCreatedTwinsDetails(input.getParentUuid()));
		/* IMP NOTE: THis will delete only First Occurance at EDC */
		if(Optional.ofNullable(aspectRelationshipResponse).isPresent() && !aspectRelationshipResponse.getChildParts().isEmpty()) {
			ChildPart childPart=aspectRelationshipResponse.getChildParts().get(0);
			deleteEDCFacilitator.deleteContractDefination(childPart.getContractDefinationId());

			deleteEDCFacilitator.deleteAccessPolicy(childPart.getAccessPolicyId());

			deleteEDCFacilitator.deleteUsagePolicy(childPart.getUsagePolicyId());

			deleteEDCFacilitator.deleteAssets(childPart.getAssetId());
		}
	}
	
	@SneakyThrows
	private void edcProcessingforAspectRelationship(AssetEntryRequest assetEntryRequest, AspectRelationship input) {
		HashMap<String, String> extensibleProperties = new HashMap<>();

		String shellId = input.getShellId();
		String subModelId = input.getSubModelId();
		edcGateway.createAsset(assetEntryRequest);

		List<ConstraintRequest> usageConstraints = policyConstraintBuilderService
				.getUsagePolicyConstraints(input.getUsagePolicies());
		List<ConstraintRequest> accessConstraints = policyConstraintBuilderService
				.getAccessConstraints(input.getBpnNumbers());

		String customValue = getCustomValue(input);
		if (StringUtils.isNotBlank(customValue)) {
			extensibleProperties.put(UsagePolicyEnum.CUSTOM.name(), customValue);
		}

		PolicyDefinitionRequest accessPolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId,
				accessConstraints, new HashMap<>());
		PolicyDefinitionRequest usagePolicyDefinitionRequest = policyFactory.getPolicy(shellId, subModelId,
				usageConstraints, extensibleProperties);

		edcGateway.createPolicyDefinition(accessPolicyDefinitionRequest);

		edcGateway.createPolicyDefinition(usagePolicyDefinitionRequest);

		ContractDefinitionRequest contractDefinitionRequest = contractFactory.getContractDefinitionRequest(
				assetEntryRequest.getAsset().getProperties().get("asset:prop:id"),
				accessPolicyDefinitionRequest.getId(), usagePolicyDefinitionRequest.getId());

		edcGateway.createContractDefinition(contractDefinitionRequest);

		// EDC transaction information for DB
		input.setAssetId(assetEntryRequest.getAsset().getProperties().get("asset:prop:id"));
		input.setAccessPolicyId(accessPolicyDefinitionRequest.getId());
		input.setUsagePolicyId(usagePolicyDefinitionRequest.getId());
		input.setContractDefinationId(contractDefinitionRequest.getId());
	}


	private String getCustomValue(AspectRelationship input) {
		if (!CollectionUtils.isEmpty(input.getUsagePolicies())) {
			return input.getUsagePolicies().stream().filter(policy -> policy.getType().equals(UsagePolicyEnum.CUSTOM))
					.map(value -> value.getValue()).findFirst().orElse(null);
		}
		return null;
	}
}
