/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import _ from 'lodash';
import Expression from './expression';
import ASTFactory from './../ast-factory';

/**
 * Class for XML text literal.
 * @class XMLTextLiteral
 * */
class XMLTextLiteral extends Expression {
    /**
     * Class Constructor.
     * @class XMLTextLiteral
     * */
    constructor(args) {
        super('XMLTextLiteral');
        this.type_name = _.get(args, 'type_name', '');
    }

    getExpressionString(isTemplate) {
        if (isTemplate) {
            return `${this.children[0].getBasicLiteralValue()}`;
        }
        return `${this.type_name} \`${this.children[0].getBasicLiteralValue()}\``;
    }

    /**
     * Initialize from the json.
     * @param {object} jsonNode - json node for XMLTextLiteral
     * */
    initFromJson(jsonNode) {
        this.type_name = _.get(jsonNode, 'type_name', '');
        _.forEach(jsonNode.children, (childNode) => {
            const child = ASTFactory.createFromJson(childNode);
            this.addChild(child, undefined, true, true);
            child.initFromJson(childNode);
        });
    }
}

export default XMLTextLiteral;
