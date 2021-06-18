package reposense.commits;

import reposense.commits.model.MergeInfo;
import reposense.commits.model.MergeResult;
import reposense.git.GitRevList;
import reposense.model.RepoConfiguration;
import reposense.system.LogsManager;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class MergeInfoAnalyzer {

    private static final Logger logger = LogsManager.getLogger(MergeInfoAnalyzer.class);
    private static final String MESSAGE_START_ANALYZING_MERGE_INFO = "Analyzing merge info for %s (%s)...";

    private static final int INDEX_HASH = 0;
    private static final int INDEX_PARENT_HASHES = 1;
    private static final int INDEX_MESSAGE = 2;
    private static final int INDEX_ROOT_PARENT = 0;
    private static final int INDEX_OTHER_PARENT = 1;

    public static List<MergeResult> analyzeMerges(List<MergeInfo> mergeInfos, RepoConfiguration config) {
        logger.info(String.format(MESSAGE_START_ANALYZING_MERGE_INFO, config.getLocation(), config.getBranch()));

        return mergeInfos.stream()
                .map(mergeInfo -> analyzeMerge(mergeInfo, config))
                .collect(Collectors.toList());
    }

    public static MergeResult analyzeMerge(MergeInfo mergeInfo, RepoConfiguration config) {
        String info = mergeInfo.getInfo();
        String[] elements = info.split("\n");
        String hash = elements[INDEX_HASH];

        String[] parentHashes = elements[INDEX_PARENT_HASHES].split(" ");
        String mainParentHash = parentHashes[INDEX_ROOT_PARENT];
        String otherParentHash = parentHashes[INDEX_OTHER_PARENT];

        String title = elements[INDEX_MESSAGE];
        List<String> newCommits = getNewCommits(config, mainParentHash, otherParentHash);

        return new MergeResult(hash, mainParentHash, otherParentHash, title, newCommits);

    }

    private static List<String> getNewCommits(RepoConfiguration config,
            String mainParentHash, String otherParentHash) {
        return GitRevList.getCommitsDifferenceBetween(config.getRepoRoot(), mainParentHash, otherParentHash);
    }



}
