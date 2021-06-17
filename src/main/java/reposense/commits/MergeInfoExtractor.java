package reposense.commits;

import reposense.commits.model.CommitInfo;
import reposense.commits.model.MergeInfo;
import reposense.git.GitCheckout;
import reposense.git.GitLog;
import reposense.model.Author;
import reposense.model.RepoConfiguration;
import reposense.system.LogsManager;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MergeInfoExtractor {

    private static final Logger logger = LogsManager.getLogger(MergeInfoExtractor.class);
    private static final String MESSAGE_START_EXTRACTING_MERGE_INFO = "Extracting merge commits from %s (%s)...";

    public static List<MergeInfo> extractMergeInfos(RepoConfiguration config) {
        logger.info(String.format(MESSAGE_START_EXTRACTING_MERGE_INFO, config.getLocation(), config.getBranch()));

        GitCheckout.checkoutBranch(config.getRepoRoot(), config.getBranch());

        List<MergeInfo> repoMergeInfos = new ArrayList<>();

        for (Author author : config.getAuthorList()) {
            String gitLogResult = GitLog.getMergeCommits(config);
            List<MergeInfo> authorMergeInfos = parseGitLogResults(gitLogResult);
            repoMergeInfos.addAll(authorMergeInfos);
        }

        return repoMergeInfos;
    }

    private static final String INFO_STAT_SEPARATOR = "|";

    private static final Pattern TRAILING_NEWLINES_PATTERN = Pattern.compile("\n+$");

    /**
     * Parses the {@code gitLogResult} into a list of {@code CommitInfo} and returns it.
     */
    private static ArrayList<MergeInfo> parseGitLogResults(String gitLogResult) {
        ArrayList<MergeInfo> mergeInfos = new ArrayList<>();
        String[] rawMergeInfos = gitLogResult.split(GitLog.MERGE_INFO_DELIMITER);

        if (rawMergeInfos.length == 0) {
            //no log (maybe because no contribution for that file type)
            return mergeInfos;
        }

        // Starts from 1 as index 0 is always empty.
        for (int i = 1; i < rawMergeInfos.length; i++) {
            Matcher matcher = TRAILING_NEWLINES_PATTERN.matcher(rawMergeInfos[i]);
            String rawMergeInfo = matcher.replaceAll("");
            mergeInfos.add(new MergeInfo(rawMergeInfo));
        }

        Collections.reverse(mergeInfos);
        return mergeInfos;
    }

}
