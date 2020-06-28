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
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;

/**
 * A command that provides a search dialog for snippets
 * @author Sam
 *
 */
class SnippetListFinder {
	private static int NUMBEROFRESULTS = 10;

	EventWaiter waiter;
	Message mainResponse;
	Message dmResponse;
	Message dmFilters;
	MessageChannel channel;

	Boolean timeout = false;

	List<Snippet> snippets;

	Language language = null;
	List<Tag> tags = new ArrayList<Tag>();
	int skipResults = 0;

	/**
	 * set up the messages for the dialog
	 * @param waiter
	 * @param event the command that initiated the event
	 */
	public SnippetListFinder(EventWaiter waiter, CommandEvent event) {
		this.waiter = waiter;
		this.channel = event.getChannel();

		dmResponse = event.getChannel().sendMessage(".").complete();
		dmFilters = event.getChannel().sendMessage(".").complete();
		updatedmMessages();

		attachWaiter(event.getEvent());
		attachReactWaiter(event.getEvent());
	}

	/**
	 * called when a event waiter times out
	 * @param forceTimeout true if this CommandSnippetList should time out
	 * @param reset event that is run if CommandSnippetList has not timed out
	 */
	protected void onTimeout(boolean forceTimeout, Runnable reset) {
		if (forceTimeout) {
			timeout = true;

			if (dmResponse != null) dmResponse.delete().queue();
			if (dmFilters != null) dmFilters.delete().queue();
			
			dmResponse = null;
			dmFilters = null;
		}

		if (timeout) return;

		reset.run();
	}

	/**
	 * attaches a message listener to a sent message
	 * @param event
	 */
	protected void attachWaiter(MessageReceivedEvent event) {
		waiter.waitForEvent(MessageReceivedEvent.class, 
				// make sure it's by the same user, and in the same channel, and for safety, a different message
				(e -> e.getAuthor().equals(event.getAuthor()) 
						&& e.getChannel().equals(channel) 
						&& !e.getMessage().equals(event.getMessage())), 
				// respond, inserting the name they listed into the response
				e -> {
					if (timeout) return;
					e.getMessage().delete().queue();
					onNewTag(e);
					attachWaiter(e);
				},
				// if the user takes more than a minute, time out
				10, TimeUnit.MINUTES,
				() -> onTimeout(true, () -> {}));
	}

	/**
	 * attaches a reaction listener to a message
	 * @param event
	 */
	protected void attachReactWaiter(MessageReceivedEvent event) {
		waiter.waitForEvent(MessageReactionAddEvent.class,
				e -> e.getUser().equals(event.getAuthor())
				&& e.getChannel().equals(channel),
				e -> {
					if (timeout) return;
					onReaction(e);
					attachReactWaiter(event);
				},
				1, TimeUnit.MINUTES,
				() -> onTimeout(false, () -> {attachReactWaiter(event);}));
	}

	/**
	 * when a tag is provided, add it to the list
	 * @param event
	 */
	protected void onNewTag(MessageReceivedEvent event) {
		String inputTag = event.getMessage().getContentRaw().toLowerCase();

		//find if the input is a language
		MorphiaCursor<Language> languages = SnippetsMorphiaConnection.getDatastore()
				.find(Language.class)
				.filter(Filters.eq("_id", inputTag))
				.iterator(new FindOptions()
						.limit(1));

		//if the input is a language
		if (languages.hasNext()) {	
			//set it as the language for the search
			language = languages.next();
			updatedmMessages();
			return;
		}

		//find if the input is a tag
		MorphiaCursor<Tag> tags = SnippetsMorphiaConnection.getDatastore()
				.find(Tag.class)
				.filter(Filters.eq("_id", inputTag))
				.iterator(new FindOptions()
						.limit(1));

		//if it is a tag
		if (tags.hasNext()) {
			Tag tag = tags.next();
			
			//find if the tag is already a filter
			Boolean contained = this.tags.parallelStream()
					.anyMatch(x -> x.equals(tag));
			
			//if the tag does not already exist
			if (!contained) {
				//add the tag
				this.tags.add(tag);
			} else {
				//if the tag does exist
				//remove it from the list
				this.tags = this.tags.parallelStream()
						.filter(x -> !x.equals(tag))
						.collect(Collectors.toList());
			}
			
			updatedmMessages();
			return;
		}

		//if the input is neither a language nor a tag, send an error
		new Thread(() -> {
			Message notFound = channel.sendMessage("Tag not found").complete();

			try {
				//wait 10s
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			//remove the error message
			notFound.delete().queue();
		}).start();
	}
	
	/**
	 * the unicode format for the number emotes
	 */
	public static final String[] numEmotes = {
			"0️⃣",
			"1️⃣",
			"2️⃣",
			"3️⃣",
			"4️⃣",
			"5️⃣",
			"6️⃣",
			"7️⃣",
			"8️⃣",
			"9️⃣"
	};
	
	/**
	 * clear and repopulate the reactions on the message
	 */
	protected void addReactions() {
		dmResponse.clearReactions().queue();
		
		//arrow navigation
		dmResponse.addReaction("◀️").queue();
		dmResponse.addReaction("▶️").queue();
		
		//add a number for each possible result
		for (int i = 0; i < numEmotes.length && i < snippets.size(); i++) {
			dmResponse.addReaction(numEmotes[i]).queue();
		}
	}
	
	protected void onReaction(MessageReactionAddEvent react) {		
		String reaction = react.getReaction().getReactionEmote().getName();
				
		if (reaction.equals("◀️")) {
			if (skipResults > 0) skipResults--;
			updatedmMessages();
			return;
		}
		
		if (reaction.equals("▶️")) {
			skipResults++;
			updatedmMessages();
			return;
		}
		
		int result = Arrays.asList(numEmotes).indexOf(reaction);
		
		if (result < 0) return;
		if (result >= snippets.size()) return;
		
		onTimeout(true, () -> {});
		
		Snippet snippet = snippets.get(result);
		
		channel.sendMessage(snippet.asReply()).queue();
		
		snippet.incUses();
		SnippetsMorphiaConnection.getDatastore().save(snippet);
	}

	/**
	 * the discord format for the number emotes
	 */
	private static final String[] numStrings = {
			":zero:",
			":one:",
			":two:",
			":three:",
			":four:",
			":five:",
			":six:",
			":seven:",
			":eight:",
			":nine:"
	};
	
	/**
	 * recalculate the message content, and edit the messages
	 */
	protected void updatedmMessages() {
		MessageBuilder message = new MessageBuilder();
		EmbedBuilder embed = new EmbedBuilder();

		//update the snippet results message
		embed.setTitle("Results");
		embed.setFooter("Page: " + skipResults);

		//find the snippet
		Query<Snippet> snippetsQuery = SnippetsMorphiaConnection.getDatastore()
				.find(Snippet.class); //use the snippets collection

		if (language != null) snippetsQuery = snippetsQuery.filter(Filters.eq("language", language));

		MorphiaCursor<Snippet> snippetsIterator = snippetsQuery
				.iterator(new FindOptions()
						.sort(Sort.descending("uses")));

		snippets = new ArrayList<Snippet>();

		//skip some messages
		int skipped = 0;
		while (snippetsIterator.hasNext() && skipped < skipResults*NUMBEROFRESULTS) {
			snippetsIterator.next();
			skipped++;
		}

		//add all necessary messages to the current snippet list
		int total = 0;
		while (snippetsIterator.hasNext() && total < NUMBEROFRESULTS) {
			Snippet snippet = snippetsIterator.next();

			//if it matches the tags
			long tagCount = tags.parallelStream()
					.parallel()
					.filter(x -> Arrays.asList(snippet.getTags()).parallelStream()
							.anyMatch(y -> y.equals(x)))
					.count();

			if (tagCount != tags.size()) continue;

			snippets.add(snippet);

			//add it to the embed
			if (snippet != null) {
				embed.addField(numStrings[total] + "   __**"+ snippet.getTitle() + "**__",
						(snippet.getLanguage()==null? "": ("`" + snippet.getLanguage() + "`, "))
						+ ((snippet.getTags().length>0)? ("`" + Arrays.stream(snippet.getTags()).map(Tag::toString).collect(Collectors.joining("`, `")) + "`"):""),
						false);
			}
			
			total++;
		}
		dmResponse.editMessage(message.setEmbed(embed.build()).build()).queue();



		//update the filters message
		message = new MessageBuilder();
		embed = new EmbedBuilder();

		embed.setTitle("Filters");
		embed.setDescription((language==null? "": ("`" + language.toString() + "`, "))
				+ ((tags.size()>0)? ("`" + tags.parallelStream().map(Tag::toString).collect(Collectors.joining("`, `")) + "`"):""));

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


		//search all snippets for every useful tag
		MorphiaCursor<Snippet> snippets = SnippetsMorphiaConnection.getDatastore()
				.find(Snippet.class)
				.iterator();

		List<Tag> filteredTags = new ArrayList<Tag>();
		while(snippets.hasNext()) {
			Snippet snippet = snippets.next();
			
			//the snippet must match the current tags
			long tagCount = tags.parallelStream()
					.parallel()
					.filter(x -> Arrays.asList(snippet.getTags()).parallelStream()
							.anyMatch(y -> y.equals(x)))
					.count();

			if (tagCount != tags.size()) continue;
			
			
			Arrays.asList(snippet.getTags()).stream()
					//if the snippet isnt in the filter
					.filter(x -> !tags.parallelStream()
							.anyMatch(y -> y.equals(x)))
					.forEach(x -> {
						//and doesnt already exist in the displayed list
						if (!filteredTags.stream()
							.anyMatch(y -> y.equals(x)))
							//add it to the display list
							filteredTags.add(x);
					});
		}
		
		//combine the display list into a string
		String tagsstr = "";
		first = true;
		for(Tag tag: filteredTags) {
			tagsstr += "`";

			if (first) {
				first = false;
			} else {
				tagsstr += ", `";
			}

			tagsstr += tag;
		}
		tagsstr += "`";

		embed.addField("Tags",
				tagsstr,
				true);

		embed.setFooter("Type in langauges or tags to filter the responses");
		dmFilters.editMessage(message.setEmbed(embed.build()).build()).queue();

		addReactions();
	}
}

public class CommandSnippetList extends Command {
	EventWaiter waiter;

	public CommandSnippetList(EventWaiter waiter) {
		this.name = "snippets";
		this.aliases = new String[] {"snippetlist"};
		this.arguments = "<tag tag tag...>";
		this.help = "search through a list of prewritten code using tags";
		this.waiter = waiter;
	}

	@Override
	protected void execute(CommandEvent event) {
		SnippetListFinder finder = new SnippetListFinder(waiter, event);
	}

}
