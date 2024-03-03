'use client'

import { ReactNode } from 'react';
import ChatBox from '@/components/ChatEmulation';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { IconProp } from '@fortawesome/fontawesome-svg-core';
import { faBell, faClock, faKeyboard, faMessage, faRightToBracket } from '@fortawesome/free-solid-svg-icons';
import Image from 'next/image';
import Link from 'next/link';
import { Button } from '@nextui-org/react';

export default function Home() {
  return (
    <div className="flex flex-col">
      <div className="flex w-full grow">
        <div className="grow space-y-10">
          
          {
            // Introduction
          }
          <div className="w-full flex flex-row justify-around items-center lg:px-8 py-[96px] bg-gradient-to-t from-green-500 to-teal-200 animate-mainscreen shadow-lg">
            <div className="hidden lg:flex justify-center items-center">
              <Image
                src={"/bot_avatar.png"}
                width={256}
                height={256}
                alt="bot's avatar"
              />
            </div>
            <div className="flex flex-col space-y-8 text-center lg:text-right lg:w-[50%]">
              <div>
                <h1 className="text-3xl font-medium [text-shadow:_1px_1px_3px_rgb(0_0_0_/_40%)]">Enhance your chat with <span className="text-rose-800">{process.env.NEXT_PUBLIC_BOT_USERNAME ?? "the bot"}</span></h1>
                <p className="text-md [text-shadow:_1px_1px_3px_rgb(0_0_0_/_40%)]">A multi-utility Twitch chat bot that brings<br/>a lot of functionality into your chat.</p>
              </div>
              <Link href={"/wiki/join"} className="h-[64px]">
                <Button className="bg-rose-800 text-stone-50 h-full text-lg font-anta rounded-lg shadow-lg [text-shadow:_1px_1px_3px_rgb(0_0_0_/_40%)]">
                  <FontAwesomeIcon icon={faRightToBracket} /> Add to my chat room
                </Button>
              </Link>
            </div>
          </div>
          
          {
            // Features
          }
          <div className="flex flex-col space-y-16 shadow-lg bg-stone-200 dark:bg-stone-800">
            {
              // Features header
            }
            <div className="w-full flex justify-center items-center p-6 bg-stone-300 dark:bg-green-400 font-anta">
              <h1 className="text-4xl font-semibold text-stone-800 dark:text-green-900 [text-shadow:_1px_1px_3px_rgb(0_0_0_/_40%)]">Features</h1>
            </div>
            {
              // Feature list
            }
            <div className="p-8 space-y-32">
              {
                // Big features
              }
              <div className="w-full flex flex-col px-16 lg:px-0">
                <div className="w-full space-y-16">
                  {
                    // Stream notifications
                  }
                    <FeatureItem name="Stream notifications" description={
                      <p>
                      A convenient way to keep track of streams on Twitch.
                      Simply use the <a href="/wiki/stream/events" className="command">!event</a> command to create a notification in chat about an upcoming stream.
                      To always be in the know, sign up for notifications using the <a href="/wiki/stream/notifications" className="command">!notify</a> command.
                      Never miss a moment and enjoy your streams on time!
                      </p>
                    }>
                      <ChatBox animate={true} lines={[
                        {
                          "name": process.env.NEXT_PUBLIC_BOT_USERNAME ?? "ThisBot",
                          "msg": "⚡ forsen live!"
                        },
                        {
                          "name": process.env.NEXT_PUBLIC_BOT_USERNAME ?? "ThisBot",
                          "msg": "⚡ psp live yippeeeee · @juh, @buh"
                        },
                        {
                          "name": process.env.NEXT_PUBLIC_BOT_USERNAME ?? "ThisBot",
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
                      <span className="font-bold text-amber-700 dark:text-amber-500">For now only for 7TV.</span>
                      </p>
                    }>
                      {
                        // TODO: Represent emotes as image instead of text.
                      }
                      <ChatBox animate={false} lines={[
                        {
                          "name": process.env.NEXT_PUBLIC_BOT_USERNAME ?? "ThisBot",
                          "msg": "(7TV) EmoteExpert added the buh emote"
                        },
                        {
                          "name": process.env.NEXT_PUBLIC_BOT_USERNAME ?? "ThisBot",
                          "msg": "(7TV) EmoteExpert updated the emote name from buh to buuuh"
                        },
                        {
                          "name": process.env.NEXT_PUBLIC_BOT_USERNAME ?? "ThisBot",
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
                <div className="w-full x-16 lg:px-0 flex flex-col space-y-6 lg:space-y-0 lg:grid lg:grid-cols-2 lg:gap-6">
                  <SmallFeatureItem
                    name="@everyone in your chat"
                    description={(
                    <p>
                      With the &quot;<a href="/wiki/mod/mass-ping" className="command">!massping</a>&quot; command,
                      moderators can quickly notify all participants by mentioning them with an important message.
                    </p>
                    )}
                    icon={(<SmallFeatureIcon icon={faBell} class_name={"from-teal-800 to-teal-600"}/>)}
                  />

                  <SmallFeatureItem
                    name="No Ctrl+Enter anymore"
                    description={(
                    <p>
                      In addition, the &quot;<a href="/wiki/mod/spam" className="command">!spam</a>&quot; command provides the ability to repeat a message the desired number of times,
                      which is useful for repetitive reminders or clarifications.
                    </p>
                    )}
                    icon={(<SmallFeatureIcon icon={faKeyboard} class_name={"from-amber-600 to-amber-800"} />)}
                  />

                  <SmallFeatureItem
                    name="Timers"
                    description={(
                    <p>
                      With the <a href="/wiki/channel/timer" className="command">!timer</a> command, you can set messages to be sent each interval.
                      moderators can quickly notify all participants by mentioning them with an important message.
                    </p>
                    )}
                    icon={(<SmallFeatureIcon icon={faClock} class_name={"from-cyan-800 to-cyan-600"} />)}
                  />

                  <SmallFeatureItem
                    name="Create your own commands"
                    description={(
                    <p>
                      The <a href="/wiki/channel/custom-commands" className="command">!cmd</a> command allow the creation of personalized commands,
                      which adds interactivity and flexibility to communication by allowing channel owners to create their own commands.
                    </p>
                    )}
                    icon={(<SmallFeatureIcon icon={faMessage} class_name={"from-fuchsia-600 to-fuchsia-800"} />)}
                  />


                </div>
              </div>
            </div>
            
          </div>
        </div>
      </div>
    </div>
  );
}

const FeatureItem = (
  {name, description, children}:
  {name: ReactNode, description: ReactNode, children: ReactNode}
): JSX.Element => (
  <div className="w-full flex flex-col lg:flex-row justify-between items-center">
    <div className="px-6">
      <h1 className="text-4xl font-medium mb-6 dark:text-teal-300">
        {name}
      </h1>
      {description}
    </div>
    <div className="my-4 lg:my-0">
      {children}
    </div>
  </div>
);

const SmallFeatureItem = (
  {name, description, icon}:
  {name: ReactNode, description: ReactNode, icon: ReactNode}
): JSX.Element => (
  <div className="flex flex-col min-w-80 p-8 space-y-4 rounded-lg bg-stone-300 dark:bg-stone-700 border-1 border-stone-300 dark:border-stone-900 shadow-[0_0px_4px_0px_rgba(0,0,0,0.3)]">
    {icon}
    <h1 className="text-2xl font-medium dark:text-teal-200">
      {name}
    </h1>
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