package com.deep.controller;

import java.io.File;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONObject;

import com.deep.config.GlobalValue;
import com.deep.model.MemberDAO;
import com.deep.model.NoticeDAO;
import com.deep.model.domain.Member;
import com.deep.model.domain.Notice;
import com.deep.model.domain.MemberUid;
import com.deep.model.domain.Upload;
import com.deep.util.CommonUtil;
import com.deep.util.EncryptUtil;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;
import com.oreilly.servlet.multipart.FilePart;
import com.oreilly.servlet.multipart.MultipartParser;
import com.oreilly.servlet.multipart.Part;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.name.Rename;

public class NoticeController {
	
	public static final String className = "NoticeController";

	
	//addDAO는 어디로 넣어야?
	public static int listNotice(HttpServletRequest req, HttpServletResponse res){
		
		HashMap<String, String> map = new HashMap<String, String>();
		
		try{
			HttpSession session = req.getSession();
			
			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			if( NoticeDAO.getNoticelist()== 0){ // 알림 리스트가 없을때
				CommonUtil.commonPrintLog("FAIL", className, "No NoticeList", map);
				jObject.put("outputResult", "-3");
				res.getWriter().write(jObject.toString());
				return 0;
			}

		}
		
		catch(Exception e){
			e.printStackTrace();
		}
		
		return 1; // 임시.  나중에 list를 반환해야 할 듯.
	}
	
	public static void setNotice(HttpServletRequest req, HttpServletResponse res) {//status읽음안읽음
		
		HashMap<String, String> map = new HashMap<String, String>();
		
		try{
			HttpSession session = req.getSession();

			int inputNoticeNo = req.getParameter("inputNoticeNo") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputNoticeNo").toString())) : 0;
		
			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			// Parameter check
			ArrayList<Object> parameterList = new ArrayList<Object>();
			parameterList.add(inputNoticeNo);
			
			if(!CommonUtil.commonParameterCheck(parameterList)) {
				CommonUtil.commonPrintLog("FAIL", className, "Parameter Missing", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			
			//DAO
			if( NoticeDAO.setNotice(inputNoticeNo) == 0){ //하나도 상태바뀐게 없음, bool타입으로 바꿔야하나?
				CommonUtil.commonPrintLog("FAIL", className, "No Update Notice Status", map);
				jObject.put("outputResult", "-3");
				res.getWriter().write(jObject.toString());
				return;
			}
			
		}
		catch(Exception e){
			e.printStackTrace();
		}
	// 확인날짜, 상태 바꿔야 할듯.
	}
	
	
	public static int getNotice(HttpServletRequest req, HttpServletResponse res){ //도메인을 하나를 가져온다??
		HashMap<String, String> map = new HashMap<String, String>();
		
		try{
			HttpSession session = req.getSession();
			
			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			
			if( NoticeDAO.getNotice() == 0){ //하나도 상태바뀐게 없음, bool타입으로 바꿔야하나?
				CommonUtil.commonPrintLog("FAIL", className, "No Notice", map);
				jObject.put("outputResult", "-3");
				res.getWriter().write(jObject.toString());
				return 0;
			}
			

		}
		
		catch(Exception e){
			e.printStackTrace();
		}
		return 0;
	}
}
