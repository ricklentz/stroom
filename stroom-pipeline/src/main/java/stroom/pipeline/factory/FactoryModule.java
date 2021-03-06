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

package stroom.pipeline.factory;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import stroom.entity.shared.Clearable;
import stroom.refdata.ReferenceDataFilter;

public class FactoryModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(ElementRegistryFactory.class).to(ElementRegistryFactoryImpl.class);
        bind(ElementFactory.class).to(ElementRegistryFactoryImpl.class);
        bind(PipelineDataCache.class).to(PipelineDataCacheImpl.class);
        bind(PipelineStackLoader.class).to(PipelineStackLoaderImpl.class);
        bind(ProcessorFactory.class).to(ProcessorFactoryImpl.class);

        final Multibinder<Clearable> clearableBinder = Multibinder.newSetBinder(binder(), Clearable.class);
        clearableBinder.addBinding().to(PipelineDataCacheImpl.class);

        final Multibinder<Element> elementBinder = Multibinder.newSetBinder(binder(), Element.class);
        elementBinder.addBinding().to(stroom.pipeline.filter.HttpPostFilter.class);
        elementBinder.addBinding().to(stroom.pipeline.filter.IdEnrichmentFilter.class);
        elementBinder.addBinding().to(stroom.pipeline.filter.RecordCountFilter.class);
        elementBinder.addBinding().to(stroom.pipeline.filter.RecordOutputFilter.class);
        elementBinder.addBinding().to(stroom.pipeline.filter.SchemaFilterSplit.class);
        elementBinder.addBinding().to(stroom.pipeline.filter.SplitFilter.class);
        elementBinder.addBinding().to(stroom.pipeline.filter.TestFilter.class);
        elementBinder.addBinding().to(stroom.pipeline.filter.XSLTFilter.class);
        elementBinder.addBinding().to(stroom.pipeline.parser.CombinedParser.class);
        elementBinder.addBinding().to(stroom.pipeline.parser.DSParser.class);
        elementBinder.addBinding().to(stroom.pipeline.parser.JSONParser.class);
        elementBinder.addBinding().to(stroom.pipeline.parser.XMLFragmentParser.class);
        elementBinder.addBinding().to(stroom.pipeline.parser.XMLParser.class);
        elementBinder.addBinding().to(stroom.pipeline.reader.BOMRemovalFilterInputElement.class);
        elementBinder.addBinding().to(stroom.pipeline.reader.BadTextXMLFilterReaderElement.class);
        elementBinder.addBinding().to(stroom.pipeline.reader.InvalidCharFilterReaderElement.class);
        elementBinder.addBinding().to(stroom.pipeline.reader.InvalidXMLCharFilterReaderElement.class);
        elementBinder.addBinding().to(stroom.pipeline.reader.ReaderElement.class);
        elementBinder.addBinding().to(stroom.pipeline.source.SourceElement.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.FileAppender.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.HDFSFileAppender.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.HTTPAppender.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.JSONWriter.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.RollingFileAppender.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.RollingStreamAppender.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.StreamAppender.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.TestAppender.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.TextWriter.class);
        elementBinder.addBinding().to(stroom.pipeline.writer.XMLWriter.class);
    }
}