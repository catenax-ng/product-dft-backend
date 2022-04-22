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

import com.catenax.dft.entities.database.FailureLogEntity;
import com.catenax.dft.entities.edc.request.asset.AssetEntryRequest;
import com.catenax.dft.entities.edc.request.contractDefinition.CreateContractDefinitionRequest;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.gateways.external.EDCGateway;
import com.catenax.dft.mapper.AssetEntryRequestMapper;
import com.catenax.dft.usecases.logs.FailureLogUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EDCAspectHandlerUseCaseTest {

    @Mock
    private AssetEntryRequestMapper assetMapper;
    @Mock
    private EDCGateway edcGateway;
    @Mock
    private FailureLogUseCase logUseCase;
    @Mock
    private StoreAspectCsvHandlerUseCase storeAspectCsvHandlerUseCase;

    @InjectMocks
    private EDCAspectHandlerUseCase useCase;

    private Aspect input;
    private String processId = "processId";


    @Test
    public void run_nullAspect_shouldStoreFailureLog() {
        // Arrange

        // Act
        useCase.run(null, processId);

        // Assert
        verify(logUseCase, only()).saveLog(argThat((FailureLogEntity entity) ->
                "Aspect cannot be null".equals(entity.getLog()) && processId.equals(entity.getProcessId()))
        );
    }

    @Test
    public void run_nullAspectWithNextUseCaseSet_cannotCallNextUseCase() {
        // Arrange

        // Act
        useCase.run(null, processId);

        // Assert
        verify(storeAspectCsvHandlerUseCase, never()).run(any(), anyString());
    }

    @Test
    public void run_AspectWithNullShellId_shouldStoreFailureLog() {
        // Arrange
        input = Aspect.builder().build();

        // Act
        useCase.run(input, processId);

        // Assert
        verify(logUseCase, only()).saveLog(argThat((FailureLogEntity entity) ->
                "Aspect must have a valid shellId".equals(entity.getLog())));
        verify(storeAspectCsvHandlerUseCase, never()).run(any(), anyString());
    }

    @Test
    public void run_AspectWithEmptyShellId_shouldStoreFailureLog() {
        // Arrange
        input = Aspect.builder().shellId(" ").build();

        // Act
        useCase.run(input, processId);

        // Assert
        verify(logUseCase, only()).saveLog(argThat((FailureLogEntity entity) ->
                "Aspect must have a valid shellId".equals(entity.getLog())));
        verify(storeAspectCsvHandlerUseCase, never()).run(any(), anyString());
    }

    @Test
    public void run_AspectWithNullSubModelId_shouldStoreFailureLog() {
        // Arrange
        input = Aspect.builder().shellId("shellId").build();

        // Act
        useCase.run(input, processId);

        // Assert
        verify(logUseCase, only()).saveLog(argThat((FailureLogEntity entity) ->
                "Aspect must have a valid subModelId".equals(entity.getLog())));
        verify(storeAspectCsvHandlerUseCase, never()).run(any(), anyString());
    }

    @Test
    public void run_shouldCallRequestAssetOnce() {
        // Arrange
        input = Aspect.builder().shellId("shellId").subModelId("subModelId").build();

        // Act
        useCase.run(input, processId);

        //Assert
        verify(assetMapper, only()).getAsset(input.getShellId() + "-" + input.getSubModelId());
    }

    @Test
    public void run_shouldCallCreateAssetOnce() {
        // Arrange
        input = Aspect.builder().shellId("shellId").subModelId("subModelId").build();
        when(assetMapper.getAsset(any()))
                .thenReturn(new AssetEntryRequest());

        // Act
        useCase.run(input, processId);

        // Assert
        verify(edcGateway, only()).createAsset(isA(AssetEntryRequest.class));
    }

    @Test
    public void run_EDCException_shouldStoreFailureLog(){
        // Arrange
        input = Aspect.builder().shellId("shellId").subModelId("subModelId").build();

        // Act
        useCase.run(input, processId);

        // Assert
        verify(logUseCase, only()).saveLog(argThat((FailureLogEntity entity) ->
                "Asset not created in EDC".equals(entity.getLog())));
        verify(storeAspectCsvHandlerUseCase, never()).run(any(), anyString());
    }

    @Test
    public void run_shouldCallCreateContractDefinitionOnce() {
        // Arrange
        input = Aspect.builder().shellId("shellId").subModelId("subModelId").build();
        when(edcGateway.createAsset(any()))
                .thenReturn(HttpStatus.NO_CONTENT);

        // Act
        useCase.run(input, processId);

        // Assert
        verify(edcGateway, times(1)).createContractDefinition(isA(CreateContractDefinitionRequest.class));
    }
}