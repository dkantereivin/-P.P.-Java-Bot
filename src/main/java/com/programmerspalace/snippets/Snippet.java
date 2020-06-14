package com.programmerspalace.snippets;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * A snippet is a section of code
 * Snippets have meta data: id, language, title, tags
 * 
 * @author Sam
 *
 */
@Entity("snippets")
public class Snippet {
	@Id
	private String id;
	private String language;
	private String title;
	private String[] tags;
	private String code;
	
	public Snippet() {
		super();
		//generate id
		this.id = Long.toHexString(System.nanoTime()) + Long.toHexString((long) (Math.random()*Long.MAX_VALUE));
	}
	
	public Snippet(String title, String language, String[] tags, String code) {
		super();
		//generate id
		this.id = Long.toHexString(System.nanoTime()) + Long.toHexString((long) (Math.random()*Long.MAX_VALUE));
		this.language = language;
		this.title = title;
		this.tags = tags;
		this.code = code;
	}

	public String getId() {
		return id;
	}

	public String getLanguage() {
		return language;
	}
	
	public String getTitle() {
		return title;
	}

	public String[] getTags() {
		return tags;
	}

	public String getCode() {
		return code;
	}
	
}
