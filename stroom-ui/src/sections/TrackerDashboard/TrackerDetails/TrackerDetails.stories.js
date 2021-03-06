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
import { storiesOf } from '@storybook/react';
import { withNotes } from '@storybook/addon-notes';

import StoryRouter from 'storybook-react-router';
import { ReduxDecoratorWithInitialisation } from 'lib/storybook/ReduxDecorator';

import TrackerDetails from './TrackerDetails';
import { actionCreators } from '../redux';
import { trackers } from '../tracker.testData';

const containerStyle = {
  border: '30px solid green',
};

const notes =
  "This is tracker details component. You can close it but enable/disable is tied to the services so clicking that won't do anything.";

storiesOf('TrackerDetails', module)
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(actionCreators.updateTrackerSelection(1));
    store.dispatch(actionCreators.updateTrackers([trackers.minimalTracker_undefinedLastPollAge]));
  }))
  .add(
    'Minimal tracker with undefined last poll age',
    withNotes(notes)(() => (
      <div style={containerStyle}>
        <TrackerDetails />
      </div>
    )),
  );

storiesOf('TrackerDetails', module)
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(actionCreators.updateTrackerSelection(2));
    store.dispatch(actionCreators.updateTrackers([trackers.minimalTracker_nullLastPollAge]));
  }))
  .add(
    'Minimal tracker with null last poll age',
    withNotes(notes)(() => (
      <div style={containerStyle}>
        <TrackerDetails />
      </div>
    )),
  );

storiesOf('TrackerDetails', module)
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(actionCreators.updateTrackerSelection(3));
    store.dispatch(actionCreators.updateTrackers([trackers.minimalTracker_emptyLastPollAge]));
  }))
  .add(
    'Minimal tracker with empty last poll age',
    withNotes(notes)(() => (
      <div style={containerStyle}>
        <TrackerDetails />
      </div>
    )),
  );

storiesOf('TrackerDetails', module)
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(actionCreators.updateTrackerSelection(4));
    store.dispatch(actionCreators.updateTrackers([trackers.maximalTracker]));
  }))
  .add(
    'Maximal tracker',
    withNotes(notes)(() => (
      <div style={containerStyle}>
        <TrackerDetails />
      </div>
    )),
  );

storiesOf('TrackerDetails', module)
  .addDecorator(ReduxDecoratorWithInitialisation((store) => {
    store.dispatch(actionCreators.updateTrackerSelection(5));
    store.dispatch(actionCreators.updateTrackers([trackers.maximalTracker_withLongName]));
  }))
  .add(
    'Maximal tracker with a long name',
    withNotes(notes)(() => (
      <div style={containerStyle}>
        <TrackerDetails />
      </div>
    )),
  );
