"use client";

import AppNavbar from "@/components/Navbar";
import { SmallFooter } from "@/components/SmallFooter";
import { Card, CardBody, CardFooter, Image, Skeleton, Spinner, Tooltip } from "@nextui-org/react";
import { useCookies } from "next-client-cookies";
import { useEffect, useRef, useState } from "react";

export default function Page() {
    const cookies = useCookies();
    const [channels, setChannels] = useState([]);
    const initialized = useRef(false);

    useEffect(() => {
        if (!initialized.current) {
            const originalUserCookie = cookies.get("twitch_user_data");
            const originalUser = JSON.parse(originalUserCookie); 

            const clientId = cookies.get("ttv_client_id");
            const token = cookies.get("ttv_token");

            const headers = {
                "Client-Id": clientId,
                "Authorization": "Bearer " + token
            };

            fetch(
                "https://api.twitch.tv/helix/moderation/channels?user_id=" + originalUser.id,
                {
                    headers: headers
                }
            )
                .then(response => response.json())
                .then(broadcaster_data => {
                    const channelIds = broadcaster_data.data.map((v) => "id=" + v.broadcaster_id).join("&");

                    fetch(
                        "https://api.twitch.tv/helix/users?" + channelIds,
                        {
                            headers: headers
                        }
                        )
                        .then(response => response.json())
                        .then(json => {
                            json.data.push(originalUser);

                            const tc = json.data;

                            const ids = json.data.map((v) => v.id);

                            fetch("http://0.0.0.0:8085/v1/channels/alias_id/" + ids.join(","))
                                .then(response => response.json())
                                .then(data => {
                                    const c = data.data;

                                    let cc = tc.map((x) => {
                                        x.already_joined = c.some((y) => y.alias_id === Number(x.id) && !y.opt_outed_at);
                                        return x;
                                    });

                                    cc.sort((a, b) => {
                                        return b.already_joined === a.already_joined ? 0 : b.already_joined ? -1 : 1;
                                    });

                                    setChannels(cc);
                                })
                                .catch((err) => console.error(err));
                        })
                        .catch((err) => console.error(err));

                })
                .catch((err) => console.error(err));


            initialized.current = true;
        }
    }, []);

    // "me when i'm in a worst code competition and my opponent is ilotterytea's code" ahhh code
    useEffect(() => {
        const channelsToJoin = channels.filter((v) => v.is_in_process);

        if (channelsToJoin.length !== 0) {
            const token = cookies.get("client_token");

            for (const channel of channelsToJoin) {
                fetch("http://0.0.0.0:8085/v1/channels/join", {
                    method: "POST",
                    headers: {
                        "Authorization": token,
                        "Content-Type": "application/json",
                        "Accept": "application/json"
                    },
                    body: JSON.stringify({alias_id: Number(channel.id)}),
                })
                .then(response => response.json())
                .then(() => {
                    const channelIndex = channels.findIndex((v) => v.id === channel.id);

                    if (channelIndex !== -1 && channels[channelIndex].is_in_process) {
                        const updatedChannels = [
                            channels[channelIndex],
                            ...channels.slice(0, channelIndex),
                            ...channels.slice(channelIndex + 1)
                        ];

                        updatedChannels[0].is_in_process = false;
                        updatedChannels[0].already_joined = true;

                        setChannels(updatedChannels);
                    }
                })
                .catch((err) => console.error(err));
            }
        }
    }, [channels]);

    const joinChannel = (alias_id: string) => {
        const channelIndex = channels.findIndex((v) => v.id === alias_id);

        if (channelIndex !== -1 && !channels[channelIndex].is_in_process && !channels[channelIndex].already_joined) {
            const updatedChannels = [
                channels[channelIndex],
                ...channels.slice(0, channelIndex),
                ...channels.slice(channelIndex + 1)
            ];

            updatedChannels[0].is_in_process = true;

            setChannels(updatedChannels);
        }
    };

    return (
        <main>
            <AppNavbar />
            <div className="w-full min-h-screen flex justify-center align-center text-lg space-y-64">
                <div className="w-[50%] flex flex-col">
                    <div className="my-16 flex w-full flex-col justify-center align-center">
                        <h1 className="text-4xl font-medium">What channel do you want to add the bot to?</h1>
                        <p>Here's a list of channels you are a moderator on:</p>
                    </div>
                    <div className="grid grid-cols-3 xl:grid-cols-6 gap-4">
                        {
                            channels.length > 0 ?
                            channels.map((value, index) => (
                                <Tooltip key={index} color={"success"} content={value.already_joined ? "Already joined!" : "uuh"} isDisabled={!value.already_joined}>
                                    <Card
                                        isFooterBlurred
                                        shadow="sm"
                                        isPressable={!value.is_in_process || !value.already_joined}
                                        isDisabled={value.is_in_process || value.already_joined}
                                        onPress={() => joinChannel(value.id)}
                                        className={value.already_joined ? "joined-channel" : "not-joined-channel"}
                                    >
                                        {
                                            value.is_in_process ?
                                            (
                                                <Spinner size={"lg"} className="w-full h-full absolute z-50"/>
                                            )
                                            :
                                            (<></>)
                                        }
                                        <Image
                                            src={value.profile_image_url}
                                            alt={value.login + "'s pfp"}
                                            width="100%"
                                            radius="lg"
                                            shadow="sm"
                                            className="w-full object-cover h-fit"
                                        />
                                        <CardFooter className="text-small justify-center bg-slate-900/50 border-white/20 border-1 overflow-hidden py-1 absolute before:rounded-xl rounded-large bottom-1 w-[calc(100%_-_8px)] shadow-small ml-1 z-10">
                                            <p className="text-stone-100">{value.login}</p>
                                        </CardFooter>
                                    </Card>
                                </Tooltip>
                            ))
                            :
                            Array.from({length: 6}).map((_, i) => (
                                <Card shadow="sm" key={i}>
                                    <CardBody className="overflow-visible p-0">
                                        <Skeleton className="rounded-lg w-full object-cover h-[140px]">
                                            <div className="w-full h-full rounded-lg bg-default-300" />
                                        </Skeleton>
                                    </CardBody>
                                    <CardFooter>
                                        <Skeleton className="rounded-lg w-full object-cover h-[32px]">
                                            <div className="w-full h-full rounded-lg bg-default-300" />
                                        </Skeleton>
                                    </CardFooter>
                                </Card>
                            ))
                        }
                    </div>
                </div>
            </div>
            <SmallFooter />
        </main>
    )
}