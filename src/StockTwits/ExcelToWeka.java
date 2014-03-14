package StockTwits;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExcelToWeka {
	public static String inDir = "./Excel Input";
	public static String outDir = "./Weka Output/";

	public static void main(String[] args) throws IOException {
		run();
	}

	private static void run() throws IOException {
		File statusDir = new File(inDir);
		File[] files = statusDir.listFiles();
		for (File f : files) {
			System.out.println("Read File : "  + f.getName());
			
			ExcelInterface excel = new ExcelInterface(f);
			BufferedWriter bw = new BufferedWriter(new FileWriter(outDir + excel.getClassName() + ".arff"));

			// write relation name
			bw.write("@relation " + excel.getClassName() + "\n");

			// write attributes
			for (String s : excel.getFeatures())
				bw.write("@attribute " + s + " real\n");

			// write tuples
			bw.write("@data\n");
			for(double[] tuple : excel.getTuples())
			{
				String s = "";
				for(double d : tuple)
					s += "," + d;
				s = s.substring(1);
				
				bw.write(s + "\n");
			}
			
			bw.close();
		}
	}

	private static class ExcelInterface {
		private String[] features;
		private double[][] tuples;
		private String className;

		public ExcelInterface(File file) {
			features = new String[]{"attr1", "attr2"};
			tuples = new double[][]{{10.1, 11.11, 12.12}, {10.2, 0.2, -1.2333}};
			className = "company name";
			
			// TODO set these variables a zankol
		}

		public String getClassName() {
			return className;
		}

		public String[] getFeatures() {
			return features;
		}

		public double[][] getTuples() {
			return tuples;
		}
	}
}
