/********************************************************************************
 * Copyright (c) 2022 Critical TechWorks GmbH
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

package com.catenax.dft.usecases.csvhandler.aspects;

import com.catenax.dft.entities.usecases.Aspect;
import com.catenax.dft.usecases.common.UUIdGenerator;
import com.catenax.dft.usecases.csvhandler.AbstractCsvHandlerUseCase;
import org.springframework.stereotype.Service;

@Service
public class GenerateAspectUuIdCsvHandlerUseCase extends AbstractCsvHandlerUseCase<Aspect, Aspect> {

    public GenerateAspectUuIdCsvHandlerUseCase(DigitalTwinsAspectCsvHandlerUseCase nextUseCase) {
        super(nextUseCase);
    }

    @Override
    protected Aspect executeUseCase(Aspect input, String processId) {
        if (input.getUuid() == null || input.getUuid().isBlank()) {
            input.setUuid(UUIdGenerator.getUrnUuid());
        }
        return input;
    }
}