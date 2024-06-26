#pragma once

#include <optional>
#include <string>

#define GET_DATABASE_CONNECTION_URL(c)                                      \
  "dbname = " + c.database.name + " user = " + c.database.user +            \
      " password = " + c.database.password + " host = " + c.database.host + \
      " port = " + c.database.port

#define GET_DATABASE_CONNECTION_URL_POINTER(c)                                \
  "dbname = " + c->database.name + " user = " + c->database.user +            \
      " password = " + c->database.password + " host = " + c->database.host + \
      " port = " + c->database.port

namespace bot {
  struct DatabaseConfiguration {
      std::string name;
      std::string user;
      std::string password;
      std::string host;
      std::string port;
  };

  struct TwitchCredentialsConfiguration {
      std::string client_id;
      std::string token;
  };

  struct CommandConfiguration {
      bool join_allowed = true;
      bool join_allow_from_other_chats = false;
  };

  struct OwnerConfiguration {
      std::optional<std::string> name = std::nullopt;
      std::optional<int> id = std::nullopt;
  };

  struct UrlConfiguration {
      std::optional<std::string> help = std::nullopt;
      std::optional<std::string> paste_service = std::nullopt;
  };

  struct FunConfiguration {
      bool send_message_at_startup = false;
      std::string startup_lines;
  };

  struct Configuration {
      TwitchCredentialsConfiguration twitch_credentials;
      DatabaseConfiguration database;
      CommandConfiguration commands;
      OwnerConfiguration owner;
      UrlConfiguration url;
      FunConfiguration fun;
  };

  std::optional<Configuration> parse_configuration_from_file(
      const std::string &file_path);
}
