package Average;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import Default.Global;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class AI {

	static String[] features = { "RTID", "RTU", "TID", "TSUM", "UFRN", "THTG",
			"TURL", "UFLW", "UID", "NEG", "POS", "POS_NEG", "NUM_NODES",
			"NUM_EDGES", "NUM_CMP", "MAX_DIST" };

	int start_raw1 = Global.start_row_t1 + 1;
	int start_col2 = Global.start_col_t2 + 1;
	int width = 7;
	int hight1 = features.length;
	private static double threshold = 0.4;
	private static com[] companyWeight;

	public static void main(String[] args) throws BiffException, IOException {
		Company data[][] = new AI().readData();

		for (int i = 0; i < features.length; i++) {
			System.out.println("Feature : " + features[i]);
			Arrays.sort(data[i]);

			int cnt1 = 0;
			for (int k = 0; k < data[i].length; k++)
				if (data[i][k].val >= threshold)
					cnt1++;
				else
					break;

			int cnt2 = 0;
			double sum = 0, average = 0;
			for (int k = 0; k < data[i].length; k++) {
				sum += data[i][k].val;
				average = sum / (k + 1);

				if (average >= threshold)
					cnt2++;
				else
					break;
			}

			if (cnt1 >= 15) {
				for (int k = 0; k < data[i].length; k++)
					companyWeight[data[i][k].idx].val += k;
			}
			System.out.println("Number of Companies Greater than " + threshold
					+ " : " + cnt1);
			System.out.println("Average Company Sum Greater than " + threshold
					+ " : " + cnt2);
			System.out.println(Arrays.toString(data[i]) + "\n");
		}

		Arrays.sort(companyWeight);
		System.out.println(Arrays.toString(companyWeight));

		System.out.println(companyWeight.length);

	}

	private Company[][] readData() throws BiffException, IOException {
		File statusDir = new File(
				"/home/mohamed/Dropbox/Stock Market Daily Data/statistics/plain");
		File[] files = statusDir.listFiles();

		Company[][] data = new Company[features.length][files.length];
		companyWeight = new com[files.length];

		File inputWorkbook;
		for (int k = 0; k < files.length; k++) {
			inputWorkbook = new File(files[k].getAbsolutePath());

			Workbook workbooks = Workbook.getWorkbook(inputWorkbook);
			Sheet sh = workbooks.getSheet(2);
			int col = start_col2 + Global.lag_var;

			for (int row = start_raw1; row < start_raw1 + hight1; row++) {
				Cell cell = sh.getCell(col, row);
				double val = Double.parseDouble(cell.getContents());
				data[row - start_raw1][k] = new Company(k, val);
			}

			companyWeight[k] = new com(inputWorkbook.getName(), k);
			workbooks.close();
		}

		return data;
	}

	static class Company implements Comparable<Company> {
		int idx;
		double val;

		public Company(int ii, double vv) {
			idx = ii;
			val = vv;
		}

		@Override
		public int compareTo(Company o) {
			return Double.compare(o.val, val);
		}

		@Override
		public String toString() {
			return idx + " " + val;
		}
	}

	static class com implements Comparable<com> {
		int val, index;
		String name;

		public com(String nn, int idx) {
			name = nn;
			index = idx;
		}

		@Override
		public int compareTo(com o) {
			return val - o.val;
		}

		@Override
		public String toString() {
			return val + " " + name + "\n";
		}
	}
}