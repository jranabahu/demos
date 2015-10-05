/*
 * Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.registry.samples.handler;

import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;
import org.wso2.carbon.governance.api.util.GovernanceUtils;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;
import org.wso2.carbon.registry.samples.handler.utils.CommonUtil;

public class MediationAttributeMapperHandler extends Handler {

    public Resource get(RequestContext requestContext) throws RegistryException {
        if (!CommonUtil.isUpdateLockAvailable()) {
            return requestContext.getResource();
        }
        CommonUtil.acquireUpdateLock();
        try {
            return setAttributes(requestContext);
        } finally {
            CommonUtil.releaseUpdateLock();
        }
    }

    private Resource setAttributes(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        Registry systemRegistry = requestContext.getSystemRegistry();

        Registry governanceUserRegistry = GovernanceUtils.getGovernanceSystemRegistry(systemRegistry);
        GenericArtifactManager genericArtifactManager = new GenericArtifactManager(governanceUserRegistry,
                                                                                   "mediations");

        GenericArtifact artifact = genericArtifactManager.getGenericArtifact(resource.getUUID());
        if (artifact != null) {
            if (resource.getProperties().get("overview_name") == null) {
                resource.addProperty("overview_name", artifact.getAttribute("overview_name"));
                resource.addProperty("overview_version", artifact.getAttribute("overview_version"));
                resource.addProperty("definition_direction", artifact.getAttribute("definition_direction"));
                resource.addProperty("projectDetails_projectName", artifact.getAttribute("projectDetails_projectName"));
            }
        }
        return resource;
    }

    @Override
    public void put(RequestContext requestContext) throws RegistryException {
        requestContext.setResource(setAttributes(requestContext));
    }
}
