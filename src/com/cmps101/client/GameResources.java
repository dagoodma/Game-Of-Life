package com.cmps101.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

public interface GameResources extends ClientBundle {
	public static final GameResources INSTANCE = GWT.create(GameResources.class);
	public static final String infoCaption = "Information";
	public static final String pageUrl = "infoPage.htm";
	
	@Source("com/cmps101/client/Life.css")
	GameCss css();
	
	public interface GameCss extends CssResource {
		int gameWidth();
		int gameHeight();
		int infoWidth();
		int infoHeight();
		String wrapper();
		String gameWindow();
		String playButton();
		String stopButton();
		String speedListBox();
		String presetListBox();
		String infoButton();
		String infoFrame();
		String infoBox();
	}

}
