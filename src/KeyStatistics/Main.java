package KeyStatistics;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import Default.Global;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class Main {

	static String[] companies;
	static HashMap<Integer, ArrayList<String>> hMap = new HashMap<>();

	public static void main(String[] args) throws BiffException, IOException {

		File inputWorkbook;
		inputWorkbook = new File("CompaniesData1.xls");

		Workbook workbooks = Workbook.getWorkbook(inputWorkbook);
		Sheet sh = workbooks.getSheet(0);
		companies = getContent(sh, 0);

		go(sh);

		workbooks.close();

	}

	private static void addToHash(double low, double high, int col, Sheet sh,
			int key) {
		ArrayList<String> tmplow = new ArrayList<>();
		ArrayList<String> tmphigh = new ArrayList<>();
		ArrayList<String> tmpmid = new ArrayList<>();

		Double[] data;
		data = getData(sh, col);
		for (int i = 0; i < companies.length; i++) {
			if (data[i] == N_A)
				continue;
			if (data[i] < low) {
				tmplow.add(companies[i]);
			} else if (data[i] > high) {
				tmphigh.add(companies[i]);
			} else {
				tmpmid.add(companies[i]);
			}
		}
		hMap.put(key, tmplow);
		hMap.put(key + 1, tmpmid);
		hMap.put(key + 2, tmphigh);

		System.out.println(tmplow.size() + " " + tmpmid.size() + " "
				+ tmphigh.size());
	}

	private static void go(Sheet sh) {
		// int col =1;
		// int col = 4; Peg_Ratio
		// int col = 8;
		// int col = 16;
		// int col = 24;
		// int col = 28;
		// int col = 36;

		// ->>long
		// MarketCap
		addToHash(13010000000f, 101310000000f, 1, sh, Global.Market_Cap_low);
		// Gross_Profit
		addToHash(911890000, 10900000000L, 16, sh, Global.Gross_Profit_low);
		// Float
		addToHash(100020000, 1010000000, 36, sh, Global.Float_low);
		// ->>double
		// Peg_Ratio(0,2,4)
		addToHash(0, 2, 4, sh, Global.Peg_Ratio_low);
		// Enterprise_Value
		addToHash(0, 50, 8, sh, Global.Enterprise_Value_low);
		// current ratio
		addToHash(3, 5, 24, sh, Global.Current_Ration_low);
		// Beta
		addToHash(1, 2, 28, sh, Global.Beta_low);

	}

	static String[] getContent(Sheet sh, int col) {
		int rows = 166;
		String[] v = new String[rows];
		for (int i = 0; i < rows; i++)
			v[i] = sh.getCell(col, i + 1).getContents();
		return v;
	}

	static int N_A = 1111111;

	static Double[] getData(Sheet sh, int col) {
		int rows = 166;
		Double v[] = new Double[rows];

		for (int i = 0; i < rows; i++) {
			String s = sh.getCell(col, i + 1).getContents();
			if (s.equals("NAN") || s.equals("N/A")) {
				v[i] = (double) N_A;
				continue;
			}
			String str = "";
			if (s.length() > 1)
				str = s.substring(0, s.length() - 1);
			if (s.contains("B")) {
				v[i] = (Double.parseDouble(str) * 1e9);
			} else if (s.contains("M")) {
				v[i] = (Double.parseDouble(str) * 1e6);
			} else if (s.contains("K")) {
				v[i] = (Double.parseDouble(str) * 1e3);
			} else {
				s = s.replace(",", "");
				v[i] = (Double.parseDouble(s));
			}
		}
		return v;
	}

}
