package com.example.xlistview;

public class GetInfoByModuleModel {
	private String IGUID;
	private String ModuleID;
	private String Title;
	private String SimpleTitle;
	private String SubTitle;
	private String PubDate;
	private String PubDateEnd;
	private String ShowTime;
	private String InputTime;
	private String HttpPath;

	public GetInfoByModuleModel(){
		
	}
	public GetInfoByModuleModel(String iGUID, String moduleID, String title, String simpleTitle, String subTitle, String pubDate, String pubDateEnd, String showTime, String inputTime, String httpPath) {
	
		IGUID = iGUID;
		ModuleID = moduleID;
		Title = title;
		SimpleTitle = simpleTitle;
		SubTitle = subTitle;
		PubDate = pubDate;
		PubDateEnd = pubDateEnd;
		ShowTime = showTime;
		InputTime = inputTime;
		HttpPath = httpPath;
	}

	public String getIGUID() {
		return IGUID;
	}

	public void setIGUID(String iGUID) {
		IGUID = iGUID;
	}

	public String getModuleID() {
		return ModuleID;
	}

	public void setModuleID(String moduleID) {
		ModuleID = moduleID;
	}

	public String getTitle() {
		return Title;
	}

	public void setTitle(String title) {
		Title = title;
	}

	public String getSimpleTitle() {
		return SimpleTitle;
	}

	public void setSimpleTitle(String simpleTitle) {
		SimpleTitle = simpleTitle;
	}

	public String getSubTitle() {
		return SubTitle;
	}

	public void setSubTitle(String subTitle) {
		SubTitle = subTitle;
	}

	public String getPubDate() {
		return PubDate;
	}

	public void setPubDate(String pubDate) {
		PubDate = pubDate;
	}

	public String getPubDateEnd() {
		return PubDateEnd;
	}

	public void setPubDateEnd(String pubDateEnd) {
		PubDateEnd = pubDateEnd;
	}

	public String getShowTime() {
		return ShowTime;
	}

	public void setShowTime(String showTime) {
		ShowTime = showTime;
	}

	public String getInputTime() {
		return InputTime;
	}

	public void setInputTime(String inputTime) {
		InputTime = inputTime;
	}

	public String getHttpPath() {
		return HttpPath;
	}

	public void setHttpPath(String httpPath) {
		HttpPath = httpPath;
	}

}
