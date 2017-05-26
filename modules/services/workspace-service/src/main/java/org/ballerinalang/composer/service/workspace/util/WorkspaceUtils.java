/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.composer.service.workspace.util;

import org.ballerinalang.composer.service.workspace.model.Action;
import org.ballerinalang.composer.service.workspace.model.Annotation;
import org.ballerinalang.composer.service.workspace.model.Connector;
import org.ballerinalang.composer.service.workspace.model.Function;
import org.ballerinalang.composer.service.workspace.model.ModelPackage;
import org.ballerinalang.composer.service.workspace.model.Parameter;
import org.ballerinalang.model.BallerinaFunction;
import org.ballerinalang.model.SymbolName;
import org.ballerinalang.model.symbols.BLangSymbol;
import org.ballerinalang.model.types.SimpleTypeName;
import org.ballerinalang.natives.AbstractNativeFunction;
import org.ballerinalang.natives.NativePackageProxy;
import org.ballerinalang.natives.NativeUnitProxy;
import org.ballerinalang.natives.connectors.AbstractNativeAction;
import org.ballerinalang.natives.connectors.AbstractNativeConnector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Utility methods for workspace service
 */
public class WorkspaceUtils {

    /**
     * Get All Native Packages for a given symbolMap
     * @return {Map} <Package name, package functions and connectors>
     * */
    public static Map<String, ModelPackage> getAllPackages(Map<SymbolName, BLangSymbol> symbolMap) {
        Map<String, ModelPackage> packages = new HashMap<>();
        symbolMap.values().stream().forEach(symbol -> {
            if (symbol instanceof NativePackageProxy) {
                ((NativePackageProxy) symbol).load().getSymbolMap().values().stream().forEach(bLangSymbol -> {
                    NativeUnitProxy nativeUnitProxy = (NativeUnitProxy) bLangSymbol;
                    if (nativeUnitProxy.load() instanceof AbstractNativeFunction) {
                        extractFunction(packages, nativeUnitProxy);
                    } else if (nativeUnitProxy.load() instanceof AbstractNativeConnector) {
                        extractConnector(packages, nativeUnitProxy);
                    }
                });
            } else if (symbol instanceof BallerinaFunction) {
                extractFunction(packages, (BallerinaFunction) symbol);
            }
        });
        return packages;
    }

    /**
     * Extract connectors from ballerina lang
     * @param packages packages to send
     * @param bLangSymbol Native unit of symbol
     * */
    private static void extractConnector(Map<String, ModelPackage> packages, NativeUnitProxy bLangSymbol) {
        AbstractNativeConnector abstractNativeConnector = (AbstractNativeConnector) bLangSymbol.load();
        if (packages.containsKey(abstractNativeConnector.getPackagePath())) {
            ModelPackage modelPackage = packages.get(abstractNativeConnector.getPackagePath());
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, abstractNativeConnector.getArgumentTypeNames());

            List<Parameter> returnParameters = new ArrayList<>();
            addParameters(returnParameters, abstractNativeConnector.getReturnParamTypeNames());

            List<Annotation> annotations = new ArrayList<>();
            List<Action> actions = new ArrayList<>();
            addActions(actions, abstractNativeConnector.getActions());

            modelPackage.addConnectorsItem(createNewConnector(abstractNativeConnector.getName(),
                    annotations, actions, parameters, returnParameters));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(abstractNativeConnector.getPackagePath());

            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, abstractNativeConnector.getArgumentTypeNames());

            List<Parameter> returnParameters = new ArrayList<>();
            addParameters(returnParameters, abstractNativeConnector.getReturnParamTypeNames());

            List<Annotation> annotations = new ArrayList<>();
            List<Action> actions = new ArrayList<>();
            addActions(actions, abstractNativeConnector.getActions());

            modelPackage.addConnectorsItem(createNewConnector(abstractNativeConnector.getName(),
                    annotations, actions, parameters, returnParameters));
            packages.put(abstractNativeConnector.getPackagePath(), modelPackage);
        }
    }

    /**
     * Extract Functions from ballerina lang.
     * @param packages packages to send.
     * @param bLangSymbol Native unit of symbol.
     * */
    private static void extractFunction(Map<String, ModelPackage> packages, NativeUnitProxy bLangSymbol) {
        AbstractNativeFunction abstractNativeFunction = (AbstractNativeFunction) bLangSymbol.load();
        if (packages.containsKey(abstractNativeFunction.getPackagePath())) {
            ModelPackage modelPackage = packages.get(abstractNativeFunction.getPackagePath());
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, abstractNativeFunction.getArgumentTypeNames());

            List<Parameter> returnParameters = new ArrayList<>();
            addParameters(returnParameters, abstractNativeFunction.getReturnParamTypeNames());

            List<Annotation> annotations = new ArrayList<>();
            addAnnotations(annotations, abstractNativeFunction.getAnnotations());

            modelPackage.addFunctionsItem(createNewFunction(abstractNativeFunction.getName(),
                    annotations, parameters, returnParameters));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(abstractNativeFunction.getPackagePath());
            List<Parameter> parameters = new ArrayList<>();
            addParameters(parameters, abstractNativeFunction.getArgumentTypeNames());

            List<Parameter> returnParameters = new ArrayList<>();
            addParameters(returnParameters, abstractNativeFunction.getReturnParamTypeNames());

            List<Annotation> annotations = new ArrayList<>();
            addAnnotations(annotations, abstractNativeFunction.getAnnotations());

            modelPackage.addFunctionsItem(createNewFunction(abstractNativeFunction.getName(),
                    annotations, parameters, returnParameters));
            packages.put(abstractNativeFunction.getPackagePath(), modelPackage);
        }
    }

    /**
     * Extract Functions from ballerina lang.
     * @param packages packages to send.
     * @param BallerinaFunction Native unit of symbol.
     * */
    private static void extractFunction(Map<String, ModelPackage> packages, BallerinaFunction ballerinaFunction) {
        if (packages.containsKey(ballerinaFunction.getPackagePath())) {
            ModelPackage modelPackage = packages.get(ballerinaFunction.getPackagePath());
            List<Parameter> parameters = new ArrayList<>();
            //addParameters(parameters, ballerinaFunction.getArgumentTypeNames());

            List<Parameter> returnParameters = new ArrayList<>();
            //addParameters(returnParameters, ballerinaFunction.getReturnParamTypeNames());

            List<Annotation> annotations = new ArrayList<>();
            addAnnotations(annotations, ballerinaFunction.getAnnotations());

            modelPackage.addFunctionsItem(createNewFunction(ballerinaFunction.getName(),
                    annotations, parameters, returnParameters));
        } else {
            ModelPackage modelPackage = new ModelPackage();
            modelPackage.setName(ballerinaFunction.getPackagePath());
            List<Parameter> parameters = new ArrayList<>();
            //addParameters(parameters, ballerinaFunction.getArgumentTypeNames());

            List<Parameter> returnParameters = new ArrayList<>();
            //addParameters(returnParameters, ballerinaFunction.getReturnParamTypeNames());

            List<Annotation> annotations = new ArrayList<>();
            addAnnotations(annotations, ballerinaFunction.getAnnotations());

            modelPackage.addFunctionsItem(createNewFunction(ballerinaFunction.getName(),
                    annotations, parameters, returnParameters));
            packages.put(ballerinaFunction.getPackagePath(), modelPackage);
        }
    }

    /**
     * Add parameters to a list from ballerina lang param list.
     * @param params params to send.
     * @param argumentTypeNames argument types from native symbol
     * */
    private static void addParameters(List<Parameter> params, SimpleTypeName[] argumentTypeNames) {
        Stream.of(argumentTypeNames)
                .forEach(item -> params.add(createNewParameter(item.getName(), item.getSymbolName().getName())));
    }

    /**
     * Add annotations to a list from ballerina lang annotation list
     * @param annotations annotations list to be sent
     * @param annotationsFromModel annotation retrieve from native symbol
     * */
    private static void addAnnotations(List<Annotation> annotations,
                                org.ballerinalang.model.AnnotationAttachment[] annotationsFromModel) {
        Stream.of(annotationsFromModel)
                .forEach(annotation -> annotations.add(createNewAnnotation(annotation.getName(),
                        annotation.getValue())));
    }

    /**
     * Add Actions to the connector.
     * @param actions action list to be sent
     * @param nativeActions native actions retrieve from the connector
     * */
    private static void addActions(List<Action> actions, NativeUnitProxy[] nativeActions) {
        Stream.of(nativeActions)
                .forEach(nativeUnitProxy -> actions.add(extractAction(nativeUnitProxy)));
    }

    /**
     * Extract action details from a connector.
     * @param nativeUnitProxy Native unit proxy.
     * @return {Action} action
     * */
    private static Action extractAction(NativeUnitProxy nativeUnitProxy) {
        AbstractNativeAction action = (AbstractNativeAction) nativeUnitProxy.load();
        List<Parameter> parameters = new ArrayList<>();
        addParameters(parameters, action.getArgumentTypeNames());
        List<Annotation> annotations = new ArrayList<>();
        addAnnotations(annotations, action.getAnnotations());
        List<Parameter> returnParameters = new ArrayList<>();
        addParameters(returnParameters, action.getReturnParamTypeNames());
        return createNewAction(action.getName(), parameters, returnParameters, annotations);
    }

    /**
     * Create new action
     * @param name action name
     * @param params list of params
     * @param returnParams list of return params
     * @param annotations list of annotations
     * @return {Action} action
     * */
    private static Action createNewAction(String name, List<Parameter> params, List<Parameter> returnParams,
                                   List<Annotation> annotations) {
        Action action = new Action();
        action.setName(name);
        action.setParameters(params);
        action.setReturnParams(returnParams);
        action.setAnnotations(annotations);
        return action;
    }

    /**
     * Create new parameter
     * @param type parameter type
     * @param name parameter name
     * @return {Parameter} parameter
     * */
    private static Parameter createNewParameter(String type, String name) {
        Parameter parameter = new Parameter();
        parameter.setType(type);
        parameter.setName(name);
        return parameter;
    }

    /**
     * Create new annotations
     * @param name annotation name
     * @param value annotation value
     * @return {Annotation} annotation
     * */
    private static Annotation createNewAnnotation(String name, String value) {
        Annotation annotation = new Annotation();
        annotation.setName(name);
        annotation.setValue(value);
        return annotation;
    }

    /**
     * Create new function
     * @param name name of the function
     * @param annotations list of annotations
     * @param params list of parameters
     * @param returnParams list of return params
     * @return {Function} function
     * */
    private static Function createNewFunction(String name, List<Annotation> annotations, List<Parameter> params,
                                       List<Parameter> returnParams) {
        Function function = new Function();
        function.setName(name);
        function.setAnnotations(annotations);
        function.setParameters(params);
        function.setReturnParams(returnParams);
        return function;
    }

    /**
     * Create new connector
     * @param name name of the connector
     * @param annotations list of annotation
     * @param actions list of actions
     * @param params list of params
     * @param returnParams list of return params
     * @return {Connector} connector
     * */
    private static Connector createNewConnector(String name, List<Annotation> annotations, List<Action> actions,
                                                List<Parameter> params, List<Parameter> returnParams) {
        Connector connector = new Connector();
        connector.setName(name);
        connector.setActions(actions);
        connector.setParameters(params);
        connector.setAnnotations(annotations);
        connector.setReturnParameters(returnParams);
        return connector;
    }
}
