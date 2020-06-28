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
public class CommandSnippet extends Command {

	public CommandSnippet() {
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
				.filter(Filters.eq("_id", args)) //filter so that its only the ones with the correct
				.iterator(new FindOptions()
						.limit(1)) //limit to one item
				.tryNext(); //get that item

		if (snippet != null) {
		//print that item
		event.reply(snippet.asReply());
		
		snippet.incUses();
		SnippetsMorphiaConnection.getDatastore().save(snippet);
		
		} else {
			event.reply("There exists no snippet with this name");
		}
	}
}