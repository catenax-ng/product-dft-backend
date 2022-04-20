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

import com.catenax.dft.entities.usecases.Aspect;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GenerateUuIdCsvHandlerUseCaseTest {

    GenerateUuIdCsvHandlerUseCase generator = new GenerateUuIdCsvHandlerUseCase(null);
    String processId = "processIdTest";
    private Aspect aspectWithUuid;
    private Aspect aspectWithoutUuid;

    @BeforeEach
    void setUp() {
        aspectWithUuid = Aspect.builder()
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
                .build();
        aspectWithoutUuid = Aspect.builder()
                .rowNumber(0)
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
                .build();
    }

    @Test
    @DisplayName("Testing input Aspect with uuid")
    void executeUseCaseWithId() {
        Aspect aspect = generator.executeUseCase(aspectWithUuid, processId);
        assertEquals(aspect.getUuid(), "urn:uuid:a848f840-b73b-4da8-8ae4-0c1bb8d72f67");
    }

    @Test
    @DisplayName("Testing input Aspect without uuid")
    void executeCaseWithoutUuid() {
        Aspect aspect = generator.executeUseCase(aspectWithoutUuid, processId);
        assertNotNull(aspect.getUuid());
    }
}