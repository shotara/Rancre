package com.rancre.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class CommonUtil {

	public static String commonCleanXSS(String value) {
		
		value = value.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		value = value.replaceAll("\\(", "&#40;").replaceAll("\\)", "&#41;");
		value = value.replaceAll("'", "&#39;");		  
		value = value.replaceAll("eval\\((.*)\\)", "");
		value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
		value = value.replaceAll("script", "");
		
		value = value.replaceAll("javascript", "x-javascript");
		value = value.replaceAll("script", "x-script");
		value = value.replaceAll("iframe", "x-iframe");
		value = value.replaceAll("document", "x-document");
		value = value.replaceAll("vbscript", "x-vbscript");
		value = value.replaceAll("applet", "x-applet");
		value = value.replaceAll("embed", "x-embed");  // embed 태그를 사용하지 않을 경우만
		value = value.replaceAll("object", "x-object");    // object 태그를 사용하지 않을 경우만
		value = value.replaceAll("frame", "x-frame");
		value = value.replaceAll("grameset", "x-grameset");
		value = value.replaceAll("layer", "x-layer");
		value = value.replaceAll("bgsound", "x-bgsound");
		value = value.replaceAll("alert", "x-alert");
		value = value.replaceAll("onblur", "x-onblur");
		value = value.replaceAll("onchange", "x-onchange");
		value = value.replaceAll("onclick", "x-onclick");
		value = value.replaceAll("ondblclick","x-ondblclick");
		value = value.replaceAll("enerror", "x-enerror");
		value = value.replaceAll("onfocus", "x-onfocus");
		value = value.replaceAll("onload", "x-onload");
		value = value.replaceAll("onmouse", "x-onmouse");
		value = value.replaceAll("onscroll", "x-onscroll");
		value = value.replaceAll("onsubmit", "x-onsubmit");
		value = value.replaceAll("onunload", "x-onunload");

		return value;
		
	}

	public static void commonPrintLog(String result, String location, String message, HashMap<String, String> map) {

		String printStr = printCurrentTime() + " " + result + " : [" + location + "] " + message + " (";
		
		if(map.get("DEVICE") != null) {
			printStr += "DEVICE : " + map.get("DEVICE") + ", ";
		}
		
		if(map.get("ACTION") != null) {
			printStr += "ACTION : " + map.get("ACTION") + ", ";
		}

		if(map.get("USER-NO") != null) {
			printStr += "USER-NO : " + map.get("USER-NO") + ", ";
		}
						
		if(map.get("POST-NO") != null) {
			printStr += "POST-NO : " + map.get("POST-NO") + ", ";
		}
		
		if(map.get("MEETING-NO") != null) {
			printStr += "MEETING-NO : " + map.get("MEETING-NO") + ", ";
		}
		
		if(map.get("FLASHING-NO") != null) {
			printStr += "FLASHING-NO : " + map.get("FLASHING-NO") + ", ";
		}

		if(map.get("PAYMENT-NO") != null) {
			printStr += "PAYMENT-NO : " + map.get("PAYMENT-NO") + ", ";
		}

		if(map.get("ZONE-NO") != null) {
			printStr += "ZONE-NO : " + map.get("ZONE-NO") + ", ";
		}
		
		if(map.get("NOTICE-NO") != null) {
			printStr += "NOTICE-NO : " + map.get("NOTICE-NO") + ", ";
		}
		
		printStr += ")";
		System.out.println(printStr);
		
		return;
		
	}

	public static byte[] hexToByteArray(String hex) {
		if (hex == null || hex.length() % 2 != 0) {
        	return new byte[]{};
        }

		byte[] bytes = new byte[hex.length() / 2];
		for (int i = 0; i < hex.length(); i += 2) {
        	byte value = (byte)Integer.parseInt(hex.substring(i, i + 2), 16);
        	bytes[(int) Math.floor(i / 2)] = value;
		}
		return bytes;
	}

	public static boolean commonParameterCheck(ArrayList<Object> list) {
		
		for(int i=0; i<list.size(); i++) {
			if(list.get(i).getClass().getName() == "java.lang.String") {
				
				String tempStr = list.get(i).toString();
				
				if(tempStr.equals("")) {
					return false;
				}

				if(tempStr.equals("null")) {
					return false;
				}
				
				if(tempStr.equals(null)) {
					return false;
				}

				if(tempStr.contains("<") || tempStr.contains(">") || tempStr.contains("'") || tempStr.contains("script")) {
					return false;
				}
			}
			
			if(list.get(i).getClass().getName() == "java.lang.Integer") {
				if(Integer.parseInt(list.get(i).toString()) == 0) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	// 현재 시각을 출력한다.
	public static String printCurrentTime() {
		long time = System.currentTimeMillis(); 
		SimpleDateFormat dayTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String str = dayTime.format(new Date(time));
		return str;
	}

	// UnixTimestamp 값을 Datetime 형식으로 변환한다.
	public static String convertUnixTime(long unixTime, int mode) {
		
		if(unixTime == 0) {
			unixTime = System.currentTimeMillis()/1000;
		}
		
		Date date = new Date(unixTime * 1000L);
		SimpleDateFormat sdf = null;
		
		switch(mode) {
			case 1:
				sdf = new SimpleDateFormat("yyyy년 MM월 dd일");
				break;
			case 2:
				sdf = new SimpleDateFormat("yyyy.MM.dd (E) HH:mm");
				break;
			case 3:
				sdf = new SimpleDateFormat("yyyy/MM/dd");
				break;
			case 4:
				sdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E) HH:mm");
				break;
			case 5:
				sdf = new SimpleDateFormat("dd E");
				break;
			case 6:
				sdf = new SimpleDateFormat("dd");
				break;
			case 7:
				sdf = new SimpleDateFormat("MM");
				break;
			case 8:
				sdf = new SimpleDateFormat("yyyyMMdd");
				break;
			case 9:
				sdf = new SimpleDateFormat("MM월 dd일 (E) HH:mm");
				break;
			case 10:
				sdf = new SimpleDateFormat("yyyy년 MM월 dd일 (E)");
				break;
			case 11:
				sdf = new SimpleDateFormat("HH:mm a");
				break;
			case 12:
				sdf = new SimpleDateFormat("HHmm");
				break;
			case 13:
				sdf = new SimpleDateFormat("MM/dd/yyyy");
				break;
			case 14:
				sdf = new SimpleDateFormat("HH:mm");
				break;
			case 15:
				sdf = new SimpleDateFormat("yy.MM.dd");
				break;
			case 16:
				long currentDate = System.currentTimeMillis()/1000;
				long compareDate = currentDate - unixTime;
				
				Date date2 = new Date(compareDate * 1000L);
				SimpleDateFormat sdf2 = null;
				
				if(compareDate <= 60) {
					return compareDate + "초 전";
				} else if(compareDate <= 60*60) {
					return compareDate/60 + "분 전";
				} else if(compareDate <= 60*60*24) {
					return compareDate/(60*60) + "시간 전";	
				} else {
					return compareDate/(60*60*24) + "일 전";
				}
		}
		sdf.setTimeZone(TimeZone.getTimeZone("GMT+9"));
		
		return sdf.format(date);
	}

	public static long convertDateTime(String date, String time) {
	
		// date - 20160222
		// time - 11:00 AM
		
		Date dt = new Date();
	
		if(time.length() == 7) {
			time = "0" + time;
		}
	
		dt.setYear(Integer.parseInt(date.substring(0,4))-1900);
		dt.setMonth(Integer.parseInt(date.substring(4,6))-1);
		dt.setDate(Integer.parseInt(date.substring(6,8)));
		
		if(time.substring(6, 8).equals("AM")) {
			dt.setHours(Integer.parseInt(time.substring(0, 2)));
		} else {
			dt.setHours(Integer.parseInt(time.substring(0, 2))+12);				
		}
		
		dt.setMinutes(Integer.parseInt(time.substring(3, 5)));
		dt.setSeconds(0);
		
		return dt.getTime()/1000; // GMT+9
	}
	
	public static long convertDateTime2(String date, String time) {
		
		// date - 20160222
		// time - 1300
		
		Date dt = new Date();
	
		dt.setYear(Integer.parseInt(date.substring(0,4))-1900);
		dt.setMonth(Integer.parseInt(date.substring(4,6))-1);
		dt.setDate(Integer.parseInt(date.substring(6,8)));
		dt.setHours(Integer.parseInt(time.substring(0,2)));
		dt.setMinutes(Integer.parseInt(time.substring(2,4)));
		dt.setSeconds(0);
		
		return dt.getTime()/1000; // GMT+9
	}
	
	public static ArrayList<String> commonSpiltBySemicolon(String str) {
	
		ArrayList<String> list = new ArrayList<String>();
		String[] temp = null;
	
		temp = str.split(";");
	
		for(int i=0; i<temp.length; i++) {
			list.add(temp[i]);
		}
		
		return list;
	}
	
	public static ArrayList<String> commonSpiltByComma(String str) {
		
		ArrayList<String> list = new ArrayList<String>();
		String[] temp = null;
		
		str = str.replaceAll(" ", "");
		temp = str.split(",");
		
		for(int i=0; i<temp.length; i++) {
			list.add(temp[i]);
		}
		
		return list;
	}

	public static String getCategoryName(int deepCategoryNo) {
		
		switch(deepCategoryNo) {
		case 1:
			return "Database";
		case 2:
			return "Language";
		case 3:
			return "Server";
		case 4:
			return "etc..";
		default:
			return "All";
		}
	}

	public static String getFeedTypeName(int deepFeedType) {
		
		switch(deepFeedType) {
		case 1:
			return "일반문서";
		case 2:
			return "번역문서";
		case 3:
			return "광고";
		default:
			return "일반문서";
		}
	}
	
	public static String splitString(String str, int mode) {
		
		switch(mode) {
		case 1:
			if(str.length() > 10) return str.substring(0,10)+"...";
			else return str;
		case 2:
			if(str.length() > 15) return str.substring(0,10)+"...";
			else return str;		
		case 3:
			if(str.length() > 20) return str.substring(0,10)+"...";
			else return str;
		default:
			return null;
		}
	}
}