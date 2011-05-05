package com.cmps101.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Frame;

public class GameInfo extends DialogBox {
	
	private final GameResources res = GameResources.INSTANCE;
	
	public GameInfo() {
		super(true,true);
		setText(res.infoCaption);
		addStyleName(res.css().infoBox());
		
		// Create our info page in an iframe
		Frame frame = new Frame();
		frame.setUrl(GameResources.pageUrl);
		frame.setSize(res.css().infoWidth() + "px",
				res.css().infoHeight() + "px");
		setGlassEnabled( true );
		setWidget(frame);
		frame.setStyleName(res.css().infoFrame());
		center();
	}
	/*
	public boolean loadHTML() {
		if (loaded)
			return true;
		loaded = true;
		BufferedReader reader;
		// Try and load file
		try {
			reader = new BufferedReader(new FileReader(infoPagePath));
		} catch (FileNotFoundException e1) { return false; }
		String line = null;
		StringBuilder builder = new StringBuilder();
		// Try and append the contents to a string
		try {
			while ( (line = reader.readLine()) != null) {
				builder.append(line);
				builder.append("\n");
			}
		} catch (IOException e) { return false; }
		// Check the contents
		String string = builder.toString();
		if (string.length() > 1) {
			HTML = builder.toString();
			return true;
		}
		// No file loaded
		return false;
	}
	*/
	
}
