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

import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.Handler;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class MediationWorkflowCallBackHandler extends Handler {

    public static final String MEDIATION_BASEPATH = "/_system/governance/meta/mediations";
    public static final String MEDIATION_LIFECYCLE_NAME = "MediationLifeCycle";
    public static final String MEDIATION_LIFECYCLE_SUCCESS_ACTION = "Promote";
    public static final String MEDIATION_LIFECYCLE_FAILURE_ACTION = "Disapprove";

    public void put(RequestContext requestContext) throws RegistryException {
        Resource resource = requestContext.getResource();
        Resource oldResource = requestContext.getOldResource();

        Registry registry = requestContext.getRegistry();

        Properties properties = (Properties) resource.getProperties().clone();
        Properties oldResourceProperties = oldResource.getProperties();

        for (Object oldPropertyKey : oldResourceProperties.keySet()) {
            properties.remove(oldPropertyKey);
        }

        String resourcePath;
        String propName;
        ArrayList<String> propValue;
        for (Map.Entry<Object, Object> propertyEntry : properties.entrySet()) {
            propName = (String) propertyEntry.getKey();
            propValue = (ArrayList<String>)propertyEntry.getValue();

            String[] attributeValues = propName.split("_");
            // We check the length of the array. if it is more than 4 that means we cant identify the attributes
            // correctly. Hence we skip
            if(attributeValues.length != 4){
                continue;
            }
            resourcePath = MEDIATION_BASEPATH + RegistryConstants.PATH_SEPARATOR +
                           attributeValues[0] + RegistryConstants.PATH_SEPARATOR + attributeValues[1] +
                           RegistryConstants.PATH_SEPARATOR + attributeValues[0];

            if(propValue.get(0).equals("true")){
                registry.invokeAspect(resourcePath, MEDIATION_LIFECYCLE_NAME, MEDIATION_LIFECYCLE_SUCCESS_ACTION);
            }else if(propValue.get(0).equals("false")){
                registry.invokeAspect(resourcePath, MEDIATION_LIFECYCLE_NAME, MEDIATION_LIFECYCLE_FAILURE_ACTION);
            }
            resource.removeProperty(propName);
            requestContext.setResource(resource);

        }
    }
}
