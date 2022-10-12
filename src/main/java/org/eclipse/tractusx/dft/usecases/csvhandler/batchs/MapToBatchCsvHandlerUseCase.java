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

import static org.eclipse.tractusx.dft.gateways.file.CsvGateway.SEPARATOR;

import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.eclipse.tractusx.dft.entities.SubmodelFileRequest;
import org.eclipse.tractusx.dft.entities.csv.RowData;
import org.eclipse.tractusx.dft.entities.usecases.Batch;
import org.eclipse.tractusx.dft.usecases.common.DftDateValidator;
import org.eclipse.tractusx.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import org.eclipse.tractusx.dft.usecases.csvhandler.CsvHandlerOrchestrator;
import org.eclipse.tractusx.dft.usecases.csvhandler.exceptions.CsvHandlerUseCaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.SneakyThrows;

@Service
public class MapToBatchCsvHandlerUseCase extends AbstractCsvHandlerUseCase<RowData, Batch> {

	private SubmodelFileRequest submodelFileRequest;
	
	@Autowired
	private DftDateValidator dftDateValidator;
	
    public MapToBatchCsvHandlerUseCase(GenerateBatchUuIdCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }
    
    public void init(SubmodelFileRequest submodelFileRequest) {
		this.submodelFileRequest = submodelFileRequest;
	}

    @SneakyThrows
    public Batch executeUseCase(RowData rowData, String processId) {
        String[] rowDataFields = rowData.content().split(SEPARATOR, -1);
        if (rowDataFields.length != CsvHandlerOrchestrator.getBatchColumnSize()) {
            throw new CsvHandlerUseCaseException(rowData.position(), "This row has the wrong amount of fields");
        }

        Batch batch = Batch.builder()
                .rowNumber(rowData.position())
                .uuid(rowDataFields[0].trim())
                .processId(processId)
                .batchId(rowDataFields[1].trim())
                .manufacturingDate(dftDateValidator.getIfValidDateTime(rowDataFields[2].trim(),rowData.position()))
                .manufacturingCountry(rowDataFields[3].trim().isBlank() ? null : rowDataFields[3])
                .manufacturerPartId(rowDataFields[4].trim())
                .customerPartId(rowDataFields[5].trim().isBlank() ? null : rowDataFields[5])
                .classification(rowDataFields[6].trim())
                .nameAtManufacturer(rowDataFields[7].trim())
                .nameAtCustomer(rowDataFields[8].trim().isBlank() ? null : rowDataFields[8])
                .optionalIdentifierKey(rowDataFields[9].isBlank() ? null : rowDataFields[9])
                .optionalIdentifierValue(rowDataFields[10].isBlank() ? null : rowDataFields[10])
                .build();

        List<String> errorMessages = validateBatchAsset(batch);
        if (!errorMessages.isEmpty()) {
            throw new CsvHandlerUseCaseException(rowData.position(), errorMessages.toString());
        }

        batch.setBpnNumbers(this.submodelFileRequest.getBpnNumbers());
        batch.setUsagePolicies(submodelFileRequest.getUsagePolicies());
        
        return batch;
    }

    private List<String> validateBatchAsset(Batch batch) {
        Validator validator = Validation.buildDefaultValidatorFactory()
                .getValidator();
        Set<ConstraintViolation<Batch>> violations = validator.validate(batch);

        return violations.stream()
                .map(ConstraintViolation::getMessage)
                .sorted().toList();
    }

	
}