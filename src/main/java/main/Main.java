package main;

import org.apache.commons.cli.*;

public class Main {

    private Options options;
    public static void main(String[] args) {
        Main m = new Main();
        m.createOptions();
        try {
            String[] result = m.parseCommandLine(args);
            StaticAnalysisMerge analysisMerge = new StaticAnalysisMerge(result);
            analysisMerge.run();
        } catch (ParseException e) {
            System.out.println("Error: " + e.getMessage());
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java Main", m.options);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String[] parseCommandLine(String[] args) throws ParseException {
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String h = cmd.getOptionValue("h");
        String[] p = cmd.getOptionValues("p");
        String b = cmd.getOptionValue("b");
        String ssm = cmd.getOptionValue("ssm");
        String gp = cmd.getOptionValue("gp");
        String mvp = cmd.getOptionValue("mvp");
        String mp = cmd.getOptionValue("mp");

        return new String[]{h, p[0], p[1], b, ssm, gp, mvp, mp};
    }

    private void createOptions() {
        options = new Options();
        Option headOption = Option.builder("h").argName("head")
                .required().hasArg().desc("the head commit")
                .build();

        Option parentsOption = Option.builder("p").argName("parents")
                .required().hasArgs().valueSeparator(' ').desc("the parents commits")
                .build();

        Option baseOption = Option.builder("b").argName("base")
                .required().hasArg().desc("the base commit")
                .build();

        Option ssmPathOption = Option.builder("ssm").argName("mergerPath")
                .required().hasArg().desc("path to ssm folder")
                .build();

        Option gradlePathOption = Option.builder("gp").argName("gradlePath")
                .required().hasArg().desc("path to gradle bin")
                .build();


        Option mavenPathOption = Option.builder("mvp").argName("mavenPath").hasArg()
                .required().desc("path to maven bin")
                .build();

        Option mergePathOption = Option.builder("mp").argName("mavenPath").hasArg()
                .required().desc("path to marge folder")
                .build();

        options.addOption(headOption);
        options.addOption(parentsOption);
        options.addOption(baseOption);
        options.addOption(ssmPathOption);
        options.addOption(gradlePathOption);
        options.addOption(mavenPathOption);
        options.addOption(mergePathOption);
    }

}
