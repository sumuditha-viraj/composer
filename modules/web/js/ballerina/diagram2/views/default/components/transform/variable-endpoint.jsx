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

import React from 'react';
import PropTypes from 'prop-types';
import './variable-endpoint.css';
import VariableTypeDropdown from './variable-type-dropdown.jsx'

export default class VariableEndpoint extends React.Component {
    constructor(props, context) {
        super(props, context);
        let type = '';
        let name = '';
        let val = '';
        if (this.props.variable.varDeclarationString) {
            type = this.props.variable.varDeclarationString.trim().split('=')[0].split(' ')[0].trim();
            name = this.props.variable.varDeclarationString.trim().split('=')[0].split(' ')[1].trim();
            val = this.props.variable.varDeclarationString.trim().split('=')[1]
                  .replace(/"/g, '').replace(';', '').trim();
        }
        this.state = {
            onEdit: false,
            varType: type,
            varName: name,
            varVal: val
        };

        this.typeChange = this.typeChange.bind(this);
        this.nameChange = this.nameChange.bind(this);
        this.valChange = this.valChange.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.onComplete = this.onComplete.bind(this);
    }

    componentWillUnmount() {
        const { id, onRemove } = this.props;
        onRemove(id);
    }

    render() {
        const { variable, makeConnectPoint, level, id, removeTypeCallbackFunc, onClick, onRemove,
            updateVariable, isFolded, onConnectPointMouseEnter } = this.props;
        let iconType = 'fw-variable';
        let className = 'transform-endpoint variable';

        if (variable.type === 'struct') {
            iconType = 'fw-struct';
            className += ' transform-endpoint-struct';

            if (isFolded) {
                className += '-folded';
            }
        }

        let folderLeft = (level * 13) + 2;

        const variableRoot = variable.root || variable;
        if (variableRoot.endpointKind === 'output') {
            folderLeft += 30;
        }

        return (
            <div className={className} style={{ paddingLeft: level > 0 ? ((level - 1) * 13) + 7 : 7 }}>
                <span >
                    {(variable.type === 'struct' || variable.isField) &&
                    <span
                        className='folder'
                        style={{ left: folderLeft }}
                        onClick={(e) => { onClick && onClick(variable.name); }}
                    />}
                    {(level > 0) && <span className='tree-view-icon' />}
                    <span className='variable-icon'>
                        <i className={`transform-endpoint-icon fw ${iconType}`} />
                    </span>
                    <span className='endpoint-content' onClick={(e) => { onClick && onClick(variable.name); }}>
                        {!this.state.onEdit && variable.displayName &&
                            <span className='endpoint-name'>
                                {variable.displayName}:
                            </span>
                        }
                        {!this.state.onEdit &&
                        <span className='endpoint-type'>
                            {variable.typeName || variable.type}
                        </span>
                        }
                        {this.props.variable.varDeclarationString && !this.state.onEdit &&
                            <span className='variable-edit-button'>
                                <i className='btn fw fw-edit' onClick={this.onEdit} />
                            </span>
                        }

                    </span>
                </span>
                { this.state.onEdit &&
                <div className='transform-edit-panel'>
                    <VariableTypeDropdown
                        value={this.state.varType}
                        placeholder='type'
                        onChange={this.typeChange}
                    />
                    <input
                        type='text'
                        className='variable-edit-name'
                        value={this.state.varName}
                        placeholder='name'
                        onChange={this.nameChange}
                    />
                    <input
                        type='text'
                        className='variable-edit-val'
                        value={this.state.varVal}
                        placeholder='value'
                        onChange={this.valChange}
                    />
                    <span>
                        <i className='btn fw fw-check' onClick={this.onComplete} />
                    </span>
                </div>
                }
                <span
                    id={variable.id + '-button'}
                    className='btn connect-point'
                >
                    <i
                        id={id}
                        ref={icon => makeConnectPoint(icon, id, variable)}
                        className='variable-endpoint fw fw-circle-outline fw-stack-1x'
                        onMouseEnter={(e) => { onConnectPointMouseEnter(variable, e); }}
                    />
                </span>
            </div>
        );
    }

    typeChange(val) {
        this.setState({ varType: val });
    }

    nameChange(e) {
        this.setState({ varName: e.target.value });
    }

    valChange(e) {
        this.setState({ varVal: e.target.value });
    }

    onEdit() {
        this.setState({ onEdit: true });
    }
    onComplete() {
        let qoutes = '';
        let validType = false;
        const validName = Number.isNaN(Number.parseFloat(this.state.varName.charAt(0))) && this.state.varName !== '';
        if (this.state.varType === 'string') {
            qoutes = '"';
            validType = true;
        } else if (this.state.varType === 'int') {
            validType = this.isInt(this.state.varVal);
        } else if (this.state.varType === 'float') {
            validType = this.isFloat(this.state.varVal) || this.isInt(this.state.varVal);
        } else if (this.state.varType !== '') {
            validType = true;
        }
        if (validType && validName) {
            const statement = this.state.varType + ' ' + this.state.varName + ' = ' + qoutes
                              + this.state.varVal + qoutes;
            const isUpdated = this.props.updateVariable(this.props.variable.name, statement, this.props.type);
            if (isUpdated) {
                this.setState({ onEdit: false });
            }
        } else {
            if (!validName) {
                this.context.alert.showError('Invalid variable name');
            }
            if (!validType) {
                this.context.alert.showError('Invalid value for variable selected type');
            }
        }
    }

    isInt(val) {
        return !isNaN(val) && Number(val).toString().length === (Number.parseInt(Number(val), 10).toString().length);
    }

    isFloat(val) {
        return !isNaN(val) && !this.isInt(Number(val)) && val.toString().length > 0;
    }
}

VariableEndpoint.contextTypes = {
    alert: PropTypes.shape({
        showInfo: PropTypes.func,
        showSuccess: PropTypes.func,
        showWarning: PropTypes.func,
        showError: PropTypes.func,
        closeEditor: PropTypes.func,
    }).isRequired,
};
