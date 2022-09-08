/*
 * Copyright 2022 CatenaX
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.catenax.dft.controllers;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catenax.dft.entities.SubmodelJsonRequest;
import com.catenax.dft.entities.batch.BatchRequest;
import com.catenax.dft.entities.batch.BatchResponse;
import com.catenax.dft.usecases.batchs.GetBatchsUseCase;
import com.catenax.dft.usecases.csvhandler.batchs.CreateBatchsUseCase;

@RestController
@RequestMapping("batch")
public class BatchController {

    private final GetBatchsUseCase batchsUseCase;
    private final CreateBatchsUseCase createBatchsUseCase;

	public BatchController( GetBatchsUseCase batchsUseCase, 
							CreateBatchsUseCase createBatchsUseCase) {
		this.batchsUseCase = batchsUseCase;
		this.createBatchsUseCase = createBatchsUseCase;
	}

    @GetMapping(value="/public/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<BatchResponse> getBatch(@PathVariable("id") String uuid) {
        BatchResponse response = batchsUseCase.execute(uuid);

        if (response == null) {
            return notFound().build();
        }
        return ok().body(response);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<String> createAspectBatch(@RequestBody SubmodelJsonRequest<BatchRequest> batchAspects){
        String processId = UUID.randomUUID().toString();

        Runnable runnable = () -> createBatchsUseCase.createBatchs(batchAspects, processId);
        new Thread(runnable).start();

        return ok().body(processId);
    }
}