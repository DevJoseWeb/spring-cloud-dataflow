/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.cloud.dataflow.registry.domain;

import javax.persistence.*;

import org.springframework.hateoas.Identifiable;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Base class for entity implementations. Uses a {@link Long} id.
 *
 * @author Oliver Gierke
 * @author Gunnar Hillert
 */
@MappedSuperclass
public class AbstractEntity implements Identifiable<Long> {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	private final Long id;

	@Version
	@JsonIgnore
	private Long objectVersion;

	protected AbstractEntity() {
		this.id = null;
	}

	@Override
	public Long getId() {
		return this.id;
	}

	public Long getObjectVersion() {
		return objectVersion;
	}

}