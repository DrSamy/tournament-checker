package org;

import lombok.val;
import org.apache.commons.cli.*;
import org.model.PlayerRank;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.EventUtils.dateAvailableChecker;
import static org.EventUtils.dateAvailableFinder;

public class App {
    public static void main( String[] args ) throws IOException {
        val options = new Options();
        val validPlayerRanks = Arrays.stream(PlayerRank.values())
                .map(Enum::name)
                .collect(Collectors.joining(", "));

        val inputMaxPlayerRank = Option.builder("M")
                .longOpt("max")
                .hasArg()
                .required(true)
                .desc("Classement maximum du tournoi (" + validPlayerRanks + ")")
                .build();
        options.addOption(inputMaxPlayerRank);

        val inputMinPlayerRank = Option.builder("m")
                .longOpt("min")
                .hasArg()
                .required(true)
                .desc("Classement minimum du tournoi (" + validPlayerRanks + ")")
                .build();
        options.addOption(inputMinPlayerRank);

        val inputPostalCode = Option.builder("p")
                .longOpt("postal")
                .hasArg()
                .required(true)
                .desc("Code postal de la ville où est organisé le tournoi")
                .build();
        options.addOption(inputPostalCode);

        val inputDateStart = Option.builder("s")
                .longOpt("start")
                .hasArg()
                .desc("Date de début au format YYYY-MM-DD")
                .build();
        options.addOption(inputDateStart);

        val inputDateEnd = Option.builder("e")
                .longOpt("end")
                .hasArg()
                .desc("Date de fin au format YYYY-MM-DD")
                .build();
        options.addOption(inputDateEnd);

        val inputWeekend = Option.builder("w")
                .longOpt("weekend")
                .desc("Vérification des weekend encore disponibles")
                .build();
        options.addOption(inputWeekend);

        val parser = new DefaultParser();
        val formatter = new HelpFormatter();

        try {
            val cmd = parser.parse(options, args);

            val inputMaxPlayerRankStr = cmd.getOptionValue("max").toUpperCase();
            val maxPlayerRank = PlayerRank.valueOf(inputMaxPlayerRankStr);

            val inputMinPlayerRankStr = cmd.getOptionValue("min").toUpperCase();
            val minPlayerRank = PlayerRank.valueOf(inputMinPlayerRankStr);

            val inputDateStartStr = cmd.getOptionValue("start");
            val dateStart = LocalDate.parse(inputDateStartStr, DateTimeFormatter.ISO_LOCAL_DATE);

            val inputDateEndStr = cmd.getOptionValue("end");
            val dateEnd = LocalDate.parse(inputDateEndStr, DateTimeFormatter.ISO_LOCAL_DATE);

            val postalCode = cmd.getOptionValue("postal");

            if (cmd.hasOption("w")) {
                dateAvailableFinder(maxPlayerRank, minPlayerRank, postalCode);
            } else {
                dateAvailableChecker(maxPlayerRank, minPlayerRank, dateStart, dateEnd, postalCode, false, null);
            }
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("MandatoryArgsApp", options);
            System.exit(1);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
