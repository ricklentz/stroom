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

package stroom.streamstore.fs;

import stroom.node.shared.Volume;
import stroom.streamstore.shared.Stream;
import stroom.streamstore.shared.StreamType;
import stroom.streamstore.shared.StreamType.FileStoreType;
import stroom.streamstore.shared.StreamVolume;
import stroom.util.date.DateUtil;
import stroom.util.io.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FileSystemStreamTypeUtil {
    /**
     * We use this rather than the File.separator as we need to be standard
     * across Windows and UNIX.
     */
    private static final String SEPERATOR_CHAR = "/";
    private static final String FILE_SEPERATOR_CHAR = "=";
    private static final String STORE_NAME = "store";

    private static String createFilePathBase(final Volume volume, final Stream stream, final StreamType streamType) {
        return volume.getPath() +
                SEPERATOR_CHAR +
                STORE_NAME +
                SEPERATOR_CHAR +
                getDirectory(stream, streamType) +
                SEPERATOR_CHAR +
                getBaseName(stream);
    }

    /**
     * Return back a input stream for a given stream type and file.
     */
    public static InputStream getInputStream(final StreamType streamType, final Path file) throws IOException {
        if (streamType == null) {
            throw new IllegalArgumentException("Must Have a non-null stream type");
        }
        if (FileStoreType.bgz.equals(streamType.getFileStoreType())) {
            return new BlockGZIPInputFile(file);
        }
        return new UncompressedInputStream(file, streamType.isStreamTypeLazy());
    }

    /**
     * <p>
     * Find all existing child files of this parent.
     * </p>
     */
    private static List<Path> findChildStreamFileList(final Path parent) {
        List<Path> kids = new ArrayList<>();
        for (StreamType type : StreamType.initialValues()) {
            if (type.isStreamTypeChild()) {
                Path child = createChildStreamFile(parent, type);
                if (Files.isRegularFile(child)) {
                    kids.add(child);
                }
            }
        }
        return kids;
    }

    /**
     * Return back a output stream for a given stream type and file.
     */
    public static OutputStream getOutputStream(final StreamType streamType, final Set<Path> fileSet)
            throws IOException {
        if (streamType == null) {
            throw new IllegalArgumentException("Must Have a non-null stream type");
        }
        IOException ioEx = null;
        Set<OutputStream> outputStreamSet = new HashSet<>();
        if (FileStoreType.bgz.equals(streamType.getFileStoreType())) {
            for (Path file : fileSet) {
                try {
                    outputStreamSet.add(new BlockGZIPOutputFile(file));
                } catch (IOException e) {
                    ioEx = e;
                }
            }
        } else {
            for (Path file : fileSet) {
                try {
                    outputStreamSet.add(new LockingFileOutputStream(file, streamType.isStreamTypeLazy()));
                } catch (IOException e) {
                    ioEx = e;
                }
            }
        }
        if (ioEx != null) {
            throw ioEx;
        }
        return ParallelOutputStream.createForStreamSet(outputStreamSet);
    }

    /**
     * Create a child file for a parent.
     */
    public static Path createChildStreamFile(final StreamVolume streamVolume, final StreamType streamType) {
        final String path = createFilePathBase(streamVolume.getVolume(), streamVolume.getStream(),
                streamVolume.getStream().getStreamType()) +
                "." +
                streamVolume.getStream().getStreamType().getExtension() +
                "." +
                streamType.getExtension() +
                "." +
                String.valueOf(streamType.getFileStoreType());
        return Paths.get(path);
    }

    /**
     * <p>
     * Build a file base name.
     * </p>
     * <p>
     * <p>
     * [feedid]_[streamid]
     * </p>
     */
    public static String getBaseName(Stream stream) {
        if (!stream.isPersistent()) {
            throw new RuntimeException("Can't build a file path until the meta data is persistent");
        }
        return stream.getFeed().getId() +
                FILE_SEPERATOR_CHAR +
                FileSystemPrefixUtil.padId(stream.getId());
    }

    public static String getDirectory(Stream stream, StreamType streamType) {
        StringBuilder builder = new StringBuilder();
        builder.append(streamType.getPath());
        builder.append(FileSystemStreamTypeUtil.SEPERATOR_CHAR);
        String utcDate = DateUtil.createNormalDateTimeString(stream.getCreateMs());
        builder.append(utcDate.substring(0, 4));
        builder.append(FileSystemStreamTypeUtil.SEPERATOR_CHAR);
        builder.append(utcDate.substring(5, 7));
        builder.append(FileSystemStreamTypeUtil.SEPERATOR_CHAR);
        builder.append(utcDate.substring(8, 10));
        String idPath = FileSystemPrefixUtil.buildIdPath(FileSystemPrefixUtil.padId(stream.getId()));
        if (idPath != null) {
            builder.append(FileSystemStreamTypeUtil.SEPERATOR_CHAR);
            builder.append(idPath);
        }
        return builder.toString();
    }

    /**
     * Create a child file set for a parent file set.
     */
    static Set<Path> createChildStreamPath(final Set<Path> parentSet, final StreamType streamType) {
        Set<Path> childSet = new HashSet<>();
        childSet.addAll(parentSet.stream().map(parent -> createChildStreamFile(parent, streamType))
                .collect(Collectors.toList()));
        return childSet;
    }

    /**
     * Find all the descendants to this file.
     */
    public static List<Path> findAllDescendantStreamFileList(final Path parent) {
        List<Path> rtn = new ArrayList<>();
        List<Path> kids = findChildStreamFileList(parent);
        for (Path kid : kids) {
            rtn.add(kid);
            rtn.addAll(findAllDescendantStreamFileList(kid));
        }
        return rtn;
    }

    /**
     * Return a File IO object.
     */
    public static Path createRootStreamFile(final Volume volume, final Stream stream, final StreamType streamType) {
        final String path = createFilePathBase(volume, stream, streamType) +
                "." +
                streamType.getExtension() +
                "." +
                String.valueOf(streamType.getFileStoreType());
        return Paths.get(path);
    }

    /**
     * Create a child file for a parent.
     */
    static Path createChildStreamFile(final Path parent, final StreamType streamType) {
        StringBuilder builder = new StringBuilder(FileUtil.getCanonicalPath(parent));
        // Drop ".dat" or ".bgz"
        builder.setLength(builder.lastIndexOf("."));
        builder.append(".");
        builder.append(streamType.getExtension());
        builder.append(".");
        builder.append(String.valueOf(streamType.getFileStoreType()));
        return Paths.get(builder.toString());
    }
}
