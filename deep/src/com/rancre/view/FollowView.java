package com.rancre.view;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.rancre.controller.FollowContorller;
import com.rancre.util.CommonUtil;

public class FollowView extends HttpServlet {
	private static final String className = "FollowView";
	
	public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		process(req,res);
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		process(req,res);
	}
	
	public void process(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
		
		req.setCharacterEncoding("UTF-8");
		HashMap<String, String> map = new HashMap<String, String>();

		try {
			String action = req.getParameter("action") != null ? CommonUtil.commonCleanXSS(req.getParameter("action").toString()) : "";
			
			switch(action) {
			case "getFollower":
				map.put("ACTION", "getFollower");
				CommonUtil.commonPrintLog("REQUEST", this.getClass().getSimpleName(), "User Request In", map);
				FollowContorller.getFollower(req,res);
				break;
			case "getFollowing":
				map.put("ACTION", "getFollowing");
				CommonUtil.commonPrintLog("REQUEST", this.getClass().getSimpleName(), "User Request In", map);
				FollowContorller.getFollowing(req,res);
				break;
			case "countFollow":
				map.put("ACTION", "countFollow");
				CommonUtil.commonPrintLog("REQUEST", this.getClass().getSimpleName(), "User Request In", map);
				FollowContorller.countFollow(req,res);
				break;				
			case "addFollow":
				map.put("ACTION", "addFollow");
				CommonUtil.commonPrintLog("REQUEST", this.getClass().getSimpleName(), "User Request In", map);
				FollowContorller.addFollow(req,res);
				break;
			case "cancelFollow":
				map.put("ACTION", "cancelFollow");
				CommonUtil.commonPrintLog("REQUEST", this.getClass().getSimpleName(), "User Request In", map);
				FollowContorller.cancelFollow(req,res);
				break;
			default:
				CommonUtil.commonPrintLog("ERROR", this.getClass().getSimpleName(), "Incorrect Action Parameter (action : " + action + ")", map);
				break;
			}
		} catch (Exception e) {
				
		}
		
	}
} 