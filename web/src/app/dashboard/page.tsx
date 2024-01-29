"use client";

import AppNavbar from "@/components/Navbar";
import { SmallFooter } from "@/components/SmallFooter";
import { Avatar, Card, CardBody, CardFooter, Image, Skeleton, Spinner, Tab, Tabs } from "@nextui-org/react";
import { useCookies } from "next-client-cookies";
import { useEffect, useRef, useState } from "react";

export default function Page() {
    const cookies = useCookies();

    const [channels, setChannels] = useState(null);
    const [channelIndex, setChannelIndex] = useState(null);

    const [internalChannel, setInternalChannel] = useState(null);

    const firstInitialization = useRef(false);

    useEffect(() => {
        if (!firstInitialization.current) {
            const moderatingChannelsCookie = cookies.get("ttv_moderated_channels");
            const moderatingChannelIndexCookie = cookies.get("ttv_moderating_index");

            const moderatingChannels = JSON.parse(moderatingChannelsCookie);
            const moderatingChannelIndex = JSON.parse(moderatingChannelIndexCookie);

            setChannels(moderatingChannels);
            setChannelIndex(moderatingChannelIndex);
        }
    }, []);

    useEffect(() => {
        if (channels !== null && channelIndex !== null) {
            const channel = channels[channelIndex];

            fetch("http://0.0.0.0:8085/v1/channels/alias_id/" + channel.id)
                .then(response => response.json())
                .then(json => {
                    if (json.status_code === 200) {
                        setInternalChannel(json.data);
                        firstInitialization.current = true;
                    }
                })
                .catch((err) => console.error(err));
        }
    }, [channels, channelIndex]);

    // i dont know if it could be done better
    // but this is the first thing that came to my mind
    // for code that synchronizes index changes
    useEffect(() => {
        const checkIndexes = () => {
            const indexCookie = cookies.get("ttv_moderating_index");
            const index: number = JSON.parse(indexCookie);

            if (index !== channelIndex) {
                setChannelIndex(index);
            }
        };

        const interval = setInterval(checkIndexes, 1000);

        return () => clearInterval(interval);
    }, []);

    const tabs = [
        {
            name: "Events",
            content: (<EventListComponent data={null} />)
        }
    ];

    return (
        <main>
            <AppNavbar />
                    <div className="w-full min-h-screen flex flex-row justify-center align-center text-lg space-y-64">
                        <div className="w-full px-6 xl:px-0 xl:w-[50%] flex flex-col">
                        {
                            firstInitialization.current ?
                            (
                            <>
                                <div className="w-full flex flex-row py-4 space-x-4">
                                    <Avatar
                                        src={channels[channelIndex].profile_image_url}
                                    />
                                    <h1 className="text-4xl font-semibold">{channels[channelIndex].login}'s dashboard</h1>
                                </div>
                                <div className="w-full flex flex-row grow space-x-4">
                                    <Tabs color={"primary"} variant={"light"} aria-label="Dashboard options" classNames={{
                                        tabList: "flex-col w-full",
                                        base: "min-w-[20%]",
                                        panel: "p-0 grow",
                                        tab: "justify-start"
                                    }}>
                                        {
                                            tabs.map((v) => (
                                                <Tab key={v.name} title={v.name}>
                                                    <Card >
                                                        <CardBody>
                                                            {v.content}
                                                        </CardBody>
                                                    </Card>
                                                </Tab>
                                            ))
                                        }
                                    </Tabs>
                                </div>
                            </>
                            ):
                        (
                            <div className="flex flex-col w-full space-y-4">
                                <div className="w-full flex flex-row space-x-4">
                                    <Skeleton className="w-16 h-16 rounded-full" />
                                    <Skeleton className="grow h-16 rounded-large" />
                                </div>
                                <div className="w-full flex flex-row space-x-4">
                                    <div className="w-[20%] space-y-4">
                                        <Skeleton className="w-full h-8 rounded-large" />
                                        <Skeleton className="w-full h-8 rounded-large" />
                                        <Skeleton className="w-full h-8 rounded-large" />
                                    </div>
                                    <div className="grow">
                                        <Skeleton className="w-full h-full rounded-large" />
                                    </div>
                                </div>
                            </div>
                        )
                    }
                    </div>
                </div>
            <SmallFooter />
        </main>
    );
}

const EventListComponent = ({data}: {data: any[] | null}): JSX.Element => {
    return data ?
        (
            <div className="grid grid-cols-3 xl:grid-cols-5 gap-4">
            {
                data.map((v, i) => (
                    <Card
                        isFooterBlurred
                        shadow="sm"
                        isPressable
                        onPress={() => console.log("hi")}
                    >
                        <Image
                            src={"/bot_avatar.png"}
                            alt={v.alias_name + "'s pfp"}
                            width="100%"
                            radius="lg"
                            shadow="sm"
                            className="w-full object-cover h-fit"
                        />
                        <CardFooter className="text-small justify-between bg-slate-900/50 border-white/20 border-1 overflow-hidden py-1 absolute rounded-large bottom-1 w-[calc(100%_-_8px)] shadow-small ml-1 z-10">
                            <p className="text-stone-100 font-medium">{v.alias_name}</p>
                            <p className="text-teal-100">{v.event_type}</p>
                        </CardFooter>
                    </Card>
                ))
            }
            </div>
        )
        :
        (
            <div className="flex flex-col justify-center items-center space-y-4 w-full py-4">
                <Spinner />
            </div>
        )
    ;
};