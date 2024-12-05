package at.ac.tuwien.model.change.management.testutils;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;

public final class TestUtils {

        private TestUtils() {
        }

        public static RecursiveComparisonConfiguration recursiveConfigurationComparison() {
            var recursiveComparisonConfiguration = new RecursiveComparisonConfiguration();
            // for some reason, these Record classes trouble AssertJ's recursive comparison
            recursiveComparisonConfiguration.ignoreFields(
                    "version",
                    "models.nodes.relations.target",
                    "models.nodes.relations.startPoint",
                    "models.nodes.relations.endPoint"
            );
            return recursiveComparisonConfiguration;
        }
}
