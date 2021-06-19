package reposense.commits.model;

import java.util.List;
import java.util.Map;

import reposense.model.Author;

/**
 * Stores the consolidated results of commit analysis.
 */
public class CommitContributionSummary {
    private final Map<Author, List<AuthorDailyContribution>> authorDailyContributionsMap;
    private final Map<Author, Float> authorContributionVariance;
    private final Map<Author, String> authorDisplayNameMap;
    private final Map<Author, List<PersonalizedMerge>> authorPersonalizedMergeMap;

    public CommitContributionSummary(
            Map<Author, String> authorDisplayNameMap,
            Map<Author, List<AuthorDailyContribution>> authorDailyContributionsMap,
            Map<Author, Float> authorContributionVariance,
            Map<Author, List<PersonalizedMerge>> authorPersonalizedMergeMap) {
        this.authorDisplayNameMap = authorDisplayNameMap;
        this.authorDailyContributionsMap = authorDailyContributionsMap;
        this.authorContributionVariance = authorContributionVariance;
        this.authorPersonalizedMergeMap = authorPersonalizedMergeMap;
    }

    public Map<Author, String> getAuthorDisplayNameMap() {
        return authorDisplayNameMap;
    }

    public Map<Author, List<AuthorDailyContribution>> getAuthorDailyContributionsMap() {
        return authorDailyContributionsMap;
    }

    public Map<Author, Float> getAuthorContributionVariance() {
        return authorContributionVariance;
    }

    public Map<Author, List<PersonalizedMerge>> getAuthorPersonalizedMergeMap() {
        return authorPersonalizedMergeMap;
    }

}
