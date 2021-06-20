package reposense.commits;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import reposense.commits.model.*;
import reposense.model.Author;
import reposense.model.RepoConfiguration;
import reposense.parser.SinceDateArgumentType;
import reposense.report.ReportGenerator;
import reposense.util.TimeUtil;

/**
 * Uses the commit analysis results to generate the summary information of a repository.
 */
public class CommitResultAggregator {

    private static final int DAYS_IN_MS = 24 * 60 * 60 * 1000;

    /**
     * Returns the {@code CommitContributionSummary} generated from aggregating the {@code commitResults}.
     */
    public static CommitContributionSummary aggregateCommitResults(
            RepoConfiguration config, List<CommitResult> commitResults, List<MergeResult> mergeResults) {
        Date startDate;
        startDate = (config.getSinceDate().equals(SinceDateArgumentType.ARBITRARY_FIRST_COMMIT_DATE))
                ? getStartOfDate(getStartDate(commitResults), config.getZoneId())
                : config.getSinceDate();
        ReportGenerator.setEarliestSinceDate(startDate);

        Map<Author, List<AuthorDailyContribution>> authorDailyContributionsMap =
                getAuthorDailyContributionsMap(config.getAuthorDisplayNameMap().keySet(), commitResults,
                        config.getZoneId());

        Date lastDate = commitResults.size() == 0
                ? null
                : getStartOfDate(commitResults.get(commitResults.size() - 1).getTime(), config.getZoneId());

        Map<Author, Float> authorContributionVariance =
                calcAuthorContributionVariance(authorDailyContributionsMap, startDate, lastDate, config.getZoneId());

        Map<Author, List<PersonalizedMerge>> authorPersonalizedMergeMap =
                analyzePersonalizedMergeMap(mergeResults, commitResults);

        return new CommitContributionSummary(
                config.getAuthorDisplayNameMap(),
                authorDailyContributionsMap,
                authorContributionVariance,
                authorPersonalizedMergeMap);
    }

    /**
     * Calculates the contribution variance of all authors.
     */
    private static Map<Author, Float> calcAuthorContributionVariance(
            Map<Author, List<AuthorDailyContribution>> intervalContributionMaps, Date startDate, Date lastDate,
            String zoneId) {
        Map<Author, Float> result = new HashMap<>();
        for (Author author : intervalContributionMaps.keySet()) {
            List<AuthorDailyContribution> contributions = intervalContributionMaps.get(author);
            result.put(author, getContributionVariance(contributions, startDate, lastDate, zoneId));
        }
        return result;
    }

    private static float getContributionVariance(List<AuthorDailyContribution> contributions,
            Date startDate, Date lastDate, String zoneId) {
        if (contributions.size() == 0) {
            return 0;
        }
        //get mean
        float total = 0;
        long totalDays = (lastDate.getTime() - startDate.getTime()) / DAYS_IN_MS + 1;

        for (AuthorDailyContribution contribution : contributions) {
            total += contribution.getTotalContribution();
        }
        float mean = total / totalDays;

        float variance = 0;
        long currentDate = TimeUtil.getZonedDateFromSystemDate(startDate, ZoneId.of(zoneId)).getTime();
        int contributionIndex = 0;
        for (int i = 0; i < totalDays; i += 1) {
            if (contributionIndex < contributions.size()
                    && currentDate == contributions.get(contributionIndex).getDate().getTime()) {
                variance += Math.pow((mean - contributions.get(contributionIndex).getTotalContribution()), 2);
                contributionIndex += 1;
            } else {
                variance += Math.pow(mean, 2);
            }
            currentDate += DAYS_IN_MS;
        }
        return variance / totalDays;
    }

    private static Map<Author, List<AuthorDailyContribution>> getAuthorDailyContributionsMap(
            Set<Author> authorSet, List<CommitResult> commitResults, String zoneId) {
        Map<Author, List<AuthorDailyContribution>> authorDailyContributionsMap = new HashMap<>();
        authorSet.forEach(author -> authorDailyContributionsMap.put(author, new ArrayList<>()));

        Date commitStartDate = null;
        for (CommitResult commitResult : commitResults) {
            commitStartDate = getSystemStartOfDate(commitResult.getTime(), zoneId);
            Author commitAuthor = commitResult.getAuthor();

            List<AuthorDailyContribution> authorDailyContributions = authorDailyContributionsMap.get(commitAuthor);

            if (authorDailyContributions.isEmpty()
                    || !authorDailyContributions.get(authorDailyContributions.size() - 1).getDate()
                            .equals(commitStartDate)) {
                addDailyContributionForNewDate(authorDailyContributions, commitStartDate);
            }

            authorDailyContributions.get(authorDailyContributions.size() - 1).addCommitContribution(commitResult);
        }

        return authorDailyContributionsMap;
    }

    private static void addDailyContributionForNewDate(
            List<AuthorDailyContribution> authorDailyContributions, Date date) {
        authorDailyContributions.add(new AuthorDailyContribution(date));
    }

    /**
     * Get the starting point of the {@code current} date with respect to the {@code zoneId} timezone.
     */
    private static Date getStartOfDate(Date current, String zoneId) {
        if (current.equals(SinceDateArgumentType.ARBITRARY_FIRST_COMMIT_DATE)) {
            return current;
        }

        int zoneRawOffset = TimeUtil.getZoneRawOffset(ZoneId.of(zoneId));
        int systemRawOffset = TimeUtil.getZoneRawOffset(ZoneId.systemDefault());

        Calendar cal = new Calendar
                .Builder()
                .setInstant(current.getTime())
                .build();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.MILLISECOND, systemRawOffset - zoneRawOffset);
        return cal.getTime();
    }

    /**
     * Get the starting point of the {@code current} date that was given in {@code zoneId} timezone and convert into the
     * system's timezone.
     */
    private static Date getSystemStartOfDate(Date current, String zoneId) {
        int zoneRawOffset = TimeUtil.getZoneRawOffset(ZoneId.of(zoneId));
        int systemRawOffset = TimeUtil.getZoneRawOffset(ZoneId.systemDefault());

        Calendar cal = new Calendar
                .Builder()
                .setInstant(getStartOfDate(current, zoneId).getTime())
                .build();
        cal.add(Calendar.MILLISECOND, zoneRawOffset - systemRawOffset);
        return cal.getTime();
    }

    private static Date getStartDate(List<CommitResult> commitInfos) {
        Date min = new Date(Long.MIN_VALUE);
        if (!commitInfos.isEmpty()) {
            min = commitInfos.get(0).getTime();
        }
        return min;
    }

    private static Map<Author, List<PersonalizedMerge>> analyzePersonalizedMergeMap(
            List<MergeResult> mergeResults, List<CommitResult> commitResults) {

        Map<Author, List<PersonalizedMerge>> authorPersonalizedMergeMap = new HashMap<>();

        Map<String, CommitResult> hashToCommitObjectMap = commitResults.stream()
                .collect(Collectors.toMap(CommitResult::getHash, x -> x));

        for (MergeResult currentMerge : mergeResults) {
            List<String> commitHashes = currentMerge.getNewCommits();

            Map<Author, List<CommitResult>> perMergeAuthorToCommitMap = new HashMap<>();

            commitHashes.stream()
                    .forEach(commitHash -> {
                        CommitResult commitResult = hashToCommitObjectMap.get(commitHash);
                        if (commitResult == null) return;
                        Author author = commitResult.getAuthor();
                        perMergeAuthorToCommitMap.compute(author, (k, v) -> {
                            if (v == null) {
                                return new ArrayList<>(List.of(commitResult));
                            } else {
                                v.add(commitResult);
                                return v;
                            }
                        });
                    });

            perMergeAuthorToCommitMap.forEach((author, list) ->
                authorPersonalizedMergeMap.compute(author, (k, v) -> {
                    PersonalizedMerge newPersonalizedMerge = new PersonalizedMerge(
                            currentMerge.getHash(),
                            currentMerge.getMessageTitle(),
                            list,
                            list.get(list.size() - 1).getTime(),
                            list.get(0).getTime());
                    if (v == null) {
                        return new ArrayList<>(List.of(newPersonalizedMerge));
                    } else {
                        v.add(newPersonalizedMerge);
                        return v;
                    }
                })
            );

        }

        return authorPersonalizedMergeMap;
    }

}
