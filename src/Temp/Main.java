package Temp;

public class Main {
	public static void main(String[] args) {
//	
		String string = "124574, 110133, 105186, 115962, 93538, 90656, 53978, 64933, 59668, 82304, 98235, 106371, 104155, 98347, 119152, 191350, 201250, 191278, 180250, 192642, 165198, 170275, 178248, 145371";
		string = string.replaceAll(",", "\n");
		System.out.println(string);
	}
}
