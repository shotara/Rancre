package com.rancre.controller;

import java.io.File;
import java.net.URL;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.rancre.config.GlobalValue;
import com.rancre.model.AdminDAO;
import com.rancre.model.ChannelDAO;
import com.rancre.model.FeedDAO;
import com.rancre.model.MemberDAO;
import com.rancre.model.UploadDAO;
import com.rancre.model.domain.Channel;
import com.rancre.model.domain.ChannelCategory;
import com.rancre.model.domain.ChannelCost;
import com.rancre.model.domain.Feed;
import com.rancre.model.domain.FeedComment;
import com.rancre.model.domain.FeedCount;
import com.rancre.model.domain.FeedHashtag;
import com.rancre.model.domain.FeedList;
import com.rancre.model.domain.FeedSeries;
import com.rancre.model.domain.Member;
import com.rancre.model.domain.MemberFavorite;
import com.rancre.model.domain.Paging;
import com.rancre.model.domain.Upload;
import com.rancre.util.CommonUtil;
import com.rancre.util.EncryptUtil;

public class AdminController {

	public static final String className = "AdminController";

	public static void getChannelList(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();
		
		try {
			HttpSession session = req.getSession();

			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			int page = req.getParameter("page") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("page").toString())) : 1;
			int size = req.getParameter("size") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("size").toString())) : 10;

			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			if(!(sessionMemberNo>0)) {
				CommonUtil.commonPrintLog("FAIL", className, "No Admin Member", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			/// Check Admin Member 

			JSONArray jChannelArray = new JSONArray();
			int channelStatus = 2; // 정상 등록 
			Paging paging = new Paging(page, size);
			int offset = (paging.getCurrentPageNo() - 1) * paging.getRecordsPerPage();

			// 현재 대출 가능한 도서(book_lending_possible이 true인 목록만 가져옴)
			ArrayList<Channel> channelList = AdminDAO.getChannelList(channelStatus, offset, paging.getRecordsPerPage());

			// bookList 전체 갯수 구하여 numberOfRecords 메소드에 셋팅함 
			paging.setNumberOfRecords(AdminDAO.countTotalChannel());
			paging.makePaging();

			for(int i=0; i<channelList.size();i++) {
				JSONObject jTempObject = new JSONObject();
				// channel
				jTempObject.put("outputChannelNo", channelList.get(i).getRacChannelNo());
				jTempObject.put("outputChannelTitle", channelList.get(i).getRacChannelTitle());
				jTempObject.put("outputChannelUrl", channelList.get(i).getRacChannelUrl());
				jTempObject.put("outputChannelFollowers", channelList.get(i).getRacChannelFollowers());
				jTempObject.put("outputChannelViews", channelList.get(i).getRacChannelViews());
				jTempObject.put("outputChannelCategory", channelList.get(i).getRacChannelCategory());
				// 후기 가져오기
				jTempObject.put("outputPostscriptCount", 0);
				// 광고 영상에 대하여 
				jTempObject.put("outputChannelAdCount", AdminDAO.countChannelAd(channelList.get(i).getRacChannelNo()));
				// 단가에 대하여  
				int costCount = AdminDAO.countChannelCost(channelList.get(i).getRacChannelNo());
				if(costCount != 0) {
					jTempObject.put("outputChannelCostCount",costCount);
					jTempObject.put("outputChannelCostEvenPrice", AdminDAO.getChannelCostPrice(channelList.get(i).getRacChannelNo())/costCount);

				} else {
					jTempObject.put("outputChannelCostCount","미등록");
					jTempObject.put("outputChannelCostEvenPrice", "0");

				}

				jChannelArray.add(jTempObject);
			}

			jObject.put("outputChannelList", jChannelArray);
			jObject.put("firstPageNo", paging.getFirstPageNo());
			jObject.put("prevPageNo", paging.getPrevPageNo());
			jObject.put("currentPageNo", paging.getCurrentPageNo());
			jObject.put("nextPageNo", paging.getNextPageNo());
			jObject.put("paging", paging);

			CommonUtil.commonPrintLog("SUCCESS", className, "Get Channel List OK", map);
			req.setAttribute("result", jObject);
			req.getRequestDispatcher("/02_page/Admin/channelList.jsp").forward(req, res);

			return;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void addChannelCost(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();

		try {
			HttpSession session = req.getSession();

			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			int inputChannelNo = req.getParameter("inputChannelNo") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputChannelNo").toString())) : 0;
			int inputChannelCostCategory = req.getParameter("inputChannelCostCategory") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputChannelCostCategory").toString())) : 0;
			int inputChannelCostPrice = req.getParameter("inputChannelCostPrice") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputChannelCostPrice").toString())) : 0;
			Calendar calendar = Calendar.getInstance();
			Timestamp inputCurrentDate = new java.sql.Timestamp(calendar.getTime().getTime());

			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");

			
			if(!(sessionMemberNo>0)) {
				CommonUtil.commonPrintLog("FAIL", className, "No Admin Member", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// Parameter check
			ArrayList<Object> parameterList = new ArrayList<Object>();
			parameterList.add(inputChannelNo);
			parameterList.add(inputChannelCostCategory);
			parameterList.add(inputChannelCostPrice);

			if(!CommonUtil.commonParameterCheck(parameterList)) {
				CommonUtil.commonPrintLog("FAIL", className, "Parameter Missing", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// Join member 
			int check = AdminDAO.addChannelCost(inputChannelNo, inputChannelCostCategory, inputChannelCostPrice, inputCurrentDate);

			if(check != 1) {
				CommonUtil.commonPrintLog("FAIL", className, "Add Channel Fail", map);
				jObject.put("outputResult", "-6");
				res.getWriter().write(jObject.toString());
				return;
			}			

			// 완료 
			CommonUtil.commonPrintLog("SUCCESS", className, "Channel Add OK", map);
			jObject.put("outputResult", "1");
			res.getWriter().write(jObject.toString());
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void addChannelAdUrl(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();

		try {
			HttpSession session = req.getSession();

			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			int inputChannelNo = req.getParameter("inputChannelNo") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputChannelNo").toString())) : 0;
			int inputChannelAdType = req.getParameter("inputChannelAdType") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputChannelAdType").toString())) : 1;
			String[] inputChannelAdUrls = req.getParameterValues("inputChannelAdUrls");
			ArrayList<String> channeAdUrls = new ArrayList<String>();
			for(int i=0; i<inputChannelAdUrls.length;i++) {
				if(!inputChannelAdUrls[i].equals(""))
					channeAdUrls.add(CommonUtil.commonCleanXSS(inputChannelAdUrls[i]));
			}
			Calendar calendar = Calendar.getInstance();
			Timestamp inputCurrentDate = new java.sql.Timestamp(calendar.getTime().getTime());

			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");

			// Parameter check
			ArrayList<Object> parameterList = new ArrayList<Object>();
			parameterList.add(channeAdUrls);

			if(!CommonUtil.commonParameterCheck(parameterList)) {
				CommonUtil.commonPrintLog("FAIL", className, "Parameter Missing", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			// Channel Ad Video  
			for(int i=0; i<channeAdUrls.size();i++) {
				String url= "https://www.youtube.com/watch?v=" + channeAdUrls.get(i);
				Document doc = Jsoup.parse(new URL(url).openStream(), "utf-8", url);
				
				String viewsContent = doc.select(".watch-view-count").toString().substring(35, doc.select(".watch-view-count").toString().length()-8).replace(",", "");
				String nameContent = doc.select("#eow-title").first().attr("title");
				String thumbContent = doc.select("#watch7-content link[itemprop='thumbnailUrl']").first().attr("href");
				String dateContent = doc.select("#watch7-content meta[itemprop='datePublished']").first().attr("content") + " 00:00:00";
				int check = AdminDAO.addChannelAdUrl(inputChannelNo, channeAdUrls.get(i), nameContent, viewsContent, thumbContent, inputChannelAdType, Timestamp.valueOf(dateContent), inputCurrentDate);
				if(check != 1) {
					CommonUtil.commonPrintLog("FAIL", className, "Add  Channel Ad Url :" + inputChannelAdUrls[i], map);
				}			
			}
		
			// 완료 
			CommonUtil.commonPrintLog("SUCCESS", className, "Channel Ad Url Add OK", map);
			jObject.put("outputResult", "성공하였습니다.");
			res.getWriter().write(jObject.toString());
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void addChannelInfo(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();

		try {
			HttpSession session = req.getSession();

			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			int inputChannelNo = req.getParameter("inputChannelNo") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputChannelNo").toString())) : 0;
			int inputCategoryNo = req.getParameter("inputCategoryNo") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputCategoryNo").toString())) : 0;
			int inputCategoryNo2 = req.getParameter("inputCategoryNo2") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputCategoryNo2").toString())) : 0;
			int inputCategoryNo3 = req.getParameter("inputCategoryNo3") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputCategoryNo3").toString())) : 0;
			int inputMcnNo = req.getParameter("inputMcnNo") != "" ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputMcnNo").toString())) : 0;

			Calendar calendar = Calendar.getInstance();
			Timestamp inputCurrentDate = new java.sql.Timestamp(calendar.getTime().getTime());

			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");

			// Parameter check
			ArrayList<Object> parameterList = new ArrayList<Object>();
			parameterList.add(inputChannelNo);
			parameterList.add(inputCategoryNo);

			if(!CommonUtil.commonParameterCheck(parameterList)) {
				CommonUtil.commonPrintLog("FAIL", className, "Parameter Missing", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			

			String category = "";
			
			category = inputCategoryNo + ";";
			if(inputCategoryNo2 != 1) category += inputCategoryNo2 + ";";
			if(inputCategoryNo3 != 1) category += inputCategoryNo3 + ";";

			
			Channel channel = ChannelDAO.getChannelByNo(inputChannelNo);			
			int check = AdminDAO.setChannelInfo(inputChannelNo, category, inputMcnNo, inputCurrentDate);
			if(check != 1) {
				CommonUtil.commonPrintLog("FAIL", className, "Add  Channel Info", map);
			}		
			
			/// 카테고리 기존 카테고리와 비교
			ArrayList<String> originalCategory = CommonUtil.commonSpiltBySemicolon(channel.getRacChannelCategory());
			if(originalCategory.size() == 1) {
				if(Integer.parseInt(originalCategory.get(0)) != inputCategoryNo) { 
					AdminDAO.deleteChannelCategory(inputChannelNo, Integer.parseInt(originalCategory.get(0)));
					if(inputCategoryNo != 1)
						AdminDAO.addChannelCategory(inputChannelNo, inputCategoryNo);
				}
				
				if(inputCategoryNo2 != 1)
					AdminDAO.addChannelCategory(inputChannelNo, inputCategoryNo2);
				if(inputCategoryNo3 != 1)
					AdminDAO.addChannelCategory(inputChannelNo, inputCategoryNo3);
				
			} else if(originalCategory.size() == 2) {
				if(Integer.parseInt(originalCategory.get(0)) != inputCategoryNo) { 
					AdminDAO.deleteChannelCategory(inputChannelNo, Integer.parseInt(originalCategory.get(0)));
					if(inputCategoryNo != 1)
						AdminDAO.addChannelCategory(inputChannelNo, inputCategoryNo);
				}
				if(Integer.parseInt(originalCategory.get(1)) != inputCategoryNo2) { 
					AdminDAO.deleteChannelCategory(inputChannelNo, Integer.parseInt(originalCategory.get(1)));
					if(inputCategoryNo2 != 1)
						AdminDAO.addChannelCategory(inputChannelNo, inputCategoryNo2);
				}
				
				if(inputCategoryNo3 != 1)
					AdminDAO.addChannelCategory(inputChannelNo, inputCategoryNo3);
				
			} else if(originalCategory.size() == 3) {
				if(Integer.parseInt(originalCategory.get(0)) != inputCategoryNo) { 
					AdminDAO.deleteChannelCategory(inputChannelNo, Integer.parseInt(originalCategory.get(0)));
					if(inputCategoryNo != 1)
						AdminDAO.addChannelCategory(inputChannelNo, inputCategoryNo);
				}
				if(Integer.parseInt(originalCategory.get(1)) != inputCategoryNo2) { 
					AdminDAO.deleteChannelCategory(inputChannelNo, Integer.parseInt(originalCategory.get(1)));
					if(inputCategoryNo2 != 1)
						AdminDAO.addChannelCategory(inputChannelNo, inputCategoryNo2);
				}
				if(Integer.parseInt(originalCategory.get(2)) != inputCategoryNo3) { 
					AdminDAO.deleteChannelCategory(inputChannelNo, Integer.parseInt(originalCategory.get(2)));
					if(inputCategoryNo3 != 1)
						AdminDAO.addChannelCategory(inputChannelNo, inputCategoryNo3);
				}		
				
				
			}
			
			// 완료 
			CommonUtil.commonPrintLog("SUCCESS", className, "Channel Info Add OK", map);
			jObject.put("outputResult", "1");
			res.getWriter().write(jObject.toString());
			return;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void getChannelInfo(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();
		
		try {
			HttpSession session = req.getSession();

			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			int inputChannelNo = req.getParameter("inputChannelNo") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputChannelNo").toString())) : 0;

			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			if(!(sessionMemberNo>0)) {
				CommonUtil.commonPrintLog("FAIL", className, "No Admin Member", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			/// Check Admin Member 


			Channel channel = AdminDAO.getChannel(inputChannelNo);
			ArrayList<ChannelCategory> categoryList = AdminDAO.getChannelCategory(inputChannelNo);
			for(int i=0; i<categoryList.size();i++) {
				switch(i) {
					case 0:
						jObject.put("outputCategoryNo", categoryList.get(i).getRacCategoryNo());
						break;
					case 1:
						jObject.put("outputCategoryNo2", categoryList.get(i).getRacCategoryNo());
						break;
					case 2:
						jObject.put("outputCategoryNo3", categoryList.get(i).getRacCategoryNo());
						break;
				}
			}
			
			if(categoryList.size() == 1) {
				jObject.put("outputCategoryNo2", 1);
				jObject.put("outputCategoryNo3", 1);

			} else if(categoryList.size() == 2) {
				jObject.put("outputCategoryNo3", 1);	
			}
			
			jObject.put("outputChannelNo", channel.getRacChannelNo());
			jObject.put("outputChannelTitle", channel.getRacChannelTitle());
			jObject.put("outputChannelUrl", channel.getRacChannelUrl());
			jObject.put("outputChannelCategory", CommonUtil.getChannelCategoryList(channel.getRacChannelCategory()));

			CommonUtil.commonPrintLog("SUCCESS", className, "Get Channel info OK", map);
			req.setAttribute("result", jObject);
			req.getRequestDispatcher("/02_page/Admin/info.jsp").forward(req, res);

			return;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void getChannelAdUrl(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();
		
		try {
			HttpSession session = req.getSession();

			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			int inputChannelNo = req.getParameter("inputChannelNo") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputChannelNo").toString())) : 0;

			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			if(!(sessionMemberNo>0)) {
				CommonUtil.commonPrintLog("FAIL", className, "No Admin Member", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			/// Check Admin Member 


			// 현재 대출 가능한 도서(book_lending_possible이 true인 목록만 가져옴)
			Channel channel = AdminDAO.getChannel(inputChannelNo);

			jObject.put("outputChannelNo", channel.getRacChannelNo());
			jObject.put("outputChannelTitle", channel.getRacChannelTitle());
			jObject.put("outputChannelUrl", channel.getRacChannelUrl());

			CommonUtil.commonPrintLog("SUCCESS", className, "Get Channel ad url OK", map);
			req.setAttribute("result", jObject);
			req.getRequestDispatcher("/02_page/Admin/adUrl.jsp").forward(req, res);

			return;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void getChannelCost(HttpServletRequest req, HttpServletResponse res) {

		HashMap<String, String> map = new HashMap<String, String>();
		
		try {
			HttpSession session = req.getSession();

			int sessionMemberNo = session.getAttribute("racMemberNo") != null ? Integer.parseInt(session.getAttribute("racMemberNo").toString()) : 0;
			int inputChannelNo = req.getParameter("inputChannelNo") != null ? Integer.parseInt(CommonUtil.commonCleanXSS(req.getParameter("inputChannelNo").toString())) : 0;

			JSONObject jObject = new JSONObject();
			res.setContentType("application/json");
			res.setCharacterEncoding("UTF-8");
			
			if(!(sessionMemberNo>0)) {
				CommonUtil.commonPrintLog("FAIL", className, "No Admin Member", map);
				jObject.put("outputResult", "-1");
				res.getWriter().write(jObject.toString());
				return;
			}
			
			/// Check Admin Member 


			// 현재 대출 가능한 도서(book_lending_possible이 true인 목록만 가져옴)
			Channel channel = AdminDAO.getChannel(inputChannelNo);

			jObject.put("outputChannelNo", channel.getRacChannelNo());
			jObject.put("outputChannelTitle", channel.getRacChannelTitle());
			jObject.put("outputChannelUrl", channel.getRacChannelUrl());

			CommonUtil.commonPrintLog("SUCCESS", className, "Get Channel cost OK", map);
			req.setAttribute("result", jObject);
			req.getRequestDispatcher("/02_page/Admin/cost.jsp").forward(req, res);

			return;
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
