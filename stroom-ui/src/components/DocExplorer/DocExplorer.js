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
import React from 'react';
import PropTypes from 'prop-types';

import { compose, lifecycle, branch, renderComponent } from 'recompose';
import { connect } from 'react-redux';

import { Input, Loader } from 'semantic-ui-react';

import Folder from './Folder';

import { actionCreators } from './redux';

const { searchTermUpdated, explorerTreeOpened } = actionCreators;

const DocExplorer = ({
  documentTree, explorerId, explorer, searchTermUpdated,
}) => (
  <div>
    <Input
      icon="search"
      placeholder="Search..."
      value={explorer.searchTerm}
      onChange={e => searchTermUpdated(explorerId, e.target.value)}
    />
    <Folder explorerId={explorerId} folder={documentTree} />
  </div>
);

DocExplorer.propTypes = {
  explorerId: PropTypes.string.isRequired,
  explorer: PropTypes.object,
  documentTree: PropTypes.object.isRequired,

  searchTermUpdated: PropTypes.func.isRequired,
};

export default compose(
  connect(
    (state, props) => ({
      documentTree: state.explorerTree.documentTree,
      explorer: state.explorerTree.explorers[props.explorerId],
    }),
    {
      searchTermUpdated,
      explorerTreeOpened,
    },
  ),
  lifecycle({
    componentDidMount() {
      this.props.explorerTreeOpened(
        this.props.explorerId,
        this.props.allowMultiSelect,
        this.props.allowDragAndDrop,
        this.props.typeFilter,
      );
    },
  }),
  branch(props => !props.explorer, renderComponent(() => <Loader active>Loading Explorer</Loader>)),
)(DocExplorer);
