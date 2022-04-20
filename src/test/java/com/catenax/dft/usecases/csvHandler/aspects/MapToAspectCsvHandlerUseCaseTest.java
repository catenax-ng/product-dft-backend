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

import com.catenax.dft.entities.csv.RowData;
import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.usecases.csvHandler.exceptions.CsvHandlerUseCaseException;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


class MapToAspectCsvHandlerUseCaseTest {


    MapToAspectCsvHandlerUseCase mapToAspect = new MapToAspectCsvHandlerUseCase(null);
    String processId = "processIdTest";
    Aspect aspect0;

    @BeforeEach
    void setUp() {
        aspect0 = Aspect.builder()
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
    }

    @Test
    @DisplayName("Testing valid row with empty optional identifiers")
    void executeValidUseCase() {
        RowData rowData0 = new RowData(0, "urn:uuid:a848f840-b73b-4da8-8ae4-0c1bb8d72f67;" +
                "NO-479638186238569445662893;2022-02-04T14:48:54;DEU;1O222E8-43;1O222E8-43;component;Clutch;Clutch;;");

        assertEquals(aspect0, mapToAspect.executeUseCase(rowData0, processId));
    }

    @Test
    @DisplayName("Testing valid row with optional identifiers")
    void executeValidUseCaseWithIdentifiers() {
        RowData rowData0 = new RowData(0, "urn:uuid:a848f840-b73b-4da8-8ae4-0c1bb8d72f67;" +
                "NO-479638186238569445662893;2022-02-04T14:48:54;DEU;1O222E8-43;1O222E8-43;component;Clutch;Clutch;VAN;someValue");

        aspect0.setOptionalIdentifierKey("VAN");
        aspect0.setOptionalIdentifierValue("someValue");

        assertEquals(aspect0, mapToAspect.executeUseCase(rowData0, processId));
    }


    @Test
    @DisplayName("Testing Size Validation ")
    void executeInvalidSizeUseCase() {

        RowData rowData0 = new RowData(0, ";;;;;;;;;;;;;");
        RowData rowData1 = new RowData(0, ";;;;;");

        CsvHandlerUseCaseException tooBigException = Assertions.assertThrows(CsvHandlerUseCaseException.class,
                () -> {
            mapToAspect.executeUseCase(rowData0, processId);
                }, "Expected to throw exception but it didn't");
        assertTrue(tooBigException.getMessage().contains("This row has the wrong amount of fields"));

        CsvHandlerUseCaseException tooSmallException = Assertions.assertThrows(CsvHandlerUseCaseException.class,
                () -> {
                    mapToAspect.executeUseCase(rowData1, processId);
                }, "Expected to throw exception but it didn't");
        assertTrue(tooSmallException.getMessage().contains("This row has the wrong amount of fields"));
    }

    @Test
    @DisplayName("Testing blank fields validation")
    void executeInvalidBlankFieldsUseCase() {

        RowData rowData = new RowData(0, ";;;;;;;;;;");

        Exception thrown = Assertions.assertThrows(Exception.class, () -> {
            mapToAspect.executeUseCase(rowData, processId);
        }, "Expected to throw exception but it didn't");
        String message = thrown.getMessage();
        assertTrue(message.contains("part_instance_id cannot be empty")
        && message.contains("manufacturing_date cannot be empty")
        && message.contains("manufacturing_part_id cannot be empty")
        && message.contains("classification cannot be empty")
        && message.contains("name_at_manufacturer cannot be empty"));
    }
}