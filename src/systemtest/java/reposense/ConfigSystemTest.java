package reposense;

import static org.apache.tools.ant.types.Commandline.translateCommandline;

import static reposense.util.TestUtil.loadResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import reposense.git.GitVersion;
import reposense.model.AuthorConfiguration;
import reposense.model.CliArguments;
import reposense.model.ConfigCliArguments;
import reposense.model.GroupConfiguration;
import reposense.model.RepoConfiguration;
import reposense.model.ReportConfiguration;
import reposense.parser.ArgsParser;
import reposense.parser.AuthorConfigCsvParser;
import reposense.parser.GroupConfigCsvParser;
import reposense.parser.RepoConfigCsvParser;
import reposense.parser.ReportConfigJsonParser;
import reposense.parser.SinceDateArgumentType;
import reposense.report.ErrorSummary;
import reposense.report.ReportGenerator;
import reposense.util.FileUtil;
import reposense.util.InputBuilder;
import reposense.util.TestUtil;

public class ConfigSystemTest {
    private static final String FT_TEMP_DIR = "ft_temp";
    private static final String DUMMY_ASSETS_DIR = "dummy";
    private static final String EXPECTED_FOLDER = "expected";
    private static final List<String> TESTING_FILE_FORMATS = Arrays.asList("java", "adoc");
    private static final String TEST_REPORT_GENERATED_TIME = "Tue Jul 24 17:45:15 SGT 2018";
    private static final String TEST_REPORT_GENERATION_TIME = "15 second(s)";
    private static final String TEST_TIME_ZONE = "Asia/Singapore";

    private static boolean haveNormallyClonedRepo = false;

    @Before
    public void setUp() throws Exception {
        FileUtil.deleteDirectory(FT_TEMP_DIR);
        ErrorSummary.getInstance().clearErrorSet();
        AuthorConfiguration.setHasAuthorConfigFile(AuthorConfiguration.DEFAULT_HAS_AUTHOR_CONFIG_FILE);
    }

    @After
    public void tearDown() throws Exception {
        FileUtil.deleteDirectory(FT_TEMP_DIR);
    }

    /**
     * System test with a specified until date and a {@link SinceDateArgumentType#FIRST_COMMIT_DATE_SHORTHAND}
     * since date to capture from the first commit.
     */
    @Test
    public void testSinceBeginningDateRange() throws Exception {
        System.out.println(Paths.get(".").toAbsolutePath());
        runTest("--config ./build/resources/systemtest/ -f java adoc " +
                        getInputWithDates(SinceDateArgumentType.FIRST_COMMIT_DATE_SHORTHAND, "2/3/2019"),
                false, false,  FT_TEMP_DIR, "sinceBeginningDateRange/expected");
    }




    private String getInputWithUntilDate(String untilDate) {
        return String.format("--until %s", untilDate);
    }

    private String getInputWithDates(String sinceDate, String untilDate) {
        return String.format("--since %s --until %s", sinceDate, untilDate);
    }

    /**
     * Generates the testing report and then compared it with the expected report
     * Re-generates a normal report after the testing finished if the first report is shallow-cloned
     */
    private void runTest(String testArgs, boolean findPreviousAuthors,
            boolean shallowCloning, String pathToResource) throws Exception {
        boolean isGitVersionInsufficient = findPreviousAuthors
                && !GitVersion.isGitVersionSufficientForFindingPreviousAuthors();
        Assert.assertFalse("Git version 2.23.0 and above necessary to run test", isGitVersionInsufficient);
        String[] testArgsArray = testArgs.split(" ");
        RepoSense.main(testArgsArray);
        Path actualFiles = loadResource(getClass(), pathToResource);
        verifyAllJson(actualFiles, FT_TEMP_DIR + "/reposense-report");
        haveNormallyClonedRepo = !shallowCloning;
    }

    /**
     * Generates the testing report and then compared it with the expected report
     * Re-generates a normal report after the testing finished if the first report is shallow-cloned
     */
    private void runTest(String testArgs, boolean findPreviousAuthors, boolean shallowCloning,
            String outputDirectory, String pathToResource) throws Exception {
        boolean isGitVersionInsufficient = findPreviousAuthors
                && !GitVersion.isGitVersionSufficientForFindingPreviousAuthors();
        Assert.assertFalse("Git version 2.23.0 and above necessary to run test", isGitVersionInsufficient);
        String[] testArgsArray = testArgs.split(" ");
        String[] mainArgs = Arrays.copyOf(testArgsArray, testArgsArray.length + 2);
        mainArgs[testArgsArray.length] = "--output";
        mainArgs[testArgsArray.length + 1] = outputDirectory;
        RepoSense.main(mainArgs);
        Path actualFiles = loadResource(getClass(), pathToResource);
        verifyAllJson(actualFiles, FT_TEMP_DIR + "/reposense-report");
        haveNormallyClonedRepo = !shallowCloning;
    }

    /**
     * Verifies all JSON files in {@code actualDirectory} with {@code expectedDirectory}
     */
    private void verifyAllJson(Path expectedDirectory, String actualRelative) {
        try (Stream<Path> pathStream = Files.list(expectedDirectory)) {
            for (Path filePath : pathStream.collect(Collectors.toList())) {
                if (Files.isDirectory(filePath)) {
                    verifyAllJson(filePath, actualRelative);
                }
                if (filePath.toString().endsWith(".json")) {
                    String relativeDirectory = filePath.toAbsolutePath().toString().split(EXPECTED_FOLDER)[1];
                    assertJson(filePath, relativeDirectory, actualRelative);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * Asserts the correctness of given JSON file.
     */
    private void assertJson(Path expectedJson, String expectedPosition, String actualRelative) {
        Path actualJson = Paths.get(actualRelative, expectedPosition);
        Assert.assertTrue(Files.exists(actualJson));
        try {
            Assert.assertTrue(TestUtil.compareFileContents(expectedJson, actualJson));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
