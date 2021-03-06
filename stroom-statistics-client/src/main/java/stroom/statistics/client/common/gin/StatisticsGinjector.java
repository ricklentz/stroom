/*
 * Copyright 2016 Crown Copyright
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

package stroom.statistics.client.common.gin;

import com.google.gwt.inject.client.AsyncProvider;
import stroom.statistics.client.common.StatisticsPlugin;
import stroom.statistics.client.common.presenter.StatisticsDataSourcePresenter;
import stroom.statistics.client.common.presenter.StatisticsDataSourceSettingsPresenter;
import stroom.statistics.client.common.presenter.StatisticsFieldEditPresenter;

public interface StatisticsGinjector {
    // Statistics Data Source
    AsyncProvider<StatisticsPlugin> getStatisticPlugin();

    AsyncProvider<StatisticsDataSourcePresenter> getStatisticsDataSourcePresenter();

    AsyncProvider<StatisticsDataSourceSettingsPresenter> getStatisticsDataSourceSettingsPresenter();

    AsyncProvider<StatisticsFieldEditPresenter> getStatisticFieldEditPresenter();

}
