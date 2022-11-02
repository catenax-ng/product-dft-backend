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

package com.catenax.sde.entities.database;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name = "aspect")
@Entity
@Data
public class AspectEntity implements Serializable {

    @Id
    @Column(name = "uuid")
    private String uuid;
    @Column(name = "process_id")
    private String processId;
    @Column(name = "part_instance_id")
    private String partInstanceId;
    @Column(name = "manufacturing_date")
    private String manufacturingDate;
    @Column(name = "manufacturing_country")
    private String manufacturingCountry;
    @Column(name = "manufacturer_part_id")
    private String manufacturerPartId;
    @Column(name = "customer_part_id")
    private String customerPartId;
    @Column(name = "classification")
    private String classification;
    @Column(name = "name_at_manufacturer")
    private String nameAtManufacturer;
    @Column(name = "name_at_customer")
    private String nameAtCustomer;
    @Column(name = "optional_identifier_key")
    private String optionalIdentifierKey;
    @Column(name = "optional_identifier_value")
    private String optionalIdentifierValue;
    @Column(name = "shell_id")
    private String shellId;
    @Column(name = "contract_defination_id")
    private String contractDefinationId;
    @Column(name = "usage_policy_id")
    private String usagePolicyId;
    @Column(name = "access_policy_id")
    private String accessPolicyId;
    @Column(name = "asset_id")
    private String assetId;
}