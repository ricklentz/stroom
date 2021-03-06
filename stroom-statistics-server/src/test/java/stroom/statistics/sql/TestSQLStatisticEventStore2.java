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
 */

package stroom.statistics.sql;

import org.junit.Assert;
import org.junit.Test;
import stroom.query.api.v2.ExpressionOperator;
import stroom.query.api.v2.ExpressionOperator.Op;
import stroom.query.api.v2.ExpressionTerm.Condition;
import stroom.query.api.v2.Query;
import stroom.query.api.v2.SearchRequest;
import stroom.statistics.shared.StatisticStoreDoc;
import stroom.statistics.shared.StatisticsDataSourceData;
import stroom.statistics.shared.common.CustomRollUpMask;
import stroom.statistics.shared.common.StatisticField;
import stroom.statistics.shared.common.StatisticRollUpType;
import stroom.statistics.sql.rollup.RollUpBitMask;
import stroom.statistics.sql.rollup.RolledUpStatisticEvent;
import stroom.statistics.sql.search.FilterTermsTree;
import stroom.statistics.sql.search.FindEventCriteria;
import stroom.statistics.sql.search.StatStoreCriteriaBuilder;
import stroom.util.date.DateUtil;
import stroom.util.test.StroomUnitTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestSQLStatisticEventStore2 extends StroomUnitTest {
    private static final long EVENT_TIME = 1234L;
    private static final String EVENT_NAME = "MyStatistic";
    private static final long EVENT_COUNT = 1;

    private static final String TAG1_NAME = "Tag1";
    private static final String TAG1_VALUE = "Tag1Val";

    private static final String TAG2_NAME = "Tag2";
    private static final String TAG2_VALUE = "Tag2Val";

    private static final String ROLLED_UP_VALUE = "*";

    @Test
    public void testGenerateTagRollUpsTwoTagsAllRollUps() {
        final StatisticEvent event = buildEvent(buildTagList());

        final StatisticStoreDoc statisticsDataSource = buildStatisticDataSource();

        final RolledUpStatisticEvent rolledUpStatisticEvent = SQLStatisticEventStore.generateTagRollUps(event,
                statisticsDataSource);

        Assert.assertEquals(4, rolledUpStatisticEvent.getPermutationCount());

        final List<TimeAgnosticStatisticEvent> timeAgnosticStatisticEvents = new ArrayList<>();

        // define all the tag/value perms we expect to get back
        final List<List<StatisticTag>> expectedTagPerms = new ArrayList<>();
        expectedTagPerms
                .add(Arrays.asList(new StatisticTag(TAG1_NAME, TAG1_VALUE), new StatisticTag(TAG2_NAME, TAG2_VALUE)));
        expectedTagPerms.add(
                Arrays.asList(new StatisticTag(TAG1_NAME, TAG1_VALUE), new StatisticTag(TAG2_NAME, ROLLED_UP_VALUE)));
        expectedTagPerms.add(
                Arrays.asList(new StatisticTag(TAG1_NAME, ROLLED_UP_VALUE), new StatisticTag(TAG2_NAME, TAG2_VALUE)));
        expectedTagPerms.add(Arrays.asList(new StatisticTag(TAG1_NAME, ROLLED_UP_VALUE),
                new StatisticTag(TAG2_NAME, ROLLED_UP_VALUE)));

        System.out.println("-------------------------------------------------");
        for (final List<StatisticTag> expectedTagList : expectedTagPerms) {
            System.out.println(expectedTagList);
        }
        System.out.println("-------------------------------------------------");

        for (final TimeAgnosticStatisticEvent eventPerm : rolledUpStatisticEvent) {
            // make sure we don't already have one like this.
            Assert.assertFalse(timeAgnosticStatisticEvents.contains(eventPerm));

            Assert.assertEquals(event.getName(), eventPerm.getName());
            Assert.assertEquals(event.getType(), eventPerm.getType());
            Assert.assertEquals(event.getCount(), eventPerm.getCount());
            Assert.assertEquals(event.getTagList().size(), eventPerm.getTagList().size());

            System.out.println(eventPerm.getTagList());

            Assert.assertTrue(expectedTagPerms.contains(eventPerm.getTagList()));

            for (int i = 0; i < event.getTagList().size(); i++) {
                Assert.assertEquals(event.getTagList().get(i).getTag(), eventPerm.getTagList().get(i).getTag());
                Assert.assertTrue(event.getTagList().get(i).getValue().equals(eventPerm.getTagList().get(i).getValue())
                        || RollUpBitMask.ROLL_UP_TAG_VALUE.equals(eventPerm.getTagList().get(i).getValue()));
            }
        }
    }

    @Test
    public void testGenerateTagRollUpsTwoTagsCustomRollUps() {
        final StatisticEvent event = buildEvent(buildTagList());

        final StatisticStoreDoc statisticsDataSource = buildStatisticDataSource(StatisticRollUpType.CUSTOM);

        final RolledUpStatisticEvent rolledUpStatisticEvent = SQLStatisticEventStore.generateTagRollUps(event,
                statisticsDataSource);

        Assert.assertEquals(3, rolledUpStatisticEvent.getPermutationCount());

        final List<TimeAgnosticStatisticEvent> timeAgnosticStatisticEvents = new ArrayList<>();

        // define all the tag/value perms we expect to get back
        final List<List<StatisticTag>> expectedTagPerms = new ArrayList<>();

        // nothing rolled up
        expectedTagPerms
                .add(Arrays.asList(new StatisticTag(TAG1_NAME, TAG1_VALUE), new StatisticTag(TAG2_NAME, TAG2_VALUE)));

        // tag 2 rolled up
        expectedTagPerms.add(
                Arrays.asList(new StatisticTag(TAG1_NAME, TAG1_VALUE), new StatisticTag(TAG2_NAME, ROLLED_UP_VALUE)));

        // tags 1 and 2 rolled up
        expectedTagPerms.add(Arrays.asList(new StatisticTag(TAG1_NAME, ROLLED_UP_VALUE),
                new StatisticTag(TAG2_NAME, ROLLED_UP_VALUE)));

        System.out.println("-------------------------------------------------");
        for (final List<StatisticTag> expectedTagList : expectedTagPerms) {
            System.out.println(expectedTagList);
        }
        System.out.println("-------------------------------------------------");

        for (final TimeAgnosticStatisticEvent eventPerm : rolledUpStatisticEvent) {
            // make sure we don't already have one like this.
            Assert.assertFalse(timeAgnosticStatisticEvents.contains(eventPerm));

            Assert.assertEquals(event.getName(), eventPerm.getName());
            Assert.assertEquals(event.getType(), eventPerm.getType());
            Assert.assertEquals(event.getCount(), eventPerm.getCount());
            Assert.assertEquals(event.getTagList().size(), eventPerm.getTagList().size());

            System.out.println(eventPerm.getTagList());

            Assert.assertTrue(expectedTagPerms.contains(eventPerm.getTagList()));

            for (int i = 0; i < event.getTagList().size(); i++) {
                Assert.assertEquals(event.getTagList().get(i).getTag(), eventPerm.getTagList().get(i).getTag());
                Assert.assertTrue(event.getTagList().get(i).getValue().equals(eventPerm.getTagList().get(i).getValue())
                        || RollUpBitMask.ROLL_UP_TAG_VALUE.equals(eventPerm.getTagList().get(i).getValue()));
            }
        }
    }

    @Test
    public void testGenerateTagRollUpsNoTags() {
        final StatisticEvent event = buildEvent(new ArrayList<>());

        final StatisticStoreDoc statisticsDataSource = buildStatisticDataSource();

        final RolledUpStatisticEvent rolledUpStatisticEvent = SQLStatisticEventStore.generateTagRollUps(event,
                statisticsDataSource);

        Assert.assertEquals(1, rolledUpStatisticEvent.getPermutationCount());

        for (final TimeAgnosticStatisticEvent eventPerm : rolledUpStatisticEvent) {
            Assert.assertEquals(event.getName(), eventPerm.getName());
            Assert.assertEquals(event.getType(), eventPerm.getType());
            Assert.assertEquals(event.getCount(), eventPerm.getCount());
            Assert.assertEquals(event.getTagList().size(), eventPerm.getTagList().size());

            for (int i = 0; i < event.getTagList().size(); i++) {
                Assert.assertEquals(event.getTagList().get(i).getTag(), eventPerm.getTagList().get(i).getTag());
                Assert.assertTrue(event.getTagList().get(i).getValue().equals(eventPerm.getTagList().get(i).getValue())
                        || RollUpBitMask.ROLL_UP_TAG_VALUE.equals(eventPerm.getTagList().get(i).getValue()));
            }
        }

    }

    @Test
    public void testGenerateTagRollUpsNotEnabled() {
        final StatisticEvent event = buildEvent(buildTagList());

        final StatisticStoreDoc statisticsDataSource = buildStatisticDataSource(StatisticRollUpType.NONE);

        final RolledUpStatisticEvent rolledUpStatisticEvent = SQLStatisticEventStore.generateTagRollUps(event,
                statisticsDataSource);

        Assert.assertEquals(1, rolledUpStatisticEvent.getPermutationCount());

        for (final TimeAgnosticStatisticEvent eventPerm : rolledUpStatisticEvent) {
            Assert.assertEquals(event.getName(), eventPerm.getName());
            Assert.assertEquals(event.getType(), eventPerm.getType());
            Assert.assertEquals(event.getCount(), eventPerm.getCount());
            Assert.assertEquals(event.getTagList().size(), eventPerm.getTagList().size());

            for (int i = 0; i < event.getTagList().size(); i++) {
                Assert.assertEquals(event.getTagList().get(i).getTag(), eventPerm.getTagList().get(i).getTag());
                Assert.assertTrue(event.getTagList().get(i).getValue().equals(eventPerm.getTagList().get(i).getValue())
                        || RollUpBitMask.ROLL_UP_TAG_VALUE.equals(eventPerm.getTagList().get(i).getValue()));
            }
        }
    }

    private StatisticEvent buildEvent(final List<StatisticTag> tagList) {
        return StatisticEvent.createCount(EVENT_TIME, EVENT_NAME, tagList, EVENT_COUNT);
    }

    private List<StatisticTag> buildTagList() {
        final List<StatisticTag> tagList = new ArrayList<>();

        tagList.add(new StatisticTag(TAG1_NAME, TAG1_VALUE));
        tagList.add(new StatisticTag(TAG2_NAME, TAG2_VALUE));

        return tagList;
    }

    private StatisticStoreDoc buildStatisticDataSource() {
        return buildStatisticDataSource(StatisticRollUpType.ALL);
    }

    private StatisticStoreDoc buildStatisticDataSource(final StatisticRollUpType statisticRollUpType) {
        final StatisticStoreDoc statisticsDataSource = new StatisticStoreDoc();

        final StatisticsDataSourceData statisticsDataSourceData = new StatisticsDataSourceData();

        final List<StatisticField> fields = new ArrayList<>();

        fields.add(new StatisticField(TAG1_NAME));
        fields.add(new StatisticField(TAG2_NAME));

        statisticsDataSourceData.setStatisticFields(fields);

        // add the custom rollup masks, which only come into play if the type is
        // CUSTOM
        statisticsDataSourceData.addCustomRollUpMask(new CustomRollUpMask(new ArrayList<>())); // no
        // tags
        statisticsDataSourceData.addCustomRollUpMask(new CustomRollUpMask(Arrays.asList(0, 1))); // tags
        // 1&2
        statisticsDataSourceData.addCustomRollUpMask(new CustomRollUpMask(Arrays.asList(1))); // tag
        // 2

        statisticsDataSource.setConfig(statisticsDataSourceData);
        statisticsDataSource.setRollUpType(statisticRollUpType);

        return statisticsDataSource;
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBuildCriteria_noDate() {
        final ExpressionOperator.Builder rootOperator = new ExpressionOperator.Builder(Op.AND);

        final Query query = new Query(null, rootOperator.build());
        final SearchRequest searchRequest = new SearchRequest(null, query, null, null, true);

        final StatisticStoreDoc dataSource = new StatisticStoreDoc();
        dataSource.setName("MyDataSource");

        StatStoreCriteriaBuilder.buildCriteria(searchRequest, dataSource);

    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBuildCriteria_invalidDateCondition() {
        final ExpressionOperator.Builder rootOperator = new ExpressionOperator.Builder(Op.AND);

        final String dateTerm = "2000-01-01T00:00:00.000Z,2010-01-01T00:00:00.000Z";

        rootOperator.addTerm(StatisticStoreDoc.FIELD_NAME_DATE_TIME,
                Condition.IN_DICTIONARY, dateTerm);

        final Query query = new Query(null, rootOperator.build());
        final SearchRequest searchRequest = new SearchRequest(null, query, null, null, true);

        final StatisticStoreDoc dataSource = new StatisticStoreDoc();
        dataSource.setName("MyDataSource");

        StatStoreCriteriaBuilder.buildCriteria(searchRequest, dataSource);

    }

    @Test
    public void testBuildCriteria_validDateTerm() {
        final ExpressionOperator.Builder rootOperator = new ExpressionOperator.Builder(Op.AND);

        final String fromDateStr = "2000-01-01T00:00:00.000Z";
        final long fromDate = DateUtil.parseNormalDateTimeString(fromDateStr);
        final String toDateStr = "2010-01-01T00:00:00.000Z";
        final long toDate = DateUtil.parseNormalDateTimeString(toDateStr);

        final String dateTerm = fromDateStr + "," + toDateStr;

        rootOperator.addTerm(StatisticStoreDoc.FIELD_NAME_DATE_TIME, Condition.BETWEEN, dateTerm);

        final Query query = new Query(null, rootOperator.build());
        final SearchRequest searchRequest = new SearchRequest(null, query, null, null, true);

        final StatisticStoreDoc dataSource = new StatisticStoreDoc();
        dataSource.setName("MyDataSource");

        final FindEventCriteria criteria = StatStoreCriteriaBuilder.buildCriteria(searchRequest, dataSource);

        Assert.assertNotNull(criteria);
        Assert.assertEquals(fromDate, criteria.getPeriod().getFrom().longValue());
        Assert.assertEquals(toDate + 1, criteria.getPeriod().getTo().longValue());

        // only a date term so the filter tree has noting in it as the date is
        // handled outside of the tree
        Assert.assertEquals(FilterTermsTree.emptyTree(), criteria.getFilterTermsTree());
    }

    @Test(expected = RuntimeException.class)
    public void testBuildCriteria_invalidDateTermOnlyOneDate() {
        final ExpressionOperator.Builder rootOperator = new ExpressionOperator.Builder(Op.AND);

        final String fromDateStr = "2000-01-01T00:00:00.000Z";
        final long fromDate = DateUtil.parseNormalDateTimeString(fromDateStr);

        final String dateTerm = fromDateStr;

        rootOperator.addTerm(StatisticStoreDoc.FIELD_NAME_DATE_TIME, Condition.BETWEEN, dateTerm);

        final Query query = new Query(null, rootOperator.build());
        final SearchRequest searchRequest = new SearchRequest(null, query, null, null, true);

        final StatisticStoreDoc dataSource = new StatisticStoreDoc();
        dataSource.setName("MyDataSource");

        StatStoreCriteriaBuilder.buildCriteria(searchRequest, dataSource);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuildCriteria_validDateTermOtherTermMissingFieldName() {
        final ExpressionOperator.Builder rootOperator = new ExpressionOperator.Builder(Op.AND);

        final String fromDateStr = "2000-01-01T00:00:00.000Z";
        final long fromDate = DateUtil.parseNormalDateTimeString(fromDateStr);
        final String toDateStr = "2010-01-01T00:00:00.000Z";
        final long toDate = DateUtil.parseNormalDateTimeString(toDateStr);

        final String dateTerm = fromDateStr + "," + toDateStr;

        rootOperator.addTerm(StatisticStoreDoc.FIELD_NAME_DATE_TIME, Condition.BETWEEN, dateTerm);
        rootOperator.addTerm(null, Condition.EQUALS, "xxx");

        final Query query = new Query(null, rootOperator.build());
        final SearchRequest searchRequest = new SearchRequest(null, query, null, null, true);

        final StatisticStoreDoc dataSource = new StatisticStoreDoc();
        dataSource.setName("MyDataSource");

        StatStoreCriteriaBuilder.buildCriteria(searchRequest, dataSource);
    }

    @Test
    public void testBuildCriteria_validDateTermOtherTermMissingFieldValue() {
        final ExpressionOperator.Builder rootOperator = new ExpressionOperator.Builder(Op.AND);

        final String fromDateStr = "2000-01-01T00:00:00.000Z";
        final long fromDate = DateUtil.parseNormalDateTimeString(fromDateStr);
        final String toDateStr = "2010-01-01T00:00:00.000Z";
        final long toDate = DateUtil.parseNormalDateTimeString(toDateStr);

        final String dateTerm = fromDateStr + "," + toDateStr;

        rootOperator.addTerm(StatisticStoreDoc.FIELD_NAME_DATE_TIME, Condition.BETWEEN, dateTerm);
        rootOperator.addTerm("MyField", Condition.EQUALS, "");

        final Query query = new Query(null, rootOperator.build());
        final SearchRequest searchRequest = new SearchRequest(null, query, null, null, true);

        final StatisticStoreDoc dataSource = new StatisticStoreDoc();
        dataSource.setName("MyDataSource");

        final FindEventCriteria criteria = StatStoreCriteriaBuilder.buildCriteria(searchRequest, dataSource);

        Assert.assertNotNull(criteria);
        Assert.assertEquals("[]", criteria.getFilterTermsTree().toString());
    }

    @Test
    public void testBuildCriteria_validDateTermAndOtherTerm() {
        final ExpressionOperator.Builder rootOperator = new ExpressionOperator.Builder(Op.AND);

        final String fromDateStr = "2000-01-01T00:00:00.000Z";
        final long fromDate = DateUtil.parseNormalDateTimeString(fromDateStr);
        final String toDateStr = "2010-01-01T00:00:00.000Z";
        final long toDate = DateUtil.parseNormalDateTimeString(toDateStr);

        final String dateTerm = fromDateStr + "," + toDateStr;

        rootOperator.addTerm(StatisticStoreDoc.FIELD_NAME_DATE_TIME, Condition.BETWEEN, dateTerm);

        rootOperator.addTerm("MyField", Condition.EQUALS, "xxx");

        final Query query = new Query(null, rootOperator.build());
        final SearchRequest searchRequest = new SearchRequest(null, query, null, null, true);

        final StatisticStoreDoc dataSource = new StatisticStoreDoc();
        dataSource.setName("MyDataSource");

        final FindEventCriteria criteria = StatStoreCriteriaBuilder.buildCriteria(searchRequest, dataSource);

        Assert.assertNotNull(criteria);
        Assert.assertEquals(fromDate, criteria.getPeriod().getFrom().longValue());
        Assert.assertEquals(toDate + 1, criteria.getPeriod().getTo().longValue());

        // only a date term so the filter tree has noting in it as the date is
        // handled outside of the tree
        Assert.assertEquals("[MyField=xxx]", criteria.getFilterTermsTree().toString());
    }
}
