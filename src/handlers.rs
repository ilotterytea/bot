use crate::arguments::Arguments;

pub async fn irc_message_handler(args: Arguments) {
    if args.message.message_text == "test" {
        args.client
            .say(args.message.channel_login.to_owned(), "test !!!".to_owned())
            .await
            .expect("Unable to send a message to chat!");
    }

    let response = args.loader.run(&args.message.message_text);

    if !&response.is_none() {
        args.client
            .say(
                args.message.channel_login.to_owned(),
                response.unwrap().to_owned(),
            )
            .await
            .expect("Unable to send a message to chat!");
    }
}
