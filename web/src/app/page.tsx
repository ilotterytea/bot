'use client'

import { ReactNode } from 'react';
import ChatBox from '@/components/ChatEmulation';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IconProp } from '@fortawesome/fontawesome-svg-core';
import { faBell, faClock, faKeyboard, faMessage } from '@fortawesome/free-solid-svg-icons';
import Footer from '@/components/Footer';
import AppNavbar from '@/components/Navbar';

export default function Home() {
  return (
    <main>
      <AppNavbar />
      <div className="w-full min-h-screen flex flex-col text-lg space-y-64">
        {
          // Join promotion
        }
        <div className="w-full min-h-screen flex flex-col justify-center items-center pb-24 pt-48 animate-mainscreen bg-gradient-to-b from-teal-200 to-teal-300">
          <div className="flex flex-col justify-center items-center text-slate-900">
            <h1 className="text-7xl font-medium my-4">Enhance your chat</h1>
            <p>A multi-utility Twitch chat bot that brings a lot of functionality into your chat.</p>
          </div>
          <div className="mt-24">
            <a href="/join">
              <div className="transition px-12 py-2 rounded-full bg-slate-900 hover:bg-teal-50 text-teal-400 hover:text-slate-800">
                <p className="text-2xl">Join</p>
              </div>
            </a>
          </div>
        </div>

        {
          // Big features
        }
        
        <div className="w-full flex flex-col items-center justify-center px-16 lg:px-0 [&>div:nth-child(even)]:flex-row-reverse">
          <div className="w-full lg:w-1/2 space-y-16">
            {
              // Stream notifications
            }

              <FeatureItem name="Stream notifications" description={
                <p>
                A convenient way to keep track of streams on Twitch.
                Simply use the <a href="/cmd/event" className="command">!event</a> command to create a notification in chat about an upcoming stream.
                To always be in the know, sign up for notifications using the <a href="/cmd/notify" className="command">!notify</a> command.
                Never miss a moment and enjoy your streams on time!
                </p>
              }>
                <ChatBox animate={true} lines={[
                  {
                    "name": "ImTeaBot",
                    "msg": "⚡ forsen live!"
                  },
                  {
                    "name": "ImTeaBot",
                    "msg": "⚡ psp live yippeeeee · @juh, @buh"
                  },
                  {
                    "name": "ImTeaBot",
                    "msg": "⚡ yo ! · @cuh"
                  }
                ]}
                />
              </FeatureItem>

            {
              // Emote updates
            }

              <FeatureItem name="Emote updates" description={
                <p>
                Instant updates on new changes to the emoji set.
                The bot automatically notifies the chat about new emotes, deletion of existing emotes, and changes in their names.<br />
                <span className="font-bold text-amber-500">For now only for 7TV.</span>
                </p>
              }>
                {
                  // TODO: Represent emotes as image instead of text.
                }
                <ChatBox animate={false} lines={[
                  {
                    "name": "ImTeaBot",
                    "msg": "(7TV) EmoteExpert added the buh emote"
                  },
                  {
                    "name": "ImTeaBot",
                    "msg": "(7TV) EmoteExpert updated the emote name from buh to buuuh"
                  },
                  {
                    "name": "ImTeaBot",
                    "msg": "(7TV) EmoteExpert removed the buuuh emote"
                  }

                ]}
                />
              </FeatureItem>
          </div>
        </div>

        {
          // Small features
        }

        <div className="w-full flex flex-col justify-center items-center">
          <div className="w-full lg:w-1/2 px-16 lg:px-0 grid grid-cols-2 gap-6">
            <SmallFeatureItem
              name="@everyone in your chat"
              description={(
              <p>
                With the "<a href="/cmd/massping" className="command">!massping</a>" command,
                moderators can quickly notify all participants by mentioning them with an important message.
              </p>
              )}
              icon={(<SmallFeatureIcon icon={faBell} class_name={"from-teal-800 to-teal-600"}/>)}
            />

            <SmallFeatureItem
              name="No Ctrl+Enter anymore"
              description={(
              <p>
                In addition, the "<a href="/cmd/spam" className="command">!spam</a>" command provides the ability to repeat a message the desired number of times,
                which is useful for repetitive reminders or clarifications.
              </p>
              )}
              icon={(<SmallFeatureIcon icon={faKeyboard} class_name={"from-amber-600 to-amber-800"} />)}
            />

            <SmallFeatureItem
              name="Timers"
              description={(
              <p>
                With the <a href="/cmd/timer" className="command">!timer</a> command, you can set messages to be sent each interval.
                moderators can quickly notify all participants by mentioning them with an important message.
              </p>
              )}
              icon={(<SmallFeatureIcon icon={faClock} class_name={"from-cyan-800 to-cyan-600"} />)}
            />

            <SmallFeatureItem
              name="Create your own commands"
              description={(
              <p>
                The <a href="/cmd/cmd" className="command">!cmd</a> command allow the creation of personalized commands,
                which adds interactivity and flexibility to communication by allowing channel owners to create their own commands.
              </p>
              )}
              icon={(<SmallFeatureIcon icon={faMessage} class_name={"from-fuchsia-600 to-fuchsia-800"} />)}
            />


          </div>
        </div>

        {
          // Last warning 
        }

        <div className="w-full flex flex-col justify-center items-center space-y-16">
          <h1 className="font-semibold text-7xl text-red-950 dark:text-red-100">Last chance...</h1>
            <a href="/join">
              <div className="transition px-12 py-2 rounded-full bg-slate-900 hover:bg-red-50 text-red-50 hover:text-slate-800">
                <p className="text-2xl">Join. Now.</p>
              </div>
            </a>
        </div>

        <Footer />

      </div>
    </main>
  )
}

const FeatureItem = (
  {name, description, children}:
  {name: ReactNode, description: ReactNode, children: ReactNode}
): JSX.Element => (
  <div className="w-full flex flex-row justify-between items-center">
    <div className="px-6">
      <h1 className="text-4xl font-medium mb-6">{name}</h1>
      {description}
    </div>
    <div>
      {children}
    </div>
  </div>
);

const SmallFeatureItem = (
  {name, description, icon}:
  {name: ReactNode, description: ReactNode, icon: ReactNode}
): JSX.Element => (
  <div className="flex flex-col min-w-80 p-8 space-y-4 bg-neutral-200 dark:bg-zinc-800 rounded-2xl border-2 border-neutral-300 dark:border-neutral-950">
    {icon}
    
    
    <h1 className="text-2xl font-medium">{name}</h1>
    {description}
    
  </div>
);

const SmallFeatureIcon = (
  {icon, class_name}:
  {icon: IconProp, class_name: string | null}
): JSX.Element => (
  <div className={"w-12 h-12 flex justify-center items-center rounded-lg text-neutral-100 bg-gradient-to-r " + class_name}>
      <FontAwesomeIcon
        icon={icon}
        className="text-2xl"
      />
  </div>
);