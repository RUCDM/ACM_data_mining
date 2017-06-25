package DataStruct;

import java.text.DecimalFormat;

public class doubleDecimal {
	public static String getString(Double d){
		DecimalFormat df=new DecimalFormat("#.###");
		return df.format(d);
	}
}
