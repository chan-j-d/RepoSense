package reposense.commits.model;

import java.util.ArrayList;
import java.util.List;

public class PersonalizedMerge {
    private final String hash;
    private final String messageTitle;
    private final List<CommitResult> newCommits;

    public PersonalizedMerge(String hash, String messageTitle, List<CommitResult> newCommits) {
        this.hash = hash;
        this.messageTitle = messageTitle;
        this.newCommits = new ArrayList<>(newCommits);
    }

    public String getHash() {
        return hash;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public List<CommitResult> getNewCommits() {
        return newCommits;
    }

    @Override
    public int hashCode() {
        return hash.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MergeResult)) {
            return false;
        }
        PersonalizedMerge otherMergeResult = (PersonalizedMerge) object;
        return hash.equals(otherMergeResult.hash)
                && messageTitle.equals(otherMergeResult.messageTitle);

    }
}
