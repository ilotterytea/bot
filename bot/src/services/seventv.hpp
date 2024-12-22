#pragma once

#include <string>
#include <unordered_map>

#include "../config.hpp"
#include "../irc/client.hpp"
#include "../localization/localization.hpp"
#include "ixwebsocket/IXWebSocket.h"
#include "nlohmann/json.hpp"

namespace bot::services {
  class SevenTVClient {
    public:
      SevenTVClient(irc::Client &irc_client,
                    const loc::Localization &localization,
                    const Configuration &configuration)
          : irc_client(irc_client),
            localization(localization),
            configuration(configuration) {
        this->websocket.setUrl("wss://events.7tv.io/v3");
      }
      ~SevenTVClient() {}

      void run();

    private:
      void parse_message(const std::string &message);

      void handle_hello_event(const nlohmann::json &json);
      void handle_dispatch_event(const nlohmann::json &json);
      void handle_close_event();

      void subscribe_new_channels();
      void join(const std::string &id);
      void part(const std::string &id);
      void subscribe(const std::string &emote_set_id);
      void unsubscribe(const std::string &emote_set_id);

      ix::WebSocket websocket;
      std::unordered_map<std::string, std::string> ids;

      irc::Client &irc_client;
      const loc::Localization &localization;
      const Configuration &configuration;
  };
}