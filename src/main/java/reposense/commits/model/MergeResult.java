package reposense.commits.model;

import java.util.ArrayList;
import java.util.List;

public class MergeResult {

    private final String hash;
    private final String rootParentHash;
    private final String otherParentHash;
    private final String messageTitle;
    private final List<String> newCommits;

    public MergeResult(String hash, String rootParentHash, String otherBranchParentHash,
            String messageTitle, List<String> newCommits) {
        this.hash = hash;
        this.rootParentHash = rootParentHash;
        this.otherParentHash = otherBranchParentHash;
        this.messageTitle = messageTitle;
        this.newCommits = new ArrayList<>(newCommits);
    }

    public String getHash() {
        return hash;
    }

    public String getRootParentHash() {
        return rootParentHash;
    }

    public String getOtherParentHash() {
        return otherParentHash;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public List<String> getNewCommits() {
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

        MergeResult otherMergeResult = (MergeResult) object;
        return hash.equals(otherMergeResult.hash)
                && rootParentHash.equals(otherMergeResult.rootParentHash)
                && otherParentHash.equals(otherMergeResult.otherParentHash)
                && messageTitle.equals(otherMergeResult.messageTitle)
                && newCommits.equals(otherMergeResult.newCommits);
    }

    public String toString() {
        return hash + "\n" + rootParentHash + " " + otherParentHash + "\n" + messageTitle + "\n" + newCommits;
    }

}
