package Utils;

import java.io.File;

// put all settings in one class so that we can easily modify them.
// modify your settings in a configuration class
public class Config {
    public static String dataFileName = "/Users/augenstern/IdeaProjects/comp_4035/src/data/data.txt";
    public static String updatesFileName = "/Users/augenstern/IdeaProjects/comp_4035/src/data/updates.txt";
    public static String queriesFileName = "/Users/augenstern/IdeaProjects/comp_4035/src/data/queries.txt";

    public static int count = 10000;
    public static int maxNumber = 100000;
    public static double percents = 0.9;
    public static double queryRange = 0.01;
}
