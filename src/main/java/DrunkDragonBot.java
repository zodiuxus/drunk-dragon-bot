import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DrunkDragonBot {
    public static JDA jda;
    public static MongoDatabase database;
    private static final Path jdatoken = Path.of("./jdatoken.txt");
    private static final Path mdbtoken = Path.of("./mdbtoken.txt");
    public static final String[] activities = new String[]{
            "a horrible drinking game",
            "Dwarf Fortress",
            "with a Red Dragon's nest",
            "jokes on Elvish orphans",
            "Blackout-Jack",
            "Deep Rock Galactic",
            "with the hearts of nations",
            "some weird robot ninja game"
    };

    public static void main(String[] args) throws LoginException, IOException {

        jda = JDABuilder.createDefault(Files.readString(jdatoken)).setActivity(Activity.playing(activities[(int) (System.currentTimeMillis()*10%activities.length)])).build();

        ConnectionString connectionString = new ConnectionString(Files.readString(mdbtoken));
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        database = mongoClient.getDatabase("ActiveGames");

        jda.addEventListener(new Listener());
    }

    public static MongoDatabase getDatabase() {
        return database;
    }
}