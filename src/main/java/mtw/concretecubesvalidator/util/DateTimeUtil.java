package mtw.concretecubesvalidator.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtil {

	private static final String DATE_TIME_PATERN = "yyyy-MM-dd kk : mm : ss";
	private static final String DATE_PATERN = "yyyy-MM-dd";
	private static final String TIME_PATERN = "kk : mm : ss";

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_PATERN);
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATERN);
	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_PATERN);

	public static String dateTimeFormat(LocalDateTime dateTime) {
		if (dateTime == null) {
			return null;
		}
		return DATE_TIME_FORMATTER.format(dateTime);
	}

	public static String dateFormat(LocalDate date) {
		if (date == null) {
			return null;
		}
		return DATE_FORMATTER.format(date);
	}

	public static String timeFormat(LocalTime time) {
		if (time == null) {
			return null;
		}
		return TIME_FORMATTER.format(time);
	}

	public static LocalDateTime dateTimeParse(String stringDateTime) {
		try {
			return DATE_TIME_FORMATTER.parse(stringDateTime, LocalDateTime::from);

		} catch (DateTimeException e) {
			return null;
		}
	}

	public static LocalDate dateParse(String stringDate) {
		try {
			return DATE_FORMATTER.parse(stringDate, LocalDate::from);

		} catch (DateTimeException e) {
			return null;
		}
	}

	public static LocalTime timeParse(String stringTime) {
		try {
			return TIME_FORMATTER.parse(stringTime, LocalTime::from);

		} catch (DateTimeException e) {
			return null;
		}
	}

	public static boolean validDateTime(String stringDateTime) {
		return DateTimeUtil.dateTimeParse(stringDateTime) != null;
	}

	public static boolean validDate(String stringDate) {
		return DateTimeUtil.dateParse(stringDate) != null;
	}

	public static boolean validTime(String stringTime) {
		return DateTimeUtil.timeParse(stringTime) != null;
	}
}
