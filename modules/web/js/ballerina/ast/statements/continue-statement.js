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
import Statement from './statement';
import ASTFactory from '../ast-factory.js';

/**
 * Class to represent an Continue statement.
 */
class ContinueStatement extends Statement {
    /**
     * Constructor for continue statement
     */
    constructor() {
        super();
        this.type = 'ContinueStatement';
        this.whiteSpace.defaultDescriptor.regions = {
            0: '',
            1: ' ',
            2: '\n',
            3: ' ',
        };
    }

    /**
     * Initialize the node from the node json.
     * @returns {void}
     * */
    initFromJson() {
    }

    /**
     * Get the statement string
     * @returns {string} break statement string
     * @override
     */
    getStatementString() {
        return 'continue';
    }

    /**
     * Define what type of nodes that this node can be added as a child.
     * @param {ASTNode} node - Parent node that this node becoming a child of.
     * @return {boolean} true|false.
     * */
    canBeAChildOf(node) {
        return this.iterateOverParent(node);
    }

    /**
     * Define what type of nodes that this node can be added as a child by iterating over its parents.
     * @param {ASTNode} node - Parent node that this node becoming a child of.
     * @return {boolean} true|false.
     * */
    iterateOverParent(node) {
        if (node === undefined) {
            return false;
        }
        if (ASTFactory.isBallerinaAstRoot(node)) {
            return false;
        } else if (ASTFactory.isWhileStatement(node)) {
            return true;
        } else {
            return this.iterateOverParent(node.getParent());
        }
    }
}

export default ContinueStatement;

