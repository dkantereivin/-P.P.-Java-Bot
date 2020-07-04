package com.programmerspalace.snippets;

import java.util.Arrays;
import java.util.stream.Collectors;

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
	static final long millisecondsInADay = 1000 * 60 * 60 * 24;

	@Id
	private String title = "";
	private Language language = new Language("");
	private Tag[] tags = new Tag[] {};
	private String code = "";
	private float uses;
	private long lastUsed;

	/**
	 * defualt constructor, do not use to create a snippet
	 */
	public Snippet() {
		super();
		uses = 0;
		lastUsed = System.currentTimeMillis();
	}

	/**
	 * use this constructor to create a snippet
	 * 
	 * @param title the name of the snippet, will be shown on all menus
	 * @param language the coding language the snippet is written in e.g. java, python
	 * @param tags the tags relating to the snippet, used for identifying and searching through snippets
	 * @param code the actual code of the snippet, will be shown in a discord code block formatted in the langauge
	 */
	public Snippet(String title, Language language, Tag[] tags, String code) {
		super();
		uses = 0;
		lastUsed = System.currentTimeMillis();
		this.language = language;
		this.title = title;
		this.tags = tags;
		this.code = code;
	}

	/**
	 * @return the language the snippet was written for
	 */
	public Language getLanguage() {
		return language;
	}
	public void setLanguage(Language language) {
		this.language = language;
	}
	
	/**
	 * @return the name of the snippet
	 */
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @return returns an array of all the tags used in the snippet
	 */
	public Tag[] getTags() {
		return tags;
	}
	public void setTags(Tag[] tags) {
		this.tags = tags;
	}
	public void addTag(Tag tag) {
		tags = Arrays.copyOf(tags, tags.length + 1);
		tags[tags.length-1] = tag;
	}
	
	/**
	 * @return returns the code relating to the snippet, unformatted
	 */
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	
	/**
	 * the number of uses goes down over time, 
	 * @return a number representing the uses of a snippet recently
	 */
	public float getUses() {
		long now = System.currentTimeMillis();
		int daysPassed = (int) (now/millisecondsInADay - lastUsed/millisecondsInADay);

		if (daysPassed > 0) {
			uses /= Math.pow(2, daysPassed);
			lastUsed = now;
		}
				
		return uses;
	}
	/**
	 * increases the number of uses by 1
	 */
	public void incUses() {
		uses = getUses() + 1;
	}

	/**
	 * @return returns a formatted string representation of the snippet ready for sending as a discord message
	 */
	public String asReply() {
		return ">>> " + getTitle() + "\n"
				+ getLanguage().toString() + ", "
				+ Arrays.asList(getTags()).stream().map(Tag::toString).collect(Collectors.joining(", ")) + "\n```"
				+ getLanguage() + "\n"
				+ getCode() + "```";
	}
}
