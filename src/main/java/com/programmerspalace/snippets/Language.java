package com.programmerspalace.snippets;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * A language represents a coding language
 * Used to describe a snippet
 * 
 * @author Sam
 *
 */

@Entity("languages")
public class Language {
	
	@Id
	private String language;
	
	public Language() {
		super();
	}
	
	public Language(String language) {
		super();
		this.language = language;
	}

	@Override
	public String toString() {
		return language;
	}
	
}