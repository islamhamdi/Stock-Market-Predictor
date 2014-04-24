package Average;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import edu.stanford.nlp.maxent.Features;

import Default.Global;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class AI {

	String[] sheetname = { "Twitter", "StockTwits", "Combined", "Positive-Twitter", "Negative-Twitter",
			"Positive-StockTwits", "Negative-StockTwits" };
	static String[] features = { "RTID", "RTU", "TID", "TSUM", "UFRN", "THTG", "TURL", "UFLW", "UID", "NEG", "POS", "POS_NEG",
			"NUM_NODES", "NUM_EDGES", "NUM_CMP", "MAX_DIST" };

	int start_raw1 = Global.start_row_t1 + 1;
	int start_col2 = Global.start_col_t2 + 1;
	int width = 7;
	int hight1 = features.length;

	public static void main(String[] args) throws BiffException, IOException {
		Company data[][] = new AI().readData();
		
		for(int i = 0; i < features.length; i++)
		{
			Arrays.sort(data[i]);
			System.out.println(Arrays.toString(data[i]));
		}
	}

	private Company[][] readData() throws BiffException, IOException {
		File statusDir = new File(Global.StatFolderPath);
		File[] files = statusDir.listFiles();

		Company[][] data = new Company[features.length][files.length];
		File inputWorkbook;
		for (int k = 0; k < files.length; k++) {
			inputWorkbook = new File(files[k].getAbsolutePath());

			Workbook workbooks = Workbook.getWorkbook(inputWorkbook);
			Sheet sh = workbooks.getSheet(5);
			int col = start_col2 + Global.lag_var;

			for (int row = start_raw1; row < start_raw1 + hight1; row++) {
				Cell cell = sh.getCell(col, row);
				double val = Double.parseDouble(cell.getContents());
				data[row - start_raw1][k] = new Company(k, val);
			}
			workbooks.close();
		}
		
		return data;
	}
	
	static class Company implements Comparable<Company>{
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
}
