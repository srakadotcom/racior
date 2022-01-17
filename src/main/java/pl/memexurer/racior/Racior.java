package pl.memexurer.racior;

import java.awt.Color;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.AnnotatedEventManager;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.requests.GatewayIntent;
import pl.memexurer.racior.lookup.LookupResponse;
import pl.memexurer.racior.lookup.LookupService;

public class Racior {

  private final LookupService service = new LookupService();

  private static EmbedBuilder getDefaultEmbedBuilder(User author) {
    return new EmbedBuilder()
        .setTitle("Bambik Botnet Lookup")
        .setThumbnail(
            "https://cdn.discordapp.com/attachments/927511988970602497/931992631804833843/3dgifmaker00080.gif")
        .setFooter("Komenda uzyta przez " + author.getAsTag(), author.getAvatarUrl());
  }

  public void startJda(String token) throws Exception {
    service.loadLookupHandlers();
    JDA jda = JDABuilder.createDefault(token, GatewayIntent.GUILD_MESSAGES)
        .setEventManager(new AnnotatedEventManager())
        .build();

    jda.addEventListener(this);
  }

  @SubscribeEvent
  public void onMessage(MessageReceivedEvent event) {
    if (event.getMessage().getContentRaw().startsWith("!ratcior")) {
      String content = event.getMessage().getContentRaw().substring(9);
      if (service.isBroken()) {
        event.getMessage().replyEmbeds(getDefaultEmbedBuilder(event.getAuthor())
                .setColor(Color.RED)
                .setDescription("Bot nie zostal skonfigurowany, jestes zjebany.").build())
            .queue();
        return;
      }

      event.getMessage().replyEmbeds(getDefaultEmbedBuilder(event.getAuthor())
              .setDescription("Rozpoczeto wyszukiwanie!").build())
          .queue(message -> {
            long start = System.currentTimeMillis();

            CompletableFuture<List<LookupResponse>> lookup = service.lookup(content);
            lookup.whenComplete((result, error) -> {
              if (error != null) {
                message.editMessageEmbeds(
                    getDefaultEmbedBuilder(event.getAuthor())
                        .setDescription(
                            "Wystapil blad: " + error.getMessage())
                        .setColor(Color.RED)
                        .build()
                ).queue();
              }
              if (!result.isEmpty()) {
                message.editMessageEmbeds(
                    getDefaultEmbedBuilder(event.getAuthor())
                        .setDescription(
                            "Znaleziono cos! (" + (System.currentTimeMillis() - start) + "ms)\n"
                                + result.stream()
                                .map(response -> response.server() + " - " + response.ip())
                                .collect(Collectors.joining("\n")))
                        .setColor(Color.GREEN)
                        .build()
                ).queue();
              } else {
                message.editMessageEmbeds(
                    getDefaultEmbedBuilder(event.getAuthor())
                        .setDescription(
                            "Pizda ocet nie znaleziono nic :C (" + (System.currentTimeMillis()
                                - start) + "ms)")
                        .setColor(Color.RED)
                        .build()
                ).queue();
              }
            });
          });
    } else if (event.getMessage().getContentRaw().startsWith("!addhaker")) {
      String[] arguments = event.getMessage().getContentRaw().substring(11).split(" ");

      try {
        service.registerLookupHandler(arguments[0], arguments[1]);
      } catch (IllegalArgumentException e) {
        replyAndDelete(event.getMessage(), e.getMessage());
      }
      replyAndDelete(event.getMessage(), "Pomyslnie zarejestrowano hakera!");
    } else if (event.getMessage().getContentRaw().startsWith("!listsperma")) {
      if (service.isBroken()) {
        replyAndDelete(event.getMessage(), "Lista hakeruw jest pusta!", 30);
        return;
      }

      replyAndDelete(event.getMessage(), "Lista hakeruw: \n" + service.getFormattedEntries(), 30);
    } else if (event.getMessage().getContentRaw().startsWith("!removehaker")) {
      String argument = event.getMessage().getContentRaw().substring(14);

      try {
        service.removeService(Integer.parseInt(argument));
      } catch (IllegalArgumentException e) {
        replyAndDelete(event.getMessage(), e.getMessage());
      }
      replyAndDelete(event.getMessage(), "Pomyslnie usunieto hakera!");
    }
  }

  private void replyAndDelete(Message trole, String content) {
    replyAndDelete(trole, content, 5);
  }

  private void replyAndDelete(Message trole, String content, int seconds) {
    trole.replyEmbeds(getDefaultEmbedBuilder(trole.getAuthor())
            .setColor(Color.WHITE)
            .setDescription(content).build())
        .queue(message -> message.delete().queueAfter(seconds, TimeUnit.SECONDS));
  }
}
