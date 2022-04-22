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
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.usecases.common.UUIdGenerator;
import com.catenax.dft.usecases.logs.FailureLogUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GenerateUuIdCsvHandlerUseCaseTest {

    @Mock
    private FailureLogUseCase logUseCase;
    @Mock
    private DigitalTwinsAspectCsvHandlerUseCase digitalTwinUseCase;
    @InjectMocks
    private GenerateUuIdCsvHandlerUseCase useCase;

    @Test
    @DisplayName("Testing with null input Aspect")
    public void run_nullAspect_shouldStoreFailureLog() {
        // Arrange
        final String processId = "processId";

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
        final String processId = "processId";

        // Act
        useCase.run(null, processId);

        // Assert
        verify(digitalTwinUseCase, never()).run(any(), anyString());
    }

    @Test
    public void run_nullProcessId_shouldStoreFailureLog() {
        // Arrange
        Aspect aspect = Aspect.builder().build();

        // Act
        useCase.run(aspect, null);

        // Assert
        verify(logUseCase, only()).saveLog(argThat((FailureLogEntity entity) ->
                "ProcessId cannot be null".equals(entity.getLog()) && entity.getProcessId() == null)
        );
    }

    @Test
    public void run_nullProcessIdWithNextUseCaseSet_cannotCallNextUseCase() {
        // Arrange
        Aspect aspect = Aspect.builder().build();

        // Act
        useCase.run(aspect, null);

        // Assert
        verify(digitalTwinUseCase, never()).run(any(), anyString());
    }

    @Test
    public void run_aspectWithNullUuid_generateNewUuid() {
        // Arrange
        Aspect aspect = Aspect.builder().build();
        final String processId = "processId";

        // Act
        useCase.run(aspect, processId);

        // Assert
        verify(digitalTwinUseCase, only()).run(
                argThat((Aspect a) -> a.getUuid().startsWith(UUIdGenerator.URN_UUID_PREFIX)),
                eq("processId")
        );
    }

    @Test
    public void run_aspectWithBlankUuid_generateNewUuid() {
        // Arrange
        Aspect aspect = Aspect.builder()
                .uuid("")
                .build();
        final String processId = "processId";

        // Act
        useCase.run(aspect, processId);

        // Assert
        verify(digitalTwinUseCase, only()).run(
                argThat((Aspect a) -> a.getUuid().startsWith(UUIdGenerator.URN_UUID_PREFIX)),
                eq("processId")
        );
    }

    @Test
    public void run_aspectWithUuid_shouldNotGenerateUuid() {
        // Arrange
        Aspect aspect = Aspect.builder()
                .uuid("uuid")
                .build();
        final String processId = "processId";

        // Act
        useCase.run(aspect, processId);

        // Assert
        verify(digitalTwinUseCase, only()).run(
                argThat((Aspect a) -> "uuid".equals(a.getUuid())),
                eq("processId")
        );
    }
}