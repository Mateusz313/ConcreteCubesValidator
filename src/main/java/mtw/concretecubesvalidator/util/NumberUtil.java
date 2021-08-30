package mtw.concretecubesvalidator.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class NumberUtil {

	public static String doubleFormat(double value, int fractionDigits) {

		NumberFormat doubleFormat = DecimalFormat.getInstance();
		doubleFormat.setMaximumFractionDigits(fractionDigits);
		doubleFormat.setMinimumFractionDigits(fractionDigits);

		return doubleFormat.format(value);

	}

	public static String intFormat(int value) {

		NumberFormat intFormat = DecimalFormat.getInstance();

		return intFormat.format(value);

	}

}
