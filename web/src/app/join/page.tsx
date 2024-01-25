"use client";

import AppNavbar from "@/components/Navbar";
import { SmallFooter } from "@/components/SmallFooter";
import { Card, CardBody, CardFooter, Image, Skeleton } from "@nextui-org/react";
import { useCookies } from "next-client-cookies";
import { useEffect, useRef, useState } from "react";

export default function Page() {
    const cookies = useCookies();
    const [channels, setChannels] = useState();
    const initialized = useRef(false);

    useEffect(() => {
        if (!initialized.current) {
            const originalUserCookie = cookies.get("twitch_user_data");
            const originalUser = JSON.parse(originalUserCookie); 

            const clientId = cookies.get("client_id");
            const token = cookies.get("token");

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
                            setChannels(json.data);
                            initialized.current = true;
                        })
                        .catch((err) => console.error(err));

                })
                .catch((err) => console.error(err));
        }
    }, []);

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
                            channels ?
                            channels.map((value, index) => (
                                <UserCard name={value.login} pfp={value.profile_image_url} id={3} key={index} />
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

const UserCard = ({name, pfp, id, key}: {name: string, pfp: string, id: number, key: number}): JSX.Element => {
    return (
        <Card shadow="sm" key={key} isPressable>
            <CardBody className="overflow-visible p-0">
                <Image
                    src={pfp}
                    alt={name + "'s pfp"}
                    width="100%"
                    radius="lg"
                    shadow="sm"
                    className="w-full object-cover h-fit"
                />
            </CardBody>
            <CardFooter className="text-small">
                <p>{name}</p>
            </CardFooter>
        </Card>
    );
}