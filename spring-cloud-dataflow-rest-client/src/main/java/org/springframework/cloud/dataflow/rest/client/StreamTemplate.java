/*
 * Copyright 2015-2016 the original author or authors.
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

package org.springframework.cloud.dataflow.rest.client;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.dataflow.rest.UpdateStreamRequest;
import org.springframework.cloud.dataflow.rest.resource.StreamDefinitionResource;
import org.springframework.cloud.skipper.domain.PackageIdentifier;
import org.springframework.cloud.skipper.domain.Release;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.ResourceSupport;
import org.springframework.http.HttpMethod;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Implementation for {@link StreamOperations}.
 *
 * @author Ilayaperumal Gopinathan
 * @author Mark Fisher
 * @author Eric Bottard
 */
public class StreamTemplate implements StreamOperations {

	public static final String DEFINITIONS_REL = "streams/definitions";

	private static final String DEFINITION_REL = "streams/definitions/definition";

	private static final String DEPLOYMENTS_REL = "streams/deployments";

	private static final String DEPLOYMENT_REL = "streams/deployments/deployment";

	private final RestTemplate restTemplate;

	private final Link definitionsLink;

	private final Link definitionLink;

	private final Link deploymentsLink;

	private final Link deploymentLink;

	StreamTemplate(RestTemplate restTemplate, ResourceSupport resources) {
		Assert.notNull(resources, "URI Resources can't be null");
		Assert.notNull(resources.getLink(DEFINITIONS_REL), "Definitions relation is required");
		Assert.notNull(resources.getLink(DEFINITION_REL), "Definition relation is required");
		Assert.notNull(resources.getLink(DEPLOYMENTS_REL), "Deployments relation is required");
		Assert.notNull(resources.getLink(DEPLOYMENT_REL), "Deployment relation is required");
		this.restTemplate = restTemplate;
		this.definitionsLink = resources.getLink(DEFINITIONS_REL);
		this.deploymentsLink = resources.getLink(DEPLOYMENTS_REL);
		this.definitionLink = resources.getLink(DEFINITION_REL);
		this.deploymentLink = resources.getLink(DEPLOYMENT_REL);
	}

	@Override
	public StreamDefinitionResource.Page list() {
		String uriTemplate = definitionsLink.expand().getHref();
		uriTemplate = uriTemplate + "?size=2000";
		return restTemplate.getForObject(uriTemplate, StreamDefinitionResource.Page.class);
	}

	@Override
	public StreamDefinitionResource createStream(String name, String definition, boolean deploy) {
		MultiValueMap<String, Object> values = new LinkedMultiValueMap<>();
		values.add("name", name);
		values.add("definition", definition);
		values.add("deploy", Boolean.toString(deploy));
		StreamDefinitionResource stream = restTemplate.postForObject(definitionsLink.expand().getHref(), values,
				StreamDefinitionResource.class);
		return stream;
	}

	@Override
	public void deploy(String name, Map<String, String> properties) {
		restTemplate.postForObject(deploymentLink.expand(name).getHref(), properties, Object.class);
	}

	@Override
	public void undeploy(String name) {
		restTemplate.delete(deploymentLink.expand(name).getHref());
	}

	@Override
	public void undeployAll() {
		restTemplate.delete(deploymentsLink.getHref());
	}

	@Override
	public void destroy(String name) {
		restTemplate.delete(definitionLink.expand(name).getHref());
	}

	@Override
	public void destroyAll() {
		restTemplate.delete(definitionsLink.getHref());
	}

	@Override
	public void updateStream(String streamName, String releaseName, PackageIdentifier packageIdentifier,
			Map<String, String> updateProperties) {
		Assert.hasText(streamName, "Stream name cannot be null or empty");
		Assert.notNull(packageIdentifier, "PackageIdentifier cannot be null");
		Assert.hasText(packageIdentifier.getPackageName(), "Package Name cannot be null or empty");
		Assert.hasText(releaseName, "Release name cannot be null or empty");
		Assert.notNull(updateProperties, "UpdateProperties cannot be null");
		UpdateStreamRequest updateStreamRequest = new UpdateStreamRequest(releaseName, packageIdentifier,
				updateProperties);
		String url = deploymentsLink.getHref() + "/update/" + streamName;
		restTemplate.postForObject(url, updateStreamRequest, Object.class);
	}

	@Override
	public void rollbackStream(String streamName, int version) {
		Assert.hasText(streamName, "Release name cannot be null or empty");
		String url = deploymentsLink.getHref() + "/rollback/" + streamName + "/" + version;
		restTemplate.postForObject(url, null, Object.class);
	}

	@Override
	public String getManifest(String streamName, int version) {
		Assert.hasText(streamName, "Release name cannot be null or empty");
		String url = url = deploymentsLink.getHref() + "/manifest/" + streamName;;
		if (version >= 1) {
			url = url + "/" + version;
		}
		return restTemplate.getForObject(url, String.class);
	}

	@Override
	public Collection<Release> history(String streamName, int maxRevisions) {
		Assert.hasText(streamName, "Release name cannot be null or empty");
		ParameterizedTypeReference<Collection<Release>> typeReference = new ParameterizedTypeReference<Collection<Release>>
				() {
		};
		Map<String, Object> parameters = new HashMap<>();
		String url = String.format("%s/%s/%s", deploymentsLink.getHref(), "history", streamName);
		if (maxRevisions >= 1) {
			url = url + "/" + maxRevisions;
		}
		return this.restTemplate.exchange(url, HttpMethod.GET, null, typeReference, parameters).getBody();
	}

	@Override
	public StreamDefinitionResource getStreamDefinition(String streamName) {
		String uriTemplate = this.definitionLink.expand(streamName).getHref();
		return restTemplate.getForObject(uriTemplate, StreamDefinitionResource.class);
	}
}
