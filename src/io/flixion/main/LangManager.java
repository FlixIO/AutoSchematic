package io.flixion.main;

import java.util.HashMap;

public class LangManager {
	private HashMap<String, String> langHash = new HashMap<>();

	public HashMap<String, String> getLangHash() {
		return langHash;
	}

	public void setLangHash(HashMap<String, String> langHash) {
		this.langHash = langHash;
	}
}
