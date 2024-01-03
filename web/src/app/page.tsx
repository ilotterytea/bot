import Image from 'next/image'
import Navbar from '@/components/Navbar'

export default function Home() {
  return (
    <main className="text-slate-800">
      <Navbar />
      <div className="flex w-screen justify-center">
        <div className="flex flex-col max-w-3xl">
          <div className="flex flex-row w-full bg-gray-100 p-5 border-solid border-2 border-slate-300 my-4">
            <div className="h-full mr-1">
                <Image
                    src="/bot_avatar.png"
                    width={128}
                    height={128}
                    style={{width: 'auto', height: '100%'}}
                    alt="Logo"
                />
            </div>
            <div className="w-full h-full justify-center">
              <p>
                <a href="https://twitch.tv/imteabot" className="text-teal-500 font-bold">@imteabot</a> is a multi-utility Twitch chat bot
                that brings a lot of functionality into your chat.
                Stream notifications, emote updates and more...
              </p>
              <p>
                Type <a href="/cmd/join" className="command">!join</a> into any chat room that has
                <a href="https://twitch.tv/imteabot" className="text-teal-500 font-bold"> @imteabot</a> or just <a href="/login" className="text-teal-500 font-bold">log in</a> if you want this bot
              </p>
            </div>
          </div>

          <div className="flex justify-center items-center align-middle">
            <h1 className="text-teal-500 font-bold text-4xl">Features</h1>
          </div>

          <div className="[&>*:nth-child(odd)]:bg-gray-200 [&>*]:bg-gray-100 [&>*:nth-child(odd)]:flex-row-reverse [&>*]:items-center">
            <div className="flex flex-row items-center w-full bg-gray-100 p-5 border-solid border-2 border-slate-300 my-4">
              <div className="h-full mr-1">
                  <Image
                      src="/stream.gif"
                      width={128}
                      height={128}
                      style={{width: 'auto', height: '100%'}}
                      alt="Logo"
                  />
              </div>
              <div className="w-full h-full justify-center">
              <h1 className="text-teal-500 font-bold text-2xl">Stream notifications</h1>
                <p>
                A convenient way to keep track of streams on Twitch.
                Simply use the <a href="/cmd/event" className="command">!event</a> command to create a notification in chat about an upcoming stream.
                To always be in the know, sign up for notifications using the <a href="/cmd/notify" className="command">!notify</a> command.
                Never miss a moment and enjoy your streams on time!
                </p>
              </div>
            </div>

            <div className="flex flex-row w-full bg-gray-100 p-5 border-solid border-2 border-slate-300 my-4">
              <div className="h-full mr-1">
                  <Image
                      src="/emotes.gif"
                      width={128}
                      height={128}
                      style={{width: 'auto', height: '100%'}}
                      alt="Logo"
                  />
              </div>
              <div className="w-full h-full justify-center">
              <h1 className="text-teal-500 font-bold text-2xl">Emote updates</h1>
                <p>
                Instant updates on new changes to the emoji set.
                The bot automatically notifies the chat about new emotes, deletion of existing emotes, and changes in their names.
                </p>
                <p className="font-bold text-amber-500">For now only for 7TV.</p>
              </div>
            </div>

            <div className="flex flex-row w-full bg-gray-100 p-5 border-solid border-2 border-slate-300 my-4">
              <div className="h-full mr-1">
                  <Image
                      src="/enhance.gif"
                      width={128}
                      height={128}
                      style={{width: 'auto', height: '100%'}}
                      alt="Logo"
                  />
              </div>
              <div className="w-full h-full justify-center">
              <h1 className="text-teal-500 font-bold text-2xl">Enhance your chat</h1>
                <p>
                With the <a href="/cmd/timer" className="command">!timer</a> command, you can set messages to be sent each interval.
                The <a href="/cmd/cmd" className="command">!cmd</a> command allow the creation of personalized commands,
                which adds interactivity and flexibility to communication by allowing channel owners to create their own commands.
                </p>
              </div>
            </div>

            <div className="flex flex-row w-full bg-gray-100 p-5 border-solid border-2 border-slate-300 my-4">
              <div className="h-full mr-1">
                  <Image
                      src="/moderation.gif"
                      width={128}
                      height={128}
                      style={{width: 'auto', height: '100%'}}
                      alt="Logo"
                  />
              </div>
              <div className="w-full h-full justify-center">
              <h1 className="text-teal-500 font-bold text-2xl">Moderation stuff</h1>
                <p>
                  Functionality for moderators includes handy tools for maintaining order in the chat room.
                </p>
                <p>
                  With the "<a href="/cmd/massping" className="command">!massping</a>" command, moderators can quickly notify all participants by mentioning them with an important message.
                </p>
                <p>
                  In addition, the "<a href="/cmd/spam" className="command">!spam</a>" command provides the ability to repeat a message the desired number of times,
                  which is useful for repetitive reminders or clarifications.
                </p>
              </div>
            </div>

            <div className="flex flex-row w-full bg-gray-100 p-5 border-solid border-2 border-slate-300 my-4">
              <div className="h-full mr-1">
                  <Image
                      src="/soon.gif"
                      width={128}
                      height={128}
                      style={{width: 'auto', height: '100%'}}
                      alt="Logo"
                  />
              </div>
              <div className="w-full h-full justify-center">
              <h1 className="text-teal-500 font-bold text-2xl">More and coming soon...</h1>
                <p>
                  You can find out more commands in the <a href="/cmd" className="text-teal-500">"commands"</a> tab.
                </p>
                <p>
                  The bot is at Beta stage of development, so there is very little functions so far.
                  <a href="https://github.com/ilotterytea/bot" className="text-teal-500"> You can follow the development of the bot on GitHub.</a>
                </p>
              </div>
            </div>
          </div>


        </div>
      </div>
    </main>
  )
}
