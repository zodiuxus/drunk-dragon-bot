import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import org.apache.commons.lang3.StringUtils;

public class Listener extends ListenerAdapter {
    public static final String prefix = "dd!";

    public static Document document = new Document();

    public static MongoDatabase database = DrunkDragonBot.getDatabase();

    public static final EmbedBuilder embedBuilder = new EmbedBuilder();

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);

    private static MongoCollection<Document> collection;

    private static final HashSet<String> helperSet = new HashSet<>(){{
        add("weekday");
        add("time");
        add("system");
        add("slots");
        add("setting");
        add("recurrence");
    }};


    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);
        LOGGER.info("{} has started the first round of drinks.", event.getJDA().getSelfUser());
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        super.onGuildMessageReceived(event);

        User user = event.getAuthor();

        String[] args = event.getMessage().getContentRaw().split(" ");

        if (user.isBot()) return;

        if(args[0].equalsIgnoreCase(prefix + "games")) {
            event.getChannel().sendMessage("Retreiving a list of active games...").queue();
            collection = database.getCollection("games");
            for (Document doc: collection.find()) {
                embedBuilder.setTitle("Game: " + StringUtils.capitalize(doc.getString("name")));
                embedBuilder.setAuthor("Game Master: " + user.getAsTag());
                for (String key: helperSet) {
                    embedBuilder.addField(StringUtils.capitalize(key), StringUtils.capitalize(doc.get(key).toString()), false);
                }
                event.getChannel().sendMessage(embedBuilder.build()).queue();
                embedBuilder.clear();
            }
        }

        if(args[0].equalsIgnoreCase(prefix + "addgame")) {

            if (args.length < 4) {
                embedBuilder.setTitle("Invalid command usage!");
                embedBuilder.addField("", "Invalid arguments provided, please use the command in the following manner:", false);
                embedBuilder.addField("", "dd!addgame [system] [game name (in one word - for now)] [weekday] [time in CEST]", false);
                embedBuilder.addField("", "Optional arguments (after the first four) are [recurrence, default is weekly] [slots, defaults to 4] [setting, default is standard fantasy]", false);
                embedBuilder.addField("", "Do note that if you insert these in an incorrect order, they may show up in the incorrect positions.", false);
                event.getChannel().sendMessage(embedBuilder.build()).queue();
                embedBuilder.clear();
            }

            else {
                document.append("dm", event.getAuthor().getAsTag());
                document.append("system", args[1].toUpperCase());
                document.append("name", args[2]);
                document.append("weekday", args[3]);
                document.append("time", args[4]);

                if (args.length == 6) document.append("recurrence", args[5]);
                else document.append("recurrence", "weekly");

                if (args.length == 7) document.append("slots", Integer.parseInt(args[6]));
                else document.append("slots", 4);

                if (args.length == 8) document.append("setting", args[7]);
                else document.append("setting", "standard");

                database.getCollection("games").insertOne(document);
                document.clear();
                event.getChannel().sendMessage("Game successfully added!").queue();
            }
        }
        //TODO: create a conflict checker (weekday + time if the same weekday)
    }
}
