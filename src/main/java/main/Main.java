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

        String h = cmd.getOptionValue("hc");
        String[] p = cmd.getOptionValues("pc");
        String b = cmd.getOptionValue("bc");
        String ssm = cmd.getOptionValue("ssm");
        String tpr = cmd.getOptionValue("tpr");
        String cn = cmd.getOptionValue("cn");
        String m = cmd.getOptionValue("m");
        String gp = cmd.getOptionValue("gp");
        String mp = cmd.getOptionValue("mp");

        return new String[]{h, p[0], p[1], b, ssm, tpr, cn, m, gp, mp};
    }

    private void createOptions() {
        options = new Options();
        Option headOption = Option.builder("hc").argName("head")
                .required().hasArg().desc("the head commit")
                .build();

        Option parentsOption = Option.builder("pc").argName("parents")
                .required().hasArgs().valueSeparator(' ').desc("the parents commits")
                .build();

        Option baseOption = Option.builder("bc").argName("base")
                .required().hasArg().desc("the base commit")
                .build();

        Option ssmPathOption = Option.builder("ssm").argName("mergerPath")
                .required().hasArg().desc("path to ssm folder")
                .build();

        Option targetProjectRootOption = Option.builder("tpr").argName("targetProjectRoot").hasArg()
                .required().desc("path to target project root folder")
                .build();

        Option classNameOption = Option.builder("cn").argName("className").hasArg()
                .required().desc("packagename to main class. Eg: org.example.Main")
                .build();

        Option mainMethodOption = Option.builder("m").argName("mainMethod").hasArg().desc("name of the main method. Eg: main")
                .build();

        Option gradlePathOption = Option.builder("gp").argName("gradlePath")
                .required().hasArg().desc("path to gradle bin")
                .build();


        Option mavenPathOption = Option.builder("mp").argName("mavenPath").hasArg()
                .required().desc("path to maven bin")
                .build();


        options.addOption(headOption);
        options.addOption(parentsOption);
        options.addOption(baseOption);
        options.addOption(ssmPathOption);
        options.addOption(targetProjectRootOption);
        options.addOption(classNameOption);
        options.addOption(mainMethodOption);
        options.addOption(gradlePathOption);
        options.addOption(mavenPathOption);
    }

}
