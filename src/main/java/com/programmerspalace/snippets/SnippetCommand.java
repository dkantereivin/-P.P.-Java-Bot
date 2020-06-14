package com.programmerspalace.snippets;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;

/**
 *  A command for printing a snippet if you know the name
 *  
 *  @author Sam
 *
 */
public class SnippetCommand extends Command {

	public SnippetCommand() {
		this.name = "snippet";
		this.arguments = "<snippet name>";
		this.help = "find pre-written code";
	}

	@Override
	protected void execute(CommandEvent event) {
		//get the arguments, which should be the name
		String args = event.getArgs();

		//find the snippet
		Snippet snippet = SnippetsMorphiaConnection.getDatastore()
				.find(Snippet.class) //use the snippets collection
				.filter(Filters.eq("title", args)) //filter so that its only the ones with the correct
				.iterator(new FindOptions()
						.limit(1)) //limit to one item
				.tryNext(); //get that item

		//print that item
		//TODO replace with an embed builder
		event.reply(">>> " + snippet.getTitle() + "\n" + snippet.getTags() + "\n```" + snippet.getLanguage() + "\n" + snippet.getCode() + "```");
	}
}