/*
 * Copyright 2018 Crown Copyright
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import React, { Component } from 'react';
import PropTypes from 'prop-types';

import { connect } from 'react-redux';

import { actionCreators } from './redux';

/**
 * This is a Higher Order Component
 * https://reactjs.org/docs/higher-order-components.html
 *
 * It provides the expression by connecting to the redux store and using a provided
 * expressionId to look it up.
 *
 * @param {React.Component} WrappedComponent
 */
export function withExpression() {
  return (WrappedComponent) => {
    const WithExpression = class extends Component {
      static propTypes = {
        expressionId: PropTypes.string.isRequired,
        expressions: PropTypes.object.isRequired,
      };

      state = {
        expression: undefined,
      };

      static getDerivedStateFromProps(nextProps, prevState) {
        return {
          expression: nextProps.expressions[nextProps.expressionId],
        };
      }

      render() {
        if (this.state.expression) {
          return <WrappedComponent {...this.state} {...this.props} />;
        }
        return <span>awaiting expression state</span>;
      }
    };

    return connect(
      state => ({
        expressions: state.expressions,
      }),
      {
        // actions
      },
    )(WithExpression);
  };
}
