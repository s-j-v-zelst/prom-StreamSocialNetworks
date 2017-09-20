package org.processmining.streamsocialnetworks.models.graphstream;

public class GSSingleGraphDefaultLayout {
	
	public static String CLASS_VERY_HIGH = "very_high";
	public static String CLASS_HIGH = "high";
	public static String CLASS_NEUTRAL = "neutral";
	public static String CLASS_LOW = "low";
	public static String CLASS_VERY_LOW = "very_low";
	public static String CLASS_DELETED = "elem_del";
	public static String CLASS_NEW = "elem_new";
	public static String CLASS_DOWN = "down";
	public static String CLASS_UP = "up";
	
	private static String COLOR_VERY_HIGH = "#08306b";
	private static String COLOR_HIGH = "#08519c";
	private static String COLOR_NEUTRAL = "#2171b5";
	private static String COLOR_LOW = "#4292c6";
	private static String COLOR_VERY_LOW = "#6baed6";
	private static String COLOR_DELETED = "#969696";
	private static String COLOR_NEW = "#f03b20";
	

	public static String LAYOUT = new StringBuilder()
			.append("graph {")
			.append("	fill-color: rgba(0,0,0,0);")
			.append("	padding: 200,200,200;")
			.append("}")
			.append("node {")
			.append("	fill-mode: gradient-vertical;")
			.append("	fill-color: #045a8d, #2b8cbe;")
			.append("	shape: rounded-box;")
			.append("	size: 25px, 25px;")
//			.append("	stroke-mode: plain;")
			.append("	text-color: black;")
			.append("	text-size: 11;")
			.append("}")
			.append("node."+ CLASS_VERY_LOW +" {")
			.append("	fill-color:"+ COLOR_DELETED + ", " + COLOR_VERY_LOW + ";")
			.append("	size: 60px, 30px;")
			.append("	text-size: 9;")			
			.append("}")
			.append("node."+ CLASS_LOW +" {")
			.append("	fill-color:"+ COLOR_VERY_LOW + ", " + COLOR_LOW + ";")
			.append("	size: 70px, 35px;")
			.append("	text-size: 10;")
			.append("}")
			.append("node."+ CLASS_NEUTRAL +" {")
			.append("	fill-color:"+ COLOR_LOW + ", " + COLOR_NEUTRAL + ";")
			.append("	size: 80px, 40px;")
			.append("	text-size: 11;")
			.append("	text-style: bold;")
			.append("}")
			.append("node."+ CLASS_HIGH +" {")
			.append("	fill-color:"+ COLOR_NEUTRAL + ", " + COLOR_HIGH + ";")
			.append("	size: 90px, 45px;")
			.append("	text-color: white;")
			.append("	text-size: 12;")
			.append("	text-style: bold;")
			.append("}")
			.append("node."+ CLASS_VERY_HIGH +" {")
			.append("	fill-color:"+ COLOR_HIGH + ", " + COLOR_VERY_HIGH + ";")
			.append("	size: 100px, 50px;")
			.append("	text-color: white;")
			.append("	text-size: 13;")
			.append("	text-style: bold;")
			.append("}")
			.append("node."+ CLASS_DELETED +" {")
			.append("	size: 30px, 30px;")
			.append("	fill-color:"+ COLOR_DELETED + ";")
			.append("}")
			.append("node."+ CLASS_NEW +" {")
			.append("	size: 50px, 50px;")
			.append("	fill-color:"+ COLOR_NEW + ";")
			.append("}")
			.append("sprite {")
			.append("	size: 0px, 0px;")
			.append("	text-background-mode: rounded-box;")
			.append("	text-color: white;")
			.append("	text-padding: 5;")
			.append("	text-size: 20;")
			.append("	text-style: bold;")
			.append("}")
			.append("sprite."+ CLASS_UP +" {")
			.append("	text-background-color: "+COLOR_VERY_HIGH+";")
			.append("}")
			.append("sprite."+ CLASS_DOWN +" {")
			.append("	text-background-color: "+COLOR_NEW+";")
			.append("}")
			.append("edge {")
			.append("	arrow-size: 15, 10;")
			.append("	size: 2;")
			.append("}")
			.append("edge."+ CLASS_VERY_LOW +" {")
			.append("	size: 3;")
			.append("}")
			.append("edge."+ CLASS_LOW +" {")
			.append("	size: 4;")
			.append("}")
			.append("edge."+ CLASS_NEUTRAL +" {")
			.append("	size: 5;")
			.append("}")
			.append("edge."+ CLASS_HIGH +" {")
			.append("	size: 6;")
			.append("}")
			.append("edge."+ CLASS_VERY_HIGH +" {")
			.append("	size: 7;")
			.append("}")
			.append("edge."+ CLASS_DELETED +" {")
			.append("	size: 1;")
			.append("	fill-color:"+ COLOR_DELETED + ";")
			.append("}")
			.append("edge."+ CLASS_NEW +" {")
			.append("	fill-color:"+ COLOR_NEW + ";")
			.append("}")
			.toString();
}
