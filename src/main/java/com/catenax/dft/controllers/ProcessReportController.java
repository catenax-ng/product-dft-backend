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
 */

package com.catenax.dft.controllers;

import com.catenax.dft.entities.usecases.ProcessReport;
import com.catenax.dft.entities.usecases.ProcessReportPageResponse;
import com.catenax.dft.usecases.processreport.ProcessReportUseCase;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("processing-report")
public class ProcessReportController {

    private final ProcessReportUseCase processReportUseCase;

    public ProcessReportController(ProcessReportUseCase processReportUseCase) {
        this.processReportUseCase = processReportUseCase;
    }

    @GetMapping(produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcessReportPageResponse> getProcessingReportsByDateDesc(@Param("page") Integer page, @Param("pageSize") Integer pageSize) {
        page = page == null ? 0 : page;
        pageSize = pageSize == null ? 10 : pageSize;

        return ok().body(processReportUseCase.listAllProcessReports(page, pageSize));
    }

    @GetMapping(value = "/{id}", produces = APPLICATION_JSON_VALUE)
    public ResponseEntity<ProcessReport> getProcessReportById(@PathVariable("id") String id) {
        ProcessReport processReportById = processReportUseCase.getProcessReportById(id);
        if (processReportById == null) {
            return notFound().build();
        }
        return ok().body(processReportById);
    }
}
