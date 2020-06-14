package com.programmerspalace.snippets;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;

/**
 * A tag relates to a snippet
 * A snippet contains 0, 1 or many tags
 * A tag represents something to do with the snippet, e.g. "file handling" "output"
 * 
 * @author Sam
 *
 */
@Entity("tags")
public class Tag {
	
	@Id
	private String tag;
	
	public Tag() {
		super();
	}
	
	public Tag(String tag) {
		super();
		this.tag = tag;
	}

	public String getTag() {
		return tag;
	}
	
}
