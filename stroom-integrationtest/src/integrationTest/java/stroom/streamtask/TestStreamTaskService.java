/*
 * Copyright 2017 Crown Copyright
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
 *
 */

package stroom.streamtask;

import org.junit.Assert;
import org.junit.Test;
import stroom.entity.shared.Period;
import stroom.feed.shared.Feed;
import stroom.node.shared.Node;
import stroom.pipeline.shared.PipelineDoc;
import stroom.streamstore.StreamStore;
import stroom.streamstore.shared.Stream;
import stroom.streamstore.shared.StreamType;
import stroom.streamtask.shared.FindStreamTaskCriteria;
import stroom.streamtask.shared.StreamProcessor;
import stroom.streamtask.shared.StreamTask;
import stroom.streamtask.shared.TaskStatus;
import stroom.task.SimpleTaskContext;
import stroom.test.AbstractCoreIntegrationTest;
import stroom.test.CommonTestScenarioCreator;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneOffset;

public class TestStreamTaskService extends AbstractCoreIntegrationTest {
    @Inject
    private CommonTestScenarioCreator commonTestScenarioCreator;
    @Inject
    private StreamTaskService streamTaskService;
    @Inject
    private StreamStore streamStore;
    @Inject
    private StreamTaskCreator streamTaskCreator;

    @Test
    public void testSaveAndGetAll() {
        final Feed efd = commonTestScenarioCreator.createSimpleFeed();
        final Stream file1 = commonTestScenarioCreator.createSample2LineRawFile(efd, StreamType.RAW_EVENTS);
        final Stream file2 = commonTestScenarioCreator.createSampleBlankProcessedFile(efd, file1);
        final Stream file3 = commonTestScenarioCreator.createSample2LineRawFile(efd, StreamType.RAW_EVENTS);

        commonTestScenarioCreator.createBasicTranslateStreamProcessor(efd);

        Assert.assertEquals("checking we can delete stand alone files", 1, streamStore.deleteStream(file3).intValue());

        // Create all required tasks.
        createTasks();

        final StreamTask ps1 = streamTaskService.find(FindStreamTaskCriteria.createWithStream(file1)).getFirst();
        Assert.assertNotNull(ps1);
        ps1.setStatus(TaskStatus.COMPLETE);

        streamTaskService.save(ps1);

        FindStreamTaskCriteria criteria = new FindStreamTaskCriteria();
        criteria.getFetchSet().add(Stream.ENTITY_TYPE);
        criteria.obtainStreamTaskStatusSet().add(TaskStatus.COMPLETE);

        Assert.assertEquals(1, streamTaskService.find(criteria).size());

        // Check the date filter works
        criteria.setCreatePeriod(new Period(file1.getCreateMs() - 10000, file1.getCreateMs() + 10000));
        Assert.assertEquals(1, streamTaskService.find(criteria).size());

        criteria.setCreatePeriod(
                new Period(Instant.ofEpochMilli(criteria.getCreatePeriod().getFrom()).atZone(ZoneOffset.UTC).plusYears(100).toInstant().toEpochMilli(),
                        Instant.ofEpochMilli(criteria.getCreatePeriod().getTo()).atZone(ZoneOffset.UTC).plusYears(100).toInstant().toEpochMilli()));
        Assert.assertEquals(0, streamTaskService.find(criteria).size());

        Assert.assertNotNull(streamStore.loadStreamById(file1.getId()));
        Assert.assertNotNull(streamStore.loadStreamById(file2.getId()));

        criteria = new FindStreamTaskCriteria();
        Assert.assertNotNull(streamTaskService.findSummary(criteria));
    }

    @Test
    public void testApplyAllCriteria() {
        commonTestScenarioCreator.createSimpleFeed();

        final Node testNode = new Node();
        testNode.setId(1L);

        final FindStreamTaskCriteria criteria = new FindStreamTaskCriteria();
        criteria.getFetchSet().add(Node.ENTITY_TYPE);
        criteria.obtainNodeIdSet().add(1L);
        criteria.setSort(FindStreamTaskCriteria.FIELD_CREATE_TIME);
        criteria.obtainStreamTaskIdSet().add(1L);
        criteria.obtainFeedIdSet().add(1L);
        criteria.obtainStreamIdSet().add(1L);
        criteria.obtainStreamTypeIdSet().add(1L);
        criteria.obtainStreamTaskStatusSet().add(TaskStatus.COMPLETE);

        criteria.setCreatePeriod(new Period(System.currentTimeMillis(), System.currentTimeMillis()));
        criteria.setEffectivePeriod(new Period(System.currentTimeMillis(), System.currentTimeMillis()));
        criteria.obtainStreamTypeIdSet().add(StreamType.CONTEXT.getId());

        criteria.getFetchSet().add(Stream.ENTITY_TYPE);
        criteria.getFetchSet().add(Node.ENTITY_TYPE);
        criteria.getFetchSet().add(Feed.ENTITY_TYPE);
        criteria.getFetchSet().add(StreamProcessor.ENTITY_TYPE);

        Assert.assertEquals(0, streamTaskService.find(criteria).size());
    }

    @Test
    public void testApplyAllCriteriaSummary() {
        commonTestScenarioCreator.createSimpleFeed();

        final Node testNode = new Node();
        testNode.setId(1L);

        final FindStreamTaskCriteria criteria = new FindStreamTaskCriteria();
        criteria.getFetchSet().add(Node.ENTITY_TYPE);
        criteria.obtainNodeIdSet().add(1L);
        criteria.setSort(FindStreamTaskCriteria.FIELD_CREATE_TIME);
        criteria.obtainStreamTaskIdSet().add(1L);
        criteria.obtainFeedIdSet().add(1L);
        criteria.obtainStreamIdSet().add(1L);
        criteria.obtainStreamTypeIdSet().add(1L);
        criteria.obtainStreamTaskStatusSet().add(TaskStatus.COMPLETE);

        criteria.setCreatePeriod(new Period(System.currentTimeMillis(), System.currentTimeMillis()));
        criteria.setEffectivePeriod(new Period(System.currentTimeMillis(), System.currentTimeMillis()));
        criteria.obtainStreamTypeIdSet().add(StreamType.CONTEXT.getId());

    }

    private void createTasks() {
        // Make sure there are no tasks yet.
        streamTaskCreator.createTasks(new SimpleTaskContext());
    }
}
