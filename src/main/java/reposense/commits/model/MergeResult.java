package reposense.commits.model;

public class MergeResult {

    private final String hash;
    private final String rootParentHash;
    private final String otherParentHash;
    private final String messageTitle;
    private final String messageBody;

    public MergeResult(String hash, String rootParentHash, String otherBranchParentHash,
            String messageTitle, String messageBody) {
        this.hash = hash;
        this.rootParentHash = rootParentHash;
        this.otherParentHash = otherBranchParentHash;
        this.messageTitle = messageTitle;
        this.messageBody = messageBody;
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

    public String getMessageBody() {
        return messageBody;
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
                && messageBody.equals(otherMergeResult.messageBody);
    }


}
