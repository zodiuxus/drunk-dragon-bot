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

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

public class Listener extends ListenerAdapter {
    public static final String prefix = "dd!";

    public static Document document = new Document();

    public static MongoDatabase database = DrunkDragonBot.getDatabase();

    public static HashMap<String, Collection<Object>> gamesMap = new HashMap<>();

    public static final EmbedBuilder embedBuilder = new EmbedBuilder();

    private static final Logger LOGGER = LoggerFactory.getLogger(Listener.class);


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
            embedBuilder.setTitle("List of active games:");
            MongoCollection<Document> collection = database.getCollection("games");
            for (Document doc : collection.find()) {
                gamesMap.put(doc.get("dm").toString(), Arrays.asList(doc.values().toArray()));
            }
            for(String str: gamesMap.keySet()) {
                embedBuilder.addField(str, gamesMap.get(str).toString(), false);
            }
            event.getChannel().sendMessage(embedBuilder.build()).queue();
        }

        if(args[0].equalsIgnoreCase(prefix + "addgame")) {

            /*
            TODO: make this thing look like this, not whatever I have set in here
            i.e "dd!addgame **kwargs:[system, default = 5e] [name] [weekday] [time in CEST]
            *args:[recurrence, default = weekly] [slots, def = 4] [setting, def = null] [duration] [other optional arguments]",
            all of which are processed as strings and stitched together to then be ported to a database,
            and retrieval of them will be run through in reverse order
             */

            document.append("dm", event.getAuthor().getAsTag());
            document.append("game", args[1]);
            database.getCollection("games").insertOne(document);
            document.clear();
            event.getChannel().sendMessage("Game successfully added!").queue();
        }
    }
}
