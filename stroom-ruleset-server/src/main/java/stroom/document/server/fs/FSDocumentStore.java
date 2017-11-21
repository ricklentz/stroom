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

package stroom.document.server.fs;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import stroom.document.server.DocumentActionHandler;
import stroom.document.shared.Document;
import stroom.entity.shared.ImportState;
import stroom.entity.shared.ImportState.ImportMode;
import stroom.entity.shared.PermissionException;
import stroom.explorer.server.ExplorerActionHandler;
import stroom.explorer.shared.ExplorerConstants;
import stroom.importexport.server.ImportExportActionHandler;
import stroom.security.SecurityHelper;
import stroom.query.api.v2.DocRef;
import stroom.security.SecurityContext;
import stroom.security.shared.DocumentPermissionNames;
import stroom.util.shared.Message;
import stroom.util.shared.Severity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.stream.Stream;

public final class FSDocumentStore<D extends Document> implements ExplorerActionHandler, DocumentActionHandler<D>, ImportExportActionHandler {
    public static final String FOLDER = ExplorerConstants.FOLDER;
    private static final String FILE_EXTENSION = ".json";
    private static final Charset CHARSET = Charset.forName("UTF-8");
    public static final String KEY = "dat";

    private final Path dir;
    private final String type;
    private final SecurityContext securityContext;
    private final StripedLock stripedLock = new StripedLock();
    private final ObjectMapper mapper;
    private final Class<D> clazz;

    public FSDocumentStore(final Path dir, final String type, final Class<D> clazz, final SecurityContext securityContext) throws IOException {
        this.dir = dir;
        this.type = type;
        this.securityContext = securityContext;
        this.mapper = getMapper(true);
        this.clazz = clazz;

        Files.createDirectories(dir);
    }

    ////////////////////////////////////////////////////////////////////////
    // START OF ExplorerActionHandler
    ////////////////////////////////////////////////////////////////////////

    @Override
    public final DocRef createDocument(final String name, final String parentFolderUUID) {
        final long now = System.currentTimeMillis();
        final String userId = securityContext.getUserId();

        final D document = create(type, UUID.randomUUID().toString(), name);
        document.setVersion(UUID.randomUUID().toString());
        document.setCreateTime(now);
        document.setUpdateTime(now);
        document.setCreateUser(userId);
        document.setUpdateUser(userId);

        final D created = create(parentFolderUUID, document);
        return createDocRef(created);
    }

    @Override
    public final DocRef copyDocument(final String uuid, final String parentFolderUUID) {
        final long now = System.currentTimeMillis();
        final String userId = securityContext.getUserId();

        final D document = read(uuid);
        document.setType(type);
        document.setUuid(UUID.randomUUID().toString());
        document.setName("Copy of " + document.getName());
        document.setVersion(UUID.randomUUID().toString());
        document.setCreateTime(now);
        document.setUpdateTime(now);
        document.setCreateUser(userId);
        document.setUpdateUser(userId);

        final D created = create(parentFolderUUID, document);
        return createDocRef(created);
    }

    @Override
    public final DocRef moveDocument(final String uuid, final String parentFolderUUID) {
        final long now = System.currentTimeMillis();
        final String userId = securityContext.getUserId();

        final D document = read(uuid);

        // If we are moving folder then make sure we are allowed to create items in the target folder.
        final String permissionName = DocumentPermissionNames.getDocumentCreatePermission(type);
        if (!securityContext.hasDocumentPermission(FOLDER, parentFolderUUID, permissionName)) {
            throw new RuntimeException("You are not authorised to create items in this folder");
        }

        document.setUpdateTime(now);
        document.setUpdateUser(userId);

        final D updated = update(document);
        return createDocRef(updated);
    }

    @Override
    public DocRef renameDocument(final String uuid, final String name) {
        final long now = System.currentTimeMillis();
        final String userId = securityContext.getUserId();

        final D document = read(uuid);

        document.setName(name);
//        document.setVersion(UUID.randomUUID().toString());
        document.setUpdateTime(now);
        document.setUpdateUser(userId);

        final D updated = update(document);
        return createDocRef(updated);
    }

    @Override
    public final void deleteDocument(final String uuid) {
        final Lock lock = stripedLock.getLockForKey(uuid);
        lock.lock();
        try {
            // Check that the user has permission to delete this item.
            if (!securityContext.hasDocumentPermission(type, uuid, DocumentPermissionNames.DELETE)) {
                throw new RuntimeException("You are not authorised to delete this item");
            }

            final Path path = getPathForUUID(uuid);
            Files.delete(path);

        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    ////////////////////////////////////////////////////////////////////////
    // END OF ExplorerActionHandler
    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    // START OF DocumentActionHandler
    ////////////////////////////////////////////////////////////////////////

    @Override
    public D readDocument(final DocRef docRef) {
        return read(docRef.getUuid());
    }

    @SuppressWarnings("unchecked")
    @Override
    public D writeDocument(final D document) {
        return update(document);
    }

    @SuppressWarnings("unchecked")
    @Override
    public D forkDocument(final D document, final String docName, final DocRef destinationFolderRef) {
        String parentFolderUUID = null;
        if (destinationFolderRef != null) {
            parentFolderUUID = destinationFolderRef.getUuid();
        }

        final long now = System.currentTimeMillis();
        final String userId = securityContext.getUserId();

        document.setUuid(UUID.randomUUID().toString());
        document.setName(docName);
        document.setVersion(UUID.randomUUID().toString());
        document.setCreateTime(now);
        document.setUpdateTime(now);
        document.setCreateUser(userId);
        document.setUpdateUser(userId);

        return create(parentFolderUUID, document);

        // TODO : Call the explorer service to notify it that a new item has been created.
    }

    ////////////////////////////////////////////////////////////////////////
    // END OF DocumentActionHandler
    ////////////////////////////////////////////////////////////////////////

    ////////////////////////////////////////////////////////////////////////
    // START OF ImportExportActionHandler
    ////////////////////////////////////////////////////////////////////////

    @Override
    public DocRef importDocument(final DocRef docRef, final Map<String, String> dataMap, final ImportState importState, final ImportMode importMode) {
        D document = null;

        final String uuid = docRef.getUuid();

        try {
            final Path filePath = getPathForUUID(uuid);

            // See if a document already exists with this uuid.
            document = readDocument(docRef);
            if (document == null) {
                if (Files.isRegularFile(filePath)) {
                    throw new RuntimeException("Document already exists with uuid=" + uuid);
                }
            } else {
                if (!securityContext.hasDocumentPermission(type, uuid, DocumentPermissionNames.UPDATE)) {
                    throw new RuntimeException("You are not authorised to update this document " + docRef);
                }
            }

            if (importState.ok(importMode)) {
                final Lock lock = stripedLock.getLockForKey(document.getUuid());
                lock.lock();
                try {
                    Files.write(filePath, dataMap.get(KEY).getBytes(CHARSET));
                } finally {
                    lock.unlock();
                }

                // Now do final update.
                document = read(uuid);
                document = update(document);
            }

        } catch (final Exception e) {
            importState.addMessage(Severity.ERROR, e.getMessage());
        }

        return docRef;
    }

    @Override
    public Map<String, String> exportDocument(final DocRef docRef, final boolean omitAuditFields, final List<Message> messageList) {
        Map<String, String> data = Collections.emptyMap();

        final String uuid = docRef.getUuid();

        try {
            // Check that the user has permission to read this item.
            if (!securityContext.hasDocumentPermission(type, uuid, DocumentPermissionNames.READ)) {
                throw new PermissionException(securityContext.getUserId(), "You are not authorised to read this document " + docRef);
            } else if (!securityContext.hasDocumentPermission(type, uuid, DocumentPermissionNames.EXPORT)) {
                throw new PermissionException(securityContext.getUserId(), "You are not authorised to export this document " + docRef);
            } else {
                D document = read(uuid);
                if (document == null) {
                    throw new IOException("Unable to read " + docRef);
                }

                if (omitAuditFields) {
                    document.setCreateTime(null);
                    document.setCreateUser(null);
                    document.setUpdateTime(null);
                    document.setUpdateUser(null);
                }

                final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                mapper.writeValue(byteArrayOutputStream, document);

                data = new HashMap<>();
                data.put(KEY, new String(byteArrayOutputStream.toByteArray(), CHARSET));
            }
        } catch (final Exception e) {
            messageList.add(new Message(Severity.ERROR, e.getMessage()));
        }

        return data;
    }

    ////////////////////////////////////////////////////////////////////////
    // END OF ImportExportActionHandler
    ////////////////////////////////////////////////////////////////////////

    private DocRef createDocRef(final D document) {
        if (document == null) {
            return null;
        }

        return new DocRef(type, document.getUuid(), document.getName());
    }

    private D create(final String parentFolderUUID, final D document) {
        try {
            // Check that the user has permission to create this item.
            final String permissionName = DocumentPermissionNames.getDocumentCreatePermission(type);
            if (!securityContext.hasDocumentPermission(FOLDER, parentFolderUUID, permissionName)) {
                throw new RuntimeException("You are not authorised to create documents of type '" + type + "' in this folder");
            }

            final Path filePath = getPathForUUID(document.getUuid());

            if (Files.isRegularFile(filePath)) {
                throw new RuntimeException("Document already exists with uuid=" + document.getUuid());
            }

            final Lock lock = stripedLock.getLockForKey(document.getUuid());
            lock.lock();
            try {
                mapper.writeValue(Files.newOutputStream(filePath), document);
            } finally {
                lock.unlock();
            }

            return document;

        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private D create(final String type, final String uuid, final String name) {
        try {
            final D document = clazz.newInstance();
            document.setType(type);
            document.setUuid(uuid);
            document.setName(name);
            return document;
        } catch (final InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public D read(final String uuid) {
        final Lock lock = stripedLock.getLockForKey(uuid);
        lock.lock();
        try {
            // Check that the user has permission to read this item.
            if (!securityContext.hasDocumentPermission(type, uuid, DocumentPermissionNames.READ)) {
                throw new RuntimeException("You are not authorised to read this document");
            }

            final Path path = getPathForUUID(uuid);
            return mapper.readValue(Files.newInputStream(path), clazz);

        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    public D update(final D document) {
        final Lock lock = stripedLock.getLockForKey(document.getUuid());
        lock.lock();
        try {
            // Check that the user has permission to read this item.
            if (!securityContext.hasDocumentPermission(type, document.getUuid(), DocumentPermissionNames.UPDATE)) {
                throw new RuntimeException("You are not authorised to update this document");
            }

            final Path path = getPathForUUID(document.getUuid());
            final D existingDocument = mapper.readValue(Files.newInputStream(path), clazz);

            // Perform version check to ensure the item hasn't been updated by somebody else before we try to update it.
            if (!existingDocument.getUuid().equals(document.getUuid())) {
                throw new RuntimeException("Document has already been updated");
            }

            final long now = System.currentTimeMillis();
            final String userId = securityContext.getUserId();

            document.setVersion(UUID.randomUUID().toString());
            document.setUpdateTime(now);
            document.setUpdateUser(userId);

            mapper.writeValue(Files.newOutputStream(path), document);

            return document;

        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            lock.unlock();
        }
    }

    public Set<D> list() {
        final Set<D> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
        try (final Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(FILE_EXTENSION)).parallel().forEach(p -> {
                try (SecurityHelper securityHelper = SecurityHelper.processingUser(securityContext)) {
                    final String fileName = p.getFileName().toString();
                    final int index = fileName.indexOf(".");
                    final String uuid = fileName.substring(0, index);
                    final D document = read(uuid);
                    set.add(document);
                }
            });
        } catch (final IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return set;
    }

    private Path getPathForUUID(final String uuid) throws IOException {
        return dir.resolve(uuid + FILE_EXTENSION);
    }

    private ObjectMapper getMapper(final boolean indent) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, indent);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // Enabling default typing adds type information where it would otherwise be ambiguous, i.e. for abstract classes
//        mapper.enableDefaultTyping();
        return mapper;
    }
}