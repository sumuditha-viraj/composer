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
import SimpleBBox from './../../../../ast/simple-bounding-box';
import SizingUtil from './../sizing-util';
import BallerinaASTFactory from './../../../../ast/ast-factory';

/**
 * Dimension visitor class for While Statement.
 *
 * @class WhileStatementDimensionCalculatorVisitor
 * */
class WhileStatementDimensionCalculatorVisitor {

    /**
     * Constructor for While statement dimensions
     * @param {object} options - options
     */
    constructor(options) {
        this.sizingUtil = new SizingUtil(options);
        this.designer = options.designer;
    }

    /**
     * can visit the visitor.
     *
     * @return {boolean} true.
     *
     * @memberOf WhileStatementDimensionCalculatorVisitor
     * */
    canVisit() {
        return true;
    }

    /**
     * begin visiting the visitor.
     *
     * @memberOf WhileStatementDimensionCalculatorVisitor
     * */
    beginVisit() {
    }

    /**
     * visit the visitor.
     *
     * @memberOf WhileStatementDimensionCalculatorVisitor
     * */
    visit() {
    }

    /**
     * visit the visitor at the end.
     *
     * @param {ASTNode} node - While Statement node.
     *
     * @memberOf WhileStatementDimensionCalculatorVisitor
     * */
    endVisit(node) {
        const viewState = node.getViewState();
        const components = viewState.components;
        const expression = node.getConditionString();
        const bBox = viewState.bBox;
        let statementContainerWidth = 0;
        let statementContainerHeight = 0;
        components.statementContainer = new SimpleBBox();
        const statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        const connectorDeclarationChildren = node.filterChildren(BallerinaASTFactory.isConnectorDeclaration);
        let widthExpansion = 0;
        let statementContainerWidthExpansion = 0;
        // Iterate over statement children
        _.forEach(statementChildren, (child) => {
            statementContainerHeight += child.viewState.bBox.h;
            if (child.viewState.bBox.w > statementContainerWidth) {
                statementContainerWidth = child.viewState.bBox.w;
            }
            if (child.viewState.bBox.expansionW > statementContainerWidthExpansion) {
                statementContainerWidthExpansion = child.viewState.bBox.expansionW;
                widthExpansion = child.viewState.bBox.expansionW;
            }
        });
        // Iterate over connector declaration children
        _.forEach(connectorDeclarationChildren, (child) => {
            statementContainerHeight += this.designer.statement.height + this.designer.statement.gutter.v;
            widthExpansion += (child.viewState.bBox.w + this.designer.blockStatement.heading.width);
            if (child.viewState.components.statementViewState.bBox.w > statementContainerWidth) {
                statementContainerWidth = child.viewState.components.statementViewState.bBox.w;
            }
        });
        viewState.bBox.expansionH = statementContainerHeight;
        // Iterating to set the height of the connector
        if (connectorDeclarationChildren.length > 0) {
            if (statementContainerHeight > (this.designer.statementContainer.height)) {
                _.forEach(connectorDeclarationChildren, (child) => {
                    child.viewState.components.statementContainer.h = statementContainerHeight - (
                        this.designer.lifeLine.head.height + this.designer.lifeLine.footer.height +
                        (this.designer.variablesPane.leftRightPadding * 2)) -
                        this.designer.statement.padding.bottom;
                });
            } else {
                statementContainerHeight = this.designer.statementContainer.height + (
                    this.designer.lifeLine.head.height + this.designer.lifeLine.footer.height +
                    (this.designer.variablesPane.leftRightPadding * 2)) + this.designer.statement.padding.bottom;
            }
        }
        /**
         * We add an extra padding to the statement container height to keep the space bet ween the statement's
         * bottom margin and the last child statement
         */
        statementContainerHeight += (statementContainerHeight > 0 ? this.designer.statement.gutter.v :
        this.designer.blockStatement.body.height - this.designer.blockStatement.heading.height);
        statementContainerWidth = this.getStatementContainerWidth(statementContainerWidth);

        const dropZoneHeight = this.designer.statement.gutter.v + node.getViewState().offSet;
        components['drop-zone'] = new SimpleBBox();
        components['drop-zone'].h = dropZoneHeight;
        components['block-header'] = new SimpleBBox();
        components['block-header'].h = this.designer.blockStatement.heading.height;
        components['block-header'].setOpaque(true);

        bBox.w = statementContainerWidth;
        bBox.h = statementContainerHeight + this.designer.blockStatement.heading.height + dropZoneHeight;

        components.statementContainer.h = statementContainerHeight;
        components.statementContainer.w = statementContainerWidth;
        viewState.bBox.expansionW = widthExpansion;
        components.statementContainer.expansionW = statementContainerWidthExpansion;

        // for compound statement like while we need to render condition expression
        // we will calculate the width of the expression and adjest the block statement
        if (expression !== undefined) {
            // see how much space we have to draw the condition
            const available = statementContainerWidth - this.designer.blockStatement.heading.width - 10;
            components.expression = this.sizingUtil.getTextWidth(expression, 0, available);
        }
    }

    /**
    * get the statement container width.
     *
     * @param {number} currentWidth - current width
     * @return {number} new width.
     *
     * @memberOf WhileStatementDimensionCalculatorVisitor
    * */
    getStatementContainerWidth(currentWidth) {
        let newWidth;
        if (currentWidth > 0) {
            newWidth = currentWidth + this.designer.blockStatement.body.padding.left +
                this.designer.blockStatement.body.padding.right;
        } else {
            newWidth = this.designer.blockStatement.width;
        }

        return newWidth;
    }
}

export default WhileStatementDimensionCalculatorVisitor;
