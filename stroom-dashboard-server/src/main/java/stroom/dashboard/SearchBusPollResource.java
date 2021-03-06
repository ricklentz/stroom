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

package stroom.dashboard;

import stroom.dashboard.shared.DashboardQueryKey;
import stroom.dashboard.shared.SearchBusPollAction;
import stroom.dashboard.shared.SearchBusPollResult;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/dashboard")
public class SearchBusPollResource {
    private final Provider<SearchBusPollActionHandler> searchBusPollActionHandler;

    @Inject
    public SearchBusPollResource(final Provider<SearchBusPollActionHandler> searchBusPollActionHandler) {
        this.searchBusPollActionHandler = searchBusPollActionHandler;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/poll")
    public Map<DashboardQueryKey, stroom.dashboard.shared.SearchResponse> poll(final Map<DashboardQueryKey, stroom.dashboard.shared.SearchRequest> searchActionMap) {
        final SearchBusPollResult result = searchBusPollActionHandler.get().exec(new SearchBusPollAction(searchActionMap));
        return result.getSearchResultMap();
    }
}