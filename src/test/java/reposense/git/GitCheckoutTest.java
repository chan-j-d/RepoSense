package reposense.git;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;

import org.junit.Assert;
import org.junit.Test;

import reposense.git.exception.CommitNotFoundException;
import reposense.template.GitTestTemplate;
import reposense.util.TestUtil;


public class GitCheckoutTest extends GitTestTemplate {

    @Test
    public void checkout_validBranch_success() {
        GitCheckout.checkout(config.getRepoRoot(), "test");
        Path branchFile = Paths.get(config.getRepoRoot(), "inTestBranch.java");
        Assert.assertTrue(Files.exists(branchFile));
    }

    @Test
    public void checkoutBranchTest() {
        Path branchFile = Paths.get(config.getRepoRoot(), "inTestBranch.java");
        Assert.assertFalse(Files.exists(branchFile));

        GitCheckout.checkoutBranch(config.getRepoRoot(), "test");
        Assert.assertTrue(Files.exists(branchFile));
    }

    @Test
    public void checkoutHashTest() {
        Path newFile = Paths.get(config.getRepoRoot(), "newFile.java");
        Assert.assertTrue(Files.exists(newFile));

        GitCheckout.checkout(config.getRepoRoot(), FIRST_COMMIT_HASH);
        Assert.assertFalse(Files.exists(newFile));
    }

    @Test
    public void checkoutToDate_validDate_success() throws Exception {
        Path newFile = Paths.get(config.getRepoRoot(), "newFile.java");
        Assert.assertTrue(Files.exists(newFile));

        LocalDateTime untilDate = TestUtil.getUntilDate(2018, Month.FEBRUARY.getValue(), 6);
        GitCheckout.checkoutDate(config.getRepoRoot(), config.getBranch(), untilDate, ZoneId.of(config.getZoneId()));
        Assert.assertFalse(Files.exists(newFile));
    }

    @Test(expected = CommitNotFoundException.class)
    public void checkoutToDate_invalidDate_throwsEmptyCommitException() throws Exception {
        LocalDateTime untilDate = TestUtil.getUntilDate(2015, Month.FEBRUARY.getValue(), 6);
        GitCheckout.checkoutDate(config.getRepoRoot(), config.getBranch(), untilDate, ZoneId.of(config.getZoneId()));
    }
}
