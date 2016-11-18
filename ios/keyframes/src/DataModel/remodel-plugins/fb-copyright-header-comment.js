"use strict";
var HEADER_COMMENTS = [
    { content: '/**\n * Copyright (c) 2016, Facebook, Inc.\n * All rights reserved.\n *\n * This source code is licensed under the license found in the LICENSE file in\n * the root directory of this source tree.\n *\n */' }
];
function createPlugin() {
    return {
        additionalFiles: function (valueType) {
            return [];
        },
        additionalTypes: function (valueType) {
            return [];
        },
        attributes: function (valueType) {
            return [];
        },
        fileTransformation: function (request) {
            return request;
        },
        fileType: function (valueType) {
            return {value: null};
        },
        forwardDeclarations: function (valueType) {
            return [];
        },
        functions: function (valueType) {
            return [];
        },
        headerComments: function (valueType) {
            return HEADER_COMMENTS;
        },
        implementedProtocols: function (valueType) {
            return [];
        },
        imports: function (valueType) {
            return [];
        },
        instanceMethods: function (valueType) {
            return [];
        },
        properties: function (valueType) {
            return [];
        },
        requiredIncludesToRun: [],
        staticConstants: function (valueType) {
            return [];
        },
        validationErrors: function (valueType) {
            return [];
        }
    };
}
exports.createPlugin = createPlugin;
function createAlgebraicTypePlugin() {
    return {
        additionalFiles: function (algebraicType) {
            return [];
        },
        blockTypes: function (algebraicType) {
            return [];
        },
        classMethods: function (algebraicType) {
            return [];
        },
        enumerations: function (algebraicType) {
            return [];
        },
        fileTransformation: function (request) {
            return request;
        },
        fileType: function (algebraicType) {
            return Maybe.Nothing();
        },
        forwardDeclarations: function (algebraicType) {
            return [];
        },
        functions: function (algebraicType) {
            return [];
        },
        headerComments: function (algebraicType) {
            return HEADER_COMMENTS;
        },
        implementedProtocols: function (algebraicType) {
            return [];
        },
        imports: function (algebraicType) {
            return [];
        },
        instanceMethods: function (algebraicType) {
            return [];
        },
        internalProperties: function (algebraicType) {
            return [];
        },
        requiredIncludesToRun: [],
        staticConstants: function (algebraicType) {
            return [];
        },
        validationErrors: function (algebraicType) {
            return [];
        }
    };
}
exports.createAlgebraicTypePlugin = createAlgebraicTypePlugin;
