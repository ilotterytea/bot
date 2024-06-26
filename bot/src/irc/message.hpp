#pragma once

#include <functional>
#include <map>
#include <optional>
#include <sstream>
#include <string>
#include <vector>

#include "../utils/string.hpp"

namespace bot {
  namespace irc {
    enum MessageType { Privmsg, Ping, Notice };
    std::optional<MessageType> define_message_type(const std::string &msg);

    struct MessageSender {
        std::string login;
        std::string display_name;
        int id;

        std::map<std::string, int> badges;

        // More fields will be here
    };

    struct MessageSource {
        std::string login;
        int id;
    };

    template <MessageType T>
    struct Message;

    template <>
    struct Message<MessageType::Privmsg> {
        MessageSender sender;
        MessageSource source;
        std::string message;
    };

    template <MessageType T>
    std::optional<Message<T>> parse_message(const std::string &msg) {
      std::vector<std::string> parts = utils::string::split_text(msg, ' ');

      if (T == MessageType::Privmsg) {
        MessageSender sender;
        MessageSource source;

        Message<MessageType::Privmsg> message;

        std::string tags = parts[0];
        tags = tags.substr(1, tags.length());
        parts.erase(parts.begin());

        std::string user = parts[0];
        user = user.substr(1, user.length());

        std::vector<std::string> user_parts =
            utils::string::split_text(user, '!');

        sender.login = user_parts[0];

        parts.erase(parts.begin(), parts.begin() + 2);

        std::string channel_login = parts[0];
        source.login = channel_login.substr(1, channel_login.length());

        parts.erase(parts.begin());

        std::string chat_message = utils::string::join_vector(parts, ' ');
        message.message = chat_message.substr(1, chat_message.length());

        std::vector<std::string> tags_parts =
            utils::string::split_text(tags, ';');

        for (const std::string &tag : tags_parts) {
          std::istringstream iss(tag);
          std::string key;
          std::string value;

          std::getline(iss, key, '=');
          std::getline(iss, value);

          if (key == "display-name") {
            sender.display_name = value;
          } else if (key == "room-id") {
            source.id = std::stoi(value);
          } else if (key == "user-id") {
            sender.id = std::stoi(value);
          } else if (key == "badges") {
            std::vector<std::string> badges =
                utils::string::split_text(value, ',');

            std::map<std::string, int> map;

            for (const auto &badge : badges) {
              std::istringstream iss2(badge);
              std::string name;
              std::string value;

              std::getline(iss2, name, '/');
              std::getline(iss2, value);

              map.insert({name, std::stoi(value)});
            }

            sender.badges = map;
          }
        }

        message.sender = sender;
        message.source = source;

        return message;
      }

      return std::nullopt;
    }

    template <MessageType T>
    struct MessageHandler;

    template <>
    struct MessageHandler<MessageType::Privmsg> {
        using fn = std::function<void(Message<Privmsg> message)>;
    };

  }
}
