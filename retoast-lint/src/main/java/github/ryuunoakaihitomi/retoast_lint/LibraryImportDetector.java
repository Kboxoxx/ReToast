package github.ryuunoakaihitomi.retoast_lint;

import com.android.sdklib.AndroidVersion;
import com.android.tools.lint.detector.api.Category;
import com.android.tools.lint.detector.api.Detector;
import com.android.tools.lint.detector.api.GradleContext;
import com.android.tools.lint.detector.api.GradleScanner;
import com.android.tools.lint.detector.api.Implementation;
import com.android.tools.lint.detector.api.Issue;
import com.android.tools.lint.detector.api.Location;
import com.android.tools.lint.detector.api.Project;
import com.android.tools.lint.detector.api.Scope;
import com.android.tools.lint.detector.api.Severity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class LibraryImportDetector extends Detector implements GradleScanner {

    private static final Implementation IMPLEMENTATION = new Implementation(
            LibraryImportDetector.class, Scope.GRADLE_SCOPE
    );

    static final Issue ISSUE_ENV = Issue.create(
            "ReToastImportEnvironment",
            "ReToast in here is unusable.",
            "You must import ReToast in a Android project that the minSdkVersion < 29."
            , Category.USABILITY, 10, Severity.ERROR,
            IMPLEMENTATION)
            .addMoreInfo("https://github.com/ryuunoakaihitomi/ReToast")
            .setEnabledByDefault(true);

    static final Issue ISSUE_WAY = Issue.create(
            "ReToastImportWay",
            "Please use \"runtimeOnly\" to import ReToast.",
            "It can avoid calling the initializer of ReToast accidentally on programming."
            , Category.A11Y, 7, Severity.WARNING,
            IMPLEMENTATION)
            .addMoreInfo("https://developer.android.com/studio/build/dependencies#dependency_configurations");


    @SuppressWarnings("SpellCheckingInspection")
    private static final String LIBRARY_GROUP_ID = "github.ryuunoakaihitomi.retoast";
//    private static final String LIBRARY_GROUP_ID = ":retoast";

    @Override
    public void checkDslPropertyAssignment(@NotNull GradleContext context, @NotNull String property, @NotNull String value, @NotNull String parent, @Nullable String parentParent, @NotNull Object propertyCookie, @NotNull Object valueCookie, @NotNull Object statementCookie) {
        if (parent.equals("dependencies")) {

            // Which library?
//            System.out.println("library value = " + value);
            if (value.contains(LIBRARY_GROUP_ID)) {

                // debugRuntimeOnly etc.
                //noinspection SpellCheckingInspection
                if (!property.toLowerCase().contains("runtimeonly")) {
                    Location location = context.getLocation(propertyCookie);
                    context.report(ISSUE_WAY, location,
                            "...runTimeOnly <ReToast>",
                            // Because composite() on replace() doesn't seems to work,
                            // so we have to only process this most common situation.
                            fix().name("Replace with \"runtimeOnly\"").replace()
                                    .range(location)
                                    .text("implementation")
                                    .with("runtimeOnly")
                                    .build());
                } else {
                    Project project = context.getProject();
                    boolean isAndroidProject = project.isAndroidProject();
                    int minSdk = project.getMinSdk();
                    Location location = context.getLocation(statementCookie);
                    if (!isAndroidProject || minSdk >= AndroidVersion.VersionCodes.Q) {
                        context.report(ISSUE_ENV, location,
                                (isAndroidProject ? "minSdk = " + minSdk + " (>= Android Q)" : "Non Android project") +
                                        " should not use ReToast.",
                                fix().name("Remove ReToast library")
                                        .replace()
                                        .all()
                                        .range(location)
                                        .with("")
                                        .build());
                    }
                }
            }
        }
    }
}
