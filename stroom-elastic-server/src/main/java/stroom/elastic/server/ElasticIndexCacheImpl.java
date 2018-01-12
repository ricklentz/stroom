package stroom.elastic.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.http.HttpStatus;
import org.springframework.stereotype.Component;
import stroom.entity.shared.ExternalDocRefConstants;
import stroom.node.server.StroomPropertyService;
import stroom.node.shared.ClientProperties;
import stroom.pipeline.server.errorhandler.LoggedException;
import stroom.query.api.v2.DocRef;
import stroom.query.audit.DocRefResourceHttpClient;
import stroom.util.cache.CacheManager;

import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.util.concurrent.TimeUnit;

@Component
public class ElasticIndexCacheImpl implements ElasticIndexCache {
    private static final int MAX_CACHE_ENTRIES = 100;

    private final LoadingCache<DocRef, ElasticIndexConfig> cache;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DocRefResourceHttpClient docRefHttpClient;

    @Inject
    ElasticIndexCacheImpl(final CacheManager cacheManager,
                          final StroomPropertyService propertyService) {
        final String urlPropKey = ClientProperties.URL_DOC_REF_SERVICE_BASE + ExternalDocRefConstants.ELASTIC_INDEX;
        docRefHttpClient = new DocRefResourceHttpClient(propertyService.getProperty(urlPropKey));

        final CacheLoader<DocRef, ElasticIndexConfig> cacheLoader = CacheLoader.from(k -> {
            try {
                final Response response = docRefHttpClient.get(k.getUuid());

                if (response.getStatus() != HttpStatus.SC_OK) {
                    final String msg = String.format("Invalid status returned by Elastic Explorer Service: %d - %s ",
                            response.getStatus(),
                            response.getEntity());
                    throw new RuntimeException(msg);
                }

                return objectMapper.readValue(response.getEntity().toString(), ElasticIndexConfig.class);
            } catch (Throwable e) {
                throw new LoggedException(String.format("Failed to retrieve elastic index config for %s", k.getUuid()), e);
            }
        });

        final CacheBuilder cacheBuilder = CacheBuilder.newBuilder()
                .maximumSize(MAX_CACHE_ENTRIES)
                .expireAfterAccess(10, TimeUnit.MINUTES);
        cache = cacheBuilder.build(cacheLoader);
        cacheManager.registerCache("Elastic Index Config Cache", cacheBuilder, cache);
    }

    @Override
    public ElasticIndexConfig get(final DocRef key) {
        return cache.getUnchecked(key);
    }

    @Override
    public void remove(final DocRef key) {
        cache.invalidate(key);
    }
}