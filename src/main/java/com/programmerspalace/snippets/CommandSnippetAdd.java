package com.programmerspalace.snippets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.internal.MorphiaCursor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

class SnippetAdd {
	EventWaiter waiter;
	Snippet snippet = new Snippet();
	Message message;

	/**
	 * a convenient way of sending an error message, gets deleted after 10s
	 * @param channel
	 * @param message
	 */
	protected void error(MessageChannel channel, String message) {
		new Thread(() -> {
			Message messsage = channel.sendMessage(message).complete();

			try {
				//wait 10s
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//remove the error message
			messsage.delete().queue();
		}).start();
	}

	/**
	 * create the command object
	 * @param waiter
	 * @param event
	 */
	public SnippetAdd(EventWaiter waiter, CommandEvent event) {
		this.waiter = waiter;
		String title = event.getArgs();

		if (title.trim() == "") {
			error(event.getChannel(), "Name cannot be empty");
			return;
		}

		//ensure name is populated and unique
		MorphiaCursor<Snippet> snippets = SnippetsMorphiaConnection.getDatastore()
				.find(Snippet.class)
				.iterator();

		boolean found = false;
		while(snippets.hasNext() && !found) {
			Snippet snippet = snippets.next();

			if (snippet.getTitle().equals(title)) {
				found = true;
				this.snippet = snippet;
			}
		}

		snippet.setTitle(title);

		message = event.getChannel().sendMessage(snippet.asReply()).complete();
		attachWaiter(event.getEvent());


		MessageBuilder message = new MessageBuilder();
		EmbedBuilder embed = new EmbedBuilder();

		//update the filters message
		message = new MessageBuilder();
		embed = new EmbedBuilder();

		//add all languages
		MorphiaCursor<Language> languages = SnippetsMorphiaConnection.getDatastore()
				.find(Language.class)
				.iterator();

		String languagesstr = "";
		boolean first = true;
		while(languages.hasNext()) {
			languagesstr += "`";

			if (first) {
				first = false;
			} else {
				languagesstr += ", `";
			}

			languagesstr += languages.next();
		}
		languagesstr += "`";

		embed.addField("Languages",
				languagesstr,
				false);

		//add all tags
		MorphiaCursor<Tag> tags = SnippetsMorphiaConnection.getDatastore()
				.find(Tag.class)
				.iterator();

		String tagstr = "";
		first = true;
		while(tags.hasNext()) {
			tagstr += "`";

			if (first) {
				first = false;
			} else {
				tagstr += ", `";
			}

			tagstr += tags.next();
		}
		tagstr += "`";

		embed.addField("Tags",
				tagstr,
				false);

		event.getChannel().sendMessage(message.setEmbed(embed.build()).build()).queue();
	}

	/**
	 * add the waiter to the message
	 * the waiter listens for input
	 * @param event
	 */
	private void attachWaiter(MessageReceivedEvent event) {
		waiter.waitForEvent(MessageReceivedEvent.class, 
				// make sure it's by the same user, and in the same channel, and for safety, a different message
				(e -> e.getAuthor().equals(event.getAuthor()) 
						&& e.getChannel().equals(event.getChannel()) 
						&& !e.getMessage().equals(event.getMessage())), 
				// respond, inserting the name they listed into the response
				e -> {
					if (addElement(e.getMessage().getContentRaw()))
						attachWaiter(e);
				},
				// if the user takes more than 5 minutes, time out
				5, TimeUnit.MINUTES,
				() -> event.getChannel().sendMessage("Snippet add command timed out"));
	}

	/**
	 * when the listener detects input, evaluate it
	 * @param element
	 * @return
	 */
	private boolean addElement(String element) {
		element = element.trim();

		//quit the command if the input is !cancel
		if (element.equals("!cancel")) {
			message.getChannel().sendMessage("Cancelled").queue();;
			return false;
		}

		//save if the input is !save
		if (element.equals("!save")) {
			SnippetsMorphiaConnection.getDatastore().save(snippet);
			message.getChannel().sendMessage("Saved").queue();
			return false;
		}


		//if its code add it as the code
		if (element.startsWith("```") && element.endsWith("```")) {
			element = element.substring(3, element.length()-3);
			int partition = element.indexOf("\n");

			String langName = element.substring(0, partition).trim();

			MorphiaCursor<Language> languages = SnippetsMorphiaConnection.getDatastore()
					.find(Language.class)
					.iterator();

			boolean found = false;
			while(languages.hasNext() && !found) {
				Language language = languages.next();

				if (language.toString().equals(langName)) {
					found = true;

					snippet.setLanguage(language);
				}
			}
			if (!found) {
				error(message.getChannel(), "Language not recognised");
				return true;
			}

			snippet.setCode(element.substring(partition).trim());

		} else {
			MorphiaCursor<Tag> tags = SnippetsMorphiaConnection.getDatastore()
					.find(Tag.class)
					.iterator();

			boolean found = false;
			while(tags.hasNext() && !found) {
				Tag tag = tags.next();

				if (tag.toString().equals(element)) {
					found = true;

					snippet.addTag(tag);
				}
			}
			if (!found) {
				error(message.getChannel(), "Tag not recognised");
				return true;
			}
		}

		updateMessage();

		return true;

	}

	/**
	 * update the output message
	 */
	private void updateMessage() {
		message.editMessage(snippet.asReply()).complete();
	}
}

public class CommandSnippetAdd extends Command {
	EventWaiter waiter;

	public CommandSnippetAdd(EventWaiter waiter) {
		this.waiter = waiter;

		this.name = "snippetadd";
		this.aliases = new String[] {"snippetAdd"};
		this.arguments = "<snippet name>";
		this.help = "create a new snippet";
	}

	@Override
	protected void execute(CommandEvent event) {
		boolean permission = false;

		//read all roles from the database
		MorphiaCursor<Role> roles = SnippetsMorphiaConnection.getDatastore()
				.find(Role.class)
				.iterator();

		while (roles.hasNext() && !permission) {
			Role role = roles.next();

			//check each role against each role the member has, if any match member has permission
			permission = event.getMember().getRoles().stream()
					.filter(memberRole -> memberRole.getId().equals(role.getRoleId()))
					.count() > 0;
		}

		if (permission) {
			new SnippetAdd(waiter, event);
		} else {
			event.reply("You do not have permission for this command");
		}
	}

}
