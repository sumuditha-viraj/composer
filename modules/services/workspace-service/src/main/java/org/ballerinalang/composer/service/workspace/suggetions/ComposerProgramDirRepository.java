package org.ballerinalang.composer.service.workspace.suggetions;

import org.ballerinalang.util.repository.PackageRepository;
import org.ballerinalang.util.repository.ProgramDirRepository;
import org.ballerinalang.util.repository.UserRepository;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

/**
 * Composer specific ProgramDirRepository. This is used to provide .bal file contents as InputStreams.
 */
public class ComposerProgramDirRepository extends ProgramDirRepository {
    private InputStream fileInputStream;

    public ComposerProgramDirRepository(InputStream fileInputStream, PackageRepository systemRepository,
                                        PackageRepository[] extensionRepos, UserRepository userRepository) {
        super(null, systemRepository, extensionRepos, userRepository);
        this.fileInputStream = fileInputStream;
    }

    public PackageSource loadFile(Path filePath) {
        HashMap fileStreamMap = new HashMap(1);
        Path fileName = filePath.getFileName();
        if (fileName != null) {
            fileStreamMap.put(fileName.toString(), this.fileInputStream);
        }
        return new PackageSource(Paths.get(".", new String[0]), fileStreamMap, this);
    }
}
