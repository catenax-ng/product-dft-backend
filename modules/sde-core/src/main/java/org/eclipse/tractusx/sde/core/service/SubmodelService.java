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

package org.eclipse.tractusx.sde.core.service;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.tractusx.sde.common.exception.NoDataFoundException;
import org.eclipse.tractusx.sde.common.exception.ValidationException;
import org.eclipse.tractusx.sde.common.mapper.SubmodelMapper;
import org.eclipse.tractusx.sde.common.model.Submodel;
import org.eclipse.tractusx.sde.core.registry.SubmodelRegistration;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.csv.CsvSchema.Builder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

@Service
@AllArgsConstructor
public class SubmodelService {

	private final SubmodelRegistration submodelRegistration;

	private final SubmodelMapper submodelMapper;

	public List<Map<String, String>> findAllSubmodels() {

		List<Map<String, String>> ls = new ArrayList<>();

		submodelRegistration.getModels().forEach(obj -> {
			Map<String, String> sbBuild = new LinkedHashMap<>();
			sbBuild.put("id", obj.getId());
			sbBuild.put("name", obj.getName());
			sbBuild.put("version", obj.getVersion());
			sbBuild.put("semanticId", obj.getSemanticId());
			ls.add(sbBuild);
		});
		return ls;
	}

	
	public Map<Object, Object> findSubmodelByName(String submodelName) {
		return readValue(submodelName).map(e -> submodelMapper.jsonPojoToMap(e.getSchema()))
				.orElseThrow(() -> new NoDataFoundException("No data found for " + submodelName));
	}

	private Optional<Submodel> readValue(String submodelName) {
		return submodelRegistration.getModels().stream()
				.filter(obj -> obj.getId().equalsIgnoreCase(submodelName.toLowerCase())).findFirst();

	}

	public Submodel findSubmodelByNameAsSubmdelObject(String submodelName) {
		return readValue(submodelName).orElseThrow(() -> new ValidationException(submodelName+" submodel is not supported"));
	}
	
	@SuppressWarnings("deprecation")
	@SneakyThrows
	public File findSubmodelCsv(String submodelName, String type) {
		
		File file = new File(submodelName +".csv");
		String csvString = "";
		JsonObject schemaObject = findSubmodelByNameAsSubmdelObject(submodelName).getSchema();
		ObjectMapper objectMapper = new ObjectMapper();
		
		switch (type.toLowerCase()) {
		case "sample":
			
			JsonArray examplesArray = schemaObject.getAsJsonArray("examples");
			JsonNode jsonNode = objectMapper.readTree(examplesArray.toString());
			Builder csvSchemaBuilder = CsvSchema.builder();
			JsonNode firstObject = jsonNode.elements().next();
			firstObject.fieldNames().forEachRemaining(csvSchemaBuilder::addColumn);
			CsvSchema csvSchema = csvSchemaBuilder.build().withHeader().withColumnSeparator(';');
			CsvMapper csvMapper = new CsvMapper();
			csvMapper.writerFor(JsonNode.class)
			  .with(csvSchema)
			  .writeValue(file, jsonNode);
			break;
			
		case "template":
			
			JsonObject asJsonObject = schemaObject.get("items").getAsJsonObject().get("properties").getAsJsonObject();
			List<String> headerList = asJsonObject.keySet().stream().toList();
			csvString = StringUtils.join(headerList, ";");
			FileUtils.writeStringToFile(file, csvString);
			break;
			
		default:
			throw new ValidationException("Unknown CSV type: " + type + " for submodel: " + submodelName);
		}
		
		return file;
	}

}
