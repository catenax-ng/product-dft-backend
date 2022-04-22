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

package com.catenax.dft.usecases.csvHandler.aspects;

import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.asset.AssetRequest;
import com.catenax.dft.entities.edc.request.asset.DataAddressRequest;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.external.EDCGateway;
import com.catenax.dft.mapper.AssetEntryRequestMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EDCAspectHandlerUseCaseTest {

    @Mock
    private AssetEntryRequestMapper assetMapper;
    @Mock
    private EDCGateway edcGateway;

    @InjectMocks
    private EDCAspectHandlerUseCase edcAspectHandlerUseCase;

    private Aspect input;
    private String processId = "processIdTest";

    @BeforeEach
    void setUp() {
        input = Aspect.builder()
                .rowNumber(0)
                .uuid("urn:uuid:a848f840-b73b-4da8-8ae4-0c1bb8d72f67")
                .processId("processIdTest")
                .partInstanceId("NO-479638186238569445662893")
                .manufacturingDate("2022-02-04T14:48:54")
                .manufacturingCountry("DEU")
                .manufacturerPartId("1O222E8-43")
                .customerPartId("1O222E8-43")
                .classification("component")
                .nameAtManufacturer("Clutch")
                .nameAtCustomer("Clutch")
                .optionalIdentifierKey(null)
                .optionalIdentifierValue(null)
                .shellId("someShellId")
                .subModelId("someModelId")
                .build();
    }

    @Test
    void executeUseCase() {
        HashMap<String, String> assetProperties = new HashMap<>();
        assetProperties.put("asset:prop:id", "someShellId-someModelId");
        assetProperties.put("asset:prop:contenttype", "application/json");
        assetProperties.put("asset:prop:name", "Serialized Part - Submodel SerialPartTypization");
        assetProperties.put("asset:prop:description", "...");
        assetProperties.put("asset:prop:version", "1.0.0");
        AssetRequest assetRequest = AssetRequest.builder().properties(assetProperties).build();

        HashMap<String, String> dataAddressProperties = new HashMap<>();
        dataAddressProperties.put("type", "AzureStorage");
        dataAddressProperties.put("endpoint", "http://some/endpoint");
        dataAddressProperties.put("name", "Backend Data Service - AAS Server");
        dataAddressProperties.put("authKey", "");
        dataAddressProperties.put("authCode", "");
        DataAddressRequest dataAddressRequest = DataAddressRequest.builder().properties(dataAddressProperties).build();
        AssetEntryRequest assetEntryRequest = AssetEntryRequest.builder().asset(assetRequest)
                .dataAddress(dataAddressRequest).build();

        //AssetEntryRequestMapper assetMapper = mock(AssetEntryRequestMapper.class);
        when(assetMapper.getAsset(input.getShellId() + "-" + input.getSubModelId()))
                .thenReturn(assetEntryRequest);
        //EDCGateway edcGateway = mock(EDCGateway.class);
        when(edcGateway.createAsset(assetEntryRequest))
                .thenReturn(HttpStatus.NO_CONTENT);
        //EDCAspectHandlerUseCase edcAspectHandlerUseCase = mock(EDCAspectHandlerUseCase.class);
        Aspect returnedAspect = edcAspectHandlerUseCase.executeUseCase(input, processId);
        assertEquals(input, returnedAspect);
    }
}