#include "seventv.hpp"

#include <algorithm>
#include <nlohmann/json.hpp>
#include <string>
#include <vector>

#include "../logger.hpp"
#include "cpr/api.h"
#include "cpr/cprtypes.h"
#include "cpr/response.h"
#include "ixwebsocket/IXWebSocketMessage.h"
#include "ixwebsocket/IXWebSocketMessageType.h"
#include "pqxx/internal/statement_parameters.hxx"

namespace bot::services {
  void SevenTVClient::run() {
    this->websocket.setOnMessageCallback(
        [this](const ix::WebSocketMessagePtr &msg) {
          switch (msg->type) {
            case ix::WebSocketMessageType::Message: {
              this->parse_message(msg->str);
              break;
            }
            case ix::WebSocketMessageType::Close: {
              this->handle_close_event();
              break;
            }
            default:
              break;
          }
        });

    this->websocket.run();
  }

  void SevenTVClient::parse_message(const std::string &message) {
    nlohmann::json json = nlohmann::json::parse(message);
    int op = json["op"];

    switch (op) {
        // Dispatch
      case 0: {
        this->handle_dispatch_event(json["d"]);
        break;
      }
      // Hello
      case 1: {
        log::info("7TV EventAPI", "Connected to 7TV EventAPI");
        break;
      }
      // Heartbeat
      case 2: {
        log::info("7TV EventAPI", "Heartbeat! Checking the channels...");
        this->subscribe_new_channels();
        break;
      }
      case 7: {
        this->handle_close_event();
        break;
      }
      default:
        break;
    }
  }

  void SevenTVClient::handle_dispatch_event(const nlohmann::json &json) {
    if (json["type"] != "emote_set.update") return;

    auto &body = json["body"];

    // Getting channel
    const std::string &emote_set_id = body["id"];
    std::string alias_id;

    for (auto it = this->ids.begin(); it != this->ids.end(); ++it) {
      if (it->second == emote_set_id) {
        alias_id = it->first;
        break;
      }
    }

    if (alias_id.empty()) {
      log::warn("7TV EventAPI", "Got dispatch event for emote set '" +
                                    emote_set_id +
                                    "' but corresponding alias ID not found");
      return;
    }

    pqxx::connection conn(GET_DATABASE_CONNECTION_URL(this->configuration));
    pqxx::work work(conn);

    pqxx::result rows = work.exec_params(
        "SELECT channels.alias_name, channel_preferences.locale FROM channels "
        "INNER JOIN channel_preferences "
        "ON channel_preferences.channel_id = channels.id AND 2 = "
        "ANY(channel_preferences.features) AND channels.opted_out_at IS NULL "
        "AND channels.alias_id = $1",
        alias_id);

    work.commit();
    conn.close();

    const pqxx::row &row = rows[0];
    const std::string &alias_name = row[0].as<std::string>(),
                      &locale = row[1].as<std::string>(), prefix = "7TV";

    // Getting the actor
    const std::string &actor_name = body["actor"]["username"];

    // Parsing the emotes
    std::vector<std::string> messages;

    if (body.contains("pulled")) {
      for (const auto &emote : body["pulled"]) {
        const std::string &name = emote["old_value"]["name"];
        messages.push_back(this->localization
                               .get_formatted_line(locale,
                                                   loc::LineId::EmotePulled,
                                                   {prefix, actor_name, name})
                               .value());
      }
    }

    if (body.contains("pushed")) {
      for (const auto &emote : body["pushed"]) {
        const std::string &name = emote["value"]["name"];
        messages.push_back(this->localization
                               .get_formatted_line(locale,
                                                   loc::LineId::EmotePushed,
                                                   {prefix, actor_name, name})
                               .value());
      }
    }

    if (body.contains("updated")) {
      for (const auto &emote : body["updated"]) {
        const std::string &name = emote["value"]["name"];
        const std::string &old_name = emote["old_value"]["name"];
        messages.push_back(
            this->localization
                .get_formatted_line(locale, loc::LineId::EmoteUpdated,
                                    {prefix, actor_name, old_name, name})
                .value());
      }
    }

    for (const std::string &message : messages) {
      this->irc_client.say(alias_name, message);
    }
  }

  void SevenTVClient::handle_close_event() { this->ids.clear(); }

  void SevenTVClient::subscribe_new_channels() {
    pqxx::connection conn(GET_DATABASE_CONNECTION_URL(this->configuration));
    pqxx::work work(conn);

    pqxx::result rows = work.exec(
        "SELECT channels.alias_id FROM channels INNER JOIN channel_preferences "
        "ON channel_preferences.channel_id = channels.id AND 2 = "
        "ANY(channel_preferences.features) AND channels.opted_out_at IS NULL");

    work.commit();
    conn.close();

    // Adding new channels
    for (const pqxx::row &row : rows) {
      std::string id = std::to_string(row[0].as<int>());
      this->join(id);
    }

    // Removing old channels
    for (const auto &pair : this->ids) {
      if (std::any_of(rows.begin(), rows.end(), [&pair](const pqxx::row &row) {
            return std::to_string(row[0].as<int>()) == pair.first;
          })) {
        continue;
      }

      this->part(pair.first);
    }
  }

  void SevenTVClient::join(const std::string &id) {
    cpr::Response response =
        cpr::Get(cpr::Url{"https://7tv.io/v3/users/twitch/" + id});

    if (response.status_code != 200) {
      log::error("7TV EventAPI", " Failed to get user ID " + id);
      return;
    }

    nlohmann::json json = nlohmann::json::parse(response.text);

    std::string emote_set_id = json["emote_set_id"];

    if (std::any_of(this->ids.begin(), this->ids.end(),
                    [&id](const auto &x) { return x.first == id; })) {
      const std::string &old_emote_set_id = this->ids[id];

      if (old_emote_set_id != emote_set_id) {
        this->unsubscribe(old_emote_set_id);
        this->subscribe(emote_set_id);

        this->ids[id] = emote_set_id;
      }
    } else {
      this->subscribe(emote_set_id);
      this->ids[id] = emote_set_id;
    }
  }

  void SevenTVClient::part(const std::string &id) {
    auto it = std::find_if(this->ids.begin(), this->ids.end(),
                           [&id](const auto &x) { return x.first == id; });
    if (it != this->ids.end()) {
      this->unsubscribe(this->ids[id]);
      this->ids.erase(it);
    }
  }

  void SevenTVClient::subscribe(const std::string &emote_set_id) {
    this->websocket.send(
        "{"
        "\"op\": 35,"
        "\"d\": {"
        "\"type\": \"emote_set.update\","
        "\"condition\": {"
        "\"object_id\": \"" +
        emote_set_id +
        "\""
        "}"
        "}"
        "}");
    log::info("7TV EventAPI", "Subcribing emote set ID " + emote_set_id);
  }

  void SevenTVClient::unsubscribe(const std::string &emote_set_id) {
    this->websocket.send(
        "{"
        "\"op\": 36,"
        "\"d\": {"
        "\"type\": \"emote_set.update\","
        "\"condition\": {"
        "\"object_id\": \"" +
        emote_set_id +
        "\""
        "}"
        "}"
        "}");
    log::info("7TV EventAPI", "Unsubscribing emote set ID " + emote_set_id);
  }
}