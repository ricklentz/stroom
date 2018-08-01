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

import { Icon } from 'semantic-ui-react';

import DocRefToPick from './DocRefToPick';
import ClickCounter from 'lib/ClickCounter';

const FolderToPick = ({
  explorerId,
  explorer,
  folder,
  typeFilters,
  folderOpenToggled,
  docRefSelected,
}) => {
  const thisIsOpen = !!explorer.isFolderOpen[folder.uuid];
  const icon = thisIsOpen ? 'folder open' : 'folder';
  const isSelected = explorer.isSelected === folder.uuid;

  let className = '';
  if (isSelected) {
    className += ' doc-ref__selected';
  }

  const clickCounter = new ClickCounter()
    .withOnSingleClick(() => docRefSelected(explorerId, folder))
    .withOnDoubleClick(() => folderOpenToggled(explorerId, folder));

  return (
    <div>
      <span
        className={className}
        onClick={() => clickCounter.onSingleClick()}
        onDoubleClick={() => clickCounter.onDoubleClick()}
      >
        <Icon name={icon} />
        <span>{folder.name}</span>
      </span>
      {thisIsOpen && (
        <div className="folder__children">
          {folder.children
            .filter(c => !!explorer.isVisible[c.uuid])
            .map(c =>
                (c.type === 'Folder' ? (
                  <FolderToPick
                    key={c.uuid}
                    explorerId={explorerId}
                    explorer={explorer}
                    folderOpenToggled={folderOpenToggled}
                    docRefSelected={docRefSelected}
                    typeFilters={typeFilters}
                    folder={c}
                  />
                ) : (
                  <DocRefToPick
                    key={c.uuid}
                    explorerId={explorerId}
                    docRef={c}
                    explorer={explorer}
                    docRefSelected={docRefSelected}
                  />
                )))}
        </div>
      )}
    </div>
  );
};

FolderToPick.propTypes = {
  explorerId: PropTypes.string.isRequired,
  folder: PropTypes.object.isRequired,
  folderOpenToggled: PropTypes.func.isRequired,
  docRefSelected: PropTypes.func.isRequired,
  explorer: PropTypes.object.isRequired,
  typeFilters: PropTypes.array.isRequired,
};

export default FolderToPick;
