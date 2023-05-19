[![Status Badge](https://github.com/ilotterytea/bot/actions/workflows/build.yml/badge.svg)](https://github.com/ilotterytea/bot/actions/workflows/build.yml)

# Huinyabot
A utility and entertainment multi-chat Twitch bot. The bot is built in Java, Gradle and uses Twitch4j as the Twitch API.
This project is for me to learn more about Java and all its tricks.

## Prerequisites
+ PostgreSQL 15
+ JDK 11

## Building from sources
### 1. Cloning the repo
```shell
git clone https://github.com/ilotterytea/bot.git -b 1.4.0
cd bot
```

### 2. Setting up the Hibernate ORM configuration
Create a file `hibernate.cfg.xml` in `src/main/resources` and put this template there.
Replace the fields `DATABASE_NAME`, `USERNAME`, `PASSWORD`.
```xml
<?xml version='1.0' encoding='utf-8'?>
<!DOCTYPE hibernate-configuration SYSTEM "http://www.hibernate.org/dtd/hibernate-configuration-3.0.dtd">
<hibernate-configuration>
    <session-factory>
        <property name="connection.driver_class">org.postgresql.Driver</property>
        <property name="connection.url">jdbc:postgresql://localhost:5432/DATABASE_NAME</property>
        <property name="connection.username">USERNAME</property>
        <property name="connection.password">PASSWORD</property>

        <property name="hibernate.dialect">org.hibernate.dialect.PostgreSQLDialect</property>
        <property name="current_session_context_class">thread</property>
        <property name="cache.provider_class">org.hibernate.cache.internal.NoCacheProvider</property>
        <property name="show_sql">false</property>
        <property name="hbm2ddl.auto">update</property>
    </session-factory>
</hibernate-configuration>
```

### 3. Build the source
```shell
./gradlew shadowJar
cd build/libs
```

### 4. Create a configuration file (config.properties)
```properties
twitch.oauth2_token=oauth:your_token_here
twitch.access_token=access_token_from_your_twitch_application
``` 

> The `twitch.access_token` is the bot's token received when logging into the application from the bot's account.
> Token must grant rights `moderator:read:chatters`, otherwise will not work `!massping`, `MASSPING` flag for stream events.
> Also, the bot must be a moderator on channels that run massping related commands.

### 5. Run the bot
```shell
java -jar bot-1.4.0-all.jar
```