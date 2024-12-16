package org;

import lombok.val;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.util.StringUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.model.Event;
import org.model.EventType;
import org.model.Season;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import static org.DateUtils.getLastYear;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.model.Department.getByCode;
import static org.model.EventType.*;
import static org.model.PlayerRank.getRangeFromString;
import static org.model.TournamentRank.getTournamentRankByPlayerRank;

public class ExcelUtils {

    private static final int MONTH_CELL = 0;
    private static final int DAY_CELL = 2;
    private static final int ICN_TITLE_CELL = 5;
    private static final int ICR_TITLE_CELL = 6;
    private static final int TOURNAMENT_PENDING_TITLE_CELL = 9;
    private static final int TOURNAMENT_TITLE_CELL = 10;
    private static final int RANK_CELL = 12;

    private static final int HEADER_LINES = 2;

    public static List<Event> getEvents(String file) throws IOException {
        val sheet = getSheet(file);
        List<Event> nextEvents = new ArrayList<>();

        val nextTournaments = getEventsByType(sheet, TOURNAMENT);
        nextEvents.addAll(nextTournaments);

        val nextICR = getEventsByType(sheet, ICR);
        nextEvents.addAll(nextICR);

        val nextICN = getEventsByType(sheet, ICN);
        nextEvents.addAll(nextICN);

        val nextPending = getEventsByType(sheet, TOURNAMENT_PENDING);
        nextEvents.addAll(nextPending);

        return nextEvents;
    }

    private static List<Event> getEventsByType(Sheet sheet, EventType eventType) {
        return StreamSupport.stream(sheet.spliterator(), false)
                .skip(HEADER_LINES)
                .filter(row -> hasContent(eventType, row))
                .map(row -> mapper(sheet, row, eventType))
                .filter(e -> LocalDate.now().isBefore(e.getDateEnd()))
                .toList();
    }

    private static boolean hasContent(EventType type, Row row) {
        val value = row.getCell(getTitleCell(type)).getStringCellValue();
        return StringUtil.isNotBlank(value) && switch(type) {
            case TOURNAMENT, CHAMPIONSHIP -> !value.contains(getLastYear());
            case ICR -> value.contains("PN") || value.contains("ICR");
            case ICN -> value.contains("ICN");
            case TOURNAMENT_PENDING -> true;
        };
    }

    private static Event mapper(Sheet sheet, Row row, EventType type) {
        val titleCell = getTitleCell(type);
        val title = row.getCell(titleCell).getStringCellValue();
        val mergedRows = getMergedRowsForCell(sheet, row.getCell(titleCell));

        return switch(type) {
            case TOURNAMENT, CHAMPIONSHIP -> mapperTournament(sheet, title, mergedRows);
            case TOURNAMENT_PENDING -> mapperInterclub(sheet, title, mergedRows, type);
            case ICR, ICN -> mapperInterclub(sheet, title, mergedRows, type);
        };
    }

    private static int getTitleCell(EventType type) {
        return switch(type) {
            case TOURNAMENT, CHAMPIONSHIP -> TOURNAMENT_TITLE_CELL;
            case TOURNAMENT_PENDING -> TOURNAMENT_PENDING_TITLE_CELL;
            case ICR -> ICR_TITLE_CELL;
            case ICN -> ICN_TITLE_CELL;
        };
    }

    private static Event mapperTournament(Sheet sheet, String title, List<Integer> mergedRows) {
        val dates = getDates(sheet, mergedRows);

        val type = title.toUpperCase().contains("CHAMPIONNAT") ? EventType.CHAMPIONSHIP : EventType.TOURNAMENT;
        val ranks = getRangeFromString(sheet.getRow(mergedRows.getFirst()).getCell(RANK_CELL).getStringCellValue(), isChampionShip(type));

        return Event.builder()
                .title(clearTitle(title))
                .dateStart(dates.getFirst())
                .dateEnd(dates.getLast())
                .playerRanks(ranks)
                .department(getByCode(getDepartmentNumber(title)))
                .type(type)
                .tournamentRank(getTournamentRankByPlayerRank(ranks))
                .build();
    }

    private static Event mapperInterclub(Sheet sheet, String title, List<Integer> mergedRows, EventType type) {
        val dates = getDates(sheet, mergedRows);

        return Event.builder()
                .title(title)
                .dateStart(dates.getFirst())
                .dateEnd(dates.getLast())
                .type(type)
                .build();
    }

    private static List<LocalDate> getDates (Sheet sheet, List<Integer> mergedRows) {
        List<LocalDate> dates = new ArrayList<>();
        for (val i : mergedRows) {
            val monthCell = getMergedRegionValue(sheet, i, MONTH_CELL);
            val monthValue = DateUtils.getMonthNumber(monthCell);
            val dayValue = Integer.parseInt(sheet.getRow(i).getCell(DAY_CELL).getStringCellValue());

            val date = LocalDate.of(Season.getSeasonYearByMonth(monthValue), monthValue, dayValue);
            dates.add(date);
        }

        // Le 1er du mois peut apparaitre comme étant sur le mois précédent dans l'excel
        for (int i = 1; i < dates.size(); i++) {
            val currentDate = dates.get(i);
            val nextDate = dates.get(i - 1);

            if (nextDate.getDayOfMonth() > currentDate.getDayOfMonth()) {
                dates.set(i, currentDate.plusMonths(1));
            }
        }

        return dates;
    }

    private static String clearTitle(String input) {
        return input.replaceAll("\\s*\\([^)]+\\)", "").trim();
    }

    private static Integer getDepartmentNumber(String input) {
        if (isBlank(input)) return null;
        val pattern = Pattern.compile("^.*\\(([0-9]{2})\\).*$");
        val matcher = pattern.matcher(input);
        if (!matcher.find()) return null;
        return Integer.valueOf(matcher.group(1));
    }

    public static String getMergedRegionValue(Sheet sheet, int rowIndex, int columnIndex) {
        // Get all merged regions
        val mergedRegions = sheet.getMergedRegions();

        // Check if the cell is in a merged region
        for (val mergedRegion : mergedRegions) {
            if (mergedRegion.isInRange(rowIndex, columnIndex)) {
                // Get the value from the top-left cell of the merged region
                val firstRow = sheet.getRow(mergedRegion.getFirstRow());
                val firstCell = firstRow.getCell(mergedRegion.getFirstColumn());

                // Return the value as a string, regardless of the cell type
                return firstCell.getStringCellValue();
            }
        }

        // If not in a merged region, get the value of the specified cell
        val row = sheet.getRow(rowIndex);
        if (row != null) {
            val cell = row.getCell(columnIndex);
            if (cell != null) {
                return cell.getStringCellValue();
            }
        }

        // Return null or empty string if cell is not found
        return null;
    }

    private static List<Integer> getMergedRowsForCell(Sheet sheet, Cell cell) {
        List<Integer> mergedRows = new ArrayList<>();
        val rowIndex = cell.getRowIndex();
        val columnIndex = cell.getColumnIndex();

        for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
            val region = sheet.getMergedRegion(i);
            if (region.isInRange(rowIndex, columnIndex)) {
                for (int j = region.getFirstRow(); j <= region.getLastRow(); j++) {
                    mergedRows.add(j);
                }
                break;
            }
        }

        if (CollectionUtils.isEmpty(mergedRows)) {
            mergedRows.add(rowIndex);
        }

        return mergedRows;
    }

    private static Sheet getSheet(String file) throws IOException {
        val fis = new FileInputStream(file);
        val workbook = new XSSFWorkbook(fis);
        return workbook.getSheetAt(0);
    }
}