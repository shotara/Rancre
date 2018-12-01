package com.rancre.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.XML;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;
import com.rancre.config.GlobalValue;
import com.rancre.model.MemberDAO;
import com.rancre.model.NoticeDAO;
import com.rancre.model.UploadDAO;
import com.rancre.model.domain.Member;
import com.rancre.model.domain.MemberUid;
import com.rancre.model.domain.Notice;
import com.rancre.model.domain.Upload;
import com.rancre.util.CommonUtil;
import com.rancre.util.EncryptUtil;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

public class MemberController {

	public static final String className = "MemberController";

	public static void checkMember(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();

		try {
			HttpSession session = req.getSession();
			// mode = 1 : mail , 3 : name
			int mode = req.getParameter("mode") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("mode").toString())) : 0;				
			String inputMemberParam = req.getParameter("inputMemberParam") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberParam").toString()) : null;				
	
			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			// Parameter check
			ArrayList<Object> parameterList = new ArrayList<Object>();
			parameterList.add(mode);	
			parameterList.add(inputMemberParam);
			if(!CommonUtil.commonParameterCheck(parameterList)) {
				CommonUtil.commonPrintLog("FAIL", className, "Parameter Missing", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// Get Private key
			PrivateKey privateKey = null;
			privateKey = (PrivateKey)session.getAttribute("PrivateKey");				
			session.removeAttribute("PrivateKey"); // 키의 재사용 방지
			
			if(privateKey == null) {
				CommonUtil.commonPrintLog("ERROR", className, "PrivateKey is Null", map);
				jObject.put("outputResult", "-2");
				res.getWriter().write(jObject.toString());
				return;
			}			

			// RSA Decrypt
			String decryptMemberParam = EncryptUtil.RSA_Decode(privateKey, inputMemberParam);
				
			// AES Encrypt
			String aesKey = EncryptUtil.AES_getKey(req.getRealPath("") + File.separator + "META-INF" + File.separator + "keys.xml");
			String encryptMemberParam = EncryptUtil.AES_Encode(decryptMemberParam, aesKey);

			// Check Member Email, Name    			
			if(MemberDAO.checkMember(mode, encryptMemberParam)>0) {
				CommonUtil.commonPrintLog("FAIL", className, "Already Member Name/Email Exist", map);
				jObject.put("outputResult", "-3");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			jObject.put("outputResult", "1");	
			CommonUtil.commonPrintLog("SUCCESS", className, "Check Member OK", map);			
			res.getWriter().write(jObject.toString());
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public static void loginMember(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();

		try {
			HttpSession session = req.getSession();

			String inputMemberEmail = req.getParameter("inputMemberEmail") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberEmail").toString()) : null;
			String inputMemberPassword = req.getParameter("inputMemberPassword") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberPassword").toString()) : null;
				
			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			// Parameter check
			ArrayList<Object> parameterList = new ArrayList<Object>();
			parameterList.add(inputMemberEmail);
			parameterList.add(inputMemberPassword);
			if(!CommonUtil.commonParameterCheck(parameterList)) {
				CommonUtil.commonPrintLog("FAIL", className, "Parameter Missing", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// Get Private key
			PrivateKey privateKey = null;
			privateKey = (PrivateKey)session.getAttribute("PrivateKey");				
			session.removeAttribute("PrivateKey"); // 키의 재사용 방지
			
			if(privateKey == null) {
				CommonUtil.commonPrintLog("ERROR", className, "PrivateKey is Null", map);
				jObject.put("outputResult", "-2");
				res.getWriter().write(jObject.toString());
				return;
			}			

			// RSA Decrypt
			String decryptMemberEmail = EncryptUtil.RSA_Decode(privateKey, inputMemberEmail);
			String decryptMemberPassword = EncryptUtil.RSA_Decode(privateKey, inputMemberPassword);
				
			// AES Encrypt
			String aesKey = EncryptUtil.AES_getKey(req.getRealPath("") + File.separator + "META-INF" + File.separator + "keys.xml");
			String encryptMemberEmail = EncryptUtil.AES_Encode(decryptMemberEmail, aesKey);
//			String encryptMemberEmail = EncryptUtil.AES_Encode(inputMemberEmail, aesKey);

			if(!(MemberDAO.checkMember(1, encryptMemberEmail)>0)) {
				CommonUtil.commonPrintLog("FAIL", className, "No Member", map);
				jObject.put("outputResult", "-3");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// SHA-256 Encrypt
			String encryptMemberPassword = EncryptUtil.SHA256_Encode(decryptMemberPassword);
//			String encryptMemberPassword = EncryptUtil.SHA256_Encode(inputMemberPassword);

			Member member = MemberDAO.getMemberByMemberMail(encryptMemberEmail, encryptMemberPassword);
			
			// Password not correct
			if(member == null) {
				CommonUtil.commonPrintLog("FAIL", className, "Password Not Correct!", map);
				jObject.put("outputResult", "-4");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// check member permit
			if(member.getRacMemberStatus() != 1) {
				CommonUtil.commonPrintLog("FAIL", className, "No Member Permit!", map);
				jObject.put("outputResult", "-5");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// Get MemberUID
			MemberUid outputMemberUid = MemberDAO.getMemberUid(member.getRacMemberNo());
			
			// Success
			MemberController.setMemberSession(session, member, outputMemberUid.getRacMemberUid(), aesKey);
			
			map.put("USER-NO", "0");
			CommonUtil.commonPrintLog("SUCCESS", className, "User Login OK", map);		
			jObject.put("outputResult", "1");
			res.getWriter().write(jObject.toString());
			return;
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	// Set member session
	public static void setMemberSession(HttpSession session, Member member, String inputMemberUid, String aesKey) {

		try {
			session.setMaxInactiveInterval(3600);
			session.setAttribute("racMemberNo", member.getRacMemberNo());
			session.setAttribute("racMemberUid", inputMemberUid);	
			session.setAttribute("racMemberEmail", EncryptUtil.AES_Decode(member.getRacMemberEmail(), aesKey));
		
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	// join 
	public static void joinMember(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();

		try {
			HttpSession session = req.getSession();

			int inputMemberStatus = 1;
			Calendar calendar = Calendar.getInstance();
			Timestamp inputCurrentDate = new java.sql.Timestamp(calendar.getTime().getTime());
			String inputMemberEmail = req.getParameter("inputMemberEmail") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberEmail").toString()) : null;
			String inputMemberPassword = req.getParameter("inputMemberPassword") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberPassword").toString()) : null;
			String inputMemberPasswordConfirm = req.getParameter("inputMemberPasswordConfirm") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberPasswordConfirm").toString()) : null;

			System.out.println("time =" + inputCurrentDate);
			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");

			// Parameter check
			ArrayList<Object> parameterList = new ArrayList<Object>();
			parameterList.add(inputMemberEmail);
			parameterList.add(inputMemberPassword);
			parameterList.add(inputMemberPasswordConfirm);
			if(!CommonUtil.commonParameterCheck(parameterList)) {
				CommonUtil.commonPrintLog("FAIL", className, "Parameter Missing", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// Get Private key
			PrivateKey privateKey = null;
			privateKey = (PrivateKey)session.getAttribute("PrivateKey");				
			session.removeAttribute("PrivateKey"); // 키의 재사용 방지
			
			if(privateKey == null) {
				CommonUtil.commonPrintLog("ERROR", className, "PrivateKey is Null", map);
				jObject.put("outputResult", "-2");
				res.getWriter().write(jObject.toString());
				return;
			}			
         
			// RSA Decrypt
//			String decryptMemberMajor = EncryptUtil.RSA_Decode(privateKey, inputMemberMajor);
//			String decryptMemberCareer = EncryptUtil.RSA_Decode(privateKey, inputMemberCareer);
			String decryptMemberEmail = EncryptUtil.RSA_Decode(privateKey, inputMemberEmail);
			String decryptMemberPassword = EncryptUtil.RSA_Decode(privateKey, inputMemberPassword);
				
			// AES Encrypt
			String aesKey = EncryptUtil.AES_getKey(req.getRealPath("") + File.separator + "META-INF" + File.separator + "keys.xml");
//			String encryptMemberMajor = EncryptUtil.AES_Encode(decryptMemberMajor, aesKey);
//			String encryptMemberCareer = EncryptUtil.AES_Encode(decryptMemberCareer, aesKey);
			String encryptMemberEmail = EncryptUtil.AES_Encode(decryptMemberEmail, aesKey);
			
			// Check the email in the database
			if((MemberDAO.checkMember(1, encryptMemberEmail)>0)) {
				CommonUtil.commonPrintLog("FAIL", className, "Alredy Exist Mail", map);
				jObject.put("outputResult", "-3");
				res.getWriter().write(jObject.toString());
				return;
			}

			// SHA-256 Encrypt
			String encryptMemberPassword = EncryptUtil.SHA256_Encode(decryptMemberPassword);			
			String decryptMemberPasswordConfirm = EncryptUtil.RSA_Decode(privateKey, inputMemberPasswordConfirm);

			// Password and passwordConfirm is not correct
			if(!(decryptMemberPassword.equals(decryptMemberPasswordConfirm))) {
				CommonUtil.commonPrintLog("FAIL", className, "Password Not Correct!", map);
				jObject.put("outputResult", "-5");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// Join member 
			int check = MemberDAO.addMember(inputMemberStatus, inputCurrentDate, encryptMemberEmail, encryptMemberPassword);

			if(check != 1) {
				CommonUtil.commonPrintLog("FAIL", className, "Add Member Fail", map);
				jObject.put("outputResult", "-6");
				res.getWriter().write(jObject.toString());
				return;
			}			
			
			// Create MemberUid
			String memberUid = encryptMemberEmail.substring(0,6) + Long.toString(System.currentTimeMillis()/1000).substring(0,4);
			int createMemberUid = MemberDAO.addMemberUid(encryptMemberEmail, memberUid);
			
			// 완료 
			CommonUtil.commonPrintLog("SUCCESS", className, "User Join OK", map);
			jObject.put("outputResult", "1");
			res.getWriter().write(jObject.toString());
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Logout Member
	public static void logoutMember(HttpServletRequest req, HttpServletResponse res) {
		
		HashMap<String, String> map = new HashMap<String, String>();
				
		try {
			HttpSession session = req.getSession(false);
			
			// 세션이 없는 경우
			if(session == null) {
				CommonUtil.commonPrintLog("FAIL", className, "No Member", map);
				res.getWriter().write("-1");
				return;
			}
			
			session.invalidate();
			
			CommonUtil.commonPrintLog("SUCCESS", className, "User Logout OK", map);
			res.getWriter().write("1");

		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setMember(HttpServletRequest req, HttpServletResponse res) {
//
//		HashMap<String, String> map = new HashMap<String, String>();
//
//		try {
//			HttpSession session = req.getSession();
//
//			int sessionMemberNo = session.getAttribute("deepMemberNo") != null ? Integer.parseInt(session.getAttribute("deepMemberNo").toString()) : 0;
//			String inputMemberMajor = req.getParameter("inputMemberMajor") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberMajor").toString()) : null;
//			String inputMemberCareer = req.getParameter("inputMemberCareer") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberCareer").toString()) : null;
//			String inputMemberName = req.getParameter("inputMemberName") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberName").toString()) : null;
//			String inputMemberPassword = req.getParameter("inputMemberPassword") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberPassword").toString()) : null;
//			String inputMemberPasswordConfirm = req.getParameter("inputMemberPasswordConfirm") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputMemberPasswordConfirm").toString()) : null;
//			int inputMemberImage = -1;
//
//			JSONObject jObject = new JSONObject();
//			res.setContentType("application/json");
//			res.setCharacterEncoding("UTF-8");
//
////			// Get Private key
////			PrivateKey privateKey = null;
////			privateKey = (PrivateKey)session.getAttribute("PrivateKey");				
////			session.removeAttribute("PrivateKey"); // 키의 재사용 방지
////			
////			if(privateKey == null) {
////				CommonUtil.commonPrintLog("ERROR", className, "PrivateKey is Null", map);
////				jObject.put("outputResult", "-2");
////				res.getWriter().write(jObject.toString());
////				return;
////			}			
////         
////			// RSA Decrypt
////			String decryptMemberMajor = EncryptUtil.RSA_Decode(privateKey, inputMemberMajor);
////			String decryptMemberCareer = EncryptUtil.RSA_Decode(privateKey, inputMemberCareer);
////			String decryptMemberEmail = EncryptUtil.RSA_Decode(privateKey, inputMemberEmail);
////			String decryptMemberName = EncryptUtil.RSA_Decode(privateKey, inputMemberName);
////			String decryptMemberPassword = EncryptUtil.RSA_Decode(privateKey, inputMemberPassword);
//
//			
//			// AES Encrypt
//			String aesKey = EncryptUtil.AES_getKey(req.getRealPath("") + File.separator + "META-INF" + File.separator + "keys.xml");
////			String encryptMemberMajor = EncryptUtil.AES_Encode(decryptMemberMajor, aesKey);
////			String encryptMemberCareer = EncryptUtil.AES_Encode(decryptMemberCareer, aesKey);
////			String encryptMemberEmail = EncryptUtil.AES_Encode(decryptMemberEmail, aesKey);
////			String encryptMemberName = EncryptUtil.AES_Encode(decryptMemberName, aesKey);
//			
//			String encryptMemberMajor = EncryptUtil.AES_Encode(inputMemberMajor, aesKey);
//			String encryptMemberCareer = EncryptUtil.AES_Encode(inputMemberCareer, aesKey);
//			String encryptMemberName = EncryptUtil.AES_Encode(inputMemberName, aesKey);
//			
//			// Check the email in the database
//			if(!(sessionMemberNo>0)) {
//				CommonUtil.commonPrintLog("FAIL", className, "No Member", map);
//				jObject.put("outputResult", "-3");
//				res.getWriter().write(jObject.toString());
//				return;
//			}
//			
//			// Check the name in the database
//			if((MemberDAO.checkMember(2, encryptMemberName)>0)) {
//				CommonUtil.commonPrintLog("FAIL", className, "Alredy Exist Name", map);
//				jObject.put("outputResult", "-4");
//				res.getWriter().write(jObject.toString());
//				return;
//			}			
//			
//			
//			// SHA-256 Encrypt
////			String encryptMemberPassword = EncryptUtil.SHA256_Encode(decryptMemberPassword);
//			String encryptMemberPassword = EncryptUtil.SHA256_Encode(inputMemberPassword);
//			
//			
//			// Set member 
//			int mode; 	// mode 1 : password change  2 : not change
//			if(!inputMemberPassword.equals("")) {
//				mode = 1;
//				// Password and passwordConfirm is not correct
//				if(!inputMemberPassword.equals(inputMemberPasswordConfirm)) {
//					CommonUtil.commonPrintLog("FAIL", className, "Password Not Correct!", map);
//					jObject.put("outputResult", "-5");
//					res.getWriter().write(jObject.toString());
//					return;
//				}
//			} else mode = 2; 
//			
//			int check = MemberDAO.setMember(mode, sessionMemberNo, encryptMemberMajor, encryptMemberCareer, encryptMemberName, encryptMemberPassword);
//
//			if(check != 1) {
//				CommonUtil.commonPrintLog("FAIL", className, "Add Member Fail", map);
//				jObject.put("outputResult", "-6");
//				res.getWriter().write(jObject.toString());
//				return;
//			}			
//
//			// 완료 
//			CommonUtil.commonPrintLog("SUCCESS", className, "User Join OK", map);			
//			res.getWriter().write(jObject.toString());
//			return;
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static void loginCheck(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();
		
		try {
			HttpSession session = req.getSession();

			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			
			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");

			if(!(sessionMemberNo>0)) {
				CommonUtil.commonPrintLog("ERROR", className, "No Member", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			CommonUtil.commonPrintLog("SUCCESS", className, "Login Check OK", map);
			jObject.put("outputResult", "1");
			res.getWriter().write(jObject.toString());
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void checkCompany(HttpServletRequest req, HttpServletResponse res) {
		
		HashMap<String, String> map = new HashMap<String, String>();
		
		try {
			HttpSession session = req.getSession();
			
			BufferedReader br = null; 
			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			int inputNumber = req.getParameter("inputNumber") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputNumber").toString())) : 0;				
			String inputCompany = req.getParameter("inputCompany") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputCompany").toString()) : null;
			String key = "zrmMgIa4mQMssyPY1Y%2Fao0z7Xr6i7i9YOdn%2B0sISrGUHkdbMsay3aU6ov%2BH5wo9%2BEBzXfCQ0teCQn1Jz45YoGg%3D%3D";
			
			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");

			PrivateKey privateKey = null;
			privateKey = (PrivateKey)session.getAttribute("PrivateKey");				
			session.removeAttribute("PrivateKey"); // 키의 재사용 방지
			
//			if(privateKey == null) {
//				CommonUtil.commonPrintLog("ERROR", className, "PrivateKey is Null", map);
//				jObject.put("outputResult", "-2");
//				res.getWriter().write(jObject.toString());
//				return;
//			}	
			
			String urlStr = "http://apis.data.go.kr/B552015/NpsBplcInfoInqireService/getBassInfoSearch?bzowr_rgst_no="
					+ inputNumber + "&wkpl_nm="+ inputCompany+"&numOfRows=1&serviceKey="+key; // 요청 할 주소

			System.out.println(urlStr);
			URL url = new URL(urlStr);
			HttpURLConnection urlconnection = (HttpURLConnection) url.openConnection();
			urlconnection.setRequestMethod("GET");
			br = new BufferedReader(new InputStreamReader(urlconnection.getInputStream(),"utf-8"));
			String result="";
			String line;
			while((line = br.readLine()) != null)  {
				result = result + line + "\n";
			}
			System.out.println(result);
			org.json.JSONObject objsct = null;
	        try {
	            org.json.JSONObject xmlJSONObj = XML.toJSONObject(result);
	            String jsonPrettyPrintString = xmlJSONObj.toString(4);
	            result = jsonPrettyPrintString;
	            objsct = xmlJSONObj;
	            System.out.println(jsonPrettyPrintString);
	        } catch (JSONException je) {
	            System.out.println(je.toString());
	        }
			
	        System.out.println(objsct.getJSONObject("response").getJSONObject("header").getString("resultCode"));
			
			CommonUtil.commonPrintLog("SUCCESS", className, "Login Check OK", map);
			jObject.put("outputResult", "1");
			res.getWriter().write(jObject.toString());
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void checkEmail(HttpServletRequest req, HttpServletResponse res) {
		
		HashMap<String, String> map = new HashMap<String, String>();
		
		try {
			HttpSession session = req.getSession();
			
			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			int inputNumber = req.getParameter("inputNumber") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputNumber").toString())) : 0;				
			String inputEmail = req.getParameter("inputEmail") != null ? CommonUtil.commonCleanXSS(req.getParameter("inputEmail").toString()) : null;
			String key = "zrmMgIa4mQMssyPY1Y%2Fao0z7Xr6i7i9YOdn%2B0sISrGUHkdbMsay3aU6ov%2BH5wo9%2BEBzXfCQ0teCQn1Jz45YoGg%3D%3D";
			
			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");

			PrivateKey privateKey = null;
			privateKey = (PrivateKey)session.getAttribute("PrivateKey");				
			session.removeAttribute("PrivateKey"); // 키의 재사용 방지
			
//			if(privateKey == null) {
//				CommonUtil.commonPrintLog("ERROR", className, "PrivateKey is Null", map);
//				jObject.put("outputResult", "-2");
//				res.getWriter().write(jObject.toString());
//				return;
//			}	
			

			String host = "smtp.naver.com";
			final String user = "shotzara";
			final String password = "rmlarmla12!";
			
		    // Get the session object
		    Properties props = new Properties();
		    props.put("mail.smtp.host", host);
		    props.put("mail.smtp.auth", "true");
		  
		    Session sessionMail = Session.getDefaultInstance(props, new javax.mail.Authenticator() {
			   protected PasswordAuthentication getPasswordAuthentication() {
			    return new PasswordAuthentication(user, password);
			   }
		    });
		  
		    MimeMessage message = new MimeMessage(sessionMail);
		    message.setFrom(new InternetAddress(user));
		    message.addRecipient(Message.RecipientType.TO, new InternetAddress(inputEmail));

		    String randomNum = "123123123";
		    // Subject
		    message.setSubject("안녕하세요. 크리에이터 광고의 모든것, 랭크리입니다.");
			   
		    // Text
		    message.setText("가입 인증 번호입니다.\n 인증번호 : "+randomNum);

		    // send the message
		    Transport.send(message);
		    System.out.println("message sent successfully...");
			jObject.put("outputAuthNo", randomNum);

			CommonUtil.commonPrintLog("SUCCESS", className, "Login Check OK", map);
			jObject.put("outputResult", "1");
			res.getWriter().write(jObject.toString());
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}